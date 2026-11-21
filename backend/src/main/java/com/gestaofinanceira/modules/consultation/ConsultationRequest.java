
package com.gestaofinanceira.modules.consultation;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ConsultationRequest(

        @NotBlank(message = "Informe seu nome.")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres.")
        String name,

        @NotBlank(message = "Informe seu e-mail.")
        @Email(message = "Informe um e-mail válido.")
        @Size(max = 150, message = "O e-mail deve ter no máximo 150 caracteres.")
        String email,

        @NotBlank(message = "Informe seu telefone.")
        @Size(max = 30, message = "O telefone deve ter no máximo 30 caracteres.")
        String phone,

        @NotBlank(message = "Escreva uma mensagem.")
        @Size(max = 3000, message = "A mensagem deve ter no máximo 3000 caracteres.")
        String message

) {}
