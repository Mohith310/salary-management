package com.acme.salary.seed;

import com.acme.salary.currency.CurrencyCatalog;
import com.acme.salary.employee.Employee;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Generates realistic, reproducible employees. The same seed always gives the same data, and hire dates use a
 * fixed window rather than the clock, so tests and demos are deterministic.
 */
public class EmployeeGenerator {

    private record Job(String title, String department, int minUsd, int maxUsd) {
    }

    /** payFactor scales US-level pay to the local market, so India and Brazil are cheaper than the US. */
    private record Market(String country, int weight, double payFactor) {
    }

    private static final List<Job> JOBS = List.of(
            new Job("Software Engineer", "Engineering", 80_000, 140_000),
            new Job("Senior Software Engineer", "Engineering", 120_000, 190_000),
            new Job("Engineering Manager", "Engineering", 150_000, 220_000),
            new Job("QA Engineer", "Engineering", 60_000, 110_000),
            new Job("DevOps Engineer", "Engineering", 90_000, 150_000),
            new Job("Data Analyst", "Data", 65_000, 115_000),
            new Job("Data Scientist", "Data", 100_000, 170_000),
            new Job("Product Manager", "Product", 110_000, 180_000),
            new Job("Product Designer", "Product", 70_000, 130_000),
            new Job("Sales Representative", "Sales", 50_000, 100_000),
            new Job("Account Executive", "Sales", 70_000, 140_000),
            new Job("Marketing Specialist", "Marketing", 55_000, 100_000),
            new Job("HR Specialist", "People", 50_000, 90_000),
            new Job("Recruiter", "People", 50_000, 95_000),
            new Job("Accountant", "Finance", 55_000, 100_000),
            new Job("Financial Analyst", "Finance", 70_000, 125_000),
            new Job("Operations Analyst", "Operations", 55_000, 100_000),
            new Job("Customer Support Agent", "Support", 35_000, 65_000));

    private static final List<Market> MARKETS = List.of(
            new Market("United States", 25, 1.00),
            new Market("India", 20, 0.30),
            new Market("United Kingdom", 12, 0.80),
            new Market("Germany", 10, 0.80),
            new Market("Canada", 8, 0.85),
            new Market("France", 6, 0.75),
            new Market("Australia", 6, 0.90),
            new Market("Singapore", 5, 0.85),
            new Market("Japan", 4, 0.70),
            new Market("Brazil", 4, 0.35));

    private static final int TOTAL_WEIGHT = MARKETS.stream().mapToInt(Market::weight).sum();

    private static final List<String> FIRST_NAMES = List.of(
            "Aarav", "Aisha", "Alex", "Ana", "Arjun", "Ben", "Carla", "Chen", "Chloe", "Daniel",
            "Diego", "Elena", "Emma", "Farah", "Gabriel", "Hana", "Hiro", "Ines", "Isaac", "Jack",
            "Jia", "Kavya", "Liam", "Lucas", "Maya", "Mei", "Mohammed", "Nina", "Noah", "Olivia",
            "Omar", "Priya", "Rahul", "Riya", "Sara", "Sofia", "Tariq", "Uma", "Victor", "Zoe");

    private static final List<String> LAST_NAMES = List.of(
            "Agarwal", "Ahmed", "Anderson", "Bose", "Brown", "Chatterjee", "Clark", "Costa", "Das", "Davis",
            "Dubois", "Evans", "Fischer", "Garcia", "Gupta", "Hall", "Ito", "Iyer", "Jones", "Kapoor",
            "Kim", "Kumar", "Lee", "Lopez", "Martin", "Meyer", "Miller", "Nair", "Nguyen", "Patel",
            "Reddy", "Rossi", "Sato", "Schmidt", "Sharma", "Silva", "Singh", "Tan", "Taylor", "Wilson");

    private static final LocalDate HIRE_WINDOW_START = LocalDate.of(2015, 1, 1);
    private static final LocalDate HIRE_WINDOW_END = LocalDate.of(2026, 6, 30);
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final long seed;
    private final CurrencyCatalog currencies;

    public EmployeeGenerator(long seed, CurrencyCatalog currencies) {
        this.seed = seed;
        this.currencies = currencies;
    }

    public List<Employee> generate(int count) {
        Random random = new Random(seed);
        long windowDays = ChronoUnit.DAYS.between(HIRE_WINDOW_START, HIRE_WINDOW_END) + 1;
        List<Employee> employees = new ArrayList<>(count);

        for (int i = 1; i <= count; i++) {
            String first = FIRST_NAMES.get(random.nextInt(FIRST_NAMES.size()));
            String last = LAST_NAMES.get(random.nextInt(LAST_NAMES.size()));
            Market market = pickMarket(random);
            Job job = JOBS.get(random.nextInt(JOBS.size()));
            CurrencyCatalog.CountryCurrency currency = currencies.find(market.country());

            employees.add(new Employee(
                    first + " " + last,
                    (first + "." + last + "." + i + "@acme.example").toLowerCase(Locale.ROOT),
                    job.title(),
                    job.department(),
                    currency.country(),
                    currency.currency(),
                    localSalary(random, job, market, currency),
                    HIRE_WINDOW_START.plusDays(random.nextLong(windowDays))));
        }
        return employees;
    }

    private static Market pickMarket(Random random) {
        int roll = random.nextInt(TOTAL_WEIGHT);
        for (Market market : MARKETS) {
            roll -= market.weight();
            if (roll < 0) {
                return market;
            }
        }
        throw new IllegalStateException("unreachable: weights always cover the roll");
    }

    /** Picks a US-level salary for the job, scales it to the market, converts to local currency, rounds to 100. */
    private static BigDecimal localSalary(Random random, Job job, Market market, CurrencyCatalog.CountryCurrency currency) {
        int usdBase = job.minUsd() + random.nextInt(job.maxUsd() - job.minUsd() + 1);
        BigDecimal usd = BigDecimal.valueOf(usdBase).multiply(BigDecimal.valueOf(market.payFactor()));
        BigDecimal local = usd.divide(currency.usdRate(), 0, RoundingMode.HALF_UP);
        return local.divide(HUNDRED, 0, RoundingMode.HALF_UP).multiply(HUNDRED).setScale(2, RoundingMode.UNNECESSARY);
    }
}
