package com.mh.rfid.core.push.components.amqp;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.scheduling.annotation.Scheduled;

import com.mh.rfid.core.push.service.api.PushErrorReportService;
import com.mh.rfid.core.push.service.api.PushService;
import com.mh.rfid.core.push.service.api.VerificationService;
import com.mh.rfid.domain.esb.BaseEntity;

import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
abstract public class PushConsumer<E extends BaseEntity> {

	abstract protected PushProducer getProducer();

	abstract protected PushService<E> getPushService();

	abstract protected PushErrorReportService getErrorReportService();

	abstract protected VerificationService<E> getVerificationService();

	// TODO reintento de corregidos
	@Scheduled(cron = "${sync.cron}")
	public void cron() {
		log.debug("sync.cron");

		//val sequences = getPushService().ge.getSequencesWithFixedRows();
		//getProducer().retryFixedRows(sequences);
	}

	@RabbitListener(queues = "${rfid.amqp.queue-push-message}")
	public void recievedMessagePushMessage(long mid) {
		log.debug("Mensaje PUSH MESSAGE: " + mid);

		try {
			val estado = getPushService().push(mid);
			switch (estado) {
			case REINTENTAR_ENVIO:
				getProducer().messageSendingRetried(mid);
				break;
			case ERROR_DURANTE_ENVIO:
				getProducer().unsuccessfulMessageSending(mid);
				break;
			case PENDIENTE_VERIFICAR:
				getProducer().successfulMessageSending(mid);
				break;
			case INTEGRADO:
				getProducer().successfulMessageIntegration(mid);
				break;
			default:
				return;
			}
		} catch (RuntimeException e) {
			getProducer().messageSendingException(e);
			return;
		}
	}

	@RabbitListener(queues = "${rfid.amqp.queue-message-sent}")
	public void recievedMessageSentMessage(long mid) {
		log.debug("Mensaje SENT MESSAGE: " + mid);

		if (getVerificationService() == null) {
			return;
		}

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

	@RabbitListener(queues = "${rfid.amqp.queue-error}")
	public void recievedMessageError(long mid) {
		log.debug("Mensaje ERROR: " + mid);

		try {
			val report = getErrorReportService().getReport(mid);
			getProducer().notifyError(report);
		} catch (RuntimeException e) {
			// TODO MANEJAR ERRORES DE NOTIFICACION
			return;
		}
	}

	// @RabbitListener(queues = "${rfid.amqp.queue-exception}")
	// public void recievedMessageError(RuntimeException exception) {
	// log.debug("Mensaje EXCEPTION: " +
	// StringUtils.defaultString(e.getMessage()));
	//
	// try {
	//// val report = getErrorReportService().getReport(sequence);
	//// val content = (String) report.get(ErrorReportService.CONTENT);
	//// getProducer().notifyError(content);
	// } catch (RuntimeException e) {
	// // TODO MANEJAR ERRORES DE NOTIFICACION
	// return;
	// }
	// }

}