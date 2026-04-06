document.addEventListener("DOMContentLoaded", () => {
  const role = byId("role");
  const studentOnly = byId("studentOnly");

  const updateRoleFields = () => {
    studentOnly.style.display = role.value === "student" ? "block" : "none";
  };

  role.addEventListener("change", updateRoleFields);
  updateRoleFields();
});
