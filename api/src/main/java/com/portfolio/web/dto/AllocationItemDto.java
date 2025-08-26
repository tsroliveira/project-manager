package com.portfolio.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "AllocationItem")
public record AllocationItemDto(String memberId, String name, String role) {}
