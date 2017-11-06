package com.mh.rfid.core.push.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.client.HttpClientErrorException;

import com.mh.rfid.core.push.components.rest.RestClient;
import com.mh.rfid.core.push.service.api.VerificationService;
import com.mh.rfid.domain.esb.BaseEntity;
import com.mh.rfid.domain.msg.Mensaje;
import com.mh.rfid.dto.MessageDto;
import com.mh.rfid.enums.EstadoMensajeType;
import com.mh.rfid.enums.IntegracionType;
import com.mh.rfid.repository.msg.MensajeRepository;

import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class VerificationServiceImpl<M extends MessageDto, E extends BaseEntity>
		implements VerificationService<E> {

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
	// verify
	// -------------------------------------------------------------------------------------
	@Override
	final public EstadoMensajeType verifyMessage(long mid) {
		log.debug("Inicio operación verification de mensaje");

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

		log.debug("Entidad con externalId:{} encontrada", entity.getExternalId());

		val result = verifyMessage(message, entity);

		return result;
	}

	final protected EstadoMensajeType verifyMessage(Mensaje message, E entity) {
		EstadoMensajeType result;
		M dto;
		try {
			dto = getRestClient().get(entity);
		} catch (RuntimeException e) {
			result = reintentar(message, entity, e);
			return result;
		}

		try {
			verificarConsistencia(entity, dto);
			result = finalizarConsistente(message, entity);
		} catch (RuntimeException e) {
			result = finalizarInconsistente(message, e);
		}

		return result;
	}

	final protected void verificarConsistencia(E entity, M dto) {
		val errores = new ArrayList<String>();

		compare(entity, dto, errores);

		if (errores.size() > 0) {
			val error = StringUtils.join(errores, "\n");
			throw new RuntimeException(error);
		}
	}

	abstract protected void compare(E entity, M dto, List<String> errores);

	protected void compare(String attributeName, String valorOrigen, String valorDestino, List<String> errores) {
		valorOrigen = StringUtils.defaultString(valorOrigen);
		valorDestino = StringUtils.defaultString(valorDestino);

		if (StringUtils.compare(valorOrigen, valorDestino) != 0) {
			val sb = new StringBuilder();
			sb.append(attributeName);
			sb.append(" ORIGEN:");
			sb.append(valorOrigen);
			sb.append(", ");
			sb.append(attributeName);
			sb.append(" DESTINO:");
			sb.append(valorDestino);
			sb.append("\n");

			errores.add(sb.toString());
		}
	}

	// -------------------------------------------------------------------------------------
	// finalizacion
	// -------------------------------------------------------------------------------------
	final protected EstadoMensajeType finalizarConsistente(Mensaje message, E entity) {
		int statusCodeValue = 200;

		message.integradoConVerificacion(entity.getId(), statusCodeValue);
		entity.integrada(entity.getId());

		val newMessage = getMensajeRepository().saveAndFlush(message);
		getRepository().saveAndFlush(entity);

		EstadoMensajeType result = newMessage.getEstado();
		return result;
	}

	final protected EstadoMensajeType finalizarInconsistente(Mensaje message, RuntimeException e) {
		int statusCodeValue = -1;
		if (e instanceof HttpClientErrorException) {
			statusCodeValue = ((HttpClientErrorException) e).getRawStatusCode();
		}

		message.inconsistente(statusCodeValue, e);

		val newMessage = getMensajeRepository().saveAndFlush(message);

		EstadoMensajeType result = newMessage.getEstado();
		return result;
	}

	// -------------------------------------------------------------------------------------
	// reintento
	// -------------------------------------------------------------------------------------
	final protected EstadoMensajeType reintentar(Mensaje message, E entity, RuntimeException e) {
		message.errorDuranteVerificacion(getNumeroMaximoReintentos(), e);

		boolean saveEntity = entity.integracionIniciada();
		val newMessage = getMensajeRepository().saveAndFlush(message);
		if (saveEntity) {
			entity = getRepository().saveAndFlush(entity);
		}

		EstadoMensajeType result = newMessage.getEstado();
		return result;
	}

	protected int getNumeroMaximoReintentos() {
		return 30;
	}
}
