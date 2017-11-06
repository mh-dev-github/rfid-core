package com.mh.rfid.core.pull.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.mh.rfid.core.pull.components.dao.RowDao;
import com.mh.rfid.core.pull.dto.Intervalo;
import com.mh.rfid.core.pull.service.api.ExtractionService;
import com.mh.rfid.dto.RowDto;
import com.mh.rfid.enums.IntegracionType;
import com.mh.rfid.repository.esb.IntegracionRepository;

import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class ExtractionServiceImpl<M extends RowDto> implements ExtractionService<M> {

	public static final String PARAMETER_NAME_FECHA_DESDE = "fechaDesde";

	public static final String PARAMETER_NAME_FECHA_HASTA = "fechaHasta";

	// -------------------------------------------------------------------------------------
	// IntegracionType
	// -------------------------------------------------------------------------------------
	abstract public IntegracionType getIntegracionType();

	@Autowired
	private IntegracionRepository integracionRepository;

	// -------------------------------------------------------------------------------------
	// JDC Templates
	// -------------------------------------------------------------------------------------
	abstract protected NamedParameterJdbcTemplate getSourceJdbcTemplate();

	// -------------------------------------------------------------------------------------
	// DAO
	// -------------------------------------------------------------------------------------
	abstract protected RowDao<M> getRowDao();

	// -------------------------------------------------------------------------------------
	// extract rows
	// -------------------------------------------------------------------------------------
	@Override
	final public List<Long> extractRows() {
		val result = new ArrayList<Long>();

		log.debug("Inicio de la extracción de registros desde la base de datos origen:{}", getIntegracionType());
		val integracion = integracionRepository.findOneByCodigo(getIntegracionType().toString());
		val intervalo = Intervalo.getIntervaloIntegracion(integracion);

		if (!intervalo.ignorar()) {

			val rows = getRowsFromSource(intervalo.getFechaDesde(), intervalo.getFechaHasta());
			if (rows.isEmpty()) {
				log.debug("No se encontraron registros. Se ha descartado la extracción de registros");
			} else {
				val sequences = getRowDao().saveExtractedRows(rows, intervalo.getBatchSize());

				log.debug("Se generaron {} secuencias de extracción", sequences.size());
				result.addAll(sequences);
			}

			integracion.setFechaUltimoPull(intervalo.getFechaHasta());
			log.debug("Actualizando fecha ultimo pull en registro de integracion {}", getIntegracionType());
			integracionRepository.saveAndFlush(integracion);
			log.debug("Registro de integracion actualizado {}", getIntegracionType());
			log.debug("Fin de la extracción de registros desde la base de datos origen:{}", getIntegracionType());
		}

		return result;
	}

	// -------------------------------------------------------------------------------------
	// Sequences With Fixed Rows
	// -------------------------------------------------------------------------------------
	@Override
	public List<Long> getSequencesWithFixedRows() {
		val result = getRowDao().getSequencesWithFixedRows();
		return result;
	}

	// -------------------------------------------------------------------------------------
	// PULL ROWS FROM SOURCE
	// -------------------------------------------------------------------------------------
	protected List<M> getRowsFromSource(LocalDateTime fechaDesde, LocalDateTime fechaHasta) {
		log.debug("Consultando los registros desde la base de datos desde {} hasta {}", fechaDesde.toString(),
				fechaHasta.toString());

		val sql = getSqlSelectFromSource();

		val parametros = buildSqlParameterSource();
		parametros.addValue(getParameterNameFechaDesde(), fechaDesde);
		parametros.addValue(getParameterNameFechaHasta(), fechaHasta);

		val result = getSourceJdbcTemplate().query(sql, parametros, getRowMapper());

		log.debug("Se encontraron {} registros", result.size());

		return result;
	}

	protected MapSqlParameterSource buildSqlParameterSource() {
		return new MapSqlParameterSource();
	}

	protected String getParameterNameFechaHasta() {
		return PARAMETER_NAME_FECHA_HASTA;
	}

	protected String getParameterNameFechaDesde() {
		return PARAMETER_NAME_FECHA_DESDE;
	}

	abstract protected String getSqlSelectFromSource();

	abstract protected RowMapper<M> getRowMapper();
}