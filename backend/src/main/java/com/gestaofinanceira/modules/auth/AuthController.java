package com.gestaofinanceira.modules.auth;

import com.gestaofinanceira.modules.user.User;
import com.gestaofinanceira.modules.verification.EmailVerificationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final EmailVerificationService emailVerificationService;

    public AuthController(
            AuthService authService,
            EmailVerificationService emailVerificationService
    ) {
        this.authService = authService;
        this.emailVerificationService = emailVerificationService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/google")
    public ResponseEntity<AuthResponse> googleLogin(
            @RequestBody Map<String, String> request
    ) {
        String credential = request.get("credential");

        AuthResponse response = authService.googleLogin(credential);

        return ResponseEntity.ok(response);
    }

   
        @PostMapping("/resend-verification")
    public ResponseEntity<Map<String, String>> resendVerification(
            @RequestBody Map<String, String> request
    ) {
        String email = request.get("email");

        authService.resendVerification(email);

        return ResponseEntity.ok(Map.of(
                "message",
                "Novo link de confirmação gerado. Verifique seu e-mail."
        ));
    }

   
       @PostMapping("/verify-email")
public ResponseEntity<Map<String, String>> verifyEmail(
        @RequestBody Map<String, String> request
) {
    String token = request.get("token");

    emailVerificationService.verifyEmail(token);

    return ResponseEntity.ok(Map.of(
            "message",
            "E-mail confirmado com sucesso! Você já pode fazer login."
    ));
}

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(UserDTO.fromEntity(user));
    }
}