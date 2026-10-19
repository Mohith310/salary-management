package com.acme.salary.insights;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.acme.salary.common.InvalidRequestException;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(InsightsController.class)
class InsightsControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private InsightsService service;

    private static GroupInsight group(String name, String currency) {
        BigDecimal v = new BigDecimal("100.00");
        return new GroupInsight(name, currency, new SalaryStats(2, v, v, v, v, new BigDecimal("200.00")));
    }

    @Test
    void byCountryReturnsOneEntryPerGroup() throws Exception {
        when(service.byCountry()).thenReturn(List.of(group("India", "INR"), group("Japan", "JPY")));

        mvc.perform(get("/api/insights/by-country"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].group").value("India"))
                .andExpect(jsonPath("$[0].currency").value("INR"))
                .andExpect(jsonPath("$[0].stats.headcount").value(2));
    }

    @Test
    void byDepartmentPassesTheOptionalCountry() throws Exception {
        when(service.byDepartment("India")).thenReturn(List.of(group("Engineering", "INR")));

        mvc.perform(get("/api/insights/by-department").param("country", "India"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].group").value("Engineering"));

        verify(service).byDepartment("India");
    }

    @Test
    void byJobTitleWithoutCountryPassesNull() throws Exception {
        when(service.byJobTitle(null)).thenReturn(List.of(group("Designer", "USD")));

        mvc.perform(get("/api/insights/by-job-title")).andExpect(status().isOk());

        verify(service).byJobTitle(null);
    }

    @Test
    void summaryAndDistributionAreExposed() throws Exception {
        when(service.summary(null)).thenReturn(group("All employees", "USD"));
        when(service.distribution(null)).thenReturn(new DistributionResponse("USD", List.of(new DistributionBand("Under 25k", 3))));

        mvc.perform(get("/api/insights/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.group").value("All employees"));
        mvc.perform(get("/api/insights/distribution"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.bands[0].label").value("Under 25k"))
                .andExpect(jsonPath("$.bands[0].headcount").value(3));
    }

    @Test
    void unsupportedCountryReturns400() throws Exception {
        when(service.byDepartment("Atlantis")).thenThrow(new InvalidRequestException("Unsupported country: Atlantis"));

        mvc.perform(get("/api/insights/by-department").param("country", "Atlantis"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Unsupported country: Atlantis"));
    }
}
