package com.mh.rfid.core.pull.service.impl;

import static com.mh.rfid.domain.esb.BaseEntity.mapRowToEntity;
import static com.mh.rfid.dto.RowDto.discard;
import static com.mh.rfid.enums.EstadoRowType.ERROR_VALIDACION;
import static com.mh.rfid.enums.EstadoRowType.VALIDADO;
import static com.mh.rfid.enums.EstadoSincronizacionType.CARGADO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mh.rfid.core.pull.components.dao.RowDao;
import com.mh.rfid.core.pull.service.api.LoadService;
import com.mh.rfid.core.pull.service.impl.exceptions.EntityDuplicatedLoadException;
import com.mh.rfid.core.pull.service.impl.exceptions.EntityNotFoundLoadException;
import com.mh.rfid.domain.esb.BaseEntity;
import com.mh.rfid.domain.stage.Error;
import com.mh.rfid.dto.RowDto;
import com.mh.rfid.enums.EstadoRowType;
import com.mh.rfid.enums.EstadoSincronizacionType;
import com.mh.rfid.enums.IntegracionType;
import com.mh.rfid.enums.OperacionType;

import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class LoadServiceImpl<M extends RowDto, E extends BaseEntity> implements LoadService<M, E> {

	// -------------------------------------------------------------------------------------
	// IntegracionType
	// -------------------------------------------------------------------------------------
	abstract public IntegracionType getIntegracionType();

	// -------------------------------------------------------------------------------------
	// DAO
	// -------------------------------------------------------------------------------------
	abstract protected RowDao<M> getRowDao();

	abstract protected JpaRepository<E, String> getRepository();

	// -------------------------------------------------------------------------------------
	// LOAD
	// -------------------------------------------------------------------------------------
	@Override
	final public List<M> load(long sequence) {
		val errores = new ArrayList<Error>();

		log.debug("Inicio del cargue de los registros de la secuencia {}\", sequence ");

		log.debug("getRows({})", sequence);
		val rows = getRows(sequence);

		log.debug("mapRows");
		val map = mapRowsToEntities(rows, errores);

		log.debug("validateEntities");
		validateEntities(map, errores);

		log.debug("saveEntities");
		saveEntities(map, errores);

		log.debug("saveLoadedRows");
		getRowDao().saveLoadedRows(rows, errores);

		log.debug("Fin del cargue de los registros de la secuencia {}", sequence);

		return rows;
	}

	// -------------------------------------------------------------------------------------
	// GET ROWS
	// -------------------------------------------------------------------------------------
	final protected List<M> getRows(long sequence) {
		log.debug("getRowDao().getRows({})", sequence);

		// @formatter:off
		val result = getRowDao()
				.getRowsFromStage(sequence)
				.stream()
				.filter(row -> row.getEstado().equals(VALIDADO))
				.collect(Collectors.toList());
		// @formatter:on

		return result;
	}

	// -------------------------------------------------------------------------------------
	// MAP ROWS TO ENTITIES
	// -------------------------------------------------------------------------------------
	final protected Map<E, List<M>> mapRowsToEntities(List<M> rows, List<Error> errores) {
		val result = new HashMap<E, List<M>>();

		val groups = rows.stream().collect(Collectors.groupingBy(M::getExternalId));

		for (val values : groups.values()) {
			E entity = mapRowsToEntity(values, errores);

			if (entity != null) {
				result.put(entity, values);
			}
		}

		return result;
	}

	protected E mapRowsToEntity(List<M> rows, List<Error> errores) {
		E entity = null;

		try {
			val row = rows.get(0);
			entity = getEntity(row);

			if (row.getOperacion() == OperacionType.U) {
				if (entity.getEstado() != EstadoSincronizacionType.INTEGRADO) {
					enqueue(entity, rows);
					entity = null;
					return entity;
				}
			}

			mapEntity(rows, entity);
		} catch (EntityDuplicatedLoadException e) {
			errores.add(error(rows, e.getCodigo(), e.getMessage()));
		} catch (EntityNotFoundLoadException e) {
			errores.add(error(rows, e.getCodigo(), e.getMessage()));
		}

		return entity;
	}

	protected void mapEntity(List<M> rows, E entity) {
		val row = rows.get(0);
		mapRowToEntity(row, entity);
	};

	protected E getEntity(M row) {
		E entity = getRepository().findOne(row.getExternalId());

		switch (row.getOperacion()) {
		case C:
			if (entity != null) {
				throw new EntityDuplicatedLoadException(getIntegracionType(), entity);
			}

			entity = getNewEntity();
			break;
		case U:
			if (entity == null) {
				throw new EntityNotFoundLoadException(getIntegracionType(), row.getExternalId());
			}

			break;
		}

		return entity;
	}

	abstract protected E getNewEntity();

	// -------------------------------------------------------------------------------------
	// VALIDATE ENTITIES
	// -------------------------------------------------------------------------------------
	final protected void validateEntities(Map<E, List<M>> map, List<Error> errores) {
		for (val entry : map.entrySet()) {
			val entity = entry.getKey();
			val rows = entry.getValue();
			val row = rows.get(0);
			val success = validateEntity(entity, row.getId(), errores);

			if (!success) {
				entity.entidadNoCargada();
				row.setEstado(ERROR_VALIDACION);
				discard(rows);
			}
		}
	}

	protected boolean validateEntity(E entity, Long rowId, List<Error> errores) {
		return true;
	}

	// -------------------------------------------------------------------------------------
	//
	// -------------------------------------------------------------------------------------
	final protected void saveEntities(Map<E, List<M>> map, List<Error> errores) {
		// @formatter:off
		val entrySet = map.entrySet()
				.stream()
				.filter(e -> e.getKey().getEstado().equals(CARGADO))
				.collect(Collectors.toList());
		// @formatter:on

		for (val entry : entrySet) {
			val entity = entry.getKey();
			val rows = entry.getValue();

			try {
				getRepository().save(entity);

				rows.forEach(row -> {
					row.setEstado(EstadoRowType.CARGADO);
				});
			} catch (RuntimeException e) {
				entity.entidadNoCargada();
				val codigo = "error_exception_on_load";
				val mensaje = e.getMessage();
				errores.add(error(rows, codigo, mensaje));
			}
		}

		getRepository().flush();
	}

	// -------------------------------------------------------------------------------------
	//
	// -------------------------------------------------------------------------------------
	private void enqueue(E entity, List<M> rows) {
		entity.incrementarSincronizacionesEnCola();
		getRepository().save(entity);

		rows.forEach(row -> {
			row.setEstado(EstadoRowType.EN_ESPERA);
		});
	}

	private Error error(List<M> rows, String codigo, String mensaje) {
		val result = Error.error(getIntegracionType(), rows.get(0), codigo, mensaje);

		rows.forEach(row -> {
			row.setEstado(EstadoRowType.ERROR_CARGUE);
		});

		return result;
	}
}