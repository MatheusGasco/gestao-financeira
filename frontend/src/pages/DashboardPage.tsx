import React, { useEffect, useState } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { dashboardApi } from '../services/api';
import type { DashboardSummary, MonthlyPoint } from '../types';
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
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  Legend,
  PieChart,
  Pie,
  Cell,
} from 'recharts';

const COLORS = ['#3B82F6', '#10B981', '#F59E0B', '#8B5CF6', '#EC4899', '#06B6D4'];

export const DashboardPage: React.FC = () => {
  const { user } = useAuth();
  const [summary, setSummary] = useState<DashboardSummary | null>(null);
  const [evolution, setEvolution] = useState<MonthlyPoint[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');

  const loadData = async () => {
    try {
      setIsLoading(true);
      const [sumData, evoData] = await Promise.all([
        dashboardApi.getSummary(),
        dashboardApi.getEvolution(),
      ]);
      setSummary(sumData);
      setEvolution(evoData);
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

  // Prepara dados de pizza por tipo de ativo
  const allocationByType = summary.positions.reduce((acc: { [key: string]: number }, pos) => {
    acc[pos.type] = (acc[pos.type] || 0) + Number(pos.currentValue);
    return acc;
  }, {});

  const pieData = Object.entries(allocationByType).map(([name, value]) => ({
    name: name === 'STOCK' ? 'Ações' : name === 'FII' ? 'Fundos Imob.' : name,
    value,
  }));

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

      {/* Gráficos */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Evolução Financeira */}
        <div className="lg:col-span-2 bg-slate-900 p-6 rounded-2xl border border-slate-800 shadow-md">
          <h2 className="text-lg font-bold text-white mb-1 flex items-center gap-2">
            <Activity className="w-5 h-5 text-blue-400" />
            Evolução de Fluxo de Caixa
          </h2>
          <p className="text-xs text-slate-400 mb-6">Receitas vs. Despesas nos últimos meses</p>

          <div className="h-72 w-full">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={evolution} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                <XAxis dataKey="month" stroke="#64748B" fontSize={12} tickLine={false} />
                <YAxis stroke="#64748B" fontSize={12} tickLine={false} tickFormatter={(v) => `R$${v}`} />
                <Tooltip
                  contentStyle={{ backgroundColor: '#0F172A', borderColor: '#334155', borderRadius: '12px', fontSize: '12px' }}
                  formatter={(value: any) => [formatCurrency(Number(value)), '']}
                />
                <Legend wrapperStyle={{ paddingTop: '10px', fontSize: '12px' }} />
                <Bar dataKey="income" name="Receitas" fill="#10B981" radius={[4, 4, 0, 0]} />
                <Bar dataKey="expense" name="Despesas" fill="#EF4444" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Alocação da Carteira */}
        <div className="bg-slate-900 p-6 rounded-2xl border border-slate-800 shadow-md flex flex-col">
          <h2 className="text-lg font-bold text-white mb-1 flex items-center gap-2">
            <PieIcon className="w-5 h-5 text-purple-400" />
            Alocação de Ativos
          </h2>
          <p className="text-xs text-slate-400 mb-6">Distribuição da custódia por classe</p>

          <div className="flex-1 flex items-center justify-center min-h-[220px]">
            {pieData.length > 0 ? (
              <ResponsiveContainer width="100%" height={220}>
                <PieChart>
                  <Pie
                    data={pieData}
                    cx="50%"
                    cy="50%"
                    innerRadius={55}
                    outerRadius={80}
                    paddingAngle={4}
                    dataKey="value"
                  >
                    {pieData.map((_, index) => (
                      <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                    ))}
                  </Pie>
                  <Tooltip
                    contentStyle={{ backgroundColor: '#0F172A', borderColor: '#334155', borderRadius: '12px', fontSize: '12px' }}
                    formatter={(val: any) => [formatCurrency(Number(val)), '']}
                  />
                  <Legend wrapperStyle={{ fontSize: '12px' }} />
                </PieChart>
              </ResponsiveContainer>
            ) : (
              <div className="text-center text-slate-500 text-xs py-8">
                Nenhum ativo em carteira ainda.<br />Cadastre compras no módulo de Investimentos.
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
