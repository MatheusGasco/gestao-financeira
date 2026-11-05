package com.gestaofinanceira.modules.investment;

import com.gestaofinanceira.modules.account.Account;
import com.gestaofinanceira.modules.account.AccountRepository;
import com.gestaofinanceira.modules.account.AccountService;
import com.gestaofinanceira.modules.asset.Asset;
import com.gestaofinanceira.modules.asset.AssetRepository;
import com.gestaofinanceira.modules.market.MarketDataService;
import com.gestaofinanceira.modules.market.MarketQuoteDTO;
import com.gestaofinanceira.modules.user.User;
import com.gestaofinanceira.shared.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class InvestmentService {

    private final InvestmentTransactionRepository investmentRepository;
    private final AssetRepository assetRepository;
    private final MarketDataService marketDataService;
    private final AccountService accountService;
    private final AccountRepository accountRepository;

    public InvestmentService(
            InvestmentTransactionRepository investmentRepository,
            AssetRepository assetRepository,
            MarketDataService marketDataService,
            AccountService accountService,
            AccountRepository accountRepository
    ) {
        this.investmentRepository = investmentRepository;
        this.assetRepository = assetRepository;
        this.marketDataService = marketDataService;
        this.accountService = accountService;
        this.accountRepository = accountRepository;
    }

    @Transactional
    public InvestmentDTO.OrderResponse recordOrder(InvestmentDTO.OrderRequest request, User user) {
        String ticker = request.getTicker().trim().toUpperCase();

        Account account = accountService.findEntityByIdAndUser(request.getAccountId(), user);

        // Buscar cotação e garantir existência do ativo
        MarketQuoteDTO quote = marketDataService.getQuote(ticker);
        Asset asset = assetRepository.findByTickerIgnoreCase(ticker)
                .orElseThrow(() -> new BusinessException("Ativo não encontrado: " + ticker));

        BigDecimal fees = request.getFees() != null ? request.getFees() : BigDecimal.ZERO;
        BigDecimal grossAmount = request.getQuantity().multiply(request.getUnitPrice());
        BigDecimal totalAmount;

        if (request.getType() == InvestmentTransactionType.BUY) {
            totalAmount = grossAmount.add(fees);
            // Sai o valor da conta escolhida para investir
            account.setBalance(account.getBalance().subtract(totalAmount));
            accountRepository.save(account);
        } else {
            // Validar se tem custódia suficiente para venda
            BigDecimal currentCustody = getCurrentCustodyQuantity(user, asset);
            if (request.getQuantity().compareTo(currentCustody) > 0) {
                throw new BusinessException(String.format(
                        "Quantidade insuficiente em custódia para venda de %s. Custódia atual: %s cotas.",
                        ticker, currentCustody.toPlainString()
                ));
            }
            totalAmount = grossAmount.subtract(fees);
            // Entra o valor resgatado na conta escolhida
            account.setBalance(account.getBalance().add(totalAmount));
            accountRepository.save(account);
        }

        InvestmentTransaction transaction = InvestmentTransaction.builder()
                .user(user)
                .account(account)
                .asset(asset)
                .type(request.getType())
                .quantity(request.getQuantity())
                .unitPrice(request.getUnitPrice())
                .fees(fees)
                .totalAmount(totalAmount.setScale(2, RoundingMode.HALF_UP))
                .transactionDate(request.getTransactionDate())
                .build();

        InvestmentTransaction saved = investmentRepository.save(transaction);
        return InvestmentDTO.OrderResponse.fromEntity(saved);
    }

    public List<InvestmentDTO.OrderResponse> listOrders(User user) {
        return investmentRepository.findByUserOrderByTransactionDateDescCreatedAtDesc(user)
                .stream()
                .map(InvestmentDTO.OrderResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<InvestmentDTO.PositionResponse> getPortfolioPositions(User user) {
        List<InvestmentTransaction> transactions = investmentRepository.findByUserOrderByTransactionDateAscCreatedAtAsc(user);

        // Agrupar por ativo mantendo ordem cronológica
        Map<Asset, List<InvestmentTransaction>> groupedByAsset = new LinkedHashMap<>();
        for (InvestmentTransaction t : transactions) {
            groupedByAsset.computeIfAbsent(t.getAsset(), k -> new ArrayList<>()).add(t);
        }

        List<InvestmentDTO.PositionResponse> positions = new ArrayList<>();

        for (Map.Entry<Asset, List<InvestmentTransaction>> entry : groupedByAsset.entrySet()) {
            Asset asset = entry.getKey();
            List<InvestmentTransaction> assetTransactions = entry.getValue();

            BigDecimal currentQty = BigDecimal.ZERO;
            BigDecimal totalCost = BigDecimal.ZERO;

            for (InvestmentTransaction t : assetTransactions) {
                if (t.getType() == InvestmentTransactionType.BUY) {
                    BigDecimal buyCost = t.getQuantity().multiply(t.getUnitPrice()).add(t.getFees());
                    totalCost = totalCost.add(buyCost);
                    currentQty = currentQty.add(t.getQuantity());
                } else if (t.getType() == InvestmentTransactionType.SELL) {
                    BigDecimal avgPrice = currentQty.compareTo(BigDecimal.ZERO) > 0
                            ? totalCost.divide(currentQty, 6, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;
                    currentQty = currentQty.subtract(t.getQuantity());

                    if (currentQty.compareTo(BigDecimal.ZERO) <= 0) {
                        currentQty = BigDecimal.ZERO;
                        totalCost = BigDecimal.ZERO;
                    } else {
                        totalCost = avgPrice.multiply(currentQty);
                    }
                }
            }

            if (currentQty.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal averagePrice = totalCost.divide(currentQty, 4, RoundingMode.HALF_UP);
                BigDecimal currentPrice = asset.getCurrentPrice() != null ? asset.getCurrentPrice() : averagePrice;
                BigDecimal currentValue = currentQty.multiply(currentPrice).setScale(2, RoundingMode.HALF_UP);
                BigDecimal profitLoss = currentValue.subtract(totalCost).setScale(2, RoundingMode.HALF_UP);
                BigDecimal profitLossPercentage = totalCost.compareTo(BigDecimal.ZERO) > 0
                        ? profitLoss.divide(totalCost, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO;

                positions.add(InvestmentDTO.PositionResponse.builder()
                        .assetId(asset.getId())
                        .ticker(asset.getTicker())
                        .name(asset.getName())
                        .type(asset.getType())
                        .quantity(currentQty.setScale(4, RoundingMode.HALF_UP))
                        .averagePrice(averagePrice.setScale(2, RoundingMode.HALF_UP))
                        .totalCost(totalCost.setScale(2, RoundingMode.HALF_UP))
                        .currentPrice(currentPrice.setScale(2, RoundingMode.HALF_UP))
                        .currentValue(currentValue)
                        .profitLoss(profitLoss)
                        .profitLossPercentage(profitLossPercentage)
                        .dailyChangePercentage(asset.getDailyChangePercentage() != null ? asset.getDailyChangePercentage() : BigDecimal.ZERO)
                        .build());
            }
        }

        return positions;
    }

    private BigDecimal getCurrentCustodyQuantity(User user, Asset asset) {
        List<InvestmentTransaction> txs = investmentRepository.findByUserAndAssetOrderByTransactionDateAscCreatedAtAsc(user, asset);
        BigDecimal qty = BigDecimal.ZERO;
        for (InvestmentTransaction t : txs) {
            if (t.getType() == InvestmentTransactionType.BUY) {
                qty = qty.add(t.getQuantity());
            } else {
                qty = qty.subtract(t.getQuantity());
            }
        }
        return qty;
    }
}
