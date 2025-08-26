package com.portfolio.repository;

import com.portfolio.domain.Project;
import com.portfolio.domain.ProjectStatus;
import org.springframework.data.jpa.repository.*;
import java.util.*;

public interface ProjectRepository extends JpaRepository<Project, UUID>, JpaSpecificationExecutor<Project> {

    long countByStatus(ProjectStatus status);

    @Query("""
           select p.status as status, count(p) as total
           from Project p
           group by p.status
           """)
    List<Object[]> countGroupByStatus();
}
