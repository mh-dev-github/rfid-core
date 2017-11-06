package com.mh.rfid.core.pull.configuration;

import org.springframework.validation.annotation.Validated;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Validated
public class AmqpProperties {

	private String exchange;
	

	
	private String queueExtract;

	private String queueTransform;

	private String queueLoad;

	private String queueStandByLoad;

	private String queueCreateMessage;

	private String queueError;

	private String queueException;
	
	private String queueNotification;
	
	

	private String routingkeyExtractRows;

	private String routingkeySuccessfulExtraction;

	private String routingkeyExtractionException;
	

	private String routingkeySuccessfulTransformation;

	private String routingkeyUnsuccessfulTransformation;

	private String routingkeyTransformationException;
	

	private String routingkeySuccessfulLoad;

	private String routingkeyLoadPutOnStandby;

	private String routingkeyUnsuccessfulLoad;

	private String routingkeyLoadException;
	
	
	private String routingkeyNotify;
}