document.addEventListener("DOMContentLoaded", () => {
  const form = byId("registerForm");
  const role = byId("role");
  const studentOnly = byId("studentOnly");
  const tip = byId("tip");

  const updateRoleFields = () => {
    studentOnly.style.display = role.value === "student" ? "block" : "none";
  };

  role.addEventListener("change", updateRoleFields);
  updateRoleFields();

  form.addEventListener("submit", async (e) => {
    e.preventDefault();

    const payload = {
      id: Date.now().toString(),
      name: byId("name").value.trim(),
      email: byId("email").value.trim(),
      password: byId("password").value.trim(),
      role: role.value,
      studentId: byId("studentId").value.trim(),
      programme: byId("programme").value.trim()
    };

    if (!payload.name || !payload.email || !payload.password) {
      tip.textContent = "Please complete all required fields.";
      tip.style.color = "#dc2626";
      return;
    }

    if (payload.role === "student" && (!payload.studentId || !payload.programme)) {
      tip.textContent = "Student role requires Student ID and Programme.";
      tip.style.color = "#dc2626";
      return;
    }

    const users = await getAuthUsers();
    if (users.some(u => u.email.toLowerCase() === payload.email.toLowerCase())) {
      tip.textContent = "This email is already registered.";
      tip.style.color = "#dc2626";
      return;
    }

    registerLocalUser(payload);
    tip.textContent = "Registration successful. Redirecting to login...";
    tip.style.color = "#16a34a";

    setTimeout(() => {
      location.href = "login.jsp";
    }, 700);
  });
});
