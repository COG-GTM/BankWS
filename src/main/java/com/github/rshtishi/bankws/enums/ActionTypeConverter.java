package com.github.rshtishi.bankws.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ActionTypeConverter implements AttributeConverter<ActionType, String> {

	@Override
	public String convertToDatabaseColumn(ActionType attribute) {
		return attribute == null ? null : attribute.getAction();
	}

	@Override
	public ActionType convertToEntityAttribute(String dbData) {
		if (dbData == null) {
			return null;
		}
		for (ActionType type : ActionType.values()) {
			if (type.getAction().equals(dbData)) {
				return type;
			}
		}
		throw new IllegalArgumentException("Unknown action type: " + dbData);
	}
}
