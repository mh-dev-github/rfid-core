package com.mh.rfid.core.push.configuration;

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
	

	
	private String queuePushMessage;

	private String queueMessageSent;

	private String queueMessageIntegrated;

	private String queueError;

	private String queueException;
	
	private String queueNotification;
	
	

	private String routingkeySuccessfulMessageCreation;

	private String routingkeyMessageSendingRetried;

	private String routingkeyUnsuccessfulMessageSending;

	private String routingkeySuccessfulMessageSending;

	private String routingkeyMessageSendingException;
	

	private String routingkeyMessageVerificationRetried;

	private String routingkeyUnsuccessfulMessageVerification;

	private String routingkeyMessageVerificationException;


	private String routingkeyUnsuccessfulMessageIntegration;
	
	private String routingkeySuccessfulMessageIntegration;

	
	private String routingkeyNotify;
}