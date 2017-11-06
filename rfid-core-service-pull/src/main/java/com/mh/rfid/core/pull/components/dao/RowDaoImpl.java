package com.mh.rfid.core.pull.components.dao;

import static com.mh.rfid.enums.EstadoRowType.CORREGIDO;

import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.mh.rfid.domain.stage.Error;
import com.mh.rfid.dto.RowDto;
import com.mh.rfid.enums.EstadoRowType;
import com.mh.rfid.repository.stage.ErrorRepository;

import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
abstract public class RowDaoImpl<M extends RowDto> implements RowDao<M> {

	private static final String PARAMETER_NAME_SECUENCIA = "secuencia";

	private static final String SELECT_NEXT_VALUE_FOR_SEQUENCE = "SELECT (NEXT VALUE FOR esb.SequenceIntegraciones)";

	@Autowired
	private ErrorRepository errorRepository;

	// -------------------------------------------------------------------------------------
	// JDC Templates
	// -------------------------------------------------------------------------------------
	abstract protected NamedParameterJdbcTemplate getStageJdbcTemplate();

	// -------------------------------------------------------------------------------------
	// GET
	// -------------------------------------------------------------------------------------
	@Override
	public List<M> getRowsFromStage(long secuencia) {
		log.debug("Consultando registros con secuencia {}", secuencia);

		val sql = getSqlSelectFromStage();

		val parametros = new MapSqlParameterSource();
		parametros.addValue(getParmeterNameSecuencia(), secuencia);

		val result = getStageJdbcTemplate().query(sql, parametros, getRowMapper());

		log.debug("Se encontraron {} registros", result.size());

		return result;
	}

	final protected Long getNextValueForSequence() {
		return getStageJdbcTemplate().queryForObject(getSQLSelectNextValue(), (Map<String, ?>) null, Long.class);
	}

	protected String getSQLSelectNextValue() {
		return SELECT_NEXT_VALUE_FOR_SEQUENCE;
	}

	protected String getParmeterNameSecuencia() {
		return PARAMETER_NAME_SECUENCIA;
	}

	// -------------------------------------------------------------------------------------
	// SAVE EXTRACTED ROWS
	// -------------------------------------------------------------------------------------
	@Override
	public List<Long> saveExtractedRows(List<M> rows, int batchSize) {
		val result = new ArrayList<Long>();

		val batches = getBatchesOfRows(rows, batchSize);

		batches.forEach((sequence, subList) -> {
			batchInsert(subList);
			result.add(sequence);
		});

		return result;
	}

	final protected Map<Long, List<M>> getBatchesOfRows(List<M> rows, int batchSize) {
		val externalIds = rows.stream().map(M::getExternalId).distinct().sorted().collect(Collectors.toList());
		val groups = rows.stream().collect(Collectors.groupingBy(M::getExternalId));

		for (int fromIndex = 0; fromIndex < externalIds.size(); fromIndex += batchSize) {
			int toIndex = getToIndex(fromIndex, externalIds.size(), batchSize);
			val subList = externalIds.subList(fromIndex, toIndex);

			Long sequence = getNextValueForSequence();
			subList.forEach(externalId -> {
				groups.get(externalId).forEach(row -> row.setSecuencia(sequence));
			});
		}

		val result = rows.stream().collect(Collectors.groupingBy(M::getSecuencia));
		return result;
	}

	final protected int getToIndex(int fromIndex, int size, int batchSize) {
		return (fromIndex + batchSize) > size ? size : fromIndex + batchSize;
	}

	// -------------------------------------------------------------------------------------
	// SAVE TRANSFORMED ROWS
	// -------------------------------------------------------------------------------------
	@Override
	public void saveTransformedRows(List<M> rows, List<Error> errores) {
		// TODO try catch DAO
		if (!errores.isEmpty()) {
			log.debug("save errors");
			errorRepository.save(errores);
		}

		val now = LocalDateTime.now();
		rows.forEach(a -> {
			a.setFechaTransformacion(now);
		});

		batchUpdateTransformedRows(rows);
	}

	// -------------------------------------------------------------------------------------
	// SAVE LOADED ROWS
	// -------------------------------------------------------------------------------------
	@Override
	public void saveLoadedRows(List<M> rows, List<Error> errores) {
		// TODO try catch DAO
		if (!errores.isEmpty()) {
			log.debug("save errors");
			errorRepository.save(errores);
		}

		val toDelete = rows.stream().filter(a -> a.getEstado() == EstadoRowType.CARGADO).collect(Collectors.toList());
		val toUpdate = rows.stream().filter(a -> a.getEstado() != EstadoRowType.CARGADO).collect(Collectors.toList());

		val now = LocalDateTime.now();
		toUpdate.forEach(a -> {
			a.setFechaCargue(now);
		});

		batchDelete(toDelete);
		batchUpdateLoadedRows(toUpdate);
	}

	// -------------------------------------------------------------------------------------
	//
	// -------------------------------------------------------------------------------------
	@Override
	public List<Long> getSequencesWithFixedRows() {
		log.debug("Consultando secuencias con registros corregidos");

		val sql = getSqlSelectSequencesWithFixedRowsFromStage();

		val parametros = new MapSqlParameterSource();
		parametros.addValue("estado", CORREGIDO.toString());

		val result = getStageJdbcTemplate().queryForList(sql, parametros, Long.class);

		log.debug("Se encontraron {} secuencias", result.size());

		return result;
	}

	// -------------------------------------------------------------------------------------
	// protected methods
	// -------------------------------------------------------------------------------------
	final protected void batchInsert(List<M> rows) {
		val sql = getSqlInsertIntoStage();
		val beans = rows.toArray();
		val params = beansToParams(beans);
		getStageJdbcTemplate().batchUpdate(sql, params);
	}

	final protected void batchUpdateTransformedRows(List<M> rows) {
		val sql = getSqlUpdateTransformedRows();
		val beans = rows.toArray();
		val params = beansToParams(beans);
		getStageJdbcTemplate().batchUpdate(sql, params);
	}

	final protected void batchUpdateLoadedRows(List<M> rows) {
		val sql = getSqlUpdateLoadedRows();
		val beans = rows.toArray();
		val params = beansToParams(beans);
		getStageJdbcTemplate().batchUpdate(sql, params);
	}

	final protected void batchDelete(List<M> rows) {
		String sql = getSqlDeleteFromStage();
		val beans = rows.toArray();
		val params = beansToParams(beans);
		getStageJdbcTemplate().batchUpdate(sql, params);
	}

	final protected BeanPropertySqlParameterSource[] beansToParams(Object[] beans) {
		val result = new BeanPropertySqlParameterSource[beans.length];
		for (int i = 0; i < beans.length; i++) {
			result[i] = new BeanPropertySqlParameterSource(beans[i]);
			registerSqlTypes(result[i]);
		}
		return result;
	}

	// -------------------------------------------------------------------------------------
	// extendable methods
	// -------------------------------------------------------------------------------------}
	protected void registerSqlTypes(BeanPropertySqlParameterSource param) {
		param.registerSqlType("operacion", Types.VARCHAR);
		param.registerSqlType("estado", Types.VARCHAR);
	}

	abstract protected RowMapper<M> getRowMapper();

	abstract protected String getSqlSelectFromStage();

	abstract protected String getSqlInsertIntoStage();

	abstract protected String getSqlUpdateTransformedRows();

	abstract protected String getSqlUpdateLoadedRows();

	abstract protected String getSqlDeleteFromStage();
	
	abstract protected String getSqlSelectSequencesWithFixedRowsFromStage();
}