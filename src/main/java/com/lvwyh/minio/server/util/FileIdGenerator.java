package com.lvwyh.minio.server.util;

import com.lvwyh.minio.server.common.exception.BusinessException;

import java.util.UUID;

public final class FileIdGenerator {

    private static final String PREFIX = "SC(";
    private static final String SUFFIX = ")";

    private FileIdGenerator() {
    }

    public static String newKey() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static String wrap(String key) {
        return PREFIX + key + SUFFIX;
    }

    public static String unwrap(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new BusinessException(400, "invalid parameter");
        }
        String value = id.trim();
        if (value.startsWith(PREFIX) && value.endsWith(SUFFIX) && value.length() > 4) {
            return value.substring(PREFIX.length(), value.length() - SUFFIX.length());
        }
        return value;
    }
}
