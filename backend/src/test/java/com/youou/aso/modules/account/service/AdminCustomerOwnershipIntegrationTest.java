package com.youou.aso.modules.account.service;

import com.youou.aso.support.IsolatedMysqlTest;
import com.youou.aso.common.error.BusinessException;
import com.youou.aso.modules.account.dto.RegisterCustomerCommand;
import com.youou.aso.modules.account.repository.CustomerAccountRepository;
import com.youou.aso.modules.appmanagement.dto.CustomerAppQuery;
import com.youou.aso.modules.appmanagement.repository.CustomerAppRepository;
import com.youou.aso.modules.order.dto.OrderQuery;
import com.youou.aso.modules.order.repository.OrderRepository;
import com.youou.aso.modules.order.repository.SpecialOrderAuditRepository;
import com.youou.aso.modules.order.service.ReviewAttachmentService;
import com.youou.aso.modules.support.repository.CustomerServiceConfigRepository;
import com.youou.aso.modules.support.domain.CustomerServiceConfig;
import com.youou.aso.modules.support.api.CustomerServiceConfigController;
import com.youou.aso.modules.wallet.repository.WalletQueryRepository;
import com.youou.aso.modules.wallet.repository.AdminWalletRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
@EnabledIfEnvironmentVariable(named="YOUOU_TEST_MYSQL_URL", matches="jdbc:mysql://.+")
class AdminCustomerOwnershipIntegrationTest extends IsolatedMysqlTest {
    @Autowired JdbcTemplate jdbc;
    @Autowired AuthService auth;
    @Autowired AdminReferralService referrals;
    @Autowired CustomerAccountRepository customers;
    @Autowired CustomerAppRepository apps;
    @Autowired OrderRepository orders;
    @Autowired SpecialOrderAuditRepository audits;
    @Autowired WalletQueryRepository wallets;
    @Autowired AdminWalletRepository adminWallets;
    @Autowired CustomerServiceConfigRepository support;
    @Autowired CustomerServiceConfigController supportController;
    @Autowired ReviewAttachmentService attachments;
    long adminA, adminB, customerA, customerB, systemCustomer, appA, appB, orderA, orderB, auditA, auditB;

    @BeforeEach void seed() {
        SecurityContextHolder.clearContext();
        adminA = admin("owner_a"); adminB = admin("owner_b");
        customerA = register("client_a", referrals.registrationPath(adminA));
        customerB = register("client_b", referrals.registrationPath(adminB));
        systemCustomer = register("system_client", null);
        appA = app(customerA); appB = app(customerB);
        orderA = order(customerA, appA); orderB = order(customerB, appB);
        auditA = audit(customerA, appA); auditB = audit(customerB, appB);
        for (long customer : List.of(customerA,customerB,systemCustomer)) {
            jdbc.update("INSERT INTO wallet_transaction (transaction_no, customer_id, wallet_id, direction, transaction_type, amount, balance_before, balance_after) VALUES (?, ?, ?, 'CREDIT', 'ADMIN_RECHARGE', 10, 0, 10)", "txn-"+customer, customer, customer);
        }
    }
    @AfterEach void clear() { SecurityContextHolder.clearContext(); }

    @Test void invitationAssignsOwnerAndOrdinaryRegistrationStaysSystem() {
        assertThat(customers.findById(customerA).orElseThrow().getOwnerAdminId()).isEqualTo(adminA);
        assertThat(customers.findById(customerB).orElseThrow().getOwnerAdminId()).isEqualTo(adminB);
        assertThat(customers.findById(systemCustomer).orElseThrow().getOwnerAdminId()).isNull();
        assertThat(referrals.registrationPath(adminA)).isEqualTo(referrals.registrationPath(adminA)).isNotEqualTo(referrals.registrationPath(adminB));
    }
    @Test void invalidOrDisabledInvitationCannotCreateCustomerOrWallet() {
        assertThatThrownBy(() -> register("invalid_client", "/register?invite=unknown")).isInstanceOf(BusinessException.class);
        String path = referrals.registrationPath(adminB);
        jdbc.update("UPDATE admin_account SET status = 'DISABLED' WHERE id = ?", adminB);
        assertThatThrownBy(() -> register("disabled_client", path)).isInstanceOf(BusinessException.class);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM customer_account WHERE username IN ('invalid_client','disabled_client')", Integer.class)).isZero();
    }
    @Test void customersSearchPaginationAndIdsCannotCrossOwnerBoundary() {
        login(adminA,"ADMIN");
        assertThat(customers.findAll(null)).extracting("id").containsExactly(customerA);
        assertThat(customers.findAll("@example.test",20,0)).extracting("id").containsExactly(customerA);
        assertThat(customers.countAll("@example.test")).isEqualTo(1);
        assertThat(customers.findByIds(List.of(customerA,customerB,systemCustomer))).extracting("id").containsExactly(customerA);
        assertThat(customers.findById(customerB)).isEmpty();
        assertThat(customers.findById(systemCustomer)).isEmpty();
        login(adminB,"ADMIN");
        assertThat(customers.findAll(null)).extracting("id").containsExactly(customerB);
    }
    @Test void ordersAppsAuditsAndWalletsAreIsolatedIncludingLocksAndCounts() {
        login(adminA,"ADMIN");
        assertThat(orders.findAll(new OrderQuery(null,null),20,0)).extracting("id").containsExactly(orderA);
        assertThat(orders.countAll(new OrderQuery(null,null))).isEqualTo(1);
        assertThat(orders.findByIdForUpdate(orderB)).isEmpty();
        assertThat(orders.findById(orderB)).isEmpty();
        assertThat(apps.findAll(new CustomerAppQuery(null,null,null,null),20,0)).extracting("id").containsExactly(appA);
        assertThat(apps.countAll(new CustomerAppQuery(null,null,null,null))).isEqualTo(1);
        assertThat(apps.findById(appB)).isEmpty();
        assertThat(audits.findAll(20,0)).extracting("id").containsExactly(auditA);
        assertThat(audits.countAll()).isEqualTo(1);
        assertThat(audits.findByIdForUpdate(auditB)).isEmpty();
        assertThat(wallets.findTransactions(null,null,20)).extracting("customerId").containsExactly(customerA);
        assertThat(wallets.countTransactions(null,null,null,null,null,null)).isEqualTo(1);
        assertThat(wallets.findAccountByCustomerId(customerB)).isEmpty();
        assertThat(adminWallets.findByCustomerIdForUpdate(customerB)).isEmpty();
    }
    @Test void guessedCustomerIdsCannotCreateAppsOrDownloadAttachments() {
        var foreignApp = apps.findById(appB).orElseThrow();
        login(adminA,"ADMIN");
        assertThatThrownBy(() -> apps.save(foreignApp)).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> apps.findActiveByCustomerStoreAndIdentifier(customerB, foreignApp.getStoreType(),foreignApp.getAppIdentifier())).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> attachments.download("guessed-id",customerB)).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> CustomerOwnership.requireCustomer(jdbc,systemCustomer)).isInstanceOf(BusinessException.class);
    }
    @Test void superAdminSeesAllIncludingSystemCustomers() {
        login(adminA,"SUPER_ADMIN");
        assertThat(customers.countAll(null)).isEqualTo(3);
        assertThat(orders.countAll(new OrderQuery(null,null))).isEqualTo(2);
        assertThat(audits.countAll()).isEqualTo(2);
        assertThat(wallets.countTransactions(null,null,null,null,null,null)).isEqualTo(3);
    }
    @Test void customerSupportUsesItsOwnerAndGlobalSupportIsPreserved() {
        var global = config("System Support"); support.save(global);
        support.saveForAdmin(adminA,config("Support A"));
        support.saveForAdmin(adminB,config("Support B"));
        assertThat(supportController.getCustomerConfig(customer(customerA)).data().serviceName()).isEqualTo("Support A");
        assertThat(supportController.getCustomerConfig(customer(customerB)).data().serviceName()).isEqualTo("Support B");
        assertThat(supportController.getCustomerConfig(customer(systemCustomer)).data().serviceName()).isEqualTo("System Support");
        assertThat(support.find().orElseThrow().getServiceName()).isEqualTo("System Support");
    }
    @Test void adminCanReadOnlyOwnSupportAndCannotReadGlobalSettings() {
        support.saveForAdmin(adminA,config("Support A")); support.saveForAdmin(adminB,config("Support B"));
        var account = new AuthenticatedAccount(adminA,"ADMIN","ADMIN");
        assertThat(supportController.getAdminConfig(true,account).data().serviceName()).isEqualTo("Support A");
        assertThatThrownBy(() -> supportController.getAdminConfig(false,account)).isInstanceOf(BusinessException.class);
    }
    @Test void personalSupportUpdatesCannotOverwriteSystemOrAnotherAdmin() {
        support.save(config("System Support")); support.saveForAdmin(adminB,config("Support B"));
        var account = new AuthenticatedAccount(adminA,"ADMIN","ADMIN");
        var request = new CustomerServiceConfigController.CustomerServiceConfigRequest("Updated A",null,"Contact A","a@example.test",null,null,null,true);
        assertThatThrownBy(() -> supportController.updateAdminConfig(false,account,request)).isInstanceOf(BusinessException.class);
        supportController.updateAdminConfig(true,account,request);
        assertThat(support.findForAdmin(adminA).orElseThrow().getServiceName()).isEqualTo("Updated A");
        assertThat(support.findForAdmin(adminB).orElseThrow().getServiceName()).isEqualTo("Support B");
        assertThat(support.find().orElseThrow().getServiceName()).isEqualTo("System Support");
    }
    @Test void missingPersonalSupportDoesNotExposeSystemContacts() {
        support.save(config("System Support"));
        var result = supportController.getCustomerConfig(customer(customerA)).data();
        assertThat(result.enabled()).isFalse();
        assertThat(result.email()).isNull();
    }
    @Test void automaticCompletionQueryOnlyLocksOwnedOrdersForAdmins() {
        jdbc.update("UPDATE aso_order SET status = 'EXECUTING', expected_completed_at = '2000-01-01 00:00:00'");
        login(adminA,"ADMIN");
        assertThat(orders.findDueBefore(java.time.LocalDateTime.now())).extracting("id").containsExactly(orderA);
        SecurityContextHolder.clearContext();
        assertThat(orders.findDueBefore(java.time.LocalDateTime.now())).hasSize(2);
    }
    private CustomerServiceConfig config(String name) { var c=new CustomerServiceConfig(); c.setServiceName(name); c.setEnabled(true); c.setEmail("support@example.test"); c.setEmailVisible(true); return c; }
    private AuthenticatedAccount customer(long id) { return new AuthenticatedAccount(id,"CUSTOMER",""); }
    private void login(long id,String role) { SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(new AuthenticatedAccount(id,"ADMIN",role),null,List.of())); }
    private long register(String name,String path) { return auth.registerCustomer(new RegisterCustomerCommand(name,name+"@example.test","TestPass123!",path==null?null:path.substring(path.indexOf("invite=")+7))).accountId(); }
    private long admin(String name) {
        jdbc.update("INSERT INTO admin_account (username,email,password_hash,role_code,status) VALUES (?,?,'unused','ADMIN','ENABLED')", name,name+"@example.test");
        return jdbc.queryForObject("SELECT id FROM admin_account WHERE username = ?",Long.class,name);
    }
    private long app(long customer) {
        jdbc.update("INSERT INTO customer_app (customer_id,store_type,region_code,app_identifier,app_name,status,verified_at) VALUES (?,'APP_STORE','US',?,'Test App','ACTIVE',NOW())",customer,"app-"+customer);
        return jdbc.queryForObject("SELECT id FROM customer_app WHERE customer_id=?",Long.class,customer);
    }
    private long order(long customer,long app) {
        jdbc.update("INSERT INTO aso_order (order_no,customer_id,customer_app_id,order_type,store_type,app_identifier,app_name,status,total_amount,balance_before,balance_after) VALUES (?,?,?,'KEYWORD_INSTALL','APP_STORE','test','Test App','PENDING_EXECUTION',1,10,9)","o-"+customer,customer,app);
        return jdbc.queryForObject("SELECT id FROM aso_order WHERE customer_id=?",Long.class,customer);
    }
    private long audit(long customer,long app) {
        jdbc.update("INSERT INTO special_order_audit (audit_no,customer_id,customer_app_id,order_type,store_type,region_code,app_identifier,app_name,requested_content,status) VALUES (?,?,?,'RANK_GUARANTEE','APP_STORE','US','test','Test App','test','PENDING_REVIEW')","a-"+customer,customer,app);
        return jdbc.queryForObject("SELECT id FROM special_order_audit WHERE customer_id=?",Long.class,customer);
    }
}
