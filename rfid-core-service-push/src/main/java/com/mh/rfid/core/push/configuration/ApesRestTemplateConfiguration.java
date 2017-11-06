package com.mh.rfid.core.push.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import lombok.val;

public class ApesRestTemplateConfiguration {

	@Bean
	@ConfigurationProperties(prefix = "custom.rest.connection")
	public HttpComponentsClientHttpRequestFactory customHttpRequestFactory() {
		val result = new HttpComponentsClientHttpRequestFactory();
		return result;
	}

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder,
			HttpComponentsClientHttpRequestFactory requestFactory) {
		// @formatter:off
		val result = builder
				.requestFactory(requestFactory)
				.build();
		// @formatter:on

		return result;
	}

	@Bean
	@ConfigurationProperties(prefix = "apes.rest")
	public ApesRestProperties apesRestProperties() {
		val result = new ApesRestProperties();
		return result;
	}

}