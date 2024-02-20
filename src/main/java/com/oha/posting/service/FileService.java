package com.oha.posting.service;

import com.oha.posting.config.file.FileUtil;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.AWTUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@Service
public class FileService {

    public void saveFileWithThumbnail(MultipartFile multipartFile, String baseSavePath, String fileName) throws IOException, JCodecException {
        File file = new File(baseSavePath+fileName);
        multipartFile.transferTo(file);

        BufferedImage image;
        if(FileUtil.isVideo(multipartFile)) {
            FrameGrab frameGrab = FrameGrab.createFrameGrab(NIOUtils.readableChannel(file));

            // 첫 프레임 얻기
            frameGrab.seekToSecondPrecise(0);
            Picture picture = frameGrab.getNativeFrame();

            // 썸네일 파일에 복사
            image = AWTUtil.toBufferedImage(picture);
        }
        else {
            image = ImageIO.read(file);
        }

        saveThumbnail(image, baseSavePath + "s_" + fileName);
    }

    public void saveThumbnail(BufferedImage image, String thumbnailPath) throws IOException {

        int targetSize = 300;

        int originWidth = image.getWidth();
        int originHeight = image.getHeight();

        int targetWidth = 300;
        int targetHeight = 300;

        double aspectRatio = (double) originWidth / originHeight;

        if (originWidth >= targetWidth && originHeight >= targetHeight) {
            int scaledWidth = (originWidth < originHeight) ? targetSize : (int) (targetSize * aspectRatio);
            int scaledHeight = (originWidth < originHeight) ? (int) (targetSize / aspectRatio) : targetSize;

            image = Thumbnails.of(image) // 리사이징
                    .size(scaledWidth, scaledHeight)
                    .asBufferedImage();

            Thumbnails.of(image)
                    .sourceRegion(Positions.CENTER, targetWidth, targetHeight) // 크롭
                    .size(targetWidth, targetHeight)
                    .outputFormat("jpg")
                    .toFile(new File(thumbnailPath));
        }
        else {
            targetWidth = Math.min(originWidth, originHeight);
            targetHeight = Math.min(originWidth, originHeight);

            Thumbnails.of(image)
                    .sourceRegion(Positions.CENTER, targetWidth, targetHeight) // 크롭
                    .size(targetWidth, targetHeight)
                    .outputFormat("jpg")
                    .toFile(new File(thumbnailPath));
        }
    }
}