#!/usr/bin/env python3
"""Backfill hiring_history.json for applications marked hired without UI history."""
import hashlib
import json
import uuid
from datetime import datetime, timedelta, timezone
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1] / "web" / "src" / "main" / "webapp" / "WEB-INF" / "data"


def parse_iso(value: str) -> datetime:
    text = value.strip()
    if text.endswith("Z"):
        text = text[:-1] + "+00:00"
    return datetime.fromisoformat(text).astimezone(timezone.utc)


def parse_deadline_end(value: str) -> datetime:
    day = datetime.fromisoformat(value.strip()).date()
    return datetime(day.year, day.month, day.day, 23, 59, 59, tzinfo=timezone.utc)


def compute_submitted_at(applied_at: str, deadline: str, application_id: str) -> str:
    applied = parse_iso(applied_at)
    deadline_end = parse_deadline_end(deadline)

    if applied >= deadline_end:
        # Application after deadline: only guarantee hire time is after application.
        submitted = applied + timedelta(hours=1)
    else:
        gap = deadline_end - applied
        gap_days = max(gap.days, 0)
        seed = int(hashlib.sha256(application_id.encode("utf-8")).hexdigest()[:8], 16)
        offset_days = min(max(1, gap_days // 3 if gap_days else 1), 7)
        offset_days = 1 + (seed % offset_days)
        submitted = applied + timedelta(days=offset_days, hours=2 + (seed % 6))
        if submitted >= deadline_end:
            submitted = applied + gap / 2
        if submitted >= deadline_end:
            submitted = deadline_end - timedelta(minutes=1)
        if submitted <= applied:
            submitted = applied + timedelta(hours=1)

    return submitted.strftime("%Y-%m-%dT%H:%M:%S.000Z")


def collect_covered_application_ids(history: list) -> set[str]:
    covered = set()
    for record in history:
        for app_id in record.get("hiredApplicationIds") or []:
            if app_id:
                covered.add(app_id)
    return covered


def build_manual_hire_records(
    applications: list,
    jobs: list,
    existing_history: list | None = None,
) -> tuple[list, int]:
    job_by_id = {job["id"]: job for job in jobs if job.get("id")}
    history = list(existing_history or [])
    covered = collect_covered_application_ids(history)
    created = 0

    for app in applications:
        if not app.get("active", True):
            continue
        if str(app.get("status", "")).lower() != "hired":
            continue

        app_id = app.get("id")
        if not app_id or app_id in covered:
            continue

        job = job_by_id.get(app.get("jobId"))
        if not job:
            print(f"  skip {app_id}: job {app.get('jobId')} not found")
            continue

        teacher_id = job.get("teacherId")
        deadline = job.get("deadline")
        applied_at = app.get("appliedAt")
        if not teacher_id or not deadline or not applied_at:
            print(f"  skip {app_id}: missing teacherId/deadline/appliedAt")
            continue

        student_name = app.get("studentName") or app.get("studentId") or "Unknown"
        record = {
            "id": "hist_" + uuid.uuid5(uuid.NAMESPACE_DNS, f"manual_hire:{app_id}").hex,
            "jobId": app["jobId"],
            "moId": teacher_id,
            "action": "manual_hire",
            "submittedAt": compute_submitted_at(applied_at, deadline, app_id),
            "hiredApplicationIds": [app_id],
            "hiredStudentNames": [student_name],
        }
        history.append(record)
        covered.add(app_id)
        created += 1

    return history, created


def main() -> None:
    apps_path = ROOT / "applications.json"
    jobs_path = ROOT / "jobs.json"
    history_path = ROOT / "hiring_history.json"

    applications = json.loads(apps_path.read_text(encoding="utf-8"))
    jobs = json.loads(jobs_path.read_text(encoding="utf-8"))

    existing: list = []
    if history_path.exists():
        existing = json.loads(history_path.read_text(encoding="utf-8")) or []

    history, created = build_manual_hire_records(applications, jobs, existing)
    history_path.write_text(json.dumps(history, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")

    hired_count = sum(
        1
        for app in applications
        if app.get("active", True) and str(app.get("status", "")).lower() == "hired"
    )
    print(f"Hiring history: {len(history)} total ({created} new), {hired_count} hired applications")
    print(f"Wrote {history_path}")


if __name__ == "__main__":
    main()
