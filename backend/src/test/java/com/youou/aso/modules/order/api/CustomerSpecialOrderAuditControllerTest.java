package com.youou.aso.modules.order.api;

import com.youou.aso.common.error.BusinessException;
import com.youou.aso.common.error.ErrorCode;
import com.youou.aso.modules.account.domain.AccountType;
import com.youou.aso.modules.account.service.AuthenticatedAccount;
import com.youou.aso.modules.appmanagement.domain.StoreType;
import com.youou.aso.modules.order.domain.AsoOrder;
import com.youou.aso.modules.order.domain.OrderStatus;
import com.youou.aso.modules.order.domain.OrderType;
import com.youou.aso.modules.order.domain.SpecialAuditStatus;
import com.youou.aso.modules.order.domain.SpecialOrderAudit;
import com.youou.aso.modules.order.dto.OrderResult;
import com.youou.aso.modules.order.dto.SpecialOrderAuditResult;
import com.youou.aso.modules.order.dto.SubmitSpecialAuditCommand;
import com.youou.aso.modules.order.service.SpecialOrderAuditService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CustomerSpecialOrderAuditControllerTest {
    private final SpecialOrderAuditService service = mock(SpecialOrderAuditService.class);
    private final CustomerSpecialOrderAuditController controller = new CustomerSpecialOrderAuditController(service);

    @Test
    void customerCanSubmitSpecialAuditRequest() {
        SpecialOrderAudit audit = sampleAudit();
        when(service.submitCustomerAudit(
                10L,
                new SubmitSpecialAuditCommand(20L, OrderType.RANK_GUARANTEE, "Keep target rank")
        )).thenReturn(audit);

        SpecialOrderAuditResult result = controller.submit(
                new AuthenticatedAccount(10L, AccountType.CUSTOMER.name(), "CUSTOMER"),
                new CustomerSpecialOrderAuditController.SubmitSpecialAuditRequest(
                        20L,
                        OrderType.RANK_GUARANTEE,
                        "Keep target rank"
                )
        ).data();

        assertThat(result.status()).isEqualTo(SpecialAuditStatus.PENDING_REVIEW);
        verify(service).submitCustomerAudit(
                10L,
                new SubmitSpecialAuditCommand(20L, OrderType.RANK_GUARANTEE, "Keep target rank")
        );
    }

    @Test
    void customerCanPayApprovedAuditAndReceiveFormalOrder() {
        AsoOrder order = sampleOrder();
        when(service.submitApprovedAudit(10L, 30L)).thenReturn(order);

        OrderResult result = controller.submitApproved(
                new AuthenticatedAccount(10L, AccountType.CUSTOMER.name(), "CUSTOMER"),
                30L
        ).data();

        assertThat(result.sourceAuditId()).isEqualTo(30L);
        assertThat(result.status()).isEqualTo(OrderStatus.PENDING_CONFIRM);
        verify(service).submitApprovedAudit(10L, 30L);
    }

    @Test
    void adminCannotUseCustomerSpecialAuditEndpoint() {
        assertThatThrownBy(() -> controller.submitApproved(
                new AuthenticatedAccount(1L, AccountType.ADMIN.name(), "SUPER_ADMIN"),
                30L
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.FORBIDDEN);
    }

    private SpecialOrderAudit sampleAudit() {
        SpecialOrderAudit audit = new SpecialOrderAudit();
        audit.setId(30L);
        audit.setAuditNo("SA202606200001");
        audit.setCustomerId(10L);
        audit.setCustomerAppId(20L);
        audit.setOrderType(OrderType.RANK_GUARANTEE);
        audit.setStoreType(StoreType.APP_STORE);
        audit.setRegionCode("US");
        audit.setAppIdentifier("123456");
        audit.setAppName("Example App");
        audit.setRequestedContent("Keep target rank");
        audit.setStatus(SpecialAuditStatus.PENDING_REVIEW);
        return audit;
    }

    private AsoOrder sampleOrder() {
        AsoOrder order = new AsoOrder();
        order.setId(40L);
        order.setOrderNo("YO202606200001");
        order.setCustomerId(10L);
        order.setCustomerAppId(20L);
        order.setSourceAuditId(30L);
        order.setOrderType(OrderType.RANK_GUARANTEE);
        order.setStoreType(StoreType.APP_STORE);
        order.setRegionCode("US");
        order.setAppIdentifier("123456");
        order.setAppName("Example App");
        order.setStatus(OrderStatus.PENDING_CONFIRM);
        order.setOrderStartDate(LocalDate.of(2026, 6, 20));
        order.setOrderEndDate(LocalDate.of(2026, 6, 20));
        order.setTotalDays(1);
        order.setQuantity(1);
        order.setTotalAmount(new BigDecimal("88.00"));
        order.setItems(List.of());
        return order;
    }
}
