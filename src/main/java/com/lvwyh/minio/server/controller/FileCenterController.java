package com.lvwyh.minio.server.controller;

import com.lvwyh.minio.server.common.response.ApiResponse;
import com.lvwyh.minio.server.service.FileCenterService;
import com.lvwyh.minio.server.vo.AttachmentVO;
import com.lvwyh.minio.server.vo.FileUploadVO;
import com.lvwyh.minio.server.vo.PreviewFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Validated
@RestController
public class FileCenterController {

    private static final Logger log = LogManager.getLogger(FileCenterController.class);

    private final FileCenterService fileCenterService;

    public FileCenterController(FileCenterService fileCenterService) {
        this.fileCenterService = fileCenterService;
    }

    @PostMapping("/member/StreamUpload/uploadFile")
    public ApiResponse<FileUploadVO> uploadFile(@RequestParam("file") MultipartFile file,
                                                @RequestParam(value = "attachId", required = false) String attachId) {
        log.info("Upload file request: attachId={}, fileName={}, size={}",
                attachId, file == null ? null : file.getOriginalFilename(), file == null ? null : file.getSize());
        return ApiResponse.success(fileCenterService.uploadFile(file, attachId));
    }

    @GetMapping("/member/fileMgr/getAttachmentListByAttachId")
    public ApiResponse<List<AttachmentVO>> getAttachmentListByAttachId(
            @RequestParam("attachId") @NotBlank String attachId) {
        return ApiResponse.success(fileCenterService.getAttachmentListByAttachId(attachId));
    }

    @PostMapping("/member/fileMgr/deleteFile")
    public ApiResponse<Boolean> deleteFile(@RequestBody List<String> attachDtlIds) {
        return ApiResponse.success(fileCenterService.deleteFile(attachDtlIds));
    }

    @GetMapping("/member/previewFrom/normalView")
    public ResponseEntity<InputStreamResource> normalView(@RequestParam("attachDtlId") @NotBlank String attachDtlId) {
        PreviewFile previewFile = fileCenterService.previewFile(attachDtlId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.inline()
                .filename(previewFile.getFileName(), StandardCharsets.UTF_8)
                .build());
        if (previewFile.getFileSize() != null) {
            headers.setContentLength(previewFile.getFileSize());
        }
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType(previewFile.getContentType()))
                .body(new InputStreamResource(previewFile.getInputStream()));
    }
}
