import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

/**
 * Resim sıkıştırma, boyutlandırma ve Base64 dönüşüm işlemlerini yapan yardımcı sınıf.
 */
public class ImageHelper {

    // Resmi 4:3 oranında ve 400x300 boyutlarında yeniden boyutlandırır
    public static Image resizeImageTo4_3(Image original) {
        int targetWidth = 400;
        int targetHeight = 300;

        WritableImage resized = new WritableImage(targetWidth, targetHeight);
        PixelReader reader = original.getPixelReader();

        double scaleX = original.getWidth() / targetWidth;
        double scaleY = original.getHeight() / targetHeight;

        for (int y = 0; y < targetHeight; y++) {
            for (int x = 0; x < targetWidth; x++) {
                int origX = (int) (x * scaleX);
                int origY = (int) (y * scaleY);

                if (origX < original.getWidth() && origY < original.getHeight()) {
                    resized.getPixelWriter().setColor(x, y, reader.getColor(origX, origY));
                }
            }
        }
        return resized;
    }

    // JavaFX Image nesnesini PNG formatında sıkıştırıp Base64 String'e çevirir
    // JSON kaydı için gereklidir.
    public static String imageToBase64(Image image) throws IOException {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();

        PixelReader pixelReader = image.getPixelReader();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // PNG Header yazımı
        baos.write(new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A});
        writeChunk(baos, "IHDR", createIHDR(width, height));

        byte[] imageData = new byte[height * (1 + width * 4)];
        int idx = 0;

        for (int y = 0; y < height; y++) {
            imageData[idx++] = 0; // Filter type
            for (int x = 0; x < width; x++) {
                Color color = pixelReader.getColor(x, y);
                imageData[idx++] = (byte) (color.getRed() * 255);
                imageData[idx++] = (byte) (color.getGreen() * 255);
                imageData[idx++] = (byte) (color.getBlue() * 255);
                imageData[idx++] = (byte) (color.getOpacity() * 255);
            }
        }

        byte[] compressedData = compress(imageData);
        writeChunk(baos, "IDAT", compressedData);
        writeChunk(baos, "IEND", new byte[0]);

        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    // Base64 String'i tekrar JavaFX Image nesnesine çevirir
    public static Image base64ToImage(String base64String) {
        try {
            byte[] imageBytes = Base64.getDecoder().decode(base64String);
            ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
            return new Image(bais);
        } catch (Exception e) {
            System.err.println("Base64'ten resim oluşturulamadı: " + e.getMessage());
            return null;
        }
    }

    // --- PNG Format Yardımcı Metodları ---

    private static byte[] createIHDR(int width, int height) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            writeInt(baos, width);
            writeInt(baos, height);
            baos.write(8); // Bit depth
            baos.write(6); // Color type (RGBA)
            baos.write(0); // Compression
            baos.write(0); // Filter
            baos.write(0); // Interlace
        } catch (IOException e) {
            e.printStackTrace();
        }
        return baos.toByteArray();
    }

    private static void writeChunk(ByteArrayOutputStream baos, String type, byte[] data) throws IOException {
        writeInt(baos, data.length);
        baos.write(type.getBytes());
        baos.write(data);

        java.util.zip.CRC32 crc = new java.util.zip.CRC32();
        crc.update(type.getBytes());
        crc.update(data);
        writeInt(baos, (int) crc.getValue());
    }

    private static void writeInt(ByteArrayOutputStream baos, int value) throws IOException {
        baos.write((value >> 24) & 0xFF);
        baos.write((value >> 16) & 0xFF);
        baos.write((value >> 8) & 0xFF);
        baos.write(value & 0xFF);
    }

    private static byte[] compress(byte[] data) throws IOException {
        java.util.zip.Deflater deflater = new java.util.zip.Deflater();
        deflater.setInput(data);
        deflater.finish();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];

        while (!deflater.finished()) {
            int count = deflater.deflate(buffer);
            baos.write(buffer, 0, count);
        }

        deflater.end();
        return baos.toByteArray();
    }
}