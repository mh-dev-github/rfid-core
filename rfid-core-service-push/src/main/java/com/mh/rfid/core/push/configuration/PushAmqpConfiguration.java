package com.mh.rfid.core.push.configuration;

import java.util.Arrays;
import java.util.List;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Binding.DestinationType;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

import lombok.val;

public class PushAmqpConfiguration {

	public PushAmqpConfiguration() {
		super();
	}

	@Bean
	@ConfigurationProperties(prefix = "rfid.amqp")
	public AmqpProperties getProperties() {
		val result = new AmqpProperties();
		return result;
	}
	
	@Bean
	public Queue queuePushMessage(AmqpProperties properties) {
		val queue = QueueBuilder.durable(properties.getQueuePushMessage()).build();
		return queue;
	}

	@Bean
	public Queue queueMessageSent(AmqpProperties properties) {
		val queue = QueueBuilder.durable(properties.getQueueMessageSent()).build();
		return queue;
	}

	@Bean
	public Queue queueMessageIntegrated(AmqpProperties properties) {
		val queue = QueueBuilder.durable(properties.getQueueMessageIntegrated()).build();
		return queue;
	}

	@Bean
	public Queue queueError(AmqpProperties properties) {
		val queue = QueueBuilder.durable(properties.getQueueError()).build();
		return queue;
	}

	@Bean
	public Queue queueException(AmqpProperties properties) {
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
				new Binding(p.getQueuePushMessage(), DestinationType.QUEUE, exchange, p.getRoutingkeySuccessfulMessageCreation(), null),
				new Binding(p.getQueuePushMessage(), DestinationType.QUEUE, exchange, p.getRoutingkeyMessageSendingRetried(), null),
				new Binding(p.getQueueError(), DestinationType.QUEUE, exchange, p.getRoutingkeyUnsuccessfulMessageSending(), null),
				new Binding(p.getQueueMessageSent(), DestinationType.QUEUE, exchange, p.getRoutingkeySuccessfulMessageSending(), null),
				new Binding(p.getQueueException(), DestinationType.QUEUE, exchange, p.getRoutingkeyMessageSendingException(), null),

				new Binding(p.getQueueMessageSent(), DestinationType.QUEUE, exchange, p.getRoutingkeyMessageVerificationRetried(), null),
				new Binding(p.getQueueError(), DestinationType.QUEUE, exchange, p.getRoutingkeyUnsuccessfulMessageVerification(), null),
				new Binding(p.getQueueException(), DestinationType.QUEUE, exchange, p.getRoutingkeyMessageVerificationException(), null),

				new Binding(p.getQueueError(), DestinationType.QUEUE, exchange, p.getRoutingkeyUnsuccessfulMessageIntegration(), null),
				new Binding(p.getQueueMessageIntegrated(), DestinationType.QUEUE, exchange, p.getRoutingkeySuccessfulMessageIntegration(), null),
				
				new Binding(p.getQueueNotification(), DestinationType.QUEUE, exchange, p.getRoutingkeyNotify(), null)
				);
		// @formatter:on
	}
}