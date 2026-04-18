document.addEventListener("DOMContentLoaded", () => {
  const form = byId("loginForm");
  const tip = byId("tip");

  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    const email = byId("email").value.trim();
    const password = byId("password").value.trim();

    try {
      const users = await getAuthUsers();
      const user = users.find(u => u.email === email && u.password === password);
      if (!user) {
        tip.textContent = "Invalid email or password.";
        tip.style.color = "#dc2626";
        return;
      }

      tip.textContent = `Login successful: ${formatRole(user.role)}`;
      tip.style.color = "#16a34a";

      if (user.role === "student") location.href = "student.jsp";
      if (user.role === "teacher") location.href = "mo-applications.jsp";
      if (user.role === "admin") location.href = "admin.jsp";
    } catch (err) {
      tip.textContent = err.message;
      tip.style.color = "#dc2626";
    }
  });
});
