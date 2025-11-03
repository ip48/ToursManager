import { useState } from 'react';
import type { FormEvent } from 'react';
import type { GuideFormData } from '../types/Guide';
import { validateGuideForm } from '../utils/validation';
import { registerGuide } from '../services/guideService';
import { LANGUAGES, languageCodesToString } from '../constants/languages';

interface FormErrors {
  [key: string]: string;
}

export default function GuideRegistration() {
  const [formData, setFormData] = useState<GuideFormData>({
    firstName: '',
    lastName: '',
    email: '',
    phoneNumber: '',
    profile: '',
    languages: ''
  });

  const [selectedLanguages, setSelectedLanguages] = useState<string[]>([]);
  const [errors, setErrors] = useState<FormErrors>({});
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitStatus, setSubmitStatus] = useState<'success' | 'error' | null>(null);
  const [submitMessage, setSubmitMessage] = useState('');

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
    // Clear error for this field when user starts typing
    if (errors[name]) {
      setErrors(prev => ({ ...prev, [name]: '' }));
    }
  };

  const handleLanguageToggle = (languageCode: string) => {
    setSelectedLanguages(prev => {
      const newSelection = prev.includes(languageCode)
        ? prev.filter(code => code !== languageCode)
        : [...prev, languageCode];
      
      // Update formData.languages with comma-separated codes
      setFormData(prevForm => ({
        ...prevForm,
        languages: languageCodesToString(newSelection)
      }));
      
      return newSelection;
    });
    
    // Clear error when user selects languages
    if (errors.languages) {
      setErrors(prev => ({ ...prev, languages: '' }));
    }
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setSubmitStatus(null);
    setSubmitMessage('');

    // Validate form using shared validation utility
    const validationErrors = validateGuideForm(formData);
    
    if (Object.keys(validationErrors).length > 0) {
      setErrors(validationErrors);
      return;
    }

    setIsSubmitting(true);

    // Call API using shared service
    const result = await registerGuide(formData);

    if (result.data) {
      setSubmitStatus('success');
      setSubmitMessage('Successfully registered as a guide!');
      // Reset form
      setFormData({
        firstName: '',
        lastName: '',
        email: '',
        phoneNumber: '',
        profile: '',
        languages: ''
      });
      setSelectedLanguages([]);
      setErrors({});
    } else {
      setSubmitStatus('error');
      setSubmitMessage(result.error || 'Failed to register. Please try again.');
      
      // If backend returns field-specific errors, show them next to the fields
      if (result.fieldErrors) {
        setErrors(result.fieldErrors);
      }
    }

    setIsSubmitting(false);
  };

  return (
    <div className="card">
      <div className="card-header">
        <h2 className="card-title">Register as a Tour Guide</h2>
        <p className="card-subtitle">
          Join our team of expert tour guides and share your passion for travel
        </p>
      </div>

      {submitStatus && (
        <div className={`alert alert-${submitStatus}`}>
          {submitMessage}
        </div>
      )}

      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label htmlFor="firstName" className="form-label">
            First Name *
          </label>
          <input
            type="text"
            id="firstName"
            name="firstName"
            className={`form-input ${errors.firstName ? 'error' : ''}`}
            value={formData.firstName}
            onChange={handleInputChange}
            placeholder="Enter your first name"
          />
          {errors.firstName && <div className="form-error">{errors.firstName}</div>}
        </div>

        <div className="form-group">
          <label htmlFor="lastName" className="form-label">
            Last Name *
          </label>
          <input
            type="text"
            id="lastName"
            name="lastName"
            className={`form-input ${errors.lastName ? 'error' : ''}`}
            value={formData.lastName}
            onChange={handleInputChange}
            placeholder="Enter your last name"
          />
          {errors.lastName && <div className="form-error">{errors.lastName}</div>}
        </div>

        <div className="form-group">
          <label htmlFor="email" className="form-label">
            Email Address *
          </label>
          <input
            type="email"
            id="email"
            name="email"
            className={`form-input ${errors.email ? 'error' : ''}`}
            value={formData.email}
            onChange={handleInputChange}
            placeholder="your.email@example.com"
          />
          {errors.email && <div className="form-error">{errors.email}</div>}
        </div>

        <div className="form-group">
          <label htmlFor="phoneNumber" className="form-label">
            Phone Number
          </label>
          <input
            type="tel"
            id="phoneNumber"
            name="phoneNumber"
            className={`form-input ${errors.phoneNumber ? 'error' : ''}`}
            value={formData.phoneNumber}
            onChange={handleInputChange}
            placeholder="+1 234 567 8900"
          />
          {errors.phoneNumber && <div className="form-error">{errors.phoneNumber}</div>}
          <div className="form-hint">Optional - Include country code if international</div>
        </div>

        <div className="form-group">
          <label className="form-label">
            Languages
          </label>
          <div className="language-selector">
            {LANGUAGES.map(language => (
              <button
                key={language.code}
                type="button"
                className={`language-chip ${selectedLanguages.includes(language.code) ? 'selected' : ''}`}
                onClick={() => handleLanguageToggle(language.code)}
              >
                {language.name}
              </button>
            ))}
          </div>
          {selectedLanguages.length > 0 && (
            <div className="form-hint">
              Selected: {selectedLanguages.length} language{selectedLanguages.length !== 1 ? 's' : ''}
            </div>
          )}
          {errors.languages && <div className="form-error">{errors.languages}</div>}
        </div>

        <div className="form-group">
          <label htmlFor="profile" className="form-label">
            Profile / Bio
          </label>
          <textarea
            id="profile"
            name="profile"
            className={`form-textarea ${errors.profile ? 'error' : ''}`}
            value={formData.profile}
            onChange={handleInputChange}
            placeholder="Tell us about your experience, interests, and what makes you a great guide..."
            rows={5}
          />
          {errors.profile && <div className="form-error">{errors.profile}</div>}
          <div className="form-hint">
            {formData.profile.length}/500 characters
          </div>
        </div>

        <div style={{ display: 'flex', gap: '1rem', marginTop: '2rem' }}>
          <button 
            type="submit" 
            className="btn btn-primary"
            disabled={isSubmitting}
          >
            {isSubmitting ? 'Submitting...' : 'Register as Guide'}
          </button>
          <button 
            type="button" 
            className="btn btn-secondary"
            onClick={() => {
              setFormData({
                firstName: '',
                lastName: '',
                email: '',
                phoneNumber: '',
                profile: '',
                languages: ''
              });
              setSelectedLanguages([]);
              setErrors({});
              setSubmitStatus(null);
            }}
          >
            Clear Form
          </button>
        </div>
      </form>
    </div>
  );
}
