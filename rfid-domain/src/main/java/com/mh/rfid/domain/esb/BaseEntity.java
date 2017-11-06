package com.mh.rfid.domain.esb;

import static com.mh.rfid.enums.EstadoSincronizacionType.EN_PROCESO;
import static com.mh.rfid.enums.EstadoSincronizacionType.INTEGRADO;
import static com.mh.rfid.enums.EstadoSincronizacionType.NO_CARGADO;
import static com.mh.rfid.enums.EstadoSincronizacionType.PENDIENTE;
import static com.mh.rfid.enums.EstadoSincronizacionType.ERROR_CONVERSION;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

import com.mh.rfid.dto.RowDto;
import com.mh.rfid.enums.EstadoSincronizacionType;
import com.mh.rfid.enums.OperacionType;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@MappedSuperclass
@Getter
@Setter(AccessLevel.PROTECTED)
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Size(max = 50)
	@Column(length = 50)
	private String externalId;

	@NotNull
	@Size(max = 50)
	@Column(length = 50)
	private String id;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(length = 1)
	private OperacionType operacion;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(length = 50)
	private EstadoSincronizacionType estado;

	private int sincronizacionesEnCola;

	@NotNull
	@DateTimeFormat(style = "M-")
	private LocalDateTime fechaUltimoCambioEnOrigen;

	@NotNull
	@DateTimeFormat(style = "M-")
	private LocalDateTime fechaUltimaExtraccion;

	@NotNull
	@DateTimeFormat(style = "M-")
	private LocalDateTime fechaUltimoCargue;

	@DateTimeFormat(style = "M-")
	private LocalDateTime fechaUltimaIntegracion;

	@CreatedDate
	@DateTimeFormat(style = "M-")
	@Column(updatable = false)
	private LocalDateTime fechaCreacion;

	@LastModifiedDate
	@DateTimeFormat(style = "M-")
	private LocalDateTime fechaModificacion;

	protected void removeAll(final Iterator<?> iter) {
		while (iter.hasNext()) {
			iter.remove();
		}
	}

	// -------------------------------------------------------------------------------------
	//
	// -------------------------------------------------------------------------------------
	public int incrementarSincronizacionesEnCola() {
		int count = getSincronizacionesEnCola() + 1;
		setSincronizacionesEnCola(count);
		return count;
	}

	public void mensajeGenerado() {
		setEstado(PENDIENTE);
	}

	public void mensajeNoGenerado() {
		setEstado(ERROR_CONVERSION);
	}

	// -------------------------------------------------------------------------------------
	//
	// -------------------------------------------------------------------------------------
	public void entidadNoCargada() {
		setEstado(NO_CARGADO);
	}

	// -------------------------------------------------------------------------------------
	//
	// -------------------------------------------------------------------------------------
	public boolean integracionIniciada() {
		boolean result = false;

		switch (getEstado()) {
		case PENDIENTE:
			setEstado(EN_PROCESO);
			result = true;
			break;
		default:
			break;
		}

		return result;
	}

	public void enviada(String id) {
		integracionIniciada();

		if (OperacionType.C.equals(getOperacion())) {
			if (getId().equals("")) {
				setId(id);
			}
		}
	}

	public void integrada(String id) {
		setEstado(INTEGRADO);
		setFechaUltimaIntegracion(LocalDateTime.now());

		if (OperacionType.C.equals(getOperacion())) {
			if (getId().equals("")) {
				setId(id);
			}
		}
	}

	// -------------------------------------------------------------------------------------
	//
	// -------------------------------------------------------------------------------------
	static public void mapRowToEntity(RowDto row, BaseEntity entity) {
		switch (row.getOperacion()) {
		case C:
			entity.setExternalId(row.getExternalId());
			entity.setId("");
			break;
		case U:
			break;
		}
		entity.setOperacion(row.getOperacion());
		entity.setEstado(EstadoSincronizacionType.CARGADO);
		entity.setSincronizacionesEnCola(0);
		entity.setFechaUltimoCambioEnOrigen(row.getFechaUltimoCambioEnOrigen());
		entity.setFechaUltimaExtraccion(row.getFechaExtraccion());
		entity.setFechaUltimoCargue(LocalDateTime.now());
	}

	// -------------------------------------------------------------------------------------
	//
	// -------------------------------------------------------------------------------------
	/**
	 * This `hashCode` implementation is specific for JPA entities and uses a
	 * fixed `int` value to be able to identify the entity in collections after
	 * a new id is assigned to the entity, following the article in
	 * https://vladmihalcea.com/2016/06/06/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
	 * 
	 * @return int
	 */
	@Override
	public int hashCode() {
		return 31;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		if (getExternalId() == null)
			return false;

		BaseEntity other = (BaseEntity) obj;

		return Objects.equals(getExternalId(), other.getExternalId());
	}
}
