package com.gestaofinanceira.modules.dashboard;

import com.gestaofinanceira.modules.account.AccountDTO;
import com.gestaofinanceira.modules.account.AccountService;
import com.gestaofinanceira.modules.investment.InvestmentDTO;
import com.gestaofinanceira.modules.investment.InvestmentService;
import com.gestaofinanceira.modules.transaction.Transaction;
import com.gestaofinanceira.modules.transaction.TransactionDTO;
import com.gestaofinanceira.modules.transaction.TransactionRepository;
import com.gestaofinanceira.modules.transaction.TransactionType;
import com.gestaofinanceira.modules.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final AccountService accountService;
    private final InvestmentService investmentService;
    private final TransactionRepository transactionRepository;

    public DashboardService(
            AccountService accountService,
            InvestmentService investmentService,
            TransactionRepository transactionRepository
    ) {
        this.accountService = accountService;
        this.investmentService = investmentService;
        this.transactionRepository = transactionRepository;
    }

    public DashboardDTO.Summary getSummary(User user) {
        List<AccountDTO.Response> accounts = accountService.listByUser(user);
        BigDecimal availableBalance = accounts.stream()
                .map(AccountDTO.Response::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<InvestmentDTO.PositionResponse> positions = investmentService.getPortfolioPositions(user);
        BigDecimal investedBalance = positions.stream()
                .map(InvestmentDTO.PositionResponse::getCurrentValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalInvestedCost = positions.stream()
                .map(InvestmentDTO.PositionResponse::getTotalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal profitLoss = investedBalance.subtract(totalInvestedCost);
        BigDecimal profitLossPercentage = totalInvestedCost.compareTo(BigDecimal.ZERO) > 0
                ? profitLoss.divide(totalInvestedCost, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal totalNetWorth = availableBalance.add(investedBalance);

        // Mês atual
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());

        List<Transaction> currentMonthTxs = transactionRepository.findByUserAndTransactionDateBetween(user, startOfMonth, endOfMonth);

        BigDecimal monthlyIncome = currentMonthTxs.stream()
                .filter(t -> t.getType() == TransactionType.INCOME && t.isPaid())
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal monthlyExpense = currentMonthTxs.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE && t.isPaid())
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal monthlyResult = monthlyIncome.subtract(monthlyExpense);

        List<TransactionDTO.Response> recentTxs = transactionRepository.findTop5ByUserOrderByTransactionDateDescCreatedAtDesc(user)
                .stream()
                .map(TransactionDTO.Response::fromEntity)
                .collect(Collectors.toList());

        return DashboardDTO.Summary.builder()
                .totalNetWorth(totalNetWorth.setScale(2, RoundingMode.HALF_UP))
                .availableBalance(availableBalance.setScale(2, RoundingMode.HALF_UP))
                .investedBalance(investedBalance.setScale(2, RoundingMode.HALF_UP))
                .totalInvestedCost(totalInvestedCost.setScale(2, RoundingMode.HALF_UP))
                .investmentsProfitLoss(profitLoss.setScale(2, RoundingMode.HALF_UP))
                .investmentsProfitLossPercentage(profitLossPercentage)
                .monthlyIncome(monthlyIncome.setScale(2, RoundingMode.HALF_UP))
                .monthlyExpense(monthlyExpense.setScale(2, RoundingMode.HALF_UP))
                .monthlyResult(monthlyResult.setScale(2, RoundingMode.HALF_UP))
                .accounts(accounts)
                .positions(positions)
                .recentTransactions(recentTxs)
                .build();
    }

    public List<DashboardDTO.MonthlyPoint> getMonthlyEvolution(User user) {
        List<DashboardDTO.MonthlyPoint> points = new ArrayList<>();
        YearMonth currentYearMonth = YearMonth.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM/yy", new Locale("pt", "BR"));

        // Últimos 6 meses
        for (int i = 5; i >= 0; i--) {
            YearMonth ym = currentYearMonth.minusMonths(i);
            LocalDate start = ym.atDay(1);
            LocalDate end = ym.atEndOfMonth();

            List<Transaction> txs = transactionRepository.findByUserAndTransactionDateBetween(user, start, end);

            BigDecimal income = txs.stream()
                    .filter(t -> t.getType() == TransactionType.INCOME && t.isPaid())
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal expense = txs.stream()
                    .filter(t -> t.getType() == TransactionType.EXPENSE && t.isPaid())
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal result = income.subtract(expense);

            points.add(DashboardDTO.MonthlyPoint.builder()
                    .month(ym.format(formatter))
                    .income(income.setScale(2, RoundingMode.HALF_UP))
                    .expense(expense.setScale(2, RoundingMode.HALF_UP))
                    .result(result.setScale(2, RoundingMode.HALF_UP))
                    .build());
        }

        return points;
    }
}
