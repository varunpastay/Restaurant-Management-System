package com.restro.utility;

import jakarta.servlet.http.Part;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Set;
import java.util.UUID;

/**
 * Saves uploaded image parts (logos, banners, category/food photos) under
 * the configurable {@code upload.dir} (see app.properties), which is kept
 * outside the deployed WAR so redeploys never wipe restaurant content. Only
 * the web-servable relative path ("/uploads/&lt;subdir&gt;/&lt;file&gt;") is
 * ever stored in the database - never the raw filesystem path.
 */
public final class FileUploadUtil {

    private static final AppLogger LOG = AppLogger.getLogger(FileUploadUtil.class);
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
    private static final long MAX_FILE_SIZE_BYTES = 5L * 1024 * 1024;

    private FileUploadUtil() {
    }

    /**
     * Saves an uploaded image under upload.dir/{subdirectory}/ with a random
     * filename. Returns null if the part is empty (the user didn't choose a
     * file) so callers can distinguish "no new file" from "clear the image."
     */
    public static String saveImage(Part filePart, String subdirectory) throws IOException {
        if (filePart == null || filePart.getSize() == 0) {
            return null;
        }
        if (filePart.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new IllegalArgumentException("Image exceeds the 5 MB upload limit");
        }

        String originalName = extractFileName(filePart);
        String extension = extractExtension(originalName);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException(
                    "Unsupported image type '" + extension + "' - allowed: " + ALLOWED_EXTENSIONS);
        }

        byte[] content;
        try (InputStream in = filePart.getInputStream()) {
            content = in.readAllBytes();
        }
        if (!isGenuineImage(content, extension)) {
            throw new IllegalArgumentException(
                    "That file isn't a valid " + extension + " image (its content doesn't match a real image, "
                            + "even though the filename looked right).");
        }

        Path targetDir = Paths.get(AppConfig.get("upload.dir"), subdirectory);
        Files.createDirectories(targetDir);

        String storedFileName = UUID.randomUUID() + "." + extension;
        Path targetPath = targetDir.resolve(storedFileName);
        Files.write(targetPath, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        LOG.info("Saved upload to " + targetPath);
        return "/uploads/" + subdirectory + "/" + storedFileName;
    }

    /**
     * Confirms the uploaded bytes actually decode as the image format their
     * extension claims, rather than trusting the filename alone - rejects an
     * HTML/script/executable file renamed to end in .jpg/.png/.webp before it
     * is ever written to disk. The JDK's ImageIO has no built-in WebP reader
     * (verified against this project's JDK - only JPEG/PNG/GIF/BMP/WBMP are
     * registered), so WebP is checked via its RIFF/WEBP magic-byte header instead.
     */
    private static boolean isGenuineImage(byte[] content, String extension) {
        if ("webp".equals(extension)) {
            return content.length >= 12
                    && content[0] == 'R' && content[1] == 'I' && content[2] == 'F' && content[3] == 'F'
                    && content[8] == 'W' && content[9] == 'E' && content[10] == 'B' && content[11] == 'P';
        }
        // ImageIO.read() throws IIOException for malformed/truncated image data
        // (not just returning null) - either outcome means "reject the upload,"
        // never "let the exception reach the client as a 500."
        try (InputStream in = new ByteArrayInputStream(content)) {
            return ImageIO.read(in) != null;
        } catch (IOException e) {
            return false;
        }
    }

    /** Deletes a previously saved image given the relative path saveImage() returned. Safe to call with null/blank. */
    public static void deleteIfExists(String relativePath) {
        if (relativePath == null || relativePath.isBlank() || !relativePath.startsWith("/uploads/")) {
            return;
        }
        Path path = Paths.get(AppConfig.get("upload.dir"), relativePath.substring("/uploads/".length()));
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            LOG.warn("Failed to delete upload file " + path, e);
        }
    }

    private static String extractFileName(Part part) {
        String header = part.getHeader("content-disposition");
        if (header == null) {
            return "";
        }
        for (String token : header.split(";")) {
            token = token.trim();
            if (token.startsWith("filename")) {
                String name = token.substring(token.indexOf('=') + 1).trim().replace("\"", "");
                // Paths.get(...).getFileName() strips any directory component the browser
                // might have sent, so a crafted "../../evil.jpg" can't escape the upload dir.
                return Paths.get(name).getFileName().toString();
            }
        }
        return "";
    }

    private static String extractExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot >= 0 && dot < fileName.length() - 1 ? fileName.substring(dot + 1).toLowerCase() : "";
    }
}
