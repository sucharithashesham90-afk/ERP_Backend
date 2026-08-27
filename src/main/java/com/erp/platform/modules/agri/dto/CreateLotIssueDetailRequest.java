package com.erp.platform.modules.agri.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateLotIssueDetailRequest(
        String issueNumber,
        LocalDate issueDate,
        String lotNumber,
        String location,
        String godown,
        String materialGroup,
        String materialItem,
        BigDecimal quantity,
        String unit,
        String issueType,
        String remarks
) {}
