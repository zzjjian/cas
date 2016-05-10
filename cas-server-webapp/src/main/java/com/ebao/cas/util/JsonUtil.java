package com.ebao.cas.util;

import java.io.InputStream;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonGenerator.Feature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.PropertyNamingStrategy.PascalCaseStrategy;

public abstract class JsonUtil {


	private static ObjectMapper writerMapper;
	private static ObjectMapper readerMapper;
	private static ObjectMapper writeMapperPrintNull;

	// private static FastDateFormat
	// dateFormat=DateFormatUtils.ISO_DATETIME_FORMAT;
	static {
		initMapper();
	}

	private static void initMapper() {
		writerMapper = new ObjectMapper();
		writerMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		writerMapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
		writerMapper.configure(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS, true);
		writerMapper.configure(MapperFeature.CAN_OVERRIDE_ACCESS_MODIFIERS, false);
		writerMapper.configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true);
		// mapper.enable(SerializationFeature.WRITE_BIGDECIMAL_AS_PLAIN);
		writerMapper.configure(Feature.WRITE_BIGDECIMAL_AS_PLAIN, true);
		writerMapper.disable(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS);
		writerMapper.setPropertyNamingStrategy(new PascalCaseStrategy());
		writerMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);

		writeMapperPrintNull = writerMapper.copy();
		writeMapperPrintNull.setSerializationInclusion(JsonInclude.Include.ALWAYS);

		readerMapper = writerMapper.copy();
	}

	/**
	 * return jackson ObjectMapper,for internal use only
	 * 
	 * @return
	 */
	public static ObjectMapper getObjectMapper() {
		return writerMapper;
	}

	/**
	 * convert object to json, the empty Map/List will be convert to null
	 * 
	 * @param object
	 * @return
	 */
	public static String toJSON(Object object) {
//		initMapper();
		try {
			if (object == null) {
				return null;
			}
			String jsonString = writerMapper.writeValueAsString(object);

			if ("[]".equals(jsonString) || "{}".equals(jsonString)) {
				jsonString = null;
			}
			return jsonString;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * convert object to json, with null value
	 * 
	 * @param object
	 * @return
	 */
	public static String toJSONWithNullValue(Object object) {
		try {
			String jsonString = writeMapperPrintNull.writeValueAsString(object);
			return jsonString;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * convert object to jackson json node
	 * 
	 * @param object
	 * @return
	 */
	public static JsonNode toJsonNode(Object object) {
		try {
			return writerMapper.convertValue(object, JsonNode.class);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static String jsonNodeToString(JsonNode jsonNode) {
		try {
			return writerMapper.writeValueAsString(jsonNode);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * parse json string as jackson JsonNode
	 * 
	 * @param json
	 * @return
	 */
	public static JsonNode parseJson(String json) {
		try {
			return writerMapper.readTree(json);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * convert object from json
	 * 
	 * @param json
	 * @param clazz
	 * @return
	 */
	public static <T> T fromJSON(String json, Class<T> clazz) {
//		initMapper();
		try {
			if (json == null) {
				return null;
			}
			Object object = readerMapper.readValue(json, clazz);
			return (T) object;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * convert object from json
	 * 
	 * @param json
	 * @param clazz
	 * @return
	 */
	public static <T> List<T> fromJSONAsList(String json, Class<T> elementClazz) {
		return fromJSONAsList(json, List.class, elementClazz);
	}

	/**
	 * convert object from json
	 * 
	 * @param json
	 * @param elementClazz
	 * @return
	 */
	public static <T> List<T> fromJSONAsList(String json, Class<? extends List> listClazz, Class<T> elementClazz) {
		if (json == null) {
			return null;
		}
		try {
			JavaType type = readerMapper.getTypeFactory().constructCollectionType(listClazz, elementClazz);

			List<T> list = (List<T>) readerMapper.readValue(json, type);
			return list;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static <T> T fromJSON(InputStream is, Class<T> clazz) {
		try {
			Object object = readerMapper.readValue(is, clazz);
			return (T) object;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	
}
