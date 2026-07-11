package com.youou.aso.modules.appmanagement.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.youou.aso.modules.appmanagement.domain.StoreType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class HttpAppStoreVerifier implements AppStoreVerifier {
    private static final Logger log = LoggerFactory.getLogger(HttpAppStoreVerifier.class);
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(4);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(8);
    private static final Pattern APPLE_STORE_ID_PATTERN = Pattern.compile("(?i)(?:/id|[?&]id=)(\\d+)");
    private static final Pattern APPLE_BUNDLE_ID_PATTERN = Pattern.compile("^[A-Za-z][A-Za-z0-9_]*(?:\\.[A-Za-z][A-Za-z0-9_]*)+$");

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public HttpAppStoreVerifier(RestClient.Builder restClientBuilder, ObjectMapper objectMapper) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(CONNECT_TIMEOUT);
        requestFactory.setReadTimeout(READ_TIMEOUT);
        this.restClient = restClientBuilder.requestFactory(requestFactory).build();
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<VerifiedStoreApp> verify(StoreType storeType, String regionCode, String appIdentifier) {
        try {
            if (storeType == StoreType.GOOGLE_PLAY) {
                return verifyGooglePlay(regionCode, appIdentifier);
            }
            return verifyApple(storeType, regionCode, appIdentifier);
        } catch (RuntimeException ex) {
            log.warn("Store app verification failed. storeType={}, regionCode={}, appIdentifier={}",
                    storeType, regionCode, appIdentifier, ex);
            return Optional.empty();
        }
    }

    @Override
    public List<StoreAppSearchResult> search(StoreType storeType, String regionCode, String keyword, int limit) {
        try {
            if (storeType == StoreType.GOOGLE_PLAY) {
                return searchGooglePlay(regionCode, keyword, limit);
            }
            return searchApple(storeType, regionCode, keyword, limit);
        } catch (RuntimeException ex) {
            log.warn("Store app search failed. storeType={}, regionCode={}, keyword={}",
                    storeType, regionCode, keyword, ex);
            return List.of();
        }
    }

    private List<StoreAppSearchResult> searchApple(StoreType storeType, String regionCode, String keyword, int limit) {
        Optional<String> lookupParameter = resolveAppleLookupParameter(keyword);
        if (lookupParameter.isPresent()) {
            return lookupApple(storeType, regionCode, lookupParameter.get())
                    .map(List::of)
                    .orElseGet(List::of);
        }

        String entity = storeType == StoreType.IPAD_STORE ? "iPadSoftware" : "software";
        String body = restClient.get()
                .uri("https://itunes.apple.com/search?term={term}&country={country}&media=software&entity={entity}&limit={limit}",
                        keyword,
                        regionCode.toLowerCase(Locale.ROOT),
                        entity,
                        limit)
                .retrieve()
                .body(String.class);
        if (body == null || body.isBlank()) {
            return List.of();
        }
        try {
            AppleLookupResponse response = objectMapper.readValue(body, AppleLookupResponse.class);
            return response.results().stream()
                    .map(item -> toAppleSearchResult(storeType, regionCode, item))
                    .toList();
        } catch (Exception ex) {
            return List.of();
        }
    }

    private List<StoreAppSearchResult> searchGooglePlay(String regionCode, String keyword, int limit) {
        String html = restClient.get()
                .uri("https://play.google.com/store/search?q={keyword}&c=apps&gl={gl}&hl=en", keyword, regionCode)
                .retrieve()
                .body(String.class);
        if (html == null || html.isBlank()) {
            return List.of();
        }
        Pattern pattern = Pattern.compile("/store/apps/details\\?id=([A-Za-z][A-Za-z0-9_]*(?:\\.[A-Za-z][A-Za-z0-9_]*)+)");
        Matcher matcher = pattern.matcher(html);
        List<String> packageNames = new ArrayList<>();
        while (matcher.find() && packageNames.size() < limit) {
            String packageName = matcher.group(1);
            if (!packageNames.contains(packageName)) {
                packageNames.add(packageName);
            }
        }
        List<StoreAppSearchResult> results = new ArrayList<>();
        for (String packageName : packageNames) {
            verifyGooglePlay(regionCode, packageName)
                    .map(app -> new StoreAppSearchResult(
                            StoreType.GOOGLE_PLAY,
                            regionCode,
                            app.appIdentifier(),
                            app.appName(),
                            app.appIconUrl(),
                            app.bundleId(),
                            app.externalAppId(),
                            app.category(),
                            null
                    ))
                    .ifPresent(results::add);
        }
        return results;
    }

    private Optional<VerifiedStoreApp> verifyApple(StoreType storeType, String regionCode, String appIdentifier) {
        String country = regionCode.toLowerCase(Locale.ROOT);
        String lookupParameter = resolveAppleLookupParameter(appIdentifier)
                .orElse("bundleId=" + appIdentifier.trim());
        String body = restClient.get()
                .uri("https://itunes.apple.com/lookup?" + lookupParameter + "&country=" + country)
                .retrieve()
                .body(String.class);
        if (body == null || body.isBlank()) {
            return Optional.empty();
        }
        try {
            AppleLookupResponse response = objectMapper.readValue(body, AppleLookupResponse.class);
            if (response.resultCount() <= 0 || response.results().isEmpty()) {
                return Optional.empty();
            }
            AppleLookupItem item = response.results().get(0);
            String normalizedIdentifier = lookupParameter.startsWith("id=") && item.trackId() != null
                    ? String.valueOf(item.trackId())
                    : appIdentifier.trim();
            return Optional.of(new VerifiedStoreApp(
                    storeType,
                    regionCode,
                    normalizedIdentifier,
                    item.trackName(),
                    item.artworkUrl100(),
                    item.bundleId(),
                    item.trackId() == null ? null : String.valueOf(item.trackId()),
                    item.primaryGenreName()
            ));
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    private Optional<StoreAppSearchResult> lookupApple(StoreType storeType, String regionCode, String lookupParameter) {
        String body = restClient.get()
                .uri("https://itunes.apple.com/lookup?" + lookupParameter + "&country=" + regionCode.toLowerCase(Locale.ROOT))
                .retrieve()
                .body(String.class);
        if (body == null || body.isBlank()) {
            return Optional.empty();
        }
        try {
            AppleLookupResponse response = objectMapper.readValue(body, AppleLookupResponse.class);
            if (response.resultCount() <= 0 || response.results().isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(toAppleSearchResult(storeType, regionCode, response.results().get(0)));
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    static Optional<String> resolveAppleLookupParameter(String input) {
        String normalized = input == null ? "" : input.trim();
        if (normalized.isBlank()) {
            return Optional.empty();
        }

        Matcher storeIdMatcher = APPLE_STORE_ID_PATTERN.matcher(normalized);
        if (storeIdMatcher.find()) {
            return Optional.of("id=" + storeIdMatcher.group(1));
        }
        if (normalized.chars().allMatch(Character::isDigit)) {
            return Optional.of("id=" + normalized);
        }
        if (APPLE_BUNDLE_ID_PATTERN.matcher(normalized).matches()) {
            return Optional.of("bundleId=" + normalized);
        }
        return Optional.empty();
    }

    private StoreAppSearchResult toAppleSearchResult(StoreType storeType, String regionCode, AppleLookupItem item) {
        return new StoreAppSearchResult(
                storeType,
                regionCode,
                item.trackId() == null ? item.bundleId() : String.valueOf(item.trackId()),
                item.trackName(),
                item.artworkUrl100(),
                item.bundleId(),
                item.trackId() == null ? null : String.valueOf(item.trackId()),
                item.primaryGenreName(),
                item.artistName()
        );
    }

    private Optional<VerifiedStoreApp> verifyGooglePlay(String regionCode, String appIdentifier) {
        String packageName = appIdentifier.trim();
        if (!packageName.matches("^[A-Za-z][A-Za-z0-9_]*(\\.[A-Za-z][A-Za-z0-9_]*)+$")) {
            return Optional.empty();
        }
        String html = restClient.get()
                .uri("https://play.google.com/store/apps/details?id={id}&gl={gl}&hl=en", packageName, regionCode)
                .retrieve()
                .body(String.class);
        if (html == null || html.isBlank() || html.contains("We're sorry, the requested URL was not found")) {
            return Optional.empty();
        }
        String appName = extractGooglePlayTitle(html).orElse(packageName);
        String iconUrl = extractGooglePlayIcon(html).orElse(null);
        String category = extractGooglePlayCategory(html).orElse(null);
        return Optional.of(new VerifiedStoreApp(
                StoreType.GOOGLE_PLAY,
                regionCode,
                packageName,
                appName,
                iconUrl,
                packageName,
                packageName,
                category
        ));
    }

    static Optional<String> extractGooglePlayTitle(String html) {
        Optional<String> name = extractJsonStringValue(html, "name");
        if (name.isPresent()) {
            return name;
        }

        int marker = html.indexOf("<title>");
        int end = html.indexOf("</title>");
        if (marker < 0 || end <= marker) {
            return Optional.empty();
        }
        String title = html.substring(marker + 7, end)
                .replace("- Apps on Google Play", "")
                .replace(" - Apps on Google Play", "")
                .replace("&amp;", "&")
                .trim();
        return title.isBlank() ? Optional.empty() : Optional.of(title);
    }

    private Optional<String> extractGooglePlayIcon(String html) {
        String marker = "\"image\":\"";
        int start = html.indexOf(marker);
        if (start < 0) {
            return Optional.empty();
        }
        int valueStart = start + marker.length();
        int valueEnd = html.indexOf('"', valueStart);
        if (valueEnd <= valueStart) {
            return Optional.empty();
        }
        return Optional.of(html.substring(valueStart, valueEnd).replace("\\u003d", "="));
    }

    private Optional<String> extractGooglePlayCategory(String html) {
        Optional<String> applicationCategory = extractJsonStringValue(html, "applicationCategory");
        if (applicationCategory.isPresent()) {
            return applicationCategory;
        }
        return extractJsonStringValue(html, "genre");
    }

    private static Optional<String> extractJsonStringValue(String html, String key) {
        String marker = "\"" + key + "\":\"";
        int start = html.indexOf(marker);
        if (start < 0) {
            return Optional.empty();
        }
        int valueStart = start + marker.length();
        int valueEnd = findJsonStringEnd(html, valueStart);
        if (valueEnd <= valueStart) {
            return Optional.empty();
        }
        String value = decodeJsonString(html.substring(valueStart, valueEnd)).trim();
        return value.isBlank() ? Optional.empty() : Optional.of(value);
    }

    private static int findJsonStringEnd(String text, int start) {
        boolean escaped = false;
        for (int i = start; i < text.length(); i++) {
            char current = text.charAt(i);
            if (current == '"' && !escaped) {
                return i;
            }
            escaped = current == '\\' && !escaped;
            if (current != '\\') {
                escaped = false;
            }
        }
        return -1;
    }

    private static String decodeJsonString(String value) {
        return value
                .replace("\\u0026", "&")
                .replace("\\u003d", "=")
                .replace("\\u003c", "<")
                .replace("\\u003e", ">")
                .replace("\\\"", "\"")
                .replace("\\/", "/")
                .replace("&amp;", "&");
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record AppleLookupResponse(int resultCount, List<AppleLookupItem> results) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record AppleLookupItem(
            Long trackId,
            String trackName,
            String bundleId,
            String artworkUrl100,
            String artistName,
            String primaryGenreName
    ) {
    }
}
