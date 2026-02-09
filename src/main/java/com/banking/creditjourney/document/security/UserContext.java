package com.banking.creditjourney.document.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class UserContext {
	private UserContext() {

	}

	public static String getUserId() {
		if (SecurityContextHolder.getContext() == null) {
			return null;
		}
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		if (auth == null) {
			return null;
		}
		return auth.getName();
	}

}
