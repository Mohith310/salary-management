# ACME Salary Management

Web app for an HR Manager to manage salary data for ~10,000 employees and answer "how do we pay people?".
Spring Boot + H2 backend, Angular + Angular Material frontend, one deployable container.

Start with the docs: [requirements](docs/requirements.md) · [design notes](docs/design.md) · [AI usage](docs/ai-usage.md)

## Run locally

Requirements: Java 21, Maven 3.9+, Node 22+.

```bash
# Backend (seeds 10,000 employees on first start if the table is empty) -> http://localhost:8080
cd backend && mvn spring-boot:run

# Frontend with live reload and /api proxy -> http://localhost:4200
cd frontend && npm ci && npm start
```

## Run with Docker (what gets deployed)

```bash
docker build -t salary-management .
docker run -p 8080:8080 -v salary-data:/data salary-management
```

Open http://localhost:8080. Spring Boot serves the built Angular app and the API from the same origin, so there is no CORS setup.

## Tests

```bash
cd backend && mvn test          # service, repository, controller and seeder tests
cd frontend && npm test         # validators, money formatting, API client
```

## Configuration

| Variable | Default | Purpose |
|---|---|---|
| `PORT` | 8080 | HTTP port |
| `DB_PATH` | `./data/salary` | H2 file location |
| `app.seed.enabled` / `count` / `random-seed` | true / 10000 / 42 | Seeding (deterministic) |

## Deploying

Any host that runs a Dockerfile works (Render, Railway, Fly.io). Set `PORT` if the platform requires it and attach a persistent volume at `/data`. Without a volume the data re-seeds on each restart, which is fine for a demo.

## Deployed app and demo

- Live URL: https://salary-management-0wcy.onrender.com/
- Demo video: https://drive.google.com/file/d/1trMII_FEGOtX-Hby84UYpTXVWD8oqz_p/view?usp=sharing
