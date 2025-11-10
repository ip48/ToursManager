// API configuration constants
// This file can be shared with React Native mobile app

export const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

export const API_ENDPOINTS = {
  GUIDES: `${API_BASE_URL}/guides`,
  HELLO: `${API_BASE_URL}/hello`,
  AUTH_LOGIN: `${API_BASE_URL}/auth/login`,
  AUTH_REGISTER: `${API_BASE_URL}/auth/register`,
} as const;
