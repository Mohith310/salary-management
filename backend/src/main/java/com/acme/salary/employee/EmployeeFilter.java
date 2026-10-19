package com.acme.salary.employee;

import java.math.BigDecimal;

/** Optional criteria for the employee list. Null or blank means "do not filter on this". */
public record EmployeeFilter(
        String q,
        String country,
        String department,
        String jobTitle,
        BigDecimal minSalary,
        BigDecimal maxSalary) {
}
