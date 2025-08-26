package com.portfolio.web.dto;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageResponse<T> {
    private List<T> content;

    private int page;            // número da página atual (0-based)
    private int size;            // tamanho da página
    private long totalElements;  // total de registros
    private int totalPages;      // total de páginas
    private boolean first;       // se é a primeira página
    private boolean last;        // se é a última página

    private List<SortOrder> sort;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SortOrder {
        private String property;
        private String direction; // ASC / DESC
    }
}
