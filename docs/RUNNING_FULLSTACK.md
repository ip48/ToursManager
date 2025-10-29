# Running the Full Stack Application

## Prerequisites
- Docker and Docker Compose installed on Windows host
- PostgreSQL database running
- Backend (Spring Boot) compiled

## Steps to Run

### 1. Start PostgreSQL (from Windows PowerShell or CMD)
```bash
cd C:\path\to\ToursManager
docker-compose up -d postgres
```

### 2. Start the Backend (Spring Boot)
Use VS Code task "Run Spring Boot" or run manually:
```bash
cd /workspaces/ToursManager
mvn spring-boot:run
```
Backend will be available at: http://localhost:8080

### 3. Start the Frontend (React + Vite)
Use VS Code task "Run Frontend" or run manually:
```bash
cd /workspaces/ToursManager/frontend
npm install  # First time only
npm run dev
```
Frontend will be available at: http://localhost:3000

## Quick Test

1. Open browser to http://localhost:3000
2. Click "Register as Guide" in the navigation
3. Fill out the form and submit
4. Check backend logs to see the registration request

## API Endpoints

### Guide Registration
- **POST** http://localhost:8080/api/guides
- **GET** http://localhost:8080/api/guides
- **GET** http://localhost:8080/api/guides?language=English
- **GET** http://localhost:8080/api/guides?search=John
- **GET** http://localhost:8080/api/guides/{id}

## Color Theme

The app uses a green & purple gradient theme:
- Primary Green: `#10b981`
- Primary Purple: `#8b5cf6`

Easily customizable in `frontend/src/App.css` under the `:root` CSS variables.

## Features Implemented

✅ Modern navigation bar with menu items
✅ Guide registration form with validation
✅ Form error handling and success messages
✅ Clean, responsive design
✅ API integration with Spring Boot backend

## Next Steps

- Add user registration functionality
- Create guide listing/management page
- Add authentication
- Create Tour entity and management
