package com.acme.salary.employee;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EmployeeResponse(
        Long id,
        String fullName,
        String email,
        String jobTitle,
        String department,
        String country,
        String currency,
        BigDecimal salary,
        LocalDate hireDate) {

    public static EmployeeResponse from(Employee e) {
        return new EmployeeResponse(e.getId(), e.getFullName(), e.getEmail(), e.getJobTitle(), e.getDepartment(),
                e.getCountry(), e.getCurrency(), e.getSalary(), e.getHireDate());
    }
}
