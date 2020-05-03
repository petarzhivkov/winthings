package org.pekoo.osgi.springboot.utils;

import java.io.IOException;
import java.util.Map;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Generic JSON serializee/desrializer
 * @author Petar Zhivkov
 */
public class JsonHelper<T> {
	
	public  static <T> JsonHelper<T> getHelper(){
		return new JsonHelper<T>();
	}
	
	public String mapToJsonConverter(Map<String,T> map) throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
    	objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    	return objectMapper.writeValueAsString(map);
	}
	
	public String objectMapToJsonConverter(Map<String,Object> map) throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
    	objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    	return objectMapper.writeValueAsString(map);
	}
	
	public Map<String,T> jsonToMapConverter(String json) throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		TypeReference<Map<String, T>> mapType = new TypeReference<Map<String,T>>() {};
    	return objectMapper.readValue(json, mapType);
	}
	
	public Map<String, Object> jsonToObjectMapConverter(String json) throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		TypeReference<Map<String, Object>> mapType = new TypeReference<Map<String,Object>>() {};
    	return objectMapper.readValue(json, mapType);
	}
	
	public String pojoToJsonConverter(T pojo) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(pojo);
	}
	
	public T jsonToPojoConverter(String json, Class<? extends T> clazz) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.readValue(json, clazz);
	}
	
	public Object jsonToObjectConverter(String json) throws  IOException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.readValue(json, Object.class);
	}
	
	public Map<String, T> serializePojoToMap(T obj){
		ObjectMapper oMapper = new ObjectMapper();
		@SuppressWarnings("unchecked")
		Map<String, T> map = oMapper.convertValue(obj, Map.class);
		return map;
	}
	
	public String serializePojoThroughMap(T obj){
		ObjectMapper oMapper = new ObjectMapper();
		oMapper.enable(SerializationFeature.INDENT_OUTPUT);
		Map<String, T> map = serializePojoToMap(obj);
		String result="";
		try {
			result = oMapper.writeValueAsString(map);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return result;
	}

}
