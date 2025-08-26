package com.portfolio.repository.spec;

import com.portfolio.domain.Project;
import com.portfolio.domain.ProjectStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class ProjectSpecifications {

    public static Specification<Project> statusEquals(ProjectStatus status) {
        return (root, q, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Project> managerEquals(String managerId) {
        return (root, q, cb) -> (managerId == null || managerId.isBlank()) ? null :
                cb.equal(root.get("managerMemberId"), managerId);
    }

    public static Specification<Project> nameContains(String name) {
        return (root, q, cb) -> (name == null || name.isBlank()) ? null :
                cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    public static Specification<Project> startDateFrom(LocalDate d) {
        return (root, q, cb) -> d == null ? null : cb.greaterThanOrEqualTo(root.get("startDate"), d);
    }

    public static Specification<Project> startDateTo(LocalDate d) {
        return (root, q, cb) -> d == null ? null : cb.lessThanOrEqualTo(root.get("startDate"), d);
    }
}
