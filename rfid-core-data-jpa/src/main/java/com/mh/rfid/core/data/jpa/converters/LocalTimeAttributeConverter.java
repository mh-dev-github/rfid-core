package com.mh.rfid.core.data.jpa.converters;

import java.sql.Time;
import java.time.LocalTime;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class LocalTimeAttributeConverter implements AttributeConverter<LocalTime, Time> {

	@Override
	public Time convertToDatabaseColumn(LocalTime locTime) {
		return (locTime == null ? null : Time.valueOf(locTime));
	}

	@Override
	public LocalTime convertToEntityAttribute(Time sqlDate) {
		return (sqlDate == null ? null : sqlDate.toLocalTime());
	}
}