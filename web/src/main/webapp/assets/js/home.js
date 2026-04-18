document.addEventListener("DOMContentLoaded", () => {
  byId("goLogin").onclick = () => location.href = "pages/login.jsp";
  byId("goRegister").onclick = () => location.href = "pages/register.jsp";
  byId("goStudent").onclick = () => location.href = "pages/student.jsp";
  byId("goTeacher").onclick = () => location.href = "pages/teacher.jsp";
  byId("goAdmin").onclick = () => location.href = "pages/admin.jsp";
});
