# How AI was used

**Tool:** Claude Code (agentic), driven by me from the assessment brief.

## Workflow
1. **Requirements first.** I gave the brief and the HR Manager persona, then reviewed and cut the proposed scope. Out-of-scope items (auth, payroll, history, live FX, import) are written down with reasons in `requirements.md`.
2. **Design before code.** Data model, API outline, indexing and trade-offs went into `design.md` before any scaffolding.
3. **Tests first, then implementation.** Commits alternate `test:` then `feat:` per slice: statistics and currency, employee CRUD and filtering, seeder, insights, frontend core.
4. **Small slices, reviewed.** Each slice was a separate commit so the history shows how the solution grew.

## Where I corrected or constrained the AI
- Aggregations and filtering stay in SQL. The UI never loads all 10,000 rows.
- Fixed-rate currency table in one place, instead of a live FX API.
- A unit test caught a UI bug: compact money labels rendered `$85.0K`. Fixed in `money.ts` with `minimumFractionDigits: 0`.
- The production build tried to inline Google Fonts at build time, which fails on restricted networks. Disabled in `angular.json`; fonts load in the browser instead.

## Verification
- Backend: `mvn test` passes locally (76 tests, 0 failures) on Java 21.
- Frontend: `npm test` passes (13 tests).
- Manually checked the full flow in the browser against the seeded 10,000 employees.

## Prompts
_Add the key prompts you used here (requirements, design, test-first slices). Reviewers will ask about them._
