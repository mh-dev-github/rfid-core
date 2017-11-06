package com.mh.rfid.repository.msg;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mh.rfid.domain.msg.Mensaje;
import com.mh.rfid.enums.EstadoMensajeType;
import com.mh.rfid.enums.IntegracionType;

public interface MensajeRepository extends JpaRepository<Mensaje, Long> {
	Mensaje findOneByIntegracionAndExternalIdAndEstado(IntegracionType integracion, String externalId,
			Set<EstadoMensajeType> estado);

	List<Mensaje> findAllByIntegracionAndEstado(IntegracionType integracion, EstadoMensajeType estado);
}
