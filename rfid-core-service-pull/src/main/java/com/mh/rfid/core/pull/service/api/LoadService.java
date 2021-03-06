package com.mh.rfid.core.pull.service.api;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.mh.rfid.domain.esb.BaseEntity;
import com.mh.rfid.dto.RowDto;
import com.mh.rfid.enums.IntegracionType;

public interface LoadService<M extends RowDto, E extends BaseEntity> {

	IntegracionType getIntegracionType();

	@Transactional
	List<M> load(long secuencia);
}