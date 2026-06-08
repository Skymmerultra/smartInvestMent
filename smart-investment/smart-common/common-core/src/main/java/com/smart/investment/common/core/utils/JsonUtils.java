package com.smart.investment.common.core.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import com.smart.investment.common.core.exception.BusinessException;
import com.smart.investment.common.core.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * JSON 工具类（基于 Jackson）
 */
@Slf4j
public final class JsonUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** 标准日期格式 */
    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final String TIME_PATTERN = "HH:mm:ss";

    static {
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDateTime.class,
                new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN)));
        javaTimeModule.addDeserializer(LocalDateTime.class,
                new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN)));
        javaTimeModule.addSerializer(LocalDate.class,
                new LocalDateSerializer(DateTimeFormatter.ofPattern(DATE_PATTERN)));
        javaTimeModule.addDeserializer(LocalDate.class,
                new LocalDateDeserializer(DateTimeFormatter.ofPattern(DATE_PATTERN)));
        javaTimeModule.addSerializer(LocalTime.class,
                new LocalTimeSerializer(DateTimeFormatter.ofPattern(TIME_PATTERN)));
        javaTimeModule.addDeserializer(LocalTime.class,
                new LocalTimeDeserializer(DateTimeFormatter.ofPattern(TIME_PATTERN)));

        MAPPER.registerModule(javaTimeModule);
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        MAPPER.setDateFormat(new SimpleDateFormat(DATE_TIME_PATTERN));
    }

    private JsonUtils() {
        throw new UnsupportedOperationException("工具类不可实例化");
    }

    /**
     * 获取 ObjectMapper 实例（供需要自定义配置的场景使用）
     */
    public static ObjectMapper getMapper() {
        return MAPPER;
    }

    /**
     * 对象 → JSON 字符串
     */
    public static String toJsonString(Object obj) {
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("序列化失败", e);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "JSON 序列化失败");
        }
    }

    /**
     * 对象 → 格式化 JSON 字符串
     */
    public static String toPrettyJsonString(Object obj) {
        try {
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("序列化失败", e);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "JSON 序列化失败");
        }
    }

    /**
     * JSON 字符串 → 对象
     */
    public static <T> T parseObject(String json, Class<T> clazz) {
        try {
            return MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.error("反序列化失败 - target: {}, json: {}", clazz.getName(), json, e);
            throw new BusinessException(ErrorCode.PARAM_FORMAT_ERROR, "JSON 解析失败");
        }
    }

    /**
     * JSON 字符串 → 泛型对象（如 List、Map）
     */
    public static <T> T parseObject(String json, TypeReference<T> typeReference) {
        try {
            return MAPPER.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            log.error("反序列化失败 - type: {}, json: {}", typeReference.getType(), json, e);
            throw new BusinessException(ErrorCode.PARAM_FORMAT_ERROR, "JSON 解析失败");
        }
    }

    /**
     * JSON 字符串 → List
     */
    public static <T> List<T> parseArray(String json, Class<T> elementClass) {
        try {
            return MAPPER.readValue(json,
                    MAPPER.getTypeFactory().constructCollectionType(List.class, elementClass));
        } catch (JsonProcessingException e) {
            log.error("反序列化 List 失败 - element: {}", elementClass.getName(), e);
            throw new BusinessException(ErrorCode.PARAM_FORMAT_ERROR, "JSON 解析失败");
        }
    }

    /**
     * JSON 字符串 → Map
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseMap(String json) {
        return parseObject(json, Map.class);
    }

    /**
     * 对象 → 另一个类型的对象（通过 JSON 中转）
     */
    public static <T> T convert(Object from, Class<T> toClass) {
        return parseObject(toJsonString(from), toClass);
    }
}
