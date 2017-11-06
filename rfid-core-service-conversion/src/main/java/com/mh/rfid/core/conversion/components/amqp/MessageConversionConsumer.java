package com.mh.rfid.core.conversion.components.amqp;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.scheduling.annotation.Scheduled;

import com.mh.rfid.core.conversion.service.api.MessageConversionService;
import com.mh.rfid.domain.esb.BaseEntity;
import com.mh.rfid.enums.EstadoSincronizacionType;
import com.mh.rfid.repository.esb.BaseEntityRepository;

import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
abstract public class MessageConversionConsumer<E extends BaseEntity> {

	abstract protected MessageConversionProducer getProducer();

	abstract protected MessageConversionService<E> getMessageConversionService();

	abstract protected BaseEntityRepository<E> getRepository();

	//TODO
	//abstract protected ErrorReportService getErrorReportService();
	
	@Scheduled(cron = "${sync.cron}")
	public void cron() {
		log.debug("sync.cron");

		val entities = getRepository().findAllByEstado(EstadoSincronizacionType.CARGUE_CORREGIDO);
		for (val entity : entities) {
			getProducer().loadFixed(entity.getExternalId());
		}
	}

	@RabbitListener(queues = "${rfid.amqp.queue-create-message}")
	public void recievedMessageCreateMessage(String externalId) {
		log.debug("Mensaje CREATE MESSAGE: " + externalId);

		try {
			val mensaje = getMessageConversionService().convert(externalId);

			if (mensaje != null) {
				getProducer().successfulMessageCreation(mensaje.getMid());
			} else {
				getProducer().unsuccessfulMessageCreation(externalId);
			}
		} catch (RuntimeException e) {
			getProducer().messageCreationException(e);
			return;
		}
	}
}