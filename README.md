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

## Tech Stack
| Layer | Technology | Purpose |
|-------|-------------|----------|
| **Frontend** | React 18 + TypeScript + Vite (or CRA) | User interface for guides and travelers |
| **Backend** | Java 21 + Spring Boot 3.3 | REST API, business logic, validation |
| **Databases** | PostgreSQL (RDS) + MongoDB (Atlas) | SQL for structured data, NoSQL for dynamic equipment lists |
| **Infrastructure** | AWS Free Tier | Hosting, RDS, S3 for assets (later), potential use of Bedrock AI |
| **Build/Deploy** | Maven + Docker + (Elastic Beanstalk or ECS later) | Local dev and cloud deployment |

## Backend (Spring Boot)
- Modules: Web, Validation, Spring Data JPA, Spring Data MongoDB, Flyway, Lombok
- Profiles:
  - **local** – Dockerized PostgreSQL & MongoDB
  - **prod** – AWS RDS (Postgres) + MongoDB Atlas
- Folders: