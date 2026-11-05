package com.gestaofinanceira.modules.investment;

import com.gestaofinanceira.modules.account.Account;
import com.gestaofinanceira.modules.asset.Asset;
import com.gestaofinanceira.modules.user.User;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "investment_transactions")
public class InvestmentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "account_id")
    private Account account;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private InvestmentTransactionType type;

    @Column(nullable = false, precision = 15, scale = 4)
    private BigDecimal quantity;

    @Column(nullable = false, precision = 15, scale = 4)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal fees = BigDecimal.ZERO;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public InvestmentTransaction() {}

    public InvestmentTransaction(UUID id, User user, Account account, Asset asset, InvestmentTransactionType type, BigDecimal quantity, BigDecimal unitPrice, BigDecimal fees, BigDecimal totalAmount, LocalDate transactionDate, LocalDateTime createdAt) {
        this.id = id;
        this.user = user;
        this.account = account;
        this.asset = asset;
        this.type = type;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.fees = fees != null ? fees : BigDecimal.ZERO;
        this.totalAmount = totalAmount;
        this.transactionDate = transactionDate;
        this.createdAt = createdAt;
    }

    public InvestmentTransaction(UUID id, User user, Asset asset, InvestmentTransactionType type, BigDecimal quantity, BigDecimal unitPrice, BigDecimal fees, BigDecimal totalAmount, LocalDate transactionDate, LocalDateTime createdAt) {
        this(id, user, null, asset, type, quantity, unitPrice, fees, totalAmount, transactionDate, createdAt);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private User user;
        private Account account;
        private Asset asset;
        private InvestmentTransactionType type;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
        private BigDecimal fees = BigDecimal.ZERO;
        private BigDecimal totalAmount;
        private LocalDate transactionDate;
        private LocalDateTime createdAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder user(User user) { this.user = user; return this; }
        public Builder account(Account account) { this.account = account; return this; }
        public Builder asset(Asset asset) { this.asset = asset; return this; }
        public Builder type(InvestmentTransactionType type) { this.type = type; return this; }
        public Builder quantity(BigDecimal quantity) { this.quantity = quantity; return this; }
        public Builder unitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; return this; }
        public Builder fees(BigDecimal fees) { this.fees = fees; return this; }
        public Builder totalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; return this; }
        public Builder transactionDate(LocalDate transactionDate) { this.transactionDate = transactionDate; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public InvestmentTransaction build() {
            return new InvestmentTransaction(id, user, account, asset, type, quantity, unitPrice, fees, totalAmount, transactionDate, createdAt);
        }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }

    public Asset getAsset() { return asset; }
    public void setAsset(Asset asset) { this.asset = asset; }

    public InvestmentTransactionType getType() { return type; }
    public void setType(InvestmentTransactionType type) { this.type = type; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public BigDecimal getFees() { return fees; }
    public void setFees(BigDecimal fees) { this.fees = fees; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public LocalDate getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDate transactionDate) { this.transactionDate = transactionDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
