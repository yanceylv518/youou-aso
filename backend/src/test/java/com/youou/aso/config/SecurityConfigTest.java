package com.youou.aso.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityConfigTest {
    @TempDir
    Path tempDir;

    @Test
    void resolveJwtSecretUsesConfiguredSecretWhenPresent() {
        String configuredSecret = "01234567890123456789012345678901";

        String resolved = SecurityConfig.resolveJwtSecret(configuredSecret, tempDir.resolve("jwt-secret"));

        assertThat(resolved).isEqualTo(configuredSecret);
        assertThat(Files.exists(tempDir.resolve("jwt-secret"))).isFalse();
    }

    @Test
    void resolveJwtSecretPersistsGeneratedSecretAcrossRestarts() {
        Path secretFile = tempDir.resolve("jwt-secret");

        String first = SecurityConfig.resolveJwtSecret("", secretFile);
        String second = SecurityConfig.resolveJwtSecret("", secretFile);

        assertThat(first).hasSizeGreaterThanOrEqualTo(32);
        assertThat(second).isEqualTo(first);
        assertThat(Files.exists(secretFile)).isTrue();
    }
}
