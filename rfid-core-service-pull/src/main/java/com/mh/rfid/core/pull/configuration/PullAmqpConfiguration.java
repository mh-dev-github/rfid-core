package com.mh.rfid.core.pull.configuration;

import java.util.Arrays;
import java.util.List;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Binding.DestinationType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;

import lombok.val;

public class PullAmqpConfiguration {

	public PullAmqpConfiguration() {
		super();
	}

	@Bean
	@ConfigurationProperties(prefix = "rfid.amqp")
	public AmqpProperties amqpProperties() {
		val result = new AmqpProperties();
		return result;
	}

	@Bean
	public Queue queueExtract(AmqpProperties properties) {
		val queue = QueueBuilder.durable(properties.getQueueExtract()).build();
		return queue;
	}

	@Bean
	public Queue queueTransform(AmqpProperties properties) {
		val queue = QueueBuilder.durable(properties.getQueueTransform()).build();
		return queue;
	}

	@Bean
	public Queue queueLoad(AmqpProperties properties) {
		val queue = QueueBuilder.durable(properties.getQueueLoad()).build();
		return queue;
	}

	@Bean
	public Queue queueStandByLoad(AmqpProperties properties) {
		val queue = QueueBuilder.durable(properties.getQueueStandByLoad()).build();
		return queue;
	}

	@Bean
	public Queue queueCreateMessage(AmqpProperties properties) {
		val queue = QueueBuilder.durable(properties.getQueueCreateMessage()).build();
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
				new Binding(p.getQueueExtract(), DestinationType.QUEUE, exchange, p.getRoutingkeyExtractRows(),null),
				new Binding(p.getQueueTransform(), DestinationType.QUEUE, exchange, p.getRoutingkeySuccessfulExtraction(), null),
				new Binding(p.getQueueException(), DestinationType.QUEUE, exchange, p.getRoutingkeyExtractionException(), null),
				
				new Binding(p.getQueueLoad(), DestinationType.QUEUE, exchange, p.getRoutingkeySuccessfulTransformation(), null),
				new Binding(p.getQueueError(), DestinationType.QUEUE, exchange, p.getRoutingkeyUnsuccessfulTransformation(), null),
				new Binding(p.getQueueException(), DestinationType.QUEUE, exchange, p.getRoutingkeyTransformationException(), null),
				
				new Binding(p.getQueueCreateMessage(), DestinationType.QUEUE, exchange, p.getRoutingkeySuccessfulLoad(), null),
				new Binding(p.getQueueStandByLoad(), DestinationType.QUEUE, exchange, p.getRoutingkeyLoadPutOnStandby(), null),
				new Binding(p.getQueueError(), DestinationType.QUEUE, exchange, p.getRoutingkeyUnsuccessfulLoad(), null),
				new Binding(p.getQueueException(), DestinationType.QUEUE, exchange, p.getRoutingkeyLoadException(), null),
				
				new Binding(p.getQueueNotification(), DestinationType.QUEUE, exchange, p.getRoutingkeyNotify(), null)
				);
		// @formatter:on
	}

}