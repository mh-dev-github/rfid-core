package com.mh.rfid.core.push.configuration;

import java.util.Arrays;
import java.util.List;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Binding.DestinationType;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;

import lombok.val;

public class VerificationAmqpConfiguration {

	public VerificationAmqpConfiguration() {
		super();
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
	public List<Binding> bindings(AmqpProperties p) {
		val exchangeName = p.getExchange();
		// @formatter:off
		return Arrays.asList(
				new Binding(p.getQueueMessageSent(), DestinationType.QUEUE, exchangeName, p.getRoutingkeySuccessfulMessageSending(), null),
				new Binding(p.getQueueMessageSent(), DestinationType.QUEUE, exchangeName, p.getRoutingkeyMessageVerificationRetried(), null),
				new Binding(p.getQueueError(), DestinationType.QUEUE, exchangeName, p.getRoutingkeyUnsuccessfulMessageVerification(), null),

				new Binding(p.getQueueMessageIntegrated(), DestinationType.QUEUE, exchangeName, p.getRoutingkeySuccessfulMessageIntegration(), null),
				new Binding(p.getQueueError(), DestinationType.QUEUE, exchangeName, p.getRoutingkeyUnsuccessfulMessageIntegration(), null),

				new Binding(p.getQueueException(), DestinationType.QUEUE, exchangeName, p.getRoutingkeyMessageVerificationException(), null)
				);
		// @formatter:on
	}
}