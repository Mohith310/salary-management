package com.acme.salary.employee;

import java.util.List;

public record FilterOptions(List<String> countries, List<String> departments, List<String> jobTitles) {
}
