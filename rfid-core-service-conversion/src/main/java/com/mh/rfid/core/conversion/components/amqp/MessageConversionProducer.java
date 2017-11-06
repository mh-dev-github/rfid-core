package com.mh.rfid.core.conversion.components.amqp;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;

import com.mh.rfid.core.conversion.configuration.AmqpProperties;

import lombok.val;
import lombok.extern.slf4j.Slf4j;

//TODO CONTROLAR LOS ERRORES CUANDO NO FUNCIONE AMQP
//TODO las excepciones deben ser convertidas a un mensaje y enviados a una cola de notificaciones
@Slf4j
abstract public class MessageConversionProducer {

	abstract protected AmqpTemplate getAmqpTemplate();

	abstract protected AmqpProperties getProperties();

	// -------------------------------------------------------------------------------------
	//
	// -------------------------------------------------------------------------------------
	public void loadFixed(String externalId) {
		val routingkey = getProperties().getRoutingkeyLoadFixed();

		convertAndSend(getExchangeName(), routingkey, externalId);
	}

	public void successfulMessageCreation(Long mid) {
		val routingkey = getProperties().getRoutingkeySuccessfulMessageCreation();

		convertAndSend(getExchangeName(), routingkey, mid);
	}

	public void unsuccessfulMessageCreation(String externalId) {
		val routingkey = getProperties().getRoutingkeyUnsuccessfulMessageCreation();

		convertAndSend(getExchangeName(), routingkey, externalId);
	}

	public void messageCreationException(RuntimeException e) {
		log.error(e.getMessage(), e);
		val routingkey = getProperties().getRoutingkeyMessageCreationException();

		convertAndSend(getExchangeName(), routingkey, e);
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
