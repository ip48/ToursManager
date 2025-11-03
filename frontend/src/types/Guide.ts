// Type definitions for Guide entity
// This file can be shared with React Native mobile app

export interface GuideFormData {
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
  profile: string;
  languages: string;
}

export interface Guide extends GuideFormData {
  id?: number;
  active?: boolean;
  createdAt?: string;
  updatedAt?: string;
}
