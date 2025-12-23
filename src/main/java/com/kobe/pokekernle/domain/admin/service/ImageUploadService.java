package com.kobe.pokekernle.domain.admin.service;

import io.awspring.cloud.s3.S3Template;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * packageName    : com.kobe.pokekernle.domain.admin.service
 * fileName       : ImageUploadService
 * author         : kobe
 * date           : 2025. 12. 22.
 * description    : 이미지 업로드 서비스 (로컬 파일 시스템 저장 또는 S3)
 */
@Slf4j
@Service
public class ImageUploadService {

    @Autowired(required = false)
    private S3Template s3Template;

    @Value("${spring.cloud.aws.s3.bucket:}")
    private String bucketName;

    @Value("${spring.cloud.aws.region.static:ap-northeast-2}")
    private String region;

    @Value("${app.s3.cloudfront-domain:}")
    private String cloudFrontDomain;

    private boolean useS3;

    @Autowired
    public void init() {
        this.bucketName = bucketName != null ? bucketName : "";
        this.region = region != null ? region : "ap-northeast-2";
        this.cloudFrontDomain = cloudFrontDomain != null ? cloudFrontDomain : "";
        this.useS3 = s3Template != null && !this.bucketName.isEmpty();
        
        log.info("[IMAGE UPLOAD SERVICE] 초기화 완료:");
        log.info("[IMAGE UPLOAD SERVICE]   useS3: {}", useS3);
        log.info("[IMAGE UPLOAD SERVICE]   bucketName: {}", this.bucketName);
        log.info("[IMAGE UPLOAD SERVICE]   region: {}", this.region);
        log.info("[IMAGE UPLOAD SERVICE]   cloudFrontDomain: {}", this.cloudFrontDomain);
    }

    /**
     * 이미지 파일을 업로드하고 접근 가능한 URL을 반환합니다.
     * @param file 업로드할 파일
     * @return 접근 가능한 이미지 URL (예: /uploads/images/xxx.jpg)
     * @throws IOException 파일 저장 실패 시
     */
    public String uploadImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String fileName = UUID.randomUUID().toString() + "." + extension;

        if (useS3 && s3Template != null) {
            // S3로 업로드 (운영 환경)
            String s3Key = "images/" + fileName;
            log.info("[S3 UPLOAD] 업로드 시작 - Bucket: {}, Key: {}", bucketName, s3Key);
            
            try {
                s3Template.upload(bucketName, s3Key, file.getInputStream());
                log.info("[S3 UPLOAD] 업로드 성공 - Bucket: {}, Key: {}", bucketName, s3Key);
            } catch (Exception e) {
                log.error("[S3 UPLOAD] 업로드 실패 - Bucket: {}, Key: {}", bucketName, s3Key, e);
                throw new IOException("S3 업로드 실패: " + e.getMessage(), e);
            }
            
            // S3 Public URL 생성
            // CloudFront 도메인이 설정되어 있으면 사용, 없으면 S3 직접 Public URL 사용
            String fileUrl;
            if (cloudFrontDomain != null && !cloudFrontDomain.isEmpty() && !cloudFrontDomain.isBlank()) {
                // CloudFront를 통한 URL (권장)
                fileUrl = "https://" + cloudFrontDomain + "/" + s3Key;
                log.info("[S3 UPLOAD] CloudFront URL 사용: {}", fileUrl);
            } else {
                // S3 직접 Public URL (버킷이 public이어야 함)
                // 형식: https://{bucket-name}.s3.{region}.amazonaws.com/{key}
                fileUrl = String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, s3Key);
                log.info("[S3 UPLOAD] S3 직접 URL 사용: {}", fileUrl);
            }
            
            log.info("[S3 UPLOAD] 최종 URL: {}", fileUrl);
            log.info("[S3 UPLOAD] Bucket: {}, Key: {}, Region: {}", bucketName, s3Key, region);
            return fileUrl;
        } else {
            // 로컬 파일 시스템에 저장 (개발 환경)
            Path uploadDir = Paths.get("uploads/images");
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            Path filePath = uploadDir.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String fileUrl = "/uploads/images/" + fileName;
            log.info("[LOCAL UPLOAD] 완료: {}", fileUrl);
            return fileUrl;
        }
    }

    /**
     * 파일 확장자를 추출합니다.
     */
    private String getFileExtension(String filename) {
        if (filename == null) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1);
    }
}

