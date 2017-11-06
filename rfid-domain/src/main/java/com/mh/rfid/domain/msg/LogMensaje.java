package com.mh.rfid.domain.msg;

import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.left;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

import com.mh.rfid.enums.EstadoMensajeType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.val;

@Entity
@Table(catalog = "msg", name = "LogMensajes")

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Builder
public class LogMensaje implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "mid")
	private Mensaje mensaje;

	@Column(updatable = false)
	private int intento;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(length = 50, nullable = false, updatable = false)
	private EstadoMensajeType estado;

	@Column(updatable = false)
	private int codigo;

	@NotNull
	@Size(max = 1024)
	@Column(length = 1024, nullable = false, updatable = false)
	private String texto;

	@NotNull
	@Size(max = 1024)
	@Column(length = 1024, nullable = false, updatable = false)
	private String exception;

	@CreatedDate
	@DateTimeFormat(style = "M-")
	@Column(updatable = false)
	private LocalDateTime fechaCreacion;

	static public LogMensaje create(int intento, EstadoMensajeType estado, int codigo) {
		return create(intento, estado, codigo, "", null);
	}

	static public LogMensaje create(int intento, EstadoMensajeType estado, int codigo, String texto, Throwable e) {
		texto = defaultString(texto);
		String exception = (e == null ? "" : e.getClass().getName());

		texto = left(texto, 1024);
		exception = left(exception, 1024);

		// @formatter:off
		val result = LogMensaje
				.builder()
				.intento(intento)
				.estado(estado)
				.codigo(codigo)
				.texto(texto)
				.exception(exception)
				.build();
		// @formatter:on

		return result;
	}
}
