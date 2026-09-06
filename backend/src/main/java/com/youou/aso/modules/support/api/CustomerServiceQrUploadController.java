package com.youou.aso.modules.support.api;

import com.youou.aso.common.api.ApiResponse;
import com.youou.aso.modules.appmanagement.dto.AppIconUploadResult;
import com.youou.aso.modules.appmanagement.service.AppIconStorageService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class CustomerServiceQrUploadController {
    private final AppIconStorageService imageStorageService;

    public CustomerServiceQrUploadController(AppIconStorageService imageStorageService) {
        this.imageStorageService = imageStorageService;
    }

    @PreAuthorize("@perm.has('customerService:update')")
    @PostMapping("/api/admin/support/customer-service/qr-image")
    public ApiResponse<AppIconUploadResult> upload(@RequestPart("file") MultipartFile file) {
        return ApiResponse.ok(new AppIconUploadResult(imageStorageService.store(file)));
    }
}
