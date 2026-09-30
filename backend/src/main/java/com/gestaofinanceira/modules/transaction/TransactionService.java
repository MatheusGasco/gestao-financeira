package com.gestaofinanceira.modules.transaction;

import com.gestaofinanceira.modules.account.Account;
import com.gestaofinanceira.modules.account.AccountRepository;
import com.gestaofinanceira.modules.account.AccountService;
import com.gestaofinanceira.modules.category.Category;
import com.gestaofinanceira.modules.category.CategoryRepository;
import com.gestaofinanceira.modules.user.User;
import com.gestaofinanceira.shared.exception.BusinessException;
import com.gestaofinanceira.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountService accountService;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;

    public TransactionService(
            TransactionRepository transactionRepository,
            AccountService accountService,
            AccountRepository accountRepository,
            CategoryRepository categoryRepository
    ) {
        this.transactionRepository = transactionRepository;
        this.accountService = accountService;
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<TransactionDTO.Response> listFiltered(
            User user,
            LocalDate startDate,
            LocalDate endDate,
            TransactionType type,
            UUID accountId
    ) {
        return transactionRepository.findFiltered(user, startDate, endDate, type, accountId)
                .stream()
                .map(TransactionDTO.Response::fromEntity)
                .collect(Collectors.toList());
    }

    public Transaction findEntityByIdAndUser(UUID id, User user) {
        return transactionRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Transação não encontrada: " + id));
    }

    @Transactional
    public TransactionDTO.Response create(TransactionDTO.Request request, User user) {
        Account account = accountService.findEntityByIdAndUser(request.getAccountId(), user);

        Account destinationAccount = null;
        if (request.getType() == TransactionType.TRANSFER) {
            if (request.getDestinationAccountId() == null) {
                throw new BusinessException("A conta de destino é obrigatória para transferências.");
            }
            if (request.getAccountId().equals(request.getDestinationAccountId())) {
                throw new BusinessException("A conta de origem e destino não podem ser as mesmas.");
            }
            destinationAccount = accountService.findEntityByIdAndUser(request.getDestinationAccountId(), user);
        }

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId()).orElse(null);
        }

        boolean isPaid = request.getIsPaid() == null || request.getIsPaid();

        Transaction transaction = Transaction.builder()
                .user(user)
                .account(account)
                .category(category)
                .destinationAccount(destinationAccount)
                .type(request.getType())
                .amount(request.getAmount())
                .transactionDate(request.getTransactionDate())
                .description(request.getDescription().trim())
                .isPaid(isPaid)
                .build();

        applyBalance(transaction);

        Transaction saved = transactionRepository.save(transaction);
        return TransactionDTO.Response.fromEntity(saved);
    }

    @Transactional
    public TransactionDTO.Response update(UUID id, TransactionDTO.Request request, User user) {
        Transaction transaction = findEntityByIdAndUser(id, user);

        // Reverter impacto financeiro anterior
        revertBalance(transaction);

        Account account = accountService.findEntityByIdAndUser(request.getAccountId(), user);
        Account destinationAccount = null;
        if (request.getType() == TransactionType.TRANSFER) {
            if (request.getDestinationAccountId() == null) {
                throw new BusinessException("A conta de destino é obrigatória para transferências.");
            }
            if (request.getAccountId().equals(request.getDestinationAccountId())) {
                throw new BusinessException("A conta de origem e destino não podem ser as mesmas.");
            }
            destinationAccount = accountService.findEntityByIdAndUser(request.getDestinationAccountId(), user);
        }

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId()).orElse(null);
        }

        boolean isPaid = request.getIsPaid() == null || request.getIsPaid();

        transaction.setAccount(account);
        transaction.setDestinationAccount(destinationAccount);
        transaction.setCategory(category);
        transaction.setType(request.getType());
        transaction.setAmount(request.getAmount());
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setDescription(request.getDescription().trim());
        transaction.setPaid(isPaid);

        // Aplicar novo saldo atualizado
        applyBalance(transaction);

        Transaction updated = transactionRepository.save(transaction);
        return TransactionDTO.Response.fromEntity(updated);
    }

    @Transactional
    public void delete(UUID id, User user) {
        Transaction transaction = findEntityByIdAndUser(id, user);
        revertBalance(transaction);
        transactionRepository.delete(transaction);
    }

    private void applyBalance(Transaction t) {
        if (!t.isPaid()) return;

        Account source = t.getAccount();
        if (t.getType() == TransactionType.INCOME) {
            source.setBalance(source.getBalance().add(t.getAmount()));
            accountRepository.save(source);
        } else if (t.getType() == TransactionType.EXPENSE) {
            source.setBalance(source.getBalance().subtract(t.getAmount()));
            accountRepository.save(source);
        } else if (t.getType() == TransactionType.TRANSFER) {
            Account dest = t.getDestinationAccount();
            source.setBalance(source.getBalance().subtract(t.getAmount()));
            dest.setBalance(dest.getBalance().add(t.getAmount()));
            accountRepository.save(source);
            accountRepository.save(dest);
        }
    }

    private void revertBalance(Transaction t) {
        if (!t.isPaid()) return;

        Account source = t.getAccount();
        if (t.getType() == TransactionType.INCOME) {
            source.setBalance(source.getBalance().subtract(t.getAmount()));
            accountRepository.save(source);
        } else if (t.getType() == TransactionType.EXPENSE) {
            source.setBalance(source.getBalance().add(t.getAmount()));
            accountRepository.save(source);
        } else if (t.getType() == TransactionType.TRANSFER) {
            Account dest = t.getDestinationAccount();
            source.setBalance(source.getBalance().add(t.getAmount()));
            dest.setBalance(dest.getBalance().subtract(t.getAmount()));
            accountRepository.save(source);
            accountRepository.save(dest);
        }
    }
}
