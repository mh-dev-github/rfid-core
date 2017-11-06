package com.mh.rfid.core.conversion.configuration;

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
	

	private String queueCreateMessage;

	private String queuePushMessage;
	
	private String queueError;
	
	private String queueException;
	
	private String queueNotification;
	
	
	private String routingkeySuccessfulLoad;
	
	private String routingkeyLoadFixed;

	private String routingkeySuccessfulMessageCreation;
	
	private String routingkeyUnsuccessfulMessageCreation;
	
	private String routingkeyMessageCreationException;
	
	private String routingkeyNotify;
}