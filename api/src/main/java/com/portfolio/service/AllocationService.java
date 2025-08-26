package com.portfolio.service;

import com.portfolio.domain.Project;
import com.portfolio.domain.ProjectMemberAllocation;
import com.portfolio.domain.ProjectStatus;
import com.portfolio.exception.BusinessException;
import com.portfolio.exception.NotFoundException;
import com.portfolio.integration.members.MemberClient;
import com.portfolio.integration.members.dto.MemberResponse;
import com.portfolio.repository.MemberCacheRepository;
import com.portfolio.repository.ProjectMemberAllocationRepository;
import com.portfolio.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

@Service
public class AllocationService {

    private static final Set<ProjectStatus> FINAL_STATUSES =
            EnumSet.of(ProjectStatus.ENCERRADO, ProjectStatus.CANCELADO);

    private final ProjectRepository projectRepository;
    private final ProjectMemberAllocationRepository allocationRepository;
    private final MemberCacheRepository memberCacheRepository;
    private final MemberClient memberClient;

    public AllocationService(ProjectRepository projectRepository,
                             ProjectMemberAllocationRepository allocationRepository,
                             MemberCacheRepository memberCacheRepository,
                             MemberClient memberClient) {
        this.projectRepository = projectRepository;
        this.allocationRepository = allocationRepository;
        this.memberCacheRepository = memberCacheRepository;
        this.memberClient = memberClient;
    }

    @Transactional
    public void allocate(UUID projectId, String memberExternalId) {
        var project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Projeto não encontrado"));

        // valida papel via client externo
        MemberResponse member = memberClient.getById(memberExternalId);
        if (member.role() == null || !member.role().equalsIgnoreCase("funcionário")) {
            throw new BusinessException("Apenas membros com atribuição 'funcionário' podem ser associados.");
        }

        // UPSERT no cache (usa o mesmo valor para id e external_id)
        memberCacheRepository.upsertByExternalId(
                memberExternalId,  // id
                memberExternalId,  // external_id
                member.name(),
                member.role()
        );

        // regras
        if (allocationRepository.existsByProject_IdAndMemberExternalId(projectId, memberExternalId)) {
            throw new BusinessException("Membro já alocado neste projeto.");
        }
        long current = allocationRepository.countByProject_Id(projectId);
        if (current >= 10) {
            throw new BusinessException("Projeto já possui o máximo de 10 membros alocados.");
        }
        long active = allocationRepository.countActiveProjectsForMember(memberExternalId);
        if (active >= 3) {
            throw new BusinessException("Membro já está alocado em 3 projetos ativos.");
        }

        allocationRepository.save(new ProjectMemberAllocation(project, memberExternalId));
    }

    @Transactional
    public void deallocate(UUID projectId, String memberExternalId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Projeto não encontrado"));

        if (!allocationRepository.existsByProject_IdAndMemberExternalId(projectId, memberExternalId)) {
            throw new NotFoundException("Alocação não encontrada para este membro no projeto.");
        }

        long current = allocationRepository.countByProject_Id(projectId);
        if (current <= 1 && !FINAL_STATUSES.contains(project.getStatus())) {
            throw new BusinessException("Projeto deve possuir ao menos 1 membro alocado.");
        }

        allocationRepository.deleteByProject_IdAndMemberExternalId(projectId, memberExternalId);
    }
}
