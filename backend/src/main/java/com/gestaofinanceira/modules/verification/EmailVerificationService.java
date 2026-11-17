package com.gestaofinanceira.modules.verification;

import com.gestaofinanceira.modules.user.User;
import com.gestaofinanceira.shared.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
public class EmailVerificationService {

    private static final Logger logger =
            LoggerFactory.getLogger(EmailVerificationService.class);

    private final EmailVerificationTokenRepository tokenRepository;
    private final RestClient restClient;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.email.show-verification-link:false}")
    private boolean showVerificationLink;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${app.email.verification-expiration-hours:24}")
    private long expirationHours;

    @Value("${brevo.api-key:}")
    private String brevoApiKey;

    @Value("${brevo.sender-email:}")
    private String senderEmail;

    @Value("${brevo.sender-name:FinanTrack}")
    private String senderName;

    public EmailVerificationService(
            EmailVerificationTokenRepository tokenRepository,
            RestClient.Builder restClientBuilder
    ) {
        this.tokenRepository = tokenRepository;
        this.restClient = restClientBuilder.build();
    }

    @Transactional
    public String createVerificationLink(User user) {

        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);

        String rawToken = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);

        String tokenHash = hashToken(rawToken);

        EmailVerificationToken verificationToken =
                new EmailVerificationToken(
                        tokenHash,
                        user,
                        LocalDateTime.now().plusHours(expirationHours)
                );

        tokenRepository.save(verificationToken);

        String separator = frontendUrl.contains("?") ? "&" : "?";
        String verificationLink =
                frontendUrl + separator + "token=" + rawToken;

        if (showVerificationLink) {
            logger.info("LINK DE CONFIRMAÇÃO LOCAL: {}", verificationLink);
        }

        if (brevoApiKey == null || brevoApiKey.isBlank()) {
            if (showVerificationLink) {
                logger.info(
                        "Brevo não configurado. Envio de e-mail ignorado no ambiente local."
                );
                return verificationLink;
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

        sendVerificationEmail(user, verificationLink);

        return verificationLink;
    }

    private void sendVerificationEmail(
            User user,
            String verificationLink
    ) {

        String safeName = escapeHtml(user.getName());
        String safeLink = escapeHtml(verificationLink);

        String htmlContent =
                "<!DOCTYPE html>" +
                "<html lang=\"pt-BR\">" +
                "<head><meta charset=\"UTF-8\"></head>" +
                "<body style=\"font-family:Arial,sans-serif;color:#334155;\">" +
                "<div style=\"max-width:600px;margin:30px auto;padding:30px;" +
                "border:1px solid #e2e8f0;border-radius:12px;\">" +
                "<h1 style=\"color:#059669;\">Bem-vindo ao FinanTrack!</h1>" +
                "<p>Olá, " + safeName + ".</p>" +
                "<p>Recebemos seu cadastro. Para liberar o acesso à sua conta, " +
                "confirme seu endereço de e-mail.</p>" +
                "<p style=\"margin:30px 0;\">" +
                "<a href=\"" + safeLink + "\" " +
                "style=\"background:#059669;color:white;padding:14px 24px;" +
                "text-decoration:none;border-radius:8px;\">" +
                "Confirmar meu e-mail</a></p>" +
                "<p>Este link é válido por " + expirationHours +
                " horas e pode ser utilizado uma única vez.</p>" +
                "<p>Se você não realizou este cadastro, ignore esta mensagem.</p>" +
                "<p>Equipe FinanTrack</p>" +
                "</div></body></html>";

        String textContent =
                "Olá, " + user.getName() + "!\n\n" +
                "Para confirmar seu e-mail no FinanTrack, acesse:\n" +
                verificationLink + "\n\n" +
                "Este link é válido por " + expirationHours + " horas.\n\n" +
                "Equipe FinanTrack";

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
                "subject", "Confirme seu e-mail - FinanTrack",
                "htmlContent", htmlContent,
                "textContent", textContent
        );

        restClient.post()
                .uri("https://api.brevo.com/v3/smtp/email")
                .header("api-key", brevoApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .toBodilessEntity();

        logger.info(
                "E-mail de confirmação enviado ao destinatário cadastrado."
        );
    }
    
    
          @Transactional
    public void resendVerificationLink(User user) {

        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new BusinessException(
                    "Este e-mail já está confirmado."
            );
        }

        tokenRepository.deleteByUserId(user.getId());

        createVerificationLink(user);
    }

        @Transactional
    public void verifyEmail(String rawToken) {

        if (rawToken == null || rawToken.isBlank()) {
            throw new BusinessException(
                    "Token de confirmação inválido."
            );
        }

        String tokenHash = hashToken(rawToken);

        EmailVerificationToken verificationToken =
                tokenRepository.findByTokenHash(tokenHash)
                        .orElseThrow(() -> new BusinessException(
                                "Link de confirmação inválido ou inexistente."
                        ));

        if (verificationToken.isUsed()) {
            throw new BusinessException(
                    "Este link de confirmação já foi utilizado."
            );
        }

        if (verificationToken.isExpired()) {
            throw new BusinessException(
                    "Este link de confirmação expirou. Solicite um novo."
            );
        }

        User user = verificationToken.getUser();
        user.setEmailVerified(true);

        verificationToken.setUsedAt(LocalDateTime.now());
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
                    "Erro ao gerar hash do token de confirmação.", e
            );
        }
    }
}
