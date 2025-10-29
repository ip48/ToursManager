# ToursManager - Product Backlog

## Sprint Planning
- **Current Sprint**: Sprint 1
- **Sprint Duration**: 2 weeks
- **Sprint Goal**: Set up basic project structure and core entities

---

## Backlog Items

### Epic: Project Setup
- [ ] **SETUP-001** - Configure Spring Boot project structure
  - **Priority**: High
  - **Story Points**: 3
  - **Status**: In Progress
  - **Description**: Set up Maven, dependencies, and basic application structure

- [ ] **SETUP-002** - Configure database connections (PostgreSQL + MongoDB)
  - **Priority**: High
  - **Story Points**: 5
  - **Status**: To Do
  - **Description**: Set up dual database configuration for structured and dynamic data

### Epic: Core Domain Models
- [ ] **MODEL-001** - Create Guide entity and repository
  - **Priority**: High
  - **Story Points**: 3
  - **Status**: To Do
  - **Description**: Basic guide model with authentication fields

- [ ] **MODEL-002** - Create Tour entity and repository
  - **Priority**: High
  - **Story Points**: 5
  - **Status**: To Do
  - **Description**: Tour model with guide relationship and basic tour information

- [ ] **MODEL-003** - Create Traveler entity and repository
  - **Priority**: High
  - **Story Points**: 3
  - **Status**: To Do
  - **Description**: Traveler model with registration capabilities

- [ ] **MODEL-004** - Create Packing Checklist MongoDB document
  - **Priority**: Medium
  - **Story Points**: 5
  - **Status**: To Do
  - **Description**: Flexible checklist structure stored in MongoDB

### Epic: REST API Endpoints
- [ ] **API-001** - Guide management endpoints
  - **Priority**: High
  - **Story Points**: 8
  - **Status**: To Do
  - **Description**: CRUD operations for guide management

- [ ] **API-002** - Tour management endpoints
  - **Priority**: High
  - **Story Points**: 8
  - **Status**: To Do
  - **Description**: CRUD operations for tour management

- [ ] **API-003** - Traveler registration endpoints
  - **Priority**: Medium
  - **Story Points**: 5
  - **Status**: To Do
  - **Description**: Registration and profile management for travelers

### Epic: Business Logic
- [ ] **BIZ-001** - Tour registration workflow
  - **Priority**: Medium
  - **Story Points**: 8
  - **Status**: To Do
  - **Description**: Complete workflow for travelers to register for tours

- [ ] **BIZ-002** - Packing checklist management
  - **Priority**: Medium
  - **Story Points**: 5
  - **Status**: To Do
  - **Description**: Dynamic checklist creation and management per tour

### Epic: Infrastructure & Deployment
- [ ] **INFRA-001** - Docker configuration for local development
  - **Priority**: Medium
  - **Story Points**: 5
  - **Status**: To Do
  - **Description**: Docker Compose for PostgreSQL and MongoDB

- [ ] **INFRA-002** - AWS deployment configuration
  - **Priority**: Low
  - **Story Points**: 13
  - **Status**: To Do
  - **Description**: Prepare for AWS deployment with RDS and MongoDB Atlas

---

## Completed Items
<!-- Move completed items here with completion date -->

---

## Notes
- Add your tasks here following the format above
- Use story points (1, 2, 3, 5, 8, 13, 21) for estimation
- Priority levels: High, Medium, Low
- Status options: To Do, In Progress, In Review, Done
- Update sprint goals and current sprint as needed

---

## Definition of Done
- [ ] Code is written and tested
- [ ] Unit tests pass
- [ ] Code review completed
- [ ] Documentation updated
- [ ] Feature tested in local environment