# Project Intro Notes

## Phase 0 Talking Points

- This project demonstrates a production-style retail order backend, not a toy CRUD app.
- Java and Spring Boot are strong fits for retail systems because they support typed domain modeling, mature persistence, validation, test tooling, and operational endpoints.
- Every phase must run, pass tests, update documentation, and be commit-ready before the next phase begins.
- Phase 0 dependencies were selected to support REST APIs, relational persistence, validation, local H2 development, health checks, and automated testing.
- The app can be verified with `GET /actuator/health` and the custom `GET /health` endpoint.
