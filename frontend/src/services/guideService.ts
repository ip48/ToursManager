// Guide service - API calls for guide-related operations
// This file can be shared with React Native mobile app

import { API_ENDPOINTS } from '../constants/api';
import type { Guide, GuideFormData } from '../types/Guide';

export interface ApiResponse<T> {
  data?: T;
  error?: string;
  fieldErrors?: { [key: string]: string };  // Field-specific validation errors
  status: number;
}

/**
 * Register a new guide
 */
export const registerGuide = async (guideData: GuideFormData): Promise<ApiResponse<Guide>> => {
  try {
    const response = await fetch(API_ENDPOINTS.GUIDES, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(guideData),
    });

    if (response.ok) {
      const data = await response.json();
      return { data, status: response.status };
    } else {
      // Parse standardized error response from backend
      try {
        const errorData = await response.json();
        return { 
          error: errorData.message || 'Failed to register guide',
          fieldErrors: errorData.errors,  // Field-specific validation errors
          status: response.status 
        };
      } catch {
        // Fallback if response is not JSON
        return { error: 'Failed to register guide', status: response.status };
      }
    }
  } catch {
    return { 
      error: 'Network error. Please make sure the backend is running.', 
      status: 0 
    };
  }
};

/**
 * Get all guides
 */
export const getAllGuides = async (): Promise<ApiResponse<Guide[]>> => {
  try {
    const response = await fetch(API_ENDPOINTS.GUIDES);
    
    if (response.ok) {
      const data = await response.json();
      return { data, status: response.status };
    } else {
      return { error: 'Failed to fetch guides', status: response.status };
    }
  } catch {
    return { error: 'Network error', status: 0 };
  }
};

/**
 * Get guide by ID
 */
export const getGuideById = async (id: number): Promise<ApiResponse<Guide>> => {
  try {
    const response = await fetch(`${API_ENDPOINTS.GUIDES}/${id}`);
    
    if (response.ok) {
      const data = await response.json();
      return { data, status: response.status };
    } else {
      return { error: 'Guide not found', status: response.status };
    }
  } catch {
    return { error: 'Network error', status: 0 };
  }
};

/**
 * Search guides by name
 */
export const searchGuides = async (searchTerm: string): Promise<ApiResponse<Guide[]>> => {
  try {
    const response = await fetch(`${API_ENDPOINTS.GUIDES}?search=${encodeURIComponent(searchTerm)}`);
    
    if (response.ok) {
      const data = await response.json();
      return { data, status: response.status };
    } else {
      return { error: 'Search failed', status: response.status };
    }
  } catch {
    return { error: 'Network error', status: 0 };
  }
};

/**
 * Search guides by language
 */
export const searchGuidesByLanguage = async (language: string): Promise<ApiResponse<Guide[]>> => {
  try {
    const response = await fetch(`${API_ENDPOINTS.GUIDES}?language=${encodeURIComponent(language)}`);
    
    if (response.ok) {
      const data = await response.json();
      return { data, status: response.status };
    } else {
      return { error: 'Search failed', status: response.status };
    }
  } catch {
    return { error: 'Network error', status: 0 };
  }
};

/**
 * Get current user's profile (authenticated)
 */
export const getMyProfile = async (token: string): Promise<ApiResponse<Guide>> => {
  try {
    const response = await fetch(`${API_ENDPOINTS.GUIDES}/profile`, {
      headers: {
        'Authorization': `Bearer ${token}`,
      },
    });
    
    if (response.ok) {
      const data = await response.json();
      return { data, status: response.status };
    } else {
      return { error: 'Failed to fetch profile', status: response.status };
    }
  } catch {
    return { error: 'Network error', status: 0 };
  }
};

/**
 * Partially update current user's profile (authenticated)
 * Only send fields you want to change
 */
export const patchMyProfile = async (
  token: string, 
  updates: Partial<GuideFormData>
): Promise<ApiResponse<Guide>> => {
  try {
    const response = await fetch(`${API_ENDPOINTS.GUIDES}/profile`, {
      method: 'PATCH',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`,
      },
      body: JSON.stringify(updates),
    });

    if (response.ok) {
      const data = await response.json();
      return { data, status: response.status };
    } else {
      try {
        const errorData = await response.json();
        return { 
          error: errorData.message || 'Failed to update profile',
          fieldErrors: errorData.errors,
          status: response.status 
        };
      } catch {
        return { error: 'Failed to update profile', status: response.status };
      }
    }
  } catch {
    return { 
      error: 'Network error. Please make sure the backend is running.', 
      status: 0 
    };
  }
};

/**
 * Fully replace current user's profile (authenticated)
 * Must send all fields
 */
export const updateMyProfile = async (
  token: string, 
  guideData: GuideFormData
): Promise<ApiResponse<Guide>> => {
  try {
    const response = await fetch(`${API_ENDPOINTS.GUIDES}/profile`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`,
      },
      body: JSON.stringify(guideData),
    });

    if (response.ok) {
      const data = await response.json();
      return { data, status: response.status };
    } else {
      try {
        const errorData = await response.json();
        return { 
          error: errorData.message || 'Failed to update profile',
          fieldErrors: errorData.errors,
          status: response.status 
        };
      } catch {
        return { error: 'Failed to update profile', status: response.status };
      }
    }
  } catch {
    return { 
      error: 'Network error. Please make sure the backend is running.', 
      status: 0 
    };
  }
};
