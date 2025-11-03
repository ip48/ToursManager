// ISO 639-1 language codes - standard for internationalization
// Focused on languages commonly used in tourism industry

export interface Language {
  code: string;  // ISO 639-1 code
  name: string;  // Display name in English
}

export const LANGUAGES: Language[] = [
  { code: 'en', name: 'English' },
  { code: 'es', name: 'Spanish' },
  { code: 'fr', name: 'French' },
  { code: 'de', name: 'German' },
  { code: 'it', name: 'Italian' },
  { code: 'pt', name: 'Portuguese' },
  { code: 'zh', name: 'Chinese' },
  { code: 'ja', name: 'Japanese' },
  { code: 'ko', name: 'Korean' },
  { code: 'ar', name: 'Arabic' },
  { code: 'he', name: 'Hebrew' },
  { code: 'ru', name: 'Russian' },
  { code: 'hi', name: 'Hindi' },
  { code: 'tr', name: 'Turkish' },
  { code: 'nl', name: 'Dutch' },
  { code: 'pl', name: 'Polish' },
  { code: 'sv', name: 'Swedish' },
  { code: 'no', name: 'Norwegian' },
  { code: 'da', name: 'Danish' },
  { code: 'fi', name: 'Finnish' },
  { code: 'cs', name: 'Czech' },
  { code: 'el', name: 'Greek' },
  { code: 'th', name: 'Thai' },
  { code: 'vi', name: 'Vietnamese' },
  { code: 'id', name: 'Indonesian' },
];

// Helper function to convert codes to comma-separated string for backend
export const languageCodesToString = (codes: string[]): string => {
  return codes.join(',');
};

// Helper function to convert comma-separated string to codes array
export const stringToLanguageCodes = (str: string): string[] => {
  return str ? str.split(',').map(s => s.trim()).filter(Boolean) : [];
};

// Helper to get language name by code
export const getLanguageName = (code: string): string => {
  return LANGUAGES.find(lang => lang.code === code)?.name || code;
};
