package com.portfolio.web;

import com.portfolio.domain.ProjectStatus;
import com.portfolio.service.ProjectService;
import com.portfolio.web.dto.PageResponse;
import com.portfolio.web.dto.ProjectCreateRequest;
import com.portfolio.web.dto.ProjectDto;
import com.portfolio.web.dto.ProjectUpdateRequest;
import com.portfolio.web.dto.StatusUpdateRequest;
import com.portfolio.web.error.ErrorResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.media.*;

import java.util.ArrayList;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
@ApiResponses({
    @ApiResponse(responseCode = "400", description = "Requisição inválida",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "404", description = "Recurso/entidade não encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "409", description = "Conflito de integridade de dados",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "422", description = "Regra de negócio violada",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Erro interno",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
})
public class ProjectController {

    private final ProjectService service;

    public ProjectController(ProjectService service) {
        this.service = service;
    }

    @Operation(summary = "Cria um novo projeto")
    @ApiResponse(responseCode = "201", description = "Criado",
            content = @Content(schema = @Schema(implementation = ProjectDto.class)))
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectDto create(@Valid @RequestBody ProjectCreateRequest req) {
        return service.create(req);
    }

    @Operation(summary = "Obtém um projeto pelo ID")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = ProjectDto.class)))
    @GetMapping("/{id}")
    public ProjectDto get(@PathVariable UUID id) {
        return service.get(id);
    }

    @Operation(summary = "Lista projetos com filtros e paginação")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = PageResponse.class)))
    @GetMapping
    public PageResponse<ProjectDto> list(
            @RequestParam(required = false) ProjectStatus status,
            @RequestParam(required = false, name = "manager") String managerId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String startDateFrom,
            @RequestParam(required = false) String startDateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        Sort sortObj = Sort.by("createdAt").descending();
        if (sort != null && !sort.isBlank()) {
            String[] parts = sort.split(",");
            sortObj = parts.length > 1 && parts[1].equalsIgnoreCase("desc")
                    ? Sort.by(parts[0]).descending()
                    : Sort.by(parts[0]).ascending();
        }
        var pageable = PageRequest.of(page, size, sortObj);

        var result = service.list(status, managerId, name, startDateFrom, startDateTo, pageable);

        var orders = new ArrayList<PageResponse.SortOrder>();
        for (Sort.Order o : result.getPageable().getSort()) {
            orders.add(new PageResponse.SortOrder(o.getProperty(), o.getDirection().name()));
        }

        return PageResponse.<ProjectDto>builder()
                .content(result.getContent())
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .first(result.isFirst())
                .last(result.isLast())
                .sort(orders)
                .build();
    }

    @Operation(summary = "Atualiza um projeto")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = ProjectDto.class)))
    @PutMapping("/{id}")
    public ProjectDto update(@PathVariable UUID id, @Valid @RequestBody ProjectUpdateRequest req) {
        return service.update(id, req);
    }

    @Operation(summary = "Exclui um projeto")
    @ApiResponse(responseCode = "204", description = "Sem conteúdo")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }

    @Operation(summary = "Altera o status de um projeto")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = ProjectDto.class)))
    @PatchMapping("/{id}/status")
    public ProjectDto changeStatus(@PathVariable UUID id, @Valid @RequestBody StatusUpdateRequest req) {
        return service.changeStatus(id, req);
    }
}
