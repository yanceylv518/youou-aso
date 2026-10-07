package com.youou.aso.modules.order.api;

import com.youou.aso.common.api.ApiResponse;
import com.youou.aso.common.api.PageResult;
import com.youou.aso.common.error.BusinessException;
import com.youou.aso.common.error.ErrorCode;
import com.youou.aso.modules.account.domain.AccountType;
import com.youou.aso.modules.account.service.AuthenticatedAccount;
import com.youou.aso.modules.appmanagement.domain.StoreType;
import com.youou.aso.modules.order.domain.AsoOrder;
import com.youou.aso.modules.order.domain.OrderStatus;
import com.youou.aso.modules.order.domain.OrderType;
import com.youou.aso.modules.order.dto.AdminCreateOrderResult;
import com.youou.aso.modules.order.dto.CreateOrderCommand;
import com.youou.aso.modules.order.dto.OrderQuery;
import com.youou.aso.modules.order.dto.OrderResult;
import com.youou.aso.modules.order.service.OrderService;
import com.youou.aso.modules.order.service.SpecialOrderAuditService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {
    private final OrderService orderService;
    private final SpecialOrderAuditService specialOrderAuditService;

    public AdminOrderController(OrderService orderService, SpecialOrderAuditService specialOrderAuditService) {
        this.orderService = orderService;
        this.specialOrderAuditService = specialOrderAuditService;
    }

    @PreAuthorize("@perm.hasAny('order:confirm', 'order:execute', 'order:pause', 'order:resume', 'order:cancel', 'order:export') || @perm.hasMenu('orders') || @perm.hasMenu('orderExecution')")
    @GetMapping
    public ApiResponse<?> list(
            @AuthenticationPrincipal AuthenticatedAccount account,
            @RequestParam(required = false) StoreType storeType,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long customerId,
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
        ensureAdmin(account);
        OrderQuery query = new OrderQuery(
                storeType,
                status,
                keyword,
                customerId,
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
            return ApiResponse.ok(mapPage(orderService.pageAdminOrders(query, page, pageSize)));
        }
        return ApiResponse.ok(orderService.listAdminOrders(query)
                .stream()
                .map(OrderResult::from)
                .toList());
    }

    @PreAuthorize("@perm.hasAny('order:confirm', 'order:execute', 'order:pause', 'order:resume', 'order:cancel', 'order:export') || @perm.hasMenu('orders') || @perm.hasMenu('orderExecution')")
    @GetMapping("/{id}")
    public ApiResponse<OrderResult> detail(
            @AuthenticationPrincipal AuthenticatedAccount account,
            @PathVariable Long id
    ) {
        ensureAdmin(account);
        return ApiResponse.ok(OrderResult.from(orderService.getAdminOrder(id)));
    }

    @PreAuthorize("@perm.has('order:confirm')")
    @PostMapping("/{id}/confirm")
    public ApiResponse<OrderResult> confirm(
            @AuthenticationPrincipal AuthenticatedAccount account,
            @PathVariable Long id
    ) {
        ensureAdmin(account);
        return ApiResponse.ok(OrderResult.from(orderService.confirmOrder(id, account.accountId())));
    }

    @PreAuthorize("@perm.has('order:confirm')")
    @PostMapping("/batch-confirm")
    public ApiResponse<List<OrderResult>> batchConfirm(
            @AuthenticationPrincipal AuthenticatedAccount account,
            @RequestBody BatchExecuteRequest request
    ) {
        ensureAdmin(account);
        return ApiResponse.ok(orderService.confirmOrders(request.orderIds(), account.accountId())
                .stream()
                .map(OrderResult::from)
                .toList());
    }

    @PreAuthorize("@perm.has('order:create')")
    @PostMapping("/create-for-customer")
    public ApiResponse<AdminCreateOrderResult> createForCustomer(
            @AuthenticationPrincipal AuthenticatedAccount account,
            @Valid @RequestBody AdminCreateOrderRequest request
    ) {
        ensureAdmin(account);
        if (request.orderType() == OrderType.RANK_GUARANTEE || request.orderType() == OrderType.CHART_RANK_GUARANTEE || request.orderType() == OrderType.KEYWORD_COVERAGE) {
            SpecialOrderAuditService.AdminSpecialOrderSubmission result = specialOrderAuditService.submitAdminSpecialOrder(
                    request.customerId(),
                    account.accountId(),
                    request.customerAppId(),
                    request.regionCode(),
                    request.orderType(),
                    request.specialItems(),
                    request.contactType(),
                    request.contactValue(),
                    request.specialAmount(), request.orderModuleId()
            );
            if (result.paid()) {
                return ApiResponse.ok(AdminCreateOrderResult.paid(result.order()));
            }
            return ApiResponse.ok(AdminCreateOrderResult.waitPayment(result.audit()));
        }
        AsoOrder order = orderService.createAdminOrderForCustomer(
                request.customerId(),
                account.accountId(),
                new CreateOrderCommand(
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
                        request.scheduledStartAt()
                )
        );
        if (OrderStatus.PENDING_PAYMENT.equals(order.getStatus())) {
            return ApiResponse.ok(AdminCreateOrderResult.waitPayment(order));
        }
        return ApiResponse.ok(AdminCreateOrderResult.paid(order));
    }

    @PreAuthorize("@perm.has('order:create')")
    @PostMapping("/{id}/edit")
    public ApiResponse<OrderResult> edit(
            @AuthenticationPrincipal AuthenticatedAccount account,
            @PathVariable Long id,
            @Valid @RequestBody AdminCreateOrderRequest request
    ) {
        ensureAdmin(account);
        if (request.orderType() == OrderType.RANK_GUARANTEE || request.orderType() == OrderType.CHART_RANK_GUARANTEE || request.orderType() == OrderType.KEYWORD_COVERAGE) {
            return ApiResponse.ok(OrderResult.from(specialOrderAuditService.editSubmittedOrder(id, request.customerId(),
                    account.accountId(), request.customerAppId(), request.orderType(), request.specialItems())));
        }
        return ApiResponse.ok(OrderResult.from(orderService.editAdminOrder(request.customerId(), id, account.accountId(),
                new CreateOrderCommand(
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
                        request.scheduledStartAt()
                ))));
    }

    @PreAuthorize("@perm.has('order:execute')")
    @PostMapping("/{id}/execute")
    public ApiResponse<OrderResult> execute(
            @AuthenticationPrincipal AuthenticatedAccount account,
            @PathVariable Long id
    ) {
        ensureAdmin(account);
        return ApiResponse.ok(OrderResult.from(orderService.executeOrder(id, account.accountId())));
    }

    @PreAuthorize("@perm.has('order:pause')")
    @PostMapping("/{id}/pause")
    public ApiResponse<OrderResult> pause(
            @AuthenticationPrincipal AuthenticatedAccount account,
            @PathVariable Long id
    ) {
        ensureAdmin(account);
        return ApiResponse.ok(OrderResult.from(orderService.pauseOrder(id, account.accountId())));
    }

    @PreAuthorize("@perm.has('order:pause')")
    @PostMapping("/{id}/paused-items")
    public ApiResponse<OrderResult> updatePausedItems(
            @AuthenticationPrincipal AuthenticatedAccount account,
            @PathVariable Long id,
            @Valid @RequestBody UpdatePausedOrderRequest request
    ) {
        ensureAdmin(account);
        return ApiResponse.ok(OrderResult.from(orderService.updatePausedOrder(
                id,
                account.accountId(),
                request.items().stream()
                        .map(item -> new OrderService.PausedItemEdit(
                                item.itemId(),
                                item.quantity(),
                                item.completedQuantity()
                        ))
                        .toList()
        )));
    }
    @PreAuthorize("@perm.has('order:pause')")
    @PostMapping("/{id}/completed-items")
    public ApiResponse<OrderResult> adjustCompletedItems(
            @AuthenticationPrincipal AuthenticatedAccount account,
            @PathVariable Long id,
            @Valid @RequestBody AdjustCompletedOrderRequest request
    ) {
        ensureAdmin(account);
        return ApiResponse.ok(OrderResult.from(orderService.adjustCompletedOrder(id, account.accountId(),
                request.items().stream().map(item -> new OrderService.CompletedItemEdit(
                        item.itemId(), item.completedQuantity())).toList(), request.reason())));
    }

    @PreAuthorize("@perm.has('order:pause')")
    @PostMapping("/{id}/close")
    public ApiResponse<OrderResult> closePausedOrder(
            @AuthenticationPrincipal AuthenticatedAccount account,
            @PathVariable Long id,
            @Valid @RequestBody UpdatePausedOrderRequest request
    ) {
        ensureAdmin(account);
        return ApiResponse.ok(OrderResult.from(orderService.closePausedOrder(id, account.accountId(),
                request.items().stream().map(item -> new OrderService.PausedItemEdit(
                        item.itemId(), item.quantity(), item.quantity())).toList())));
    }

    @PreAuthorize("@perm.has('order:resume')")
    @PostMapping("/{id}/resume")
    public ApiResponse<OrderResult> resume(
            @AuthenticationPrincipal AuthenticatedAccount account,
            @PathVariable Long id
    ) {
        ensureAdmin(account);
        return ApiResponse.ok(OrderResult.from(orderService.resumeOrder(id, account.accountId())));
    }

    @PreAuthorize("@perm.has('order:cancel')")
    @PostMapping("/{id}/cancel")
    public ApiResponse<OrderResult> cancel(
            @AuthenticationPrincipal AuthenticatedAccount account,
            @PathVariable Long id,
            @RequestBody(required = false) CancelOrderRequest request
    ) {
        ensureAdmin(account);
        String reason = request == null ? null : request.reason();
        return ApiResponse.ok(OrderResult.from(orderService.cancelOrder(id, account.accountId(), reason)));
    }

    @PreAuthorize("@perm.has('order:execute')")
    @PostMapping("/batch-execute")
    public ApiResponse<List<OrderResult>> batchExecute(
            @AuthenticationPrincipal AuthenticatedAccount account,
            @RequestBody BatchExecuteRequest request
    ) {
        ensureAdmin(account);
        return ApiResponse.ok(orderService.executeOrders(request.orderIds(), account.accountId())
                .stream()
                .map(OrderResult::from)
                .toList());
    }

    @PreAuthorize("@perm.has('order:pause')")
    @PostMapping("/batch-pause")
    public ApiResponse<List<OrderResult>> batchPause(
            @AuthenticationPrincipal AuthenticatedAccount account,
            @RequestBody BatchExecuteRequest request
    ) {
        ensureAdmin(account);
        return ApiResponse.ok(orderService.pauseOrders(request.orderIds(), account.accountId())
                .stream()
                .map(OrderResult::from)
                .toList());
    }

    private void ensureAdmin(AuthenticatedAccount account) {
        if (account == null || !AccountType.ADMIN.name().equals(account.accountType())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    private PageResult<OrderResult> mapPage(PageResult<AsoOrder> page) {
        return new PageResult<>(
                page.items().stream().map(OrderResult::from).toList(),
                page.page(),
                page.pageSize(),
                page.total()
        );
    }

    public record AdjustCompletedOrderRequest(@NotNull @Valid List<PausedItemEditRequest> items,
            @jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Size(max = 500) String reason) {}

    public record UpdatePausedOrderRequest(@NotNull List<PausedItemEditRequest> items) {
    }

    public record PausedItemEditRequest(@NotNull Long itemId, @NotNull Integer quantity, @NotNull Integer completedQuantity) {
    }

    public record BatchExecuteRequest(List<Long> orderIds) {
    }

    public record CancelOrderRequest(String reason) {
    }

    public record AdminCreateOrderRequest(
            @NotNull
            Long customerId,

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
            String contactType,
            String contactValue,
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
            List<com.youou.aso.modules.order.dto.SubmitSpecialAuditCommand.AuditItem> specialItems,
            java.math.BigDecimal specialAmount,
            java.time.LocalDateTime scheduledStartAt
    ) {
        public AdminCreateOrderRequest(
            @NotNull
            Long customerId,

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
            String contactType,
            String contactValue,
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
            List<com.youou.aso.modules.order.dto.SubmitSpecialAuditCommand.AuditItem> specialItems,
            java.math.BigDecimal specialAmount) {
            this(customerId, customerAppId, regionCode, orderType, startDate, endDate, executionHours, contactType, contactValue, keywords, keywordItems, regionItems, reviewDetails, dailyDownloadCount, rating5Count, rating4Count, review5Count, review4Count, orderModuleId, specialItems, specialAmount, null);
        }

        public AdminCreateOrderRequest(
                Long customerId,
                Long customerAppId,
                String regionCode,
                OrderType orderType,
                LocalDate startDate,
                LocalDate endDate,
                Integer executionHours,
                String contactType,
                String contactValue,
                List<String> keywords,
                List<CreateOrderCommand.KeywordQuantity> keywordItems,
                List<CreateOrderCommand.RegionOrderItem> regionItems,
                List<CreateOrderCommand.ReviewDetail> reviewDetails,
                Integer dailyDownloadCount,
                Integer rating5Count,
                Integer rating4Count,
                Integer review5Count,
                Integer review4Count,
                List<com.youou.aso.modules.order.dto.SubmitSpecialAuditCommand.AuditItem> specialItems,
                java.math.BigDecimal specialAmount
        ) {
            this(customerId, customerAppId, regionCode, orderType, startDate, endDate, executionHours,
                    contactType, contactValue, keywords, keywordItems, regionItems, reviewDetails,
                    dailyDownloadCount, rating5Count, rating4Count, review5Count, review4Count,
                    null, specialItems, specialAmount);
        }

        public AdminCreateOrderRequest(
                Long customerId,
                Long customerAppId,
                OrderType orderType,
                java.time.LocalDate startDate,
                java.time.LocalDate endDate,
                Integer executionHours,
                List<String> keywords,
                List<CreateOrderCommand.KeywordQuantity> keywordItems,
                Integer dailyDownloadCount,
                Integer rating5Count,
                Integer rating4Count,
                Integer review5Count,
                Integer review4Count
        ) {
            this(
                    customerId,
                    customerAppId,
                    null,
                    orderType,
                    startDate,
                    endDate,
                    executionHours,
                    null,
                    null,
                    keywords,
                    keywordItems,
                    null,
                    null,
                    dailyDownloadCount,
                    rating5Count,
                    rating4Count,
                    review5Count,
                    review4Count,
                    null,
                    null,
                    null
            );
        }

        public AdminCreateOrderRequest(
                Long customerId,
                Long customerAppId,
                String regionCode,
                OrderType orderType,
                java.time.LocalDate startDate,
                java.time.LocalDate endDate,
                Integer executionHours,
                List<String> keywords,
                List<CreateOrderCommand.KeywordQuantity> keywordItems,
                Integer dailyDownloadCount,
                Integer rating5Count,
                Integer rating4Count,
                Integer review5Count,
                Integer review4Count,
                java.math.BigDecimal specialAmount
        ) {
            this(
                    customerId,
                    customerAppId,
                    regionCode,
                    orderType,
                    startDate,
                    endDate,
                    executionHours,
                    null,
                    null,
                    keywords,
                    keywordItems,
                    null,
                    null,
                    dailyDownloadCount,
                    rating5Count,
                    rating4Count,
                    review5Count,
                    review4Count,
                    null,
                    null,
                    specialAmount
            );
        }

        public AdminCreateOrderRequest(
                Long customerId,
                Long customerAppId,
                OrderType orderType,
                java.time.LocalDate startDate,
                java.time.LocalDate endDate,
                Integer executionHours,
                List<String> keywords,
                List<CreateOrderCommand.KeywordQuantity> keywordItems,
                List<CreateOrderCommand.RegionOrderItem> regionItems,
                Integer dailyDownloadCount,
                Integer rating5Count,
                Integer rating4Count,
                Integer review5Count,
                Integer review4Count
        ) {
            this(
                    customerId,
                    customerAppId,
                    null,
                    orderType,
                    startDate,
                    endDate,
                    executionHours,
                    null,
                    null,
                    keywords,
                    keywordItems,
                    regionItems,
                    null,
                    dailyDownloadCount,
                    rating5Count,
                    rating4Count,
                    review5Count,
                    review4Count,
                    null,
                    null,
                    null
            );
        }
    }
}
