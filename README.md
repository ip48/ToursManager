# ToursManager

Tours Management System

## Project Goals

<!-- Add your project goals and roadmap here -->
# Travel SaaS Platform (MVP)

## Overview
A minimal SaaS platform for a travel company.

- **Guides** create and manage **Tours**.
- **Travelers** register for tours.
- Each tour includes a flexible **packing checklist** stored in MongoDB.
- Later versions may add AI-powered packing suggestions.

## Current Implementation Status

✅ **Completed:**
- Guide registration and management (CRUD operations)
- PostgreSQL database with proper relational model
- Language support (25 ISO 639-1 codes with efficient querying)
- OpenAPI/Swagger documentation
- Standardized error handling
- Docker Compose for local development
- React frontend with TypeScript
- Code organized for future React Native mobile app

🚧 **Planned:**
- Tours entity and management
- Traveler registration and bookings
- MongoDB for packing checklists
- AWS deployment
- AI-powered features

## Tech Stack
| Layer | Technology | Purpose | Status |
|-------|-------------|----------|---------|
| **Frontend** | React 18 + TypeScript + Vite | User interface for guides and travelers | ✅ Active |
| **Backend** | Java 21 + Spring Boot 3.3 | REST API, business logic, validation | ✅ Active |
| **Databases** | PostgreSQL + MongoDB (planned) | SQL for structured data, NoSQL for dynamic equipment lists | ✅ PostgreSQL / 🚧 MongoDB |
| **Infrastructure** | Docker Compose (local) + AWS (planned) | Local development and cloud deployment | ✅ Local / 🚧 AWS |
| **Build/Deploy** | Maven + Docker | Build and containerization | ✅ Active |

## Backend (Spring Boot)
- Modules: Web, Validation, Spring Data JPA, Spring Data MongoDB, Flyway, Lombok
- Profiles:
  - **local** – Dockerized PostgreSQL & MongoDB
  - **prod** – AWS RDS (Postgres) + MongoDB Atlas
- Folders: