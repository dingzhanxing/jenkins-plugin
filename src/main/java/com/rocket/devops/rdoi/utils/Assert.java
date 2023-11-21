package com.rocket.devops.rdoi.utils;

public class Assert {
	public static void notNull(Object object) {
		notNull(object, "The argument must not be null");
	}

	public static void notNull(Object object, String message) {
		if (object == null) {
			throw new IllegalArgumentException(message);
		}
	}
}
