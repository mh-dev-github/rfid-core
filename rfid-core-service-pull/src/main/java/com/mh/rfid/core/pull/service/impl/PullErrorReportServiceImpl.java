package com.mh.rfid.core.pull.service.impl;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import com.mh.rfid.core.pull.service.api.PullErrorReportService;
import com.mh.rfid.core.reports.service.impl.ErrorReportServiceImpl;

import lombok.val;

public abstract class PullErrorReportServiceImpl extends ErrorReportServiceImpl<Long>
		implements PullErrorReportService {

	// -------------------------------------------------------------------------------------
	// REPORT CODE / SUBJECT
	// -------------------------------------------------------------------------------------
	@Override
	protected String getReportCode() {
		val result = "ERRORES_PULL_" + getIntegracionType().toString();
		return result;
	}

	@Override
	protected String getSubject() {
		val result = "Flujo " + getIntegracionType().toString() + ": Errores ETL";
		return result;
	}

	// -------------------------------------------------------------------------------------
	// DATA
	// -------------------------------------------------------------------------------------
	@Override
	protected MapSqlParameterSource setParameters(Long request) {
		val result = super.setParameters(request);
		result.addValue("secuencia", request);
		return result;
	}
}