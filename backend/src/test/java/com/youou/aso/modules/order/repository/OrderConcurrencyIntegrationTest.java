package com.youou.aso.modules.order.repository;

import com.youou.aso.support.IsolatedMysqlTest;
import com.youou.aso.modules.account.domain.*;
import com.youou.aso.modules.account.dto.*;
import com.youou.aso.modules.account.repository.*;
import com.youou.aso.modules.account.service.*;
import com.youou.aso.modules.appmanagement.domain.StoreType;
import com.youou.aso.modules.order.domain.*;
import com.youou.aso.modules.order.service.*;
import com.youou.aso.common.error.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;

@org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable(named = "YOUOU_TEST_MYSQL_URL", matches = "jdbc:mysql://.+")
@SpringBootTest
class OrderConcurrencyIntegrationTest extends IsolatedMysqlTest {
    @Autowired CustomerAccountRepository customers;
    @Autowired WalletAccountRepository wallets;
    @Autowired OrderRepository orders;
    @Autowired OrderItemRepository items;
    @Autowired SpecialOrderAuditRepository audits;
    @Autowired SpecialOrderAuditService auditService;
    @Autowired OrderService orderService;
    @Autowired AuthService auth;
    @Autowired AdminAccountService admins;
    @Autowired JdbcTemplate jdbc;
    @Autowired PlatformTransactionManager transactions;

    private Long customer() {
        String name = "test_" + UUID.randomUUID().toString().replace("-", "");
        CustomerAccount account = new CustomerAccount();
        account.setUsername(name); account.setEmail(name + "@example.com"); account.setPasswordHash("test-hash");
        account.setStatus(AccountStatus.ENABLED); account.setPreferredLocale("zh-CN");
        Long id = customers.save(account).getId();
        WalletAccount wallet = new WalletAccount();
        wallet.setCustomerId(id); wallet.setBalance(new BigDecimal("100")); wallet.setFrozenBalance(BigDecimal.ZERO); wallet.setVersion(0L);
        wallets.save(wallet);
        return id;
    }
    private AsoOrder order(Long customer, OrderStatus status) {
        AsoOrder order = new AsoOrder();
        order.setOrderNo("TEST-" + UUID.randomUUID()); order.setCustomerId(customer); order.setCustomerAppId(-1L);
        order.setOrderType(OrderType.DOWNLOAD); order.setStoreType(StoreType.APP_STORE); order.setRegionCode("US");
        order.setAppIdentifier("test-app"); order.setAppName("Test"); order.setStatus(status);
        order.setOrderStartDate(LocalDate.now().plusDays(1)); order.setOrderEndDate(LocalDate.now().plusDays(1));
        order.setTotalDays(1); order.setQuantity(10); order.setUnitPrice(BigDecimal.ONE); order.setTotalAmount(BigDecimal.TEN);
        order.setBalanceBefore(new BigDecimal("100")); order.setBalanceAfter(new BigDecimal("90"));
        return orders.save(order);
    }
    private List<Boolean> race(Callable<?> first, Callable<?> second) throws Exception {
        var pool = Executors.newFixedThreadPool(2);
        var barrier = new CyclicBarrier(2);
        try {
            List<Future<Boolean>> results = new ArrayList<>();
            for (var task : List.of(first, second)) results.add(pool.submit(() -> {
                barrier.await(10, TimeUnit.SECONDS);
                try { task.call(); return true; } catch (BusinessException expected) { return false; }
            }));
            return List.of(results.get(0).get(20, TimeUnit.SECONDS), results.get(1).get(20, TimeUnit.SECONDS));
        } finally { pool.shutdownNow(); }
    }
    private BigDecimal balance(Long customer) {
        return jdbc.queryForObject("SELECT balance FROM wallet_account WHERE customer_id=?", BigDecimal.class, customer);
    }
    @Test void completedAdjustmentPersistsReasonWithRefundAndRejectsBlankReason() {
        Long customer = customer();
        AsoOrder order = order(customer, OrderStatus.COMPLETED);
        OrderItem item = new OrderItem();
        item.setItemType("DOWNLOAD"); item.setItemName("Test"); item.setRegionCode("US");
        item.setQuantity(10); item.setUnitPrice(BigDecimal.ONE); item.setAmount(BigDecimal.TEN);
        items.saveAll(order.getId(), List.of(item));
        Long itemId = items.findByOrderIds(List.of(order.getId())).get(order.getId()).get(0).getId();
        var edits = List.of(new OrderService.CompletedItemEdit(itemId, 8));
        for (String reason : Arrays.asList(null, "", "   ", "x".repeat(501))) {
            assertThatThrownBy(() -> orderService.adjustCompletedOrder(order.getId(), 7L, edits, reason)).isInstanceOf(BusinessException.class);
        }
        assertThat(balance(customer)).isEqualByComparingTo("100");
        orderService.adjustCompletedOrder(order.getId(), 7L, edits, "  应用中途下架  ");
        assertThat(balance(customer)).isEqualByComparingTo("102");
        var result = com.youou.aso.modules.order.dto.OrderResult.from(orderService.getCustomerOrder(customer, order.getId()));
        assertThat(result.events()).hasSize(1);
        assertThat(result.events().get(0).reason()).isEqualTo("应用中途下架");
        assertThat(result.events().get(0).completedAfter()).isEqualTo(8);
        assertThat(result.refundAmount()).isEqualByComparingTo("2");
    }
    @Test void concurrentSpecialSubmissionCreatesAndChargesExactlyOnce() throws Exception {
        Long customer = customer();
        SpecialOrderAudit audit = new SpecialOrderAudit();
        audit.setAuditNo("TEST-" + UUID.randomUUID()); audit.setCustomerId(customer); audit.setCustomerAppId(-1L);
        audit.setOrderType(OrderType.KEYWORD_COVERAGE); audit.setStoreType(StoreType.APP_STORE); audit.setRegionCode("US");
        audit.setAppIdentifier("test-app"); audit.setAppName("Test"); audit.setRequestedContent("test keyword");
        audit.setNegotiatedPrice(BigDecimal.TEN); audit.setStatus(SpecialAuditStatus.APPROVED_WAIT_SUBMIT);
        Long id = audits.save(audit).getId();
        assertThat(race(() -> auditService.submitApprovedAudit(customer, id), () -> auditService.submitApprovedAudit(customer, id)))
                .containsExactlyInAnyOrder(true, false);
        assertThat(balance(customer)).isEqualByComparingTo("90");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM aso_order WHERE source_audit_id=?", Integer.class, id)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM wallet_transaction WHERE customer_id=?", Integer.class, customer)).isEqualTo(1);
    }
    @Test void concurrentAutomaticCompletionRefundsOnlyOnce() throws Exception {
        Long customer = customer();
        AsoOrder order = order(customer, OrderStatus.PAUSED);
        order.setQuantity(3); order.setTotalAmount(new BigDecimal("0.01")); order.setExpectedCompletedAt(LocalDateTime.now().minusDays(2));
        orders.update(order);
        OrderItem item = new OrderItem();
        item.setItemType("DOWNLOAD"); item.setItemName("Test"); item.setRegionCode("US"); item.setQuantity(3);
        item.setUnitPrice(new BigDecimal("0.004")); item.setAmount(new BigDecimal("0.01"));
        items.saveAll(order.getId(), List.of(item));
        assertThat(race(() -> orderService.completeExpiredExecutingOrders(), () -> orderService.completeExpiredExecutingOrders())).containsOnly(true);
        assertThat(balance(customer)).isEqualByComparingTo("100.01");
        assertThat(orders.findById(order.getId()).orElseThrow().getStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM wallet_transaction WHERE related_order_id=? AND transaction_type='ORDER_REFUND'", Integer.class, order.getId())).isEqualTo(1);
    }
    @Test void paymentPersistsDebitAndCancellationRefundsSameCustomer() {
        Long customer = customer();
        AsoOrder order = order(customer, OrderStatus.PENDING_PAYMENT);
        var paid = orderService.payPendingPaymentOrder(customer, order.getId());
        assertThat(paid.getDeductedTransactionId()).isNotNull();
        assertThat(balance(customer)).isEqualByComparingTo("90");
        assertThat(jdbc.queryForObject("SELECT related_order_id FROM wallet_transaction WHERE id=?", Long.class, paid.getDeductedTransactionId())).isEqualTo(order.getId());
        var cancelled = orderService.cancelOrder(order.getId(), 7L, "test cancellation");
        assertThat(cancelled.getRefundAmount()).isEqualByComparingTo("10");
        assertThat(balance(customer)).isEqualByComparingTo("100");
        assertThat(jdbc.queryForList("SELECT event_type FROM aso_order_event WHERE order_id=? ORDER BY id", String.class, order.getId())).containsExactly("PAID", "CANCELLED");
    }
    @Test void pauseWaitsForCompletionAndCannotOverwriteCompletedStatus() throws Exception {
        AsoOrder order = order(customer(), OrderStatus.EXECUTING);
        var pool = Executors.newSingleThreadExecutor();
        var started = new CountDownLatch(1);
        final Future<?>[] future = new Future<?>[1];
        try {
            new TransactionTemplate(transactions).executeWithoutResult(tx -> {
                AsoOrder locked = orders.findByIdForUpdate(order.getId()).orElseThrow();
                future[0] = pool.submit(() -> { started.countDown(); return orderService.pauseOrder(order.getId(), 7L); });
                try {
                    assertThat(started.await(5, TimeUnit.SECONDS)).isTrue();
                    assertThatThrownBy(() -> future[0].get(250, TimeUnit.MILLISECONDS)).isInstanceOf(TimeoutException.class);
                } catch (InterruptedException error) { throw new RuntimeException(error); }
                locked.setStatus(OrderStatus.COMPLETED); orders.update(locked);
            });
            assertThatThrownBy(() -> future[0].get(10, TimeUnit.SECONDS)).hasCauseInstanceOf(BusinessException.class);
            assertThat(orders.findById(order.getId()).orElseThrow().getStatus()).isEqualTo(OrderStatus.COMPLETED);
        } finally { pool.shutdownNow(); }
    }
    @Test void customerAndAdminRegistrationShareOneIdentityNamespaceUnderConcurrency() throws Exception {
        String name = "race_" + UUID.randomUUID().toString().replace("-", "");
        assertThat(race(() -> auth.registerCustomer(new RegisterCustomerCommand(name, name + "c@example.com", "TestPassword123")),
                () -> admins.createAdmin(new CreateAdminAccountCommand(name, name + "a@example.com", "TestPassword123"))))
                .containsExactlyInAnyOrder(true, false);
        int count = jdbc.queryForObject("SELECT COUNT(*) FROM customer_account WHERE username=?", Integer.class, name)
                + jdbc.queryForObject("SELECT COUNT(*) FROM admin_account WHERE username=?", Integer.class, name);
        assertThat(count).isEqualTo(1);
    }
}
