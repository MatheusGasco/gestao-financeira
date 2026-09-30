package com.gestaofinanceira.modules.asset;

import jakarta.persistence.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "assets")
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 12)
    private String ticker;

    @Column(nullable = false, length = 120)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AssetType type;

    @Column(name = "current_price", nullable = false, precision = 15, scale = 4)
    private BigDecimal currentPrice = BigDecimal.ZERO;

    @Column(name = "daily_change_percentage", precision = 8, scale = 4)
    private BigDecimal dailyChangePercentage = BigDecimal.ZERO;

    @UpdateTimestamp
    @Column(name = "last_price_update")
    private LocalDateTime lastPriceUpdate;

    public Asset() {}

    public Asset(UUID id, String ticker, String name, AssetType type, BigDecimal currentPrice, BigDecimal dailyChangePercentage, LocalDateTime lastPriceUpdate) {
        this.id = id;
        this.ticker = ticker;
        this.name = name;
        this.type = type;
        this.currentPrice = currentPrice != null ? currentPrice : BigDecimal.ZERO;
        this.dailyChangePercentage = dailyChangePercentage != null ? dailyChangePercentage : BigDecimal.ZERO;
        this.lastPriceUpdate = lastPriceUpdate;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private String ticker;
        private String name;
        private AssetType type;
        private BigDecimal currentPrice = BigDecimal.ZERO;
        private BigDecimal dailyChangePercentage = BigDecimal.ZERO;
        private LocalDateTime lastPriceUpdate;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder ticker(String ticker) { this.ticker = ticker; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder type(AssetType type) { this.type = type; return this; }
        public Builder currentPrice(BigDecimal currentPrice) { this.currentPrice = currentPrice; return this; }
        public Builder dailyChangePercentage(BigDecimal dailyChangePercentage) { this.dailyChangePercentage = dailyChangePercentage; return this; }
        public Builder lastPriceUpdate(LocalDateTime lastPriceUpdate) { this.lastPriceUpdate = lastPriceUpdate; return this; }

        public Asset build() {
            return new Asset(id, ticker, name, type, currentPrice, dailyChangePercentage, lastPriceUpdate);
        }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTicker() { return ticker; }
    public void setTicker(String ticker) { this.ticker = ticker; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public AssetType getType() { return type; }
    public void setType(AssetType type) { this.type = type; }

    public BigDecimal getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(BigDecimal currentPrice) { this.currentPrice = currentPrice; }

    public BigDecimal getDailyChangePercentage() { return dailyChangePercentage; }
    public void setDailyChangePercentage(BigDecimal dailyChangePercentage) { this.dailyChangePercentage = dailyChangePercentage; }

    public LocalDateTime getLastPriceUpdate() { return lastPriceUpdate; }
    public void setLastPriceUpdate(LocalDateTime lastPriceUpdate) { this.lastPriceUpdate = lastPriceUpdate; }
}
