package com.lvwyh.minio.server.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 项目统一接口响应结构。
 *
 * Controller 层统一返回该对象，前端可以稳定读取 code、status、message、data、timestamp。
 */
@Schema(description = "统一接口返回结构")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /** 业务状态码，和 HTTP 状态码解耦。 */
    @Schema(description = "业务状态码", example = "200")
    private Integer code;

    /** 业务是否成功。 */
    @Schema(description = "是否成功", example = "true")
    private Boolean status;

    /** 返回给调用方的提示信息。 */
    @Schema(description = "提示信息", example = "操作成功")
    private String message;

    /** 实际业务数据。 */
    @Schema(description = "实际返回数据")
    private T data;

    /** 响应生成时间戳，便于前端排查请求时序。 */
    @Schema(description = "时间戳", example = "1710000000000")
    private Long timestamp;

    public ApiResponse() {
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 创建默认成功响应。
     */
    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> r = new ApiResponse<>();
        r.code = 200;
        r.status = true;
        r.message = "操作成功";
        r.data = data;
        return r;
    }

    /**
     * 创建带自定义成功消息的响应。
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        ApiResponse<T> r = new ApiResponse<>();
        r.code = 200;
        r.status = true;
        r.message = message;
        r.data = data;
        return r;
    }

    /**
     * 创建失败响应。
     */
    public static <T> ApiResponse<T> fail(int code, String message) {
        ApiResponse<T> r = new ApiResponse<>();
        r.code = code;
        r.status = false;
        r.message = message;
        return r;
    }

    public static <T> ApiResponse<T> fail(String message, int code) {
        return fail(code, message);
    }

    public Integer getCode() {
        return code;
    }

    public Boolean getStatus() {
        return status;
    }

    public boolean isStatus() {
        return Boolean.TRUE.equals(status);
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public Long getTimestamp() {
        return timestamp;
    }
}
