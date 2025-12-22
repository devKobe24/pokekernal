package com.kobe.pokekernle.domain.admin.service;

import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
 * description    : 이미지 업로드 서비스 (로컬 파일 시스템 저장)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImageUploadService {

    private final S3Template s3Template;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucketName;
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

        // 파일 확장자 검증
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        // S3 내 파일 키 (이름)
        String s3Key = "images/" + UUID.randomUUID().toString() + "." + extension;

        // S3로 업로드
        s3Template.upload(bucketName, s3Key, file.getInputStream());

        // 업로드된 파일의 접근 URL 생성 (S3 URL)
        String fileUrl = s3Template.download(bucketName, s3Key).getURL().toString();

        log.info("[S3 UPLOAD] 완료: {}", fileUrl);
        return fileUrl;
    }

    /**
     * 파일 확장자를 추출합니다.
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1);
    }
}

