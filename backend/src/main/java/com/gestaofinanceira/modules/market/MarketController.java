package com.gestaofinanceira.modules.market;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/market")
public class MarketController {

    private final MarketDataService marketDataService;

    public MarketController(MarketDataService marketDataService) {
        this.marketDataService = marketDataService;
    }

    @GetMapping("/quote/{ticker}")
    public ResponseEntity<MarketQuoteDTO> getQuote(@PathVariable String ticker) {
        return ResponseEntity.ok(marketDataService.getQuote(ticker));
    }
}
