package com.youou.aso.modules.appmanagement.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.Flow;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class AppIconStorageServiceTest {
    @TempDir Path directory;
    @Test void rejectsInternalAndUntrustedTargetsWithoutFetching() {
        var storage = new AppIconStorageService(directory, 1024);
        for (String url : List.of("http://127.0.0.1/icon", "https://localhost/icon", "https://[::1]/icon",
                "http://169.254.169.254/latest/meta-data/", "https://mzstatic.com.evil.example/icon",
                "https://evil.example/mzstatic.com/icon", "https://mzstatic.com@127.0.0.1/icon",
                "https://is1-ssl.mzstatic.com:8080/icon", "file:///etc/passwd")) {
            assertThat(storage.storeRemote(url)).isEmpty();
        }
        assertThat(directory.toFile().list()).isEmpty();
    }
    @Test void cancelsOversizedStreamingBodyBeforeBufferingIt() {
        var body = new AppIconStorageService.LimitedBodySubscriber(5);
        var subscription = mock(Flow.Subscription.class);
        body.onSubscribe(subscription);
        body.onNext(List.of(ByteBuffer.wrap(new byte[3])));
        body.onNext(List.of(ByteBuffer.wrap(new byte[3])));
        verify(subscription).cancel();
        assertThat(body.getBody().toCompletableFuture()).isCompletedExceptionally();
    }
    @Test void acceptsResponseExactlyAtLimit() {
        var body = new AppIconStorageService.LimitedBodySubscriber(5);
        body.onSubscribe(mock(Flow.Subscription.class));
        body.onNext(List.of(ByteBuffer.wrap(new byte[] {1,2}), ByteBuffer.wrap(new byte[] {3,4,5})));
        body.onComplete();
        assertThat(body.getBody().toCompletableFuture().join()).containsExactly(1,2,3,4,5);
    }
}
