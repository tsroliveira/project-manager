package com.portfolio.web;

import com.portfolio.repository.ProjectMemberAllocationRepository;
import com.portfolio.service.AllocationService;
import com.portfolio.web.dto.AllocationItemDto;
import com.portfolio.web.dto.AllocationRequest;
import com.portfolio.web.error.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/projects/{projectId}/allocations")
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
public class AllocationController {

    private final AllocationService service;
    private final ProjectMemberAllocationRepository allocationRepository;

    public AllocationController(AllocationService service,
                                ProjectMemberAllocationRepository allocationRepository) {
        this.service = service;
        this.allocationRepository = allocationRepository;
    }

    @Operation(summary = "Lista membros alocados do projeto")
    @GetMapping
    public java.util.List<AllocationItemDto> list(@PathVariable UUID projectId) {
        return allocationRepository.findAllocationsWithCache(projectId).stream()
                .map(p -> new AllocationItemDto(p.getMemberId(), p.getName(), p.getRole()))
                .toList();
    }

    @Operation(summary = "Aloca um membro ao projeto (apenas 'funcionário')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping
    public void allocate(@PathVariable UUID projectId, @Valid @RequestBody AllocationRequest req) {
        service.allocate(projectId, req.memberId());
    }

    @Operation(summary = "Remove a alocação de um membro do projeto")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{memberId}")
    public void deallocate(@PathVariable UUID projectId, @PathVariable String memberId) {
        service.deallocate(projectId, memberId);
    }
}
