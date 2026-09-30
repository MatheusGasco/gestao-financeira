import React, { useState, useEffect } from 'react';
import { accountsApi } from '../services/api';
import type { Account, AccountType } from '../types';
import { Wallet, Plus, Trash2, Landmark, PiggyBank, CircleDollarSign, TrendingUp } from 'lucide-react';

export const AccountsPage: React.FC = () => {
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  // Modal
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [name, setName] = useState('');
  const [type, setType] = useState<AccountType>('CHECKING');
  const [initialBalance, setInitialBalance] = useState('0.00');
  const [color, setColor] = useState('#3B82F6');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const loadAccounts = async () => {
    try {
      setIsLoading(true);
      const data = await accountsApi.list();
      setAccounts(data);
    } catch (err) {
      console.error(err);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadAccounts();
  }, []);

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim()) return;

    try {
      setIsSubmitting(true);
      await accountsApi.create({
        name: name.trim(),
        type,
        initialBalance: parseFloat(initialBalance) || 0,
        color,
      });

      setIsModalOpen(false);
      setName('');
      setInitialBalance('0.00');
      await loadAccounts();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Erro ao criar conta');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleDelete = async (id: string) => {
    if (!window.confirm('Tem certeza que deseja excluir esta conta? Todas as transações associadas serão removidas.')) {
      return;
    }

    try {
      await accountsApi.delete(id);
      await loadAccounts();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Erro ao excluir conta');
    }
  };

  const formatCurrency = (val: number) => {
    return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(val);
  };

  const getAccountIcon = (accType: AccountType) => {
    switch (accType) {
      case 'CHECKING':
        return <Landmark className="w-5 h-5 text-blue-400" />;
      case 'SAVINGS':
        return <PiggyBank className="w-5 h-5 text-emerald-400" />;
      case 'INVESTMENT':
        return <TrendingUp className="w-5 h-5 text-purple-400" />;
      case 'CASH':
        return <CircleDollarSign className="w-5 h-5 text-amber-400" />;
    }
  };

  const getAccountTypeLabel = (accType: AccountType) => {
    switch (accType) {
      case 'CHECKING': return 'Conta Corrente';
      case 'SAVINGS': return 'Poupança';
      case 'INVESTMENT': return 'Investimentos';
      case 'CASH': return 'Dinheiro Físico';
    }
  };

  const totalBalance = accounts.reduce((sum, a) => sum + Number(a.balance), 0);

  return (
    <div className="space-y-8 pb-12">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl sm:text-3xl font-bold text-white tracking-tight">
            Contas & Carteiras
          </h1>
          <p className="text-slate-400 text-sm mt-1">
            Gerencie suas contas bancárias, corretoras e reservas
          </p>
        </div>

        <button
          onClick={() => setIsModalOpen(true)}
          className="flex items-center justify-center gap-2 bg-gradient-to-r from-blue-600 to-emerald-600 hover:from-blue-500 hover:to-emerald-500 text-white px-4 py-2.5 rounded-xl font-medium shadow-lg shadow-blue-500/20 transition-all cursor-pointer"
        >
          <Plus className="w-4 h-4" />
          Nova Conta
        </button>
      </div>

      {/* Card Total */}
      <div className="bg-slate-900 p-6 rounded-2xl border border-slate-800 shadow-md flex items-center justify-between">
        <div>
          <span className="text-xs font-semibold uppercase tracking-wider text-slate-400">
            Saldo Total em Todas as Contas
          </span>
          <div className="mt-2 text-3xl font-extrabold text-white">
            {formatCurrency(totalBalance)}
          </div>
        </div>
        <div className="p-3 rounded-2xl bg-blue-500/10 border border-blue-500/20 text-blue-400">
          <Wallet className="w-8 h-8" />
        </div>
      </div>

      {/* Grid de Contas */}
      {isLoading ? (
        <div className="p-8 text-center text-slate-400">Carregando contas...</div>
      ) : accounts.length === 0 ? (
        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-12 text-center text-slate-500">
          Nenhuma conta bancária cadastrada. Clique em "Nova Conta" para cadastrar seu primeiro banco ou carteira.
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {accounts.map((acc) => (
            <div
              key={acc.id}
              className="bg-slate-900 rounded-2xl border border-slate-800 p-5 shadow-lg relative overflow-hidden flex flex-col justify-between"
            >
              <div
                className="absolute top-0 left-0 right-0 h-1.5"
                style={{ backgroundColor: acc.color || '#3B82F6' }}
              />

              <div>
                <div className="flex items-center justify-between mb-4">
                  <div className="flex items-center gap-3">
                    <div className="p-2 rounded-xl bg-slate-800 border border-slate-700">
                      {getAccountIcon(acc.type)}
                    </div>
                    <div>
                      <h3 className="font-bold text-white text-base">{acc.name}</h3>
                      <span className="text-xs text-slate-400">{getAccountTypeLabel(acc.type)}</span>
                    </div>
                  </div>

                  <button
                    onClick={() => handleDelete(acc.id)}
                    className="p-1.5 text-slate-500 hover:text-rose-400 hover:bg-rose-500/10 rounded-lg transition-colors cursor-pointer"
                    title="Excluir conta"
                  >
                    <Trash2 className="w-4 h-4" />
                  </button>
                </div>

                <div className="mt-4">
                  <span className="text-xs text-slate-400">Saldo Atual</span>
                  <div className="text-2xl font-extrabold text-white mt-0.5">
                    {formatCurrency(acc.balance)}
                  </div>
                </div>
              </div>

              <div className="mt-6 pt-3 border-t border-slate-800 text-[11px] text-slate-500 flex justify-between">
                <span>Moeda: {acc.currency}</span>
                <span>Criada em {new Date(acc.createdAt).toLocaleDateString('pt-BR')}</span>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Modal Nova Conta */}
      {isModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 backdrop-blur-xs p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl max-w-md w-full p-6 shadow-2xl">
            <h2 className="text-xl font-bold text-white mb-4">Nova Conta Financeira</h2>

            <form onSubmit={handleCreate} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-400 uppercase mb-1">
                  Nome da Conta
                </label>
                <input
                  type="text"
                  required
                  placeholder="Ex: Nubank, Itaú Corrente, Carteira"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  className="w-full px-3 py-2 bg-slate-800 border border-slate-700 rounded-lg text-sm text-white focus:outline-none focus:ring-1 focus:ring-blue-500"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-400 uppercase mb-1">
                  Tipo de Conta
                </label>
                <select
                  value={type}
                  onChange={(e) => setType(e.target.value as AccountType)}
                  className="w-full px-3 py-2 bg-slate-800 border border-slate-700 rounded-lg text-sm text-white focus:outline-none focus:ring-1 focus:ring-blue-500"
                >
                  <option value="CHECKING">Conta Corrente</option>
                  <option value="SAVINGS">Poupança</option>
                  <option value="INVESTMENT">Corretora / Investimentos</option>
                  <option value="CASH">Dinheiro Físico</option>
                </select>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-400 uppercase mb-1">
                  Saldo Inicial (R$)
                </label>
                <input
                  type="number"
                  step="0.01"
                  required
                  value={initialBalance}
                  onChange={(e) => setInitialBalance(e.target.value)}
                  className="w-full px-3 py-2 bg-slate-800 border border-slate-700 rounded-lg text-sm text-white focus:outline-none focus:ring-1 focus:ring-blue-500"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-400 uppercase mb-1">
                  Cor de Identificação
                </label>
                <input
                  type="color"
                  value={color}
                  onChange={(e) => setColor(e.target.value)}
                  className="h-9 w-full rounded-lg cursor-pointer bg-slate-800 border border-slate-700"
                />
              </div>

              <div className="flex items-center justify-end gap-3 pt-4 border-t border-slate-800">
                <button
                  type="button"
                  onClick={() => setIsModalOpen(false)}
                  className="px-4 py-2 text-sm text-slate-400 hover:text-white transition-colors cursor-pointer"
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  disabled={isSubmitting}
                  className="px-5 py-2 text-sm font-semibold text-white bg-blue-600 hover:bg-blue-500 rounded-lg shadow-md transition-all cursor-pointer disabled:opacity-50"
                >
                  {isSubmitting ? 'Criando...' : 'Criar Conta'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
