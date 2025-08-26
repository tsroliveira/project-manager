package com.portfolio.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "member_cache",
       uniqueConstraints = {
           @UniqueConstraint(name = "uq_member_cache_external", columnNames = "external_id")
       },
       indexes = {
           @Index(name = "idx_member_cache_role", columnList = "role")
       })
public class MemberCache {

    @Id
    @Column(length = 64, nullable = false)
    private String id;  // PK interna (varchar no banco)

    @Column(name = "external_id", length = 64, unique = true)
    private String externalId; // <<<<<< mapeia a coluna external_id

    @Column(length = 150, nullable = false)
    private String name;

    @Column(length = 50, nullable = false)
    private String role;

    // deixamos o banco preencher (DEFAULT now())
    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private OffsetDateTime updatedAt;

    public MemberCache() { }

    // getters/setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getExternalId() { return externalId; }
    public void setExternalId(String externalId) { this.externalId = externalId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
