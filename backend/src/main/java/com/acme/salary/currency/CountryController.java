package com.acme.salary.currency;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Supported countries, so the UI form can offer a dropdown instead of free text. */
@RestController
@RequestMapping("/api/countries")
public class CountryController {

    private final CurrencyCatalog catalog;

    public CountryController(CurrencyCatalog catalog) {
        this.catalog = catalog;
    }

    @GetMapping
    public List<CurrencyCatalog.CountryCurrency> countries() {
        return catalog.countries();
    }
}
