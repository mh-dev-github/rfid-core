package com.mh.rfid.core.push.dto;

import java.io.Serializable;
import java.util.List;

import com.mh.rfid.dto.MessageDto;
import com.mh.rfid.dto.RemisionMessageDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetResponseType<M extends MessageDto> implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private List<RemisionMessageDto> items;
}
