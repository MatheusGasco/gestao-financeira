package com.gestaofinanceira.modules.market;

import com.gestaofinanceira.modules.asset.Asset;
import com.gestaofinanceira.modules.asset.AssetRepository;
import com.gestaofinanceira.modules.asset.AssetType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class MarketDataService {

    private static final Logger log = LoggerFactory.getLogger(MarketDataService.class);

    private final AssetRepository assetRepository;

    @Value("${brapi.base-url:https://brapi.dev/api}")
    private String brapiBaseUrl;

    @Value("${brapi.token:}")
    private String brapiToken;

    @Value("${brapi.cache-ttl-seconds:300}")
    private long cacheTtlSeconds;

    public MarketDataService(AssetRepository assetRepository) {
        this.assetRepository = assetRepository;
    }

    @Transactional
    public MarketQuoteDTO getQuote(String ticker) {
        if (ticker == null || ticker.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Informe o código do ativo."
            );
        }

        String cleanTicker = ticker.trim().toUpperCase();

        Optional<Asset> existingOpt =
                assetRepository.findByTickerIgnoreCase(cleanTicker);

        LocalDateTime now = LocalDateTime.now();

        if (existingOpt.isPresent()) {
            Asset existing = existingOpt.get();

            if (existing.getCurrentPrice() != null
                    && existing.getCurrentPrice().compareTo(BigDecimal.ZERO) > 0
                    && existing.getLastPriceUpdate() != null
                    && cacheTtlSeconds > 0
                    && existing.getLastPriceUpdate().isAfter(
                            now.minusSeconds(cacheTtlSeconds))) {

                log.info("Cotação {} recuperada do cache.", cleanTicker);
                return buildQuote(existing, false);
            }
        }

        try {
            RestClient restClient = RestClient.builder()
                    .baseUrl(brapiBaseUrl)
                    .build();

            BrapiResponse response = restClient.get()
                    .uri(uriBuilder -> {
                        var builder = uriBuilder.path("/quote/{ticker}");

                        if (brapiToken != null && !brapiToken.isBlank()) {
                            builder.queryParam("token", brapiToken);
                        }

                        return builder.build(cleanTicker);
                    })
                    .retrieve()
                    .body(BrapiResponse.class);

            if (response != null
                    && response.getResults() != null
                    && !response.getResults().isEmpty()) {

                BrapiResponse.Result result = response.getResults().get(0);

                if (result.getRegularMarketPrice() != null
                        && result.getRegularMarketPrice()
                        .compareTo(BigDecimal.ZERO) > 0) {

                    BigDecimal price = result.getRegularMarketPrice();

                    BigDecimal change =
                            result.getRegularMarketChangePercent() != null
                                    ? result.getRegularMarketChangePercent()
                                    : BigDecimal.ZERO;

                    String name = result.getLongName() != null
                            ? result.getLongName()
                            : result.getShortName() != null
                            ? result.getShortName()
                            : cleanTicker;

                    Asset asset = updateOrCreateAsset(
                            cleanTicker, name, price, change
                    );

                    log.info("Cotação {} atualizada pela Brapi.", cleanTicker);
                    return buildQuote(asset, false);
                }
            }

            log.warn("Brapi não retornou cotação válida para {}.", cleanTicker);

        } catch (RestClientException e) {
            log.warn(
                    "Falha ao consultar Brapi para {}: {}",
                    cleanTicker, e.getMessage()
            );
        }

        return getFallbackQuote(cleanTicker);
    }

    private MarketQuoteDTO getFallbackQuote(String ticker) {
        Optional<Asset> existingOpt =
                assetRepository.findByTickerIgnoreCase(ticker);

        if (existingOpt.isPresent()) {
            Asset asset = existingOpt.get();

            if (asset.getCurrentPrice() != null
                    && asset.getCurrentPrice().compareTo(BigDecimal.ZERO) > 0) {

                log.warn(
                        "Utilizando última cotação armazenada para {}.",
                        ticker
                );

                return buildQuote(asset, true);
            }
        }

        throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Não foi possível obter a cotação de " + ticker
                        + ". Nenhuma cotação válida está armazenada."
        );
    }

    private MarketQuoteDTO buildQuote(Asset asset, boolean fromFallback) {
        return MarketQuoteDTO.builder()
                .ticker(asset.getTicker())
                .name(asset.getName())
                .price(asset.getCurrentPrice())
                .changePercentage(
                        asset.getDailyChangePercentage() != null
                                ? asset.getDailyChangePercentage()
                                : BigDecimal.ZERO
                )
                .updatedAt(asset.getLastPriceUpdate())
                .fromFallback(fromFallback)
                .build();
    }

    private Asset updateOrCreateAsset(
            String ticker,
            String name,
            BigDecimal price,
            BigDecimal change
    ) {
        Asset asset = assetRepository.findByTickerIgnoreCase(ticker)
                .orElseGet(() -> Asset.builder()
                        .ticker(ticker)
                        .name(name)
                        .type(inferAssetType(ticker))
                        .build());

        asset.setCurrentPrice(price.setScale(4, RoundingMode.HALF_UP));
        asset.setDailyChangePercentage(
                change.setScale(4, RoundingMode.HALF_UP)
        );

        if (asset.getName() == null || asset.getName().isBlank()) {
            asset.setName(name);
        }

        asset.setLastPriceUpdate(LocalDateTime.now());

        return assetRepository.save(asset);
    }

    public static AssetType inferAssetType(String ticker) {
        if (ticker.endsWith("11")) {
            if (ticker.startsWith("IVVB")
                    || ticker.startsWith("BOVA")
                    || ticker.startsWith("SMAL")
                    || ticker.startsWith("HASH")) {
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
