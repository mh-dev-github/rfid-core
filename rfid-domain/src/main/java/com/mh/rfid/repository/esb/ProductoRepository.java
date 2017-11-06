package com.mh.rfid.repository.esb;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mh.rfid.domain.esb.Producto;

public interface ProductoRepository extends BaseEntityRepository<Producto>{

	@Query("SELECT a.id FROM Producto a WHERE a.externalId = :externalId")
    String translateCode(@Param("externalId") String externalId);

}
