package com.acme.salary.insights;

import java.math.BigDecimal;

/** The few columns insights need from each employee, so we never load full entities for statistics. */
public record SalaryPoint(String country, String department, String jobTitle, String currency, BigDecimal salary) {
}
