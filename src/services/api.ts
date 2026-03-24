/// <reference types="vite/client" />
import { User, Service, Employee, Appointment, ClientFile, TimeSlot, AuthResponse, ProfessionalNote } from '../../types';

// IMPORTANT: Ensure VITE_API_BASE_URL is set in Vercel settings later
const BASE = import.meta.env.VITE_API_BASE_URL || '';

async function request(path: string, options: RequestInit = {}) {
  const token = localStorage.getItem('token');
  const res = await fetch(`${BASE}${path}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...((options as any).headers || {}),
    },
  });
  const body = await res.json().catch(() => ({}));
  if (res.status === 401) {
    localStorage.removeItem('token');
    window.location.href = '/#/login';
    throw new Error('Session expirée. Veuillez vous reconnecter.');
  }
  if (!res.ok) throw new Error(body.message || `HTTP ${res.status}`);
  return body;
}

export async function fetchSecureImage(url: string): Promise<string> {
  const token = localStorage.getItem('token');
  const base = (BASE || '').replace(/\/+$/, '');
  const fullUrl = url.startsWith('http') ? url : `${base}${url}`;
  try {
    const res = await fetch(fullUrl, {
      headers: token ? { Authorization: `Bearer ${token}` } : {},
    });
    if (!res.ok) return '';
    const blob = await res.blob();
    return URL.createObjectURL(blob);
  } catch {
    return '';
  }
}

export const api = {
  auth: {
    login: (email: string, password: string) =>
      request('/api/auth/login', { method: 'POST', body: JSON.stringify({ email, password }) }),

    register: (data: { firstName: string; lastName: string; email: string; password: string }) =>
      request('/api/auth/register', {
        method: 'POST',
        body: JSON.stringify({
          fullName: `${data.firstName} ${data.lastName}`.trim(),
          email: data.email,
          password: data.password,
        }),
      }),

    me: () => request('/api/auth/me'),
  },

  services: {
    list: () => request('/api/services'),
  },

  employees: {
    list: () => request('/api/employees'),
  },

  availability: {
    getSlots: (employeeId: string | null, serviceId: string, date: string) => {
      const params = new URLSearchParams();
      params.set('serviceId', serviceId);
      params.set('date', date);
      if (employeeId) params.set('employeeId', employeeId);
      return request(`/api/availability?${params.toString()}`);
    },
  },

  appointments: {
    list: async () => {
      const [aptsRes, servicesRes] = await Promise.all([
        request('/api/appointments'),
        request('/api/services').catch(() => ({ data: [] })),
      ]);

      const apts = Array.isArray(aptsRes?.data) ? aptsRes.data : [];
      const services = Array.isArray(servicesRes?.data) ? servicesRes.data : [];

      const enriched = apts.map((apt: any) => ({
        ...apt,
        serviceName: services.find((s: any) => s.id === apt.serviceId)?.name ?? null,
        durationMinutes: services.find((s: any) => s.id === apt.serviceId)?.durationMinutes
          ?? services.find((s: any) => s.id === apt.serviceId)?.duration_min ?? 60,
        price: services.find((s: any) => s.id === apt.serviceId)?.price ?? null,
      }));

      return { ...aptsRes, data: enriched };
    },

    create: (data: { serviceId: string; startAt: string; endAt?: string; employeeId?: string }) =>
      request('/api/appointments', {
        method: 'POST',
        body: JSON.stringify({
          serviceId: data.serviceId,
          startAt: data.startAt,
        }),
      }),

    cancel: (id: string, reason?: string) =>
      request(`/api/appointments/${id}/cancel`, {
        method: 'POST',
        body: JSON.stringify({ cancellationReason: reason || '' }),
      }),
  },

  clientFile: {
    get: () => request('/api/client/me/file'),

    update: (data: any) =>
      request('/api/client/me/file', {
        method: 'PUT',
        body: JSON.stringify({
          ...data,
          photoConsentForFollowup: data.photoConsentForFollowUp ?? data.photoConsentForFollowup,
        }),
      }),
  },

  notes: {
    getByAppointment: (appointmentId: string) =>
      request(`/api/appointments/${appointmentId}/notes`),
  },

  users: {
    updateProfile: (data: { fullName: string; phone: string }) =>
      request('/api/users/profile', { method: 'PUT', body: JSON.stringify(data) }),
    updateProfileById: (userId: string, data: { fullName: string; phone: string }) =>
      request(`/api/users/${userId}`, { method: 'PUT', body: JSON.stringify(data) }),
  },

  photos: {
    getClientPhotos: () =>
      request('/api/photos/clients/me').catch(() => ({ data: [] })),

    getServicePhotos: (serviceId: string) =>
      request(`/api/photos/services/${serviceId}`).catch(() => ({ data: [] })),

    getImageUrl: (url: string) => {
      const base = (BASE || '').replace(/\/+$/, '');
      return `${base}${url}`;
    },
  },
};