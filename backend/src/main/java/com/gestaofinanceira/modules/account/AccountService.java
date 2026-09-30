package com.gestaofinanceira.modules.account;

import com.gestaofinanceira.modules.user.User;
import com.gestaofinanceira.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public List<AccountDTO.Response> listByUser(User user) {
        return accountRepository.findByUserOrderByCreatedAtAsc(user)
                .stream()
                .map(AccountDTO.Response::fromEntity)
                .collect(Collectors.toList());
    }

    public Account findEntityByIdAndUser(UUID id, User user) {
        return accountRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada: " + id));
    }

    @Transactional
    public AccountDTO.Response create(AccountDTO.Request request, User user) {
        BigDecimal balance = request.getInitialBalance() != null ? request.getInitialBalance() : BigDecimal.ZERO;
        String currency = request.getCurrency() != null && !request.getCurrency().isBlank() ? request.getCurrency() : "BRL";
        String color = request.getColor() != null && !request.getColor().isBlank() ? request.getColor() : "#3B82F6";

        Account account = Account.builder()
                .user(user)
                .name(request.getName().trim())
                .type(request.getType())
                .balance(balance)
                .currency(currency)
                .color(color)
                .build();

        Account saved = accountRepository.save(account);
        return AccountDTO.Response.fromEntity(saved);
    }

    @Transactional
    public AccountDTO.Response update(UUID id, AccountDTO.Request request, User user) {
        Account account = findEntityByIdAndUser(id, user);

        account.setName(request.getName().trim());
        account.setType(request.getType());
        if (request.getColor() != null && !request.getColor().isBlank()) {
            account.setColor(request.getColor());
        }
        if (request.getCurrency() != null && !request.getCurrency().isBlank()) {
            account.setCurrency(request.getCurrency());
        }

        Account updated = accountRepository.save(account);
        return AccountDTO.Response.fromEntity(updated);
    }

    @Transactional
    public void delete(UUID id, User user) {
        Account account = findEntityByIdAndUser(id, user);
        accountRepository.delete(account);
    }
}
