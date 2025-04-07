package com.aifinancial.clarity.poc.converter;

import com.aifinancial.clarity.poc.model.Role;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.stereotype.Component;

@Converter(autoApply = true)
@Component
public class RoleConverter implements AttributeConverter<Role, String> {

    @Override
    public String convertToDatabaseColumn(Role attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name();
    }

    @Override
    public Role convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        try {
            return Role.valueOf(dbData);
        } catch (IllegalArgumentException e) {
            // 處理可能的轉換錯誤
            return Role.NORMAL; // 預設角色
        }
    }
} 