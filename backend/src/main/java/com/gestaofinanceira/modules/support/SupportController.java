package com.gestaofinanceira.modules.support;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/support")
public class SupportController {

    private final SupportService supportService;

    public SupportController(SupportService supportService) {
        this.supportService = supportService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> sendSupport(
            @Valid @RequestBody SupportRequest request,
            Authentication authentication
    ) {
        supportService.sendSupport(
                request,
                authentication.getName()
        );

        return ResponseEntity.ok(Map.of(
                "message",
                "Mensagem enviada com sucesso! Obrigado pelo contato."
        ));
    }
}