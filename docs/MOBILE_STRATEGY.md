# Future Mobile App Strategy (React Native)

## Vision
This project will eventually have **both** a web app (React) and a mobile app (React Native), sharing the same Spring Boot backend API.

## Current State
- **Phase 1 (NOW)**: Building React web application
- **Phase 2 (FUTURE)**: Add React Native mobile app

## Architecture Principles to Maintain

### 1. Backend API Design
✅ **DO:**
- Keep REST API responses consistent and clean
- Use standard HTTP status codes
- Return JSON (not HTML) from all `/api/*` endpoints
- Make API stateless (token-based auth when we add it)
- Design API endpoints that work for both web and mobile

❌ **DON'T:**
- Don't embed HTML in API responses
- Don't use session cookies for auth (use JWT/OAuth tokens instead)
- Don't assume web-only features (like file downloads via HTML)

### 2. Frontend Component Structure
✅ **DO:**
- Separate **business logic** from **UI components**
- Create reusable service functions for API calls
- Keep state management logic independent of UI
- Use TypeScript interfaces for data models
- Extract constants (API URLs, validation rules) to separate files

**Example structure to follow:**
```
frontend/src/
├── components/           # UI components (React-specific)
├── services/            # API calls (can be reused in React Native)
│   ├── guideService.ts
│   └── tourService.ts
├── types/               # TypeScript interfaces (reusable)
│   ├── Guide.ts
│   └── Tour.ts
├── utils/               # Helper functions (reusable)
│   └── validation.ts
└── constants/           # Config (reusable)
    └── api.ts
```

❌ **DON'T:**
- Don't put API calls directly in components
- Don't hardcode API URLs in multiple places
- Don't mix validation logic with UI rendering

### 3. Code That CAN Be Shared Between Web & Mobile
- ✅ API service functions (`fetch` calls)
- ✅ TypeScript interfaces/types
- ✅ Business logic (validation, calculations)
- ✅ Utility functions
- ✅ Constants and configuration
- ✅ State management logic (Redux/Zustand if we use it)

### 4. Code That CANNOT Be Shared
- ❌ UI Components (`<div>`, `<button>` vs `<View>`, `<TouchableOpacity>`)
- ❌ CSS styling (web CSS vs React Native StyleSheet)
- ❌ Navigation (React Router vs React Navigation)
- ❌ Platform-specific features (web: cookies, RN: camera)

## Best Practices for Now (React Web)

### When Writing Components:
1. **Extract API logic to service files**
   ```typescript
   // ✅ Good: services/guideService.ts
   export const registerGuide = async (data: GuideFormData) => {
     const response = await fetch('/api/guides', {
       method: 'POST',
       headers: { 'Content-Type': 'application/json' },
       body: JSON.stringify(data)
     });
     return response.json();
   };
   
   // Component just calls the service
   import { registerGuide } from '../services/guideService';
   ```

2. **Define types separately**
   ```typescript
   // ✅ Good: types/Guide.ts
   export interface Guide {
     id?: number;
     firstName: string;
     lastName: string;
     // ...
   }
   ```

3. **Extract validation to utilities**
   ```typescript
   // ✅ Good: utils/validation.ts
   export const validateEmail = (email: string): boolean => {
     return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
   };
   ```

### When Designing APIs:
1. **Use RESTful conventions**
   - `GET /api/guides` - list all
   - `GET /api/guides/{id}` - get one
   - `POST /api/guides` - create
   - `PUT /api/guides/{id}` - update
   - `DELETE /api/guides/{id}` - delete

2. **Return consistent response formats**
   ```json
   {
     "data": { "id": 1, "firstName": "John" },
     "message": "Success",
     "timestamp": "2025-10-29T14:00:00Z"
   }
   ```

3. **Use proper HTTP status codes**
   - 200: Success
   - 201: Created
   - 400: Bad request
   - 404: Not found
   - 500: Server error

## Future React Native Migration Path

When ready to add mobile app:

1. **Create new React Native project** (separate repo or monorepo)
2. **Extract shared code** from React web to shared packages:
   - `/shared/services/` - API calls
   - `/shared/types/` - TypeScript interfaces
   - `/shared/utils/` - Validation, helpers
   - `/shared/constants/` - Config
3. **Rebuild UI components** using React Native components
4. **Both apps** use the same backend API

## Tools to Consider Later
- **Monorepo**: Nx, Turborepo, or Yarn workspaces
- **Shared code**: npm private packages or Git submodules
- **Cross-platform**: Expo (easier) vs React Native CLI (more control)

## References for Future
- React Native docs: https://reactnative.dev/
- Expo (easier setup): https://expo.dev/
- Sharing code guide: https://reactnative.dev/docs/platform-specific-code

---

**Key Takeaway**: Build clean, separated code now, and React Native later will be much easier! 🚀
