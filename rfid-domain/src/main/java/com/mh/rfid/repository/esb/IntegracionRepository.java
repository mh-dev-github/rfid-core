package com.mh.rfid.repository.esb;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mh.rfid.domain.esb.Integracion;

public interface IntegracionRepository extends JpaRepository<Integracion, String>{

	Integracion findOneByCodigo(String string);

}
