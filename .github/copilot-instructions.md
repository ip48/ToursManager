## Quick summary

This repository is a small full-stack app: a Spring Boot backend (Java) and a React + Vite TypeScript frontend in `frontend/`.

Use these notes to be productive quickly: which files to read, how to build/run, and important project-specific conventions and mismatches to watch for.

## Developer Experience Level & Learning Goals

**Background:**
- Strong Java 8 knowledge, comfortable with SQL
- Experienced with J2EE but **new to Spring/Spring Boot**
- Learning journey: Spring Boot + AWS + MongoDB + AI agent workflows
- Solo project for hands-on learning from scratch

**Teaching Approach:**
- Explain Spring-specific patterns (dependency injection, annotations, auto-configuration)
- Compare to J2EE when relevant (e.g., Spring @RestController vs JAX-RS, Spring Data JPA vs EJB)
- For AWS integrations: explain concepts step-by-step (SDK setup, IAM, services)
- Prefer explicit examples over assumptions about Spring Boot "magic"
- Call out modern Java features (Java 17/21) that differ from Java 8 (records, switch expressions, var, etc.)

**Current Tech Stack (in transition):**
- Java 21 (migrating from references to Java 17)
- PostgreSQL (current), MongoDB (planned)
- AWS services (planned integrations - not yet implemented)
- Dev environment: Both local Windows (Java/Maven/Node installed) and Dev Containers available

## Big picture (what to know)
- Backend: Spring Boot app in `src/main/java/com/innatour/toursmanager` with the main class `ToursManagerApplication`.
- Frontend: Vite + React + TypeScript in `frontend/` (entry: `frontend/src/main.tsx`, main UI in `frontend/src/App.tsx`).
- Local dev uses Docker Compose (see `docker-compose.yml`) that brings up `app` (backend), `frontend`, and `postgres` with sensible env vars.
- API surface is minimal and uses `/api/*` paths — example: `GET /api/hello` implemented in `src/main/java/.../controller/HelloController.java`.

## How to run (concrete commands)
- Docker compose (recommended for quick parity with CI/dev env):

  From repo root:

  ```bash
  docker compose up --build
  # (or) docker-compose up --build
  ```

  Services exposed: frontend -> http://localhost:3000, backend -> http://localhost:8080

- Run backend locally (without Docker):

  ```bash
  # from repo root
  mvn -DskipTests package
  java -jar target/*.jar
  # or for dev
  mvn spring-boot:run
  ```

  If you need a local DB, run Postgres (docker-compose does this) or point `SPRING_DATASOURCE_URL` at your instance. The compose file uses `postgres:5432` and DB `toursdb`.

- Run frontend locally:

  ```bash
  cd frontend
  npm install
  npm run dev
  ```

  Note: the frontend's `build` script runs `tsc -b` then `vite build`.

## Debugging tips
- Backend: attach a debugger to `ToursManagerApplication` (standard Spring Boot main). You can start with remote debug flags when using `mvn spring-boot:run` via `-Dspring-boot.run.jvmArguments`.
- Frontend: Vite dev server supports HMR on port 3000; edit `frontend/src/App.tsx` to verify hot reload.

## Project-specific conventions & important details
- The top-level `README.md` mentions Java 21, MongoDB, Flyway, Lombok and other future tech. The authoritative build config is `pom.xml` — it currently targets Java 17 and includes Web, JPA, Postgres, H2. Prefer what is in `pom.xml` and source code over the README when automating edits.
- Environment-driven DB config: `application.properties` uses `spring.datasource.*` and the `docker-compose.yml` sets `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD` for the `app` service. Use these env vars when creating or running containers.
- API prefix convention: controllers are simple and return JSON or text; new endpoints typically live under `com.innatour.toursmanager.controller`.
- Frontend fetch pattern: an example fetch to `/api/hello` is present but commented out in `frontend/src/App.tsx` — follow that pattern for simple API calls.

## Integration points & external dependencies
- Local: Postgres is required by default (see `docker-compose.yml`). The README states MongoDB and AWS services are planned, but they are not yet wired into the codebase. If you add Mongo, also add Spring Data MongoDB dependency in `pom.xml`.
- Build: backend uses Maven; frontend uses node (npm) + Vite. CI or Dockerfiles expect both to be buildable independently.

## Files to inspect first (concrete examples)
- `pom.xml` — authoritative Java dependencies and build plugin.
- `docker-compose.yml` — local dev composition, service names, env vars (postgres, app, frontend).
- `src/main/resources/application.properties` — DB and JPA settings.
- `src/main/java/com/innatour/toursmanager/ToursManagerApplication.java` — application entry.
- `src/main/java/com/innatour/toursmanager/controller/HelloController.java` — smallest example endpoint.
- `frontend/package.json`, `frontend/src/main.tsx`, `frontend/src/App.tsx` — frontend entry points and scripts.

## Common tasks — explicit examples
- Add a new REST endpoint: create a controller in `com.innatour.toursmanager.controller`, annotate with `@RestController` and `@GetMapping("/api/your-path")`.
- Add a JPA repository: create an interface under `...repository` that extends `JpaRepository<MyEntity, Long>` and wire via constructor injection in services or controllers.
- Hook frontend to backend: enable the commented fetch in `frontend/src/App.tsx` and run frontend with `npm run dev`; proxying is done implicitly when both servers are reachable (use full URL in dev if needed).

## What to watch out for (gotchas)
- README vs code mismatch: README references features (MongoDB, Flyway, Java 21) that are not present in `pom.xml`. Don't assume those integrations exist.
- No test suite is currently present in the repo — running `mvn test` will likely be a no-op. If you add tests, update CI and Docker builds accordingly.

## When in doubt
- Prefer the build files and code (`pom.xml`, `frontend/package.json`, `Dockerfile`, `docker-compose.yml`) over prose docs. When changing environment-sensitive code, update `docker-compose.yml` and `application.properties` together.

---
If you want, I can now:
- open a small PR that adds this file (I will add it if you confirm), or
- extend these instructions with exact CI/Makefile steps if you provide the CI config.

Please tell me which sections need more detail or examples.
