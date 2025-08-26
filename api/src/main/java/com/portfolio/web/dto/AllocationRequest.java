package com.portfolio.web.dto;

import jakarta.validation.constraints.NotBlank;

public record AllocationRequest(@NotBlank String memberId) { }
