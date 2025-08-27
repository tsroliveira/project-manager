package com.portfolio.web.dto;

import com.portfolio.domain.ProjectStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;

@Schema(name = "PortfolioReport")
public class PortfolioReportDto {

    @Schema(description = "Resumo por status (quantidade e soma de orçamento)")
    private Map<ProjectStatus, StatusSummary> byStatus = new EnumMap<>(ProjectStatus.class);

    @Schema(description = "Média de duração (em dias) dos projetos encerrados")
    private Double averageClosedDurationDays;

    @Schema(description = "Total de membros únicos alocados no portfólio")
    private Long uniqueAllocatedMembers;

    public Map<ProjectStatus, StatusSummary> getByStatus() { return byStatus; }
    public void setByStatus(Map<ProjectStatus, StatusSummary> byStatus) { this.byStatus = byStatus; }
    public Double getAverageClosedDurationDays() { return averageClosedDurationDays; }
    public void setAverageClosedDurationDays(Double v) { this.averageClosedDurationDays = v; }
    public Long getUniqueAllocatedMembers() { return uniqueAllocatedMembers; }
    public void setUniqueAllocatedMembers(Long v) { this.uniqueAllocatedMembers = v; }

    public static class StatusSummary {
        private long count;
        private BigDecimal totalBudget;

        public StatusSummary() {}
        public StatusSummary(long count, BigDecimal totalBudget) {
            this.count = count;
            this.totalBudget = totalBudget;
        }
        public long getCount() { return count; }
        public void setCount(long count) { this.count = count; }
        public BigDecimal getTotalBudget() { return totalBudget; }
        public void setTotalBudget(BigDecimal totalBudget) { this.totalBudget = totalBudget; }
    }
}
