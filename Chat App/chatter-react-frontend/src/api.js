const API_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";
export const WS_URL = import.meta.env.VITE_WS_URL || "http://localhost:8080/ws";

export const token = () => localStorage.getItem("chat_token");

export async function api(path, { body, ...opts } = {}) {
  const headers = {
    ...(body !== undefined ? { "Content-Type": "application/json" } : {}),
    ...(opts.headers || {}),
  };

  const currentToken = token();

  if (currentToken) {
    headers.Authorization = `Bearer ${currentToken}`;
  }

  const res = await fetch(`${API_URL}${path}`, {
    ...opts,
    headers,
    body: body === undefined ? undefined : JSON.stringify(body),
  });

  const raw = await res.text();

  let data;
  try {
    data = raw ? JSON.parse(raw) : null;
  } catch {
    data = raw;
  }

  if (!res.ok) {
    throw new Error(
      typeof data === "string"
        ? data
        : data?.message || data?.error || `HTTP ${res.status}`,
    );
  }

  return data;
}

export const auth = {
  login: (b) => api("/api/v1/auth/login", { method: "POST", body: b }),
  register: (b) => api("/api/v1/auth/register", { method: "POST", body: b }),
  me: () => api("/api/v1/users/me"),
};

export const users = {
  search: (q) => api(`/api/v1/users/search?username=${encodeURIComponent(q)}`),
  connections: () => api("/api/v1/connections"),
};

export const requests = {
  received: () => api("/api/v1/connections/requests/received"),
  send: (id) =>
    api("/api/v1/connections/requests", {
      method: "POST",
      body: { receiverId: id },
    }),
  accept: (id) =>
    api(`/api/v1/connections/requests/${id}/accept`, { method: "PUT" }),
  reject: (id) =>
    api(`/api/v1/connections/requests/${id}/reject`, { method: "PUT" }),
};

export const chats = {
  all: () => api("/api/v1/conversations"),

  createPrivate: (id) =>
    api("/api/v1/conversations/private", {
      method: "POST",
      body: { otherUserId: id },
    }),

  messages: (id) => api(`/api/v1/messages/${id}?page=0&size=30`),

  send: (id, content) =>
    api(`/api/v1/conversations/${id}/messages`, {
      method: "POST",
      body: { content },
    }),

  createGroup: (body) =>
    api("/api/v1/groups", {
      method: "POST",
      body,
    }),

  deleteMessage: (messageId) =>
    api(`/api/v1/messages/${messageId}`, {
      method: "DELETE",
    }),

  hideMessage: (messageId) =>
    api(`/api/v1/messages/${messageId}/hide`, {
      method: "DELETE",
    }),
};
