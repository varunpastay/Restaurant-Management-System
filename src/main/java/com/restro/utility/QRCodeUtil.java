package com.restro.utility;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

/** Renders a table's QR code image (ZXing) as a PNG under upload.dir/qrcodes/. */
public final class QRCodeUtil {

    private static final int SIZE_PX = 400;

    private QRCodeUtil() {
    }

    /** Returns the web-servable relative path ("/uploads/qrcodes/&lt;uuid&gt;.png") to store via QRCodeDao. */
    public static String generate(String targetUrl) throws IOException {
        try {
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.MARGIN, 1);

            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(targetUrl, BarcodeFormat.QR_CODE, SIZE_PX, SIZE_PX, hints);

            Path targetDir = Paths.get(AppConfig.get("upload.dir"), "qrcodes");
            Files.createDirectories(targetDir);

            String fileName = UUID.randomUUID() + ".png";
            Path targetPath = targetDir.resolve(fileName);
            MatrixToImageWriter.writeToPath(matrix, "PNG", targetPath);

            return "/uploads/qrcodes/" + fileName;
        } catch (WriterException e) {
            throw new IOException("Failed to generate QR code for URL: " + targetUrl, e);
        }
    }
}
