package com.mh.rfid.core.push.configuration;

import org.springframework.validation.annotation.Validated;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Validated
public class ApesRestProperties {
	
	private String uriBase;
	
	private String uriResourcePath;

	private String authorizationToken;
	
	private int numeroMaximoReintentos;
}
