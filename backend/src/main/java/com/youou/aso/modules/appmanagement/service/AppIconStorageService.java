package com.youou.aso.modules.appmanagement.service;

import com.youou.aso.common.error.BusinessException;
import com.youou.aso.common.error.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class AppIconStorageService {
    public static final String PUBLIC_PATH_PREFIX = "/uploads/app-icons/";

    private static final Map<String, String> ALLOWED_CONTENT_TYPES = Map.of(
            "image/png", ".png",
            "image/jpeg", ".jpg",
            "image/webp", ".webp",
            "image/gif", ".gif"
    );

    private final Path uploadRoot;
    private final long maxBytes;
    private final HttpClient httpClient;

    @Autowired
    public AppIconStorageService(
            @Value("${youou.upload.app-icon-dir:${user.home}/.youou-aso/uploads/app-icons}") String uploadRoot,
            @Value("${youou.upload.app-icon-max-bytes:1048576}") long maxBytes
    ) {
        this(Path.of(uploadRoot), maxBytes);
    }

    public AppIconStorageService(Path uploadRoot, long maxBytes) {
        this.uploadRoot = uploadRoot.toAbsolutePath().normalize();
        this.maxBytes = maxBytes;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        if (file.getSize() > maxBytes) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }

        String contentType = normalizeContentType(file.getContentType());
        String extension = ALLOWED_CONTENT_TYPES.get(contentType);
        if (extension == null) {
            throw new BusinessException(ErrorCode.FILE_TYPE_NOT_ALLOWED);
        }

        String filename = UUID.randomUUID() + extension;
        Path target = uploadRoot.resolve(filename).normalize();
        if (!target.startsWith(uploadRoot)) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED);
        }

        try {
            Files.createDirectories(uploadRoot);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
            }
            return PUBLIC_PATH_PREFIX + filename;
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    public Optional<String> storeRemote(String remoteUrl) {
        URI uri = parseHttpUri(remoteUrl).orElse(null);
        if (uri == null) {
            return Optional.empty();
        }
        try {
            HttpRequest request = HttpRequest.newBuilder(uri)
                    .timeout(Duration.ofSeconds(10))
                    .header("Accept", "image/avif,image/webp,image/apng,image/*,*/*;q=0.8")
                    .GET()
                    .build();
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return Optional.empty();
            }
            byte[] body = response.body();
            if (body == null || body.length == 0 || body.length > maxBytes) {
                return Optional.empty();
            }
            String contentType = normalizeContentType(response.headers().firstValue("Content-Type").orElse(""));
            String extension = ALLOWED_CONTENT_TYPES.get(contentType);
            if (extension == null) {
                return Optional.empty();
            }
            return Optional.of(storeBytes(body, extension));
        } catch (IOException | InterruptedException | IllegalArgumentException exception) {
            if (exception instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return Optional.empty();
        }
    }

    private String storeBytes(byte[] body, String extension) {
        String filename = UUID.randomUUID() + extension;
        Path target = uploadRoot.resolve(filename).normalize();
        if (!target.startsWith(uploadRoot)) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED);
        }
        try {
            Files.createDirectories(uploadRoot);
            Files.write(target, body);
            return PUBLIC_PATH_PREFIX + filename;
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    private Optional<URI> parseHttpUri(String remoteUrl) {
        if (remoteUrl == null || remoteUrl.isBlank()) {
            return Optional.empty();
        }
        try {
            URI uri = URI.create(remoteUrl.trim());
            String scheme = uri.getScheme();
            if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                return Optional.empty();
            }
            return Optional.of(uri);
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    private String normalizeContentType(String contentType) {
        String normalized = contentType == null ? "" : contentType.toLowerCase(Locale.ROOT).trim();
        int semicolonIndex = normalized.indexOf(';');
        return semicolonIndex < 0 ? normalized : normalized.substring(0, semicolonIndex).trim();
    }
}