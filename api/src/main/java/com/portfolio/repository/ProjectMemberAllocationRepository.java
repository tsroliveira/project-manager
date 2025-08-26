package com.portfolio.repository;

import com.portfolio.domain.ProjectMemberAllocation;
import com.portfolio.repository.projection.AllocationView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ProjectMemberAllocationRepository extends JpaRepository<ProjectMemberAllocation, UUID> {

    boolean existsByProject_IdAndMemberExternalId(UUID projectId, String memberExternalId);

    long countByProject_Id(UUID projectId);

    void deleteByProject_IdAndMemberExternalId(UUID projectId, String memberExternalId);

    void deleteByProject_Id(UUID projectId);

    @Query("""
      select count(distinct p.id)
      from ProjectMemberAllocation a
        join a.project p
      where a.memberExternalId = :memberId
        and p.status <> com.portfolio.domain.ProjectStatus.ENCERRADO
        and p.status <> com.portfolio.domain.ProjectStatus.CANCELADO
    """)
    long countActiveProjectsForMember(@Param("memberId") String memberId);

    @Query("""
      select a.memberExternalId as memberId,
             coalesce(c.name, a.memberExternalId) as name,
             coalesce(c.role, 'desconhecido') as role
      from ProjectMemberAllocation a
        left join MemberCache c on c.externalId = a.memberExternalId
      where a.project.id = :projectId
      order by a.id asc
    """)
    List<AllocationView> findAllocationsWithCache(@Param("projectId") UUID projectId);
}
