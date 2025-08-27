package com.portfolio.service;

import com.portfolio.domain.Project;
import com.portfolio.domain.ProjectStatus;
import com.portfolio.repository.ProjectMemberAllocationRepository;
import com.portfolio.repository.ProjectRepository;
import com.portfolio.web.dto.PortfolioReportDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    private final ProjectRepository projectRepo;
    private final ProjectMemberAllocationRepository allocRepo;

    public ReportService(ProjectRepository projectRepo,
                         ProjectMemberAllocationRepository allocRepo) {
        this.projectRepo = projectRepo;
        this.allocRepo = allocRepo;
    }

    @Transactional(readOnly = true)
    public PortfolioReportDto getPortfolioReport() {
        var dto = new PortfolioReportDto();

        // Inicializa todos os status
        Map<ProjectStatus, PortfolioReportDto.StatusSummary> map =
                new EnumMap<>(ProjectStatus.class);
        for (var s : ProjectStatus.values()) {
            map.put(s, new PortfolioReportDto.StatusSummary(0, BigDecimal.ZERO));
        }

        // Contagem por status (método já existia)
        for (Object[] row : projectRepo.countGroupByStatus()) {
            var status = (ProjectStatus) row[0];
            var count  = ((Number) row[1]).longValue();
            map.get(status).setCount(count);
        }

        // Soma de orçamento por status (novo método)
        for (Object[] row : projectRepo.sumBudgetGroupByStatus()) {
            var status = (ProjectStatus) row[0];
            var total  = (BigDecimal) row[1];
            map.get(status).setTotalBudget(total != null ? total : BigDecimal.ZERO);
        }

        // Média de duração dos ENCERRADO (dias) sem query nativa
        List<Project> closed = projectRepo.findByStatusAndActualEndDateNotNull(ProjectStatus.ENCERRADO);
        double avg = closed.stream()
                .filter(p -> p.getStartDate() != null && p.getActualEndDate() != null)
                .mapToLong(p -> ChronoUnit.DAYS.between(p.getStartDate(), p.getActualEndDate()))
                .average()
                .orElse(0.0);

        dto.setByStatus(map);
        dto.setAverageClosedDurationDays(closed.isEmpty() ? null : avg);

        // Total de membros únicos alocados
        dto.setUniqueAllocatedMembers(allocRepo.countDistinctMembers());

        return dto;
    }
}
