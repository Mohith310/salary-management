package com.acme.salary.insights;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class SalaryDistributionTest {

    private static List<BigDecimal> values(String... v) {
        return java.util.Arrays.stream(v).map(BigDecimal::new).toList();
    }

    @Test
    void alwaysReturnsAllBandsInOrderEvenWhenEmpty() {
        List<DistributionBand> bands = SalaryDistribution.of(List.of());

        assertThat(bands).extracting(DistributionBand::label)
                .containsExactly("Under 25k", "25k-50k", "50k-75k", "75k-100k", "100k-150k", "150k+");
        assertThat(bands).extracting(DistributionBand::headcount).containsOnly(0L);
    }

    @Test
    void placesValuesInTheirBands() {
        List<DistributionBand> bands = SalaryDistribution.of(values("10000", "30000", "31000", "80000", "120000", "400000"));

        assertThat(bands).extracting(DistributionBand::headcount).containsExactly(1L, 2L, 0L, 1L, 1L, 1L);
    }

    @Test
    void lowerBoundIsInclusiveAndUpperBoundIsExclusive() {
        List<DistributionBand> bands = SalaryDistribution.of(values("24999.99", "25000", "149999.99", "150000"));

        assertThat(bands).extracting(DistributionBand::headcount).containsExactly(1L, 1L, 0L, 0L, 1L, 1L);
    }
}
