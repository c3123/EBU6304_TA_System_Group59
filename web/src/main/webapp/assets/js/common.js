function byId(id) {
  return document.getElementById(id);
}

function formatRole(role) {
  if (role === "student") return "Student";
  if (role === "teacher") return "Teacher";
  if (role === "admin") return "Admin";
  return role;
}

function getContextPath() {
  const parts = window.location.pathname.split("/").filter(Boolean);
  if (!parts.length) return "";
  const first = parts[0];
  if (first === "pages" || first === "assets") {
    return "";
  }
  return `/${first}`;
}
