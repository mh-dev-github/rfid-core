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
@Table(catalog = "esb", name = "Remisiones")
public class Remision extends BaseEntity {
	
	private static final long serialVersionUID = 1L;

	@NotNull
	@Column(length = 50, nullable = false)
	private String orderNumber;
	
	@NotNull
	@Column(length = 10, nullable = false)
	private String remissionDate;

	@NotNull
	@Column(length = 6, nullable = false)
	private String destinationCode;

	@NotNull
	@Column(length = 50, nullable = false)
	private String destinationId;
}