package com.gestaofinanceira.modules.investment;

import com.gestaofinanceira.modules.asset.Asset;
import com.gestaofinanceira.modules.asset.AssetRepository;
import com.gestaofinanceira.modules.asset.AssetType;
import com.gestaofinanceira.modules.market.MarketDataService;
import com.gestaofinanceira.modules.user.User;
import com.gestaofinanceira.shared.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvestmentServiceTest {

    @Mock
    private InvestmentTransactionRepository investmentRepository;

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private MarketDataService marketDataService;

    @Mock
    private com.gestaofinanceira.modules.account.AccountService accountService;

    @Mock
    private com.gestaofinanceira.modules.account.AccountRepository accountRepository;

    @InjectMocks
    private InvestmentService investmentService;

    private User mockUser;
    private Asset petr4;
    private com.gestaofinanceira.modules.account.Account mockAccount;

    @BeforeEach
    void setUp() {
        mockUser = User.builder().id(UUID.randomUUID()).name("Matheus").email("matheus@teste.com").build();
        mockAccount = com.gestaofinanceira.modules.account.Account.builder()
                .id(UUID.randomUUID())
                .user(mockUser)
                .name("Nubank")
                .balance(new BigDecimal("5000.00"))
                .build();
        petr4 = Asset.builder()
                .id(UUID.randomUUID())
                .ticker("PETR4")
                .name("Petrobras PN")
                .type(AssetType.STOCK)
                .currentPrice(new BigDecimal("38.00"))
                .dailyChangePercentage(new BigDecimal("1.50"))
                .build();
    }

    @Test
    @DisplayName("Deve calcular Preço Médio ponderado e rentabilidade corretamente para compras consecutivas")
    void testCalculateAveragePriceAndYield() {
        // Compra 1: 100 cotas a R$ 35,00 (Total = 3.500)
        InvestmentTransaction buy1 = InvestmentTransaction.builder()
                .id(UUID.randomUUID())
                .user(mockUser)
                .asset(petr4)
                .type(InvestmentTransactionType.BUY)
                .quantity(new BigDecimal("100"))
                .unitPrice(new BigDecimal("35.00"))
                .fees(BigDecimal.ZERO)
                .totalAmount(new BigDecimal("3500.00"))
                .transactionDate(LocalDate.now().minusDays(10))
                .build();

        // Compra 2: 100 cotas a R$ 40,00 (Total = 4.000)
        // Custo total = 7.500 para 200 cotas -> Preço Médio esperado = R$ 37,50
        InvestmentTransaction buy2 = InvestmentTransaction.builder()
                .id(UUID.randomUUID())
                .user(mockUser)
                .asset(petr4)
                .type(InvestmentTransactionType.BUY)
                .quantity(new BigDecimal("100"))
                .unitPrice(new BigDecimal("40.00"))
                .fees(BigDecimal.ZERO)
                .totalAmount(new BigDecimal("4000.00"))
                .transactionDate(LocalDate.now().minusDays(5))
                .build();

        when(investmentRepository.findByUserOrderByTransactionDateAscCreatedAtAsc(mockUser))
                .thenReturn(List.of(buy1, buy2));

        List<InvestmentDTO.PositionResponse> positions = investmentService.getPortfolioPositions(mockUser);

        assertEquals(1, positions.size());
        InvestmentDTO.PositionResponse pos = positions.get(0);

        assertEquals("PETR4", pos.getTicker());
        assertEquals(new BigDecimal("200.0000"), pos.getQuantity());
        assertEquals(new BigDecimal("37.50"), pos.getAveragePrice());
        assertEquals(new BigDecimal("7500.00"), pos.getTotalCost());
        // Cotação atual = 38.00 -> Valor atual = 200 * 38 = 7.600,00
        assertEquals(new BigDecimal("7600.00"), pos.getCurrentValue());
        // Lucro = 7.600 - 7.500 = +100,00
        assertEquals(new BigDecimal("100.00"), pos.getProfitLoss());
        // Rentabilidade = (100 / 7500) * 100 = 1.33%
        assertEquals(new BigDecimal("1.33"), pos.getProfitLossPercentage());
    }

    @Test
    @DisplayName("Venda parcial não altera o preço médio das cotas remanescentes")
    void testPartialSaleKeepsAveragePrice() {
        // Compra 1: 100 cotas a R$ 30,00
        InvestmentTransaction buy = InvestmentTransaction.builder()
                .id(UUID.randomUUID())
                .user(mockUser)
                .asset(petr4)
                .type(InvestmentTransactionType.BUY)
                .quantity(new BigDecimal("100"))
                .unitPrice(new BigDecimal("30.00"))
                .fees(BigDecimal.ZERO)
                .totalAmount(new BigDecimal("3000.00"))
                .transactionDate(LocalDate.now().minusDays(10))
                .build();

        // Venda: 40 cotas a R$ 36,00 (Sobram 60 cotas a PM R$ 30,00)
        InvestmentTransaction sell = InvestmentTransaction.builder()
                .id(UUID.randomUUID())
                .user(mockUser)
                .asset(petr4)
                .type(InvestmentTransactionType.SELL)
                .quantity(new BigDecimal("40"))
                .unitPrice(new BigDecimal("36.00"))
                .fees(BigDecimal.ZERO)
                .totalAmount(new BigDecimal("1440.00"))
                .transactionDate(LocalDate.now().minusDays(2))
                .build();

        when(investmentRepository.findByUserOrderByTransactionDateAscCreatedAtAsc(mockUser))
                .thenReturn(List.of(buy, sell));

        List<InvestmentDTO.PositionResponse> positions = investmentService.getPortfolioPositions(mockUser);

        assertEquals(1, positions.size());
        InvestmentDTO.PositionResponse pos = positions.get(0);

        assertEquals(new BigDecimal("60.0000"), pos.getQuantity());
        assertEquals(new BigDecimal("30.00"), pos.getAveragePrice());
        assertEquals(new BigDecimal("1800.00"), pos.getTotalCost());
    }

    @Test
    @DisplayName("Ao comprar ativo (BUY), debita o valor total do investimento da conta escolhida")
    void testRecordOrderBuyDebitsAccountBalance() {
        InvestmentDTO.OrderRequest request = new InvestmentDTO.OrderRequest(
                "PETR4",
                InvestmentTransactionType.BUY,
                new BigDecimal("100"),
                new BigDecimal("35.00"),
                new BigDecimal("10.00"),
                LocalDate.now(),
                mockAccount.getId()
        );

        when(accountService.findEntityByIdAndUser(mockAccount.getId(), mockUser)).thenReturn(mockAccount);
        when(marketDataService.getQuote("PETR4")).thenReturn(null);
        when(assetRepository.findByTickerIgnoreCase("PETR4")).thenReturn(Optional.of(petr4));
        when(investmentRepository.save(any(InvestmentTransaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InvestmentDTO.OrderResponse response = investmentService.recordOrder(request, mockUser);

        assertNotNull(response);
        // Saldo inicial era 5000.00. Custo total = (100 * 35.00) + 10.00 = 3510.00
        // Saldo final esperado = 5000.00 - 3510.00 = 1490.00
        assertEquals(new BigDecimal("1490.00"), mockAccount.getBalance());
        assertEquals(new BigDecimal("3510.00"), response.getTotalAmount());
        assertEquals(mockAccount.getName(), response.getAccountName());
        verify(accountRepository).save(mockAccount);
        verify(investmentRepository).save(any(InvestmentTransaction.class));
    }

    @Test
    @DisplayName("Ao vender/resgatar ativo (SELL), credita o valor líquido resgatado na conta escolhida")
    void testRecordOrderSellCreditsAccountBalance() {
        InvestmentDTO.OrderRequest request = new InvestmentDTO.OrderRequest(
                "PETR4",
                InvestmentTransactionType.SELL,
                new BigDecimal("50"),
                new BigDecimal("40.00"),
                new BigDecimal("5.00"),
                LocalDate.now(),
                mockAccount.getId()
        );

        InvestmentTransaction previousBuy = InvestmentTransaction.builder()
                .id(UUID.randomUUID())
                .user(mockUser)
                .account(mockAccount)
                .asset(petr4)
                .type(InvestmentTransactionType.BUY)
                .quantity(new BigDecimal("100"))
                .unitPrice(new BigDecimal("35.00"))
                .fees(BigDecimal.ZERO)
                .totalAmount(new BigDecimal("3500.00"))
                .transactionDate(LocalDate.now().minusDays(5))
                .build();

        when(accountService.findEntityByIdAndUser(mockAccount.getId(), mockUser)).thenReturn(mockAccount);
        when(marketDataService.getQuote("PETR4")).thenReturn(null);
        when(assetRepository.findByTickerIgnoreCase("PETR4")).thenReturn(Optional.of(petr4));
        when(investmentRepository.findByUserAndAssetOrderByTransactionDateAscCreatedAtAsc(mockUser, petr4))
                .thenReturn(List.of(previousBuy));
        when(investmentRepository.save(any(InvestmentTransaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InvestmentDTO.OrderResponse response = investmentService.recordOrder(request, mockUser);

        assertNotNull(response);
        // Saldo inicial era 5000.00. Valor líquido da venda = (50 * 40.00) - 5.00 = 1995.00
        // Saldo final esperado = 5000.00 + 1995.00 = 6995.00
        assertEquals(new BigDecimal("6995.00"), mockAccount.getBalance());
        assertEquals(new BigDecimal("1995.00"), response.getTotalAmount());
        assertEquals(mockAccount.getName(), response.getAccountName());
        verify(accountRepository).save(mockAccount);
        verify(investmentRepository).save(any(InvestmentTransaction.class));
    }

    @Test
    @DisplayName("Ao tentar vender quantidade superior à custódia, deve lançar BusinessException")
    void testRecordOrderSellThrowsWhenCustodyInsufficient() {
        InvestmentDTO.OrderRequest request = new InvestmentDTO.OrderRequest(
                "PETR4",
                InvestmentTransactionType.SELL,
                new BigDecimal("50"),
                new BigDecimal("40.00"),
                BigDecimal.ZERO,
                LocalDate.now(),
                mockAccount.getId()
        );

        when(accountService.findEntityByIdAndUser(mockAccount.getId(), mockUser)).thenReturn(mockAccount);
        when(marketDataService.getQuote("PETR4")).thenReturn(null);
        when(assetRepository.findByTickerIgnoreCase("PETR4")).thenReturn(Optional.of(petr4));
        when(investmentRepository.findByUserAndAssetOrderByTransactionDateAscCreatedAtAsc(mockUser, petr4))
                .thenReturn(List.of()); // Custódia zero

        BusinessException ex = assertThrows(BusinessException.class, () ->
                investmentService.recordOrder(request, mockUser)
        );

        assertTrue(ex.getMessage().contains("Quantidade insuficiente em custódia"));
    }
}
