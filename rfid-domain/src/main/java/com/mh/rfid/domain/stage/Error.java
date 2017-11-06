package com.mh.rfid.domain.stage;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

import com.mh.rfid.dto.RowDto;
import com.mh.rfid.enums.IntegracionType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.val;

@Entity
@Table(catalog = "stage", name = "Errores")

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Builder
public class Error implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long idError;

	private long id;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(length = 50, nullable = false)
	private IntegracionType integracion;

	@NotNull
	@Size(max = 50)
	@Column(length = 50, nullable = false)
	private String externalId;

	@NotNull
	@Size(max = 50)
	@Column(length = 50, nullable = false)
	private String codigo;

	@NotNull
	@Size(max = 1024)
	@Column(length = 1024, nullable = false)
	private String mensaje;
	
	@CreatedDate
	@DateTimeFormat(style = "M-")
	@Column(updatable = false)
	private LocalDateTime fechaCreacion;

	public static Error error(IntegracionType integracion, RowDto row, String codigo, String mensaje) {
		// @formatter:off
 		val result = Error
				.builder()
				.id(row.getId())
				.integracion(integracion)
				.externalId(row.getExternalId())
				.codigo(codigo)
				.mensaje(mensaje)
				.build();
		// @formatter:on
		return result;
	}
}