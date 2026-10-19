# Design Notes

## Architecture

```
Angular 20+ SPA (Angular Material)  --HTTP/JSON-->  Spring Boot 3 REST API  -->  H2 (file mode)
        served as static files by Spring Boot             Flyway migrations
```

One deployable unit: the Angular build is copied into Spring Boot's `static/` folder inside a multi-stage Docker image. This avoids CORS, keeps deployment to a single free-tier service, and is the simplest thing that works for one user persona.

### Backend packages (`com.acme.salary`)
| Package | Responsibility |
|---|---|
| `employee` | Entity, repository, service, controller, request/response DTOs, filter specification |
| `insights` | Statistics calculation, currency conversion, insights service and controller |
| `seed` | Deterministic employee generator and the startup seeder |
| `common` | Error handling (RFC 7807 problem responses), SPA forwarding |

Rules followed: controllers only translate HTTP, services hold business rules, and the statistics maths lives in plain classes with no Spring dependency so it is tested without a context.

## Data model

`employee`
| Column | Type | Notes |
|---|---|---|
| id | bigint PK | generated |
| full_name | varchar(150) | not null |
| email | varchar(200) | not null, **unique** |
| job_title | varchar(100) | not null, indexed |
| department | varchar(100) | not null, indexed |
| country | varchar(60) | not null, indexed |
| currency | char(3) | derived from country, not null |
| salary | decimal(14,2) | annual, local currency, > 0 |
| hire_date | date | not null |

Currency is stored per row so that a future change (an employee paid in a different currency from their country) needs no migration.

## API

| Method | Path | Purpose |
|---|---|---|
| GET | `/api/employees` | Page of employees. Params: `page`, `size`, `sort`, `q`, `country`, `department`, `jobTitle`, `minSalary`, `maxSalary` |
| GET | `/api/employees/{id}` | One employee |
| POST | `/api/employees` | Create (201) |
| PUT | `/api/employees/{id}` | Update |
| DELETE | `/api/employees/{id}` | Delete (204) |
| GET | `/api/employees/filter-options` | Distinct countries, departments, job titles for dropdowns |
| GET | `/api/insights/summary` | Company-wide headcount, payroll, average, median (USD) |
| GET | `/api/insights/by-country` | Stats per country, local currency |
| GET | `/api/insights/by-department` | Stats per department. Optional `country` |
| GET | `/api/insights/by-job-title` | Stats per job title. Optional `country` |
| GET | `/api/insights/distribution` | Headcount per salary band (USD). Optional `country` |

Errors use `application/problem+json` with a `fieldErrors` map for validation failures.

## Performance considerations
- **List screen:** pagination, sorting and filters are executed in SQL (`Pageable` plus JPA `Specification`). Indexes on country, department, job title and email keep filters fast.
- **Search:** `LIKE` on name and email. Fine for 10,000 rows. For millions of rows this becomes a full-text index (Postgres `pg_trgm` or similar).
- **Insights:** one lightweight query returns only `(country, department, job title, currency, salary)` for the requested scope, and the statistics are computed in a single pass in Java. For 10,000 rows this is a few milliseconds and keeps the median logic simple and unit-testable.
- **Seeding:** JDBC batch inserts (1,000 per batch) instead of JPA saves, so 10,000 rows load in well under a few seconds.

## Trade-offs
| Decision | Alternative | Why this one |
|---|---|---|
| Median computed in Java | SQL `PERCENTILE_CONT` | The function differs per database and is awkward to test. The Java version is portable and fully covered by unit tests. The cost is moving 10,000 small rows, which is negligible. If rows grow 100x, move to SQL percentiles or pre-aggregated tables. |
| Fixed currency rates | Live rates API | Removes an external dependency and keeps numbers reproducible. Rates are in one class, so swapping is simple. |
| H2 file database | SQLite or Postgres | Zero setup for reviewers, runs embedded, and the schema is plain SQL via Flyway so moving to Postgres is a config change plus a driver. |
| Single container | Separate frontend and backend deploys | One URL, no CORS, one thing to deploy. |
| No authentication | Spring Security | Out of scope for the stated persona. Documented as the first follow-up. |

## Testing strategy
- **Unit (no Spring):** statistics, currency conversion, employee generator, services with mocked repositories.
- **Slice:** `@WebMvcTest` for request validation and status codes, `@DataJpaTest` for the filter specification against H2.
- **Frontend:** service and component tests with Angular's test runner and an HTTP testing controller.
- All tests are deterministic: the generator takes a seed, and no test depends on the clock or on network access.
