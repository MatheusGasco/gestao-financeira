package com.gestaofinanceira.modules.transaction;

import com.gestaofinanceira.modules.account.Account;
import com.gestaofinanceira.modules.category.Category;
import com.gestaofinanceira.modules.user.User;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "destination_account_id")
    private Account destinationAccount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(nullable = false, length = 200)
    private String description;

    @Column(nullable = false)
    private boolean isPaid = true;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public Transaction() {}

    public Transaction(UUID id, User user, Account account, Category category, Account destinationAccount, TransactionType type, BigDecimal amount, LocalDate transactionDate, String description, boolean isPaid, LocalDateTime createdAt) {
        this.id = id;
        this.user = user;
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

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private User user;
        private Account account;
        private Category category;
        private Account destinationAccount;
        private TransactionType type;
        private BigDecimal amount;
        private LocalDate transactionDate;
        private String description;
        private boolean isPaid = true;
        private LocalDateTime createdAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder user(User user) { this.user = user; return this; }
        public Builder account(Account account) { this.account = account; return this; }
        public Builder category(Category category) { this.category = category; return this; }
        public Builder destinationAccount(Account destinationAccount) { this.destinationAccount = destinationAccount; return this; }
        public Builder type(TransactionType type) { this.type = type; return this; }
        public Builder amount(BigDecimal amount) { this.amount = amount; return this; }
        public Builder transactionDate(LocalDate transactionDate) { this.transactionDate = transactionDate; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder isPaid(boolean isPaid) { this.isPaid = isPaid; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public Transaction build() {
            return new Transaction(id, user, account, category, destinationAccount, type, amount, transactionDate, description, isPaid, createdAt);
        }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public Account getDestinationAccount() { return destinationAccount; }
    public void setDestinationAccount(Account destinationAccount) { this.destinationAccount = destinationAccount; }

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
