package com.youou.aso.modules.account.api;

import com.youou.aso.common.api.ApiResponse;
import com.youou.aso.modules.account.dto.ChangePasswordCommand;
import com.youou.aso.modules.account.dto.ConfirmPasswordResetCommand;
import com.youou.aso.modules.account.dto.CurrentAccountResult;
import com.youou.aso.modules.account.dto.LoginCommand;
import com.youou.aso.modules.account.dto.LoginResult;
import com.youou.aso.modules.account.dto.RegisterCustomerCommand;
import com.youou.aso.modules.account.dto.RegisterCustomerResult;
import com.youou.aso.modules.account.dto.RequestPasswordResetCodeCommand;
import com.youou.aso.modules.account.service.AuthService;
import com.youou.aso.modules.account.service.AuthenticatedAccount;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ApiResponse<RegisterCustomerResult> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.ok(authService.registerCustomer(new RegisterCustomerCommand(
                request.username(),
                request.email(),
                request.password()
        )));
    }

    @PostMapping("/login")
    public ApiResponse<LoginResult> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(new LoginCommand(request.account(), request.password())));
    }

    @PostMapping("/password-reset/code")
    public ApiResponse<Void> requestPasswordResetCode(@Valid @RequestBody PasswordResetCodeRequest request) {
        authService.requestPasswordResetCode(new RequestPasswordResetCodeCommand(request.email()));
        return ApiResponse.ok(null);
    }

    @PostMapping("/password-reset/confirm")
    public ApiResponse<Void> confirmPasswordReset(@Valid @RequestBody ConfirmPasswordResetRequest request) {
        authService.confirmPasswordReset(new ConfirmPasswordResetCommand(
                request.email(),
                request.code(),
                request.newPassword(),
                request.confirmPassword()
        ));
        return ApiResponse.ok(null);
    }

    @GetMapping("/me")
    public ApiResponse<CurrentAccountResult> me(@AuthenticationPrincipal AuthenticatedAccount account) {
        return ApiResponse.ok(authService.currentAccount(account));
    }

    @PutMapping("/password")
    public ApiResponse<Void> changePassword(
            @AuthenticationPrincipal AuthenticatedAccount account,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        authService.changePassword(account, new ChangePasswordCommand(
                request.oldPassword(),
                request.newPassword(),
                request.confirmPassword()
        ));
        return ApiResponse.ok(null);
    }

    public record RegisterRequest(
            @NotBlank
            @Size(min = 3, max = 64)
            @Pattern(regexp = "^[A-Za-z0-9_]+$")
            String username,

            @NotBlank
            @Email
            @Size(max = 128)
            String email,

            @NotBlank
            @Size(min = 8, max = 72)
            String password
    ) {
    }

    public record LoginRequest(
            @NotBlank
            @Size(max = 128)
            String account,

            @NotBlank
            @Size(min = 1, max = 72)
            String password
    ) {
    }

    public record ChangePasswordRequest(
            @NotBlank
            @Size(min = 1, max = 72)
            String oldPassword,

            @NotBlank
            @Size(min = 8, max = 72)
            String newPassword,

            @NotBlank
            @Size(min = 8, max = 72)
            String confirmPassword
    ) {
    }

    public record PasswordResetCodeRequest(
            @NotBlank
            @Email
            @Size(max = 128)
            String email
    ) {
    }

    public record ConfirmPasswordResetRequest(
            @NotBlank
            @Email
            @Size(max = 128)
            String email,

            @NotBlank
            @Pattern(regexp = "^\\d{6}$")
            String code,

            @NotBlank
            @Size(min = 8, max = 72)
            String newPassword,

            @NotBlank
            @Size(min = 8, max = 72)
            String confirmPassword
    ) {
    }
}
