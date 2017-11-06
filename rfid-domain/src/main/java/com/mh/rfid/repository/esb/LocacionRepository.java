package com.mh.rfid.repository.esb;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mh.rfid.domain.esb.Locacion;

public interface LocacionRepository extends BaseEntityRepository<Locacion> {

	@Query("SELECT a.id FROM Locacion a WHERE a.externalId = :externalId")
	String translateCode(@Param("externalId") String externalId);

}
