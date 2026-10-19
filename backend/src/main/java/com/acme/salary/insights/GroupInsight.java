package com.acme.salary.insights;

/** Statistics for one group (a country, department, job title, or the whole company) in one currency. */
public record GroupInsight(String group, String currency, SalaryStats stats) {
}
