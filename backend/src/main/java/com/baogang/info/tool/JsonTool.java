package com.baogang.info.tool;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JSON 工具类（缩进输出格式）
 */
public class JsonTool {

    private static final Logger log = LoggerFactory.getLogger(JsonTool.class);

    /** ObjectMapper 线程安全，全局复用 */
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    private JsonTool() {
    }

    /**
     * 序列化为缩进格式 JSON。
     *
     * @param obj 可为 null（返回 JSON 字面量 "null"）
     * @return JSON 字符串
     * @throws IllegalStateException 序列化失败时抛出，不吞异常
     */
    public static String toJson(Object obj) {
        if (obj == null) {
            return "null";
        }
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("JSON序列化失败", e);
        }
    }

    /** 以 DEBUG 级别输出 JSON（替代 System.out，避免泄露敏感信息到 stdout） */
    public static void print(Object obj) {
        if (log.isDebugEnabled()) {
            log.debug("{}", toJson(obj));
        }
    }

}
