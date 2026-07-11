package com.youou.aso.modules.appmanagement.api;

import com.youou.aso.common.api.ApiResponse;
import com.youou.aso.common.api.PageResult;
import com.youou.aso.common.error.BusinessException;
import com.youou.aso.common.error.ErrorCode;
import com.youou.aso.modules.account.domain.AccountType;
import com.youou.aso.modules.account.service.AuthenticatedAccount;
import com.youou.aso.modules.appmanagement.domain.StoreType;
import com.youou.aso.modules.appmanagement.dto.CreateCustomerAppCommand;
import com.youou.aso.modules.appmanagement.dto.CreateManualCustomerAppCommand;
import com.youou.aso.modules.appmanagement.dto.CustomerAppQuery;
import com.youou.aso.modules.appmanagement.dto.CustomerAppResult;
import com.youou.aso.modules.appmanagement.dto.SearchStoreAppCommand;
import com.youou.aso.modules.appmanagement.service.CustomerAppService;
import com.youou.aso.modules.appmanagement.service.StoreAppSearchResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/customer/apps")
public class CustomerAppController {
    private final CustomerAppService customerAppService;

    public CustomerAppController(CustomerAppService customerAppService) {
        this.customerAppService = customerAppService;
    }

    @GetMapping
    public ApiResponse<?> list(
            @AuthenticationPrincipal AuthenticatedAccount account,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) StoreType storeType,
            @RequestParam(required = false) String regionCode,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize
    ) {
        ensureCustomer(account);
        CustomerAppQuery query = new CustomerAppQuery(keyword, storeType, regionCode, null);
        if (page != null || pageSize != null) {
            PageResult<CustomerAppResult> result = customerAppService.pageByCustomer(account.accountId(), query, page, pageSize);
            return ApiResponse.ok(result);
        }
        return ApiResponse.ok(customerAppService.listByCustomer(
                account.accountId(),
                query
        ));
    }

    @GetMapping("/search")
    public ApiResponse<List<StoreAppSearchResult>> search(
            @AuthenticationPrincipal AuthenticatedAccount account,
            @RequestParam StoreType storeType,
            @RequestParam String regionCode,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "10") int limit
    ) {
        ensureCustomer(account);
        return ApiResponse.ok(customerAppService.search(new SearchStoreAppCommand(
                storeType,
                regionCode,
                keyword,
                limit
        )));
    }

    @PostMapping
    public ApiResponse<CustomerAppResult> create(
            @AuthenticationPrincipal AuthenticatedAccount account,
            @Valid @RequestBody CreateCustomerAppRequest request
    ) {
        ensureCustomer(account);
        if (request.appName() != null && !request.appName().trim().isBlank()) {
            return ApiResponse.ok(customerAppService.createManual(
                    account.accountId(),
                    new CreateManualCustomerAppCommand(
                            request.storeType(),
                            request.regionCode(),
                            request.appIdentifier(),
                            request.appName(),
                            request.appIconUrl(),
                            request.category()
                    )
            ));
        }
        return ApiResponse.ok(customerAppService.create(
                account.accountId(),
                new CreateCustomerAppCommand(
                        request.storeType(),
                        request.regionCode(),
                        request.appIdentifier(),
                        request.category()
                )
        ));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(
            @AuthenticationPrincipal AuthenticatedAccount account,
            @PathVariable Long id
    ) {
        ensureCustomer(account);
        customerAppService.disable(account.accountId(), id);
        return ApiResponse.ok(null);
    }

    private void ensureCustomer(AuthenticatedAccount account) {
        if (account == null || !AccountType.CUSTOMER.name().equals(account.accountType())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    public record CreateCustomerAppRequest(
            @NotNull
            StoreType storeType,

            @NotBlank
            @Size(max = 16)
            String regionCode,

            @NotBlank
            @Size(max = 255)
            String appIdentifier,

            @Size(max = 128)
            String category,

            @Size(max = 255)
            String appName,

            @Size(max = 500)
            String appIconUrl
    ) {
    }
}
