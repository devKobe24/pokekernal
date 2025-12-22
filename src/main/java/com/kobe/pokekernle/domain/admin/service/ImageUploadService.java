package com.kobe.pokekernle.domain.admin.service;

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
public class ImageUploadService {

    @Value("${app.upload.dir:uploads/images}")
    private String uploadDir;

    @Value("${app.upload.url-prefix:/uploads/images}")
    private String urlPrefix;

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
        if (originalFilename == null || !isImageFile(originalFilename)) {
            throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다. (jpg, jpeg, png, gif, webp)");
        }

        // 업로드 디렉토리 생성
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            log.info("[IMAGE UPLOAD] 업로드 디렉토리 생성: {}", uploadPath.toAbsolutePath());
        }

        // 고유한 파일명 생성 (UUID + 원본 확장자)
        String extension = getFileExtension(originalFilename);
        String uniqueFilename = UUID.randomUUID().toString() + "." + extension;
        Path filePath = uploadPath.resolve(uniqueFilename);

        // 파일 저장
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        log.info("[IMAGE UPLOAD] 이미지 업로드 완료: {} -> {}", originalFilename, filePath.toAbsolutePath());

        // 접근 가능한 URL 반환
        return urlPrefix + "/" + uniqueFilename;
    }

    /**
     * 파일이 이미지 파일인지 확인합니다.
     */
    private boolean isImageFile(String filename) {
        String extension = getFileExtension(filename).toLowerCase();
        return extension.equals("jpg") || extension.equals("jpeg") || 
               extension.equals("png") || extension.equals("gif") || 
               extension.equals("webp");
    }

    /**
     * 파일 확장자를 추출합니다.
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1);
    }
}

