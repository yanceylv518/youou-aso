package com.youou.aso.modules.pricing.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class OrderModuleTranslationService {
    private static final Map<String, String> LANGUAGE_CODES = Map.of(
            "zh-CN", "zh-CN",
            "en-US", "en",
            "ru-RU", "ru",
            "pt-PT", "pt",
            "es-ES", "es"
    );

    private final JsonNode serviceNames;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public OrderModuleTranslationService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        try (var stream = getClass().getResourceAsStream("/i18n/service-names.json")) {
            this.serviceNames = objectMapper.readTree(stream);
        } catch (java.io.IOException ex) {
            throw new IllegalStateException("Unable to load service terminology", ex);
        }
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    public Map<String, Translation> translate(String sourceLocale, String name, String description) {
        String sourceLanguage = LANGUAGE_CODES.get(sourceLocale);
        if (sourceLanguage == null) {
            throw new IllegalArgumentException("Unsupported source locale");
        }

        Map<String, CompletableFuture<Translation>> futures = new LinkedHashMap<>();
        LANGUAGE_CODES.forEach((locale, targetLanguage) -> {
            if (!locale.equals(sourceLocale)) {
                String canonicalName = canonicalName(sourceLocale, locale, name);
                CompletableFuture<String> translatedName = canonicalName == null
                        ? translateText(sourceLanguage, targetLanguage, name)
                        : CompletableFuture.completedFuture(canonicalName);
                CompletableFuture<String> translatedDescription = translateText(sourceLanguage, targetLanguage, description);
                futures.put(locale, translatedName.thenCombine(translatedDescription, Translation::new));
            }
        });

        CompletableFuture.allOf(futures.values().toArray(CompletableFuture[]::new))
                .orTimeout(15, TimeUnit.SECONDS)
                .join();
        Map<String, Translation> translations = new LinkedHashMap<>();
        futures.forEach((locale, future) -> translations.put(locale, future.join()));
        return translations;
    }

    String canonicalName(String sourceLocale, String targetLocale, String name) {
        if (name == null) return null;
        String normalized = normalizeName(name);
        // Historical English labels used "General" and "Key installation".
        if (sourceLocale.equals("en-US")) {
            normalized = normalized.replace("keyinstallation", "keywordinstalls")
                    .replace("keywordinstallation", "keywordinstalls")
                    .replace("(general)", "(standard)");
        }
        for (JsonNode entry : serviceNames) {
            if (normalizeName(entry.path(sourceLocale).asText()).equals(normalized)) {
                return entry.path(targetLocale).asText();
            }
        }
        return null;
    }

    private static String normalizeName(String value) {
        return value.replace('（', '(').replace('）', ')').replaceAll("\\s+", "").toLowerCase(java.util.Locale.ROOT);
    }

    private CompletableFuture<String> translateText(String sourceLanguage, String targetLanguage, String text) {
        if (text == null || text.isBlank()) {
            return CompletableFuture.completedFuture("");
        }
        String url = "https://translate.googleapis.com/translate_a/single?client=gtx&sl="
                + encode(sourceLanguage) + "&tl=" + encode(targetLanguage) + "&dt=t&q=" + encode(text.trim());
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
                .thenApply(response -> {
                    if (response.statusCode() < 200 || response.statusCode() >= 300) {
                        throw new IllegalStateException("Translation provider returned HTTP " + response.statusCode());
                    }
                    return readTranslation(response.body());
                });
    }

    private String readTranslation(String body) {
        try {
            JsonNode segments = objectMapper.readTree(body).path(0);
            if (!segments.isArray()) {
                throw new IllegalStateException("Invalid translation response");
            }
            StringBuilder result = new StringBuilder();
            for (JsonNode segment : segments) {
                result.append(segment.path(0).asText(""));
            }
            if (result.isEmpty()) {
                throw new IllegalStateException("Empty translation response");
            }
            return result.toString();
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to parse translation response", exception);
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    public record Translation(String name, String description) {}
}
