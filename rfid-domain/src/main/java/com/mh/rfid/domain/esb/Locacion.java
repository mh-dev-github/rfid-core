package com.mh.rfid.domain.esb;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor

@Entity
@Table(catalog = "esb", name = "Locaciones2")
public class Locacion extends BaseEntity {
	private static final long serialVersionUID = 1L;

	@NotNull
	@Column(length = 50, nullable = false)
	private String name;
	
	@NotNull
	@Column(length = 100, nullable = false)
	private String address;
	
	@NotNull
	@Column(length = 50, nullable = false)
	private String type;
	
	@NotNull
	@Column(length = 300, nullable = false, name = "directorio_salidas")
	private String directorioSalidas;
}
