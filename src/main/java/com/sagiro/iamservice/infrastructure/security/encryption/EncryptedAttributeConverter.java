package com.sagiro.iamservice.infrastructure.security.encryption;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class EncryptedAttributeConverter implements AttributeConverter<String, String> {

    private EncryptionService getEncryptionService() {
        return SpringContext.getBean(EncryptionService.class);
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) {
            return null;
        }
        return getEncryptionService().encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return getEncryptionService().decrypt(dbData);
    }
}
