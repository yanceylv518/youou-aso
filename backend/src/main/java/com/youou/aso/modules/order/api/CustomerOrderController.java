package com.youou.aso.modules.order.api;

import com.youou.aso.common.api.ApiResponse;
import com.youou.aso.common.api.PageResult;
import com.youou.aso.common.error.BusinessException;
import com.youou.aso.common.error.ErrorCode;
import com.youou.aso.modules.account.domain.AccountType;
import com.youou.aso.modules.account.service.AuthenticatedAccount;
import com.youou.aso.modules.appmanagement.domain.StoreType;
import com.youou.aso.modules.order.domain.OrderStatus;
import com.youou.aso.modules.order.domain.OrderType;
import com.youou.aso.modules.order.dto.CreateOrderCommand;
import com.youou.aso.modules.order.dto.OrderQuery;
import com.youou.aso.modules.order.dto.OrderResult;
import com.youou.aso.modules.order.service.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/customer/orders")
public class CustomerOrderController {
    private final OrderService orderService;

    public CustomerOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ApiResponse<?> list(
            @AuthenticationPrincipal AuthenticatedAccount account,
            @RequestParam(required = false) StoreType storeType,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long customerAppId,
            @RequestParam(required = false) String regionCode,
            @RequestParam(required = false) OrderType orderType,
            @RequestParam(required = false) Boolean specialOrder,
            @RequestParam(required = false) LocalDate orderDateFrom,
            @RequestParam(required = false) LocalDate orderDateTo,
            @RequestParam(required = false) LocalDate createdDateFrom,
            @RequestParam(required = false) LocalDate createdDateTo,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize
    ) {
        ensureCustomer(account);
        OrderQuery query = new OrderQuery(
                storeType,
                status,
                keyword,
                null,
                customerAppId,
                regionCode,
                orderType,
                specialOrder,
                orderDateFrom,
                orderDateTo,
                createdDateFrom,
                createdDateTo
        );
        if (page != null || pageSize != null) {
            return ApiResponse.ok(mapPage(orderService.pageCustomerOrders(account.accountId(), query, page, pageSize)));
        }
        return ApiResponse.ok(orderService.listCustomerOrders(account.accountId(), query)
                .stream()
                .map(OrderResult::from)
                .toList());
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderResult> detail(
            @AuthenticationPrincipal AuthenticatedAccount account,
            @PathVariable Long id
    ) {
        ensureCustomer(account);
        return ApiResponse.ok(OrderResult.from(orderService.getCustomerOrder(account.accountId(), id)));
    }

    @PostMapping
    public ApiResponse<OrderResult> create(
            @AuthenticationPrincipal AuthenticatedAccount account,
            @Valid @RequestBody CreateOrderRequest request
    ) {
        ensureCustomer(account);
        return ApiResponse.ok(OrderResult.from(orderService.createCustomerOrder(account.accountId(), toCommand(request))));
    }

    @PostMapping("/{id}/resubmit")
    public ApiResponse<OrderResult> resubmit(
            @AuthenticationPrincipal AuthenticatedAccount account,
            @PathVariable Long id,
            @Valid @RequestBody CreateOrderRequest request
    ) {
        ensureCustomer(account);
        return ApiResponse.ok(OrderResult.from(orderService.resubmitEditableOrder(account.accountId(), id, toCommand(request))));
    }

    @PostMapping("/{id}/pay")
    public ApiResponse<OrderResult> pay(
            @AuthenticationPrincipal AuthenticatedAccount account,
            @PathVariable Long id
    ) {
        ensureCustomer(account);
        return ApiResponse.ok(OrderResult.from(orderService.payPendingPaymentOrder(account.accountId(), id)));
    }

    private CreateOrderCommand toCommand(CreateOrderRequest request) {
        return new CreateOrderCommand(
                request.customerAppId(),
                request.regionCode(),
                request.orderType(),
                request.startDate(),
                request.endDate(),
                request.executionHours(),
                request.keywords(),
                request.keywordItems(),
                request.regionItems(),
                request.reviewDetails(),
                request.dailyDownloadCount(),
                request.rating5Count(),
                request.rating4Count(),
                request.review5Count(),
                request.review4Count(),
                request.orderModuleId(),
                request.scheduledStartAt(),
                request.startImmediately()
        );
    }

    private void ensureCustomer(AuthenticatedAccount account) {
        if (account == null || !AccountType.CUSTOMER.name().equals(account.accountType())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    private PageResult<OrderResult> mapPage(PageResult<com.youou.aso.modules.order.domain.AsoOrder> page) {
        return new PageResult<>(
                page.items().stream().map(OrderResult::from).toList(),
                page.page(),
                page.pageSize(),
                page.total()
        );
    }

    public record CreateOrderRequest(
            @NotNull
            Long customerAppId,

            String regionCode,

            @NotNull
            OrderType orderType,

            @NotNull
            LocalDate startDate,

            @NotNull
            LocalDate endDate,

            Integer executionHours,
            List<String> keywords,
            List<CreateOrderCommand.KeywordQuantity> keywordItems,
            List<CreateOrderCommand.RegionOrderItem> regionItems,
            List<CreateOrderCommand.ReviewDetail> reviewDetails,
            Integer dailyDownloadCount,
            Integer rating5Count,
            Integer rating4Count,
            Integer review5Count,
            Integer review4Count,
            @NotNull Long orderModuleId,
            LocalDateTime scheduledStartAt,
            Boolean startImmediately
    ) {
    }
}
