package com.mh.rfid.core.push.service.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import com.mh.rfid.core.push.service.api.PushErrorReportService;
import com.mh.rfid.core.reports.service.impl.ErrorReportServiceImpl;

import lombok.val;

public abstract class PushErrorReportServiceImpl extends ErrorReportServiceImpl<Long>
		implements PushErrorReportService {

	// -------------------------------------------------------------------------------------
	// REPORT CODE / SUBJECT
	// -------------------------------------------------------------------------------------
	@Override
	protected String getReportCode() {
		val result = "ERRORES_PUSH_" + getIntegracionType().toString();
		return result;
	}

	@Override
	protected String getSubject() {
		val result = "Flujo " + getIntegracionType().toString() + ": Errores integración REST SERVICES";
		return result;
	}

	// -------------------------------------------------------------------------------------
	// DATA
	// -------------------------------------------------------------------------------------
	@Override
	protected List<String> getHeaders() {
		// @formatter:off
		String[] headers = { 
				"MID", 
				"integracion",
				"External ID",
				"ID",
				"Operación", 
				"Estado Mensaje",
				"Intentos",
				"Datos",
				
				"Intento",
				"Estado Intento",
				"Código Error", 
				"Texto Error",
				"Excepción",
				"Fecha/Hora",
				
				};
		// @formatter:on

		val result = Arrays.asList(headers);
		return result;
	}

	@Override
	protected String getSqlSelect() {
		// @formatter:off
		val result = "" 
			    + " SELECT  "
			    + "      a.mid  "
			    + "     ,a.integracion  "
			    + "     ,a.externalId  "
			    + "     ,a.id  "
			    + "     ,a.operacion  "
			    + "     ,a.estado AS estado_mensaje  "
			    + "     ,a.intentos  "
			    + "     ,a.datos  "
			    + "   "
			    + "     ,b.intento  "
			    + "     ,b.estado  "
			    + "     ,b.codigo  "
			    + "     ,b.texto  "
			    + "     ,b.exception  "
			    + "     ,b.fechaCreacion  "
			    + " FROM msg.Mensajes a  "
			    + " INNER JOIN msg.LogMensajes b ON  "
			    + "     b.mid = a.mid  "
			    + " WHERE  "
			    + "     a.mid = :mid  "
			    + " ORDER BY  "
			    + "     b.id  "
			    + "   ";
		// @formatter:on

		return result;
	}
	
	@Override
	protected MapSqlParameterSource setParameters(Long request) {
		val result = super.setParameters(request);
		result.addValue("mid", request);
		return result;
	}

	@Override
	protected RowMapper<List<String>> getRowMapper() {
		// @formatter:off
		return (rs, rowNum) -> {
			val result = new ArrayList<String>();
			Timestamp fecha;

			result.add(rs.getString("mid"));
			result.add(rs.getString("integracion"));
			result.add(rs.getString("externalId"));
			result.add(rs.getString("id"));
			result.add(rs.getString("operacion"));
			result.add(rs.getString("estado_mensaje"));
			result.add(rs.getString("intentos"));
			result.add(rs.getString("datos"));

			result.add(rs.getString("intento"));
			result.add(rs.getString("estado"));
			result.add(rs.getString("codigo"));
			result.add(rs.getString("texto"));
			result.add(rs.getString("exception"));
			
			fecha = rs.getTimestamp("fechaCreacion");
			result.add(getFormatterDateTime().format(fecha.toLocalDateTime()));
			
			return result;
		};
		// @formatter:on		
	}
}