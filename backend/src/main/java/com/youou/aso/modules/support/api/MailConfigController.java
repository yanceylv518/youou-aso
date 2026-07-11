package com.youou.aso.modules.support.api;

import com.youou.aso.common.api.ApiResponse;
import com.youou.aso.common.error.BusinessException;
import com.youou.aso.common.error.ErrorCode;
import com.youou.aso.modules.account.domain.AccountType;
import com.youou.aso.modules.account.domain.AdminRole;
import com.youou.aso.modules.account.service.AuthenticatedAccount;
import com.youou.aso.modules.support.dto.MailConfigResult;
import com.youou.aso.modules.support.dto.UpdateMailConfigCommand;
import com.youou.aso.modules.support.service.MailConfigService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/support/mail")
public class MailConfigController {
    private final MailConfigService service;

    public MailConfigController(MailConfigService service) {
        this.service = service;
    }

    @PreAuthorize("@perm.hasMenu('system.mail')")
    @GetMapping
    public ApiResponse<MailConfigResult> getConfig(
            @AuthenticationPrincipal AuthenticatedAccount account
    ) {
        requireSuperAdmin(account);
        return ApiResponse.ok(service.getConfig());
    }

    @PreAuthorize("@perm.has('mail:update')")
    @PutMapping
    public ApiResponse<MailConfigResult> updateConfig(
            @AuthenticationPrincipal AuthenticatedAccount account,
            @Valid @RequestBody MailConfigRequest request
    ) {
        requireSuperAdmin(account);
        return ApiResponse.ok(service.updateConfig(new UpdateMailConfigCommand(
                request.smtpHost(),
                request.smtpPort(),
                request.username(),
                request.password(),
                request.keepExistingPassword(),
                request.fromAddress(),
                request.smtpAuth(),
                request.startTlsEnabled(),
                request.sslEnabled(),
                request.orderNotificationRecipients(),
                request.orderNotificationEnabled()
        )));
    }

    private void requireSuperAdmin(AuthenticatedAccount account) {
        if (account == null
                || !AccountType.ADMIN.name().equals(account.accountType())
                || !AdminRole.SUPER_ADMIN.name().equals(account.roleCode())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    public record MailConfigRequest(
            @Size(max = 255)
            String smtpHost,

            @Min(1)
            @Max(65535)
            Integer smtpPort,

            @Size(max = 255)
            String username,

            @Size(max = 512)
            String password,

            boolean keepExistingPassword,

            @Size(max = 255)
            String fromAddress,

            boolean smtpAuth,

            boolean startTlsEnabled,

            boolean sslEnabled,

            @Size(max = 1024)
            String orderNotificationRecipients,

            boolean orderNotificationEnabled
    ) {
    }
}
