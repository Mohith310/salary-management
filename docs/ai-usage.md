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

Tool: Claude (Claude Code). I gave it the assessment brief and worked through it step by step.


### Delivering the work as incremental commits
- "give me the zip file and give file by file for each commit"
- "give me the initial zip file before first commit, I will upload that to git repo as first commit, then give the updated file for next commit one by one so that I can proceed with all the commits"
- Pasted my git errors (no repository, no remote) and got the fix.
- "i am done with the commits to the repo, what should i do next"

### Verifying locally and fixing environment problems
- Pasted `mvn` not recognized, then my `java -version` (Java 8), and was told to install JDK 21 and Maven manually.
- Pasted `winget install Apache.Maven` failing, then `java -version` showing 21.
- Pasted Vite proxy errors (ECONNREFUSED) and then the backend startup log. This showed the backend simply wasn't running yet.
- Pasted the `mvn test` result (76 tests, 0 failures) and the `npm test` result (13 tests passing).

### 4. Deployment
- Pasted the Render Docker build log (the `npm ci` failure). The AI traced it to an out-of-sync `package-lock.json`, regenerated it and verified a clean install and build.
- Shared a screenshot of the failed and in-progress deploys, then confirmed "it is live".


## What I verified and corrected myself
- Ran the backend (76 tests) and frontend (13 tests) suites locally. All pass.
- Clicked through the app locally and on the live URL.
- Found while recording the demo that the employee count showed 5,000 on first load after a cold start, then 10,000. This is a seeding race on the free tier.
- Caught during development: a unit test found a UI bug (compact money labels showed "$85.0K"), fixed by adjusting the formatter.
- Caught during deployment: Google Fonts inlining hung the production build, and the lock file broke `npm ci` in Docker. Both were fixed.