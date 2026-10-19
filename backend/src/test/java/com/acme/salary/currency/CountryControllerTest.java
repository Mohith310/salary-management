package com.acme.salary.currency;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CountryController.class)
@Import(CurrencyCatalog.class)
class CountryControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void listsSupportedCountriesWithTheirCurrency() throws Exception {
        mvc.perform(get("/api/countries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(10))
                .andExpect(jsonPath("$[?(@.country=='India')].currency").value("INR"));
    }
}
