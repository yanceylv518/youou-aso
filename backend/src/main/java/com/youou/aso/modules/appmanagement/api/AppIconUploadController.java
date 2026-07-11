package com.youou.aso.modules.appmanagement.api;

import com.youou.aso.common.api.ApiResponse;
import com.youou.aso.modules.account.service.AuthenticatedAccount;
import com.youou.aso.modules.appmanagement.dto.AppIconUploadResult;
import com.youou.aso.modules.appmanagement.service.AppIconStorageService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class AppIconUploadController {
    private final AppIconStorageService appIconStorageService;

    public AppIconUploadController(AppIconStorageService appIconStorageService) {
        this.appIconStorageService = appIconStorageService;
    }

    @PostMapping("/api/app-icons")
    public ApiResponse<AppIconUploadResult> upload(
            @AuthenticationPrincipal AuthenticatedAccount account,
            @RequestPart("file") MultipartFile file
    ) {
        return ApiResponse.ok(new AppIconUploadResult(appIconStorageService.store(file)));
    }
}
