package com.youou.aso.modules.order.repository;

import com.youou.aso.modules.appmanagement.domain.StoreType;
import com.youou.aso.modules.order.domain.AsoOrder;
import com.youou.aso.modules.order.domain.OrderStatus;
import com.youou.aso.modules.order.domain.OrderType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable(named = "YOUOU_TEST_MYSQL_URL", matches = "jdbc:mysql://.+")
@SpringBootTest
@Transactional
class JdbcOrderRepositoryScheduleTest extends com.youou.aso.support.IsolatedMysqlTest {
    @Autowired
    private JdbcOrderRepository repository;
    @Autowired
    private JdbcSpecialOrderAuditRepository auditRepository;
    @Autowired
    private JdbcSpecialOrderAuditItemRepository auditItemRepository;

    @Test
    void specialEditPersistsRegionPriceAndReplacesKeywordsWithoutDuplicates() {
        var audit = new com.youou.aso.modules.order.domain.SpecialOrderAudit();
        audit.setAuditNo("EDIT-TEST-" + UUID.randomUUID());
        audit.setCustomerId(-1L);
        audit.setCustomerAppId(-1L);
        audit.setOrderType(OrderType.KEYWORD_COVERAGE);
        audit.setOrderModuleId(77L);
        audit.setStoreType(StoreType.APP_STORE);
        audit.setRegionCode("US");
        audit.setAppIdentifier("edit-test");
        audit.setAppName("edit-test");
        audit.setRequestedContent("edit-test");
        audit.setStatus(com.youou.aso.modules.order.domain.SpecialAuditStatus.SUBMITTED);
        audit.setNegotiatedPrice(BigDecimal.ONE);
        audit = auditRepository.save(audit);
        var original = new com.youou.aso.modules.order.domain.SpecialOrderAuditItem();
        original.setRegionCode("US");
        original.setKeyword("original");
        original.setUnitPrice(BigDecimal.ONE);
        original.setExecutionDays(1);
        auditItemRepository.saveAll(audit.getId(), java.util.List.of(original));
        audit.setRegionCode("JP");
        audit.setNegotiatedPrice(new BigDecimal("6.00"));
        auditRepository.update(audit);
        auditItemRepository.deleteByAuditId(audit.getId());
        original.setKeyword("changed");
        original.setRegionCode("JP");
        original.setUnitPrice(new BigDecimal("2.00"));
        original.setExecutionDays(3);
        auditItemRepository.saveAll(audit.getId(), java.util.List.of(original));
        var persisted = auditRepository.findById(audit.getId()).orElseThrow();
        assertThat(persisted.getOrderModuleId()).isEqualTo(77L);
        assertThat(persisted.getRegionCode()).isEqualTo("JP");
        assertThat(persisted.getNegotiatedPrice()).isEqualByComparingTo("6.00");
        var items = auditItemRepository.findByAuditIds(java.util.List.of(audit.getId())).get(audit.getId());
        assertThat(items).hasSize(1);
        assertThat(items.get(0).getKeyword()).isEqualTo("changed");
        assertThat(items.get(0).getExecutionDays()).isEqualTo(3);
    }

    @Test
    void minuteScheduleSurvivesInsertDraftUpdateAndStatusUpdate() {
        LocalDateTime scheduled = LocalDateTime.of(2027, 1, 10, 14, 35);
        AsoOrder order = new AsoOrder();
        order.setOrderNo("SCHEDULE-TEST-" + UUID.randomUUID());
        order.setCustomerId(-1L);
        order.setCustomerAppId(-1L);
        order.setOrderType(OrderType.KEYWORD_INSTALL);
        order.setStoreType(StoreType.APP_STORE);
        order.setRegionCode("US");
        order.setAppIdentifier("schedule-test");
        order.setAppName("schedule-test");
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        order.setOrderStartDate(scheduled.toLocalDate());
        order.setOrderEndDate(scheduled.toLocalDate());
        order.setScheduledStartAt(scheduled);
        order.setExecutionHours(1);
        order.setTotalDays(1);
        order.setQuantity(1);
        order.setTotalAmount(BigDecimal.ONE);
        order.setBalanceBefore(BigDecimal.ZERO);
        order.setBalanceAfter(BigDecimal.ZERO);
        order.setExpectedCompletedAt(scheduled.plusHours(1));

        AsoOrder saved = repository.save(order);
        assertThat(saved.getScheduledStartAt()).isEqualTo(scheduled);
        saved.setScheduledStartAt(scheduled.plusMinutes(20));
        saved.setExpectedCompletedAt(scheduled.plusMinutes(20).plusHours(1));
        AsoOrder edited = repository.updatePaymentDraft(saved);
        assertThat(edited.getScheduledStartAt()).isEqualTo(scheduled.plusMinutes(20));
        assertThat(edited.getExpectedCompletedAt()).isEqualTo(scheduled.plusMinutes(20).plusHours(1));
        edited.setDeductedTransactionId(456L);
        edited.setStatus(OrderStatus.PENDING_CONFIRM);
        repository.update(edited);
        assertThat(repository.findById(saved.getId()).orElseThrow().getDeductedTransactionId()).isEqualTo(456L);
        assertThat(repository.findById(saved.getId()).orElseThrow().getScheduledStartAt())
                .isEqualTo(scheduled.plusMinutes(20));
        edited.setStatus(OrderStatus.CANCELLED);
        edited.setRejectReason("cancelled");
        edited.setRefundTransactionId(123L);
        edited.setRefundAmount(BigDecimal.ONE);
        edited.setConfirmedByAdminId(7L);
        edited.setConfirmedAt(scheduled);
        edited = repository.update(edited);
        edited.setStatus(OrderStatus.PENDING_CONFIRM);
        edited.setRejectReason(null);
        edited.setRefundTransactionId(null);
        edited.setRefundAmount(BigDecimal.ZERO);
        edited.setConfirmedByAdminId(null);
        edited.setConfirmedAt(null);
        AsoOrder reopened = repository.updatePaymentDraft(edited);
        assertThat(reopened.getStatus()).isEqualTo(OrderStatus.PENDING_CONFIRM);
        assertThat(reopened.getRejectReason()).isNull();
        assertThat(reopened.getRefundTransactionId()).isNull();
        assertThat(reopened.getRefundAmount()).isEqualByComparingTo("0");
        assertThat(reopened.getConfirmedByAdminId()).isNull();
        assertThat(reopened.getConfirmedAt()).isNull();
    }
}
