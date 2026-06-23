package com.lvwyh.minio.server.service;

import com.lvwyh.minio.server.vo.AttachmentVO;
import com.lvwyh.minio.server.vo.PreviewFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MinioFileStorageService {

    void upload(String attachIdKey, String attachDtlIdKey, MultipartFile file);

    List<AttachmentVO> listByAttachId(String attachIdKey);

    boolean deleteByAttachDtlId(String attachDtlIdKey);

    PreviewFile getByAttachDtlId(String attachDtlIdKey);
}
