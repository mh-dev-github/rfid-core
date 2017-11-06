package com.mh.rfid.core.pull.components.dao;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.mh.rfid.domain.stage.Error;
import com.mh.rfid.dto.RowDto;

public interface RowDao<M extends RowDto> {

	List<M> getRowsFromStage(long secuencia);

	@Transactional
	List<Long> saveExtractedRows(List<M> rows, int batchSize);

	@Transactional
	void saveTransformedRows(List<M> rows, List<Error> errores);

	@Transactional
	void saveLoadedRows(List<M> rows, List<Error> errores);

	@Transactional(readOnly=true)
	List<Long> getSequencesWithFixedRows();
}