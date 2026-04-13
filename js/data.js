// =============================================
// SplitSmart - API Client & Utilities
// Replaces mock data with real backend calls
// =============================================

const API_BASE = 'http://localhost:8080/api';

// ===== Token Management =====
const Auth = {
    getToken() { return localStorage.getItem('splitsmart_token'); },
    setToken(t) { localStorage.setItem('splitsmart_token', t); },
    clearToken() { localStorage.removeItem('splitsmart_token'); },
    isLoggedIn() { return !!this.getToken(); }
};

// ===== API Helper =====
async function apiCall(method, endpoint, body = null) {
    const headers = { 'Content-Type': 'application/json' };
    const token = Auth.getToken();
    if (token) headers['Authorization'] = `Bearer ${token}`;

    const options = { method, headers };
    if (body) options.body = JSON.stringify(body);

    const res = await fetch(API_BASE + endpoint, options);

    if (res.status === 204) return null; // No content

    const data = await res.json().catch(() => ({ message: res.statusText }));

    if (!res.ok) {
        throw new Error(data.message || `HTTP ${res.status}`);
    }
    return data;
}

// ===== Utility Formatters =====
const DataHelpers = {
    formatCurrency(amount) {
        return new Intl.NumberFormat('en-IN', {
            style: 'currency',
            currency: 'INR'
        }).format(amount || 0);
    },

    formatDate(dateStr) {
        if (!dateStr) return '';
        return new Date(dateStr).toLocaleDateString('en-US', {
            year: 'numeric', month: 'short', day: 'numeric'
        });
    },

    getUserInitials(name) {
        if (!name) return '?';
        return name.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2);
    }
};

// ===== API Methods =====

// Auth
const AuthAPI = {
    login: (data) => apiCall('POST', '/auth/login', data),
    signup: (data) => apiCall('POST', '/auth/signup', data),
};

// Users
const UserAPI = {
    me: () => apiCall('GET', '/users/me'),
    update: (data) => apiCall('PUT', '/users/me', data),
    changePassword: (data) => apiCall('PUT', '/users/me/password', data),
    searchByEmail: (email) => apiCall('GET', `/users/search?email=${encodeURIComponent(email)}`),
};

// Groups
const GroupAPI = {
    list: () => apiCall('GET', '/groups'),
    create: (data) => apiCall('POST', '/groups', data),
    get: (id) => apiCall('GET', `/groups/${id}`),
    updateMembers: (id, data) => apiCall('PUT', `/groups/${id}/members`, data),
    leave: (id) => apiCall('DELETE', `/groups/${id}/leave`),
    expenses: (id) => apiCall('GET', `/groups/${id}/expenses`),
    balances: (id) => apiCall('GET', `/groups/${id}/balances`),
};

// Expenses
const ExpenseAPI = {
    list: (params = {}) => {
        const q = new URLSearchParams(params).toString();
        return apiCall('GET', `/expenses${q ? '?' + q : ''}`);
    },
    create: (data) => apiCall('POST', '/expenses', data),
    get: (id) => apiCall('GET', `/expenses/${id}`),
    update: (id, data) => apiCall('PUT', `/expenses/${id}`, data),
    delete: (id) => apiCall('DELETE', `/expenses/${id}`),
};

// Settlements
const SettlementAPI = {
    list: (status) => apiCall('GET', `/settlements${status ? '?status=' + status : ''}`),
    create: (data) => apiCall('POST', '/settlements', data),
    confirm: (id) => apiCall('PUT', `/settlements/${id}/confirm`),
    reject: (id) => apiCall('PUT', `/settlements/${id}/reject`),
};

// Dashboard
const DashboardAPI = {
    get: () => apiCall('GET', '/dashboard'),
    balances: () => apiCall('GET', '/balances'),
};

// Categories
const CategoryAPI = {
    list: () => apiCall('GET', '/categories'),
};
