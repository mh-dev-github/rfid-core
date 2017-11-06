package com.mh.rfid.core.reports.service.impl;

import java.util.List;

import com.mh.rfid.core.reports.service.api.ExceptionReportService;
import com.mh.rfid.enums.IntegracionType;

//@Slf4j
public abstract class ExceptionReportServiceImpl extends ReportServiceImpl<RuntimeException, List<List<String>>>
		implements ExceptionReportService {

	// -------------------------------------------------------------------------------------
	// IntegracionType
	// -------------------------------------------------------------------------------------
	abstract public IntegracionType getIntegracionType();

	// @Override
	// protected String getData(RuntimeException e) {
	// return e.getMessage();
	// }

	@Override
	protected List<List<String>> getReportData(RuntimeException request) {
		// TODO Auto-generated method stub
		return null;
	}
}