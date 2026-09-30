package com.gestaofinanceira.modules.market;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BrapiResponse {
    private List<Result> results;

    public BrapiResponse() {}

    public BrapiResponse(List<Result> results) {
        this.results = results;
    }

    public List<Result> getResults() { return results; }
    public void setResults(List<Result> results) { this.results = results; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Result {
        private String symbol;
        private String shortName;
        private String longName;
        private BigDecimal regularMarketPrice;
        private BigDecimal regularMarketChangePercent;
        private String regularMarketTime;

        public Result() {}

        public Result(String symbol, String shortName, String longName, BigDecimal regularMarketPrice, BigDecimal regularMarketChangePercent, String regularMarketTime) {
            this.symbol = symbol;
            this.shortName = shortName;
            this.longName = longName;
            this.regularMarketPrice = regularMarketPrice;
            this.regularMarketChangePercent = regularMarketChangePercent;
            this.regularMarketTime = regularMarketTime;
        }

        public String getSymbol() { return symbol; }
        public void setSymbol(String symbol) { this.symbol = symbol; }

        public String getShortName() { return shortName; }
        public void setShortName(String shortName) { this.shortName = shortName; }

        public String getLongName() { return longName; }
        public void setLongName(String longName) { this.longName = longName; }

        public BigDecimal getRegularMarketPrice() { return regularMarketPrice; }
        public void setRegularMarketPrice(BigDecimal regularMarketPrice) { this.regularMarketPrice = regularMarketPrice; }

        public BigDecimal getRegularMarketChangePercent() { return regularMarketChangePercent; }
        public void setRegularMarketChangePercent(BigDecimal regularMarketChangePercent) { this.regularMarketChangePercent = regularMarketChangePercent; }

        public String getRegularMarketTime() { return regularMarketTime; }
        public void setRegularMarketTime(String regularMarketTime) { this.regularMarketTime = regularMarketTime; }
    }
}
