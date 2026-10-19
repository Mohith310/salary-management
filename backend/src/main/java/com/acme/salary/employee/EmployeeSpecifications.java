package com.acme.salary.employee;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

/** Turns an {@link EmployeeFilter} into a SQL WHERE clause, so paging and counting happen in the database. */
public final class EmployeeSpecifications {

    private EmployeeSpecifications() {
    }

    public static Specification<Employee> matching(EmployeeFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(filter.q())) {
                String pattern = "%" + filter.q().trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("fullName")), pattern),
                        cb.like(cb.lower(root.get("email")), pattern)));
            }
            if (StringUtils.hasText(filter.country())) {
                predicates.add(cb.equal(root.get("country"), filter.country().trim()));
            }
            if (StringUtils.hasText(filter.department())) {
                predicates.add(cb.equal(root.get("department"), filter.department().trim()));
            }
            if (StringUtils.hasText(filter.jobTitle())) {
                predicates.add(cb.equal(root.get("jobTitle"), filter.jobTitle().trim()));
            }
            if (filter.minSalary() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("salary"), filter.minSalary()));
            }
            if (filter.maxSalary() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("salary"), filter.maxSalary()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
