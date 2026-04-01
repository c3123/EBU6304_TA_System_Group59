const DATA_PATH = "../assets/data/mock-data.json";
const REGISTERED_USERS_KEY = "ta_registered_users";

async function loadMockData() {
  const res = await fetch(DATA_PATH);
  if (!res.ok) {
    throw new Error("Failed to load JSON data file.");
  }
  return res.json();
}

function byId(id) {
  return document.getElementById(id);
}

function formatRole(role) {
  if (role === "student") return "Student";
  if (role === "teacher") return "Teacher";
  if (role === "admin") return "Admin";
  return role;
}

function loadRegisteredUsers() {
  try {
    return JSON.parse(localStorage.getItem(REGISTERED_USERS_KEY) || "[]");
  } catch {
    return [];
  }
}

function saveRegisteredUsers(users) {
  localStorage.setItem(REGISTERED_USERS_KEY, JSON.stringify(users));
}

async function getAuthUsers() {
  const data = await loadMockData();
  return [...data.users, ...loadRegisteredUsers()];
}

function registerLocalUser(user) {
  const users = loadRegisteredUsers();
  users.push(user);
  saveRegisteredUsers(users);
}
