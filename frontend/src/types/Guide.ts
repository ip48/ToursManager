// Type definitions for Guide entity
// This file can be shared with React Native mobile app

export interface Guide {
  id?: number;
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber?: string;
  profile?: string;
  languages?: string;
  active?: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface GuideFormData {
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
  profile: string;
  languages: string;
}
