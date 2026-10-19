package com.acme.salary;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** Boots the whole app on an in-memory database seeded with 500 employees to prove the wiring works end to end. */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:smoke;DB_CLOSE_DELAY=-1",
        "app.seed.enabled=true",
        "app.seed.count=500",
        "app.seed.random-seed=42"})
@AutoConfigureMockMvc
class SalaryManagementApplicationSmokeTest {

    private static final String NEW_EMPLOYEE = """
            {"fullName":"Smoke Test","email":"smoke.test@acme.example","jobTitle":"QA Engineer",
             "department":"Engineering","country":"Japan","salary":8000000,"hireDate":"2024-02-01"}
            """;

    @Autowired
    private MockMvc mvc;

    @Test
    void seededEmployeesAreListedWithServerSidePaging() throws Exception {
        mvc.perform(get("/api/employees").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(500))
                .andExpect(jsonPath("$.content.length()").value(10))
                .andExpect(jsonPath("$.totalPages").value(50));
    }

    @Test
    void countryFilterOnlyReturnsThatCountry() throws Exception {
        mvc.perform(get("/api/employees").param("country", "India").param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].country", everyItem(is("India"))));
    }

    @Test
    void insightsCoverAllTenCountries() throws Exception {
        mvc.perform(get("/api/insights/by-country"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(10));
        mvc.perform(get("/api/insights/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stats.headcount").value(500));
        mvc.perform(get("/api/insights/by-department"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].currency").value("USD"));
    }

    @Test
    void employeeLifecycleCreateReadDuplicateDelete() throws Exception {
        String created = mvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON).content(NEW_EMPLOYEE))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.currency").value("JPY"))
                .andReturn().getResponse().getContentAsString();
        Number id = JsonPath.read(created, "$.id");

        mvc.perform(get("/api/employees/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("smoke.test@acme.example"));

        mvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON).content(NEW_EMPLOYEE))
                .andExpect(status().isConflict());

        mvc.perform(delete("/api/employees/" + id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/employees/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void angularRoutesAreForwardedToTheSinglePageApp() throws Exception {
        mvc.perform(get("/employees")).andExpect(forwardedUrl("/index.html"));
        mvc.perform(get("/insights")).andExpect(forwardedUrl("/index.html"));
    }
}
