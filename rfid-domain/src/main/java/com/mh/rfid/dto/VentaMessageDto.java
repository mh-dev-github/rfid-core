package com.mh.rfid.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mh.rfid.dto.MessageDto;

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
@AllArgsConstructor
public class VentaMessageDto extends MessageDto {

	private static final long serialVersionUID = 1L;

	@JsonProperty("ticket_number")
	@Override
	public String getExternalId() {
		return super.getExternalId();
	}

	@JsonIgnore
	@Override
	public String getId() {
		return super.getExternalId();
	}
	
	@JsonProperty("store_code")
	private String storeCode;

	@JsonProperty("sale_date")
	private String saleDate;

	@JsonProperty("lines")
	private List<Linea> lineas = new ArrayList<>();

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Linea {
		@JsonProperty("barcode")
		private String barCode;
		
		@JsonProperty("quantity")
		private int quantity;
	}

	@Builder
	private VentaMessageDto(String externalId, String id, String storeCode, String saleDate, List<Linea> lineas) {
		super(externalId, id);
		this.storeCode = storeCode;
		this.saleDate = saleDate;
		this.lineas = lineas;
	}
}