package com.github.rshtishi.bankws.converter;

import java.util.Arrays;

import com.github.rshtishi.bankws.enums.ActionType;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
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
		return Arrays.stream(ActionType.values())
				.filter(type -> type.getAction().equals(action))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Unknown action type: " + action));
	}

}
