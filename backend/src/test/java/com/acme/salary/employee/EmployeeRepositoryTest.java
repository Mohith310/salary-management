package com.acme.salary.employee;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

/** Runs against an in-memory H2 that Flyway has migrated, so it also proves the schema matches the entity. */
@DataJpaTest
class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository repository;

    @BeforeEach
    void seed() {
        repository.saveAll(List.of(
                employee("Asha Rao", "asha.rao@acme.example", "Software Engineer", "Engineering", "India", "INR", "1800000"),
                employee("Bob Miller", "bob.miller@acme.example", "Designer", "Product", "United States", "USD", "95000"),
                employee("Chen Wei", "chen.wei@acme.example", "Software Engineer", "Engineering", "Singapore", "SGD", "120000"),
                employee("Dana Ross", "dana.ross@acme.example", "Accountant", "Finance", "United States", "USD", "70000")));
    }

    private static Employee employee(String name, String email, String title, String dept, String country,
                                     String currency, String salary) {
        return new Employee(name, email, title, dept, country, currency, new BigDecimal(salary), LocalDate.of(2021, 6, 1));
    }

    private List<Employee> find(EmployeeFilter filter) {
        return repository.findAll(EmployeeSpecifications.matching(filter));
    }

    @Test
    void emptyFilterReturnsEveryone() {
        assertThat(find(new EmployeeFilter(null, null, null, null, null, null))).hasSize(4);
    }

    @Test
    void blankFilterValuesAreIgnored() {
        assertThat(find(new EmployeeFilter("  ", "", " ", "", null, null))).hasSize(4);
    }

    @Test
    void filtersByCountry() {
        List<Employee> found = find(new EmployeeFilter(null, "United States", null, null, null, null));

        assertThat(found).extracting(Employee::getFullName).containsExactlyInAnyOrder("Bob Miller", "Dana Ross");
    }

    @Test
    void searchMatchesNameCaseInsensitively() {
        assertThat(find(new EmployeeFilter("RAO", null, null, null, null, null)))
                .extracting(Employee::getFullName).containsExactly("Asha Rao");
    }

    @Test
    void searchMatchesEmail() {
        assertThat(find(new EmployeeFilter("chen.wei@", null, null, null, null, null)))
                .extracting(Employee::getFullName).containsExactly("Chen Wei");
    }

    @Test
    void salaryRangeIsInclusiveOnBothEnds() {
        List<Employee> found = find(new EmployeeFilter(null, null, null, null, new BigDecimal("70000"), new BigDecimal("95000")));

        assertThat(found).extracting(Employee::getFullName).containsExactlyInAnyOrder("Bob Miller", "Dana Ross");
    }

    @Test
    void filtersCombineWithAnd() {
        List<Employee> found = find(new EmployeeFilter(null, "United States", "Finance", "Accountant", null, null));

        assertThat(found).extracting(Employee::getFullName).containsExactly("Dana Ross");
    }

    @Test
    void emailExistenceCheckIgnoresCase() {
        assertThat(repository.existsByEmailIgnoreCase("ASHA.RAO@acme.example")).isTrue();
        assertThat(repository.existsByEmailIgnoreCase("nobody@acme.example")).isFalse();
    }

    @Test
    void emailExistenceCheckCanExcludeTheEmployeeBeingEdited() {
        Long ashaId = repository.findAll(EmployeeSpecifications.matching(
                new EmployeeFilter("asha", null, null, null, null, null))).get(0).getId();

        assertThat(repository.existsByEmailIgnoreCaseAndIdNot("asha.rao@acme.example", ashaId)).isFalse();
        assertThat(repository.existsByEmailIgnoreCaseAndIdNot("asha.rao@acme.example", ashaId + 1000)).isTrue();
    }

    @Test
    void distinctFilterOptionsAreSorted() {
        assertThat(repository.findDistinctCountries()).containsExactly("India", "Singapore", "United States");
        assertThat(repository.findDistinctDepartments()).containsExactly("Engineering", "Finance", "Product");
        assertThat(repository.findDistinctJobTitles()).containsExactly("Accountant", "Designer", "Software Engineer");
    }
}
