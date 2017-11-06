package com.mh.rfid.core.pull.components.amqp;

import java.util.stream.Collectors;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.scheduling.annotation.Scheduled;

import com.mh.rfid.core.pull.service.api.ExtractionService;
import com.mh.rfid.core.pull.service.api.LoadService;
import com.mh.rfid.core.pull.service.api.PullErrorReportService;
import com.mh.rfid.core.pull.service.api.TransformationService;
import com.mh.rfid.domain.esb.BaseEntity;
import com.mh.rfid.dto.RowDto;

import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
abstract public class PullConsumer<M extends RowDto, E extends BaseEntity> {

	abstract protected PullProducer<M> getProducer();

	abstract protected ExtractionService<M> getExtractionService();

	abstract protected TransformationService<M> getTransformationService();

	abstract protected LoadService<M, E> getLoadService();

	abstract protected PullErrorReportService getErrorReportService();

	@Scheduled(cron = "${sync.cron}")
	public void cron() {
		log.debug("sync.cron");

		getProducer().extract();

		val sequences = getExtractionService().getSequencesWithFixedRows();
		getProducer().retryFixedRows(sequences);
	}

	@RabbitListener(queues = "${rfid.amqp.queue-extract}")
	public void recievedMessageExtract(String message) {
		log.debug("Mensaje EXTRACT: " + message);

		try {
			val sequences = getExtractionService().extractRows();

			for (val sequence : sequences) {
				getProducer().successfulExtraction(sequence);
			}
		} catch (RuntimeException e) {
			getProducer().extractionException(e);
			return;
		}

	}

	@RabbitListener(queues = "${rfid.amqp.queue-transform}")
	public void recievedMessageTransform(long sequence) {
		log.debug("Mensaje TRANSFORM: " + sequence);

		try {
			boolean error = false;
			val rows = getTransformationService().transformRows(sequence);

			val groups = rows.stream().collect(Collectors.groupingBy(RowDto::getEstado));
			for (val entry : groups.entrySet()) {
				switch (entry.getKey()) {
				case VALIDADO:
					getProducer().successfulTransformation(sequence);
					break;
				case ERROR_ENRIQUECIMIENTO:
				case ERROR_HOMOLOGACION:
				case ERROR_VALIDACION:
				case DESCARTADO:
					error = true;
					break;
				default:
					break;
				}
			}

			if (error) {
				getProducer().unsuccessfulTransformation(sequence);
			}
		} catch (DeadlockLoserDataAccessException e) {
			getProducer().successfulExtraction(sequence);
		} catch (RuntimeException e) {
			getProducer().transformationException(sequence, e);
			return;
		}
	}

	@RabbitListener(queues = "${rfid.amqp.queue-load}")
	public void recievedMessageLoad(long sequence) {
		log.debug("Mensaje LOAD: " + sequence);

		try {
			boolean standBy = false;
			boolean error = false;
			val rows = getLoadService().load(sequence);

			val groups = rows.stream().collect(Collectors.groupingBy(RowDto::getEstado));
			for (val entry : groups.entrySet()) {
				switch (entry.getKey()) {
				case CARGADO:
					getProducer().successfulLoad(entry.getValue());
					break;
				case EN_ESPERA:
					standBy = true;
					break;
				case ERROR_VALIDACION:
				case ERROR_CARGUE:
					error = true;
					break;
				default:
					break;
				}
			}

			if (standBy) {
				getProducer().loadPutOnStandby(sequence);
			}
			if (error) {
				getProducer().unsuccessfulLoad(sequence);
			}
		} catch (RuntimeException e) {
			getProducer().loadException(e);
			return;
		}
	}

	@RabbitListener(queues = "${rfid.amqp.queue-error}")
	public void recievedMessageError(long sequence) {
		log.debug("Mensaje ERROR: " + sequence);

		try {
			val report = getErrorReportService().getReport(sequence);
			getProducer().notifyError(report);
		} catch (RuntimeException e) {
			// TODO MANEJAR ERRORES DE NOTIFICACION
			return;
		}
	}
	
//	@RabbitListener(queues = "${rfid.amqp.queue-exception}")
//	public void recievedMessageError(RuntimeException exception) {
//		log.debug("Mensaje EXCEPTION: " + StringUtils.defaultString(e.getMessage()));
//
//		try {
////			val report = getErrorReportService().getReport(sequence);
////			val content = (String) report.get(ErrorReportService.CONTENT);
////			getProducer().notifyError(content);
//		} catch (RuntimeException e) {
//			// TODO MANEJAR ERRORES DE NOTIFICACION
//			return;
//		}
//	}	
}