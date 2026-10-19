# Requirements: ACME Salary Management

## Goal
Replace the Excel sheets the HR team uses today with a web app where the HR Manager can maintain salary data for ~10,000 employees across multiple countries and quickly answer "how do we pay people?".

**Persona:** one HR Manager. They know the data well, need to find and fix records fast, and need numbers they can quote in a meeting.

## Scope and features

**1. Employee records (replaces the spreadsheet)**
- List all employees with server-side pagination (25 per page), sorting and a free-text search on name or email.
- Filter by country, department, job title and salary range.
- Add, edit and delete an employee: name, email, job title, department, country, annual salary, hire date.
- Validation: required fields, valid email, unique email, salary greater than zero, hire date not in the future.

**2. Pay insights (answers the questions)**
- Headline numbers: headcount, total annual payroll, average and median salary.
- Min, max, average, median and headcount grouped by **country**, **department** and **job title**.
- Salary distribution across bands, to see how pay is spread.
- Optional country filter so a manager can ask "what do engineers earn in India?".

**3. Seed data**
- A deterministic script that generates 10,000 realistic employees across 10 countries, so the app is demoable on first start.

## Deliberately left out (and why)

| Left out | Reason |
|---|---|
| Login and user roles | There is a single persona. Auth is a real piece of work and would not change how pay is analysed. It is the first thing to add before real data goes in. |
| Payroll, tax, bonuses, equity | A different problem. Base annual salary is what answers "how do we pay people". |
| Salary history and audit trail | Valuable, but v2. It needs its own data model and UI, and the current-state questions come first. |
| Live currency conversion | Rates change daily and need an external service. A fixed, documented rate table is enough for comparing across countries. |
| Excel/CSV import | The aim is to move off Excel. The seed script covers initial data, and a one-off import can be done by the team. |
| Charts library, reports export, i18n | Tables and simple bars answer the questions. Extras can follow once usage shows what is needed. |

## Assumptions
- Salary is the **annual base salary in the employee's local currency**. Each country has one currency.
- Per-country insights are shown in local currency. Cross-country insights (department, job title, company-wide) are converted to **USD with fixed rates** held in one place in the code, and are labelled as such.
- Median is the middle value, or the average of the two middle values when the count is even.
- 10,000 rows is small for a database. Filtering and paging happen in SQL; the UI never loads all rows.

## Success criteria
- The HR Manager can find any employee in under 10 seconds and edit them in under a minute.
- All insights load in under a second on the 10,000-employee dataset.
- Core logic (statistics, validation, filtering, seeding) is covered by fast, deterministic unit tests.
