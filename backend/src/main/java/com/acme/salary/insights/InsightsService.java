package com.acme.salary.insights;

import com.acme.salary.currency.CurrencyCatalog;
import com.acme.salary.employee.EmployeeRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Answers "how do we pay people?".
 *
 * <p>Currency rule: when a country is given, every salary in scope shares one currency, so results stay in that
 * local currency. Without a country, salaries are converted to USD at the catalog's fixed rates.
 */
@Service
@Transactional(readOnly = true)
public class InsightsService {

    private static final String USD = "USD";

    private final EmployeeRepository repository;
    private final CurrencyCatalog currencies;

    public InsightsService(EmployeeRepository repository, CurrencyCatalog currencies) {
        this.repository = repository;
        this.currencies = currencies;
    }

    public GroupInsight summary(String country) {
        String scope = canonicalCountry(country);
        boolean local = scope != null;
        List<BigDecimal> amounts = load(scope).stream().map(p -> amount(p, local)).toList();
        return new GroupInsight(local ? scope : "All employees", currencyFor(scope), SalaryStatistics.of(amounts));
    }

    /** One group per country, each in its own local currency. */
    public List<GroupInsight> byCountry() {
        Map<String, List<SalaryPoint>> byCountry = repository.findAllSalaryPoints().stream()
                .collect(Collectors.groupingBy(SalaryPoint::country, TreeMap::new, Collectors.toList()));
        return byCountry.entrySet().stream()
                .map(entry -> new GroupInsight(
                        entry.getKey(),
                        entry.getValue().get(0).currency(),
                        SalaryStatistics.of(entry.getValue().stream().map(SalaryPoint::salary).toList())))
                .toList();
    }

    public List<GroupInsight> byDepartment(String country) {
        return grouped(country, SalaryPoint::department);
    }

    public List<GroupInsight> byJobTitle(String country) {
        return grouped(country, SalaryPoint::jobTitle);
    }

    public DistributionResponse distribution(String country) {
        List<BigDecimal> usdSalaries = load(canonicalCountry(country)).stream()
                .map(p -> currencies.toUsd(p.salary(), p.currency()))
                .toList();
        return new DistributionResponse(USD, SalaryDistribution.of(usdSalaries));
    }

    private List<GroupInsight> grouped(String country, Function<SalaryPoint, String> groupKey) {
        String scope = canonicalCountry(country);
        boolean local = scope != null;
        String currency = currencyFor(scope);

        Map<String, List<BigDecimal>> groups = load(scope).stream()
                .collect(Collectors.groupingBy(groupKey, TreeMap::new,
                        Collectors.mapping(p -> amount(p, local), Collectors.toList())));

        return groups.entrySet().stream()
                .map(entry -> new GroupInsight(entry.getKey(), currency, SalaryStatistics.of(entry.getValue())))
                .toList();
    }

    /** Returns the catalog's spelling of the country, or null when no country filter was requested. */
    private String canonicalCountry(String country) {
        return StringUtils.hasText(country) ? currencies.find(country).country() : null;
    }

    private String currencyFor(String canonicalCountry) {
        return canonicalCountry == null ? USD : currencies.find(canonicalCountry).currency();
    }

    private List<SalaryPoint> load(String canonicalCountry) {
        return canonicalCountry == null
                ? repository.findAllSalaryPoints()
                : repository.findSalaryPointsByCountry(canonicalCountry);
    }

    private BigDecimal amount(SalaryPoint point, boolean local) {
        return local ? point.salary() : currencies.toUsd(point.salary(), point.currency());
    }
}
