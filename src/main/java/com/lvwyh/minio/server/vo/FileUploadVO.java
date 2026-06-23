package com.lvwyh.minio.server.vo;

public class FileUploadVO {

    private String attachId;

    private String attachDtlId;

    private String previewUrl;

    public FileUploadVO() {
    }

    public FileUploadVO(String attachId, String attachDtlId, String previewUrl) {
        this.attachId = attachId;
        this.attachDtlId = attachDtlId;
        this.previewUrl = previewUrl;
    }

    public String getAttachId() {
        return attachId;
    }

    public String getAttachDtlId() {
        return attachDtlId;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }
}
