package com.github.rshtishi.bankws.entity;

import com.github.rshtishi.bankws.enums.ActionType;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ActionTypeConverter implements AttributeConverter<ActionType, String> {

	@Override
	public String convertToDatabaseColumn(ActionType actionType) {
		return actionType == null ? null : actionType.getAction();
	}

	@Override
	public ActionType convertToEntityAttribute(String action) {
		if (action == null) {
			return null;
		}
		for (ActionType actionType : ActionType.values()) {
			if (actionType.getAction().equalsIgnoreCase(action)) {
				return actionType;
			}
		}
		throw new IllegalArgumentException("Unknown action type: " + action);
	}

}
