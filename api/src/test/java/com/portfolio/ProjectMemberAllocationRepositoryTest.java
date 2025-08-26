package com.portfolio.repository;

import com.portfolio.domain.Project;
import com.portfolio.domain.ProjectMemberAllocation;
import com.portfolio.domain.ProjectStatus;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import static java.util.stream.Collectors.joining;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProjectMemberAllocationRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired private ProjectRepository projectRepository;
    @Autowired private ProjectMemberAllocationRepository allocationRepository;
    @Autowired private EntityManager em;

    @Test
    void existsAndCountByProjectWork() {
        // Projeto mínimo
        Project p = new Project();
        p.setName("Projeto X");
        p.setStartDate(LocalDate.now());
        p.setExpectedEndDate(LocalDate.now().plusDays(30));
        p.setTotalBudget(new BigDecimal("1000.00"));
        p.setDescription("teste");
        p.setManagerMemberId("mgr-1");
        p.setStatus(ProjectStatus.PLANEJADO);
        p = projectRepository.save(p);

        // Descobre a coluna alvo do FK fk_alloc_member_cache em member_cache (ex.: external_id)
        String memberCacheKey = fkTargetColumn();

        // Garante as chaves no lado referenciado (member_cache) preenchendo TODAS as NOT NULL sem default
        upsertMember(memberCacheKey, "user-1");
        upsertMember(memberCacheKey, "user-2");

        // Cria alocações
        var a1 = new ProjectMemberAllocation();
        a1.setProject(p);
        a1.setMemberExternalId("user-1");

        var a2 = new ProjectMemberAllocation();
        a2.setProject(p);
        a2.setMemberExternalId("user-2");

        allocationRepository.save(a1);
        allocationRepository.save(a2);

        // Asserts
        assertThat(allocationRepository.existsByProject_IdAndMemberExternalId(p.getId(), "user-1")).isTrue();
        assertThat(allocationRepository.existsByProject_IdAndMemberExternalId(p.getId(), "user-999")).isFalse();
        assertThat(allocationRepository.countByProject_Id(p.getId())).isEqualTo(2L);

        allocationRepository.deleteByProject_IdAndMemberExternalId(p.getId(), "user-2");
        assertThat(allocationRepository.countByProject_Id(p.getId())).isEqualTo(1L);
    }

    /** Descobre a coluna de member_cache referenciada pelo FK fk_alloc_member_cache */
    private String fkTargetColumn() {
        Object col = em.createNativeQuery("""
            select ccu.column_name
            from information_schema.table_constraints tc
            join information_schema.key_column_usage kcu
              on tc.constraint_name = kcu.constraint_name
             and tc.table_schema   = kcu.table_schema
            join information_schema.constraint_column_usage ccu
              on ccu.constraint_name = tc.constraint_name
             and ccu.table_schema    = tc.table_schema
            where tc.constraint_type = 'FOREIGN KEY'
              and tc.table_name      = 'project_member_allocations'
              and tc.constraint_name = 'fk_alloc_member_cache'
            """).getSingleResult();
        return String.valueOf(col);
    }

    /** Insere em member_cache todos os campos necessários (NOT NULL sem default) + a coluna referenciada pelo FK */
    private void upsertMember(String keyCol, String externalId) {
        // Tipo do ID para gerarmos um valor válido
        String idType = String.valueOf(
            em.createNativeQuery("""
               select data_type
               from information_schema.columns
               where table_name = 'member_cache' and column_name = 'id'
            """).getSingleResult()
        ).toLowerCase();

        Object idValue;
        if (idType.contains("uuid")) {
            idValue = UUID.randomUUID();
        } else if (idType.contains("bigint")) {
            idValue = Math.abs(UUID.randomUUID().getMostSignificantBits());
        } else if (idType.contains("int")) {
            idValue = (int) Math.abs(UUID.randomUUID().getMostSignificantBits() % Integer.MAX_VALUE);
        } else if (idType.contains("char") || idType.contains("text")) {
            idValue = UUID.randomUUID().toString();
        } else {
            idValue = UUID.randomUUID().toString();
        }

        // Descobre TODAS as colunas NOT NULL e sem default (tirando created_at/updated_at, id e a key do FK)
        @SuppressWarnings("unchecked")
        List<Object[]> required = em.createNativeQuery("""
            select column_name, data_type
            from information_schema.columns
            where table_name = 'member_cache'
              and is_nullable = 'NO'
              and (column_default is null or column_default = '')
              and column_name not in ('created_at','updated_at', :keyCol, 'id')
            order by ordinal_position
        """).setParameter("keyCol", keyCol).getResultList();

        // Monta colunas e valores na ordem
        LinkedHashMap<String, Object> values = new LinkedHashMap<>();
        values.put("id", idValue);

        for (Object[] row : required) {
            String col = String.valueOf(row[0]);
            String type = String.valueOf(row[1]).toLowerCase();

            Object v;
            if ("name".equals(col)) {
                v = "User " + externalId;
            } else if ("email".equals(col)) {
                v = externalId.replaceAll("[^a-zA-Z0-9._-]", "") + "@test.local";
            } else if (type.contains("uuid")) {
                v = UUID.randomUUID();
            } else if (type.contains("char") || type.contains("text")) {
                v = externalId; // texto único e simples
            } else if (type.contains("bigint")) {
                v = Math.abs(UUID.randomUUID().getMostSignificantBits());
            } else if (type.contains("int")) {
                v = (int) Math.abs(UUID.randomUUID().getMostSignificantBits() % Integer.MAX_VALUE);
            } else if (type.contains("bool")) {
                v = Boolean.FALSE;
            } else {
                // fallback razoável para tipos menos comuns
                v = externalId;
            }
            values.put(col, v);
        }

        // Por fim a coluna que o FK referencia (ex.: external_id)
        values.put(keyCol, externalId);

        String cols = values.keySet().stream().collect(joining(", "));
        String qs   = values.keySet().stream().map(k -> "?").collect(joining(", "));
        String sql  = "insert into member_cache (" + cols + ") values (" + qs + ") on conflict (" + keyCol + ") do nothing";

        Query q = em.createNativeQuery(sql);
        int idx = 1;
        for (Object v : values.values()) q.setParameter(idx++, v);
        q.executeUpdate();
    }
}
