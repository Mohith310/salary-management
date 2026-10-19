package com.acme.salary.employee;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.acme.salary.common.PageResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    private static final String VALID_BODY = """
            {"fullName":"Asha Rao","email":"asha@acme.example","jobTitle":"Software Engineer",
             "department":"Engineering","country":"India","salary":1800000,"hireDate":"2022-03-01"}
            """;

    private static final String INVALID_BODY = """
            {"fullName":"","email":"not-an-email","jobTitle":"Software Engineer",
             "department":"Engineering","country":"India","salary":-5,"hireDate":"2999-01-01"}
            """;

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private EmployeeService service;

    private static EmployeeResponse sample() {
        return new EmployeeResponse(1L, "Asha Rao", "asha@acme.example", "Software Engineer", "Engineering",
                "India", "INR", new BigDecimal("1800000.00"), LocalDate.of(2022, 3, 1));
    }

    @Test
    void createReturns201WithTheCreatedEmployee() throws Exception {
        when(service.create(any(EmployeeRequest.class))).thenReturn(sample());

        mvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("asha@acme.example"))
                .andExpect(jsonPath("$.currency").value("INR"));
    }

    @Test
    void createWithInvalidBodyReturns400ListingEachBadField() throws Exception {
        mvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON).content(INVALID_BODY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.fullName").exists())
                .andExpect(jsonPath("$.fieldErrors.email").exists())
                .andExpect(jsonPath("$.fieldErrors.salary").exists())
                .andExpect(jsonPath("$.fieldErrors.hireDate").exists());
    }

    @Test
    void createWithDuplicateEmailReturns409() throws Exception {
        when(service.create(any(EmployeeRequest.class))).thenThrow(new DuplicateEmailException("asha@acme.example"));

        mvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isConflict());
    }

    @Test
    void getOfUnknownEmployeeReturns404() throws Exception {
        when(service.get(42L)).thenThrow(new EmployeeNotFoundException(42L));

        mvc.perform(get("/api/employees/42"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Employee 42 not found"));
    }

    @Test
    void updateReturnsTheUpdatedEmployee() throws Exception {
        when(service.update(any(Long.class), any(EmployeeRequest.class))).thenReturn(sample());

        mvc.perform(put("/api/employees/1").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Asha Rao"));
    }

    @Test
    void deleteReturns204() throws Exception {
        mvc.perform(delete("/api/employees/5")).andExpect(status().isNoContent());

        verify(service).delete(5L);
    }

    @Test
    void listPassesFiltersPagingAndSortingToTheService() throws Exception {
        when(service.search(any(EmployeeFilter.class), anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(new PageResponse<>(List.of(sample()), 2, 10, 21, 3));

        mvc.perform(get("/api/employees")
                        .param("q", "asha").param("country", "India").param("minSalary", "50000")
                        .param("page", "2").param("size", "10").param("sortBy", "salary").param("direction", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].fullName").value("Asha Rao"))
                .andExpect(jsonPath("$.totalElements").value(21))
                .andExpect(jsonPath("$.totalPages").value(3));

        verify(service).search(
                new EmployeeFilter("asha", "India", null, null, new BigDecimal("50000"), null),
                2, 10, "salary", "desc");
    }

    @Test
    void listWithNonNumericSalaryFilterReturns400() throws Exception {
        mvc.perform(get("/api/employees").param("minSalary", "lots")).andExpect(status().isBadRequest());
    }

    @Test
    void filterOptionsAreExposedForTheDropdowns() throws Exception {
        when(service.filterOptions()).thenReturn(new FilterOptions(List.of("India"), List.of("Engineering"), List.of("Designer")));

        mvc.perform(get("/api/employees/filter-options"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.countries[0]").value("India"))
                .andExpect(jsonPath("$.departments[0]").value("Engineering"))
                .andExpect(jsonPath("$.jobTitles[0]").value("Designer"));
    }
}
