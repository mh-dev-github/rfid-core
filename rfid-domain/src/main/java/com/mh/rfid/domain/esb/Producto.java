package com.mh.rfid.domain.esb;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(catalog = "esb", name = "Productos")
@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@AttributeOverrides({ 
	@AttributeOverride(name = "fechaUltimoPull", column = @Column(name = "fecha_ultimo_pull")),
	@AttributeOverride(name = "fechaUltimoPush", column = @Column(name = "fecha_ultimo_push"))
})
public class Producto extends BaseEntity {

	private static final long serialVersionUID = 1L;

	@NotNull
	@Column(length = 7, nullable = false)
	private String companyPrefix;

	@NotNull
	@Column(length = 50, nullable = false)
	private String name;

	@NotNull
	@Column(length = 6, nullable = false)
	private String reference;

	@NotNull
	@Column(length = 25, nullable = false)
	private String ean;

	@NotNull
	@Column(length = 50, nullable = false)
	private String color;

	@NotNull
	@Column(length = 50, nullable = false)
	private String codigoColor;

	@NotNull
	@Column(length = 50, nullable = false)
	private String talla;

	@NotNull
	@Column(length = 50, nullable = false)
	private String tipoProducto;

	@NotNull
	@Column(length = 50, nullable = false)
	private String coleccion;

	@NotNull
	@Column(length = 50, nullable = false)
	private String grupoProducto;

	@NotNull
	@Column(length = 50, nullable = false)
	private String subGrupoProducto;

	@NotNull
	@Column(length = 50, nullable = false)
	private String fabricante;

	@NotNull
	@Column(length = 50, nullable = false)
	private String temporada;

	@NotNull
	@Column(length = 50, nullable = false)
	private String referencia;

	@NotNull
	@Column(length = 50, nullable = false)
	private String modelo;

	@NotNull
	@Column(length = 50, nullable = false)
	private String genero;
}
