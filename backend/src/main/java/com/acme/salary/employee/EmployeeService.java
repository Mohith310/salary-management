package com.acme.salary.employee;

import com.acme.salary.common.InvalidRequestException;
import com.acme.salary.common.PageResponse;
import com.acme.salary.currency.CurrencyCatalog;
import java.util.Locale;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class EmployeeService {

    static final int MAX_PAGE_SIZE = 100;
    private static final Set<String> SORTABLE_FIELDS =
            Set.of("fullName", "email", "jobTitle", "department", "country", "salary", "hireDate");

    private final EmployeeRepository repository;
    private final CurrencyCatalog currencies;

    public EmployeeService(EmployeeRepository repository, CurrencyCatalog currencies) {
        this.repository = repository;
        this.currencies = currencies;
    }

    public PageResponse<EmployeeResponse> search(EmployeeFilter filter, int page, int size, String sortBy, String direction) {
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), MAX_PAGE_SIZE),
                sortFor(sortBy, direction));
        Page<Employee> result = repository.findAll(EmployeeSpecifications.matching(filter), pageable);
        return PageResponse.from(result.map(EmployeeResponse::from));
    }

    public EmployeeResponse get(long id) {
        return repository.findById(id).map(EmployeeResponse::from).orElseThrow(() -> new EmployeeNotFoundException(id));
    }

    @Transactional
    public EmployeeResponse create(EmployeeRequest request) {
        CurrencyCatalog.CountryCurrency market = currencies.find(request.country());
        String email = normaliseEmail(request.email());
        if (repository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateEmailException(email);
        }
        Employee employee = new Employee();
        apply(employee, request, email, market);
        return EmployeeResponse.from(repository.save(employee));
    }

    @Transactional
    public EmployeeResponse update(long id, EmployeeRequest request) {
        Employee employee = repository.findById(id).orElseThrow(() -> new EmployeeNotFoundException(id));
        CurrencyCatalog.CountryCurrency market = currencies.find(request.country());
        String email = normaliseEmail(request.email());
        if (repository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new DuplicateEmailException(email);
        }
        apply(employee, request, email, market);
        return EmployeeResponse.from(repository.save(employee));
    }

    @Transactional
    public void delete(long id) {
        if (!repository.existsById(id)) {
            throw new EmployeeNotFoundException(id);
        }
        repository.deleteById(id);
    }

    public FilterOptions filterOptions() {
        return new FilterOptions(
                repository.findDistinctCountries(),
                repository.findDistinctDepartments(),
                repository.findDistinctJobTitles());
    }

    private static Sort sortFor(String sortBy, String direction) {
        if (!SORTABLE_FIELDS.contains(sortBy)) {
            throw new InvalidRequestException("Cannot sort by '" + sortBy + "'. Allowed: " + SORTABLE_FIELDS);
        }
        Sort.Direction dir;
        if ("asc".equalsIgnoreCase(direction)) {
            dir = Sort.Direction.ASC;
        } else if ("desc".equalsIgnoreCase(direction)) {
            dir = Sort.Direction.DESC;
        } else {
            throw new InvalidRequestException("Sort direction must be 'asc' or 'desc'");
        }
        // The id tie-break keeps page boundaries stable when many rows share the same sort value.
        return Sort.by(dir, sortBy).and(Sort.by(Sort.Direction.ASC, "id"));
    }

    private static String normaliseEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private static void apply(Employee employee, EmployeeRequest request, String email, CurrencyCatalog.CountryCurrency market) {
        employee.setFullName(request.fullName().trim());
        employee.setEmail(email);
        employee.setJobTitle(request.jobTitle().trim());
        employee.setDepartment(request.department().trim());
        employee.setCountry(market.country());
        employee.setCurrency(market.currency());
        employee.setSalary(request.salary());
        employee.setHireDate(request.hireDate());
    }
}
