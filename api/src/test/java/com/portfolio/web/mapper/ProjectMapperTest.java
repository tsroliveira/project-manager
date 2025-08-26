package com.portfolio.web.mapper;

import com.portfolio.domain.Project;
import com.portfolio.web.dto.ProjectCreateRequest;
import com.portfolio.web.dto.ProjectDto;
import com.portfolio.web.dto.ProjectUpdateRequest;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectMapperTest {

    private final ProjectMapper mapper = Mappers.getMapper(ProjectMapper.class);

    @Test
    void toEntity_mapsCreateFields() {
        var req = new ProjectCreateRequest();
        req.setName("Projeto Teste");
        req.setStartDate(LocalDate.of(2025, 1, 1));
        req.setExpectedEndDate(LocalDate.of(2025, 2, 1));
        req.setTotalBudget(new BigDecimal("123.45"));
        req.setDescription("desc");
        req.setManagerMemberId("mgr-007");

        Project entity = mapper.toEntity(req);

        assertThat(entity.getName()).isEqualTo("Projeto Teste");
        assertThat(entity.getStartDate()).isEqualTo(LocalDate.of(2025, 1, 1));
        assertThat(entity.getExpectedEndDate()).isEqualTo(LocalDate.of(2025, 2, 1));
        assertThat(entity.getTotalBudget()).isEqualByComparingTo("123.45");
        assertThat(entity.getDescription()).isEqualTo("desc");
        assertThat(entity.getManagerMemberId()).isEqualTo("mgr-007");
    }

    @Test
    void updateEntity_ignoresNulls() {
        var target = new Project();
        target.setName("A");
        target.setDescription("D");

        var req = new ProjectUpdateRequest();
        req.setName("B");
        // description nula NÃO deve sobrescrever
        req.setDescription(null);

        mapper.updateEntity(target, req);

        assertThat(target.getName()).isEqualTo("B");
        assertThat(target.getDescription()).isEqualTo("D");
    }

    @Test
    void toDto_basicMapping() {
        var p = new Project();
        p.setName("Nome");
        p.setDescription("Descr");
        ProjectDto dto = mapper.toDto(p);

        assertThat(dto.getName()).isEqualTo("Nome");
        assertThat(dto.getDescription()).isEqualTo("Descr");
        // risk está ignorado no mapper
    }
}
