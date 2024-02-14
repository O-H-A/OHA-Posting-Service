package com.oha.posting.config.file;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Configuration
public class FileConfig {

    private final List<String> ALLOWED_EXTENSIONS = List.of(
            "jpg", "jpeg", "png", "gif", "heic", "heif" // IMAGE
            , "mp4", "mov" // VIDEO
    );

    public boolean isAllowedFile(MultipartFile file) {
        if (file == null || file.getOriginalFilename() == null) {
            return false;
        }
        String fileName = file.getOriginalFilename();
        int lastIndex = fileName.lastIndexOf(".");
        if(lastIndex == -1 || lastIndex == fileName.length()-1)
            return false;

        String fileExtension = FileUtil.getFileExtension(file.getOriginalFilename());
        return ALLOWED_EXTENSIONS.contains(fileExtension.toLowerCase());
    }

}
