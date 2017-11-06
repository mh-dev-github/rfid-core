package com.mh.rfid.domain.msg;

import static com.mh.rfid.enums.EstadoMensajeType.REINTENTAR_ENVIO;
import static com.mh.rfid.enums.EstadoMensajeType.ERROR_DURANTE_ENVIO;

import static com.mh.rfid.enums.EstadoMensajeType.PENDIENTE_VERIFICAR;
import static com.mh.rfid.enums.EstadoMensajeType.REINTENTAR_VERIFICACION;
import static com.mh.rfid.enums.EstadoMensajeType.ERROR_DURANTE_VERIFICACION;
import static com.mh.rfid.enums.EstadoMensajeType.INTEGRADO;
import static com.mh.rfid.enums.EstadoMensajeType.INCONSISTENTE;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

import com.mh.rfid.enums.EstadoMensajeType;
import com.mh.rfid.enums.IntegracionType;
import com.mh.rfid.enums.OperacionType;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.val;

@Entity
@Table(catalog = "msg", name = "Mensajes")

@Getter
@Setter(AccessLevel.PROTECTED)
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Builder
public class Mensaje implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long mid;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(length = 50, nullable = false, updatable = false)
	private IntegracionType integracion;

	@NotNull
	@Size(max = 50)
	@Column(length = 50, nullable = false, updatable = false)
	private String externalId;

	@NotNull
	@Size(max = 50)
	@Column(length = 50, nullable = false)
	private String id;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(length = 50, nullable = false)
	private OperacionType operacion;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(length = 50, nullable = false)
	private EstadoMensajeType estado;

	private int intentos;

	@NotNull
	@DateTimeFormat(style = "M-")
	private LocalDateTime fechaUltimoIntento;

	@Lob
	@NotNull
	@Column(nullable = false, updatable = false)
	private String datos;

	@CreatedDate
	@DateTimeFormat(style = "M-")
	@Column(updatable = false)
	private LocalDateTime fechaCreacion;

	@LastModifiedDate
	@DateTimeFormat(style = "M-")
	private LocalDateTime fechaModificacion;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "mid")
	private List<LogMensaje> logs;

	public static class MensajeBuilder {
		private List<LogMensaje> logs = new ArrayList<>();
	}

	public void addLogs(LogMensaje logMensaje) {
		getLogs().add(logMensaje);
		logMensaje.setMensaje(this);
	}

	// -------------------------------------------------------------------------------------
	//
	// -------------------------------------------------------------------------------------
	public void errorDuranteEnvio(int numeroMaximoReintentos, RuntimeException e) {
		val estado = (getIntentos() < numeroMaximoReintentos) ? REINTENTAR_ENVIO : ERROR_DURANTE_ENVIO;

		intentoConError(e, estado);
	}

	public void enviado(String id, int codigo) {
		val estado = PENDIENTE_VERIFICAR;

		intentoSinError(id, codigo, estado);
		setIntentos(0);
	}

	public void integradoSinVerificar(String id, int codigo) {
		val estado = INTEGRADO;

		intentoSinError(id, codigo, estado);
	}

	public void errorDuranteVerificacion(int numeroMaximoReintentos, RuntimeException e) {
		val estado = (getIntentos() < numeroMaximoReintentos) ? REINTENTAR_VERIFICACION
				: ERROR_DURANTE_VERIFICACION;

		intentoConError(e, estado);
	}

	public void integradoConVerificacion(String id, int codigo) {
		val estado = INTEGRADO;

		intentoSinError(id, codigo, estado);
	}

	public void inconsistente(int codigo, RuntimeException e) {
		val estado = INCONSISTENTE;

		intentoConError(e, estado);
	}

	// -------------------------------------------------------------------------------------
	// excepciones
	// -------------------------------------------------------------------------------------

	private LogMensaje intentoConError(RuntimeException e, EstadoMensajeType estado) {
		setEstado(estado);
		incrementarIntentos();

		val result = onException(e);
		addLogs(result);

		return result;
	}

	protected LogMensaje intentoSinError(String id, int codigo, EstadoMensajeType estado) {
		if (OperacionType.C.equals(getOperacion())) {
			setId(id);
		}

		setEstado(estado);
		incrementarIntentos();

		val result = LogMensaje.create(getIntentos(), getEstado(), codigo);
		addLogs(result);
		
		return result;
	}

	protected void incrementarIntentos() {
		val now = LocalDateTime.now();
		setIntentos(getIntentos() + 1);
		setFechaUltimoIntento(now);
	}

	// -------------------------------------------------------------------------------------
	// excepciones
	// -------------------------------------------------------------------------------------
	protected LogMensaje onException(Exception e) {
		LogMensaje result;

		if (e instanceof RestClientResponseException) {
			result = onRestClientResponseException((RestClientResponseException) e);
		} else {
			if (e instanceof ResourceAccessException) {
				result = onResourceAccessException((ResourceAccessException) e);
			} else {
				if (e instanceof RuntimeException) {
					result = onRuntimeException((RuntimeException) e);
				} else {
					int codigo = -1;
					String texto = e.getMessage();
					result = LogMensaje.create(getIntentos(), getEstado(), codigo, texto, e);
				}
			}
		}

		return result;
	}

	protected LogMensaje onRestClientResponseException(RestClientResponseException e) {
		LogMensaje result;

		try {
			JSONTokener tokener = new JSONTokener(e.getResponseBodyAsString());
			JSONObject jsonObject = new JSONObject(tokener);
			if (jsonObject.has("error")) {
				jsonObject = jsonObject.getJSONObject("error");

				int codigo = jsonObject.getInt("code");
				String texto = jsonObject.getString("message");
				try {
					val s = texto.split("-");
					codigo = Integer.parseInt(s[0]);
				} catch (NumberFormatException nfe) {
					;
				}

				result = LogMensaje.create(getIntentos(), getEstado(), codigo, texto, e);
			} else {
				result = onRuntimeException(e);
			}
		} catch (JSONException je) {
			result = onRuntimeException(e);
		}

		return result;
	}

	protected LogMensaje onResourceAccessException(ResourceAccessException e) {
		val result = onRuntimeException(e);
		return result;
	}

	protected LogMensaje onRuntimeException(RuntimeException e) {
		int codigo = -1;
		String texto = e.getMessage();

		val result = LogMensaje.create(getIntentos(), getEstado(), codigo, texto, e);
		return result;
	}
}