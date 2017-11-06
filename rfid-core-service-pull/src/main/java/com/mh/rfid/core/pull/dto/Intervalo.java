package com.mh.rfid.core.pull.dto;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import com.mh.rfid.domain.esb.Integracion;

import lombok.Data;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class Intervalo {
	private LocalDateTime fechaDesde;

	private LocalDateTime fechaHasta;

	private Integer batchSize;

	public Integer getBatchSize() {
		if (batchSize == null) {
			batchSize = 1000;
		}
		return batchSize;
	}

	public boolean ignorar() {
		boolean result = false;

		result |= (getFechaDesde() == null) || (getFechaHasta() == null);

		if (!result) {
			result |= (getFechaHasta().isBefore(getFechaDesde()) || getFechaHasta().isEqual(getFechaDesde()));
		}

		return result;
	}

	public static Intervalo getIntervaloIntegracion(Integracion integracion) {
		val intervalo = new Intervalo();
		intervalo.setFechaDesde(integracion.getFechaUltimoPull());
		intervalo.setFechaHasta(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
		intervalo.setBatchSize(integracion.getBatchSize());

		log.debug("Intervalo:{}", intervalo.toString());
		return intervalo;
	}
}
