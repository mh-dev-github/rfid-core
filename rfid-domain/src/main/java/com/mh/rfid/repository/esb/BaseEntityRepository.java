package com.mh.rfid.repository.esb;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import com.mh.rfid.domain.esb.BaseEntity;
import com.mh.rfid.enums.EstadoSincronizacionType;

@NoRepositoryBean
public interface BaseEntityRepository<E extends BaseEntity> extends JpaRepository<E, String> {

	List<E> findAllByEstado(EstadoSincronizacionType estado);

}