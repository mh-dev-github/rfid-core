package com.mh.rfid.core.reports.service.api;

import org.springframework.transaction.annotation.Transactional;

import com.mh.rfid.dto.ReportDto;

public interface ReportService<T, D> {

	@Transactional
	ReportDto getReport(T request);
}