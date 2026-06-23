package com.lvwyh.minio.server.common.exception;

/**
 * 业务异常。
 *
 * 只用于可预期的业务失败，例如未登录、无权限、资源不存在、参数不符合业务规则。
 * 不用于包装空指针、SQL 异常等系统错误，系统错误统一交给兜底异常处理。
 */
public class BusinessException extends RuntimeException {

    /**
     * 业务错误码。
     *
     * 约定：
     * 400 表示普通业务错误；
     * 401 表示未登录或登录失效；
     * 403 表示无权限；
     * 404 表示资源不存在。
     */
    private final int code;

    /**
     * 默认业务错误 400
     */
    public BusinessException(String message) {
        super(message);
        this.code = 400;
    }

    /**
     * 自定义错误码
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 带异常原因
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = 400;
    }

    /**
     * 带错误码 + 原因
     */
    public BusinessException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    /**
     * 获取业务错误码，供全局异常处理器组装统一响应。
     */
    public int getCode() {
        return code;
    }
}
