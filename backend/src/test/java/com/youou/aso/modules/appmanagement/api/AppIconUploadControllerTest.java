package com.youou.aso.modules.appmanagement.api;

import com.youou.aso.common.error.BusinessException;
import com.youou.aso.common.error.ErrorCode;
import com.youou.aso.modules.account.domain.AccountType;
import com.youou.aso.modules.account.service.AuthenticatedAccount;
import com.youou.aso.modules.appmanagement.dto.AppIconUploadResult;
import com.youou.aso.modules.appmanagement.service.AppIconStorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AppIconUploadControllerTest {
    @TempDir
    Path uploadRoot;

    @Test
    void uploadsPngIconAndReturnsPublicUrl() {
        AppIconUploadController controller = controller();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "icon.png",
                "image/png",
                new byte[]{1, 2, 3}
        );

        AppIconUploadResult result = controller.upload(
                new AuthenticatedAccount(10L, AccountType.CUSTOMER.name(), "CUSTOMER"),
                file
        ).data();

        assertThat(result.url()).startsWith("/uploads/app-icons/");
        assertThat(result.url()).endsWith(".png");
        assertThat(Files.exists(uploadRoot.resolve(result.url().replace("/uploads/app-icons/", "")))).isTrue();
    }

    @Test
    void rejectsNonImageFile() {
        AppIconUploadController controller = controller();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "icon.txt",
                "text/plain",
                new byte[]{1, 2, 3}
        );

        assertThatThrownBy(() -> controller.upload(
                new AuthenticatedAccount(10L, AccountType.CUSTOMER.name(), "CUSTOMER"),
                file
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.FILE_TYPE_NOT_ALLOWED);
    }

    @Test
    void rejectsOversizedIcon() {
        AppIconUploadController controller = controller();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "icon.png",
                "image/png",
                new byte[1024 * 1024 + 1]
        );

        assertThatThrownBy(() -> controller.upload(
                new AuthenticatedAccount(1L, AccountType.ADMIN.name(), "ADMIN"),
                file
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }

    private AppIconUploadController controller() {
        return new AppIconUploadController(new AppIconStorageService(uploadRoot, 1024 * 1024L));
    }
}
