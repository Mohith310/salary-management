package com.acme.salary.seed;

import static org.assertj.core.api.Assertions.assertThat;

import com.acme.salary.currency.CurrencyCatalog;
import com.acme.salary.employee.Employee;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class EmployeeGeneratorTest {

    private final CurrencyCatalog catalog = new CurrencyCatalog();

    private List<Employee> generate(long seed, int count) {
        return new EmployeeGenerator(seed, catalog).generate(count);
    }

    @Test
    void generatesTheRequestedNumberOfEmployees() {
        assertThat(generate(42, 250)).hasSize(250);
    }

    @Test
    void sameSeedGivesIdenticalData() {
        List<Employee> first = generate(7, 200);
        List<Employee> second = generate(7, 200);

        for (int i = 0; i < first.size(); i++) {
            assertThat(second.get(i).getEmail()).isEqualTo(first.get(i).getEmail());
            assertThat(second.get(i).getSalary()).isEqualByComparingTo(first.get(i).getSalary());
            assertThat(second.get(i).getCountry()).isEqualTo(first.get(i).getCountry());
            assertThat(second.get(i).getHireDate()).isEqualTo(first.get(i).getHireDate());
        }
    }

    @Test
    void differentSeedsGiveDifferentData() {
        List<BigDecimal> a = generate(1, 100).stream().map(Employee::getSalary).toList();
        List<BigDecimal> b = generate(2, 100).stream().map(Employee::getSalary).toList();

        assertThat(a).isNotEqualTo(b);
    }

    @Test
    void emailsAreUnique() {
        List<Employee> employees = generate(42, 2_000);

        assertThat(employees.stream().map(Employee::getEmail).collect(Collectors.toSet())).hasSize(2_000);
    }

    @Test
    void everyEmployeeHasASupportedCountryWithItsMatchingCurrency() {
        for (Employee e : generate(42, 500)) {
            CurrencyCatalog.CountryCurrency market = catalog.find(e.getCountry());
            assertThat(e.getCurrency()).isEqualTo(market.currency());
        }
    }

    @Test
    void salariesArePositiveAndPlausibleWhenConvertedToUsd() {
        for (Employee e : generate(42, 1_000)) {
            BigDecimal usd = catalog.toUsd(e.getSalary(), e.getCurrency());
            assertThat(usd).isBetween(new BigDecimal("8000"), new BigDecimal("250000"));
        }
    }

    @Test
    void hireDatesFallInAFixedWindowSoRunsDoNotDependOnTheClock() {
        for (Employee e : generate(42, 500)) {
            assertThat(e.getHireDate()).isBetween(LocalDate.of(2015, 1, 1), LocalDate.of(2026, 6, 30));
        }
    }

    @Test
    void coversMultipleCountriesDepartmentsAndJobTitles() {
        List<Employee> employees = generate(42, 3_000);

        assertThat(employees.stream().map(Employee::getCountry).distinct().count()).isEqualTo(10);
        assertThat(employees.stream().map(Employee::getDepartment).distinct().count()).isGreaterThanOrEqualTo(8);
        assertThat(employees.stream().map(Employee::getJobTitle).distinct().count()).isGreaterThanOrEqualTo(15);
    }
}
