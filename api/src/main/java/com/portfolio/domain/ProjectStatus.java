package com.portfolio.domain;

public enum ProjectStatus {
    EM_ANALISE("em_analise"),
    ANALISE_REALIZADA("analise_realizada"),
    ANALISE_APROVADA("analise_aprovada"),
    INICIADO("iniciado"),
    PLANEJADO("planejado"),
    EM_ANDAMENTO("em_andamento"),
    ENCERRADO("encerrado"),
    CANCELADO("cancelado");

    private final String dbValue;

    ProjectStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() { return dbValue; }

    /** Regra de transição (sem pular etapas). Cancelado pode a qualquer momento. */
    public boolean canTransitionTo(ProjectStatus next) {
        if (next == CANCELADO) return true;
        if (this == CANCELADO || this == ENCERRADO) return false;

        return switch (this) {
            case EM_ANALISE        -> next == ANALISE_REALIZADA;
            case ANALISE_REALIZADA -> next == ANALISE_APROVADA;
            case ANALISE_APROVADA  -> next == INICIADO;
            case INICIADO          -> next == PLANEJADO;
            case PLANEJADO         -> next == EM_ANDAMENTO;
            case EM_ANDAMENTO      -> next == ENCERRADO;
            default -> false;
        };
    }

    /** Apoio para mapear string do banco (enum Postgres) */
    public static ProjectStatus fromDb(String value) {
        for (var s : values()) if (s.dbValue.equals(value)) return s;
        throw new IllegalArgumentException("Status desconhecido: " + value);
    }
}
