package com.mh.rfid.domain.esb;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor

@Entity
@Table(catalog = "esb", name = "Ventas")
public class Venta extends BaseEntity {
	
	private static final long serialVersionUID = 1L;

	@NotNull
	@Column(length = 6, nullable = false)
	private String storeCode;
	
	@NotNull
	@Column(length = 10, nullable = false)
	private String saleDate;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(catalog = "esb", name = "VentasLineas", joinColumns = @JoinColumn(name = "externalId", nullable = false))
	private List<Linea> lineas = new ArrayList<>();

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Embeddable
	@Builder
	public static class Linea {
		@NotNull
		@Column(length = 13, nullable = false)
		private String barCode;
		
		private int quantity;
	}
}