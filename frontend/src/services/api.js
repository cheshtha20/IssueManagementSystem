/**
 * API Service Layer
 * -----------------
 * All backend API calls go through this file.
 * Uses the fetch API with JWT Bearer auth.
 * 
 * The Vite dev server proxies /api/* to http://localhost:8080
 * so we use relative URLs (no need for a base URL).
 */

const API_BASE = '/api';

// ---------- Helpers ----------

/**
 * Get the stored JWT token from localStorage.
 */
function getToken() {
  return localStorage.getItem('token');
}

/**
 * Build headers with JSON content type and optional auth token.
 */
function authHeaders() {
  const headers = { 'Content-Type': 'application/json' };
  const token = getToken();
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }
  return headers;
}

/**
 * Generic request wrapper — handles JSON parsing and error extraction.
 */
async function request(url, options = {}) {
  const res = await fetch(url, {
    ...options,
    headers: { ...authHeaders(), ...options.headers },
  });

  // 204 No Content
  if (res.status === 204) return null;

  const data = await res.json().catch(() => null);

  if (!res.ok) {
    if (res.status === 401) {
      // Token expired or invalid — clear state and force login
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
    }
    const message = data?.message || data?.error || `Request failed (${res.status})`;
    const err = new Error(message);
    err.status = res.status;
    throw err;
  }

  return data;
}

// ---------- Auth ----------

/**
 * Login with email & password and optional reCAPTCHA token.
 * Returns { token, tokenType, username, email, role }.
 */
export async function login(email, password, captchaToken) {
  return request(`${API_BASE}/auth/login`, {
    method: 'POST',
    body: JSON.stringify({ email, password, captchaToken }),
  });
}

/**
 * Public signup — creates a new user.
 */
export async function signup(userData) {
  return request(`${API_BASE}/auth/signup`, {
    method: 'POST',
    body: JSON.stringify(userData),
  });
}

/**
 * Confirm email with OTP.
 */
export async function confirmEmail(email, otp) {
  return request(`${API_BASE}/auth/email-verification/confirm`, {
    method: 'POST',
    body: JSON.stringify({ email, otp }),
  });
}

/**
 * Request a new verification OTP.
 */
export async function requestOtp(email) {
  return request(`${API_BASE}/auth/email-verification/request`, {
    method: 'POST',
    body: JSON.stringify({ email }),
  });
}

/**
 * Get all users in the system (ADMIN only).
 */
export async function getAllUsers() {
  return request(`${API_BASE}/users`);
}

/**
 * Update a user's role (ADMIN only).
 */
export async function updateUserRole(userId, role) {
  return request(`${API_BASE}/users/${userId}/role`, {
    method: 'PUT',
    body: JSON.stringify(role),
  });
}

// ---------- Tickets ----------

/**
 * Get a single ticket by ID.
 */
export async function getTicket(ticketId) {
  return request(`${API_BASE}/tickets/${ticketId}`);
}

/**
 * Search / list tickets with optional filters and pagination.
 * Returns a Spring Page object: { content, totalPages, totalElements, number, ... }
 */
export async function searchTickets({ keyword, status, priority, page = 0, size = 10 } = {}) {
  const params = new URLSearchParams();
  if (keyword) params.set('keyword', keyword);
  if (status) params.set('status', status);
  if (priority) params.set('priority', priority);
  params.set('page', page);
  params.set('size', size);
  return request(`${API_BASE}/search/tickets?${params.toString()}`);
}

/**
 * Create a new ticket (TICKET_RAISER only).
 */
export async function createTicket(ticketData) {
  return request(`${API_BASE}/tickets`, {
    method: 'POST',
    body: JSON.stringify(ticketData),
  });
}

/**
 * Get tickets pending assignment (ADMIN, MANAGER, TEAM_LEAD).
 */
export async function getPendingAssignments() {
  return request(`${API_BASE}/tickets/pending-assignment`);
}

/**
 * Assign a ticket to a user by email (ADMIN, MANAGER, TEAM_LEAD).
 */
export async function assignTicket(ticketId, assigneeEmail) {
  return request(`${API_BASE}/tickets/${ticketId}/assign`, {
    method: 'POST',
    body: JSON.stringify({ assigneeEmail }),
  });
}

/**
 * Reroute a ticket to a new category (ADMIN, ASSIGNEE).
 */
export async function rerouteTicket(ticketId, data) {
  return request(`${API_BASE}/tickets/${ticketId}/reroute`, {
    method: 'POST',
    body: JSON.stringify(data),
  });
}

export async function updateTicketStatus(ticketId, status) {
  return request(`${API_BASE}/tickets/${ticketId}/status`, {
    method: 'POST',
    body: JSON.stringify({ status }),
  });
}

/**
 * Update ticket progress (SUPPORT_ENGINEER only).
 */
export async function updateTicketProgress(ticketId, progress) {
  return request(`${API_BASE}/tickets/${ticketId}/progress`, {
    method: 'PATCH',
    body: JSON.stringify({ progress }),
  });
}

/**
 * Get dashboard stats summary.
 */
export async function getDashboardSummary() {
  return request(`${API_BASE}/dashboard/summary`);
}

/**
 * Get detailed work SLA timings status list.
 */
export async function getWorkStatus() {
  return request(`${API_BASE}/dashboard/work-status`);
}

export async function getAvailableAssignees(ticketId) {
  return request(`${API_BASE}/tickets/${ticketId}/available-assignees`);
}

export async function getAllAssignees() {
  return request(`${API_BASE}/users/assignees`);
}

// ---------- Comments ----------

/**
 * Get all comments for a ticket.
 */
export async function getComments(ticketId) {
  return request(`${API_BASE}/tickets/${ticketId}/comments`);
}

/**
 * Add a comment to a ticket.
 */
export async function addComment(ticketId, text) {
  return request(`${API_BASE}/tickets/${ticketId}/comments`, {
    method: 'POST',
    body: JSON.stringify({ text }),
  });
}

// ---------- Remarks ----------

/**
 * Get all remarks for a ticket.
 */
export async function getRemarks(ticketId) {
  return request(`${API_BASE}/tickets/${ticketId}/remarks`);
}

/**
 * Add a remark to a ticket.
 */
export async function addRemark(ticketId, data) {
  return request(`${API_BASE}/tickets/${ticketId}/remarks`, {
    method: 'POST',
    body: JSON.stringify(data),
  });
}
