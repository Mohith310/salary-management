package com.acme.salary.insights;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.List;

/** Pure statistics calculation. No framework dependencies, so it is trivial to test. */
public final class SalaryStatistics {

    static final int SCALE = 2;

    private SalaryStatistics() {
    }

    public static SalaryStats of(Collection<BigDecimal> salaries) {
        if (salaries.isEmpty()) {
            return SalaryStats.empty();
        }
        List<BigDecimal> sorted = salaries.stream().sorted().toList();
        int count = sorted.size();
        BigDecimal total = sorted.stream().reduce(BigDecimal.ZERO, BigDecimal::add);

        return new SalaryStats(
                count,
                scaled(sorted.get(0)),
                scaled(sorted.get(count - 1)),
                total.divide(BigDecimal.valueOf(count), SCALE, RoundingMode.HALF_UP),
                median(sorted),
                scaled(total));
    }

    private static BigDecimal median(List<BigDecimal> sorted) {
        int count = sorted.size();
        int middle = count / 2;
        if (count % 2 == 1) {
            return scaled(sorted.get(middle));
        }
        return sorted.get(middle - 1).add(sorted.get(middle)).divide(BigDecimal.valueOf(2), SCALE, RoundingMode.HALF_UP);
    }

    private static BigDecimal scaled(BigDecimal value) {
        return value.setScale(SCALE, RoundingMode.HALF_UP);
    }
}
