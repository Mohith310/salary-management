package com.acme.salary.insights;

import java.math.BigDecimal;

/** Summary statistics for a set of salaries, all in one currency. */
public record SalaryStats(
        long headcount,
        BigDecimal min,
        BigDecimal max,
        BigDecimal average,
        BigDecimal median,
        BigDecimal total) {

    public static SalaryStats empty() {
        BigDecimal zero = BigDecimal.ZERO.setScale(SalaryStatistics.SCALE);
        return new SalaryStats(0, zero, zero, zero, zero, zero);
    }
}
