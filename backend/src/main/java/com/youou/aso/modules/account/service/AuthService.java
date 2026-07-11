package com.youou.aso.modules.account.service;

import com.youou.aso.common.error.BusinessException;
import com.youou.aso.common.error.ErrorCode;
import com.youou.aso.modules.account.domain.AccountStatus;
import com.youou.aso.modules.account.domain.AccountType;
import com.youou.aso.modules.account.domain.AdminAccount;
import com.youou.aso.modules.account.domain.CustomerAccount;
import com.youou.aso.modules.account.domain.PasswordResetCode;
import com.youou.aso.modules.account.domain.WalletAccount;
import com.youou.aso.modules.account.dto.AdminAccessResult;
import com.youou.aso.modules.account.dto.ChangePasswordCommand;
import com.youou.aso.modules.account.dto.ConfirmPasswordResetCommand;
import com.youou.aso.modules.account.dto.CurrentAccountResult;
import com.youou.aso.modules.account.dto.LoginCommand;
import com.youou.aso.modules.account.dto.LoginResult;
import com.youou.aso.modules.account.dto.RegisterCustomerCommand;
import com.youou.aso.modules.account.dto.RegisterCustomerResult;
import com.youou.aso.modules.account.dto.RequestPasswordResetCodeCommand;
import com.youou.aso.modules.account.repository.AdminAccountRepository;
import com.youou.aso.modules.account.repository.CustomerAccountRepository;
import com.youou.aso.modules.account.repository.PasswordResetCodeRepository;
import com.youou.aso.modules.account.repository.WalletAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class AuthService {
    private static final int PASSWORD_RESET_TTL_MINUTES = 10;
    private static final int PASSWORD_RESET_COOLDOWN_SECONDS = 60;
    private static final int PASSWORD_RESET_MAX_ATTEMPTS = 5;
    private static final Pattern RESET_CODE_PATTERN = Pattern.compile("^\\d{6}$");

    private final CustomerAccountRepository customerAccountRepository;
    private final AdminAccountRepository adminAccountRepository;
    private final WalletAccountRepository walletAccountRepository;
    private final PasswordResetCodeRepository passwordResetCodeRepository;
    private final PasswordResetMailSender passwordResetMailSender;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final PasswordResetCodeGenerator passwordResetCodeGenerator;
    private final AdminPermissionService adminPermissionService;
    private final Clock clock;

    public AuthService(
            CustomerAccountRepository customerAccountRepository,
            AdminAccountRepository adminAccountRepository,
            WalletAccountRepository walletAccountRepository,
            PasswordResetCodeRepository passwordResetCodeRepository,
            PasswordResetMailSender passwordResetMailSender,
            PasswordEncoder passwordEncoder,
            JwtTokenService jwtTokenService,
            PasswordResetCodeGenerator passwordResetCodeGenerator,
            Clock clock
    ) {
        this(
                customerAccountRepository,
                adminAccountRepository,
                walletAccountRepository,
                passwordResetCodeRepository,
                passwordResetMailSender,
                passwordEncoder,
                jwtTokenService,
                passwordResetCodeGenerator,
                null,
                clock
        );
    }

@Autowired
    public AuthService(
            CustomerAccountRepository customerAccountRepository,
            AdminAccountRepository adminAccountRepository,
            WalletAccountRepository walletAccountRepository,
            PasswordResetCodeRepository passwordResetCodeRepository,
            PasswordResetMailSender passwordResetMailSender,
            PasswordEncoder passwordEncoder,
            JwtTokenService jwtTokenService,
            PasswordResetCodeGenerator passwordResetCodeGenerator,
            AdminPermissionService adminPermissionService,
            Clock clock
    ) {
        this.customerAccountRepository = customerAccountRepository;
        this.adminAccountRepository = adminAccountRepository;
        this.walletAccountRepository = walletAccountRepository;
        this.passwordResetCodeRepository = passwordResetCodeRepository;
        this.passwordResetMailSender = passwordResetMailSender;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
        this.passwordResetCodeGenerator = passwordResetCodeGenerator;
        this.adminPermissionService = adminPermissionService;
        this.clock = clock;
    }

    public RegisterCustomerResult registerCustomer(RegisterCustomerCommand command) {
        String username = command.username().trim();
        String email = command.email().trim().toLowerCase();
        if (customerAccountRepository.existsByUsername(username)) {
            throw new BusinessException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }
        if (customerAccountRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        CustomerAccount customer = new CustomerAccount();
        customer.setUsername(username);
        customer.setEmail(email);
        customer.setPasswordHash(passwordEncoder.encode(command.password()));
        customer.setStatus(AccountStatus.ENABLED);
        customer.setForcePasswordChange(false);
        customer.setPreferredLocale("zh-CN");
        CustomerAccount saved = customerAccountRepository.save(customer);

        WalletAccount walletAccount = new WalletAccount();
        walletAccount.setCustomerId(saved.getId());
        walletAccount.setBalance(BigDecimal.ZERO);
        walletAccount.setFrozenBalance(BigDecimal.ZERO);
        walletAccount.setVersion(0L);
        walletAccountRepository.save(walletAccount);

        return new RegisterCustomerResult(saved.getId(), saved.getUsername(), saved.getEmail());
    }

    public LoginResult login(LoginCommand command) {
        String account = command.account().trim();
        return customerAccountRepository.findByUsernameOrEmail(account)
                .map(customer -> loginCustomer(customer, command.password()))
                .or(() -> adminAccountRepository.findByUsernameOrEmail(account)
                        .map(admin -> loginAdmin(admin, command.password())))
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_USERNAME_OR_PASSWORD));
    }

    public CurrentAccountResult currentAccount(AuthenticatedAccount authenticatedAccount) {
        if (AccountType.CUSTOMER.name().equals(authenticatedAccount.accountType())) {
            CustomerAccount customer = customerAccountRepository.findById(authenticatedAccount.accountId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
            return new CurrentAccountResult(
                    customer.getId(),
                    customer.getUsername(),
                    customer.getEmail(),
                    AccountType.CUSTOMER.name(),
                    "",
                    customer.isForcePasswordChange(),
                    customer.getPreferredLocale(),
                    List.of(),
                    List.of(),
                    List.of()
            );
        }
        AdminAccount admin = adminAccountRepository.findById(authenticatedAccount.accountId())
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
        AdminAccessResult adminAccess = adminPermissionService == null
                ? AdminAccessResult.empty()
                : adminPermissionService.accessForAdmin(admin.getId(), admin.getRoleCode().name());
        return new CurrentAccountResult(
                admin.getId(),
                admin.getUsername(),
                admin.getEmail(),
                AccountType.ADMIN.name(),
                admin.getRoleCode().name(),
                admin.isForcePasswordChange(),
                admin.getPreferredLocale(),
                adminAccess.roleKeys(),
                adminAccess.menuCodes(),
                adminAccess.permissions()
        );
    }

    public void changePassword(AuthenticatedAccount authenticatedAccount, ChangePasswordCommand command) {
        if (authenticatedAccount == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        validatePasswordChange(command);
        if (AccountType.CUSTOMER.name().equals(authenticatedAccount.accountType())) {
            changeCustomerPassword(authenticatedAccount.accountId(), command);
            return;
        }
        if (AccountType.ADMIN.name().equals(authenticatedAccount.accountType())) {
            changeAdminPassword(authenticatedAccount.accountId(), command);
            return;
        }
        throw new BusinessException(ErrorCode.UNAUTHORIZED);
    }

    public void requestPasswordResetCode(RequestPasswordResetCodeCommand command) {
        if (command == null || isBlank(command.email())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        String email = command.email().trim().toLowerCase();
        Optional<ResetAccount> account = findResetAccountByEmail(email);
        if (account.isEmpty()) {
            return;
        }
        Instant now = clock.instant();
        Optional<PasswordResetCode> activeCode = passwordResetCodeRepository.findLatestActiveByEmail(email, now);
        if (activeCode.isPresent()
                && activeCode.get().getCreatedAt().isAfter(now.minus(PASSWORD_RESET_COOLDOWN_SECONDS, ChronoUnit.SECONDS))) {
            return;
        }
        String code = passwordResetCodeGenerator.generate();
        PasswordResetCode resetCode = new PasswordResetCode();
        resetCode.setEmail(email);
        resetCode.setAccountType(account.get().accountType());
        resetCode.setAccountId(account.get().accountId());
        resetCode.setCodeHash(passwordEncoder.encode(code));
        resetCode.setAttemptCount(0);
        resetCode.setExpiresAt(now.plus(PASSWORD_RESET_TTL_MINUTES, ChronoUnit.MINUTES));
        resetCode.setCreatedAt(now);
        passwordResetCodeRepository.invalidateActiveCodes(email);
        passwordResetCodeRepository.save(resetCode);
        passwordResetMailSender.sendResetCode(email, code);
    }

    public void confirmPasswordReset(ConfirmPasswordResetCommand command) {
        validatePasswordReset(command);
        String email = command.email().trim().toLowerCase();
        Instant now = clock.instant();
        PasswordResetCode resetCode = passwordResetCodeRepository.findLatestActiveByEmail(email, now)
                .orElseThrow(() -> new BusinessException(ErrorCode.PASSWORD_RESET_TOKEN_EXPIRED));
        if (resetCode.getAttemptCount() >= PASSWORD_RESET_MAX_ATTEMPTS) {
            throw new BusinessException(ErrorCode.PASSWORD_RESET_TOKEN_INVALID);
        }
        if (!passwordEncoder.matches(command.code(), resetCode.getCodeHash())) {
            passwordResetCodeRepository.incrementAttemptCount(resetCode.getId());
            throw new BusinessException(ErrorCode.PASSWORD_RESET_TOKEN_INVALID);
        }
        String passwordHash = passwordEncoder.encode(command.newPassword());
        if (resetCode.getAccountType() == AccountType.CUSTOMER) {
            customerAccountRepository.updatePassword(resetCode.getAccountId(), passwordHash, false);
        } else if (resetCode.getAccountType() == AccountType.ADMIN) {
            adminAccountRepository.updatePassword(resetCode.getAccountId(), passwordHash, false);
        } else {
            throw new BusinessException(ErrorCode.PASSWORD_RESET_TOKEN_INVALID);
        }
        passwordResetCodeRepository.markUsed(resetCode.getId(), now);
    }

    private LoginResult loginCustomer(CustomerAccount customer, String password) {
        if (!passwordEncoder.matches(password, customer.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_USERNAME_OR_PASSWORD);
        }
        if (customer.getStatus() == AccountStatus.DISABLED) {
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLED);
        }
        if (customer.getStatus() == AccountStatus.LOCKED) {
            throw new BusinessException(ErrorCode.ACCOUNT_LOCKED);
        }
        String token = jwtTokenService.issue(customer.getId(), AccountType.CUSTOMER.name(), "");
        return new LoginResult(
                token,
                customer.getId(),
                customer.getUsername(),
                customer.getEmail(),
                AccountType.CUSTOMER.name(),
                "",
                customer.isForcePasswordChange(),
                customer.getPreferredLocale(),
                List.of(),
                List.of(),
                List.of()
        );
    }

    private LoginResult loginAdmin(AdminAccount admin, String password) {
        if (!passwordEncoder.matches(password, admin.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_USERNAME_OR_PASSWORD);
        }
        if (admin.getStatus() == AccountStatus.DISABLED) {
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLED);
        }
        if (admin.getStatus() == AccountStatus.LOCKED) {
            throw new BusinessException(ErrorCode.ACCOUNT_LOCKED);
        }
        String token = jwtTokenService.issue(admin.getId(), AccountType.ADMIN.name(), admin.getRoleCode().name());
        AdminAccessResult adminAccess = adminPermissionService == null
                ? AdminAccessResult.empty()
                : adminPermissionService.accessForAdmin(admin.getId(), admin.getRoleCode().name());
        return new LoginResult(
                token,
                admin.getId(),
                admin.getUsername(),
                admin.getEmail(),
                AccountType.ADMIN.name(),
                admin.getRoleCode().name(),
                admin.isForcePasswordChange(),
                admin.getPreferredLocale(),
                adminAccess.roleKeys(),
                adminAccess.menuCodes(),
                adminAccess.permissions()
        );
    }

    private void changeCustomerPassword(Long accountId, ChangePasswordCommand command) {
        CustomerAccount customer = customerAccountRepository.findById(accountId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
        if (!passwordEncoder.matches(command.oldPassword(), customer.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_USERNAME_OR_PASSWORD);
        }
        customerAccountRepository.updatePassword(accountId, passwordEncoder.encode(command.newPassword()), false);
    }

    private void changeAdminPassword(Long accountId, ChangePasswordCommand command) {
        AdminAccount admin = adminAccountRepository.findById(accountId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
        if (!passwordEncoder.matches(command.oldPassword(), admin.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_USERNAME_OR_PASSWORD);
        }
        adminAccountRepository.updatePassword(accountId, passwordEncoder.encode(command.newPassword()), false);
    }

    private void validatePasswordChange(ChangePasswordCommand command) {
        if (command == null
                || isBlank(command.oldPassword())
                || isBlank(command.newPassword())
                || isBlank(command.confirmPassword())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        if (!command.newPassword().equals(command.confirmPassword())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        if (command.newPassword().length() < 8 || command.newPassword().length() > 72) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        if (command.oldPassword().equals(command.newPassword())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
    }

    private void validatePasswordReset(ConfirmPasswordResetCommand command) {
        if (command == null
                || isBlank(command.email())
                || isBlank(command.code())
                || isBlank(command.newPassword())
                || isBlank(command.confirmPassword())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        if (!RESET_CODE_PATTERN.matcher(command.code()).matches()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        if (!command.newPassword().equals(command.confirmPassword())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        if (command.newPassword().length() < 8 || command.newPassword().length() > 72) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
    }

    private Optional<ResetAccount> findResetAccountByEmail(String email) {
        return customerAccountRepository.findByUsernameOrEmail(email)
                .filter(customer -> customer.getEmail().equalsIgnoreCase(email))
                .map(customer -> new ResetAccount(AccountType.CUSTOMER, customer.getId()))
                .or(() -> adminAccountRepository.findByUsernameOrEmail(email)
                        .filter(admin -> admin.getEmail().equalsIgnoreCase(email))
                        .map(admin -> new ResetAccount(AccountType.ADMIN, admin.getId())));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private record ResetAccount(AccountType accountType, Long accountId) {
    }
}