package com.erp.platform.modules.quality.dto;

import java.time.LocalDate;

public record CreatePublishResultRequest(
        String publishNumber,
        LocalDate publishDate,
        String lotNumber,
        String testResultNumber,
        String publishedBy,
        String publicationNote,
        String publishStatus
) {}
