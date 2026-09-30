package com.gestaofinanceira.modules.transaction;

import com.gestaofinanceira.modules.user.User;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public ResponseEntity<List<TransactionDTO.Response>> list(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) UUID accountId
    ) {
        return ResponseEntity.ok(transactionService.listFiltered(user, startDate, endDate, type, accountId));
    }

    @PostMapping
    public ResponseEntity<TransactionDTO.Response> create(
            @Valid @RequestBody TransactionDTO.Request request,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.create(request, user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionDTO.Response> update(
            @PathVariable UUID id,
            @Valid @RequestBody TransactionDTO.Request request,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(transactionService.update(id, request, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user
    ) {
        transactionService.delete(id, user);
        return ResponseEntity.noContent().build();
    }
}
