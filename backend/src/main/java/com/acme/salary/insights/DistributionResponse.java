package com.acme.salary.insights;

import java.util.List;

public record DistributionResponse(String currency, List<DistributionBand> bands) {
}
