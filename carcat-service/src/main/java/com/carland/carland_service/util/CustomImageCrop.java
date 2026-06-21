package com.carland.carland_service.util;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
public class CustomImageCrop {
    private static final int TARGET_RATIO_WIDTH = 4;
    private static final int TARGET_RATIO_HEIGHT = 3;

    public static byte[] resizeAndCropImage(byte[] imageBytes, String targetFormat) throws IOException {
        BufferedImage originalImage = ImageIO.read(new java.io.ByteArrayInputStream(imageBytes));

        if (originalImage == null) {
            throw new IOException("Photo can not read");
        }

        int origWidth = originalImage.getWidth();
        int origHeight = originalImage.getHeight();

        float targetRatio = (float) TARGET_RATIO_WIDTH / TARGET_RATIO_HEIGHT;
        float currentRatio = (float) origWidth / origHeight;

        int cropWidth = origWidth;
        int cropHeight = origHeight;
        int cropX = 0;
        int cropY = 0;

        if (currentRatio > targetRatio) {
            cropWidth = (int) (origHeight * targetRatio);
            cropX = (origWidth - cropWidth) / 2;
        } else {
            cropHeight = (int) (origWidth / targetRatio);
            cropY = (origHeight - cropHeight) / 2;
        }

        BufferedImage croppedImage = originalImage.getSubimage(cropX, cropY, cropWidth, cropHeight);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(croppedImage, targetFormat.toLowerCase(), outputStream);

        return outputStream.toByteArray();
    }

}
