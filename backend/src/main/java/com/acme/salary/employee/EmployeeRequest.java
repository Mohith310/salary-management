package com.acme.salary.employee;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Payload for creating or updating an employee. Currency is derived from the country, never sent by the client. */
public record EmployeeRequest(
        @NotBlank @Size(max = 150) String fullName,
        @NotBlank @Email @Size(max = 200) String email,
        @NotBlank @Size(max = 100) String jobTitle,
        @NotBlank @Size(max = 100) String department,
        @NotBlank @Size(max = 60) String country,
        @NotNull @Positive @Digits(integer = 12, fraction = 2) BigDecimal salary,
        @NotNull @PastOrPresent LocalDate hireDate) {
}
