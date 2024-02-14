package com.oha.posting.service;

import com.oha.posting.config.file.FileUtil;
import marvin.image.MarvinImage;
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

import static marvinplugins.MarvinPluginCollection.crop;
import static marvinplugins.MarvinPluginCollection.scale;

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
        MarvinImage imageMarvin = new MarvinImage(image);

        int centerX, centerY, halfWidth, halfHeight, cropX, cropY;

        if (originWidth >= targetWidth && originHeight >= targetHeight) {
            int scaledWidth = (originWidth < originHeight) ? targetSize : (int) (targetSize * aspectRatio);
            int scaledHeight = (originWidth < originHeight) ? (int) (targetSize / aspectRatio) : targetSize;

            scale(imageMarvin.clone(), imageMarvin, scaledWidth, scaledHeight);

            // 원본 이미지의 중심 좌표
            centerX = scaledWidth / 2;
            centerY = scaledHeight / 2;
        }
        else {
            targetWidth = Math.min(originWidth, originHeight);
            targetHeight = Math.min(originWidth, originHeight);

            // 원본 이미지의 중심 좌표
            centerX = originWidth / 2;
            centerY = originHeight / 2;
        }

        // 자를 영역의 반 너비와 반 높이
        halfWidth = targetWidth / 2;
        halfHeight = targetHeight / 2;

        // 자를 영역의 좌상단 좌표
        cropX = centerX - halfWidth;
        cropY = centerY - halfHeight;

        crop(imageMarvin.clone(), imageMarvin, cropX, cropY, targetWidth, targetHeight);

        BufferedImage bi = imageMarvin.getBufferedImageNoAlpha();
        ImageIO.write(bi, "jpg" ,new File(thumbnailPath.substring(0, thumbnailPath.indexOf(".")+1) + "jpg"));
    }
}
