
package com.gestaofinanceira.modules.consultation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class ConsultationService {

    private static final Logger logger =
            LoggerFactory.getLogger(ConsultationService.class);

    private static final String RECIPIENT =
            "mrgasco18@gmail.com";

    private final RestClient restClient;

    @Value("${brevo.api-key:}")
    private String brevoApiKey;

    @Value("${brevo.sender-email:}")
    private String senderEmail;

    @Value("${brevo.sender-name:FinanTrack}")
    private String senderName;

    public ConsultationService(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    public void sendConsultation(ConsultationRequest request) {

        if (brevoApiKey == null || brevoApiKey.isBlank()) {
            throw new IllegalStateException(
                    "Serviço de e-mail não configurado."
            );
        }

        if (senderEmail == null || senderEmail.isBlank()) {
            throw new IllegalStateException(
                    "Remetente de e-mail não configurado."
            );
        }

        String safeName = escapeHtml(request.name());
        String safeEmail = escapeHtml(request.email());
        String safePhone = escapeHtml(request.phone());
        String safeMessage = escapeHtml(request.message())
                .replace("\n", "<br>");

        String htmlContent =
                "<!DOCTYPE html>" +
                "<html lang=\"pt-BR\">" +
                "<head><meta charset=\"UTF-8\"></head>" +
                "<body style=\"font-family:Arial,sans-serif;color:#334155;\">" +
                "<div style=\"max-width:600px;margin:30px auto;padding:30px;" +
                "border:1px solid #e2e8f0;border-radius:12px;\">" +
                "<h2 style=\"color:#059669;\">Nova solicitação de consultoria</h2>" +
                "<p><strong>Nome:</strong> " + safeName + "</p>" +
                "<p><strong>E-mail:</strong> " + safeEmail + "</p>" +
                "<p><strong>Telefone:</strong> " + safePhone + "</p>" +
                "<p><strong>Mensagem:</strong></p>" +
                "<p>" + safeMessage + "</p>" +
                "</div></body></html>";

        String textContent =
                "Nova solicitação de consultoria financeira\n\n" +
                "Nome: " + request.name() + "\n" +
                "E-mail: " + request.email() + "\n" +
                "Telefone: " + request.phone() + "\n\n" +
                "Mensagem:\n" + request.message();

        Map<String, Object> payload = Map.of(
                "sender", Map.of(
                        "name", senderName,
                        "email", senderEmail
                ),
                "to", List.of(
                        Map.of(
                                "email", RECIPIENT
                        )
                ),
                "replyTo", Map.of(
                        "email", request.email(),
                        "name", request.name()
                ),
                "subject", "Solicitação de consultoria financeira",
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

            logger.info("Solicitação de consultoria enviada com sucesso.");

        } catch (Exception e) {
            logger.error("Falha ao enviar solicitação de consultoria.", e);

            throw new IllegalStateException(
                    "Não foi possível enviar sua solicitação. Tente novamente."
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
