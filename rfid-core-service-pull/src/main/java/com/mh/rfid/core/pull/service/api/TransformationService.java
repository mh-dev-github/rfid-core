package com.mh.rfid.core.pull.service.api;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.mh.rfid.dto.RowDto;
import com.mh.rfid.enums.IntegracionType;

public interface TransformationService<M extends RowDto> {

	IntegracionType getIntegracionType();

	@Transactional
	List<M> transformRows(long sequence);
}