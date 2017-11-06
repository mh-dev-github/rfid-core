package com.mh.rfid.core.conversion.service.api;

import org.springframework.transaction.annotation.Transactional;

import com.mh.rfid.domain.esb.BaseEntity;
import com.mh.rfid.domain.msg.Mensaje;
import com.mh.rfid.enums.IntegracionType;

public interface MessageConversionService<E extends BaseEntity> {

	IntegracionType getIntegracionType();

	@Transactional
	Mensaje convert(String externalId);
}