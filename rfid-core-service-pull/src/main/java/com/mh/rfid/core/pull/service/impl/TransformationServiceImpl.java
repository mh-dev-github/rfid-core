package com.mh.rfid.core.pull.service.impl;

import static com.mh.rfid.dto.RowDto.discard;
import static com.mh.rfid.enums.EstadoRowType.CORREGIDO;
import static com.mh.rfid.enums.EstadoRowType.ERROR_ENRIQUECIMIENTO;
import static com.mh.rfid.enums.EstadoRowType.ERROR_HOMOLOGACION;
import static com.mh.rfid.enums.EstadoRowType.ERROR_VALIDACION;
import static com.mh.rfid.enums.EstadoRowType.ESTRUCTURA_VALIDA;
import static com.mh.rfid.enums.EstadoRowType.HOMOLOGADO;
import static com.mh.rfid.enums.EstadoRowType.VALIDADO;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.mh.rfid.core.pull.components.dao.RowDao;
import com.mh.rfid.core.pull.service.api.TransformationService;
import com.mh.rfid.domain.stage.Error;
import com.mh.rfid.dto.RowDto;
import com.mh.rfid.enums.IntegracionType;

import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
abstract public class TransformationServiceImpl<M extends RowDto> implements TransformationService<M> {

	// -------------------------------------------------------------------------------------
	// IntegracionType
	// -------------------------------------------------------------------------------------
	abstract public IntegracionType getIntegracionType();

	// -------------------------------------------------------------------------------------
	// DAO
	// -------------------------------------------------------------------------------------
	abstract protected RowDao<M> getRowDao();

	// -------------------------------------------------------------------------------------
	// transform rows
	// -------------------------------------------------------------------------------------
	@Override
	final public List<M> transformRows(long sequence) {
		val errores = new ArrayList<Error>();

		log.debug("Inicio de la transformación de los registros de la secuencia {}", sequence);

		log.debug("getRows({})", sequence);
		val rows = getRows(sequence);

		log.debug("beforeTranslateRows"); 
		beforeTranslateRows(rows, errores);

		log.debug("translateRows");
		translateRows(rows, errores);

		log.debug("beforeValidateRows");
		beforeValidateRows(rows, errores);

		log.debug("validateRows");
		validateRows(rows, errores);

		log.debug("getRowDao().saveTransformedRows");
		getRowDao().saveTransformedRows(rows, errores);

		log.debug("Fin de la transformación de los registros de la secuencia {}", sequence);

		return rows;
	}

	// -------------------------------------------------------------------------------------
	//
	// -------------------------------------------------------------------------------------
	final protected List<M> getRows(long sequence) {
		log.debug("getRowDao().getRows({})", sequence);
		// @formatter:off
		val result = getRowDao()
				.getRowsFromStage(sequence)
				.stream()
				.filter(row -> row.getEstado().equals(ESTRUCTURA_VALIDA) || row.getEstado().equals(CORREGIDO))
				.collect(Collectors.toList());
		// @formatter:on

		val groups = result.stream().collect(Collectors.groupingBy(RowDto::getEstado));
		for (val entry : groups.entrySet()) {
			log.debug("Se encontraron {} registros con estado {}", entry.getValue().size(), entry.getKey());
		}

		return result;
	}

	final protected void beforeTranslateRows(List<M> rows, List<Error> errores) {
		for (val row : rows) {
			switch (row.getEstado()) {
			case ESTRUCTURA_VALIDA:
			case CORREGIDO:
				val success = beforeTranslateRow(row, errores);

				if (!success) {
					row.setEstado(ERROR_ENRIQUECIMIENTO);
				}
				break;
			default:
				break;
			}
		}

		discard(rows);
	}

	final protected void translateRows(List<M> rows, List<Error> errores) {
		for (val row : rows) {
			switch (row.getEstado()) {
			case ESTRUCTURA_VALIDA:
			case CORREGIDO:
				val success = translateRow(row, errores);

				if (success) {
					row.setEstado(HOMOLOGADO);
				} else {
					row.setEstado(ERROR_HOMOLOGACION);
				}
				break;
			default:
				break;
			}
		}

		discard(rows);
	}

	final protected void beforeValidateRows(List<M> rows, List<Error> errores) {
		for (val row : rows) {
			switch (row.getEstado()) {
			case HOMOLOGADO:
				val success = beforeValidateRow(row, errores);

				if (!success) {
					row.setEstado(ERROR_ENRIQUECIMIENTO);
				}
				break;
			default:
				break;
			}
		}

		discard(rows);
	}

	final protected void validateRows(final List<M> rows, final List<Error> errores) {
		for (val row : rows) {
			switch (row.getEstado()) {
			case HOMOLOGADO:
				val success = validateRow(row, errores);

				if (success) {
					row.setEstado(VALIDADO);
				} else {
					row.setEstado(ERROR_VALIDACION);
				}
				break;
			default:
				break;
			}
		}

		discard(rows);
	}

	// -------------------------------------------------------------------------------------
	//
	// -------------------------------------------------------------------------------------
	protected boolean beforeTranslateRow(final M row, List<Error> errores) {
		return true;
	}

	protected boolean translateRow(final M row, final List<Error> errores) {
		return true;
	}

	protected boolean beforeValidateRow(final M row, final List<Error> errores) {
		return true;
	}

	protected boolean validateRow(final M row, final List<Error> errores) {
		return true;
	}
}