package com.mh.rfid.core.push.components.amqp;

import org.springframework.amqp.rabbit.annotation.RabbitListener;

import com.mh.rfid.core.push.service.api.VerificationService;
import com.mh.rfid.domain.esb.BaseEntity;

import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
abstract public class VerificationConsumer<E extends BaseEntity> {

	abstract protected PushProducer getProducer();

	abstract protected VerificationService<E> getVerificationService();
	
	@RabbitListener(queues = "${rfid.amqp.queue-message-sent}")
	public void recievedMessageSentMessage(long mid) {
		log.debug("Mensaje SENT MESSAGE: " + mid);

		try {
			val estado = getVerificationService().verifyMessage(mid);
			switch (estado) {
			case REINTENTAR_VERIFICACION:
				getProducer().messageVerificationRetried(mid);
				break;
			case ERROR_DURANTE_VERIFICACION:
				getProducer().unsuccessfulMessageVerification(mid);
				break;
			case INTEGRADO:
				getProducer().successfulMessageIntegration(mid);
				break;
			case INCONSISTENTE:
				getProducer().unsuccessfulMessageIntegration(mid);				
				break;
			default:
				return;
			}
		} catch (RuntimeException e) {
			getProducer().messageVerificationException(e);
			return;
		}
	}
}