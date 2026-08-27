package com.erp.platform.modules.quality.dto;

import java.time.LocalDate;
import java.util.UUID;

public record PublishResultDto(
        UUID id,
        String publishNumber,
        LocalDate publishDate,
        String lotNumber,
        String testResultNumber,
        String publishedBy,
        String publicationNote,
        String publishStatus,
        UUID sampleId
) {}
