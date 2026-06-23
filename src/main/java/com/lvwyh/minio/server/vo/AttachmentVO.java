package com.lvwyh.minio.server.vo;

public class AttachmentVO {

    private String attachId;

    private String attachDtlId;

    private String fileName;

    private Long fileSize;

    private String contentType;

    public AttachmentVO() {
    }

    public AttachmentVO(String attachId, String attachDtlId, String fileName, Long fileSize, String contentType) {
        this.attachId = attachId;
        this.attachDtlId = attachDtlId;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.contentType = contentType;
    }

    public String getAttachId() {
        return attachId;
    }

    public String getAttachDtlId() {
        return attachDtlId;
    }

    public String getFileName() {
        return fileName;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public String getContentType() {
        return contentType;
    }
}
