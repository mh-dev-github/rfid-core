package com.mh.rfid.core.pull.components.amqp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;

import com.mh.rfid.core.pull.configuration.AmqpProperties;
import com.mh.rfid.dto.ReportDto;
import com.mh.rfid.dto.RowDto;

import lombok.val;
import lombok.extern.slf4j.Slf4j;

//TODO CONTROLAR LOS ERRORES CUANDO NO FUNCIONE AMQP
//TODO las excepciones deben ser convertidas a un mensaje y enviados a una cola de notificaciones
@Slf4j
abstract public class PullProducer<M extends RowDto> {

	abstract protected AmqpTemplate getAmqpTemplate();

	abstract protected AmqpProperties getProperties();

	// -------------------------------------------------------------------------------------
	//
	// -------------------------------------------------------------------------------------
	public void extract() {
		val routingkey = getProperties().getRoutingkeyExtractRows();

		convertAndSend(getExchangeName(), routingkey, LocalDateTime.now().toString());
	}

	public void successfulExtraction(Long sequence) {
		val routingkey = getProperties().getRoutingkeySuccessfulExtraction();

		convertAndSend(getExchangeName(), routingkey, sequence);
	}

	public void retryFixedRows(List<Long> sequences) {
		for (val sequence : sequences) {
			successfulExtraction(sequence);
		}
	}

	public void extractionException(RuntimeException e) {
		log.error(e.getMessage(), e);
		val routingkey = getProperties().getRoutingkeyExtractionException();

		convertAndSend(getExchangeName(), routingkey, e);
	}

	// -------------------------------------------------------------------------------------
	//
	// -------------------------------------------------------------------------------------
	public void successfulTransformation(long sequence) {
		val routingkey = getProperties().getRoutingkeySuccessfulTransformation();

		convertAndSend(getExchangeName(), routingkey, sequence);
	}

	public void unsuccessfulTransformation(long sequence) {
		val routingkey = getProperties().getRoutingkeyUnsuccessfulTransformation();

		convertAndSend(getExchangeName(), routingkey, sequence);
	}

	public void transformationException(long sequence, RuntimeException e) {
		log.error(e.getMessage(), e);
		val routingkey = getProperties().getRoutingkeyTransformationException();

		convertAndSend(getExchangeName(), routingkey, e);
	}

	// -------------------------------------------------------------------------------------
	//
	// -------------------------------------------------------------------------------------
	public void successfulLoad(List<M> rows) {
		val externalIds = rows.stream().map(RowDto::getExternalId).distinct().collect(Collectors.toList());

		externalIds.forEach(externalId -> {
			val routingkey = getProperties().getRoutingkeySuccessfulLoad();

			convertAndSend(getExchangeName(), routingkey, externalId);
		});
	}

	public void loadPutOnStandby(long sequence) {
		val routingkey = getProperties().getRoutingkeyLoadPutOnStandby();

		convertAndSend(getExchangeName(), routingkey, sequence);
	}

	public void unsuccessfulLoad(long sequence) {
		val routingkey = getProperties().getRoutingkeyUnsuccessfulLoad();

		convertAndSend(getExchangeName(), routingkey, sequence);
	}

	public void loadException(RuntimeException e) {
		log.error(e.getMessage(), e);
		val routingkey = getProperties().getRoutingkeyLoadException();

		convertAndSend(getExchangeName(), routingkey, e);
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
