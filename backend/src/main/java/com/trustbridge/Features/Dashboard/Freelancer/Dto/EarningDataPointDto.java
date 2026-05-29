package com.trustbridge.Features.Dashboard.Freelancer.Dto;

import java.math.BigDecimal;

public record EarningDataPointDto(String month, BigDecimal total) {

    // Maps the SQL EXTRACT results directly into "YYYY-MM" for Next.js
    public EarningDataPointDto(Number ledgerMonth, Number ledgerYear, Number total) {
        this(
                String.format("%04d-%02d", ledgerYear.intValue(), ledgerMonth.intValue()),
                total != null ? new BigDecimal(total.toString()) : BigDecimal.ZERO
        );
    }
}