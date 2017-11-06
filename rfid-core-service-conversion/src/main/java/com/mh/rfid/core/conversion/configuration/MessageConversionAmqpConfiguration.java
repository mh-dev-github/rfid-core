package com.mh.rfid.core.conversion.configuration;

import java.util.Arrays;
import java.util.List;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Binding.DestinationType;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

import lombok.val;

public class MessageConversionAmqpConfiguration {

	public MessageConversionAmqpConfiguration() {
		super();
	}

	@Bean
	@ConfigurationProperties(prefix = "rfid.amqp")
	public AmqpProperties amqpProperties() {
		val result = new AmqpProperties();
		return result;
	}

	@Bean
	public Queue queueCreateMessage(AmqpProperties properties) {
		val queue = QueueBuilder.durable(properties.getQueueCreateMessage()).build();
		return queue;
	}

	@Bean
	public Queue queuePushMessage(AmqpProperties properties) {
		val queue = QueueBuilder.durable(properties.getQueuePushMessage()).build();
		return queue;
	}

	@Bean
	public Queue queueError(AmqpProperties properties) {
		val queue = QueueBuilder.durable(properties.getQueueError()).build();
		return queue;
	}

	@Bean
	public Queue queueQueueException(AmqpProperties properties) {
		val queue = QueueBuilder.durable(properties.getQueueException()).build();
		return queue;
	}
	
	@Bean
	public Queue queueNotification(AmqpProperties properties) {
		val queue = QueueBuilder.durable(properties.getQueueNotification()).build();
		return queue;
	}

	@Bean
	public List<Binding> bindings(AmqpProperties p) {
		val exchange = p.getExchange();
		// @formatter:off
		return Arrays.asList(
				new Binding(p.getQueueCreateMessage(), DestinationType.QUEUE, exchange, p.getRoutingkeySuccessfulLoad(), null),
				new Binding(p.getQueueCreateMessage(), DestinationType.QUEUE, exchange, p.getRoutingkeyLoadFixed(), null),
				
				new Binding(p.getQueuePushMessage(), DestinationType.QUEUE, exchange, p.getRoutingkeySuccessfulMessageCreation(), null),
				new Binding(p.getQueueError(), DestinationType.QUEUE, exchange, p.getRoutingkeyUnsuccessfulMessageCreation(), null),
				
				new Binding(p.getQueueException(), DestinationType.QUEUE, exchange, p.getRoutingkeyMessageCreationException(), null),
				
				new Binding(p.getQueueNotification(), DestinationType.QUEUE, exchange, p.getRoutingkeyNotify(), null)
				);
		// @formatter:on
	}

}