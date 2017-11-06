package com.mh.rfid.core.domain;

import java.io.Serializable;

public interface IdentifiedDomainObject<ID extends Serializable> extends Serializable {

	String getExternalId();

	void setExternalId(String externalId);

	String getId();

	void setId(String version);
}
