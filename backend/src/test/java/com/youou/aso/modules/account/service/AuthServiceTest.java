package com.youou.aso.modules.account.service;

import com.youou.aso.common.error.BusinessException;
import com.youou.aso.common.error.ErrorCode;
import com.youou.aso.modules.account.domain.AccountStatus;
import com.youou.aso.modules.account.domain.AccountType;
import com.youou.aso.modules.account.domain.AdminRole;
import com.youou.aso.modules.account.domain.CustomerAccount;
import com.youou.aso.modules.account.domain.AdminAccount;
import com.youou.aso.modules.account.domain.WalletAccount;
import com.youou.aso.modules.account.dto.ChangePasswordCommand;
import com.youou.aso.modules.account.dto.ConfirmPasswordResetCommand;
import com.youou.aso.modules.account.dto.RegisterCustomerCommand;
import com.youou.aso.modules.account.dto.RequestPasswordResetCodeCommand;
import com.youou.aso.modules.account.dto.LoginCommand;
import com.youou.aso.modules.account.domain.PasswordResetCode;
import com.youou.aso.modules.account.repository.PasswordResetCodeRepository;
import com.youou.aso.modules.account.repository.AdminAccountRepository;
import com.youou.aso.modules.account.repository.CustomerAccountRepository;
import com.youou.aso.modules.account.repository.WalletAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthServiceTest {
    private final InMemoryCustomerAccountRepository customerRepository = new InMemoryCustomerAccountRepository();
    private final InMemoryAdminAccountRepository adminRepository = new InMemoryAdminAccountRepository();
    private final InMemoryWalletAccountRepository walletRepository = new InMemoryWalletAccountRepository();
    private final InMemoryPasswordResetCodeRepository passwordResetCodeRepository = new InMemoryPasswordResetCodeRepository();
    private final CapturingPasswordResetMailSender mailSender = new CapturingPasswordResetMailSender();
    private final MutableClock clock = new MutableClock(Instant.parse("2026-06-30T00:00:00Z"));
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final AuthService authService = new AuthService(
            customerRepository,
            adminRepository,
            walletRepository,
            passwordResetCodeRepository,
            mailSender,
            passwordEncoder,
            new JwtTokenService("test-issuer", "01234567890123456789012345678901", 3600),
            new FixedPasswordResetCodeGenerator("123456"),
            clock
    );

    @Test
    void registerCustomerCreatesEnabledCustomerAndWallet() {
        var result = authService.registerCustomer(new RegisterCustomerCommand(
                "demo_user",
                "demo@example.com",
                "StrongPass123"
        ));

        CustomerAccount customer = customerRepository.findById(result.accountId()).orElseThrow();
        assertThat(customer.getUsername()).isEqualTo("demo_user");
        assertThat(customer.getEmail()).isEqualTo("demo@example.com");
        assertThat(customer.getStatus()).isEqualTo(AccountStatus.ENABLED);
        assertThat(passwordEncoder.matches("StrongPass123", customer.getPasswordHash())).isTrue();
        assertThat(walletRepository.balanceOf(result.accountId())).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void registerCustomerRejectsDuplicateUsername() {
        authService.registerCustomer(new RegisterCustomerCommand("demo_user", "demo@example.com", "StrongPass123"));

        assertThatThrownBy(() -> authService.registerCustomer(new RegisterCustomerCommand(
                "demo_user",
                "other@example.com",
                "StrongPass123"
        )))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USERNAME_ALREADY_EXISTS);
    }

    @Test
    void registerCustomerRejectsDuplicateEmail() {
        authService.registerCustomer(new RegisterCustomerCommand("demo_user", "demo@example.com", "StrongPass123"));

        assertThatThrownBy(() -> authService.registerCustomer(new RegisterCustomerCommand(
                "other_user",
                "demo@example.com",
                "StrongPass123"
        )))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.EMAIL_ALREADY_EXISTS);
    }

    @Test
    void loginCustomerAcceptsUsernameOrEmailAndIssuesToken() {
        authService.registerCustomer(new RegisterCustomerCommand("demo_user", "demo@example.com", "StrongPass123"));

        var usernameLogin = authService.login(new LoginCommand("demo_user", "StrongPass123"));
        var emailLogin = authService.login(new LoginCommand("demo@example.com", "StrongPass123"));

        assertThat(usernameLogin.token()).isNotBlank();
        assertThat(usernameLogin.username()).isEqualTo("demo_user");
        assertThat(usernameLogin.email()).isEqualTo("demo@example.com");
        assertThat(usernameLogin.accountType()).isEqualTo("CUSTOMER");
        assertThat(usernameLogin.roleCode()).isEmpty();
        assertThat(emailLogin.accountId()).isEqualTo(usernameLogin.accountId());
        assertThat(emailLogin.username()).isEqualTo("demo_user");
        assertThat(emailLogin.email()).isEqualTo("demo@example.com");
    }

    @Test
    void loginRejectsWrongPassword() {
        authService.registerCustomer(new RegisterCustomerCommand("demo_user", "demo@example.com", "StrongPass123"));

        assertThatThrownBy(() -> authService.login(new LoginCommand("demo_user", "WrongPass123")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_USERNAME_OR_PASSWORD);
    }

    @Test
    void customerCanChangeOwnPassword() {
        var registered = authService.registerCustomer(new RegisterCustomerCommand("demo_user", "demo@example.com", "StrongPass123"));
        CustomerAccount customer = customerRepository.findById(registered.accountId()).orElseThrow();
        customer.setForcePasswordChange(true);
        customerRepository.save(customer);

        authService.changePassword(
                new AuthenticatedAccount(registered.accountId(), AccountType.CUSTOMER.name(), ""),
                new ChangePasswordCommand("StrongPass123", "NewStrongPass123", "NewStrongPass123")
        );

        CustomerAccount updated = customerRepository.findById(registered.accountId()).orElseThrow();
        assertThat(passwordEncoder.matches("NewStrongPass123", updated.getPasswordHash())).isTrue();
        assertThat(updated.isForcePasswordChange()).isFalse();
        assertThat(authService.login(new LoginCommand("demo_user", "NewStrongPass123")).accountId()).isEqualTo(registered.accountId());
    }

    @Test
    void changePasswordRejectsWrongOldPassword() {
        var registered = authService.registerCustomer(new RegisterCustomerCommand("demo_user", "demo@example.com", "StrongPass123"));

        assertThatThrownBy(() -> authService.changePassword(
                new AuthenticatedAccount(registered.accountId(), AccountType.CUSTOMER.name(), ""),
                new ChangePasswordCommand("WrongPass123", "NewStrongPass123", "NewStrongPass123")
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_USERNAME_OR_PASSWORD);
    }

    @Test
    void changePasswordRejectsMismatchedConfirmation() {
        var registered = authService.registerCustomer(new RegisterCustomerCommand("demo_user", "demo@example.com", "StrongPass123"));

        assertThatThrownBy(() -> authService.changePassword(
                new AuthenticatedAccount(registered.accountId(), AccountType.CUSTOMER.name(), ""),
                new ChangePasswordCommand("StrongPass123", "NewStrongPass123", "OtherStrongPass123")
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }

    @Test
    void adminCanChangeOwnPassword() {
        AdminAccount admin = new AdminAccount();
        admin.setUsername("admin");
        admin.setEmail("admin@example.com");
        admin.setPasswordHash(passwordEncoder.encode("AdminPass123"));
        admin.setRoleCode(AdminRole.SUPER_ADMIN);
        admin.setStatus(AccountStatus.ENABLED);
        admin.setForcePasswordChange(true);
        admin.setPreferredLocale("zh-CN");
        AdminAccount saved = adminRepository.save(admin);

        authService.changePassword(
                new AuthenticatedAccount(saved.getId(), AccountType.ADMIN.name(), AdminRole.SUPER_ADMIN.name()),
                new ChangePasswordCommand("AdminPass123", "NewAdminPass123", "NewAdminPass123")
        );

        AdminAccount updated = adminRepository.findById(saved.getId()).orElseThrow();
        assertThat(passwordEncoder.matches("NewAdminPass123", updated.getPasswordHash())).isTrue();
        assertThat(updated.isForcePasswordChange()).isFalse();
    }

    @Test
    void requestPasswordResetCodeSendsCodeWithoutRevealingUnknownEmail() {
        authService.registerCustomer(new RegisterCustomerCommand("demo_user", "demo@example.com", "StrongPass123"));

        authService.requestPasswordResetCode(new RequestPasswordResetCodeCommand("demo@example.com"));
        authService.requestPasswordResetCode(new RequestPasswordResetCodeCommand("missing@example.com"));

        assertThat(mailSender.sentMessages).hasSize(1);
        assertThat(mailSender.sentMessages.get(0).email()).isEqualTo("demo@example.com");
        assertThat(mailSender.sentMessages.get(0).code()).isEqualTo("123456");
        assertThat(passwordResetCodeRepository.codes).hasSize(1);
        PasswordResetCode stored = passwordResetCodeRepository.codes.values().iterator().next();
        assertThat(stored.getCodeHash()).isNotEqualTo("123456");
        assertThat(stored.getExpiresAt()).isEqualTo(clock.instant().plus(10, ChronoUnit.MINUTES));
    }

    @Test
    void requestPasswordResetCodeDoesNotSendAgainWithinCooldown() {
        authService.registerCustomer(new RegisterCustomerCommand("demo_user", "demo@example.com", "StrongPass123"));

        authService.requestPasswordResetCode(new RequestPasswordResetCodeCommand("demo@example.com"));
        authService.requestPasswordResetCode(new RequestPasswordResetCodeCommand("demo@example.com"));

        assertThat(mailSender.sentMessages).hasSize(1);
        assertThat(passwordResetCodeRepository.codes).hasSize(1);
    }

    @Test
    void confirmPasswordResetUpdatesCustomerPasswordAndConsumesCode() {
        authService.registerCustomer(new RegisterCustomerCommand("demo_user", "demo@example.com", "StrongPass123"));
        authService.requestPasswordResetCode(new RequestPasswordResetCodeCommand("demo@example.com"));

        authService.confirmPasswordReset(new ConfirmPasswordResetCommand(
                "demo@example.com",
                "123456",
                "NewStrongPass123",
                "NewStrongPass123"
        ));

        PasswordResetCode stored = passwordResetCodeRepository.codes.values().iterator().next();
        assertThat(stored.getUsedAt()).isEqualTo(clock.instant());
        assertThat(authService.login(new LoginCommand("demo_user", "NewStrongPass123")).username()).isEqualTo("demo_user");
    }

    @Test
    void confirmPasswordResetRejectsWrongCodeAndStopsAfterMaxAttempts() {
        authService.registerCustomer(new RegisterCustomerCommand("demo_user", "demo@example.com", "StrongPass123"));
        authService.requestPasswordResetCode(new RequestPasswordResetCodeCommand("demo@example.com"));

        for (int index = 0; index < 5; index++) {
            assertThatThrownBy(() -> authService.confirmPasswordReset(new ConfirmPasswordResetCommand(
                    "demo@example.com",
                    "000000",
                    "NewStrongPass123",
                    "NewStrongPass123"
            )))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.PASSWORD_RESET_TOKEN_INVALID);
        }
        assertThatThrownBy(() -> authService.confirmPasswordReset(new ConfirmPasswordResetCommand(
                "demo@example.com",
                "123456",
                "NewStrongPass123",
                "NewStrongPass123"
        )))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PASSWORD_RESET_TOKEN_INVALID);
    }

    @Test
    void confirmPasswordResetRejectsExpiredCode() {
        authService.registerCustomer(new RegisterCustomerCommand("demo_user", "demo@example.com", "StrongPass123"));
        authService.requestPasswordResetCode(new RequestPasswordResetCodeCommand("demo@example.com"));
        clock.advanceMinutes(11);

        assertThatThrownBy(() -> authService.confirmPasswordReset(new ConfirmPasswordResetCommand(
                "demo@example.com",
                "123456",
                "NewStrongPass123",
                "NewStrongPass123"
        )))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PASSWORD_RESET_TOKEN_EXPIRED);
    }

    private static class InMemoryCustomerAccountRepository implements CustomerAccountRepository {
        private final Map<Long, CustomerAccount> customers = new HashMap<>();
        private long nextId = 1L;

        @Override
        public boolean existsByUsername(String username) {
            return customers.values().stream().anyMatch(customer -> customer.getUsername().equals(username));
        }

        @Override
        public boolean existsByEmail(String email) {
            return customers.values().stream().anyMatch(customer -> customer.getEmail().equals(email));
        }

        @Override
        public Optional<CustomerAccount> findById(Long id) {
            return Optional.ofNullable(customers.get(id));
        }

        @Override
        public Optional<CustomerAccount> findByUsernameOrEmail(String account) {
            return customers.values().stream()
                    .filter(customer -> customer.getUsername().equals(account) || customer.getEmail().equals(account))
                    .findFirst();
        }

        @Override
        public List<CustomerAccount> findAll(String keyword) {
            return customers.values().stream().toList();
        }

        @Override
        public CustomerAccount save(CustomerAccount customer) {
            if (customer.getId() == null) {
                customer.setId(nextId++);
            }
            customers.put(customer.getId(), customer);
            return customer;
        }

        @Override
        public void updatePassword(Long id, String passwordHash, boolean forcePasswordChange) {
            CustomerAccount customer = customers.get(id);
            if (customer != null) {
                customer.setPasswordHash(passwordHash);
                customer.setForcePasswordChange(forcePasswordChange);
            }
        }

        @Override
        public void updateStatus(Long id, AccountStatus status) {
            CustomerAccount customer = customers.get(id);
            if (customer != null) {
                customer.setStatus(status);
            }
        }
    }

    private static class InMemoryAdminAccountRepository implements AdminAccountRepository {
        private final Map<Long, AdminAccount> admins = new HashMap<>();
        private long nextId = 1L;

        @Override
        public boolean existsByUsername(String username) {
            return admins.values().stream().anyMatch(admin -> admin.getUsername().equals(username));
        }

        @Override
        public boolean existsByEmail(String email) {
            return admins.values().stream().anyMatch(admin -> admin.getEmail().equals(email));
        }

        @Override
        public Optional<AdminAccount> findById(Long id) {
            return Optional.ofNullable(admins.get(id));
        }

        @Override
        public Optional<AdminAccount> findByUsernameOrEmail(String account) {
            return admins.values().stream()
                    .filter(admin -> admin.getUsername().equals(account) || admin.getEmail().equals(account))
                    .findFirst();
        }

        @Override
        public List<AdminAccount> findAll() {
            return admins.values().stream().toList();
        }

        @Override
        public AdminAccount save(AdminAccount admin) {
            if (admin.getId() == null) {
                admin.setId(nextId++);
            }
            admins.put(admin.getId(), admin);
            return admin;
        }

        @Override
        public void updatePassword(Long id, String passwordHash, boolean forcePasswordChange) {
            AdminAccount admin = admins.get(id);
            if (admin != null) {
                admin.setPasswordHash(passwordHash);
                admin.setForcePasswordChange(forcePasswordChange);
            }
        }

        @Override
        public void updateStatus(Long id, AccountStatus status) {
        }
    }

    private static class InMemoryWalletAccountRepository implements WalletAccountRepository {
        private final Map<Long, WalletAccount> wallets = new HashMap<>();
        private long nextId = 1L;

        @Override
        public WalletAccount save(WalletAccount walletAccount) {
            if (walletAccount.getId() == null) {
                walletAccount.setId(nextId++);
            }
            wallets.put(walletAccount.getCustomerId(), walletAccount);
            return walletAccount;
        }

        private BigDecimal balanceOf(Long customerId) {
            return wallets.get(customerId).getBalance();
        }
    }

    private static class InMemoryPasswordResetCodeRepository implements PasswordResetCodeRepository {
        private final Map<Long, PasswordResetCode> codes = new HashMap<>();
        private long nextId = 1L;

        @Override
        public void invalidateActiveCodes(String email) {
            codes.values().stream()
                    .filter(code -> code.getEmail().equals(email) && code.getUsedAt() == null)
                    .forEach(code -> code.setUsedAt(Instant.EPOCH));
        }

        @Override
        public PasswordResetCode save(PasswordResetCode code) {
            if (code.getId() == null) {
                code.setId(nextId++);
            }
            codes.put(code.getId(), code);
            return code;
        }

        @Override
        public Optional<PasswordResetCode> findLatestActiveByEmail(String email, Instant now) {
            return codes.values().stream()
                    .filter(code -> code.getEmail().equals(email))
                    .filter(code -> code.getUsedAt() == null)
                    .filter(code -> code.getExpiresAt().isAfter(now))
                    .reduce((first, second) -> first.getCreatedAt().isAfter(second.getCreatedAt()) ? first : second);
        }

        @Override
        public void incrementAttemptCount(Long id) {
            PasswordResetCode code = codes.get(id);
            code.setAttemptCount(code.getAttemptCount() + 1);
        }

        @Override
        public void markUsed(Long id, Instant usedAt) {
            codes.get(id).setUsedAt(usedAt);
        }
    }

    private record SentMail(String email, String code) {
    }

    private static class CapturingPasswordResetMailSender implements PasswordResetMailSender {
        private final List<SentMail> sentMessages = new java.util.ArrayList<>();

        @Override
        public void sendResetCode(String email, String code) {
            sentMessages.add(new SentMail(email, code));
        }
    }

    private record FixedPasswordResetCodeGenerator(String code) implements PasswordResetCodeGenerator {
        @Override
        public String generate() {
            return code;
        }
    }

    private static class MutableClock extends Clock {
        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        @Override
        public ZoneOffset getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }

        private void advanceMinutes(long minutes) {
            instant = instant.plus(minutes, ChronoUnit.MINUTES);
        }
    }
}
