package com.portfolio.repository;

import com.portfolio.domain.Project;
import com.portfolio.domain.ProjectStatus;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.*;

public interface ProjectRepository extends JpaRepository<Project, UUID>, JpaSpecificationExecutor<Project> {

    long countByStatus(ProjectStatus status);

    @Query("""
           select p.status as status, count(p) as total
           from Project p
           group by p.status
           """)
    List<Object[]> countGroupByStatus();

    @Query("""
           select p.status as status, sum(p.totalBudget) as totalBudget
           from Project p
           group by p.status
           """)
    List<Object[]> sumBudgetGroupByStatus();

    // Postgres: média em dias entre actual_end_date e start_date para projetos ENCERRADO
    @Query(value = """
        select avg(extract(epoch from (actual_end_date - start_date)) / 86400.0)
        from projects
        where status = 'ENCERRADO' and actual_end_date is not null
        """, nativeQuery = true)
    Double avgDurationDaysForClosed();

    // sem @Query: método derivado; evita nativo e não mexe em nomes de coluna
    List<Project> findByStatusAndActualEndDateNotNull(ProjectStatus status);
}
