package com.rocket.devops.rdoi.common;

import com.rocket.devops.rdoi.common.exception.RDORuntimeException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Support config wit properties file
 */
public class AppConfiguration {
	private static final String CONFIG_FILE = "app.properties";
	private static final Properties properties;

	public static Integer HTTP_CLIENT_CONNECTION_TIMEOUT;
	public static Integer HTTP_RESPONSE_TIMEOUT;

	static {
		properties = new Properties();
		try (InputStream inputStream = AppConfiguration.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
			properties.load(inputStream);
			assemble(properties);
		} catch (IOException e) {
			throw new RDORuntimeException(e);
		}
	}

	public static void assemble(Properties properties) {
		HTTP_CLIENT_CONNECTION_TIMEOUT = Integer.parseInt(properties.getProperty("http.client.connection.timeout", "120"));
		HTTP_RESPONSE_TIMEOUT = Integer.parseInt(properties.getProperty("http.response.timeout", "120"));
	}

	public AppConfiguration() {}

	public static String getProperty(String key) {
		return properties.getProperty(key);
	}
}
