package com.mh.rfid.core.push.components.amqp;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;

import com.mh.rfid.core.push.configuration.AmqpProperties;
import com.mh.rfid.dto.ReportDto;

import lombok.val;
import lombok.extern.slf4j.Slf4j;

//TODO CONTROLAR LOS ERRORES CUANDO NO FUNCIONE AMQP
//TODO las excepciones deben ser convertidas a un mensaje y enviados a una cola de notificaciones
@Slf4j
abstract public class PushProducer {

	abstract protected AmqpTemplate getAmqpTemplate();

	abstract protected AmqpProperties getProperties();

	// -------------------------------------------------------------------------------------
	//
	// -------------------------------------------------------------------------------------
	public void messageSendingRetried(long mid) {
		val routingkey = getProperties().getRoutingkeyMessageSendingRetried();
		val delay = randomDelay();

		convertAndSend(getExchangeName(), routingkey, mid, delay);
	}

	public void unsuccessfulMessageSending(long mid) {
		val routingkey = getProperties().getRoutingkeyUnsuccessfulMessageSending();

		convertAndSend(getExchangeName(), routingkey, mid);
	}

	public void successfulMessageSending(long mid) {
		val routingkey = getProperties().getRoutingkeySuccessfulMessageSending();
		val delay = randomDelay();

		convertAndSend(getExchangeName(), routingkey, mid, delay);
	}
	
	public void messageSendingException(RuntimeException e) {
		log.error(e.getMessage(), e);
		val routingkey = getProperties().getRoutingkeyMessageSendingException();
		
		convertAndSend(getExchangeName(), routingkey, e);
	}

	public void messageVerificationRetried(long mid) {
		val routingkey = getProperties().getRoutingkeyMessageVerificationRetried();
		val delay = randomDelay();

		convertAndSend(getExchangeName(), routingkey, mid, delay);
	}

	public void unsuccessfulMessageVerification(long mid) {
		val routingkey = getProperties().getRoutingkeyUnsuccessfulMessageVerification();

		convertAndSend(getExchangeName(), routingkey, mid);
	}

	public void messageVerificationException(RuntimeException e) {
		log.error(e.getMessage(), e);
		val routingkey = getProperties().getRoutingkeyMessageVerificationException();
		
		convertAndSend(getExchangeName(), routingkey, e);
	}

	public void unsuccessfulMessageIntegration(long mid) {
		val routingkey = getProperties().getRoutingkeyUnsuccessfulMessageIntegration();

		convertAndSend(getExchangeName(), routingkey, mid);
	}
	
	public void successfulMessageIntegration(long mid) {
		val routingkey = getProperties().getRoutingkeySuccessfulMessageIntegration();

		convertAndSend(getExchangeName(), routingkey, mid);
	}

	
	// -------------------------------------------------------------------------------------
	//
	// -------------------------------------------------------------------------------------
	public void notifyError(ReportDto report) {
		val routingkey = getProperties().getRoutingkeyNotify();

		convertAndSend(getExchangeName(), routingkey, report);
	}

	// -------------------------------------------------------------------------------------
	//
	// -------------------------------------------------------------------------------------
	protected String getExchangeName() {
		return getProperties().getExchange();
	}

	protected void convertAndSend(String exchange, String routingKey, Object message) {
		convertAndSend(exchange, routingKey, message, 1);
	}

	protected void convertAndSend(String exchange, String routingKey, Object message, int delay) {
		log.debug("{}:{}:{delay={}}", routingKey, message, delay);
		getAmqpTemplate().convertAndSend(exchange, routingKey, message, getMessagePostProcessor(delay));
	}

	protected MessagePostProcessor getMessagePostProcessor(final int delay) {
		return new MessagePostProcessor() {
			@Override
			public Message postProcessMessage(Message message) throws AmqpException {
				message.getMessageProperties().setDelay(delay);
				return message;
			}
		};
	}

	public int randomDelay() {
		return (60) * 1000;
	}
}
