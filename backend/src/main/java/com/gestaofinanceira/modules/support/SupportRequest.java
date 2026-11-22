package com.gestaofinanceira.modules.support;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SupportRequest(

        @NotBlank(message = "Selecione o tipo de contato.")
        @Pattern(
                regexp = "SUGESTAO|RECLAMACAO",
                message = "Tipo de contato inválido."
        )
        String type,

        @NotBlank(message = "Informe o assunto.")
        @Size(max = 120, message = "O assunto deve ter no máximo 120 caracteres.")
        String subject,

        @NotBlank(message = "Escreva sua mensagem.")
        @Size(max = 3000, message = "A mensagem deve ter no máximo 3000 caracteres.")
        String message

) {}