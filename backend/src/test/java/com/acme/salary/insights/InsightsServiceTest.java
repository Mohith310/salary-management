package com.acme.salary.insights;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.acme.salary.common.InvalidRequestException;
import com.acme.salary.currency.CurrencyCatalog;
import com.acme.salary.employee.EmployeeRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InsightsServiceTest {

    // INR converts at 0.012 to USD: 1,200,000 INR = 14,400 USD and 2,400,000 INR = 28,800 USD.
    private static final List<SalaryPoint> POINTS = List.of(
            point("India", "Engineering", "Software Engineer", "INR", "1200000"),
            point("India", "Engineering", "Software Engineer", "INR", "2400000"),
            point("United States", "Engineering", "Software Engineer", "USD", "100000"),
            point("United States", "Finance", "Accountant", "USD", "60000"));

    private static final List<SalaryPoint> INDIA_ONLY = POINTS.subList(0, 2);

    @Mock
    private EmployeeRepository repository;

    private InsightsService service;

    @BeforeEach
    void setUp() {
        service = new InsightsService(repository, new CurrencyCatalog());
    }

    private static SalaryPoint point(String country, String department, String title, String currency, String salary) {
        return new SalaryPoint(country, department, title, currency, new BigDecimal(salary));
    }

    @Test
    void byCountryReportsEachCountryInItsOwnCurrencySortedByName() {
        when(repository.findAllSalaryPoints()).thenReturn(POINTS);

        List<GroupInsight> groups = service.byCountry();

        assertThat(groups).extracting(GroupInsight::group).containsExactly("India", "United States");
        GroupInsight india = groups.get(0);
        assertThat(india.currency()).isEqualTo("INR");
        assertThat(india.stats().headcount()).isEqualTo(2);
        assertThat(india.stats().average()).isEqualByComparingTo("1800000");
        assertThat(groups.get(1).currency()).isEqualTo("USD");
        assertThat(groups.get(1).stats().average()).isEqualByComparingTo("80000");
    }

    @Test
    void byDepartmentWithoutCountryConvertsEverythingToUsd() {
        when(repository.findAllSalaryPoints()).thenReturn(POINTS);

        List<GroupInsight> groups = service.byDepartment(null);

        assertThat(groups).extracting(GroupInsight::group).containsExactly("Engineering", "Finance");
        GroupInsight engineering = groups.get(0);
        assertThat(engineering.currency()).isEqualTo("USD");
        assertThat(engineering.stats().headcount()).isEqualTo(3);
        assertThat(engineering.stats().min()).isEqualByComparingTo("14400");
        assertThat(engineering.stats().max()).isEqualByComparingTo("100000");
        assertThat(engineering.stats().median()).isEqualByComparingTo("28800");
    }

    @Test
    void byJobTitleWithCountryStaysInLocalCurrencyAndOnlyQueriesThatCountry() {
        when(repository.findSalaryPointsByCountry("India")).thenReturn(INDIA_ONLY);

        List<GroupInsight> groups = service.byJobTitle("india");

        assertThat(groups).hasSize(1);
        assertThat(groups.get(0).group()).isEqualTo("Software Engineer");
        assertThat(groups.get(0).currency()).isEqualTo("INR");
        assertThat(groups.get(0).stats().median()).isEqualByComparingTo("1800000");
        verify(repository, never()).findAllSalaryPoints();
    }

    @Test
    void blankCountryMeansNoCountryFilter() {
        when(repository.findAllSalaryPoints()).thenReturn(POINTS);

        assertThat(service.byJobTitle("  ")).extracting(GroupInsight::group)
                .containsExactly("Accountant", "Software Engineer");
    }

    @Test
    void unsupportedCountryIsRejected() {
        assertThatThrownBy(() -> service.byDepartment("Atlantis")).isInstanceOf(InvalidRequestException.class);
    }

    @Test
    void summaryAcrossTheCompanyIsInUsd() {
        when(repository.findAllSalaryPoints()).thenReturn(POINTS);

        GroupInsight summary = service.summary(null);

        assertThat(summary.group()).isEqualTo("All employees");
        assertThat(summary.currency()).isEqualTo("USD");
        assertThat(summary.stats().headcount()).isEqualTo(4);
        assertThat(summary.stats().total()).isEqualByComparingTo("203200");
        assertThat(summary.stats().average()).isEqualByComparingTo("50800");
    }

    @Test
    void summaryForACountryIsInLocalCurrency() {
        when(repository.findSalaryPointsByCountry("India")).thenReturn(INDIA_ONLY);

        GroupInsight summary = service.summary("India");

        assertThat(summary.group()).isEqualTo("India");
        assertThat(summary.currency()).isEqualTo("INR");
        assertThat(summary.stats().total()).isEqualByComparingTo("3600000");
    }

    @Test
    void distributionBucketsUsdSalariesIntoBands() {
        when(repository.findAllSalaryPoints()).thenReturn(POINTS);

        DistributionResponse distribution = service.distribution(null);

        assertThat(distribution.currency()).isEqualTo("USD");
        // 14,400 | 28,800 | 60,000 | 100,000
        assertThat(distribution.bands()).extracting(DistributionBand::headcount)
                .containsExactly(1L, 1L, 1L, 0L, 1L, 0L);
    }

    @Test
    void emptyDatabaseGivesEmptyGroupsAndAZeroSummary() {
        when(repository.findAllSalaryPoints()).thenReturn(List.of());

        assertThat(service.byCountry()).isEmpty();
        assertThat(service.byDepartment(null)).isEmpty();
        assertThat(service.summary(null).stats().headcount()).isZero();
    }
}
