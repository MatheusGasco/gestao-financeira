package com.gestaofinanceira.modules.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class AccountDTO {

    public static class Response {
        private UUID id;
        private String name;
        private AccountType type;
        private BigDecimal balance;
        private String currency;
        private String color;
        private LocalDateTime createdAt;

        public Response() {}

        public Response(UUID id, String name, AccountType type, BigDecimal balance, String currency, String color, LocalDateTime createdAt) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.balance = balance;
            this.currency = currency;
            this.color = color;
            this.createdAt = createdAt;
        }

        public static Response fromEntity(Account account) {
            return new Response(
                    account.getId(),
                    account.getName(),
                    account.getType(),
                    account.getBalance(),
                    account.getCurrency(),
                    account.getColor(),
                    account.getCreatedAt()
            );
        }

        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public AccountType getType() { return type; }
        public void setType(AccountType type) { this.type = type; }

        public BigDecimal getBalance() { return balance; }
        public void setBalance(BigDecimal balance) { this.balance = balance; }

        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }

        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }

    public static class Request {
        @NotBlank(message = "O nome da conta é obrigatório")
        private String name;

        @NotNull(message = "O tipo de conta é obrigatório")
        private AccountType type;

        private BigDecimal initialBalance;
        private String currency;
        private String color;

        public Request() {}

        public Request(String name, AccountType type, BigDecimal initialBalance, String currency, String color) {
            this.name = name;
            this.type = type;
            this.initialBalance = initialBalance;
            this.currency = currency;
            this.color = color;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public AccountType getType() { return type; }
        public void setType(AccountType type) { this.type = type; }

        public BigDecimal getInitialBalance() { return initialBalance; }
        public void setInitialBalance(BigDecimal initialBalance) { this.initialBalance = initialBalance; }

        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }

        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }
    }
}
