# Code Organization for Future Mobile App

## ✅ What We Just Did

Refactored the frontend code to prepare for future React Native mobile app:

### New Structure
```
frontend/src/
├── components/              # React web UI components (NOT shareable)
│   └── GuideRegistration.tsx
├── services/               # ✅ API calls (SHAREABLE with React Native)
│   └── guideService.ts
├── types/                  # ✅ TypeScript interfaces (SHAREABLE)
│   └── Guide.ts
├── utils/                  # ✅ Helper functions (SHAREABLE)
│   └── validation.ts
└── constants/              # ✅ Configuration (SHAREABLE)
    └── api.ts
```

### Files Marked for Future Sharing

#### 1. `/types/Guide.ts`
- TypeScript interfaces for Guide entity
- `GuideFormData` - Form input data (base interface)
- `Guide` - Extends GuideFormData with server metadata (id, timestamps)
- `languages` field: comma-separated language codes (e.g., "en,es,fr")
- Matches backend API response structure (uses GuideDTO)
- Can be used in both React and React Native

#### 2. `/services/guideService.ts`
- All API calls for guide operations
- `registerGuide()`, `getAllGuides()`, `searchGuides()`, etc.
- Returns consistent `ApiResponse<T>` format
- Uses `fetch` API (works in both web and React Native)

#### 3. `/utils/validation.ts`
- Form validation logic
- `validateEmail()`, `validateGuideForm()`, etc.
- Pure functions with no UI dependencies

#### 4. `/constants/api.ts`
- API endpoint URLs
- Can be configured via environment variables
- Single source of truth for backend URLs

#### 5. `/constants/languages.ts`
- ISO 639-1 language codes and names
- Matches backend `Language` entity codes
- Helper functions: `languageCodesToString()`, `stringToLanguageCodes()`, `getLanguageName()`
- Shared between web and mobile for language selection UIs

### Component Refactoring

**GuideRegistration.tsx** now:
- ✅ Uses shared types (`GuideFormData`, `Guide`)
- ✅ Uses shared service (`registerGuide()`)
- ✅ Uses shared validation (`validateGuideForm()`)
- ✅ Only contains UI-specific React code
- ❌ No hardcoded API URLs
- ❌ No inline validation logic
- ❌ No direct fetch calls

## When Building React Native App (Future)

1. **Keep these files** in a shared location:
   - `types/*.ts`
   - `services/*.ts`
   - `utils/*.ts`
   - `constants/*.ts`

2. **Rewrite only the UI components**:
   - Replace `<div>` with `<View>`
   - Replace CSS with StyleSheet
   - Use React Native form inputs
   - Keep same business logic

3. **Same API, different UI**:
   ```typescript
   // React Native component
   import { registerGuide } from '../shared/services/guideService';
   import { validateGuideForm } from '../shared/utils/validation';
   import type { GuideFormData } from '../shared/types/Guide';
   
   // Then build mobile UI using React Native components
   ```

## Benefits

- ✅ Reduced code duplication
- ✅ Consistent validation across platforms
- ✅ Single source of truth for API calls
- ✅ Easier testing (services/utils are pure functions)
- ✅ Clear separation of concerns
- ✅ Ready for monorepo structure

## Next Steps for All New Features

When adding new features (Tours, Bookings, etc.):

1. Create types in `/types/`
2. Create service in `/services/`
3. Add validation in `/utils/`
4. Build UI component that uses these

Follow the same pattern we established with Guide!
