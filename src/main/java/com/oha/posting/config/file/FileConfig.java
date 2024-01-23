package com.oha.posting.config.file;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Configuration
public class FileConfig {

    private final List<String> ALLOWED_EXTENSIONS = List.of("jpg", "jpeg", "png", "gif"); // IMAGE

    public boolean isAllowedFile(MultipartFile file) {
        String fileExtension = getFileExtension(file.getOriginalFilename());
        return ALLOWED_EXTENSIONS.contains(fileExtension.toLowerCase());
    }

    public String getFileExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf('.') + 1);
    }
}
