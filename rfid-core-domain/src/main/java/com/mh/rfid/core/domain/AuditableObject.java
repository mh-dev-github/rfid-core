package com.mh.rfid.core.domain;

import java.time.LocalDateTime;

public interface AuditableObject {
	
	LocalDateTime getCreatedDate();

	void setCreatedDate(LocalDateTime createdDate);

	LocalDateTime getModifiedDate();

	void setModifiedDate(LocalDateTime modifiedDate);
}
