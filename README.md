# soen345-meow
Group Project for SOEN 345

# Project Report
## Software Development Method

For this project, our team plans to adopt an Agile Scrum-based approach. Since the system will be developed over the course of the semester, an iterative process will allow us to gradually build core features, test them, and refine the design based on feedback and validation results.

We intend to divide the work into short sprints aligned with the bi-weekly progress reports. In each sprint, we will focus on a small set of functional requirements, such as user registration, event browsing, search and filtering, reservation handling, or administrator controls. This incremental approach will help us avoid last-minute integration issues and maintain a stable and organized codebase throughout development.

Before implementation, we will clearly analyze the requirements and define a basic system architecture. The application will follow a layered structure separating presentation logic, business logic, and data management. This separation will improve maintainability and make the system easier to test.

For collaboration and version control, we will use GitHub, with feature branches and pull requests to ensure clean integration of code. We will also configure GitHub Actions to support Continuous Integration (CI), automatically building the project and executing tests whenever new changes are pushed.

## Software Testing Method

Testing will be treated as an essential and continuous activity throughout development, not as a final step. Since this course focuses on software testing and quality assurance, we plan to follow a structured testing strategy from the beginning.

We intend to apply Test-Driven Development (TDD) for core components whenever possible. This means writing unit tests before implementing the corresponding functionality. By defining expected behavior in advance, we can better clarify requirements and reduce logical errors during implementation.

For unit and component testing, we will use JUnit 5. Individual classes and methods will be tested in isolation, including input validation, event filtering logic, reservation creation and cancellation, and administrator operations. Edge cases such as invalid inputs, empty searches, and concurrent booking attempts will also be considered to ensure robustness.

In addition to unit testing, we will perform functional and acceptance testing to validate complete user workflows. These tests will verify that the system satisfies the original requirements, such as registering an account, browsing events, reserving tickets, canceling reservations, and managing events as an administrator. Each functional requirement will be mapped to one or more test cases to ensure proper coverage.

Continuous Integration (CI) will play a key role in our testing strategy. All tests will run automatically through GitHub Actions on every push and pull request. This ensures that new changes do not break existing functionality and helps maintain code quality as the project evolves.

Overall, our testing approach aims to ensure correctness, reliability, and maintainability while aligning with the quality assurance principles emphasized in SOEN 345.

## Deployment (NFR-2)

This project supports cloud deployment for backend and frontend services.

### Backend (Railway)

- Service root directory: `backend/`
- Start command: `./gradlew bootRun`
- Required environment variables:
	- `SPRING_PROFILES_ACTIVE=prod`
	- `JWT_SECRET=<secure-random-value>`
	- `DATABASE_URL=<provider-db-url>` (optional if keeping SQLite)
	- `DB_DRIVER` and `HIBERNATE_DIALECT` (optional overrides)
	- `MAILTRAP_USERNAME`, `MAILTRAP_PASSWORD` (if Mailtrap is used in cloud)

`backend/src/main/resources/application-prod.properties` contains the production profile defaults and env-var mapping.

### Frontend (Vercel or Railway Static Service)

- Service root directory: `frontend/`
- Build command: `npm run build`
- Output directory: `dist/`

### CI Deploy Hooks

`.github/workflows/ci.yml` triggers deploy hooks on push to `main` after backend/frontend builds pass.

Add these GitHub repository secrets:

- `RAILWAY_DEPLOY_HOOK_URL`
- `FRONTEND_DEPLOY_HOOK_URL`

If either secret is missing, the deploy job is skipped without failing CI.

### Live URLs

- Backend API URL: `https://soen345-meow-production.up.railway.app/`
- Frontend URL: `TBD (pending Vercel deployment by repo owner/member)`
