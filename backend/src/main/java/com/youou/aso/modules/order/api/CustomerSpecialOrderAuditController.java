package com.youou.aso.modules.order.api;

import com.youou.aso.common.api.ApiResponse;
import com.youou.aso.common.api.PageResult;
import com.youou.aso.common.error.BusinessException;
import com.youou.aso.common.error.ErrorCode;
import com.youou.aso.modules.account.domain.AccountType;
import com.youou.aso.modules.account.service.AuthenticatedAccount;
import com.youou.aso.modules.order.domain.OrderType;
import com.youou.aso.modules.order.dto.OrderResult;
import com.youou.aso.modules.order.dto.SpecialOrderAuditResult;
import com.youou.aso.modules.order.dto.SubmitSpecialAuditCommand;
import com.youou.aso.modules.order.service.SpecialOrderAuditService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/customer/special-order-audits")
public class CustomerSpecialOrderAuditController {
    private final SpecialOrderAuditService specialOrderAuditService;

    public CustomerSpecialOrderAuditController(SpecialOrderAuditService specialOrderAuditService) {
        this.specialOrderAuditService = specialOrderAuditService;
    }

    @GetMapping
    public ApiResponse<?> list(
            @AuthenticationPrincipal AuthenticatedAccount account,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize
    ) {
        ensureCustomer(account);
        if (page != null || pageSize != null) {
            PageResult<SpecialOrderAuditResult> result = mapPage(specialOrderAuditService.pageCustomerAudits(
                    account.accountId(),
                    page,
                    pageSize
            ));
            return ApiResponse.ok(result);
        }
        return ApiResponse.ok(specialOrderAuditService.listCustomerAudits(account.accountId())
                .stream()
                .map(SpecialOrderAuditResult::from)
                .toList());
    }

    @PostMapping
    public ApiResponse<SpecialOrderAuditResult> submit(
            @AuthenticationPrincipal AuthenticatedAccount account,
            @Valid @RequestBody SubmitSpecialAuditRequest request
    ) {
        ensureCustomer(account);
        return ApiResponse.ok(SpecialOrderAuditResult.from(specialOrderAuditService.submitCustomerAudit(
                account.accountId(),
                new SubmitSpecialAuditCommand(
                        request.customerAppId(),
                        request.regionCode(),
                        request.orderType(),
                        request.requestedContent(),
                        request.contactType(),
                        request.contactValue(),
                        request.items()
                )
        )));
    }

    @PostMapping("/{id}/submit")
    public ApiResponse<OrderResult> submitApproved(
            @AuthenticationPrincipal AuthenticatedAccount account,
            @PathVariable Long id
    ) {
        ensureCustomer(account);
        return ApiResponse.ok(OrderResult.from(specialOrderAuditService.submitApprovedAudit(account.accountId(), id)));
    }

    private void ensureCustomer(AuthenticatedAccount account) {
        if (account == null || !AccountType.CUSTOMER.name().equals(account.accountType())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    private PageResult<SpecialOrderAuditResult> mapPage(PageResult<com.youou.aso.modules.order.domain.SpecialOrderAudit> page) {
        return new PageResult<>(
                page.items().stream().map(SpecialOrderAuditResult::from).toList(),
                page.page(),
                page.pageSize(),
                page.total()
        );
    }

    public record SubmitSpecialAuditRequest(
            @NotNull
            Long customerAppId,

            String regionCode,

            @NotNull
            OrderType orderType,

            String requestedContent,

            String contactType,
            String contactValue,

            List<SubmitSpecialAuditCommand.AuditItem> items
    ) {
        public SubmitSpecialAuditRequest(Long customerAppId, OrderType orderType, String requestedContent) {
            this(customerAppId, null, orderType, requestedContent, null, null, null);
        }
    }
}
