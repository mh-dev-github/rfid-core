package com.mh.rfid.core.push.service.api;

import org.springframework.transaction.annotation.Transactional;

import com.mh.rfid.domain.esb.BaseEntity;
import com.mh.rfid.enums.EstadoMensajeType;
import com.mh.rfid.enums.IntegracionType;

public interface PushService<E extends BaseEntity> {

	IntegracionType getIntegracionType();

	// -------------------------------------------------------------------------------------
	// push
	// -------------------------------------------------------------------------------------
	@Transactional
	EstadoMensajeType push(long mid);
}