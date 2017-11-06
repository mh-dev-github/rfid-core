package com.mh.rfid.core.pull.service.impl.exceptions;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.mh.rfid.domain.esb.BaseEntity;
import com.mh.rfid.enums.IntegracionType;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class EntityDuplicatedLoadException extends EntityLoadException {
	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

	private static final long serialVersionUID = 1L;

	public EntityDuplicatedLoadException(IntegracionType integracionType, BaseEntity entity) {
		super(message(integracionType, entity));
		codigo = "error_try_create_but_already_exist";
	}

	private static String message(IntegracionType integracionType, BaseEntity entity) {
		StringBuilder sb = new StringBuilder();
		sb.append("Se solicitó crear una nueva entidad del flujo ");
		sb.append(integracionType.toString());
		sb.append(", pero ya existe en la base de datos.");

		LocalDateTime ldt;
		ldt = entity.getFechaUltimoCambioEnOrigen();
		{
			String mensaje = "\nLa última vez que esta entidad fue modificada en el origen fue el %s.";
			sb.append(String.format(mensaje, ldt.format(formatter)));
		}

		ldt = entity.getFechaUltimaExtraccion();
		{
			String mensaje = "\\nLa última vez que esta entidad fue extraida desde el origen fue el %s.";
			sb.append(String.format(mensaje, ldt.format(formatter)));
		}

		ldt = entity.getFechaUltimaIntegracion();
		if (ldt != null) {
			String mensaje = "La última vez que esta entidad fue sincronizada en el destino fue el %s.";
			sb.append(String.format(mensaje, ldt.format(formatter)));
		} else {
			String mensaje = "Esta entidad actualmente se encuentra en proceso de sincronización hacia el destino y tiene %d sincronizaciones pendientes";
			sb.append(String.format(mensaje, entity.getSincronizacionesEnCola()));
		}

		String result = sb.toString();

		return result;
	}
}