package com.gestaofinanceira.modules.account;

import com.gestaofinanceira.modules.user.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public ResponseEntity<List<AccountDTO.Response>> list(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(accountService.listByUser(user));
    }

    @PostMapping
    public ResponseEntity<AccountDTO.Response> create(
            @Valid @RequestBody AccountDTO.Request request,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accountService.create(request, user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AccountDTO.Response> update(
            @PathVariable UUID id,
            @Valid @RequestBody AccountDTO.Request request,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(accountService.update(id, request, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user
    ) {
        accountService.delete(id, user);
        return ResponseEntity.noContent().build();
    }
}
