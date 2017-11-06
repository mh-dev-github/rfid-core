
package com.mh.rfid.core.data.jpa.auditing;

import org.apache.commons.lang3.SystemUtils;
import org.springframework.data.domain.AuditorAware;

public class AuditorAwareImpl implements AuditorAware<String> {
	
	@Override
	public String getCurrentAuditor() {
		return SystemUtils.USER_NAME;
	}
}