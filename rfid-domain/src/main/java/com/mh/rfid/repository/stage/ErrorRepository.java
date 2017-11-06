package com.mh.rfid.repository.stage;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mh.rfid.domain.stage.Error;

public interface ErrorRepository extends JpaRepository<Error, Long> {
	List<Error> findAllById(Long id);
}
