package com.gestaofinanceira.modules.support;

import com.gestaofinanceira.modules.user.User;
import com.gestaofinanceira.modules.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class SupportService {

    private static final Logger logger =
            LoggerFactory.getLogger(SupportService.class);

    private static final String RECIPIENT = "mrgasco18@gmail.com";

    private final RestClient restClient;
    private final UserRepository userRepository;

    @Value("${brevo.api-key:}")
    private String brevoApiKey;

    @Value("${brevo.sender-email:}")
    private String senderEmail;

    @Value("${brevo.sender-name:FinanTrack}")
    private String senderName;

    public SupportService(
            RestClient.Builder restClientBuilder,
            UserRepository userRepository
    ) {
        this.restClient = restClientBuilder.build();
        this.userRepository = userRepository;
    }

    public void sendSupport(SupportRequest request, String userEmail) {

        if (brevoApiKey == null || brevoApiKey.isBlank()
                || senderEmail == null || senderEmail.isBlank()) {
            throw new IllegalStateException(
                    "Serviço de suporte não configurado."
            );
        }

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalStateException(
                        "Usuário autenticado não encontrado."
                ));

        String name = user.getName();
        String email = user.getEmail();

        String type = request.type().equals("SUGESTAO")
                ? "Sugestão"
                : "Reclamação";

        String safeName = escapeHtml(name);
        String safeEmail = escapeHtml(email);
        String safeSubject = escapeHtml(request.subject());
        String safeMessage = escapeHtml(request.message())
                .replace("\n", "<br>");

        String htmlContent =
                "<!DOCTYPE html>" +
                "<html lang=\"pt-BR\">" +
                "<head><meta charset=\"UTF-8\"></head>" +
                "<body style=\"font-family:Arial,sans-serif;color:#334155;\">" +
                "<div style=\"max-width:600px;margin:30px auto;padding:30px;" +
                "border:1px solid #e2e8f0;border-radius:12px;\">" +
                "<h2 style=\"color:#059669;\">Nova mensagem de suporte - FinanTrack</h2>" +
                "<p><strong>Tipo:</strong> " + type + "</p>" +
                "<p><strong>Usuário:</strong> " + safeName + "</p>" +
                "<p><strong>E-mail:</strong> " + safeEmail + "</p>" +
                "<p><strong>Assunto:</strong> " + safeSubject + "</p>" +
                "<p><strong>Mensagem:</strong></p>" +
                "<p>" + safeMessage + "</p>" +
                "</div></body></html>";

        String textContent =
                "Nova mensagem de suporte - FinanTrack\n\n" +
                "Tipo: " + type + "\n" +
                "Usuário: " + name + "\n" +
                "E-mail: " + email + "\n" +
                "Assunto: " + request.subject() + "\n\n" +
                "Mensagem:\n" + request.message();

        Map<String, Object> payload = Map.of(
                "sender", Map.of(
                        "name", senderName,
                        "email", senderEmail
                ),
                "to", List.of(
                        Map.of("email", RECIPIENT)
                ),
                "replyTo", Map.of(
                        "email", email,
                        "name", name
                ),
                "subject", "[FinanTrack - " + type + "] " + request.subject(),
                "htmlContent", htmlContent,
                "textContent", textContent
        );

        try {
            restClient.post()
                    .uri("https://api.brevo.com/v3/smtp/email")
                    .header("api-key", brevoApiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();

            logger.info("Mensagem de suporte enviada com sucesso.");

        } catch (Exception e) {
            logger.error("Falha ao enviar mensagem de suporte.", e);

            throw new IllegalStateException(
                    "Não foi possível enviar sua mensagem. Tente novamente."
            );
        }
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
}