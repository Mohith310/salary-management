package com.acme.salary.insights;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** Buckets USD salaries into fixed bands. A band includes its lower bound and excludes its upper bound. */
public final class SalaryDistribution {

    private static final int[] UPPER_BOUNDS = {25_000, 50_000, 75_000, 100_000, 150_000};
    private static final String[] LABELS = {"Under 25k", "25k-50k", "50k-75k", "75k-100k", "100k-150k", "150k+"};

    private SalaryDistribution() {
    }

    public static List<DistributionBand> of(Collection<BigDecimal> usdSalaries) {
        long[] counts = new long[LABELS.length];
        for (BigDecimal salary : usdSalaries) {
            counts[bandIndex(salary)]++;
        }
        List<DistributionBand> bands = new ArrayList<>(LABELS.length);
        for (int i = 0; i < LABELS.length; i++) {
            bands.add(new DistributionBand(LABELS[i], counts[i]));
        }
        return bands;
    }

    private static int bandIndex(BigDecimal salary) {
        for (int i = 0; i < UPPER_BOUNDS.length; i++) {
            if (salary.compareTo(BigDecimal.valueOf(UPPER_BOUNDS[i])) < 0) {
                return i;
            }
        }
        return UPPER_BOUNDS.length;
    }
}
