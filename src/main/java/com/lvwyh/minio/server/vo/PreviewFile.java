package com.lvwyh.minio.server.vo;

import java.io.InputStream;

public class PreviewFile {

    private final InputStream inputStream;

    private final String fileName;

    private final String contentType;

    private final Long fileSize;

    public PreviewFile(InputStream inputStream, String fileName, String contentType, Long fileSize) {
        this.inputStream = inputStream;
        this.fileName = fileName;
        this.contentType = contentType;
        this.fileSize = fileSize;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public String getFileName() {
        return fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public Long getFileSize() {
        return fileSize;
    }
}
