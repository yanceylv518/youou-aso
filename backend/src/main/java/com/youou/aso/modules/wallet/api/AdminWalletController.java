package com.youou.aso.modules.wallet.api;

import com.youou.aso.common.api.ApiResponse;
import com.youou.aso.common.error.BusinessException;
import com.youou.aso.common.error.ErrorCode;
import com.youou.aso.modules.account.domain.AccountType;
import com.youou.aso.modules.account.service.AuthenticatedAccount;
import com.youou.aso.modules.order.domain.OrderType;
import com.youou.aso.modules.wallet.domain.AdminWalletAdjustmentType;
import com.youou.aso.modules.wallet.dto.AdminRechargeCommand;
import com.youou.aso.modules.wallet.dto.AdminBalanceAdjustmentCommand;
import com.youou.aso.modules.wallet.dto.UpdateWalletTransactionTypeConfigCommand;
import com.youou.aso.modules.wallet.dto.WalletTransactionTypeConfigResult;
import com.youou.aso.modules.wallet.dto.WalletTransactionResult;
import com.youou.aso.modules.wallet.domain.WalletDirection;
import com.youou.aso.modules.wallet.domain.WalletTransactionType;
import com.youou.aso.modules.wallet.service.AdminWalletService;
import com.youou.aso.modules.wallet.service.WalletQueryService;
import com.youou.aso.modules.wallet.service.WalletTransactionTypeConfigService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/wallet")
public class AdminWalletController {
    private final WalletQueryService walletQueryService;
    private final AdminWalletService adminWalletService;
    private final WalletTransactionTypeConfigService typeConfigService;

    public AdminWalletController(
            WalletQueryService walletQueryService,
            AdminWalletService adminWalletService,
            WalletTransactionTypeConfigService typeConfigService
    ) {
        this.walletQueryService = walletQueryService;
        this.adminWalletService = adminWalletService;
        this.typeConfigService = typeConfigService;
    }

    @PreAuthorize("@perm.hasMenu('finance.transactions') || @perm.hasMenu('finance.recharges')")
    @GetMapping("/transactions")
    public ApiResponse<?> transactions(
            @AuthenticationPrincipal AuthenticatedAccount account,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) WalletTransactionType transactionType,
            @RequestParam(required = false) WalletDirection direction,
            @RequestParam(required = false) OrderType orderType,
            @RequestParam(required = false) LocalDate createdDateFrom,
            @RequestParam(required = false) LocalDate createdDateTo,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize
    ) {
        ensureAdmin(account);
        if (page != null || pageSize != null) {
            return ApiResponse.ok(walletQueryService.pageAdminTransactions(
                    customerId,
                    transactionType,
                    direction,
                    orderType,
                    createdDateFrom,
                    createdDateTo,
                    page,
                    pageSize
            ));
        }
        return ApiResponse.ok(walletQueryService.listAdminTransactions(customerId, transactionType, limit));
    }

    @PreAuthorize("@perm.has('wallet:recharge')")
    @PostMapping("/recharge")
    public ApiResponse<WalletTransactionResult> recharge(
            @AuthenticationPrincipal AuthenticatedAccount account,
            @Valid @RequestBody RechargeRequest request
    ) {
        ensureAdmin(account);
        return ApiResponse.ok(adminWalletService.recharge(new AdminRechargeCommand(
                request.customerId(),
                request.amount(),
                request.remark()
        )));
    }

    @PreAuthorize("@perm.has('wallet:adjust')")
    @PostMapping("/adjustments")
    public ApiResponse<WalletTransactionResult> adjustBalance(
            @AuthenticationPrincipal AuthenticatedAccount account,
            @Valid @RequestBody BalanceAdjustmentRequest request
    ) {
        ensureAdmin(account);
        return ApiResponse.ok(adminWalletService.adjustBalance(new AdminBalanceAdjustmentCommand(
                request.customerId(),
                request.adjustmentType(),
                request.amount(),
                request.remark()
        )));
    }

    @PreAuthorize("@perm.hasMenu('system.walletTypes')")
    @GetMapping("/transaction-type-configs")
    public ApiResponse<List<WalletTransactionTypeConfigResult>> transactionTypeConfigs(
            @AuthenticationPrincipal AuthenticatedAccount account
    ) {
        ensureAdmin(account);
        return ApiResponse.ok(typeConfigService.listConfigs());
    }

    @PreAuthorize("@perm.has('walletType:update')")
    @PutMapping("/transaction-type-configs")
    public ApiResponse<List<WalletTransactionTypeConfigResult>> updateTransactionTypeConfigs(
            @AuthenticationPrincipal AuthenticatedAccount account,
            @Valid @RequestBody UpdateTransactionTypeConfigsRequest request
    ) {
        ensureAdmin(account);
        return ApiResponse.ok(typeConfigService.updateConfigs(request.items().stream()
                .map(item -> new UpdateWalletTransactionTypeConfigCommand(
                        item.transactionType(),
                        item.displayNameZh(),
                        item.displayNameEn(),
                        item.displayNameRu(),
                        item.displayNamePt(),
                        item.displayNameEs()
                ))
                .toList()));
    }

    private void ensureAdmin(AuthenticatedAccount account) {
        if (account == null || !AccountType.ADMIN.name().equals(account.accountType())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    public record RechargeRequest(
            @NotNull
            Long customerId,

            @NotNull
            @DecimalMin(value = "0.00", inclusive = false)
            BigDecimal amount,

            String remark
    ) {
    }

    public record BalanceAdjustmentRequest(
            @NotNull
            Long customerId,

            @NotNull
            AdminWalletAdjustmentType adjustmentType,

            @NotNull
            @DecimalMin(value = "0.00", inclusive = false)
            BigDecimal amount,

            @NotBlank
            @Size(max = 200)
            String remark
    ) {
    }

    public record UpdateTransactionTypeConfigsRequest(
            @NotNull
            List<@Valid TransactionTypeConfigRequest> items
    ) {
    }

    public record TransactionTypeConfigRequest(
            @NotNull
            WalletTransactionType transactionType,

            @NotBlank
            @Size(max = 80)
            String displayNameZh,

            @NotBlank
            @Size(max = 120)
            String displayNameEn
            ,
            @NotBlank @Size(max = 120) String displayNameRu,
            @NotBlank @Size(max = 120) String displayNamePt,
            @NotBlank @Size(max = 120) String displayNameEs
    ) {
    }
}
