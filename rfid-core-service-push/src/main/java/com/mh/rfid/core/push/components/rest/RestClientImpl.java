package com.mh.rfid.core.push.components.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.mh.rfid.core.push.configuration.ApesRestProperties;
import com.mh.rfid.domain.esb.BaseEntity;
import com.mh.rfid.domain.msg.Mensaje;
import com.mh.rfid.dto.MessageDto;

import lombok.val;


public abstract class RestClientImpl<M extends MessageDto, E extends BaseEntity> implements RestClient<M, E> {

	protected static final String HTTP_HEADER_AUTHORIZATION = "Authorization";

	@Autowired
	private ApesRestProperties properties;

	@Autowired
	private RestTemplate restTemplate;

	// -------------------------------------------------------------------------------------
	// HTTP Methods
	// -------------------------------------------------------------------------------------
	@Override
	public M get(E entity) {
		val responseType = getResponseType();
		val result = get(entity, responseType);
		return result.getBody();
	}

	protected <T> ResponseEntity<T> get(E entity, Class<T> responseType) {
		val url = getApiUriResourceGetById(entity);
		val authorizationToken = getAuthorizationToken();

		val request = createRequestEntity("", authorizationToken);
		val result = restTemplate.exchange(url, HttpMethod.GET, request, responseType);
		return result;
	}

	@Override
	public ResponseEntity<M> post(Mensaje message) {
		val url = getApiUriResourcePost(message);
		val authorizationToken = getAuthorizationToken();
		val body = message.getDatos();
		val responseType = getResponseType();

		val request = createRequestEntity(body, authorizationToken);
		val result = restTemplate.exchange(url, HttpMethod.POST, request, responseType);
		return result;
	}

	@Override
	public ResponseEntity<M> put(Mensaje message) {
		val url = getApiUriResourcePut(message);
		val authorizationToken = getAuthorizationToken();
		val body = message.getDatos();
		val responseType = getResponseType();

		val request = createRequestEntity(body, authorizationToken);
		val result = restTemplate.exchange(url, HttpMethod.PUT, request, responseType);
		return result;
	}

	protected HttpEntity<?> createRequestEntity(String body, String authorizationToken) {
		HttpEntity<?> result;
		HttpHeaders headers = createHttpHeaders(authorizationToken);
		result = new HttpEntity<>(body, headers);
		return result;
	}

	protected HttpHeaders createHttpHeaders(String authorizationToken) {
		HttpHeaders headers;
		headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set(HTTP_HEADER_AUTHORIZATION, authorizationToken);
		return headers;
	}

	@Override
	public int getNumeroMaximoReintentos() {
		val result = properties.getNumeroMaximoReintentos();
		return result;
	}

	abstract protected Class<M> getResponseType();

	// -------------------------------------------------------------------------------------
	//
	// -------------------------------------------------------------------------------------
	protected String getUriBase() {
		return properties.getUriBase();
	}

	protected String getUriResourcePath() {
		return properties.getUriResourcePath();
	}

	public String getAuthorizationToken() {
		return properties.getAuthorizationToken();
	}

	protected String getApiUriResource() {
		String apiUriBase = getUriBase();
		String resourcePath = getUriResourcePath();

		if (apiUriBase.endsWith("/")) {
			apiUriBase = apiUriBase.substring(0, apiUriBase.length() - 1);
		}

		if (!resourcePath.startsWith("/")) {
			apiUriBase = "/" + resourcePath;
		}

		val result = getUriBase() + getUriResourcePath();

		return result;
	}

	protected String getApiUriResourceGetById(E entity) {
		return getApiUriResource() + "/" + entity.getExternalId();
	}

	protected String getApiUriResourcePost(Mensaje message) {
		return getApiUriResource();
	}

	protected String getApiUriResourcePut(Mensaje message) {
		return getApiUriResource() + "/" + message.getExternalId();
	}
}
