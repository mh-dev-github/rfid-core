package com.mh.rfid.core.push.service.impl;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.client.HttpClientErrorException;

import com.mh.rfid.core.push.components.rest.RestClient;
import com.mh.rfid.core.push.service.api.PushService;
import com.mh.rfid.domain.esb.BaseEntity;
import com.mh.rfid.domain.msg.Mensaje;
import com.mh.rfid.dto.MessageDto;
import com.mh.rfid.enums.EstadoMensajeType;
import com.mh.rfid.enums.IntegracionType;
import com.mh.rfid.enums.OperacionType;
import com.mh.rfid.repository.msg.MensajeRepository;

import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class PushServiceImpl<M extends MessageDto, E extends BaseEntity> implements PushService<E> {

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
	// RestClient
	// -------------------------------------------------------------------------------------
	abstract protected RestClient<M, E> getRestClient();

	// -------------------------------------------------------------------------------------
	// push
	// -------------------------------------------------------------------------------------
	@Override
	final public EstadoMensajeType push(long mid) {
		log.debug("Inicio operación push de mensaje");

		log.debug("Consultando mensaje con mid:{}", mid);
		Mensaje message = mensajeRepository.findOne(mid);
		if (message == null) {
			throw new EntityNotFoundException(String.valueOf(mid));
		}

		log.debug("Consultando entidad con externalId:{}", message.getExternalId());
		E entity = this.getRepository().findOne(message.getExternalId());
		if (entity == null) {
			throw new EntityNotFoundException(message.getExternalId());
		}

		EstadoMensajeType result;
		switch (message.getOperacion()) {
		case C:
			result = post(message, entity);
			break;
		case U:
			result = put(message, entity);
			break;
		default:
			throw new UnsupportedOperationException("Solo se admiten las operaciones de creación y modificación");
		}

		return result;
	}

	// -------------------------------------------------------------------------------------
	// post
	// -------------------------------------------------------------------------------------
	final protected EstadoMensajeType post(Mensaje message, E entity) {
		EstadoMensajeType result;

		if (requiereConciliacion(message)) {
			result = conciliar(message, entity);

			if (result != null) {
				return result;
			}
		}

		try {
			val response = getRestClient().post(message);
			val dto = response.getBody();
			int statusCodeValue = response.getStatusCodeValue();

			if (requiereVerificacion()) {
				result = verificar(message, entity, dto, statusCodeValue);
			} else {
				result = integracionFinalizadaSinVerificacion(message, entity, dto, statusCodeValue);
			}
		} catch (RuntimeException e) {
			result = reintentar(message, entity, e);
		}

		return result;
	}

	// -------------------------------------------------------------------------------------
	// put
	// -------------------------------------------------------------------------------------
	final protected EstadoMensajeType put(Mensaje message, E entity) {
		EstadoMensajeType result;

		try {
			val response = getRestClient().put(message);
			val dto = response.getBody();
			int statusCodeValue = response.getStatusCodeValue();

			if (requiereVerificacion()) {
				result = verificar(message, entity, dto, statusCodeValue);
			} else {
				result = integracionFinalizadaSinVerificacion(message, entity, dto, statusCodeValue);
			}
		} catch (RuntimeException e) {
			result = reintentar(message, entity, e);
		}

		return result;
	}

	// -------------------------------------------------------------------------------------
	// conciliacion
	// -------------------------------------------------------------------------------------
	protected boolean requiereConciliacion(Mensaje message) {
		if (!integracionSoportaConciliacion()) {
			return false;
		}
		if (!OperacionType.C.equals(message.getOperacion())) {
			return false;
		}
		switch(message.getEstado()) {
		case CORREGIDO:
		case REINTENTAR_ENVIO:
			break;
		default:
			return false;
		}

		return true;
	}

	protected boolean integracionSoportaConciliacion() {
		return true;
	}

	final protected EstadoMensajeType conciliar(Mensaje message, E entity) {
		EstadoMensajeType result;

		try {
			val dto = getRestClient().get(entity);
			if(dto != null) {
				int statusCodeValue = 200;

				if (requiereVerificacion()) {
					result = verificar(message, entity, dto, statusCodeValue);
				} else {
					result = integracionFinalizadaSinVerificacion(message, entity, dto, statusCodeValue);
				}
			}else {
				result = null;
			}
		} catch (HttpClientErrorException e) {
			result = null;
		} catch (RuntimeException e) {
			result = reintentar(message, entity, e);
		}

		return result;
	}

	// -------------------------------------------------------------------------------------
	// verificacion
	// -------------------------------------------------------------------------------------
	protected boolean requiereVerificacion() {
		return true;
	}

	final protected EstadoMensajeType verificar(Mensaje message, E entity, MessageDto dto, int statusCodeValue) {
		message.enviado(dto.getId(), statusCodeValue);

		entity.enviada(dto.getId());
		entity = getRepository().saveAndFlush(entity);

		val newMessage = getMensajeRepository().saveAndFlush(message);
		val result = newMessage.getEstado();
		return result;
	}

	// -------------------------------------------------------------------------------------
	// finalizacion
	// -------------------------------------------------------------------------------------
	protected EstadoMensajeType integracionFinalizadaSinVerificacion(Mensaje message, E entity, MessageDto dto, int statusCodeValue) {
		message.integradoSinVerificar(dto.getId(), statusCodeValue);

		entity.integrada(dto.getId());
		getRepository().saveAndFlush(entity);

		val newMessage = getMensajeRepository().saveAndFlush(message);
		val result = newMessage.getEstado();

		return result;
	}

	// -------------------------------------------------------------------------------------
	// reintento
	// -------------------------------------------------------------------------------------
	final protected EstadoMensajeType reintentar(Mensaje message, E entity, RuntimeException e) {
		message.errorDuranteEnvio(getRestClient().getNumeroMaximoReintentos(), e);

		boolean saveEntity = entity.integracionIniciada();
		if (saveEntity) {
			entity = getRepository().saveAndFlush(entity);
		}

		val newMessage = getMensajeRepository().saveAndFlush(message);
		val result = newMessage.getEstado();

		return result;
	}
}
