#!/usr/bin/env python3
"""Generate demo JSON data for TA system."""
import json
import random
from datetime import date, datetime, timedelta, timezone
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1] / "web" / "src" / "main" / "webapp" / "WEB-INF" / "data"
TODAY = date(2026, 5, 17)
random.seed(42)

DEPARTMENTS = [
    "Computer Science",
    "Electronic Engineering",
    "Mathematics",
    "Business School",
    "Law",
    "Psychology",
    "Chemistry",
]

FIRST = [
    "Alex", "Jordan", "Taylor", "Morgan", "Casey", "Riley", "Jamie", "Quinn", "Avery", "Skyler",
    "Wei", "Ming", "Yuki", "Hana", "Omar", "Sara", "Liam", "Emma", "Noah", "Olivia",
    "Ethan", "Sophia", "Lucas", "Mia", "Henry", "Amelia", "Jack", "Isabella", "Leo", "Charlotte",
]
LAST = [
    "Chen", "Wang", "Li", "Zhang", "Liu", "Brown", "Smith", "Johnson", "Williams", "Jones",
    "Garcia", "Miller", "Davis", "Wilson", "Moore", "Taylor", "Anderson", "Thomas", "Jackson", "White",
]
MODULE_PREFIX = ["EBU", "ECS", "MAT", "BUS", "LAW", "PSY", "CHM"]
TITLES = [
    "Teaching Assistant",
    "Lab Demonstrator",
    "Tutorial Support",
    "Course Support Assistant",
    "Marking Assistant",
]

def iso_z(d: date, hour=10) -> str:
    return datetime(d.year, d.month, d.day, hour, 0, 0, tzinfo=timezone.utc).strftime("%Y-%m-%dT%H:%M:%S.000Z")

def iso_day(d: date) -> str:
    return d.isoformat()


def main():
    users = []
    students = []

    # Demo accounts (unchanged logins)
    users.append({
        "id": "stu001", "name": "Alex Chen", "email": "student@demo.com",
        "password": "demo123", "role": "student", "studentId": "201234567", "programme": "MSc Computer Science",
    })
    students.append({
        "userId": "stu001", "studentId": "201234567", "name": "Alex Chen", "email": "student@demo.com",
        "programme": "MSc Computer Science", "skills": "Java,Python,C", "experience": "", "attachments": [],
    })
    users.append({
        "id": "mo001", "name": "Dr. Smith", "email": "teacher@demo.com",
        "password": "demo123", "role": "teacher", "studentId": "", "programme": "",
    })
    users.append({
        "id": "adm001", "name": "Admin User", "email": "admin@demo.com",
        "password": "demo123", "role": "admin", "studentId": "", "programme": "",
    })

    teacher_profiles = [("mo001", "Dr. Smith")]
    for i in range(2, 21):
        tid = f"tch{i:03d}"
        first = random.choice(FIRST)
        last = random.choice(LAST)
        name = f"Dr. {first} {last}" if i % 3 else f"Prof. {first} {last}"
        email = f"teacher{i:02d}@demo.qmul.ac.uk"
        users.append({
            "id": tid, "name": name, "email": email,
            "password": "demo123", "role": "teacher", "studentId": "", "programme": "",
        })
        teacher_profiles.append((tid, name))

    student_profiles = [("stu001", "Alex Chen", "201234567")]
    for i in range(2, 51):
        sid = f"stu{i:03d}"
        first = random.choice(FIRST)
        last = random.choice(LAST)
        name = f"{first} {last}"
        student_no = f"20{100000 + i}"
        email = f"student{i:02d}@demo.qmul.ac.uk"
        prog = random.choice([
            "MSc Computer Science", "BSc Electronic Engineering", "MSc Data Science",
            "BSc Mathematics", "MSc Business Analytics", "BSc Psychology",
        ])
        users.append({
            "id": sid, "name": name, "email": email,
            "password": "demo123", "role": "student", "studentId": student_no, "programme": prog,
        })
        students.append({
            "userId": sid, "studentId": student_no, "name": name, "email": email,
            "programme": prog,
            "skills": random.choice(["Java,Python", "C++,MATLAB", "SQL,R", "Python,ML", "Java,Spring"]),
            "experience": random.choice(["", "Previous TA for labs", "Tutoring experience"]),
            "attachments": [],
        })
        student_profiles.append((sid, name, student_no))

    jobs = []
    job_id = 100
    open_published_jobs = []

    for t_idx, (tid, tname) in enumerate(teacher_profiles):
        n_jobs = random.randint(1, 5)
        for j in range(n_jobs):
            job_id += 1
            jid = str(job_id)
            dept = DEPARTMENTS[(t_idx + j) % len(DEPARTMENTS)]
            mod = f"{MODULE_PREFIX[(t_idx + j) % len(MODULE_PREFIX)]}{6000 + job_id % 900}"
            title = f"{mod} {random.choice(TITLES)}"
            positions = random.randint(1, 3)
            hours = random.randint(6, 12)

            pub_offset = random.randint(5, 45)
            published_at = TODAY - timedelta(days=pub_offset)
            created_at = published_at - timedelta(days=random.randint(1, 5))

            # Mix statuses: ~15% pending, ~10% rejected, rest published open/closed/overdue
            roll = random.random()
            if roll < 0.12:
                approval = "pending"
                status = "draft"
                published = False
                published_at_d = None
                deadline = TODAY + timedelta(days=random.randint(14, 60))
            elif roll < 0.20:
                approval = "rejected"
                status = "draft"
                published = False
                published_at_d = None
                deadline = TODAY + timedelta(days=random.randint(7, 30))
            else:
                approval = "approved"
                published = True
                published_at_d = published_at
                # ~25% overdue among published open jobs
                if random.random() < 0.28:
                    deadline = TODAY - timedelta(days=random.randint(3, 45))
                    status = "open"
                elif random.random() < 0.15:
                    deadline = TODAY + timedelta(days=random.randint(20, 90))
                    status = "closed"
                else:
                    deadline = TODAY + timedelta(days=random.randint(5, 40))
                    status = "open"

            job = {
                "id": jid,
                "teacherId": tid,
                "teacherName": tname,
                "moduleCode": mod,
                "title": title,
                "hours": hours,
                "positions": positions,
                "status": status,
                "deadline": iso_day(deadline),
                "hourMin": max(4, hours - 2),
                "hourMax": hours + 2,
                "department": dept,
                "schedule": random.choice([
                    "Mon 10:00-12:00", "Tue 14:00-16:00", "Wed 09:00-11:00",
                    "Thu 13:00-15:00", "Fri 11:00-13:00",
                ]),
                "approvalStatus": approval,
                "published": published,
                "withdrawn": False,
                "location": random.choice(["online", "offline"]),
                "requirements": random.choice(["Java", "Python", "SQL", "C++", "Statistics background"]),
                "createdAt": iso_z(created_at),
                "updatedAt": iso_z(published_at_d or created_at),
                "rejectionReason": "Insufficient module coverage." if approval == "rejected" else "",
            }
            if published_at_d:
                job["publishedAt"] = iso_z(published_at_d)
                job["reviewedAt"] = iso_z(created_at)
            if status == "closed":
                job["recruitmentClosed"] = True
                job["closedAt"] = iso_z(TODAY - timedelta(days=2))
            else:
                job["recruitmentClosed"] = False

            jobs.append(job)
            if approval == "approved" and published and status == "open":
                open_published_jobs.append(jid)

    applications = []
    app_id = 0
    app_statuses = ["pending", "viewed", "shortlisted", "hired", "rejected"]

    for sid, sname, sno in student_profiles:
        if not open_published_jobs:
            targets = [j["id"] for j in jobs if j.get("published")][:3]
        else:
            k = random.randint(1, 3)
            targets = random.sample(open_published_jobs, min(k, len(open_published_jobs)))
        for job_id_s in targets:
            app_id += 1
            applied = TODAY - timedelta(days=random.randint(1, 25))
            status = random.choice(app_statuses)
            applications.append({
                "id": f"app{app_id:04d}",
                "jobId": job_id_s,
                "studentId": sid,
                "studentName": sname,
                "studentNo": sno,
                "courseGrade": random.choice(["A", "A-", "B+", "B", "B-"]),
                "appliedAt": iso_z(applied, hour=random.randint(8, 18)),
                "status": status,
                "active": status != "rejected",
                "selectedAttachmentIds": [],
            })

    # Keep legacy job 101 for demo student if missing
    if not any(j["id"] == "101" for j in jobs):
        jobs.insert(0, {
            "id": "101",
            "teacherId": "mo001",
            "teacherName": "Dr. Smith",
            "moduleCode": "EBU6304",
            "title": "Software Engineering Teaching Assistant",
            "hours": 10,
            "positions": 2,
            "status": "open",
            "deadline": "2026-04-20",
            "department": "Computer Science",
            "approvalStatus": "approved",
            "published": True,
            "withdrawn": False,
            "publishedAt": "2026-04-01T10:00:00.000Z",
        })

    ROOT.mkdir(parents=True, exist_ok=True)
    (ROOT / "users.json").write_text(json.dumps(users, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    (ROOT / "students.json").write_text(json.dumps(students, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    (ROOT / "jobs.json").write_text(json.dumps(jobs, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    (ROOT / "applications.json").write_text(json.dumps(applications, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    print(f"Wrote {len(users)} users, {len(students)} students, {len(jobs)} jobs, {len(applications)} applications")


if __name__ == "__main__":
    main()
