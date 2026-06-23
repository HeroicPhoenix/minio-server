package com.lvwyh.minio.server.service.impl;

import com.lvwyh.minio.server.common.exception.BusinessException;
import com.lvwyh.minio.server.service.FileCenterService;
import com.lvwyh.minio.server.service.MinioFileStorageService;
import com.lvwyh.minio.server.util.FileIdGenerator;
import com.lvwyh.minio.server.vo.AttachmentVO;
import com.lvwyh.minio.server.vo.FileUploadVO;
import com.lvwyh.minio.server.vo.PreviewFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class FileCenterServiceImpl implements FileCenterService {

    private final MinioFileStorageService minioFileStorageService;

    public FileCenterServiceImpl(MinioFileStorageService minioFileStorageService) {
        this.minioFileStorageService = minioFileStorageService;
    }

    @Override
    public FileUploadVO uploadFile(MultipartFile file, String attachId) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "invalid parameter");
        }
        String attachIdKey = attachId == null || attachId.trim().isEmpty()
                ? FileIdGenerator.newKey()
                : FileIdGenerator.unwrap(attachId);
        String attachDtlIdKey = FileIdGenerator.newKey();
        minioFileStorageService.upload(attachIdKey, attachDtlIdKey, file);

        String wrappedAttachDtlId = FileIdGenerator.wrap(attachDtlIdKey);
        return new FileUploadVO(
                FileIdGenerator.wrap(attachIdKey),
                wrappedAttachDtlId,
                "/member/previewFrom/normalView?attachDtlId=" + wrappedAttachDtlId);
    }

    @Override
    public List<AttachmentVO> getAttachmentListByAttachId(String attachId) {
        String attachIdKey = FileIdGenerator.unwrap(attachId);
        return minioFileStorageService.listByAttachId(attachIdKey);
    }

    @Override
    public boolean deleteFile(List<String> attachDtlIds) {
        if (attachDtlIds == null || attachDtlIds.isEmpty()) {
            throw new BusinessException(400, "invalid parameter");
        }
        for (String attachDtlId : attachDtlIds) {
            minioFileStorageService.deleteByAttachDtlId(FileIdGenerator.unwrap(attachDtlId));
        }
        return true;
    }

    @Override
    public PreviewFile previewFile(String attachDtlId) {
        return minioFileStorageService.getByAttachDtlId(FileIdGenerator.unwrap(attachDtlId));
    }
}
