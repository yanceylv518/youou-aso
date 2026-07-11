package com.youou.aso.modules.order.api;

import com.youou.aso.common.error.BusinessException;
import com.youou.aso.common.error.ErrorCode;
import com.youou.aso.modules.account.domain.AccountType;
import com.youou.aso.modules.account.service.AuthenticatedAccount;
import com.youou.aso.modules.appmanagement.domain.StoreType;
import com.youou.aso.modules.order.domain.OrderType;
import com.youou.aso.modules.order.domain.SpecialAuditStatus;
import com.youou.aso.modules.order.domain.SpecialOrderAudit;
import com.youou.aso.modules.order.dto.ReviewSpecialAuditCommand;
import com.youou.aso.modules.order.dto.SpecialOrderAuditResult;
import com.youou.aso.modules.order.service.SpecialOrderAuditService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminSpecialOrderAuditControllerTest {
    private final SpecialOrderAuditService service = mock(SpecialOrderAuditService.class);
    private final AdminSpecialOrderAuditController controller = new AdminSpecialOrderAuditController(service);

    @Test
    void adminCanReviewSpecialAuditWithNegotiatedContentAndPrice() {
        SpecialOrderAudit approved = sampleAudit();
        approved.setStatus(SpecialAuditStatus.APPROVED_WAIT_SUBMIT);
        approved.setNegotiatedContent("Final monthly package");
        approved.setNegotiatedPrice(new BigDecimal("88.00"));
        when(service.reviewAudit(
                30L,
                1L,
                new ReviewSpecialAuditCommand("Final monthly package", new BigDecimal("88.00"))
        )).thenReturn(approved);

        SpecialOrderAuditResult result = controller.review(
                new AuthenticatedAccount(1L, AccountType.ADMIN.name(), "SUPER_ADMIN"),
                30L,
                new AdminSpecialOrderAuditController.ReviewSpecialAuditRequest(
                        "Final monthly package",
                        new BigDecimal("88.00")
                )
        ).data();

        assertThat(result.status()).isEqualTo(SpecialAuditStatus.APPROVED_WAIT_SUBMIT);
        assertThat(result.negotiatedPrice()).isEqualByComparingTo("88.00");
        verify(service).reviewAudit(
                30L,
                1L,
                new ReviewSpecialAuditCommand("Final monthly package", new BigDecimal("88.00"))
        );
    }

    @Test
    void adminCanCancelUnsubmittedSpecialAudit() {
        SpecialOrderAudit cancelled = sampleAudit();
        cancelled.setStatus(SpecialAuditStatus.CANCELLED);
        cancelled.setCancelReason("Customer changed plan");
        when(service.cancelAudit(30L, 1L, "Customer changed plan")).thenReturn(cancelled);

        SpecialOrderAuditResult result = controller.cancel(
                new AuthenticatedAccount(1L, AccountType.ADMIN.name(), "SUPER_ADMIN"),
                30L,
                new AdminSpecialOrderAuditController.CancelSpecialAuditRequest("Customer changed plan")
        ).data();

        assertThat(result.status()).isEqualTo(SpecialAuditStatus.CANCELLED);
        assertThat(result.cancelReason()).isEqualTo("Customer changed plan");
        verify(service).cancelAudit(30L, 1L, "Customer changed plan");
    }

    @Test
    void customerCannotUseAdminSpecialAuditEndpoint() {
        assertThatThrownBy(() -> controller.review(
                new AuthenticatedAccount(10L, AccountType.CUSTOMER.name(), "CUSTOMER"),
                30L,
                new AdminSpecialOrderAuditController.ReviewSpecialAuditRequest(
                        "Final monthly package",
                        new BigDecimal("88.00")
                )
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
        audit.setOrderType(OrderType.KEYWORD_COVERAGE);
        audit.setStoreType(StoreType.APP_STORE);
        audit.setRegionCode("US");
        audit.setAppIdentifier("123456");
        audit.setAppName("Example App");
        audit.setRequestedContent("Expand keyword coverage");
        audit.setStatus(SpecialAuditStatus.PENDING_REVIEW);
        return audit;
    }
}
