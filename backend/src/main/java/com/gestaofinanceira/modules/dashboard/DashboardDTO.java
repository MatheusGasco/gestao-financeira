package com.gestaofinanceira.modules.dashboard;

import com.gestaofinanceira.modules.account.AccountDTO;
import com.gestaofinanceira.modules.investment.InvestmentDTO;
import com.gestaofinanceira.modules.transaction.TransactionDTO;

import java.math.BigDecimal;
import java.util.List;

public class DashboardDTO {

    public static class Summary {
        private BigDecimal totalNetWorth;
        private BigDecimal availableBalance;
        private BigDecimal investedBalance;
        private BigDecimal totalInvestedCost;
        private BigDecimal investmentsProfitLoss;
        private BigDecimal investmentsProfitLossPercentage;

        private BigDecimal monthlyIncome;
        private BigDecimal monthlyExpense;
        private BigDecimal monthlyResult;

        private List<AccountDTO.Response> accounts;
        private List<InvestmentDTO.PositionResponse> positions;
        private List<TransactionDTO.Response> recentTransactions;

        public Summary() {}

        public Summary(BigDecimal totalNetWorth, BigDecimal availableBalance, BigDecimal investedBalance, BigDecimal totalInvestedCost, BigDecimal investmentsProfitLoss, BigDecimal investmentsProfitLossPercentage, BigDecimal monthlyIncome, BigDecimal monthlyExpense, BigDecimal monthlyResult, List<AccountDTO.Response> accounts, List<InvestmentDTO.PositionResponse> positions, List<TransactionDTO.Response> recentTransactions) {
            this.totalNetWorth = totalNetWorth;
            this.availableBalance = availableBalance;
            this.investedBalance = investedBalance;
            this.totalInvestedCost = totalInvestedCost;
            this.investmentsProfitLoss = investmentsProfitLoss;
            this.investmentsProfitLossPercentage = investmentsProfitLossPercentage;
            this.monthlyIncome = monthlyIncome;
            this.monthlyExpense = monthlyExpense;
            this.monthlyResult = monthlyResult;
            this.accounts = accounts;
            this.positions = positions;
            this.recentTransactions = recentTransactions;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private BigDecimal totalNetWorth;
            private BigDecimal availableBalance;
            private BigDecimal investedBalance;
            private BigDecimal totalInvestedCost;
            private BigDecimal investmentsProfitLoss;
            private BigDecimal investmentsProfitLossPercentage;
            private BigDecimal monthlyIncome;
            private BigDecimal monthlyExpense;
            private BigDecimal monthlyResult;
            private List<AccountDTO.Response> accounts;
            private List<InvestmentDTO.PositionResponse> positions;
            private List<TransactionDTO.Response> recentTransactions;

            public Builder totalNetWorth(BigDecimal totalNetWorth) { this.totalNetWorth = totalNetWorth; return this; }
            public Builder availableBalance(BigDecimal availableBalance) { this.availableBalance = availableBalance; return this; }
            public Builder investedBalance(BigDecimal investedBalance) { this.investedBalance = investedBalance; return this; }
            public Builder totalInvestedCost(BigDecimal totalInvestedCost) { this.totalInvestedCost = totalInvestedCost; return this; }
            public Builder investmentsProfitLoss(BigDecimal investmentsProfitLoss) { this.investmentsProfitLoss = investmentsProfitLoss; return this; }
            public Builder investmentsProfitLossPercentage(BigDecimal investmentsProfitLossPercentage) { this.investmentsProfitLossPercentage = investmentsProfitLossPercentage; return this; }
            public Builder monthlyIncome(BigDecimal monthlyIncome) { this.monthlyIncome = monthlyIncome; return this; }
            public Builder monthlyExpense(BigDecimal monthlyExpense) { this.monthlyExpense = monthlyExpense; return this; }
            public Builder monthlyResult(BigDecimal monthlyResult) { this.monthlyResult = monthlyResult; return this; }
            public Builder accounts(List<AccountDTO.Response> accounts) { this.accounts = accounts; return this; }
            public Builder positions(List<InvestmentDTO.PositionResponse> positions) { this.positions = positions; return this; }
            public Builder recentTransactions(List<TransactionDTO.Response> recentTransactions) { this.recentTransactions = recentTransactions; return this; }

            public Summary build() {
                return new Summary(totalNetWorth, availableBalance, investedBalance, totalInvestedCost, investmentsProfitLoss, investmentsProfitLossPercentage, monthlyIncome, monthlyExpense, monthlyResult, accounts, positions, recentTransactions);
            }
        }

        public BigDecimal getTotalNetWorth() { return totalNetWorth; }
        public void setTotalNetWorth(BigDecimal totalNetWorth) { this.totalNetWorth = totalNetWorth; }

        public BigDecimal getAvailableBalance() { return availableBalance; }
        public void setAvailableBalance(BigDecimal availableBalance) { this.availableBalance = availableBalance; }

        public BigDecimal getInvestedBalance() { return investedBalance; }
        public void setInvestedBalance(BigDecimal investedBalance) { this.investedBalance = investedBalance; }

        public BigDecimal getTotalInvestedCost() { return totalInvestedCost; }
        public void setTotalInvestedCost(BigDecimal totalInvestedCost) { this.totalInvestedCost = totalInvestedCost; }

        public BigDecimal getInvestmentsProfitLoss() { return investmentsProfitLoss; }
        public void setInvestmentsProfitLoss(BigDecimal investmentsProfitLoss) { this.investmentsProfitLoss = investmentsProfitLoss; }

        public BigDecimal getInvestmentsProfitLossPercentage() { return investmentsProfitLossPercentage; }
        public void setInvestmentsProfitLossPercentage(BigDecimal investmentsProfitLossPercentage) { this.investmentsProfitLossPercentage = investmentsProfitLossPercentage; }

        public BigDecimal getMonthlyIncome() { return monthlyIncome; }
        public void setMonthlyIncome(BigDecimal monthlyIncome) { this.monthlyIncome = monthlyIncome; }

        public BigDecimal getMonthlyExpense() { return monthlyExpense; }
        public void setMonthlyExpense(BigDecimal monthlyExpense) { this.monthlyExpense = monthlyExpense; }

        public BigDecimal getMonthlyResult() { return monthlyResult; }
        public void setMonthlyResult(BigDecimal monthlyResult) { this.monthlyResult = monthlyResult; }

        public List<AccountDTO.Response> getAccounts() { return accounts; }
        public void setAccounts(List<AccountDTO.Response> accounts) { this.accounts = accounts; }

        public List<InvestmentDTO.PositionResponse> getPositions() { return positions; }
        public void setPositions(List<InvestmentDTO.PositionResponse> positions) { this.positions = positions; }

        public List<TransactionDTO.Response> getRecentTransactions() { return recentTransactions; }
        public void setRecentTransactions(List<TransactionDTO.Response> recentTransactions) { this.recentTransactions = recentTransactions; }
    }

    public static class MonthlyPoint {
        private String month;
        private BigDecimal income;
        private BigDecimal expense;
        private BigDecimal result;

        public MonthlyPoint() {}

        public MonthlyPoint(String month, BigDecimal income, BigDecimal expense, BigDecimal result) {
            this.month = month;
            this.income = income;
            this.expense = expense;
            this.result = result;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String month;
            private BigDecimal income;
            private BigDecimal expense;
            private BigDecimal result;

            public Builder month(String month) { this.month = month; return this; }
            public Builder income(BigDecimal income) { this.income = income; return this; }
            public Builder expense(BigDecimal expense) { this.expense = expense; return this; }
            public Builder result(BigDecimal result) { this.result = result; return this; }

            public MonthlyPoint build() {
                return new MonthlyPoint(month, income, expense, result);
            }
        }

        public String getMonth() { return month; }
        public void setMonth(String month) { this.month = month; }

        public BigDecimal getIncome() { return income; }
        public void setIncome(BigDecimal income) { this.income = income; }

        public BigDecimal getExpense() { return expense; }
        public void setExpense(BigDecimal expense) { this.expense = expense; }

        public BigDecimal getResult() { return result; }
        public void setResult(BigDecimal result) { this.result = result; }
    }
}
