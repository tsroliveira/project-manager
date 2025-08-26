package com.portfolio.domain.converter;

import com.portfolio.domain.ProjectStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ProjectStatusConverter implements AttributeConverter<ProjectStatus, String> {
    @Override
    public String convertToDatabaseColumn(ProjectStatus attribute) {
        return (attribute == null) ? null : attribute.getDbValue(); // usa 'em_analise', etc.
    }

    @Override
    public ProjectStatus convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        for (var s : ProjectStatus.values()) {
            if (s.getDbValue().equals(dbData)) return s;
        }
        throw new IllegalArgumentException("Status desconhecido no banco: " + dbData);
    }
}
