
package com.gestaofinanceira.modules.consultation;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/consultations")
public class ConsultationController {

    private final ConsultationService consultationService;

    public ConsultationController(
            ConsultationService consultationService
    ) {
        this.consultationService = consultationService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> sendConsultation(
            @Valid @RequestBody ConsultationRequest request
    ) {
        consultationService.sendConsultation(request);

        return ResponseEntity.ok(Map.of(
                "message",
                "Solicitação enviada com sucesso! Entraremos em contato em breve."
        ));
    }
}
