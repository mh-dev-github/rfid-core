package com.mh.rfid.domain.esb;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(catalog = "esb", name = "Integraciones")
@Getter
@Setter
@ToString
@NoArgsConstructor
public class Integracion implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id_integracion")
	private int id;

	@Column(name = "codigo", nullable = false, length = 50, updatable = false)
	private String codigo;

	@Column(name = "nombre", nullable = false, length = 100, updatable = false)
	private String nombre;

	@Column(name = "descripcion", nullable = false, length = 200, updatable = false)
	private String descripcion;

	@Column(name = "fecha_ultimo_pull", nullable = false)
	private LocalDateTime fechaUltimoPull;

	@Column(name = "batch_size")
	private Integer batchSize;
}
