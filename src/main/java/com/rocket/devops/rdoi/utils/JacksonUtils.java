package com.rocket.devops.rdoi.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

public final class JacksonUtils {
	private static final Logger logger = LoggerFactory.getLogger(JacksonUtils.class);

	private static final ObjectMapper mapper;

	static {
		mapper = new ObjectMapper();
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
	}

	public static ObjectMapper defaultObjectMapper() {
		return mapper;
	}

	/**
	 * serialize object.
	 *
	 * @param obj obj
	 * @return json string
	 */
	public static String serialize(Object obj) {
		try {
			return mapper.writeValueAsString(obj);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return null;
	}

	/**
	 * deserialize object.
	 * @param json
	 * @param cls
	 * @param <T>
	 * @return
	 */
	public static <T> T deserialize(String json, Class<T> cls) {
		try {
			return mapper.readValue(json, cls);
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		return null;
	}

	public static Map<String, Object> convertToMap(String source) {
		try {
			Map<String, Object> rawBaseInfo = mapper.readValue(source, new TypeReference<Map<String, Object>>(){});
			return rawBaseInfo;
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		return null;
	}

}
