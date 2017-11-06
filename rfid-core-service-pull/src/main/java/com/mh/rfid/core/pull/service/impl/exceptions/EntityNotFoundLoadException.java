package com.mh.rfid.core.pull.service.impl.exceptions;

import com.mh.rfid.enums.IntegracionType;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class EntityNotFoundLoadException extends EntityLoadException {

	private static final long serialVersionUID = 1L;

	private String codigo;

	public EntityNotFoundLoadException(IntegracionType integracionType, String externalId) {
		super(message(integracionType, externalId));
		codigo = "error_try_update_but_not_exist";
	}

	private static String message(IntegracionType integracionType, String externalId) {
		StringBuilder sb = new StringBuilder();
		sb.append("Se solicitó actualizar la entidad con external id " + externalId + " del flujo ");
		sb.append(integracionType.toString());
		sb.append(", pero no existe en la base de datos.");

		String result = sb.toString();

		return result;
	}
}
