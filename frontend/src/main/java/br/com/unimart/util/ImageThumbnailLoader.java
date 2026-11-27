package br.com.unimart.util;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.ImageIcon;

import br.com.unimart.model.Anuncio;

public final class ImageThumbnailLoader {

    private static final int CONNECT_TIMEOUT_MS = 4000;
    private static final int READ_TIMEOUT_MS = 5000;
    private static final int MAX_DOWNLOAD_BYTES = 5 * 1024 * 1024;
    private static final int MAX_IMAGE_DIMENSION = 8192;
    private static final long MAX_IMAGE_PIXELS = 13_000_000L;

    private ImageThumbnailLoader() {
    }

    public static ImageIcon load(String imageUrl, int targetWidth, int targetHeight) throws IOException {
        Anuncio.validarUrlFoto(imageUrl);
        if (targetWidth < 1 || targetHeight < 1) {
            throw new IllegalArgumentException("As dimensões da miniatura devem ser positivas.");
        }

        URL url = URI.create(imageUrl).toURL();
        URLConnection connection = url.openConnection();
        connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
        connection.setReadTimeout(READ_TIMEOUT_MS);
        connection.setUseCaches(false);

        try {
            if (connection instanceof HttpURLConnection httpConnection) {
                httpConnection.setInstanceFollowRedirects(true);
                int responseCode = httpConnection.getResponseCode();
                if (responseCode < 200 || responseCode >= 300) {
                    throw new IOException("O servidor retornou o status HTTP " + responseCode + ".");
                }
            }

            String contentType = connection.getContentType();
            if (contentType != null
                    && !contentType.toLowerCase(java.util.Locale.ROOT).startsWith("image/")) {
                throw new IOException("O link não retornou uma imagem.");
            }

            long contentLength = connection.getContentLengthLong();
            if (contentLength > MAX_DOWNLOAD_BYTES) {
                throw new IOException("A imagem excede o tamanho máximo permitido.");
            }

            byte[] imageBytes;
            try (InputStream input = connection.getInputStream()) {
                imageBytes = readBounded(input);
            }
            return decodeAndScale(imageBytes, targetWidth, targetHeight);
        } finally {
            if (connection instanceof HttpURLConnection httpConnection) {
                httpConnection.disconnect();
            }
        }
    }

    private static byte[] readBounded(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int total = 0;
        int bytesRead;
        while ((bytesRead = input.read(buffer)) != -1) {
            total += bytesRead;
            if (total > MAX_DOWNLOAD_BYTES) {
                throw new IOException("A imagem excede o tamanho máximo permitido.");
            }
            output.write(buffer, 0, bytesRead);
        }
        return output.toByteArray();
    }

    private static ImageIcon decodeAndScale(byte[] imageBytes, int targetWidth, int targetHeight) throws IOException {
        try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(imageBytes))) {
            if (input == null) {
                throw new IOException("O link não retornou uma imagem válida.");
            }
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) {
                throw new IOException("O link não retornou uma imagem válida.");
            }

            ImageReader reader = readers.next();
            try {
                reader.setInput(input, true, true);
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                if (width < 1 || height < 1
                        || width > MAX_IMAGE_DIMENSION
                        || height > MAX_IMAGE_DIMENSION
                        || (long) width * height > MAX_IMAGE_PIXELS) {
                    throw new IOException("As dimensões da imagem excedem o limite permitido.");
                }
                int subsampling = Math.max(1, Math.min(width / targetWidth, height / targetHeight));
                ImageReadParam readParam = reader.getDefaultReadParam();
                readParam.setSourceSubsampling(subsampling, subsampling, 0, 0);
                BufferedImage original = reader.read(0, readParam);
                return new ImageIcon(scaleToFit(original, targetWidth, targetHeight));
            } finally {
                reader.dispose();
            }
        }
    }

    private static BufferedImage scaleToFit(BufferedImage original, int targetWidth, int targetHeight) {
        double scale = Math.min(
                (double) targetWidth / original.getWidth(),
                (double) targetHeight / original.getHeight()
        );
        int width = Math.max(1, (int) Math.round(original.getWidth() * scale));
        int height = Math.max(1, (int) Math.round(original.getHeight() * scale));
        BufferedImage scaled = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = scaled.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics.drawImage(original, (targetWidth - width) / 2, (targetHeight - height) / 2, width, height, null);
        } finally {
            graphics.dispose();
        }
        return scaled;
    }
}
