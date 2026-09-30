package com.gestaofinanceira.modules.market;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MarketQuoteDTO {
    private String ticker;
    private String name;
    private BigDecimal price;
    private BigDecimal changePercentage;
    private LocalDateTime updatedAt;
    private boolean fromFallback;

    public MarketQuoteDTO() {}

    public MarketQuoteDTO(String ticker, String name, BigDecimal price, BigDecimal changePercentage, LocalDateTime updatedAt, boolean fromFallback) {
        this.ticker = ticker;
        this.name = name;
        this.price = price;
        this.changePercentage = changePercentage;
        this.updatedAt = updatedAt;
        this.fromFallback = fromFallback;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String ticker;
        private String name;
        private BigDecimal price;
        private BigDecimal changePercentage;
        private LocalDateTime updatedAt;
        private boolean fromFallback;

        public Builder ticker(String ticker) { this.ticker = ticker; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder price(BigDecimal price) { this.price = price; return this; }
        public Builder changePercentage(BigDecimal changePercentage) { this.changePercentage = changePercentage; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        public Builder fromFallback(boolean fromFallback) { this.fromFallback = fromFallback; return this; }

        public MarketQuoteDTO build() {
            return new MarketQuoteDTO(ticker, name, price, changePercentage, updatedAt, fromFallback);
        }
    }

    public String getTicker() { return ticker; }
    public void setTicker(String ticker) { this.ticker = ticker; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public BigDecimal getChangePercentage() { return changePercentage; }
    public void setChangePercentage(BigDecimal changePercentage) { this.changePercentage = changePercentage; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public boolean isFromFallback() { return fromFallback; }
    public void setFromFallback(boolean fromFallback) { this.fromFallback = fromFallback; }
}
