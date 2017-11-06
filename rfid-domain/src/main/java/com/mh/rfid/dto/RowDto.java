package com.mh.rfid.dto;

import static com.mh.rfid.enums.EstadoRowType.DESCARTADO;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.mh.rfid.enums.EstadoRowType;
import com.mh.rfid.enums.OperacionType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.val;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class RowDto implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long id;

	private Long secuencia;

	@NotNull
	@Size(max = 50)
	private String externalId;

	@NotNull
	private OperacionType operacion;

	@NotNull
	private EstadoRowType estado;

	@NotNull
	private LocalDateTime fechaUltimoCambioEnOrigen;

	@NotNull
	private LocalDateTime fechaExtraccion;

	private LocalDateTime fechaTransformacion;
	
	private LocalDateTime fechaCargue;

	static public <M extends RowDto> void discard(List<M> list) {
		// @formatter:off
		val conErrores = list
				.stream()
				.filter(row -> hasErrors(row))
				.map(RowDto::getExternalId)
				.distinct()
				.collect(Collectors.toList());
		// @formatter:on

		// @formatter:off
		val sinErrores = list
				.stream()
				.filter(row -> !hasErrors(row))
				.collect(Collectors.toList());
		// @formatter:on

		sinErrores.forEach(row -> {
			if (conErrores.stream().anyMatch(a -> a.equals(row.getExternalId()))) {
				row.setEstado(DESCARTADO);
			}
		});
	}

	static protected boolean hasErrors(RowDto M) {
		val estado = M.getEstado();
		switch (estado) {
		case ERROR_ENRIQUECIMIENTO:
		case ERROR_HOMOLOGACION:
		case ERROR_VALIDACION:
			return true;
		default:
			return false;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RowDto other = (RowDto) obj;
		if (id != other.id)
			return false;
		return true;
	}
}