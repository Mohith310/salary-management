package com.acme.salary.employee;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.acme.salary.common.InvalidRequestException;
import com.acme.salary.common.PageResponse;
import com.acme.salary.currency.CurrencyCatalog;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    private static final EmployeeFilter NO_FILTER = new EmployeeFilter(null, null, null, null, null, null);

    @Mock
    private EmployeeRepository repository;

    private EmployeeService service;

    @BeforeEach
    void setUp() {
        service = new EmployeeService(repository, new CurrencyCatalog());
    }

    private static EmployeeRequest request(String email, String country) {
        return new EmployeeRequest("Asha Rao", email, "Software Engineer", "Engineering", country,
                new BigDecimal("1800000"), LocalDate.of(2022, 3, 1));
    }

    private static Employee existing() {
        return new Employee("Old Name", "old@acme.example", "Designer", "Product", "India", "INR",
                new BigDecimal("900000.00"), LocalDate.of(2020, 1, 15));
    }

    // ---- create ----

    @Test
    void createNormalisesEmailAndDerivesCountryAndCurrency() {
        when(repository.existsByEmailIgnoreCase("asha.rao@acme.example")).thenReturn(false);
        when(repository.save(any(Employee.class))).thenAnswer(call -> call.getArgument(0));

        EmployeeResponse created = service.create(request("  Asha.Rao@ACME.example ", "india"));

        assertThat(created.email()).isEqualTo("asha.rao@acme.example");
        assertThat(created.country()).isEqualTo("India");
        assertThat(created.currency()).isEqualTo("INR");
        assertThat(created.salary()).isEqualByComparingTo("1800000");
    }

    @Test
    void createRejectsDuplicateEmail() {
        when(repository.existsByEmailIgnoreCase("asha@acme.example")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request("asha@acme.example", "India")))
                .isInstanceOf(DuplicateEmailException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void createRejectsUnsupportedCountry() {
        assertThatThrownBy(() -> service.create(request("asha@acme.example", "Atlantis")))
                .isInstanceOf(InvalidRequestException.class);
        verify(repository, never()).save(any());
    }

    // ---- update ----

    @Test
    void updateOverwritesFieldsOfTheExistingEmployee() {
        when(repository.findById(5L)).thenReturn(Optional.of(existing()));
        when(repository.existsByEmailIgnoreCaseAndIdNot("asha@acme.example", 5L)).thenReturn(false);
        when(repository.save(any(Employee.class))).thenAnswer(call -> call.getArgument(0));

        EmployeeResponse updated = service.update(5L, request("asha@acme.example", "Germany"));

        assertThat(updated.fullName()).isEqualTo("Asha Rao");
        assertThat(updated.country()).isEqualTo("Germany");
        assertThat(updated.currency()).isEqualTo("EUR");
    }

    @Test
    void updateOfMissingEmployeeFails() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(99L, request("asha@acme.example", "India")))
                .isInstanceOf(EmployeeNotFoundException.class);
    }

    @Test
    void updateCannotTakeAnotherEmployeesEmail() {
        when(repository.findById(5L)).thenReturn(Optional.of(existing()));
        when(repository.existsByEmailIgnoreCaseAndIdNot("taken@acme.example", 5L)).thenReturn(true);

        assertThatThrownBy(() -> service.update(5L, request("taken@acme.example", "India")))
                .isInstanceOf(DuplicateEmailException.class);
        verify(repository, never()).save(any());
    }

    // ---- delete / get ----

    @Test
    void deleteOfMissingEmployeeFails() {
        when(repository.existsById(7L)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(7L)).isInstanceOf(EmployeeNotFoundException.class);
        verify(repository, never()).deleteById(any());
    }

    @Test
    void deleteRemovesExistingEmployee() {
        when(repository.existsById(7L)).thenReturn(true);

        service.delete(7L);

        verify(repository).deleteById(7L);
    }

    @Test
    void getOfMissingEmployeeFails() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(1L)).isInstanceOf(EmployeeNotFoundException.class);
    }

    // ---- search ----

    @Test
    void searchRejectsUnknownSortField() {
        assertThatThrownBy(() -> service.search(NO_FILTER, 0, 25, "password", "asc"))
                .isInstanceOf(InvalidRequestException.class);
    }

    @Test
    void searchRejectsUnknownSortDirection() {
        assertThatThrownBy(() -> service.search(NO_FILTER, 0, 25, "salary", "sideways"))
                .isInstanceOf(InvalidRequestException.class);
    }

    @Test
    void searchCapsPageSizeAndAppliesRequestedSortWithStableTieBreak() {
        when(repository.findAll(ArgumentMatchers.<Specification<Employee>>any(), any(Pageable.class)))
                .thenReturn(Page.empty());

        PageResponse<EmployeeResponse> result = service.search(NO_FILTER, -3, 500, "salary", "desc");

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAll(ArgumentMatchers.<Specification<Employee>>any(), pageable.capture());
        assertThat(pageable.getValue().getPageSize()).isEqualTo(100);
        assertThat(pageable.getValue().getPageNumber()).isZero();
        assertThat(pageable.getValue().getSort().getOrderFor("salary").getDirection()).isEqualTo(Sort.Direction.DESC);
        assertThat(pageable.getValue().getSort().getOrderFor("id")).isNotNull();
        assertThat(result.content()).isEmpty();
    }

    // ---- filter options ----

    @Test
    void filterOptionsComeFromDistinctValuesInTheDatabase() {
        when(repository.findDistinctCountries()).thenReturn(List.of("India", "Japan"));
        when(repository.findDistinctDepartments()).thenReturn(List.of("Engineering"));
        when(repository.findDistinctJobTitles()).thenReturn(List.of("Designer", "Software Engineer"));

        FilterOptions options = service.filterOptions();

        assertThat(options.countries()).containsExactly("India", "Japan");
        assertThat(options.departments()).containsExactly("Engineering");
        assertThat(options.jobTitles()).containsExactly("Designer", "Software Engineer");
    }
}
