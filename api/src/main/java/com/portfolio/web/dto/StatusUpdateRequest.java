package com.portfolio.web.dto;

import com.portfolio.domain.ProjectStatus;
import jakarta.validation.constraints.NotNull;

public class StatusUpdateRequest {

    @NotNull(message = "{project.status.notNull}")
    private ProjectStatus status;

    public ProjectStatus getStatus() { return status; }
    public void setStatus(ProjectStatus status) { this.status = status; }
}
