package com.gestaofinanceira.modules.passwordreset;

import com.gestaofinanceira.modules.user.User;
import com.gestaofinanceira.modules.user.UserRepository;
import com.gestaofinanceira.shared.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final RestClient restClient;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();
    

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${app.email.verification-expiration-hours:24}")
    private long expirationHours;

    @Value("${app.email.show-verification-link:false}")
    private boolean showVerificationLink;

    @Value("${brevo.api-key:}")
    private String brevoApiKey;

    @Value("${brevo.sender-email:}")
    private String senderEmail;

    @Value("${brevo.sender-name:FinanTrack}")
    private String senderName;

  public PasswordResetService(
        PasswordResetTokenRepository tokenRepository,
        UserRepository userRepository,
        RestClient.Builder restClientBuilder,
        PasswordEncoder passwordEncoder
) {
    this.tokenRepository = tokenRepository;
    this.userRepository = userRepository;
    this.restClient = restClientBuilder.build();
    this.passwordEncoder = passwordEncoder;
}

    @Transactional
    public void requestPasswordReset(String email) {

        if (email == null || email.isBlank()) {
            return;
        }

        String normalizedEmail = email.toLowerCase().trim();

        userRepository.findByEmail(normalizedEmail).ifPresent(user -> {

            tokenRepository.deleteByUserId(user.getId());

            byte[] randomBytes = new byte[32];
            secureRandom.nextBytes(randomBytes);

            String rawToken = Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(randomBytes);

            String tokenHash = hashToken(rawToken);

            PasswordResetToken token = new PasswordResetToken(
                    tokenHash,
                    user,
                    LocalDateTime.now().plusHours(expirationHours)
            );

            tokenRepository.save(token);

            String separator = frontendUrl.contains("?") ? "&" : "?";

            String resetLink = frontendUrl
                    + separator
                    + "resetToken="
                    + rawToken;

            if (showVerificationLink) {
                org.slf4j.LoggerFactory.getLogger(PasswordResetService.class)
                        .info("LINK DE RECUPERAÇÃO LOCAL: {}", resetLink);
            }

            sendResetEmail(user, resetLink);
        });
    }

    @Transactional
    public void resetPassword(String rawToken, String newPassword)
      {

        if (rawToken == null || rawToken.isBlank()) {
            throw new BusinessException("Link de recuperação inválido.");
        }

        if (newPassword == null || newPassword.length() < 8) {
            throw new BusinessException(
                    "A senha deve possuir pelo menos 8 caracteres."
            );
        }

        String tokenHash = hashToken(rawToken);

        PasswordResetToken token = tokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new BusinessException(
                        "Link de recuperação inválido ou inexistente."
                ));

        if (token.isUsed()) {
            throw new BusinessException(
                    "Este link de recuperação já foi utilizado."
            );
        }

        if (token.isExpired()) {
            throw new BusinessException(
                    "Este link de recuperação expirou. Solicite outro."
            );
        }

        User user = token.getUser();

        user.setPassword(passwordEncoder.encode(newPassword));

        token.setUsedAt(LocalDateTime.now());
    }

    private void sendResetEmail(User user, String resetLink) {

        if (brevoApiKey == null || brevoApiKey.isBlank()) {
            if (showVerificationLink) {
                return;
            }

            throw new IllegalStateException(
                    "A variável BREVO_API_KEY não está configurada."
            );
        }

        if (senderEmail == null || senderEmail.isBlank()) {
            throw new IllegalStateException(
                    "A variável BREVO_SENDER_EMAIL não está configurada."
            );
        }

        String htmlContent =
                "<h2>Recuperação de senha - FinanTrack</h2>"
                + "<p>Olá, " + escapeHtml(user.getName()) + ".</p>"
                + "<p>Recebemos uma solicitação para redefinir sua senha.</p>"
                + "<p><a href=\"" + escapeHtml(resetLink) + "\">"
                + "Redefinir minha senha</a></p>"
                + "<p>Este link é válido por " + expirationHours
                + " horas e só pode ser utilizado uma vez.</p>"
                + "<p>Se você não solicitou esta alteração, ignore este e-mail.</p>";

        Map<String, Object> payload = Map.of(
                "sender", Map.of(
                        "name", senderName,
                        "email", senderEmail
                ),
                "to", List.of(
                        Map.of(
                                "email", user.getEmail(),
                                "name", user.getName()
                        )
                ),
                "subject", "Recuperação de senha - FinanTrack",
                "htmlContent", htmlContent,
                "textContent",
                "Para redefinir sua senha, acesse: " + resetLink
        );

        restClient.post()
                .uri("https://api.brevo.com/v3/smtp/email")
                .header("api-key", brevoApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .toBodilessEntity();
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    token.getBytes(StandardCharsets.UTF_8)
            );

            StringBuilder hex = new StringBuilder();

            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }

            return hex.toString();

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Erro ao gerar hash do token de recuperação.", e
            );
        }
    }
}