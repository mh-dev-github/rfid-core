package com.mh.rfid.core.reports.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFTable;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTable;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableColumn;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableColumns;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableStyleInfo;
import org.springframework.core.io.ClassPathResource;

import com.mh.rfid.core.reports.service.api.ReportService;
import com.mh.rfid.dto.ReportDto;

import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class ReportServiceImpl<T, D> implements ReportService<T, D> {

	// -------------------------------------------------------------------------------------
	// REPORT
	// -------------------------------------------------------------------------------------
	@Override
	final public ReportDto getReport(T request) {
		val reportCode = getReportCode();

		log.debug("Inicio operación generación de reporte {}", reportCode);

		val subject = getSubject();

		val data = getReportData(request);

		val content = getReportContent(reportCode, data);

		val attachments = getAttachments(reportCode, data);

		val result = new ReportDto(reportCode, subject, content, attachments);

		log.debug("Reporte generado");

		return result;
	}

	// -------------------------------------------------------------------------------------
	// REPORT CODE / SUBJECT
	// -------------------------------------------------------------------------------------
	abstract protected String getReportCode();

	abstract protected String getSubject();

	// -------------------------------------------------------------------------------------
	// DATA
	// -------------------------------------------------------------------------------------
	abstract protected D getReportData(T request);

	// -------------------------------------------------------------------------------------
	// CONTENT
	// -------------------------------------------------------------------------------------
	abstract protected String getReportContent(String reportCode, D data);

	protected String getTemplate(String path) {
		try {
			val result = new String(Files.readAllBytes((new ClassPathResource(path).getFile()).toPath()));
			return result;
		} catch (IOException e) {
			throw new RuntimeException("Error al acceder al recurso:" + path, e);
		}
	}

	// -------------------------------------------------------------------------------------
	// ATTACHMENTS
	// -------------------------------------------------------------------------------------
	protected File[] getAttachments(String reportCode, D data) {
		return new File[0];
	}

	protected XSSFWorkbook createWorkbook(List<List<String>> data) {
		XSSFWorkbook workbook = new XSSFWorkbook();

		XSSFSheet sheet = workbook.createSheet();
		populateSheet(sheet, data);
		formatSheet(sheet, data);

		return workbook;
	}

	protected void populateSheet(XSSFSheet sheet, List<List<String>> data) {
		int rowNum = 0;
		for (val record : data) {
			Row row = sheet.createRow(rowNum++);

			int colNum = 0;
			for (val field : record) {
				Cell cell = row.createCell(colNum++);
				cell.setCellValue(field);
			}
		}
	}

	protected void formatSheet(XSSFSheet sheet, List<List<String>> data) {
		XSSFTable my_table = sheet.createTable();
		CTTable cttable = my_table.getCTTable();
		CTTableStyleInfo table_style = cttable.addNewTableStyleInfo();
		table_style.setName("TableStyleMedium2");

		/* Define the data range including headers */
		val row = data.size();
		val col = data.get(0).size();
		val ref1 = new CellReference(0, 0);
		val ref2 = new CellReference(row - 1, col - 1);
		AreaReference range = new AreaReference(ref1, ref2, SpreadsheetVersion.EXCEL2007);
		/* Set Range to the Table */
		cttable.setRef(range.formatAsString());
		/* this is the display name of the table */
		cttable.setDisplayName("ERRORES");
		/* This maps to "displayName" attribute in &lt;table&gt;, OOXML */
		cttable.setName("ERRORES");
		// id attribute against table as long value
		cttable.setId(1L);

		CTTableColumns columns = cttable.addNewTableColumns();
		columns.setCount(col);
		/* Define Header Information for the Table */
		for (int i = 0; i < col; i++) {
			CTTableColumn column = columns.addNewTableColumn();
			column.setName("Column" + i);
			column.setId(i + 1);
			sheet.autoSizeColumn(i);
		}
	}

	protected File writeWorkbook(String filename, XSSFWorkbook workbook) {
		File file;
		try {
			file = File.createTempFile(filename, ".xlsx");
			try (FileOutputStream outputStream = new FileOutputStream(file)) {
				workbook.write(outputStream);
				workbook.close();
			}
			return file;
		} catch (IOException e) {
			log.error("Ocurrio un error al intentar crear el archivo adjunto " + filename, e);
			throw new RuntimeException(e);
		}
	}

	// -------------------------------------------------------------------------------------
	// FORMATTERS
	// -------------------------------------------------------------------------------------
	protected static final DateTimeFormatter formatterDate = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	protected static final DateTimeFormatter formatterDateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

	protected DateTimeFormatter getFormatterDate() {
		return formatterDate;
	}

	protected DateTimeFormatter getFormatterDateTime() {
		return formatterDateTime;
	}
}