package com.gonaturefarms.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Generic {success, message, ...extra} response envelope.
 * <p>
 * The original Express routes returned ad-hoc JSON objects like
 * {@code { success: true, products: rows } } or {@code { success: false, message: '...' } }.
 * To keep the exact same JSON shape for the frontend, this class behaves like a
 * simple ordered map that always serializes "success" first, then "message" (if present),
 * then any additional named fields added via {@link #with(String, Object)}.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse extends LinkedHashMap<String, Object> {

    private ApiResponse(boolean success) {
        super();
        put("success", success);
    }

    public static ApiResponse ok() {
        return new ApiResponse(true);
    }

    public static ApiResponse ok(String message) {
        ApiResponse r = new ApiResponse(true);
        r.put("message", message);
        return r;
    }

    public static ApiResponse fail(String message) {
        ApiResponse r = new ApiResponse(false);
        r.put("message", message);
        return r;
    }

    public ApiResponse with(String key, Object value) {
        this.put(key, value);
        return this;
    }

    public ApiResponse withMessage(String message) {
        this.put("message", message);
        return this;
    }

    public static ApiResponse from(Map<String, Object> map) {
        ApiResponse r = new ApiResponse(true);
        r.putAll(map);
        return r;
    }
}
