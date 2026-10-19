package com.acme.salary.seed;

import com.acme.salary.currency.CurrencyCatalog;
import com.acme.salary.employee.Employee;
import com.acme.salary.employee.EmployeeRepository;
import java.sql.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Fills an empty database with generated employees on startup. Uses JDBC batches rather than JPA saves:
 * 10,000 rows load in a couple of seconds instead of tens of seconds.
 * Control it with app.seed.enabled, app.seed.count and app.seed.random-seed.
 */
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    private static final int BATCH_SIZE = 1_000;
    private static final String INSERT = """
            insert into employees (full_name, email, job_title, department, country, currency, salary, hire_date)
            values (?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private final EmployeeRepository repository;
    private final JdbcTemplate jdbc;
    private final CurrencyCatalog currencies;
    private final int count;
    private final long randomSeed;

    public DataSeeder(EmployeeRepository repository,
                      JdbcTemplate jdbc,
                      CurrencyCatalog currencies,
                      @Value("${app.seed.count:10000}") int count,
                      @Value("${app.seed.random-seed:42}") long randomSeed) {
        this.repository = repository;
        this.jdbc = jdbc;
        this.currencies = currencies;
        this.count = count;
        this.randomSeed = randomSeed;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (repository.count() > 0) {
            log.info("Seeding skipped: employees table already has data");
            return;
        }
        long start = System.nanoTime();
        List<Employee> employees = new EmployeeGenerator(randomSeed, currencies).generate(count);

        jdbc.batchUpdate(INSERT, employees, BATCH_SIZE, (ps, e) -> {
            ps.setString(1, e.getFullName());
            ps.setString(2, e.getEmail());
            ps.setString(3, e.getJobTitle());
            ps.setString(4, e.getDepartment());
            ps.setString(5, e.getCountry());
            ps.setString(6, e.getCurrency());
            ps.setBigDecimal(7, e.getSalary());
            ps.setDate(8, Date.valueOf(e.getHireDate()));
        });
        log.info("Seeded {} employees in {} ms", count, (System.nanoTime() - start) / 1_000_000);
    }
}
