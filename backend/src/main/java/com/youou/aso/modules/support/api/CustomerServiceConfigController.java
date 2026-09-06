package com.youou.aso.modules.support.api;

import com.youou.aso.common.api.ApiResponse;
import com.youou.aso.common.error.BusinessException;
import com.youou.aso.common.error.ErrorCode;
import com.youou.aso.modules.account.domain.AccountType;
import com.youou.aso.modules.account.service.AuthenticatedAccount;
import com.youou.aso.modules.support.dto.CustomerServiceConfigResult;
import com.youou.aso.modules.support.dto.UpdateCustomerServiceConfigCommand;
import com.youou.aso.modules.support.service.CustomerServiceConfigService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CustomerServiceConfigController {
    private final CustomerServiceConfigService service;

    public CustomerServiceConfigController(CustomerServiceConfigService service) {
        this.service = service;
    }

    @GetMapping("/customer/support/customer-service")
    public ApiResponse<CustomerServiceConfigResult> getCustomerConfig(
            @AuthenticationPrincipal AuthenticatedAccount account
    ) {
        requireAccountType(account, AccountType.CUSTOMER);
        return ApiResponse.ok(service.getConfig());
    }

    @PreAuthorize("@perm.hasMenu('system.customerService')")
    @GetMapping("/admin/support/customer-service")
    public ApiResponse<CustomerServiceConfigResult> getAdminConfig(
            @AuthenticationPrincipal AuthenticatedAccount account
    ) {
        requireAccountType(account, AccountType.ADMIN);
        return ApiResponse.ok(service.getConfig());
    }

    @PreAuthorize("@perm.has('customerService:update')")
    @PutMapping("/admin/support/customer-service")
    public ApiResponse<CustomerServiceConfigResult> updateAdminConfig(
            @AuthenticationPrincipal AuthenticatedAccount account,
            @Valid @RequestBody CustomerServiceConfigRequest request
    ) {
        requireAccountType(account, AccountType.ADMIN);
        return ApiResponse.ok(service.updateConfig(new UpdateCustomerServiceConfigCommand(
                request.serviceName(),
                request.qrCodeUrl(),
                request.contactHint(),
                request.email(),
                request.emailVisible(),
                request.phone(),
                request.phoneVisible(),
                request.teamsUrl(),
                request.telegramUrl(),
                request.telegramQrUrl(),
                request.telegramQrVisible(),
                request.wechatQrUrl(),
                request.wechatQrVisible(),
                request.whatsappUrl(),
                request.enabled()
        )));
    }

    private void requireAccountType(AuthenticatedAccount account, AccountType accountType) {
        if (account == null || !accountType.name().equals(account.accountType())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    public record CustomerServiceConfigRequest(
            @Size(max = 120)
            String serviceName,

            @Size(max = 1024)
            String qrCodeUrl,

            @Size(max = 512)
            String contactHint,

            @Size(max = 254)
            String email,

            boolean emailVisible,

            @Size(max = 40)
            String phone,

            boolean phoneVisible,

            @Size(max = 1024)
            String teamsUrl,

            @Size(max = 1024)
            String telegramUrl,

            @Size(max = 1024)
            String telegramQrUrl,

            boolean telegramQrVisible,

            @Size(max = 1024)
            String wechatQrUrl,

            boolean wechatQrVisible,

            @Size(max = 1024)
            String whatsappUrl,

            boolean enabled
    ) {
        public CustomerServiceConfigRequest(
                String serviceName,
                String qrCodeUrl,
                String contactHint,
                String email,
                String teamsUrl,
                String telegramUrl,
                String whatsappUrl,
                boolean enabled
        ) {
            this(serviceName, qrCodeUrl, contactHint, email, email != null && !email.isBlank(), null, false,
                    teamsUrl, telegramUrl, null, false, null, false, whatsappUrl, enabled);
        }
    }
}
