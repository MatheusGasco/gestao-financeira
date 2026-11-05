import React, { useEffect, useState } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { dashboardApi, transactionsApi } from '../services/api';
import type { DashboardSummary, Transaction } from '../types';
import {
  Wallet,
  TrendingUp,
  ArrowUpRight,
  ArrowDownRight,
  PieChart as PieIcon,
  DollarSign,
  Activity,
  Calendar,
  AlertCircle
} from 'lucide-react';
import {
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
  Tooltip,
} from 'recharts';

const DEBIT_PALETTE = ['#EF4444', '#F97316', '#F59E0B', '#EC4899', '#8B5CF6', '#6366F1', '#14B8A6'];
const CREDIT_PALETTE = ['#10B981', '#059669', '#34D399', '#06B6D4', '#3B82F6', '#8B5CF6', '#F59E0B'];

export const DashboardPage: React.FC = () => {
  const { user } = useAuth();
  const [summary, setSummary] = useState<DashboardSummary | null>(null);
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [periodFilter, setPeriodFilter] = useState<'ALL' | 'CURRENT_MONTH'>('ALL');
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');

  const loadData = async () => {
    try {
      setIsLoading(true);
      const [sumData, txData] = await Promise.all([
        dashboardApi.getSummary(),
        transactionsApi.list(),
      ]);
      setSummary(sumData);
      setTransactions(txData);
    } catch {
      setError('Não foi possível carregar os dados do dashboard.');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const formatCurrency = (val: number | undefined) => {
    const num = val || 0;
    return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(num);
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <div className="flex flex-col items-center gap-3">
          <div className="w-10 h-10 border-4 border-blue-500 border-t-transparent rounded-full animate-spin"></div>
          <p className="text-slate-400 text-sm">Carregando painel consolidado...</p>
        </div>
      </div>
    );
  }

  if (error || !summary) {
    return (
      <div className="p-8 max-w-4xl mx-auto">
        <div className="bg-rose-500/10 border border-rose-500/20 p-4 rounded-xl text-rose-400 flex items-center gap-3">
          <AlertCircle className="w-6 h-6" />
          <span>{error || 'Erro ao carregar dados'}</span>
        </div>
      </div>
    );
  }

  const currentMonthPrefix = new Date().toISOString().slice(0, 7);

  const activeTransactions = transactions.filter((tx) => {
    if (periodFilter === 'CURRENT_MONTH') {
      return tx.transactionDate && tx.transactionDate.startsWith(currentMonthPrefix);
    }
    return true;
  });

  // Débitos (Despesas) por Categoria
  const expenseTxs = activeTransactions.filter((tx) => tx.type === 'EXPENSE');
  const totalExpense = expenseTxs.reduce((sum, tx) => sum + Number(tx.amount), 0);

  const expenseMap: { [catName: string]: { name: string; value: number; color?: string } } = {};
  expenseTxs.forEach((tx) => {
    const name = tx.category?.name || 'Sem Categoria';
    const color = tx.category?.color;
    if (!expenseMap[name]) {
      expenseMap[name] = { name, value: 0, color };
    }
    expenseMap[name].value += Number(tx.amount);
  });

  const expensePieData = Object.values(expenseMap).map((item, idx) => ({
    name: item.name,
    value: item.value,
    color: item.color || DEBIT_PALETTE[idx % DEBIT_PALETTE.length],
    percentage: totalExpense > 0 ? (item.value / totalExpense) * 100 : 0,
  })).sort((a, b) => b.value - a.value);

  // Créditos (Receitas) por Categoria
  const incomeTxs = activeTransactions.filter((tx) => tx.type === 'INCOME');
  const totalIncome = incomeTxs.reduce((sum, tx) => sum + Number(tx.amount), 0);

  const incomeMap: { [catName: string]: { name: string; value: number; color?: string } } = {};
  incomeTxs.forEach((tx) => {
    const name = tx.category?.name || 'Sem Categoria';
    const color = tx.category?.color;
    if (!incomeMap[name]) {
      incomeMap[name] = { name, value: 0, color };
    }
    incomeMap[name].value += Number(tx.amount);
  });

  const incomePieData = Object.values(incomeMap).map((item, idx) => ({
    name: item.name,
    value: item.value,
    color: item.color || CREDIT_PALETTE[idx % CREDIT_PALETTE.length],
    percentage: totalIncome > 0 ? (item.value / totalIncome) * 100 : 0,
  })).sort((a, b) => b.value - a.value);

  return (
    <div className="space-y-8 pb-12">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl sm:text-3xl font-bold text-white tracking-tight">
            Olá, {user?.name.split(' ')[0]} 👋
          </h1>
          <p className="text-slate-400 text-sm mt-1">
            Aqui está o panorama consolidado do seu patrimônio e finanças hoje.
          </p>
        </div>
        <div className="flex items-center gap-2 bg-slate-800/80 px-3 py-1.5 rounded-xl border border-slate-700/80 text-xs font-medium text-slate-300">
          <Calendar className="w-4 h-4 text-emerald-400" />
          <span>{new Date().toLocaleDateString('pt-BR', { month: 'long', year: 'numeric' })}</span>
        </div>
      </div>

      {/* Summary Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {/* Patrimônio Total */}
        <div className="bg-gradient-to-br from-slate-900 to-slate-850 p-6 rounded-2xl border border-slate-800 shadow-lg relative overflow-hidden">
          <div className="absolute top-0 right-0 p-4 opacity-10">
            <DollarSign className="w-20 h-20 text-emerald-400" />
          </div>
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold uppercase tracking-wider text-slate-400">Patrimônio Total</span>
            <div className="p-2 rounded-xl bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
              <DollarSign className="w-5 h-5" />
            </div>
          </div>
          <div className="mt-4">
            <span className="text-2xl lg:text-3xl font-extrabold text-white">
              {formatCurrency(summary.totalNetWorth)}
            </span>
          </div>
          <p className="mt-2 text-xs text-slate-400">
            Saldo em Contas + Mercado de Ações
          </p>
        </div>

        {/* Saldo Disponível */}
        <div className="bg-gradient-to-br from-slate-900 to-slate-850 p-6 rounded-2xl border border-slate-800 shadow-lg relative overflow-hidden">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold uppercase tracking-wider text-slate-400">Saldo Disponível</span>
            <div className="p-2 rounded-xl bg-blue-500/10 text-blue-400 border border-blue-500/20">
              <Wallet className="w-5 h-5" />
            </div>
          </div>
          <div className="mt-4">
            <span className="text-2xl lg:text-3xl font-extrabold text-white">
              {formatCurrency(summary.availableBalance)}
            </span>
          </div>
          <p className="mt-2 text-xs text-slate-400">
            {summary.accounts.length} conta{summary.accounts.length !== 1 ? 's' : ''} cadastrada{summary.accounts.length !== 1 ? 's' : ''}
          </p>
        </div>

        {/* Investimentos */}
        <div className="bg-gradient-to-br from-slate-900 to-slate-850 p-6 rounded-2xl border border-slate-800 shadow-lg relative overflow-hidden">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold uppercase tracking-wider text-slate-400">Investimentos B3</span>
            <div className="p-2 rounded-xl bg-purple-500/10 text-purple-400 border border-purple-500/20">
              <TrendingUp className="w-5 h-5" />
            </div>
          </div>
          <div className="mt-4">
            <span className="text-2xl lg:text-3xl font-extrabold text-white">
              {formatCurrency(summary.investedBalance)}
            </span>
          </div>
          <div className="mt-2 flex items-center gap-1.5 text-xs">
            <span className={`font-semibold flex items-center ${summary.investmentsProfitLoss >= 0 ? 'text-emerald-400' : 'text-rose-400'}`}>
              {summary.investmentsProfitLoss >= 0 ? '+' : ''}{formatCurrency(summary.investmentsProfitLoss)} ({summary.investmentsProfitLossPercentage >= 0 ? '+' : ''}{summary.investmentsProfitLossPercentage}%)
            </span>
          </div>
        </div>

        {/* Resultado do Mês */}
        <div className="bg-gradient-to-br from-slate-900 to-slate-850 p-6 rounded-2xl border border-slate-800 shadow-lg relative overflow-hidden">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold uppercase tracking-wider text-slate-400">Resultado do Mês</span>
            <div className={`p-2 rounded-xl border ${summary.monthlyResult >= 0 ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20' : 'bg-rose-500/10 text-rose-400 border-rose-500/20'}`}>
              <Activity className="w-5 h-5" />
            </div>
          </div>
          <div className="mt-4">
            <span className={`text-2xl lg:text-3xl font-extrabold ${summary.monthlyResult >= 0 ? 'text-emerald-400' : 'text-rose-400'}`}>
              {formatCurrency(summary.monthlyResult)}
            </span>
          </div>
          <div className="mt-2 flex items-center justify-between text-xs text-slate-400">
            <span className="flex items-center text-emerald-400">
              <ArrowUpRight className="w-3.5 h-3.5 mr-0.5" />
              {formatCurrency(summary.monthlyIncome)}
            </span>
            <span className="flex items-center text-rose-400">
              <ArrowDownRight className="w-3.5 h-3.5 mr-0.5" />
              {formatCurrency(summary.monthlyExpense)}
            </span>
          </div>
        </div>
      </div>

      {/* Seção de Gráficos: Débitos e Créditos por Categoria */}
      <div className="space-y-4">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pt-2">
          <div>
            <h2 className="text-xl font-bold text-white tracking-tight flex items-center gap-2">
              <PieIcon className="w-5 h-5 text-blue-400" />
              Distribuição por Categoria
            </h2>
            <p className="text-xs text-slate-400">
              Detalhamento de cada débito (gasto) e crédito (receita) registrado em transações
            </p>
          </div>

          <div className="flex items-center bg-slate-800/90 p-1 rounded-xl border border-slate-700/80 text-xs font-semibold self-start sm:self-auto">
            <button
              type="button"
              onClick={() => setPeriodFilter('ALL')}
              className={`px-3 py-1.5 rounded-lg transition-all cursor-pointer ${
                periodFilter === 'ALL'
                  ? 'bg-blue-600 text-white shadow-md'
                  : 'text-slate-400 hover:text-white'
              }`}
            >
              Todo o Período
            </button>
            <button
              type="button"
              onClick={() => setPeriodFilter('CURRENT_MONTH')}
              className={`px-3 py-1.5 rounded-lg transition-all cursor-pointer ${
                periodFilter === 'CURRENT_MONTH'
                  ? 'bg-blue-600 text-white shadow-md'
                  : 'text-slate-400 hover:text-white'
              }`}
            >
              Mês Atual
            </button>
          </div>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {/* Gráfico 1: Débitos / Gastos por Categoria */}
          <div className="bg-slate-900 p-6 rounded-2xl border border-slate-800 shadow-md flex flex-col justify-between">
            <div>
              <div className="flex items-center justify-between">
                <h3 className="text-base font-bold text-white flex items-center gap-2">
                  <ArrowDownRight className="w-5 h-5 text-rose-400" />
                  Débitos por Categoria (Despesas)
                </h3>
                <span className="text-xs font-extrabold text-rose-400 bg-rose-500/10 border border-rose-500/20 px-2.5 py-1 rounded-lg">
                  Total: {formatCurrency(totalExpense)}
                </span>
              </div>
              <p className="text-xs text-slate-400 mt-1">
                Onde o seu dinheiro foi gasto nas transações
              </p>
            </div>

            {expensePieData.length > 0 ? (
              <div className="flex flex-col mt-4">
                <div className="h-52 w-full flex items-center justify-center">
                  <ResponsiveContainer width="100%" height="100%">
                    <PieChart>
                      <Pie
                        data={expensePieData}
                        cx="50%"
                        cy="50%"
                        innerRadius={55}
                        outerRadius={80}
                        paddingAngle={4}
                        dataKey="value"
                      >
                        {expensePieData.map((entry, index) => (
                          <Cell key={`expense-cell-${index}`} fill={entry.color} />
                        ))}
                      </Pie>
                      <Tooltip
                        contentStyle={{ backgroundColor: '#0F172A', borderColor: '#334155', borderRadius: '12px', fontSize: '12px' }}
                        formatter={(val: any, _name: any, item: any) => [
                          `${formatCurrency(Number(val))} (${item.payload.percentage.toFixed(1)}%)`,
                          item.payload.name
                        ]}
                      />
                    </PieChart>
                  </ResponsiveContainer>
                </div>

                {/* Legenda com as Porcentagens de cada Item */}
                <div className="mt-4 grid grid-cols-1 sm:grid-cols-2 gap-2 max-h-44 overflow-y-auto pr-1">
                  {expensePieData.map((item, index) => (
                    <div key={index} className="flex items-center justify-between text-xs p-2 rounded-lg bg-slate-800/60 border border-slate-800">
                      <div className="flex items-center gap-2 truncate pr-2">
                        <span className="w-2.5 h-2.5 rounded-full shrink-0" style={{ backgroundColor: item.color }} />
                        <span className="text-slate-300 font-medium truncate">{item.name}</span>
                      </div>
                      <div className="text-right shrink-0">
                        <span className="text-white font-semibold">{formatCurrency(item.value)}</span>
                        <span className="text-rose-400 ml-1.5 font-bold">({item.percentage.toFixed(1)}%)</span>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            ) : (
              <div className="text-center text-slate-500 text-xs py-14">
                Nenhum débito ou despesa registrado no período selecionado.
              </div>
            )}
          </div>

          {/* Gráfico 2: Créditos / Receitas por Categoria */}
          <div className="bg-slate-900 p-6 rounded-2xl border border-slate-800 shadow-md flex flex-col justify-between">
            <div>
              <div className="flex items-center justify-between">
                <h3 className="text-base font-bold text-white flex items-center gap-2">
                  <ArrowUpRight className="w-5 h-5 text-emerald-400" />
                  Créditos por Categoria (Receitas)
                </h3>
                <span className="text-xs font-extrabold text-emerald-400 bg-emerald-500/10 border border-emerald-500/20 px-2.5 py-1 rounded-lg">
                  Total: {formatCurrency(totalIncome)}
                </span>
              </div>
              <p className="text-xs text-slate-400 mt-1">
                Origem e fontes dos créditos registrados
              </p>
            </div>

            {incomePieData.length > 0 ? (
              <div className="flex flex-col mt-4">
                <div className="h-52 w-full flex items-center justify-center">
                  <ResponsiveContainer width="100%" height="100%">
                    <PieChart>
                      <Pie
                        data={incomePieData}
                        cx="50%"
                        cy="50%"
                        innerRadius={55}
                        outerRadius={80}
                        paddingAngle={4}
                        dataKey="value"
                      >
                        {incomePieData.map((entry, index) => (
                          <Cell key={`income-cell-${index}`} fill={entry.color} />
                        ))}
                      </Pie>
                      <Tooltip
                        contentStyle={{ backgroundColor: '#0F172A', borderColor: '#334155', borderRadius: '12px', fontSize: '12px' }}
                        formatter={(val: any, _name: any, item: any) => [
                          `${formatCurrency(Number(val))} (${item.payload.percentage.toFixed(1)}%)`,
                          item.payload.name
                        ]}
                      />
                    </PieChart>
                  </ResponsiveContainer>
                </div>

                {/* Legenda com as Porcentagens de cada Item */}
                <div className="mt-4 grid grid-cols-1 sm:grid-cols-2 gap-2 max-h-44 overflow-y-auto pr-1">
                  {incomePieData.map((item, index) => (
                    <div key={index} className="flex items-center justify-between text-xs p-2 rounded-lg bg-slate-800/60 border border-slate-800">
                      <div className="flex items-center gap-2 truncate pr-2">
                        <span className="w-2.5 h-2.5 rounded-full shrink-0" style={{ backgroundColor: item.color }} />
                        <span className="text-slate-300 font-medium truncate">{item.name}</span>
                      </div>
                      <div className="text-right shrink-0">
                        <span className="text-white font-semibold">{formatCurrency(item.value)}</span>
                        <span className="text-emerald-400 ml-1.5 font-bold">({item.percentage.toFixed(1)}%)</span>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            ) : (
              <div className="text-center text-slate-500 text-xs py-14">
                Nenhum crédito ou receita registrado no período selecionado.
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Tables Section */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Minha Carteira Preview */}
        <div className="bg-slate-900 p-6 rounded-2xl border border-slate-800 shadow-md">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-lg font-bold text-white flex items-center gap-2">
              <TrendingUp className="w-5 h-5 text-emerald-400" />
              Minha Carteira
            </h2>
            <span className="text-xs text-slate-400">
              {summary.positions.length} ativo{summary.positions.length !== 1 ? 's' : ''}
            </span>
          </div>

          {summary.positions.length === 0 ? (
            <p className="text-slate-500 text-sm py-4">Nenhum investimento registrado.</p>
          ) : (
            <div className="divide-y divide-slate-800">
              {summary.positions.slice(0, 5).map((pos) => (
                <div key={pos.assetId} className="py-3 flex items-center justify-between">
                  <div>
                    <div className="flex items-center gap-2">
                      <span className="font-bold text-white text-sm">{pos.ticker}</span>
                      <span className="text-[10px] font-semibold px-2 py-0.5 rounded bg-slate-800 text-slate-300 border border-slate-700">
                        {pos.type}
                      </span>
                    </div>
                    <span className="text-xs text-slate-400">
                      {pos.quantity} cotas • PM: {formatCurrency(pos.averagePrice)}
                    </span>
                  </div>

                  <div className="text-right">
                    <span className="block font-semibold text-white text-sm">
                      {formatCurrency(pos.currentValue)}
                    </span>
                    <span className={`text-xs font-medium ${pos.profitLoss >= 0 ? 'text-emerald-400' : 'text-rose-400'}`}>
                      {pos.profitLoss >= 0 ? '+' : ''}{formatCurrency(pos.profitLoss)} ({pos.profitLossPercentage >= 0 ? '+' : ''}{pos.profitLossPercentage}%)
                    </span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Últimas Transações */}
        <div className="bg-slate-900 p-6 rounded-2xl border border-slate-800 shadow-md">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-lg font-bold text-white flex items-center gap-2">
              <Wallet className="w-5 h-5 text-blue-400" />
              Últimas Transações
            </h2>
            <span className="text-xs text-slate-400">Recentes</span>
          </div>

          {summary.recentTransactions.length === 0 ? (
            <p className="text-slate-500 text-sm py-4">Nenhuma transação financeira registrada.</p>
          ) : (
            <div className="divide-y divide-slate-800">
              {summary.recentTransactions.map((tx) => (
                <div key={tx.id} className="py-3 flex items-center justify-between">
                  <div>
                    <span className="font-semibold text-white text-sm block">{tx.description}</span>
                    <span className="text-xs text-slate-400">
                      {new Date(tx.transactionDate).toLocaleDateString('pt-BR')} • {tx.account.name}
                    </span>
                  </div>

                  <div className="text-right">
                    <span
                      className={`font-bold text-sm ${
                        tx.type === 'INCOME'
                          ? 'text-emerald-400'
                          : tx.type === 'EXPENSE'
                          ? 'text-rose-400'
                          : 'text-blue-400'
                      }`}
                    >
                      {tx.type === 'INCOME' ? '+' : tx.type === 'EXPENSE' ? '-' : ''}
                      {formatCurrency(tx.amount)}
                    </span>
                    <span className="block text-[11px] text-slate-400 capitalize">
                      {tx.type === 'INCOME' ? 'Receita' : tx.type === 'EXPENSE' ? 'Despesa' : 'Transferência'}
                    </span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
