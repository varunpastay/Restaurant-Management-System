package com.restro.utility;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import com.restro.dao.UploadedFileDao;
import com.restro.daoimpl.UploadedFileDaoImpl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

/** Renders a table's QR code image (ZXing) as a PNG stored in the uploaded_file table. */
public final class QRCodeUtil {

    private static final int SIZE_PX = 400;
    private static final UploadedFileDao uploadedFileDao = new UploadedFileDaoImpl();

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

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", out);

            String relativePath = "/uploads/qrcodes/" + UUID.randomUUID() + ".png";
            uploadedFileDao.insert(relativePath, "image/png", out.toByteArray());
            return relativePath;
        } catch (WriterException e) {
            throw new IOException("Failed to generate QR code for URL: " + targetUrl, e);
        } catch (SQLException e) {
            throw new IOException("Failed to save generated QR code to the database", e);
        }
    }
}
