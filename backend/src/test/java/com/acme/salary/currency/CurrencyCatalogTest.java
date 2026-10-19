package com.acme.salary.currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.acme.salary.common.InvalidRequestException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class CurrencyCatalogTest {

    private final CurrencyCatalog catalog = new CurrencyCatalog();

    @Test
    void findsCountryIgnoringCaseAndSurroundingSpaces() {
        CurrencyCatalog.CountryCurrency found = catalog.find("  india ");

        assertThat(found.country()).isEqualTo("India");
        assertThat(found.currency()).isEqualTo("INR");
    }

    @Test
    void unknownCountryIsRejected() {
        assertThatThrownBy(() -> catalog.find("Atlantis"))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("Atlantis");
    }

    @Test
    void usdIsConvertedToItself() {
        assertThat(catalog.toUsd(new BigDecimal("125000.00"), "USD")).isEqualByComparingTo("125000.00");
    }

    @Test
    void convertsUsingFixedRateAndRoundsToTwoDecimals() {
        assertThat(catalog.toUsd(new BigDecimal("1200000"), "INR")).isEqualByComparingTo("14400.00");
        assertThat(catalog.toUsd(new BigDecimal("100000"), "GBP")).isEqualByComparingTo("127000.00");
    }

    @Test
    void unknownCurrencyIsRejected() {
        assertThatThrownBy(() -> catalog.toUsd(BigDecimal.TEN, "XYZ"))
                .isInstanceOf(InvalidRequestException.class);
    }

    @Test
    void listsTenCountriesSortedByName() {
        assertThat(catalog.countries()).hasSize(10);
        assertThat(catalog.countries()).extracting(CurrencyCatalog.CountryCurrency::country).isSorted();
    }
}
