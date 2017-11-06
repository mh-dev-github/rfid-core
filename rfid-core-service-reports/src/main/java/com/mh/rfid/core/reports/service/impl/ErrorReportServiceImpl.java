package com.mh.rfid.core.reports.service.impl;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.text.StrSubstitutor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.mh.rfid.enums.IntegracionType;

import lombok.val;

public abstract class ErrorReportServiceImpl<T> extends ReportServiceImpl<T, List<List<String>>> {

	// -------------------------------------------------------------------------------------
	// JDC Templates
	// -------------------------------------------------------------------------------------
	abstract protected NamedParameterJdbcTemplate getJdbcTemplate();

	// -------------------------------------------------------------------------------------
	// IntegracionType
	// -------------------------------------------------------------------------------------
	abstract public IntegracionType getIntegracionType();

	// -------------------------------------------------------------------------------------
	// DATA
	// -------------------------------------------------------------------------------------
	@Override
	protected List<List<String>> getReportData(T request) {
		val result = new ArrayList<List<String>>();

		val headers = getHeaders();

		val sql = getSqlSelect();
		val parametros = setParameters(request);
		val rows = getJdbcTemplate().query(sql, parametros, getRowMapper());

		result.add(headers);
		result.addAll(rows);

		return result;
	}

	protected MapSqlParameterSource setParameters(T request) {
		val result = new MapSqlParameterSource();
		return result;
	}

	abstract protected List<String> getHeaders();

	abstract protected String getSqlSelect();

	abstract protected RowMapper<List<String>> getRowMapper();

	// -------------------------------------------------------------------------------------
	// CONTENT
	// -------------------------------------------------------------------------------------
	@Override
	protected String getReportContent(String reportCode, List<List<String>> data) {
		val valueMap = new HashMap<String, String>();
		valueMap.put("integracion", getIntegracionType().toString());
		valueMap.put("numero_errores", String.valueOf(data.size() - 1));

		String pathContent = "templates\\alert.html";
		String template = getTemplate(pathContent);
		val result = StrSubstitutor.replace(template, valueMap);

		return result;
	}

	// -------------------------------------------------------------------------------------
	// ATTACHMENTS
	// -------------------------------------------------------------------------------------
	@Override
	protected File[] getAttachments(String reportCode, List<List<String>> data) {
		XSSFWorkbook workbook = createWorkbook(data);

		val filename = getFileName();
		val file = writeWorkbook(filename, workbook);

		val result = new File[] { file };
		return result;
	}

	protected String getFileName() {
		val now = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").format(LocalDateTime.now());
		val result = getReportCode() + "-" + now + "-";
		return result;
	}
}
