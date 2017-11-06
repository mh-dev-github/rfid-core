package com.mh.rfid.core.push.components.rest;

import org.springframework.http.ResponseEntity;

import com.mh.rfid.domain.esb.BaseEntity;
import com.mh.rfid.domain.msg.Mensaje;
import com.mh.rfid.dto.MessageDto;

public interface RestClient<M extends MessageDto, E extends BaseEntity> {

	M get(E entity);

	ResponseEntity<M> post(Mensaje message);

	ResponseEntity<M> put(Mensaje message);

	int getNumeroMaximoReintentos();
}
