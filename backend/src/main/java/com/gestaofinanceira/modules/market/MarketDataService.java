package com.gestaofinanceira.modules.market;

import com.gestaofinanceira.modules.asset.Asset;
import com.gestaofinanceira.modules.asset.AssetRepository;
import com.gestaofinanceira.modules.asset.AssetType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class MarketDataService {

    private static final Logger log = LoggerFactory.getLogger(MarketDataService.class);

    private final AssetRepository assetRepository;

    @Value("${brapi.base-url:https://brapi.dev/api}")
    private String brapiBaseUrl;

    @Value("${brapi.token:}")
    private String brapiToken;

    private static final Map<String, BigDecimal> MOCK_PRICES = new HashMap<>();

    static {
        MOCK_PRICES.put("PETR4", new BigDecimal("38.50"));
        MOCK_PRICES.put("VALE3", new BigDecimal("62.30"));
        MOCK_PRICES.put("ITUB4", new BigDecimal("35.75"));
        MOCK_PRICES.put("BBDC4", new BigDecimal("14.20"));
        MOCK_PRICES.put("BBAS3", new BigDecimal("28.90"));
        MOCK_PRICES.put("WEGE3", new BigDecimal("54.10"));
        MOCK_PRICES.put("MGLU3", new BigDecimal("9.80"));
        MOCK_PRICES.put("MXRF11", new BigDecimal("10.45"));
        MOCK_PRICES.put("HGLG11", new BigDecimal("165.20"));
        MOCK_PRICES.put("KNIP11", new BigDecimal("94.30"));
        MOCK_PRICES.put("XPML11", new BigDecimal("112.50"));
        MOCK_PRICES.put("IVVB11", new BigDecimal("325.40"));
        MOCK_PRICES.put("BOVA11", new BigDecimal("128.60"));
        MOCK_PRICES.put("SMAL11", new BigDecimal("104.20"));
    }

    public MarketDataService(AssetRepository assetRepository) {
        this.assetRepository = assetRepository;
    }

    @Transactional
    public MarketQuoteDTO getQuote(String ticker) {
        String cleanTicker = ticker.trim().toUpperCase();

        try {
            RestClient restClient = RestClient.builder().baseUrl(brapiBaseUrl).build();
            String uri = "/quote/" + cleanTicker;
            if (brapiToken != null && !brapiToken.isBlank()) {
                uri += "?token=" + brapiToken;
            }

            BrapiResponse response = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(BrapiResponse.class);

            if (response != null && response.getResults() != null && !response.getResults().isEmpty()) {
                BrapiResponse.Result result = response.getResults().get(0);
                if (result.getRegularMarketPrice() != null) {
                    BigDecimal price = result.getRegularMarketPrice();
                    BigDecimal change = result.getRegularMarketChangePercent() != null
                            ? result.getRegularMarketChangePercent()
                            : BigDecimal.ZERO;
                    String name = result.getLongName() != null ? result.getLongName() :
                            (result.getShortName() != null ? result.getShortName() : cleanTicker);

                    Asset asset = updateOrCreateAsset(cleanTicker, name, price, change);

                    return MarketQuoteDTO.builder()
                            .ticker(asset.getTicker())
                            .name(asset.getName())
                            .price(price)
                            .changePercentage(change)
                            .updatedAt(asset.getLastPriceUpdate())
                            .fromFallback(false)
                            .build();
                }
            }
        } catch (Exception e) {
            log.warn("Falha ao consultar Brapi para ticker {}: {}. Utilizando estratégia de fallback.", cleanTicker, e.getMessage());
        }

        return getFallbackQuote(cleanTicker);
    }

    @Transactional
    public MarketQuoteDTO getFallbackQuote(String cleanTicker) {
        Optional<Asset> existingOpt = assetRepository.findByTickerIgnoreCase(cleanTicker);

        BigDecimal price;
        BigDecimal change = new BigDecimal("0.75");
        String name = cleanTicker;

        if (existingOpt.isPresent() && existingOpt.get().getCurrentPrice().compareTo(BigDecimal.ZERO) > 0) {
            Asset existing = existingOpt.get();
            price = existing.getCurrentPrice();
            change = existing.getDailyChangePercentage();
            name = existing.getName();
        } else if (MOCK_PRICES.containsKey(cleanTicker)) {
            price = MOCK_PRICES.get(cleanTicker);
            name = cleanTicker + " Cotação Referência";
        } else {
            price = new BigDecimal("50.00");
            name = cleanTicker + " Ativo B3";
        }

        Asset asset = updateOrCreateAsset(cleanTicker, name, price, change);

        return MarketQuoteDTO.builder()
                .ticker(asset.getTicker())
                .name(asset.getName())
                .price(price)
                .changePercentage(change)
                .updatedAt(LocalDateTime.now())
                .fromFallback(true)
                .build();
    }

    private Asset updateOrCreateAsset(String ticker, String name, BigDecimal price, BigDecimal change) {
        Asset asset = assetRepository.findByTickerIgnoreCase(ticker)
                .orElseGet(() -> Asset.builder()
                        .ticker(ticker)
                        .name(name)
                        .type(inferAssetType(ticker))
                        .build());

        asset.setCurrentPrice(price.setScale(4, RoundingMode.HALF_UP));
        asset.setDailyChangePercentage(change.setScale(4, RoundingMode.HALF_UP));
        if (asset.getName() == null || asset.getName().isBlank()) {
            asset.setName(name);
        }
        asset.setLastPriceUpdate(LocalDateTime.now());
        return assetRepository.save(asset);
    }

    public static AssetType inferAssetType(String ticker) {
        if (ticker.endsWith("11")) {
            if (ticker.startsWith("IVVB") || ticker.startsWith("BOVA") || ticker.startsWith("SMAL") || ticker.startsWith("HASH")) {
                return AssetType.ETF;
            }
            return AssetType.FII;
        } else if (ticker.endsWith("34") || ticker.endsWith("35")) {
            return AssetType.BDR;
        } else {
            return AssetType.STOCK;
        }
    }
}
