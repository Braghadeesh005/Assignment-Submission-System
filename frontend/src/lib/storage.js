export const AUTH_STORAGE_KEY = "assignment-platform-auth";

export function getAuth() {
  const raw = localStorage.getItem(AUTH_STORAGE_KEY);
  return raw ? JSON.parse(raw) : null;
}

export function setAuth(data) {
  localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(data));
}

export function clearAuth() {
  localStorage.removeItem(AUTH_STORAGE_KEY);
}
