package com.portfolio.service;

import com.portfolio.domain.Project;
import com.portfolio.domain.ProjectStatus;
import com.portfolio.domain.RiskLevel;
import com.portfolio.exception.BusinessException;
import com.portfolio.exception.NotFoundException;
import com.portfolio.repository.ProjectRepository;
import com.portfolio.repository.ProjectMemberAllocationRepository; // <- import CORRETO
import com.portfolio.repository.spec.ProjectSpecifications;
import com.portfolio.web.dto.ProjectCreateRequest;
import com.portfolio.web.dto.ProjectDto;
import com.portfolio.web.dto.ProjectUpdateRequest;
import com.portfolio.web.dto.StatusUpdateRequest;
import com.portfolio.web.mapper.ProjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.springframework.data.jpa.domain.Specification.where;

@Service
@Transactional
public class ProjectService {

    private final ProjectRepository repo;
    private final ProjectMapper mapper;
    private final ProjectMemberAllocationRepository allocationRepo;

    public ProjectService(ProjectRepository repo,
                          ProjectMapper mapper,
                          ProjectMemberAllocationRepository allocationRepo) {
        this.repo = repo;
        this.mapper = mapper;
        this.allocationRepo = allocationRepo;
    }

    @Transactional
    public ProjectDto create(ProjectCreateRequest req) {
        Project p = mapper.toEntity(req);

        if (p.getStatus() == null) {
            p.setStatus(ProjectStatus.EM_ANALISE);
        }

        p = repo.save(p);

        ProjectDto dto = mapper.toDto(p);
        dto.setRisk(computeRisk(p));
        return dto;
    }

    @Transactional(readOnly = true)
    public ProjectDto get(UUID id) {
        Project p = repo.findById(id).orElseThrow(() -> new NotFoundException("Projeto não encontrado"));
        ProjectDto dto = mapper.toDto(p);
        dto.setRisk(computeRisk(p));
        return dto;
    }

    @Transactional(readOnly = true)
    public Page<ProjectDto> list(ProjectStatus status, String managerId, String name,
                                 String startFrom, String startTo, Pageable pageable) {

        Specification<Project> spec = buildSpec(status, managerId, name, startFrom, startTo);

        return repo.findAll(spec, pageable)
                .map(p -> {
                    ProjectDto dto = mapper.toDto(p);
                    dto.setRisk(computeRisk(p));
                    return dto;
                });
    }

    public ProjectDto update(UUID id, ProjectUpdateRequest req) {
        Project p = repo.findById(id).orElseThrow(() -> new NotFoundException("Projeto não encontrado"));
        mapper.updateEntity(p, req);
        Project saved = repo.save(p);
        ProjectDto dto = mapper.toDto(saved);
        dto.setRisk(computeRisk(saved));
        return dto;
    }

    public void delete(UUID id) {
        Project p = repo.findById(id).orElseThrow(() -> new NotFoundException("Projeto não encontrado"));

        if (p.getStatus() == ProjectStatus.INICIADO
                || p.getStatus() == ProjectStatus.EM_ANDAMENTO
                || p.getStatus() == ProjectStatus.ENCERRADO) {
            throw new BusinessException("Não é permitido excluir projetos iniciados, em andamento ou encerrados.");
        }

        // evita erro de FK
        allocationRepo.deleteByProject_Id(id);

        repo.delete(p);
    }

    public ProjectDto changeStatus(UUID id, StatusUpdateRequest req) {
        Project p = repo.findById(id).orElseThrow(() -> new NotFoundException("Projeto não encontrado"));
        ProjectStatus next = req.getStatus();

        if (next != ProjectStatus.CANCELADO && !p.getStatus().canTransitionTo(next)) {
            throw new BusinessException("Transição de status inválida: não é permitido pular etapas.");
        }
        p.setStatus(next);
        Project saved = repo.save(p);
        ProjectDto dto = mapper.toDto(saved);
        dto.setRisk(computeRisk(saved));
        return dto;
    }

    // ===== helpers =====

    private RiskLevel computeRisk(Project p) {
        if (p == null) return RiskLevel.MEDIO;

        LocalDate start = p.getStartDate();
        LocalDate end   = p.getExpectedEndDate();
        BigDecimal budget = p.getTotalBudget();

        if (start == null || end == null) return RiskLevel.MEDIO;

        long months = ChronoUnit.MONTHS.between(
                start.withDayOfMonth(1),
                end.withDayOfMonth(1)
        );

        if (budget != null && budget.compareTo(new BigDecimal("500000")) > 0) return RiskLevel.ALTO;
        if (months > 6) return RiskLevel.ALTO;

        if (budget != null
                && budget.compareTo(new BigDecimal("100000")) <= 0
                && months <= 3) return RiskLevel.BAIXO;

        return RiskLevel.MEDIO;
    }

    private Specification<Project> buildSpec(ProjectStatus status, String managerId, String name,
                                             String startFrom, String startTo) {
        LocalDate from = (startFrom == null || startFrom.isBlank()) ? null : LocalDate.parse(startFrom);
        LocalDate to   = (startTo   == null || startTo.isBlank())   ? null : LocalDate.parse(startTo);

        return where(ProjectSpecifications.statusEquals(status))
                .and(ProjectSpecifications.managerEquals(managerId))
                .and(ProjectSpecifications.nameContains(name))
                .and(ProjectSpecifications.startDateFrom(from))
                .and(ProjectSpecifications.startDateTo(to));
    }
}
