package com.portfolio.web.mapper;

import com.portfolio.domain.Project;
import com.portfolio.web.dto.ProjectCreateRequest;
import com.portfolio.web.dto.ProjectDto;
import com.portfolio.web.dto.ProjectUpdateRequest;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-08-26T19:11:20-0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.16 (Ubuntu)"
)
@Component
public class ProjectMapperImpl implements ProjectMapper {

    @Override
    public Project toEntity(ProjectCreateRequest src) {
        if ( src == null ) {
            return null;
        }

        Project project = new Project();

        project.setName( src.getName() );
        project.setStartDate( src.getStartDate() );
        project.setExpectedEndDate( src.getExpectedEndDate() );
        project.setTotalBudget( src.getTotalBudget() );
        project.setDescription( src.getDescription() );
        project.setManagerMemberId( src.getManagerMemberId() );

        return project;
    }

    @Override
    public void updateEntity(Project target, ProjectUpdateRequest src) {
        if ( src == null ) {
            return;
        }

        if ( src.getName() != null ) {
            target.setName( src.getName() );
        }
        if ( src.getStartDate() != null ) {
            target.setStartDate( src.getStartDate() );
        }
        if ( src.getExpectedEndDate() != null ) {
            target.setExpectedEndDate( src.getExpectedEndDate() );
        }
        if ( src.getTotalBudget() != null ) {
            target.setTotalBudget( src.getTotalBudget() );
        }
        if ( src.getDescription() != null ) {
            target.setDescription( src.getDescription() );
        }
    }

    @Override
    public ProjectDto toDto(Project src) {
        if ( src == null ) {
            return null;
        }

        ProjectDto.ProjectDtoBuilder projectDto = ProjectDto.builder();

        projectDto.id( src.getId() );
        projectDto.name( src.getName() );
        projectDto.startDate( src.getStartDate() );
        projectDto.expectedEndDate( src.getExpectedEndDate() );
        projectDto.actualEndDate( src.getActualEndDate() );
        projectDto.totalBudget( src.getTotalBudget() );
        projectDto.description( src.getDescription() );
        projectDto.managerMemberId( src.getManagerMemberId() );
        projectDto.status( src.getStatus() );
        projectDto.createdAt( src.getCreatedAt() );
        projectDto.updatedAt( src.getUpdatedAt() );

        return projectDto.build();
    }
}
