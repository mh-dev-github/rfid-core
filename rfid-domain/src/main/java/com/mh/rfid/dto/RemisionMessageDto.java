package com.mh.rfid.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mh.rfid.dto.MessageDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class RemisionMessageDto extends MessageDto {

	private static final long serialVersionUID = 1L;

	@JsonProperty("remission_number")
	@Override
	public String getExternalId() {
		return super.getExternalId();
	}
	
	@JsonIgnore
	@Override
	public String getId() {
		return super.getExternalId();
	}
	
	@JsonProperty("order_number")
	private String orderNumber;

	@JsonProperty("date")
	private String remissionDate;

	@JsonProperty("destination_id")
	private String destinationId;



	@Builder
	private RemisionMessageDto(String externalId, String id, String orderNumber, String remissionDate,
			String destinationId) {
		super(externalId, id);
		this.orderNumber = orderNumber;
		this.remissionDate = remissionDate;
		this.destinationId = destinationId;
	}
}