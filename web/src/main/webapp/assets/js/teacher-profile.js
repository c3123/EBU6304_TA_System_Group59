function teacherProfileContextPath() {
  const parts = window.location.pathname.split("/").filter(Boolean);
  if (parts.length === 0) return "";
  const first = parts[0];
  if (first === "pages" || first === "assets") return "";
  return "/" + first;
}

function teacherProfileAccountApiBase() {
  return `${window.location.origin}${teacherProfileContextPath()}/api/account`;
}

function setProfileNotice(message, isError) {
  const notice = byId("profileNotice");
  if (!notice) return;
  notice.textContent = message || "";
  notice.style.color = isError ? "#dc2626" : "#475569";
}

function setProfileButtonLoading(button, loadingText, fallbackText, isLoading) {
  if (!button) return;
  if (isLoading) {
    button.dataset.label = button.textContent;
    button.textContent = loadingText;
    button.disabled = true;
  } else {
    button.textContent = button.dataset.label || fallbackText;
    button.disabled = false;
  }
}

async function changeTeacherProfilePassword(event) {
  event.preventDefault();
  const button = byId("teacherChangePasswordBtn");
  setProfileButtonLoading(button, "Changing...", "Change Password", true);

  try {
    const response = await fetch(`${teacherProfileAccountApiBase()}/change-password`, {
      method: "POST",
      credentials: "same-origin",
      headers: {
        "Content-Type": "application/json; charset=UTF-8"
      },
      body: JSON.stringify({
        oldPassword: byId("teacherOldPassword").value.trim(),
        newPassword: byId("teacherNewPassword").value.trim(),
        confirmPassword: byId("teacherConfirmPassword").value.trim()
      })
    });

    const body = await response.json();
    if (!response.ok || !body.success) {
      throw new Error(body.message || "Request failed.");
    }

    byId("teacherChangePasswordForm").reset();
    setProfileNotice("Password changed successfully.", false);
  } catch (err) {
    setProfileNotice(err.message || "Failed to change password.", true);
  } finally {
    setProfileButtonLoading(button, "Changing...", "Change Password", false);
  }
}

document.addEventListener("DOMContentLoaded", () => {
  const form = byId("teacherChangePasswordForm");
  if (form) {
    form.addEventListener("submit", changeTeacherProfilePassword);
  }
});
