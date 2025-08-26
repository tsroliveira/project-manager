package com.portfolio.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "project_member_allocations",
       uniqueConstraints = @UniqueConstraint(name = "uq_alloc_project_member",
                                            columnNames = {"project_id","member_external_id"}))
public class ProjectMemberAllocation {

    @Id
    @Column(nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "member_external_id", nullable = false, length = 64)
    private String memberExternalId;

    @Column(name = "allocated_at", nullable = false)
    private OffsetDateTime allocatedAt;

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (allocatedAt == null) allocatedAt = OffsetDateTime.now();
    }

    public ProjectMemberAllocation() { }

    public ProjectMemberAllocation(Project project, String memberExternalId) {
        this.project = project;
        this.memberExternalId = memberExternalId;
    }

    // getters/setters
    public UUID getId() { return id; }
    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }
    public String getMemberExternalId() { return memberExternalId; }
    public void setMemberExternalId(String memberExternalId) { this.memberExternalId = memberExternalId; }
    public OffsetDateTime getAllocatedAt() { return allocatedAt; }
    public void setAllocatedAt(OffsetDateTime allocatedAt) { this.allocatedAt = allocatedAt; }
}
