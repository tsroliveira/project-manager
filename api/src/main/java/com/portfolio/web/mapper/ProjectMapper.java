package com.portfolio.web.mapper;

import com.portfolio.domain.Project;
import com.portfolio.web.dto.ProjectCreateRequest;
import com.portfolio.web.dto.ProjectDto;
import com.portfolio.web.dto.ProjectUpdateRequest;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProjectMapper {

    // CREATE
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "name",            source = "name")
    @Mapping(target = "startDate",       source = "startDate")
    @Mapping(target = "expectedEndDate", source = "expectedEndDate")
    @Mapping(target = "totalBudget",     source = "totalBudget")
    @Mapping(target = "description",     source = "description")
    @Mapping(target = "managerMemberId", source = "managerMemberId") // <-- só aqui
    Project toEntity(ProjectCreateRequest src);

    // UPDATE parcial (sem managerMemberId)
    @BeanMapping(ignoreByDefault = true,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "name",            source = "name")
    @Mapping(target = "startDate",       source = "startDate")
    @Mapping(target = "expectedEndDate", source = "expectedEndDate")
    @Mapping(target = "totalBudget",     source = "totalBudget")
    @Mapping(target = "description",     source = "description")
    // @Mapping(target = "status", source = "status") // adicione se existir no UpdateRequest
    void updateEntity(@MappingTarget Project target, ProjectUpdateRequest src);

    // DTO (enquanto não houver cálculo de risco)
    @Mapping(target = "risk", ignore = true)
    ProjectDto toDto(Project src);
}