import axios from 'axios';
import type {
  AuthResponse,
  User,
  Account,
  AccountType,
  Category,
  CategoryType,
  Transaction,
  TransactionType,
  InvestmentOrder,
  InvestmentPosition,
  InvestmentTransactionType,
  MarketQuote,
  DashboardSummary,
  MonthlyPoint
} from '../types';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

export const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.dispatchEvent(new Event('auth:unauthorized'));
    }
    return Promise.reject(error);
  }
);

// Auth Endpoints
export const authApi = {
  login: async (email: string, password: string): Promise<AuthResponse> => {
    const res = await api.post<AuthResponse>('/auth/login', { email, password });
    return res.data;
  },
  register: async (name: string, email: string, password: string): Promise<AuthResponse> => {
    const res = await api.post<AuthResponse>('/auth/register', { name, email, password });
    return res.data;
  },
  getMe: async (): Promise<User> => {
    const res = await api.get<User>('/auth/me');
    return res.data;
  },
};

// Accounts Endpoints
export const accountsApi = {
  list: async (): Promise<Account[]> => {
    const res = await api.get<Account[]>('/accounts');
    return res.data;
  },
  create: async (data: { name: string; type: AccountType; initialBalance?: number; color?: string }): Promise<Account> => {
    const res = await api.post<Account>('/accounts', data);
    return res.data;
  },
  delete: async (id: string): Promise<void> => {
    await api.delete(`/accounts/${id}`);
  },
};

// Categories Endpoints
export const categoriesApi = {
  list: async (): Promise<Category[]> => {
    const res = await api.get<Category[]>('/categories');
    return res.data;
  },
  create: async (data: { name: string; type: CategoryType; icon?: string; color?: string }): Promise<Category> => {
    const res = await api.post<Category>('/categories', data);
    return res.data;
  },
};

// Transactions Endpoints
export const transactionsApi = {
  list: async (filters?: { startDate?: string; endDate?: string; type?: TransactionType; accountId?: string }): Promise<Transaction[]> => {
    const res = await api.get<Transaction[]>('/transactions', { params: filters });
    return res.data;
  },
  create: async (data: {
    accountId: string;
    categoryId?: string;
    destinationAccountId?: string;
    type: TransactionType;
    amount: number;
    transactionDate: string;
    description: string;
    isPaid?: boolean;
  }): Promise<Transaction> => {
    const res = await api.post<Transaction>('/transactions', data);
    return res.data;
  },
  delete: async (id: string): Promise<void> => {
    await api.delete(`/transactions/${id}`);
  },
};

// Investments Endpoints
export const investmentsApi = {
  getPositions: async (): Promise<InvestmentPosition[]> => {
    const res = await api.get<InvestmentPosition[]>('/investments/positions');
    return res.data;
  },
  listOrders: async (): Promise<InvestmentOrder[]> => {
    const res = await api.get<InvestmentOrder[]>('/investments/transactions');
    return res.data;
  },
  recordOrder: async (data: {
    ticker: string;
    type: InvestmentTransactionType;
    quantity: number;
    unitPrice: number;
    fees?: number;
    transactionDate: string;
    accountId: string;
  }): Promise<InvestmentOrder> => {
    const res = await api.post<InvestmentOrder>('/investments/transactions', data);
    return res.data;
  },
};

// Market Endpoints
export const marketApi = {
  getQuote: async (ticker: string): Promise<MarketQuote> => {
    const res = await api.get<MarketQuote>(`/market/quote/${ticker}`);
    return res.data;
  },
};

// Dashboard Endpoints
export const dashboardApi = {
  getSummary: async (): Promise<DashboardSummary> => {
    const res = await api.get<DashboardSummary>('/dashboard/summary');
    return res.data;
  },
  getEvolution: async (): Promise<MonthlyPoint[]> => {
    const res = await api.get<MonthlyPoint[]>('/dashboard/evolution');
    return res.data;
  },
};
