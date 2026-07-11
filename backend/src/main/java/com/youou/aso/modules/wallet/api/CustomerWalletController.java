package com.youou.aso.modules.wallet.api;

import com.youou.aso.common.api.ApiResponse;
import com.youou.aso.common.error.BusinessException;
import com.youou.aso.common.error.ErrorCode;
import com.youou.aso.modules.account.domain.AccountType;
import com.youou.aso.modules.account.service.AuthenticatedAccount;
import com.youou.aso.modules.order.domain.OrderType;
import com.youou.aso.modules.wallet.domain.WalletDirection;
import com.youou.aso.modules.wallet.dto.WalletOverviewResult;
import com.youou.aso.modules.wallet.dto.WalletTransactionTypeConfigResult;
import com.youou.aso.modules.wallet.dto.WalletTransactionResult;
import com.youou.aso.modules.wallet.domain.WalletTransactionType;
import com.youou.aso.modules.wallet.service.WalletQueryService;
import com.youou.aso.modules.wallet.service.WalletTransactionTypeConfigService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/customer/wallet")
public class CustomerWalletController {
    private final WalletQueryService walletQueryService;
    private final WalletTransactionTypeConfigService typeConfigService;

    public CustomerWalletController(
            WalletQueryService walletQueryService,
            WalletTransactionTypeConfigService typeConfigService
    ) {
        this.walletQueryService = walletQueryService;
        this.typeConfigService = typeConfigService;
    }

    @GetMapping
    public ApiResponse<WalletOverviewResult> overview(@AuthenticationPrincipal AuthenticatedAccount account) {
        ensureCustomer(account);
        return ApiResponse.ok(walletQueryService.getCustomerWallet(account.accountId()));
    }

    @GetMapping("/transactions")
    public ApiResponse<?> transactions(
            @AuthenticationPrincipal AuthenticatedAccount account,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) WalletTransactionType transactionType,
            @RequestParam(required = false) WalletDirection direction,
            @RequestParam(required = false) OrderType orderType,
            @RequestParam(required = false) LocalDate createdDateFrom,
            @RequestParam(required = false) LocalDate createdDateTo,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize
    ) {
        ensureCustomer(account);
        if (page != null || pageSize != null) {
            return ApiResponse.ok(walletQueryService.pageCustomerTransactions(
                    account.accountId(),
                    transactionType,
                    direction,
                    orderType,
                    createdDateFrom,
                    createdDateTo,
                    page,
                    pageSize
            ));
        }
        return ApiResponse.ok(walletQueryService.listCustomerTransactions(account.accountId(), limit));
    }

    @GetMapping("/transaction-type-configs")
    public ApiResponse<List<WalletTransactionTypeConfigResult>> transactionTypeConfigs(
            @AuthenticationPrincipal AuthenticatedAccount account
    ) {
        ensureCustomer(account);
        return ApiResponse.ok(typeConfigService.listConfigs());
    }

    private void ensureCustomer(AuthenticatedAccount account) {
        if (account == null || !AccountType.CUSTOMER.name().equals(account.accountType())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }
}
