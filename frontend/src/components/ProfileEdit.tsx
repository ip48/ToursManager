import { useState, useEffect } from 'react';
import type { FormEvent } from 'react';
import type { Guide, GuideFormData } from '../types/Guide';
import { authService } from '../services/authService';
import { getMyProfile, patchMyProfile } from '../services/guideService';
import { LANGUAGES, languageCodesToString } from '../constants/languages';

interface FormErrors {
  [key: string]: string;
}

export default function ProfileEdit() {
  const [profile, setProfile] = useState<Guide | null>(null);
  const [loading, setLoading] = useState(true);
  
  // Track which fields user wants to update
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [email, setEmail] = useState('');
  const [phoneNumber, setPhoneNumber] = useState('');
  const [profileText, setProfileText] = useState('');
  const [selectedLanguages, setSelectedLanguages] = useState<string[]>([]);
  
  // Track which fields have been changed
  const [changedFields, setChangedFields] = useState<Set<string>>(new Set());
  
  const [errors, setErrors] = useState<FormErrors>({});
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitStatus, setSubmitStatus] = useState<'success' | 'error' | null>(null);
  const [submitMessage, setSubmitMessage] = useState('');

  // Load current profile
  useEffect(() => {
    const loadProfile = async () => {
      const token = authService.getToken();
      if (!token) return;
      
      const result = await getMyProfile(token);
      if (result.data) {
        setProfile(result.data);
        setFirstName(result.data.firstName);
        setLastName(result.data.lastName);
        setEmail(result.data.email);
        setPhoneNumber(result.data.phoneNumber || '');
        setProfileText(result.data.profile || '');
        
        // Parse languages from comma-separated string
        if (result.data.languages) {
          setSelectedLanguages(result.data.languages.split(',').map(l => l.trim()));
        }
      }
      setLoading(false);
    };
    
    loadProfile();
  }, []);

  const markFieldChanged = (fieldName: string) => {
    setChangedFields(prev => new Set(prev).add(fieldName));
  };

  const handleLanguageToggle = (languageCode: string) => {
    setSelectedLanguages(prev => {
      const newSelection = prev.includes(languageCode)
        ? prev.filter(code => code !== languageCode)
        : [...prev, languageCode];
      
      markFieldChanged('languages');
      return newSelection;
    });
    
    if (errors.languages) {
      setErrors(prev => ({ ...prev, languages: '' }));
    }
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setSubmitStatus(null);
    setSubmitMessage('');

    const token = authService.getToken();
    if (!token) {
      setSubmitStatus('error');
      setSubmitMessage('You must be logged in to update your profile');
      return;
    }

    if (changedFields.size === 0) {
      setSubmitStatus('error');
      setSubmitMessage('No changes detected. Please modify at least one field.');
      return;
    }

    setIsSubmitting(true);

    // Build PATCH payload with ONLY changed fields
    const updates: Record<string, string | boolean> = {};
    
    if (changedFields.has('firstName')) updates.firstName = firstName;
    if (changedFields.has('lastName')) updates.lastName = lastName;
    if (changedFields.has('email')) updates.email = email;
    if (changedFields.has('phoneNumber')) updates.phoneNumber = phoneNumber;
    if (changedFields.has('profile')) updates.profile = profileText;
    if (changedFields.has('languages')) {
      updates.languages = languageCodesToString(selectedLanguages);
    }

    // Call PATCH API
    const result = await patchMyProfile(token, updates);

    if (result.data) {
      setSubmitStatus('success');
      setSubmitMessage(`Successfully updated: ${Array.from(changedFields).join(', ')}`);
      setProfile(result.data);
      setChangedFields(new Set()); // Clear changed fields
      setErrors({});
    } else {
      setSubmitStatus('error');
      setSubmitMessage(result.error || 'Failed to update profile. Please try again.');
      
      if (result.fieldErrors) {
        setErrors(result.fieldErrors);
      }
    }

    setIsSubmitting(false);
  };

  if (loading) {
    return <div className="card"><div>Loading profile...</div></div>;
  }

  if (!profile) {
    return <div className="card"><div>Failed to load profile</div></div>;
  }

  return (
    <div className="card">
      <div className="card-header">
        <h2 className="card-title">Edit Your Profile</h2>
        <p className="card-subtitle">
          Change only the fields you want to update (partial update with PATCH)
        </p>
      </div>

      {submitStatus && (
        <div className={`alert alert-${submitStatus}`}>
          {submitMessage}
        </div>
      )}

      {changedFields.size > 0 && (
        <div style={{ padding: '1rem', background: '#f0f9ff', borderRadius: '6px', marginBottom: '1rem' }}>
          <strong>Fields to update ({changedFields.size}):</strong>{' '}
          {Array.from(changedFields).join(', ')}
        </div>
      )}

      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label htmlFor="firstName" className="form-label">
            First Name
          </label>
          <input
            type="text"
            id="firstName"
            name="firstName"
            className={`form-input ${errors.firstName ? 'error' : ''}`}
            value={firstName}
            onChange={(e) => {
              setFirstName(e.target.value);
              markFieldChanged('firstName');
            }}
            placeholder="Enter your first name"
          />
          {errors.firstName && <div className="form-error">{errors.firstName}</div>}
        </div>

        <div className="form-group">
          <label htmlFor="lastName" className="form-label">
            Last Name
          </label>
          <input
            type="text"
            id="lastName"
            name="lastName"
            className={`form-input ${errors.lastName ? 'error' : ''}`}
            value={lastName}
            onChange={(e) => {
              setLastName(e.target.value);
              markFieldChanged('lastName');
            }}
            placeholder="Enter your last name"
          />
          {errors.lastName && <div className="form-error">{errors.lastName}</div>}
        </div>

        <div className="form-group">
          <label htmlFor="email" className="form-label">
            Email Address
          </label>
          <input
            type="email"
            id="email"
            name="email"
            className={`form-input ${errors.email ? 'error' : ''}`}
            value={email}
            onChange={(e) => {
              setEmail(e.target.value);
              markFieldChanged('email');
            }}
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
            value={phoneNumber}
            onChange={(e) => {
              setPhoneNumber(e.target.value);
              markFieldChanged('phoneNumber');
            }}
            placeholder="+1 234 567 8900"
          />
          {errors.phoneNumber && <div className="form-error">{errors.phoneNumber}</div>}
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
            value={profileText}
            onChange={(e) => {
              setProfileText(e.target.value);
              markFieldChanged('profile');
            }}
            placeholder="Tell us about your experience, interests, and what makes you a great guide..."
            rows={5}
          />
          {errors.profile && <div className="form-error">{errors.profile}</div>}
          <div className="form-hint">
            {profileText.length}/500 characters
          </div>
        </div>

        <div style={{ display: 'flex', gap: '1rem', marginTop: '2rem' }}>
          <button 
            type="submit" 
            className="btn btn-primary"
            disabled={isSubmitting || changedFields.size === 0}
          >
            {isSubmitting ? 'Saving...' : `Update ${changedFields.size} Field${changedFields.size !== 1 ? 's' : ''}`}
          </button>
          <button 
            type="button" 
            className="btn btn-secondary"
            onClick={() => {
              // Reset to original values
              if (profile) {
                setFirstName(profile.firstName);
                setLastName(profile.lastName);
                setEmail(profile.email);
                setPhoneNumber(profile.phoneNumber || '');
                setProfileText(profile.profile || '');
                if (profile.languages) {
                  setSelectedLanguages(profile.languages.split(',').map(l => l.trim()));
                }
              }
              setChangedFields(new Set());
              setErrors({});
              setSubmitStatus(null);
            }}
          >
            Reset Changes
          </button>
        </div>
      </form>

      <div style={{ marginTop: '2rem', padding: '1rem', background: '#f9fafb', borderRadius: '6px' }}>
        <h3 style={{ fontSize: '0.875rem', fontWeight: 600, marginBottom: '0.5rem' }}>
          💡 Tip: Partial Updates (PATCH)
        </h3>
        <p style={{ fontSize: '0.875rem', color: '#6b7280', margin: 0 }}>
          This form uses PATCH, so only the fields you change will be sent to the server. 
          For example, if you only update languages, the other fields remain unchanged.
        </p>
      </div>
    </div>
  );
}
