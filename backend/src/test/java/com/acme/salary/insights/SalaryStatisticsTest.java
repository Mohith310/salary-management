package com.acme.salary.insights;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class SalaryStatisticsTest {

    private static List<BigDecimal> salaries(String... values) {
        return java.util.Arrays.stream(values).map(BigDecimal::new).toList();
    }

    @Test
    void emptyInputGivesZeroHeadcountAndZeroValues() {
        SalaryStats stats = SalaryStatistics.of(List.of());

        assertThat(stats.headcount()).isZero();
        assertThat(stats.min()).isEqualByComparingTo("0");
        assertThat(stats.max()).isEqualByComparingTo("0");
        assertThat(stats.average()).isEqualByComparingTo("0");
        assertThat(stats.median()).isEqualByComparingTo("0");
        assertThat(stats.total()).isEqualByComparingTo("0");
    }

    @Test
    void singleValueIsMinMaxAverageAndMedian() {
        SalaryStats stats = SalaryStatistics.of(salaries("50000"));

        assertThat(stats.headcount()).isEqualTo(1);
        assertThat(stats.min()).isEqualByComparingTo("50000");
        assertThat(stats.max()).isEqualByComparingTo("50000");
        assertThat(stats.average()).isEqualByComparingTo("50000");
        assertThat(stats.median()).isEqualByComparingTo("50000");
    }

    @Test
    void medianOfOddCountIsTheMiddleValueRegardlessOfInputOrder() {
        SalaryStats stats = SalaryStatistics.of(salaries("90000", "30000", "60000"));

        assertThat(stats.median()).isEqualByComparingTo("60000");
    }

    @Test
    void medianOfEvenCountIsTheMeanOfTheTwoMiddleValues() {
        SalaryStats stats = SalaryStatistics.of(salaries("40000", "10000", "30000", "20000"));

        assertThat(stats.median()).isEqualByComparingTo("25000");
    }

    @Test
    void computesMinMaxTotalAndAverage() {
        SalaryStats stats = SalaryStatistics.of(salaries("100", "200", "600"));

        assertThat(stats.min()).isEqualByComparingTo("100");
        assertThat(stats.max()).isEqualByComparingTo("600");
        assertThat(stats.total()).isEqualByComparingTo("900");
        assertThat(stats.average()).isEqualByComparingTo("300");
    }

    @Test
    void averageIsRoundedHalfUpToTwoDecimals() {
        SalaryStats stats = SalaryStatistics.of(salaries("1", "1", "2"));

        assertThat(stats.average()).isEqualByComparingTo("1.33");
    }

    @Test
    void doesNotModifyTheInputList() {
        List<BigDecimal> input = salaries("3", "1", "2");

        SalaryStatistics.of(input);

        assertThat(input).containsExactly(new BigDecimal("3"), new BigDecimal("1"), new BigDecimal("2"));
    }
}
