package com.youou.aso.config;

import com.youou.aso.modules.appmanagement.service.AppIconStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class UploadResourceConfig implements WebMvcConfigurer {
    private final Path appIconDir;

    public UploadResourceConfig(
            @Value("${youou.upload.app-icon-dir:${user.home}/.youou-aso/uploads/app-icons}") String appIconDir
    ) {
        this.appIconDir = Path.of(appIconDir).toAbsolutePath().normalize();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(AppIconStorageService.PUBLIC_PATH_PREFIX + "**")
                .addResourceLocations(appIconDir.toUri().toString());
    }
}
