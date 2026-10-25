package com.youou.aso.modules.appmanagement.service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.youou.aso.common.error.*;
import com.youou.aso.modules.appmanagement.domain.StoreType;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.http.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;
import static org.junit.jupiter.api.Assertions.*;
class HttpAppAvailabilityTest {
    @Test void appleEmptyResultMeansDelistedButMalformedResponseDoesNot() {
        for (String json : new String[]{"{\"resultCount\":0,\"results\":[]}", "{}", "not json", ""}) {
            var builder = RestClient.builder();
            var server = MockRestServiceServer.bindTo(builder).build();
            var verifier = new HttpAppStoreVerifier(builder.build(), new ObjectMapper());
            server.expect(requestTo("https://itunes.apple.com/lookup?id=123&country=us")).andRespond(withSuccess(json, MediaType.APPLICATION_JSON));
            if (json.contains("resultCount")) assertTrue(verifier.verify(StoreType.APP_STORE, "US", "123").isEmpty());
            else assertEquals(ErrorCode.APP_VERIFICATION_FAILED, assertThrows(BusinessException.class, () -> verifier.verify(StoreType.APP_STORE, "US", "123")).getErrorCode());
            server.verify();
        }
    }
    @Test void google404IsDelistedButRateLimitingIsUnknown() {
        for (HttpStatus status : new HttpStatus[]{HttpStatus.NOT_FOUND, HttpStatus.TOO_MANY_REQUESTS, HttpStatus.INTERNAL_SERVER_ERROR}) {
            var builder = RestClient.builder();
            var server = MockRestServiceServer.bindTo(builder).build();
            var verifier = new HttpAppStoreVerifier(builder.build(), new ObjectMapper());
            server.expect(requestTo("https://play.google.com/store/apps/details?id=com.example.app&gl=US&hl=en")).andRespond(withStatus(status));
            if (status == HttpStatus.NOT_FOUND) assertTrue(verifier.verify(StoreType.GOOGLE_PLAY, "US", "com.example.app").isEmpty());
            else assertEquals(ErrorCode.APP_VERIFICATION_FAILED, assertThrows(BusinessException.class, () -> verifier.verify(StoreType.GOOGLE_PLAY, "US", "com.example.app")).getErrorCode());
            server.verify();
        }
    }
}
