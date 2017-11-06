package com.mh.rfid.core.conversion.service.impl;

import static com.mh.rfid.enums.EstadoMensajeType.PENDIENTE_ENVIAR;

import java.time.LocalDateTime;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mh.rfid.core.conversion.service.api.MessageConversionService;
import com.mh.rfid.domain.esb.BaseEntity;
import com.mh.rfid.domain.msg.Mensaje;
import com.mh.rfid.dto.MessageDto;
import com.mh.rfid.enums.IntegracionType;
import com.mh.rfid.repository.msg.MensajeRepository;

import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class MessageConversionServiceImpl<E extends BaseEntity> implements MessageConversionService<E> {

	// -------------------------------------------------------------------------------------
	// IntegracionType
	// -------------------------------------------------------------------------------------
	abstract public IntegracionType getIntegracionType();

	// -------------------------------------------------------------------------------------
	// Repositories
	// -------------------------------------------------------------------------------------
	@Autowired
	private MensajeRepository mensajeRepository;

	protected MensajeRepository getMensajeRepository() {
		return mensajeRepository;
	}

	abstract protected JpaRepository<E, String> getRepository();

	// -------------------------------------------------------------------------------------
	// ObjectMapper
	// -------------------------------------------------------------------------------------
	abstract protected ObjectMapper getObjectMapper();

	// -------------------------------------------------------------------------------------
	// convert
	// -------------------------------------------------------------------------------------
	@Override
	final public Mensaje convert(String externalId) {
		Mensaje result = null;

		log.debug("Inicio operación conversión de entidad a mensaje");

		log.debug("Consultando entidad con externalId {}", externalId);
		E entity = this.getRepository().findOne(externalId);

		if (entity == null) {
			throw new EntityNotFoundException(externalId);
		}

		log.debug("Entidad con externalId {} encontrada y corresponde al flujo {}", externalId, getIntegracionType());
		try {
			Mensaje message = convertEntityToMessage(entity);

			result = getMensajeRepository().saveAndFlush(message);
			log.debug("Mensaje creado");

			entity.mensajeGenerado();
		} catch (RuntimeException e) {
			log.error(e.getMessage(), e);
			entity.mensajeNoGenerado();
		}

		getRepository().saveAndFlush(entity);
		log.debug("Entidad actualizada");

		return result;
	}

	final protected Mensaje convertEntityToMessage(E entity) {
		Mensaje result = null;

		val dto = convertEntityToDto(entity);

		String json = convertDtoToJson(dto);

		if (json != null) {
			// @formatter:off
			result = Mensaje
					.builder()
					.integracion(getIntegracionType())
					.externalId(entity.getExternalId())
					.id(entity.getId())
					.operacion(entity.getOperacion())
					.estado(PENDIENTE_ENVIAR)
					.intentos(0)
					.fechaUltimoIntento(LocalDateTime.now())
					.datos(json)
					.build();
			// @formatter:on
		}

		return result;
	}

	final protected String convertDtoToJson(final MessageDto dto) {
		String result = null;

		try {
			result = getObjectMapper().writeValueAsString(dto);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e.getMessage(), e);
		}

		return result;
	}

	// -------------------------------------------------------------------------------------
	//
	// -------------------------------------------------------------------------------------
	abstract protected MessageDto convertEntityToDto(E entity);
}
