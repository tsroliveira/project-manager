package com.portfolio.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ProjectCreateRequest {

    @NotBlank
    private String name;

    @NotNull
    @Schema(format = "date")
    private LocalDate startDate;

    @NotNull
    @Schema(format = "date")
    private LocalDate expectedEndDate;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal totalBudget;

    private String description;

    @NotBlank
    private String managerMemberId;
}
