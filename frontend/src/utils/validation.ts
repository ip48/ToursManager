// Validation utilities
// This file can be shared with React Native mobile app

export interface ValidationError {
  [key: string]: string;
}

export const validateEmail = (email: string): boolean => {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
};

export const validateRequired = (value: string, minLength: number = 1): boolean => {
  return value.trim().length >= minLength;
};

export const validateMaxLength = (value: string, maxLength: number): boolean => {
  return value.length <= maxLength;
};

export const validateGuideForm = (data: {
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber?: string;
  profile?: string;
  languages?: string;
}): ValidationError => {
  const errors: ValidationError = {};

  if (!validateRequired(data.firstName, 2)) {
    errors.firstName = 'First name must be at least 2 characters';
  }

  if (!validateRequired(data.lastName, 2)) {
    errors.lastName = 'Last name must be at least 2 characters';
  }

  if (!validateRequired(data.email)) {
    errors.email = 'Email is required';
  } else if (!validateEmail(data.email)) {
    errors.email = 'Please enter a valid email address';
  }

  if (data.phoneNumber && !validateMaxLength(data.phoneNumber, 20)) {
    errors.phoneNumber = 'Phone number cannot exceed 20 characters';
  }

  if (data.profile && !validateMaxLength(data.profile, 500)) {
    errors.profile = 'Profile cannot exceed 500 characters';
  }

  if (data.languages && !validateMaxLength(data.languages, 200)) {
    errors.languages = 'Languages cannot exceed 200 characters';
  }

  return errors;
};
