package com.portfolio.web;

import com.portfolio.service.ReportService;
import com.portfolio.web.dto.PortfolioReportDto;
import com.portfolio.web.error.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@ApiResponses({
    @ApiResponse(responseCode = "400", description = "Requisição inválida",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Erro interno",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
})
public class ReportController {

    private final ReportService service;

    public ReportController(ReportService service) {
        this.service = service;
    }

    @Operation(summary = "Relatório resumido do portfólio")
    @GetMapping("/portfolio")
    public PortfolioReportDto portfolio() {
        return service.getPortfolioReport();
    }
}
