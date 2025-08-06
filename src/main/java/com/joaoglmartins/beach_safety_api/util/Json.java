package com.joaoglmartins.beach_safety_api.util;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Json {
	private static final ObjectMapper mapper = new ObjectMapper();

	public static <T> String encode(T object) {
		try {
			return mapper.writeValueAsString(object);
		} catch (Exception e) {
			throw new RuntimeException("Encoding error", e);
		}
	}

	public static <T> T decode(String json, Class<T> type) {
		try {
			return mapper.readValue(json, type);
		} catch (Exception e) {
			throw new RuntimeException("Decoding error", e);
		}
	}
}
