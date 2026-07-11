package com.youou.aso.modules.appmanagement.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HttpAppStoreVerifierTest {

    @Test
    void resolvesNumericAppleIdAsLookupId() {
        assertThat(HttpAppStoreVerifier.resolveAppleLookupParameter("1148524888"))
                .contains("id=1148524888");
    }

    @Test
    void resolvesAppleStoreUrlAsLookupId() {
        assertThat(HttpAppStoreVerifier.resolveAppleLookupParameter("https://apps.apple.com/cn/app/example/id1148524888"))
                .contains("id=1148524888");
        assertThat(HttpAppStoreVerifier.resolveAppleLookupParameter("https://itunes.apple.com/lookup?id=1148524888"))
                .contains("id=1148524888");
    }

    @Test
    void resolvesBundleIdAsLookupBundleId() {
        assertThat(HttpAppStoreVerifier.resolveAppleLookupParameter("com.example.app"))
                .contains("bundleId=com.example.app");
    }

    @Test
    void keepsNameSearchAsSearchTerm() {
        assertThat(HttpAppStoreVerifier.resolveAppleLookupParameter("Example App"))
                .isEmpty();
    }

    @Test
    void extractsGooglePlayTitleFromStructuredDataBeforeTitleFallback() {
        String html = "<html><head><script>{\"name\":\"Alipay - Simplify Your Life\",\"url\":\"/store/apps/details?id=com.eg.android.AlipayGphone\"}</script><title>com.eg.android.AlipayGphone - Apps on Google Play</title></head></html>";

        assertThat(HttpAppStoreVerifier.extractGooglePlayTitle(html))
                .contains("Alipay - Simplify Your Life");
    }
}
