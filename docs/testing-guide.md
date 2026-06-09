# Testing Guide

## Phase 0

Run the test suite:

```bash
mvn test
```

Current test coverage verifies:

- Spring application context starts.
- `GET /health` returns service status metadata.
- `GET /actuator/health` returns `UP`.
