// API configuration constants
// This file can be shared with React Native mobile app

export const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

export const API_ENDPOINTS = {
  GUIDES: `${API_BASE_URL}/api/guides`,
  HELLO: `${API_BASE_URL}/api/hello`,
} as const;
