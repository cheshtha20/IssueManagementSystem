package com.issuemanage.auth.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Converter to handle the mapping between legacy role names in the database
 * and the new enum constants.
 */
@Converter(autoApply = true)
public class RoleConverter implements AttributeConverter<Role, String> {

    @Override
    public String convertToDatabaseColumn(Role role) {
        if (role == null) {
            return null;
        }
        return role.name();
    }

    @Override
    public Role convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }

        // Handle legacy role names
        switch (dbData) {
            case "ROLE_TICKET_RAISER":
                return Role.ROLE_EMPLOYEE;
            case "ROLE_ASSIGNEE":
                return Role.ROLE_SUPPORT_ENGINEER;
            case "ROLE_MANAGER":
                return Role.ROLE_TEAM_LEAD;
            default:
                try {
                    return Role.valueOf(dbData);
                } catch (IllegalArgumentException e) {
                    return null;
                }
        }
    }
}
