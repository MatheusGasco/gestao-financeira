export type Role = 'ROLE_USER' | 'ROLE_ADMIN';

export interface User {
  id: string;
  name: string;
  email: string;
  role: Role;
}

export interface AuthResponse {
  token: string;
  tokenType: string;
  user: User;
}

export type AccountType = 'CHECKING' | 'SAVINGS' | 'INVESTMENT' | 'CASH';

export interface Account {
  id: string;
  name: string;
  type: AccountType;
  balance: number;
  currency: string;
  color: string;
  createdAt: string;
}

export type CategoryType = 'INCOME' | 'EXPENSE';

export interface Category {
  id: string;
  name: string;
  type: CategoryType;
  icon: string;
  color: string;
  isDefault: boolean;
}

export type TransactionType = 'INCOME' | 'EXPENSE' | 'TRANSFER';

export interface Transaction {
  id: string;
  account: Account;
  category?: Category;
  destinationAccount?: Account;
  type: TransactionType;
  amount: number;
  transactionDate: string;
  description: string;
  isPaid: boolean;
  createdAt: string;
}

export type AssetType = 'STOCK' | 'FII' | 'ETF' | 'BDR';

export type InvestmentTransactionType = 'BUY' | 'SELL';

export interface InvestmentOrder {
  id: string;
  ticker: string;
  assetName: string;
  assetType: AssetType;
  type: InvestmentTransactionType;
  quantity: number;
  unitPrice: number;
  fees: number;
  totalAmount: number;
  transactionDate: string;
  createdAt: string;
}

export interface InvestmentPosition {
  assetId: string;
  ticker: string;
  name: string;
  type: AssetType;
  quantity: number;
  averagePrice: number;
  totalCost: number;
  currentPrice: number;
  currentValue: number;
  profitLoss: number;
  profitLossPercentage: number;
  dailyChangePercentage: number;
}

export interface MarketQuote {
  ticker: string;
  name: string;
  price: number;
  changePercentage: number;
  updatedAt: string;
  fromFallback: boolean;
}

export interface DashboardSummary {
  totalNetWorth: number;
  availableBalance: number;
  investedBalance: number;
  totalInvestedCost: number;
  investmentsProfitLoss: number;
  investmentsProfitLossPercentage: number;
  monthlyIncome: number;
  monthlyExpense: number;
  monthlyResult: number;
  accounts: Account[];
  positions: InvestmentPosition[];
  recentTransactions: Transaction[];
}

export interface MonthlyPoint {
  month: string;
  income: number;
  expense: number;
  result: number;
}
