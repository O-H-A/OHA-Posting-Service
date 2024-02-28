package com.oha.posting.config.file;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class FileUtil {
    public static void deleteFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        Files.delete(path);
    }

    public static String getFileExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf('.') + 1);
    }

    public static boolean isVideo(MultipartFile multipartFile) {
        return List.of("mp4", "mov").contains(getFileExtension(multipartFile.getOriginalFilename()));
    }

    public static boolean isVideo(String fileName) {
        return List.of("mp4", "mov").contains(getFileExtension(fileName));
    }

    public static String getFileNameWithoutExtension(String fileName) {
        return fileName.substring(0, fileName.lastIndexOf('.'));
    }
}
