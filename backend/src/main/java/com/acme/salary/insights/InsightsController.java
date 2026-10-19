package com.acme.salary.insights;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/insights")
public class InsightsController {

    private final InsightsService service;

    public InsightsController(InsightsService service) {
        this.service = service;
    }

    @GetMapping("/summary")
    public GroupInsight summary(@RequestParam(required = false) String country) {
        return service.summary(country);
    }

    @GetMapping("/by-country")
    public List<GroupInsight> byCountry() {
        return service.byCountry();
    }

    @GetMapping("/by-department")
    public List<GroupInsight> byDepartment(@RequestParam(required = false) String country) {
        return service.byDepartment(country);
    }

    @GetMapping("/by-job-title")
    public List<GroupInsight> byJobTitle(@RequestParam(required = false) String country) {
        return service.byJobTitle(country);
    }

    @GetMapping("/distribution")
    public DistributionResponse distribution(@RequestParam(required = false) String country) {
        return service.distribution(country);
    }
}
