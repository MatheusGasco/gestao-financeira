import React, { useState, useEffect } from 'react';
import { transactionsApi, accountsApi, categoriesApi } from '../services/api';
import type { Transaction, Account, Category, TransactionType } from '../types';
import {
  Plus,
  ArrowUpRight,
  ArrowDownRight,
  ArrowRightLeft,
  Trash2,
  Filter,
  Search,
  AlertCircle
} from 'lucide-react';

export const TransactionsPage: React.FC = () => {
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');

  // Filtros
  const [filterType, setFilterType] = useState<string>('ALL');
  const [searchTerm, setSearchTerm] = useState<string>('');

  // Modal de criação
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [newType, setNewType] = useState<TransactionType>('EXPENSE');
  const [newAccountId, setNewAccountId] = useState('');
  const [newDestinationAccountId, setNewDestinationAccountId] = useState('');
  const [newCategoryId, setNewCategoryId] = useState('');
  const [newAmount, setNewAmount] = useState('');
  const [newDate, setNewDate] = useState(new Date().toISOString().split('T')[0]);
  const [newDescription, setNewDescription] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const loadData = async () => {
    try {
      setIsLoading(true);
      setError('');
      const [txData, accData, catData] = await Promise.all([
        transactionsApi.list(),
        accountsApi.list(),
        categoriesApi.list(),
      ]);
      setTransactions(txData);
      setAccounts(accData);
      setCategories(catData);
      if (accData.length > 0 && !newAccountId) {
        setNewAccountId(accData[0].id);
      }
    } catch {
      setError('Erro ao carregar transações.');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newAccountId || !newAmount || !newDescription) return;

    try {
      setIsSubmitting(true);
      await transactionsApi.create({
        accountId: newAccountId,
        destinationAccountId: newType === 'TRANSFER' ? newDestinationAccountId : undefined,
        categoryId: newCategoryId ? newCategoryId : undefined,
        type: newType,
        amount: parseFloat(newAmount),
        transactionDate: newDate,
        description: newDescription,
        isPaid: true,
      });

      setIsModalOpen(false);
      setNewAmount('');
      setNewDescription('');
      await loadData();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Erro ao lançar transação');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleDelete = async (id: string) => {
    if (!window.confirm('Tem certeza que deseja excluir esta transação? O saldo da conta será recalculado.')) {
      return;
    }

    try {
      await transactionsApi.delete(id);
      await loadData();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Erro ao excluir transação');
    }
  };

  const formatCurrency = (val: number) => {
    return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(val);
  };

  const filteredTransactions = transactions.filter((t) => {
    const matchesType = filterType === 'ALL' || t.type === filterType;
    const matchesSearch =
      t.description.toLowerCase().includes(searchTerm.toLowerCase()) ||
      t.account.name.toLowerCase().includes(searchTerm.toLowerCase());
    return matchesType && matchesSearch;
  });

  return (
    <div className="space-y-6 pb-12">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl sm:text-3xl font-bold text-white tracking-tight">
            Transações Financeiras
          </h1>
          <p className="text-slate-400 text-sm mt-1">
            Lance receitas, despesas e transferências com recálculo automático de saldos
          </p>
        </div>

        <button
          onClick={() => setIsModalOpen(true)}
          disabled={accounts.length === 0}
          className="flex items-center justify-center gap-2 bg-gradient-to-r from-blue-600 to-emerald-600 hover:from-blue-500 hover:to-emerald-500 text-white px-4 py-2.5 rounded-xl font-medium shadow-lg shadow-blue-500/20 transition-all cursor-pointer disabled:opacity-50"
        >
          <Plus className="w-4 h-4" />
          Nova Transação
        </button>
      </div>

      {error && (
        <div className="bg-rose-500/10 border border-rose-500/20 p-4 rounded-xl text-rose-400 flex items-center gap-3">
          <AlertCircle className="w-5 h-5 flex-shrink-0" />
          <span>{error}</span>
        </div>
      )}

      {accounts.length === 0 && !isLoading && (
        <div className="bg-amber-500/10 border border-amber-500/20 p-4 rounded-xl text-amber-400 flex items-center gap-3">
          <AlertCircle className="w-5 h-5 flex-shrink-0" />
          <span>Você precisa cadastrar pelo menos uma conta bancária antes de lançar transações.</span>
        </div>
      )}

      {/* Barra de Filtros */}
      <div className="bg-slate-900 p-4 rounded-xl border border-slate-800 flex flex-col sm:flex-row gap-4 justify-between items-center">
        <div className="flex items-center gap-2 w-full sm:w-auto">
          <div className="relative w-full sm:w-64">
            <Search className="w-4 h-4 text-slate-500 absolute left-3 top-3" />
            <input
              type="text"
              placeholder="Buscar por descrição..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full pl-9 pr-3 py-2 bg-slate-800/80 border border-slate-700 rounded-lg text-sm text-white placeholder-slate-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
            />
          </div>
        </div>

        <div className="flex items-center gap-2 w-full sm:w-auto">
          <Filter className="w-4 h-4 text-slate-400" />
          <select
            value={filterType}
            onChange={(e) => setFilterType(e.target.value)}
            className="bg-slate-800 border border-slate-700 rounded-lg px-3 py-2 text-sm text-white focus:outline-none focus:ring-1 focus:ring-blue-500"
          >
            <option value="ALL">Todos os Tipos</option>
            <option value="INCOME">Receitas</option>
            <option value="EXPENSE">Despesas</option>
            <option value="TRANSFER">Transferências</option>
          </select>
        </div>
      </div>

      {/* Lista / Tabela */}
      <div className="bg-slate-900 rounded-2xl border border-slate-800 overflow-hidden shadow-md">
        {isLoading ? (
          <div className="p-8 text-center text-slate-400">Carregando transações...</div>
        ) : filteredTransactions.length === 0 ? (
          <div className="p-12 text-center text-slate-500">
            Nenhuma transação encontrada para os filtros selecionados.
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="border-b border-slate-800 bg-slate-950/40 text-slate-400 text-xs uppercase font-semibold">
                  <th className="py-3 px-4">Data</th>
                  <th className="py-3 px-4">Descrição</th>
                  <th className="py-3 px-4">Conta</th>
                  <th className="py-3 px-4">Categoria</th>
                  <th className="py-3 px-4 text-right">Valor</th>
                  <th className="py-3 px-4 text-center">Ações</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800 text-sm">
                {filteredTransactions.map((tx) => (
                  <tr key={tx.id} className="hover:bg-slate-850/50 transition-colors">
                    <td className="py-3.5 px-4 text-slate-400 whitespace-nowrap">
                      {new Date(tx.transactionDate).toLocaleDateString('pt-BR')}
                    </td>
                    <td className="py-3.5 px-4 font-medium text-white">
                      <div className="flex items-center gap-2">
                        <div
                          className={`p-1.5 rounded-lg ${
                            tx.type === 'INCOME'
                              ? 'bg-emerald-500/10 text-emerald-400'
                              : tx.type === 'EXPENSE'
                              ? 'bg-rose-500/10 text-rose-400'
                              : 'bg-blue-500/10 text-blue-400'
                          }`}
                        >
                          {tx.type === 'INCOME' ? (
                            <ArrowUpRight className="w-3.5 h-3.5" />
                          ) : tx.type === 'EXPENSE' ? (
                            <ArrowDownRight className="w-3.5 h-3.5" />
                          ) : (
                            <ArrowRightLeft className="w-3.5 h-3.5" />
                          )}
                        </div>
                        <span>{tx.description}</span>
                      </div>
                    </td>
                    <td className="py-3.5 px-4 text-slate-300">
                      {tx.type === 'TRANSFER' && tx.destinationAccount ? (
                        <span>
                          {tx.account.name} → {tx.destinationAccount.name}
                        </span>
                      ) : (
                        tx.account.name
                      )}
                    </td>
                    <td className="py-3.5 px-4 text-slate-400">
                      {tx.category ? (
                        <span className="inline-flex items-center px-2 py-0.5 rounded text-xs bg-slate-800 text-slate-300 border border-slate-700">
                          {tx.category.name}
                        </span>
                      ) : (
                        <span className="text-slate-600">—</span>
                      )}
                    </td>
                    <td
                      className={`py-3.5 px-4 text-right font-semibold whitespace-nowrap ${
                        tx.type === 'INCOME'
                          ? 'text-emerald-400'
                          : tx.type === 'EXPENSE'
                          ? 'text-rose-400'
                          : 'text-blue-400'
                      }`}
                    >
                      {tx.type === 'INCOME' ? '+' : tx.type === 'EXPENSE' ? '-' : ''}
                      {formatCurrency(tx.amount)}
                    </td>
                    <td className="py-3.5 px-4 text-center">
                      <button
                        onClick={() => handleDelete(tx.id)}
                        className="p-1.5 text-slate-500 hover:text-rose-400 hover:bg-rose-500/10 rounded-lg transition-colors cursor-pointer"
                        title="Excluir"
                      >
                        <Trash2 className="w-4 h-4" />
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Modal Nova Transação */}
      {isModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 backdrop-blur-xs p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl max-w-md w-full p-6 shadow-2xl">
            <h2 className="text-xl font-bold text-white mb-4">Nova Transação</h2>

            <form onSubmit={handleCreate} className="space-y-4">
              {/* Tipo */}
              <div>
                <label className="block text-xs font-semibold text-slate-400 uppercase mb-2">
                  Tipo
                </label>
                <div className="grid grid-cols-3 gap-2">
                  <button
                    type="button"
                    onClick={() => setNewType('EXPENSE')}
                    className={`py-2 text-xs font-semibold rounded-lg border transition-all cursor-pointer ${
                      newType === 'EXPENSE'
                        ? 'bg-rose-500/20 text-rose-400 border-rose-500/40'
                        : 'bg-slate-800 text-slate-400 border-slate-700'
                    }`}
                  >
                    Despesa
                  </button>
                  <button
                    type="button"
                    onClick={() => setNewType('INCOME')}
                    className={`py-2 text-xs font-semibold rounded-lg border transition-all cursor-pointer ${
                      newType === 'INCOME'
                        ? 'bg-emerald-500/20 text-emerald-400 border-emerald-500/40'
                        : 'bg-slate-800 text-slate-400 border-slate-700'
                    }`}
                  >
                    Receita
                  </button>
                  <button
                    type="button"
                    onClick={() => setNewType('TRANSFER')}
                    className={`py-2 text-xs font-semibold rounded-lg border transition-all cursor-pointer ${
                      newType === 'TRANSFER'
                        ? 'bg-blue-500/20 text-blue-400 border-blue-500/40'
                        : 'bg-slate-800 text-slate-400 border-slate-700'
                    }`}
                  >
                    Transferência
                  </button>
                </div>
              </div>

              {/* Descrição */}
              <div>
                <label className="block text-xs font-semibold text-slate-400 uppercase mb-1">
                  Descrição
                </label>
                <input
                  type="text"
                  required
                  placeholder="Ex: Supermercado, Salário, etc."
                  value={newDescription}
                  onChange={(e) => setNewDescription(e.target.value)}
                  className="w-full px-3 py-2 bg-slate-800 border border-slate-700 rounded-lg text-sm text-white focus:outline-none focus:ring-1 focus:ring-blue-500"
                />
              </div>

              {/* Valor */}
              <div>
                <label className="block text-xs font-semibold text-slate-400 uppercase mb-1">
                  Valor (R$)
                </label>
                <input
                  type="number"
                  step="0.01"
                  required
                  placeholder="0,00"
                  value={newAmount}
                  onChange={(e) => setNewAmount(e.target.value)}
                  className="w-full px-3 py-2 bg-slate-800 border border-slate-700 rounded-lg text-sm text-white focus:outline-none focus:ring-1 focus:ring-blue-500"
                />
              </div>

              {/* Conta de Origem */}
              <div>
                <label className="block text-xs font-semibold text-slate-400 uppercase mb-1">
                  {newType === 'TRANSFER' ? 'Conta de Origem' : 'Conta'}
                </label>
                <select
                  required
                  value={newAccountId}
                  onChange={(e) => setNewAccountId(e.target.value)}
                  className="w-full px-3 py-2 bg-slate-800 border border-slate-700 rounded-lg text-sm text-white focus:outline-none focus:ring-1 focus:ring-blue-500"
                >
                  {accounts.map((acc) => (
                    <option key={acc.id} value={acc.id}>
                      {acc.name} (Saldo: {formatCurrency(acc.balance)})
                    </option>
                  ))}
                </select>
              </div>

              {/* Se for transferência: Conta de Destino */}
              {newType === 'TRANSFER' && (
                <div>
                  <label className="block text-xs font-semibold text-slate-400 uppercase mb-1">
                    Conta de Destino
                  </label>
                  <select
                    required
                    value={newDestinationAccountId}
                    onChange={(e) => setNewDestinationAccountId(e.target.value)}
                    className="w-full px-3 py-2 bg-slate-800 border border-slate-700 rounded-lg text-sm text-white focus:outline-none focus:ring-1 focus:ring-blue-500"
                  >
                    <option value="">Selecione a conta destino...</option>
                    {accounts
                      .filter((acc) => acc.id !== newAccountId)
                      .map((acc) => (
                        <option key={acc.id} value={acc.id}>
                          {acc.name} (Saldo: {formatCurrency(acc.balance)})
                        </option>
                      ))}
                  </select>
                </div>
              )}

              {/* Categoria (se não for transferência) */}
              {newType !== 'TRANSFER' && (
                <div>
                  <label className="block text-xs font-semibold text-slate-400 uppercase mb-1">
                    Categoria
                  </label>
                  <select
                    value={newCategoryId}
                    onChange={(e) => setNewCategoryId(e.target.value)}
                    className="w-full px-3 py-2 bg-slate-800 border border-slate-700 rounded-lg text-sm text-white focus:outline-none focus:ring-1 focus:ring-blue-500"
                  >
                    <option value="">Sem categoria</option>
                    {categories
                      .filter((c) => c.type === newType)
                      .map((cat) => (
                        <option key={cat.id} value={cat.id}>
                          {cat.name}
                        </option>
                      ))}
                  </select>
                </div>
              )}

              {/* Data */}
              <div>
                <label className="block text-xs font-semibold text-slate-400 uppercase mb-1">
                  Data
                </label>
                <input
                  type="date"
                  required
                  value={newDate}
                  onChange={(e) => setNewDate(e.target.value)}
                  className="w-full px-3 py-2 bg-slate-800 border border-slate-700 rounded-lg text-sm text-white focus:outline-none focus:ring-1 focus:ring-blue-500"
                />
              </div>

              {/* Botões */}
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
                  {isSubmitting ? 'Salvando...' : 'Salvar Transação'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
