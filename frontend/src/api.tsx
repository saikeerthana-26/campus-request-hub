const API_BASE = import.meta.env.VITE_API_BASE || "http://localhost:8080";

export type LoginResponse = { token: string; username: string; roles: string[] };

export type RequestItem = {
  id: string;
  title: string;
  description: string;
  category: string;
  status: "DRAFT" | "SUBMITTED" | "APPROVED" | "REJECTED" | "COMPLETED";
  createdBy: string;
  assignedTo?: string | null;
  createdAt: string;
  updatedAt: string;
};

function getToken() {
  return localStorage.getItem("token") || "";
}

async function api<T>(path: string, opts: RequestInit = {}): Promise<T> {
  const headers: Record<string, string> = {
    "Content-Type": "application/json",
    ...(opts.headers as any)
  };
  const token = getToken();
  if (token) headers["Authorization"] = `Bearer ${token}`;

  const res = await fetch(`${API_BASE}${path}`, { ...opts, headers });
  if (!res.ok) {
    const text = await res.text();
    throw new Error(text || `HTTP ${res.status}`);
  }
  return res.json();
}

export async function login(username: string, password: string) {
  return api<LoginResponse>("/api/auth/login", {
    method: "POST",
    body: JSON.stringify({ username, password })
  });
}

export async function listRequests() {
  return api<RequestItem[]>("/api/requests");
}

export async function createRequest(title: string, description: string, category: string) {
  return api<RequestItem>("/api/requests", {
    method: "POST",
    body: JSON.stringify({ title, description, category })
  });
}

export async function updateStatus(id: string, status: RequestItem["status"]) {
  return api<RequestItem>(`/api/requests/${id}/status`, {
    method: "POST",
    body: JSON.stringify({ status })
  });
}

export async function audit(id: string) {
  return api<{ items: any[] }>(`/api/requests/${id}/audit`);
}

export async function legacyEmployee(id: string) {
  return api<any>(`/api/requests/legacy/employee/${id}`);
}
