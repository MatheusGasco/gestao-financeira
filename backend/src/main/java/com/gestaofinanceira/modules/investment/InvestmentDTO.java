package com.gestaofinanceira.modules.investment;

import com.gestaofinanceira.modules.asset.AssetType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class InvestmentDTO {

    public static class OrderRequest {
        @NotBlank(message = "O ticker do ativo é obrigatório")
        private String ticker;

        @NotNull(message = "O tipo de operação é obrigatório (BUY/SELL)")
        private InvestmentTransactionType type;

        @NotNull(message = "A quantidade é obrigatória")
        @DecimalMin(value = "0.0001", message = "A quantidade deve ser maior que zero")
        private BigDecimal quantity;

        @NotNull(message = "O preço unitário é obrigatório")
        @DecimalMin(value = "0.0001", message = "O preço deve ser maior que zero")
        private BigDecimal unitPrice;

        private BigDecimal fees;

        @NotNull(message = "A data da operação é obrigatória")
        private LocalDate transactionDate;

        @NotNull(message = "A conta bancária é obrigatória")
        private UUID accountId;

        public OrderRequest() {}

        public OrderRequest(String ticker, InvestmentTransactionType type, BigDecimal quantity, BigDecimal unitPrice, BigDecimal fees, LocalDate transactionDate, UUID accountId) {
            this.ticker = ticker;
            this.type = type;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.fees = fees;
            this.transactionDate = transactionDate;
            this.accountId = accountId;
        }

        public OrderRequest(String ticker, InvestmentTransactionType type, BigDecimal quantity, BigDecimal unitPrice, BigDecimal fees, LocalDate transactionDate) {
            this(ticker, type, quantity, unitPrice, fees, transactionDate, null);
        }

        public String getTicker() { return ticker; }
        public void setTicker(String ticker) { this.ticker = ticker; }

        public InvestmentTransactionType getType() { return type; }
        public void setType(InvestmentTransactionType type) { this.type = type; }

        public BigDecimal getQuantity() { return quantity; }
        public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

        public BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

        public BigDecimal getFees() { return fees; }
        public void setFees(BigDecimal fees) { this.fees = fees; }

        public LocalDate getTransactionDate() { return transactionDate; }
        public void setTransactionDate(LocalDate transactionDate) { this.transactionDate = transactionDate; }

        public UUID getAccountId() { return accountId; }
        public void setAccountId(UUID accountId) { this.accountId = accountId; }
    }

    public static class OrderResponse {
        private UUID id;
        private String ticker;
        private String assetName;
        private AssetType assetType;
        private InvestmentTransactionType type;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
        private BigDecimal fees;
        private BigDecimal totalAmount;
        private LocalDate transactionDate;
        private LocalDateTime createdAt;
        private UUID accountId;
        private String accountName;

        public OrderResponse() {}

        public OrderResponse(UUID id, String ticker, String assetName, AssetType assetType, InvestmentTransactionType type, BigDecimal quantity, BigDecimal unitPrice, BigDecimal fees, BigDecimal totalAmount, LocalDate transactionDate, LocalDateTime createdAt, UUID accountId, String accountName) {
            this.id = id;
            this.ticker = ticker;
            this.assetName = assetName;
            this.assetType = assetType;
            this.type = type;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.fees = fees;
            this.totalAmount = totalAmount;
            this.transactionDate = transactionDate;
            this.createdAt = createdAt;
            this.accountId = accountId;
            this.accountName = accountName;
        }

        public OrderResponse(UUID id, String ticker, String assetName, AssetType assetType, InvestmentTransactionType type, BigDecimal quantity, BigDecimal unitPrice, BigDecimal fees, BigDecimal totalAmount, LocalDate transactionDate, LocalDateTime createdAt) {
            this(id, ticker, assetName, assetType, type, quantity, unitPrice, fees, totalAmount, transactionDate, createdAt, null, null);
        }

        public static OrderResponse fromEntity(InvestmentTransaction t) {
            UUID accId = t.getAccount() != null ? t.getAccount().getId() : null;
            String accName = t.getAccount() != null ? t.getAccount().getName() : null;
            return new OrderResponse(
                    t.getId(),
                    t.getAsset().getTicker(),
                    t.getAsset().getName(),
                    t.getAsset().getType(),
                    t.getType(),
                    t.getQuantity(),
                    t.getUnitPrice(),
                    t.getFees(),
                    t.getTotalAmount(),
                    t.getTransactionDate(),
                    t.getCreatedAt(),
                    accId,
                    accName
            );
        }

        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }

        public String getTicker() { return ticker; }
        public void setTicker(String ticker) { this.ticker = ticker; }

        public String getAssetName() { return assetName; }
        public void setAssetName(String assetName) { this.assetName = assetName; }

        public AssetType getAssetType() { return assetType; }
        public void setAssetType(AssetType assetType) { this.assetType = assetType; }

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

        public UUID getAccountId() { return accountId; }
        public void setAccountId(UUID accountId) { this.accountId = accountId; }

        public String getAccountName() { return accountName; }
        public void setAccountName(String accountName) { this.accountName = accountName; }
    }

    public static class PositionResponse {
        private UUID assetId;
        private String ticker;
        private String name;
        private AssetType type;
        private BigDecimal quantity;
        private BigDecimal averagePrice;
        private BigDecimal totalCost;
        private BigDecimal currentPrice;
        private BigDecimal currentValue;
        private BigDecimal profitLoss;
        private BigDecimal profitLossPercentage;
        private BigDecimal dailyChangePercentage;

        public PositionResponse() {}

        public PositionResponse(UUID assetId, String ticker, String name, AssetType type, BigDecimal quantity, BigDecimal averagePrice, BigDecimal totalCost, BigDecimal currentPrice, BigDecimal currentValue, BigDecimal profitLoss, BigDecimal profitLossPercentage, BigDecimal dailyChangePercentage) {
            this.assetId = assetId;
            this.ticker = ticker;
            this.name = name;
            this.type = type;
            this.quantity = quantity;
            this.averagePrice = averagePrice;
            this.totalCost = totalCost;
            this.currentPrice = currentPrice;
            this.currentValue = currentValue;
            this.profitLoss = profitLoss;
            this.profitLossPercentage = profitLossPercentage;
            this.dailyChangePercentage = dailyChangePercentage;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private UUID assetId;
            private String ticker;
            private String name;
            private AssetType type;
            private BigDecimal quantity;
            private BigDecimal averagePrice;
            private BigDecimal totalCost;
            private BigDecimal currentPrice;
            private BigDecimal currentValue;
            private BigDecimal profitLoss;
            private BigDecimal profitLossPercentage;
            private BigDecimal dailyChangePercentage;

            public Builder assetId(UUID assetId) { this.assetId = assetId; return this; }
            public Builder ticker(String ticker) { this.ticker = ticker; return this; }
            public Builder name(String name) { this.name = name; return this; }
            public Builder type(AssetType type) { this.type = type; return this; }
            public Builder quantity(BigDecimal quantity) { this.quantity = quantity; return this; }
            public Builder averagePrice(BigDecimal averagePrice) { this.averagePrice = averagePrice; return this; }
            public Builder totalCost(BigDecimal totalCost) { this.totalCost = totalCost; return this; }
            public Builder currentPrice(BigDecimal currentPrice) { this.currentPrice = currentPrice; return this; }
            public Builder currentValue(BigDecimal currentValue) { this.currentValue = currentValue; return this; }
            public Builder profitLoss(BigDecimal profitLoss) { this.profitLoss = profitLoss; return this; }
            public Builder profitLossPercentage(BigDecimal profitLossPercentage) { this.profitLossPercentage = profitLossPercentage; return this; }
            public Builder dailyChangePercentage(BigDecimal dailyChangePercentage) { this.dailyChangePercentage = dailyChangePercentage; return this; }

            public PositionResponse build() {
                return new PositionResponse(assetId, ticker, name, type, quantity, averagePrice, totalCost, currentPrice, currentValue, profitLoss, profitLossPercentage, dailyChangePercentage);
            }
        }

        public UUID getAssetId() { return assetId; }
        public void setAssetId(UUID assetId) { this.assetId = assetId; }

        public String getTicker() { return ticker; }
        public void setTicker(String ticker) { this.ticker = ticker; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public AssetType getType() { return type; }
        public void setType(AssetType type) { this.type = type; }

        public BigDecimal getQuantity() { return quantity; }
        public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

        public BigDecimal getAveragePrice() { return averagePrice; }
        public void setAveragePrice(BigDecimal averagePrice) { this.averagePrice = averagePrice; }

        public BigDecimal getTotalCost() { return totalCost; }
        public void setTotalCost(BigDecimal totalCost) { this.totalCost = totalCost; }

        public BigDecimal getCurrentPrice() { return currentPrice; }
        public void setCurrentPrice(BigDecimal currentPrice) { this.currentPrice = currentPrice; }

        public BigDecimal getCurrentValue() { return currentValue; }
        public void setCurrentValue(BigDecimal currentValue) { this.currentValue = currentValue; }

        public BigDecimal getProfitLoss() { return profitLoss; }
        public void setProfitLoss(BigDecimal profitLoss) { this.profitLoss = profitLoss; }

        public BigDecimal getProfitLossPercentage() { return profitLossPercentage; }
        public void setProfitLossPercentage(BigDecimal profitLossPercentage) { this.profitLossPercentage = profitLossPercentage; }

        public BigDecimal getDailyChangePercentage() { return dailyChangePercentage; }
        public void setDailyChangePercentage(BigDecimal dailyChangePercentage) { this.dailyChangePercentage = dailyChangePercentage; }
    }
}
