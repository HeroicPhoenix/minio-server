package com.lvwyh.minio.server.service.impl;

import com.lvwyh.minio.server.common.exception.BusinessException;
import com.lvwyh.minio.server.config.minio.FileCenterProperties;
import com.lvwyh.minio.server.service.MinioFileStorageService;
import com.lvwyh.minio.server.util.FileIdGenerator;
import com.lvwyh.minio.server.vo.AttachmentVO;
import com.lvwyh.minio.server.vo.PreviewFile;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.ListObjectsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.messages.Item;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class MinioFileStorageServiceImpl implements MinioFileStorageService {

    private static final Logger log = LogManager.getLogger(MinioFileStorageServiceImpl.class);

    private static final String META_FILE_NAME = "file-name";

    private final MinioClient minioClient;

    private final FileCenterProperties properties;

    public MinioFileStorageServiceImpl(MinioClient minioClient, FileCenterProperties properties) {
        this.minioClient = minioClient;
        this.properties = properties;
    }

    @PostConstruct
    public void initBucket() {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(properties.getBucketName())
                    .build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(properties.getBucketName())
                        .build());
            }
        } catch (Exception e) {
            throw new BusinessException(500, "system error", e);
        }
    }

    @Override
    public void upload(String attachIdKey, String attachDtlIdKey, MultipartFile file) {
        String objectName = buildObjectName(attachIdKey, attachDtlIdKey, getExtension(file.getOriginalFilename()));
        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(properties.getBucketName())
                    .object(objectName)
                    .contentType(resolveContentType(file.getContentType()))
                    .userMetadata(buildMetadata(file.getOriginalFilename()))
                    .stream(inputStream, file.getSize(), -1)
                    .build());
        } catch (Exception e) {
            log.error("Upload file to MinIO failed: objectName={}", objectName, e);
            throw new BusinessException(500, "system error", e);
        }
    }

    @Override
    public List<AttachmentVO> listByAttachId(String attachIdKey) {
        String prefix = buildAttachPrefix(attachIdKey);
        List<AttachmentVO> attachments = new ArrayList<>();
        try {
            Iterable<io.minio.Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
                    .bucket(properties.getBucketName())
                    .prefix(prefix)
                    .recursive(true)
                    .build());
            for (io.minio.Result<Item> result : results) {
                Item item = result.get();
                if (item.isDir()) {
                    continue;
                }
                StatObjectResponse stat = stat(item.objectName());
                String attachDtlIdKey = getAttachDtlIdKey(item.objectName());
                attachments.add(new AttachmentVO(
                        FileIdGenerator.wrap(attachIdKey),
                        FileIdGenerator.wrap(attachDtlIdKey),
                        readFileName(stat, item.objectName()),
                        stat.size(),
                        resolveContentType(stat.contentType())));
            }
            return attachments;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("List MinIO attachments failed: prefix={}", prefix, e);
            throw new BusinessException(500, "system error", e);
        }
    }

    @Override
    public boolean deleteByAttachDtlId(String attachDtlIdKey) {
        String objectName = findObjectNameByAttachDtlId(attachDtlIdKey);
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(properties.getBucketName())
                    .object(objectName)
                    .build());
            return true;
        } catch (Exception e) {
            log.error("Delete MinIO object failed: objectName={}", objectName, e);
            throw new BusinessException(500, "system error", e);
        }
    }

    @Override
    public PreviewFile getByAttachDtlId(String attachDtlIdKey) {
        String objectName = findObjectNameByAttachDtlId(attachDtlIdKey);
        try {
            StatObjectResponse stat = stat(objectName);
            InputStream inputStream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(properties.getBucketName())
                    .object(objectName)
                    .build());
            return new PreviewFile(inputStream, readFileName(stat, objectName), resolveContentType(stat.contentType()), stat.size());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Get MinIO object failed: objectName={}", objectName, e);
            throw new BusinessException(500, "system error", e);
        }
    }

    private StatObjectResponse stat(String objectName) {
        try {
            return minioClient.statObject(StatObjectArgs.builder()
                    .bucket(properties.getBucketName())
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            throw new BusinessException(404, "file not found", e);
        }
    }

    private String findObjectNameByAttachDtlId(String attachDtlIdKey) {
        String prefix = properties.normalizedObjectPrefix() + "/";
        try {
            Iterable<io.minio.Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
                    .bucket(properties.getBucketName())
                    .prefix(prefix)
                    .recursive(true)
                    .build());
            for (io.minio.Result<Item> result : results) {
                Item item = result.get();
                if (!item.isDir() && attachDtlIdKey.equals(getAttachDtlIdKey(item.objectName()))) {
                    return item.objectName();
                }
            }
        } catch (Exception e) {
            throw new BusinessException(500, "system error", e);
        }
        throw new BusinessException(404, "file not found");
    }

    private String buildObjectName(String attachIdKey, String attachDtlIdKey, String extension) {
        return buildAttachPrefix(attachIdKey) + attachDtlIdKey + extension;
    }

    private String buildAttachPrefix(String attachIdKey) {
        return properties.normalizedObjectPrefix() + "/" + attachIdKey + "/";
    }

    private String getAttachDtlIdKey(String objectName) {
        String fileName = objectName.substring(objectName.lastIndexOf('/') + 1);
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex <= 0) {
            return fileName;
        }
        return fileName.substring(0, dotIndex);
    }

    private String readFileName(StatObjectResponse stat, String objectName) {
        Map<String, String> metadata = stat.userMetadata();
        String encoded = metadata.get(META_FILE_NAME);
        if (encoded == null) {
            encoded = metadata.get("X-Amz-Meta-" + META_FILE_NAME);
        }
        if (encoded != null && !encoded.trim().isEmpty()) {
            return urlDecode(encoded);
        }
        return objectName.substring(objectName.lastIndexOf('/') + 1);
    }

    private Map<String, String> buildMetadata(String originalFilename) {
        java.util.HashMap<String, String> metadata = new java.util.HashMap<>();
        metadata.put(META_FILE_NAME, urlEncode(originalFilename == null ? "file" : originalFilename));
        return metadata;
    }

    private String getExtension(String originalFilename) {
        if (originalFilename == null) {
            return "";
        }
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == originalFilename.length() - 1) {
            return "";
        }
        return originalFilename.substring(dotIndex);
    }

    private String resolveContentType(String contentType) {
        if (contentType == null || contentType.trim().isEmpty()) {
            return "application/octet-stream";
        }
        return contentType;
    }

    private String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (Exception e) {
            throw new BusinessException(500, "system error", e);
        }
    }

    private String urlDecode(String value) {
        try {
            return URLDecoder.decode(value, "UTF-8");
        } catch (Exception e) {
            throw new BusinessException(500, "system error", e);
        }
    }
}
