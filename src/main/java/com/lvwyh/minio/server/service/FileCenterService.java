package com.lvwyh.minio.server.service;

import com.lvwyh.minio.server.vo.AttachmentVO;
import com.lvwyh.minio.server.vo.FileUploadVO;
import com.lvwyh.minio.server.vo.PreviewFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileCenterService {

    FileUploadVO uploadFile(MultipartFile file, String attachId);

    List<AttachmentVO> getAttachmentListByAttachId(String attachId);

    boolean deleteFile(List<String> attachDtlIds);

    PreviewFile previewFile(String attachDtlId);
}
