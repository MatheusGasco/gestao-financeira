package com.gestaofinanceira.modules.transaction;

import com.gestaofinanceira.modules.account.AccountDTO;
import com.gestaofinanceira.modules.category.CategoryDTO;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class TransactionDTO {

    public static class Response {
        private UUID id;
        private AccountDTO.Response account;
        private CategoryDTO.Response category;
        private AccountDTO.Response destinationAccount;
        private TransactionType type;
        private BigDecimal amount;
        private LocalDate transactionDate;
        private String description;
        private boolean isPaid;
        private LocalDateTime createdAt;

        public Response() {}

        public Response(UUID id, AccountDTO.Response account, CategoryDTO.Response category, AccountDTO.Response destinationAccount, TransactionType type, BigDecimal amount, LocalDate transactionDate, String description, boolean isPaid, LocalDateTime createdAt) {
            this.id = id;
            this.account = account;
            this.category = category;
            this.destinationAccount = destinationAccount;
            this.type = type;
            this.amount = amount;
            this.transactionDate = transactionDate;
            this.description = description;
            this.isPaid = isPaid;
            this.createdAt = createdAt;
        }

        public static Response fromEntity(Transaction t) {
            return new Response(
                    t.getId(),
                    AccountDTO.Response.fromEntity(t.getAccount()),
                    t.getCategory() != null ? CategoryDTO.Response.fromEntity(t.getCategory()) : null,
                    t.getDestinationAccount() != null ? AccountDTO.Response.fromEntity(t.getDestinationAccount()) : null,
                    t.getType(),
                    t.getAmount(),
                    t.getTransactionDate(),
                    t.getDescription(),
                    t.isPaid(),
                    t.getCreatedAt()
            );
        }

        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }

        public AccountDTO.Response getAccount() { return account; }
        public void setAccount(AccountDTO.Response account) { this.account = account; }

        public CategoryDTO.Response getCategory() { return category; }
        public void setCategory(CategoryDTO.Response category) { this.category = category; }

        public AccountDTO.Response getDestinationAccount() { return destinationAccount; }
        public void setDestinationAccount(AccountDTO.Response destinationAccount) { this.destinationAccount = destinationAccount; }

        public TransactionType getType() { return type; }
        public void setType(TransactionType type) { this.type = type; }

        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }

        public LocalDate getTransactionDate() { return transactionDate; }
        public void setTransactionDate(LocalDate transactionDate) { this.transactionDate = transactionDate; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public boolean isPaid() { return isPaid; }
        public void setPaid(boolean paid) { isPaid = paid; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }

    public static class Request {
        @NotNull(message = "A conta é obrigatória")
        private UUID accountId;

        private UUID categoryId;

        private UUID destinationAccountId;

        @NotNull(message = "O tipo da transação é obrigatório")
        private TransactionType type;

        @NotNull(message = "O valor é obrigatório")
        @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero")
        private BigDecimal amount;

        @NotNull(message = "A data da transação é obrigatória")
        private LocalDate transactionDate;

        @NotBlank(message = "A descrição é obrigatória")
        private String description;

        private Boolean isPaid;

        public Request() {}

        public Request(UUID accountId, UUID categoryId, UUID destinationAccountId, TransactionType type, BigDecimal amount, LocalDate transactionDate, String description, Boolean isPaid) {
            this.accountId = accountId;
            this.categoryId = categoryId;
            this.destinationAccountId = destinationAccountId;
            this.type = type;
            this.amount = amount;
            this.transactionDate = transactionDate;
            this.description = description;
            this.isPaid = isPaid;
        }

        public UUID getAccountId() { return accountId; }
        public void setAccountId(UUID accountId) { this.accountId = accountId; }

        public UUID getCategoryId() { return categoryId; }
        public void setCategoryId(UUID categoryId) { this.categoryId = categoryId; }

        public UUID getDestinationAccountId() { return destinationAccountId; }
        public void setDestinationAccountId(UUID destinationAccountId) { this.destinationAccountId = destinationAccountId; }

        public TransactionType getType() { return type; }
        public void setType(TransactionType type) { this.type = type; }

        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }

        public LocalDate getTransactionDate() { return transactionDate; }
        public void setTransactionDate(LocalDate transactionDate) { this.transactionDate = transactionDate; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public Boolean getIsPaid() { return isPaid; }
        public void setIsPaid(Boolean isPaid) { this.isPaid = isPaid; }
    }
}
