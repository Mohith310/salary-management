package com.acme.salary.currency;

import com.acme.salary.common.InvalidRequestException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * The countries ACME operates in, the currency each one pays in, and a FIXED rate to USD.
 * Rates are deliberately static so reports are reproducible; swap this class to use live rates.
 */
@Component
public class CurrencyCatalog {

    public record CountryCurrency(String country, String currency, BigDecimal usdRate) {
    }

    private static final List<CountryCurrency> ENTRIES = List.of(
            entry("Australia", "AUD", "0.66"),
            entry("Brazil", "BRL", "0.20"),
            entry("Canada", "CAD", "0.73"),
            entry("France", "EUR", "1.08"),
            entry("Germany", "EUR", "1.08"),
            entry("India", "INR", "0.012"),
            entry("Japan", "JPY", "0.0067"),
            entry("Singapore", "SGD", "0.74"),
            entry("United Kingdom", "GBP", "1.27"),
            entry("United States", "USD", "1"));

    private final Map<String, CountryCurrency> byCountry = ENTRIES.stream()
            .collect(Collectors.toMap(e -> key(e.country()), Function.identity()));

    private final Map<String, BigDecimal> rateByCurrency = ENTRIES.stream()
            .collect(Collectors.toMap(CountryCurrency::currency, CountryCurrency::usdRate, (a, b) -> a));

    public List<CountryCurrency> countries() {
        return ENTRIES.stream().sorted(Comparator.comparing(CountryCurrency::country)).toList();
    }

    public CountryCurrency find(String country) {
        CountryCurrency found = country == null ? null : byCountry.get(key(country));
        if (found == null) {
            throw new InvalidRequestException("Unsupported country: " + country);
        }
        return found;
    }

    public BigDecimal toUsd(BigDecimal amount, String currency) {
        BigDecimal rate = rateByCurrency.get(currency);
        if (rate == null) {
            throw new InvalidRequestException("Unsupported currency: " + currency);
        }
        return amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }

    private static CountryCurrency entry(String country, String currency, String rate) {
        return new CountryCurrency(country, currency, new BigDecimal(rate));
    }

    private static String key(String country) {
        return country.trim().toLowerCase(Locale.ROOT);
    }
}
