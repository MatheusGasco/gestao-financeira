import React, { useState, useEffect } from 'react';
import { investmentsApi, marketApi } from '../services/api';
import type { InvestmentPosition, InvestmentOrder, InvestmentTransactionType, MarketQuote } from '../types';
import {
  Plus,
  Search,
  Briefcase,
  Clock,
} from 'lucide-react';

export const InvestmentsPage: React.FC = () => {
  const [positions, setPositions] = useState<InvestmentPosition[]>([]);
  const [orders, setOrders] = useState<InvestmentOrder[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  // Busca de cotação
  const [searchTicker, setSearchTicker] = useState('');
  const [searchedQuote, setSearchedQuote] = useState<MarketQuote | null>(null);
  const [isSearchingQuote, setIsSearchingQuote] = useState(false);
  const [searchError, setSearchError] = useState('');

  // Modal de Ordem
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [orderType, setOrderType] = useState<InvestmentTransactionType>('BUY');
  const [orderTicker, setOrderTicker] = useState('');
  const [orderQuantity, setOrderQuantity] = useState('');
  const [orderPrice, setOrderPrice] = useState('');
  const [orderFees, setOrderFees] = useState('0.00');
  const [orderDate, setOrderDate] = useState(new Date().toISOString().split('T')[0]);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const loadData = async () => {
    try {
      setIsLoading(true);
      const [posData, ordData] = await Promise.all([
        investmentsApi.getPositions(),
        investmentsApi.listOrders(),
      ]);
      setPositions(posData);
      setOrders(ordData);
    } catch (err) {
      console.error(err);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleSearchQuote = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!searchTicker.trim()) return;

    try {
      setIsSearchingQuote(true);
      setSearchError('');
      const quote = await marketApi.getQuote(searchTicker.trim());
      setSearchedQuote(quote);
    } catch {
      setSearchError('Ativo não encontrado ou erro na cotação.');
    } finally {
      setIsSearchingQuote(false);
    }
  };

  const handleOpenOrderModalWithTicker = (ticker: string, currentPrice?: number) => {
    setOrderTicker(ticker);
    if (currentPrice) {
      setOrderPrice(currentPrice.toString());
    }
    setIsModalOpen(true);
  };

  const handleCreateOrder = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!orderTicker || !orderQuantity || !orderPrice) return;

    try {
      setIsSubmitting(true);
      await investmentsApi.recordOrder({
        ticker: orderTicker.trim().toUpperCase(),
        type: orderType,
        quantity: parseFloat(orderQuantity),
        unitPrice: parseFloat(orderPrice),
        fees: parseFloat(orderFees) || 0,
        transactionDate: orderDate,
      });

      setIsModalOpen(false);
      setOrderQuantity('');
      setOrderPrice('');
      setOrderFees('0.00');
      await loadData();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Erro ao registrar ordem');
    } finally {
      setIsSubmitting(false);
    }
  };

  const formatCurrency = (val: number) => {
    return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(val);
  };

  const totalCost = positions.reduce((acc, p) => acc + Number(p.totalCost), 0);
  const totalValue = positions.reduce((acc, p) => acc + Number(p.currentValue), 0);
  const totalProfit = totalValue - totalCost;
  const totalProfitPercentage = totalCost > 0 ? (totalProfit / totalCost) * 100 : 0;

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <div className="flex flex-col items-center gap-3">
          <div className="w-10 h-10 border-4 border-purple-500 border-t-transparent rounded-full animate-spin"></div>
          <p className="text-slate-400 text-sm">Carregando carteira de investimentos...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-8 pb-12">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl sm:text-3xl font-bold text-white tracking-tight">
            Investimentos & Bolsa B3
          </h1>
          <p className="text-slate-400 text-sm mt-1">
            Acompanhe sua carteira com Preço Médio ponderado e cotações de mercado
          </p>
        </div>

        <button
          onClick={() => {
            setOrderTicker('');
            setOrderPrice('');
            setIsModalOpen(true);
          }}
          className="flex items-center justify-center gap-2 bg-gradient-to-r from-purple-600 to-blue-600 hover:from-purple-500 hover:to-blue-500 text-white px-4 py-2.5 rounded-xl font-medium shadow-lg shadow-purple-500/20 transition-all cursor-pointer"
        >
          <Plus className="w-4 h-4" />
          Registrar Operação
        </button>
      </div>

      {/* Cards de Métricas da Carteira */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="bg-slate-900 p-5 rounded-2xl border border-slate-800 shadow-md">
          <span className="text-xs font-semibold uppercase tracking-wider text-slate-400">Total Investido (Custo)</span>
          <div className="mt-3 text-2xl font-bold text-white">
            {formatCurrency(totalCost)}
          </div>
          <p className="mt-1 text-xs text-slate-500">Soma dos aportes ponderados</p>
        </div>

        <div className="bg-slate-900 p-5 rounded-2xl border border-slate-800 shadow-md">
          <span className="text-xs font-semibold uppercase tracking-wider text-slate-400">Valor Atual a Mercado</span>
          <div className="mt-3 text-2xl font-bold text-white">
            {formatCurrency(totalValue)}
          </div>
          <p className="mt-1 text-xs text-slate-500">Cotações em tempo real</p>
        </div>

        <div className="bg-slate-900 p-5 rounded-2xl border border-slate-800 shadow-md">
          <span className="text-xs font-semibold uppercase tracking-wider text-slate-400">Resultado Não Realizado</span>
          <div className={`mt-3 text-2xl font-bold ${totalProfit >= 0 ? 'text-emerald-400' : 'text-rose-400'}`}>
            {totalProfit >= 0 ? '+' : ''}{formatCurrency(totalProfit)}
          </div>
          <p className="mt-1 text-xs text-slate-500">Valor atual - Custo de aquisição</p>
        </div>

        <div className="bg-slate-900 p-5 rounded-2xl border border-slate-800 shadow-md">
          <span className="text-xs font-semibold uppercase tracking-wider text-slate-400">Rentabilidade Global</span>
          <div className={`mt-3 text-2xl font-bold ${totalProfitPercentage >= 0 ? 'text-emerald-400' : 'text-rose-400'}`}>
            {totalProfitPercentage >= 0 ? '+' : ''}{totalProfitPercentage.toFixed(2)}%
          </div>
          <p className="mt-1 text-xs text-slate-500">Retorno percentual da carteira</p>
        </div>
      </div>

      {/* Caixa de Busca de Cotação da B3 */}
      <div className="bg-gradient-to-r from-slate-900 via-slate-850 to-slate-900 p-6 rounded-2xl border border-slate-800 shadow-lg">
        <h2 className="text-base font-bold text-white mb-2 flex items-center gap-2">
          <Search className="w-5 h-5 text-blue-400" />
          Consultar Cotação B3 (Brapi)
        </h2>
        <p className="text-xs text-slate-400 mb-4">
          Digite o código de qualquer ação, FII ou ETF (ex: PETR4, VALE3, MXRF11, IVVB11, WEGE3)
        </p>

        <form onSubmit={handleSearchQuote} className="flex gap-2 max-w-md">
          <input
            type="text"
            placeholder="Ex: PETR4"
            value={searchTicker}
            onChange={(e) => setSearchTicker(e.target.value.toUpperCase())}
            className="flex-1 px-3 py-2 bg-slate-800 border border-slate-700 rounded-xl text-sm font-semibold tracking-wider text-white uppercase placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
          <button
            type="submit"
            disabled={isSearchingQuote}
            className="px-4 py-2 bg-blue-600 hover:bg-blue-500 text-white rounded-xl text-sm font-semibold transition-all cursor-pointer disabled:opacity-50"
          >
            {isSearchingQuote ? 'Buscando...' : 'Consultar'}
          </button>
        </form>

        {searchError && (
          <p className="mt-3 text-xs text-rose-400">{searchError}</p>
        )}

        {searchedQuote && (
          <div className="mt-4 p-4 rounded-xl bg-slate-800/80 border border-slate-700 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
            <div>
              <div className="flex items-center gap-2">
                <span className="text-lg font-bold text-white">{searchedQuote.ticker}</span>
                <span className="text-xs text-slate-400">{searchedQuote.name}</span>
                {searchedQuote.fromFallback && (
                  <span className="text-[10px] bg-amber-500/20 text-amber-300 border border-amber-500/30 px-1.5 py-0.5 rounded">
                    Referência local
                  </span>
                )}
              </div>
              <div className="flex items-center gap-3 mt-1">
                <span className="text-xl font-extrabold text-white">
                  {formatCurrency(searchedQuote.price)}
                </span>
                <span
                  className={`text-xs font-semibold flex items-center ${
                    searchedQuote.changePercentage >= 0 ? 'text-emerald-400' : 'text-rose-400'
                  }`}
                >
                  {searchedQuote.changePercentage >= 0 ? '+' : ''}
                  {searchedQuote.changePercentage.toFixed(2)}%
                </span>
              </div>
            </div>

            <button
              onClick={() => handleOpenOrderModalWithTicker(searchedQuote.ticker, searchedQuote.price)}
              className="px-4 py-2 bg-emerald-600 hover:bg-emerald-500 text-white rounded-xl text-xs font-semibold shadow-md transition-all cursor-pointer"
            >
              Comprar / Vender este Ativo
            </button>
          </div>
        )}
      </div>

      {/* Tabela de Posições em Carteira */}
      <div className="bg-slate-900 rounded-2xl border border-slate-800 overflow-hidden shadow-md">
        <div className="p-4 border-b border-slate-800 flex items-center justify-between">
          <h2 className="text-base font-bold text-white flex items-center gap-2">
            <Briefcase className="w-5 h-5 text-emerald-400" />
            Posições em Custódia
          </h2>
          <span className="text-xs text-slate-400">
            {positions.length} ativo{positions.length !== 1 ? 's' : ''} em carteira
          </span>
        </div>

        {positions.length === 0 ? (
          <div className="p-12 text-center text-slate-500">
            Você ainda não possui posições em custódia. Registre sua primeira compra de ações ou FIIs.
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="border-b border-slate-800 bg-slate-950/40 text-slate-400 text-xs uppercase font-semibold">
                  <th className="py-3 px-4">Ativo</th>
                  <th className="py-3 px-4">Classe</th>
                  <th className="py-3 px-4 text-right">Qtd.</th>
                  <th className="py-3 px-4 text-right">Preço Médio</th>
                  <th className="py-3 px-4 text-right">Cotação Atual</th>
                  <th className="py-3 px-4 text-right">Custo Total</th>
                  <th className="py-3 px-4 text-right">Valor Atual</th>
                  <th className="py-3 px-4 text-right">Rentabilidade</th>
                  <th className="py-3 px-4 text-center">Ações</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800 text-sm">
                {positions.map((p) => (
                  <tr key={p.assetId} className="hover:bg-slate-850/50 transition-colors">
                    <td className="py-3.5 px-4 font-bold text-white">
                      <div>
                        <span>{p.ticker}</span>
                        <span className="block text-xs font-normal text-slate-400 truncate max-w-[150px]">
                          {p.name}
                        </span>
                      </div>
                    </td>
                    <td className="py-3.5 px-4">
                      <span className="text-xs font-semibold px-2 py-0.5 rounded bg-slate-800 text-slate-300 border border-slate-700">
                        {p.type}
                      </span>
                    </td>
                    <td className="py-3.5 px-4 text-right text-slate-200">{p.quantity}</td>
                    <td className="py-3.5 px-4 text-right text-slate-300 font-medium">{formatCurrency(p.averagePrice)}</td>
                    <td className="py-3.5 px-4 text-right text-white font-semibold">{formatCurrency(p.currentPrice)}</td>
                    <td className="py-3.5 px-4 text-right text-slate-400">{formatCurrency(p.totalCost)}</td>
                    <td className="py-3.5 px-4 text-right text-white font-bold">{formatCurrency(p.currentValue)}</td>
                    <td className="py-3.5 px-4 text-right whitespace-nowrap">
                      <span className={`font-bold ${p.profitLoss >= 0 ? 'text-emerald-400' : 'text-rose-400'}`}>
                        {p.profitLoss >= 0 ? '+' : ''}{formatCurrency(p.profitLoss)}
                      </span>
                      <span className={`block text-xs font-medium ${p.profitLossPercentage >= 0 ? 'text-emerald-400' : 'text-rose-400'}`}>
                        {p.profitLossPercentage >= 0 ? '+' : ''}{p.profitLossPercentage}%
                      </span>
                    </td>
                    <td className="py-3.5 px-4 text-center">
                      <button
                        onClick={() => handleOpenOrderModalWithTicker(p.ticker, p.currentPrice)}
                        className="text-xs bg-slate-800 hover:bg-slate-700 text-slate-200 px-2.5 py-1 rounded-lg border border-slate-700 transition-colors cursor-pointer"
                      >
                        Operar
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Tabela de Histórico de Ordens */}
      <div className="bg-slate-900 rounded-2xl border border-slate-800 overflow-hidden shadow-md">
        <div className="p-4 border-b border-slate-800">
          <h2 className="text-base font-bold text-white flex items-center gap-2">
            <Clock className="w-5 h-5 text-blue-400" />
            Histórico de Ordens
          </h2>
        </div>

        {orders.length === 0 ? (
          <div className="p-8 text-center text-slate-500">
            Nenhuma operação cadastrada no histórico.
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="border-b border-slate-800 bg-slate-950/40 text-slate-400 text-xs uppercase font-semibold">
                  <th className="py-3 px-4">Data</th>
                  <th className="py-3 px-4">Ativo</th>
                  <th className="py-3 px-4">Tipo</th>
                  <th className="py-3 px-4 text-right">Qtd.</th>
                  <th className="py-3 px-4 text-right">Preço Unit.</th>
                  <th className="py-3 px-4 text-right">Taxas</th>
                  <th className="py-3 px-4 text-right">Total</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800 text-sm">
                {orders.map((ord) => (
                  <tr key={ord.id} className="hover:bg-slate-850/50 transition-colors">
                    <td className="py-3 px-4 text-slate-400">
                      {new Date(ord.transactionDate).toLocaleDateString('pt-BR')}
                    </td>
                    <td className="py-3 px-4 font-bold text-white">{ord.ticker}</td>
                    <td className="py-3 px-4">
                      <span
                        className={`text-xs font-bold px-2 py-0.5 rounded ${
                          ord.type === 'BUY'
                            ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20'
                            : 'bg-rose-500/10 text-rose-400 border border-rose-500/20'
                        }`}
                      >
                        {ord.type === 'BUY' ? 'COMPRA' : 'VENDA'}
                      </span>
                    </td>
                    <td className="py-3 px-4 text-right text-slate-200">{ord.quantity}</td>
                    <td className="py-3 px-4 text-right text-slate-300">{formatCurrency(ord.unitPrice)}</td>
                    <td className="py-3 px-4 text-right text-slate-400">{formatCurrency(ord.fees)}</td>
                    <td className="py-3 px-4 text-right font-semibold text-white">{formatCurrency(ord.totalAmount)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Modal Nova Ordem */}
      {isModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 backdrop-blur-xs p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl max-w-md w-full p-6 shadow-2xl">
            <h2 className="text-xl font-bold text-white mb-4">Registrar Operação na B3</h2>

            <form onSubmit={handleCreateOrder} className="space-y-4">
              {/* Tipo COMPRA / VENDA */}
              <div>
                <label className="block text-xs font-semibold text-slate-400 uppercase mb-2">
                  Tipo de Ordem
                </label>
                <div className="grid grid-cols-2 gap-2">
                  <button
                    type="button"
                    onClick={() => setOrderType('BUY')}
                    className={`py-2 text-xs font-semibold rounded-lg border transition-all cursor-pointer ${
                      orderType === 'BUY'
                        ? 'bg-emerald-500/20 text-emerald-400 border-emerald-500/40'
                        : 'bg-slate-800 text-slate-400 border-slate-700'
                    }`}
                  >
                    Compra
                  </button>
                  <button
                    type="button"
                    onClick={() => setOrderType('SELL')}
                    className={`py-2 text-xs font-semibold rounded-lg border transition-all cursor-pointer ${
                      orderType === 'SELL'
                        ? 'bg-rose-500/20 text-rose-400 border-rose-500/40'
                        : 'bg-slate-800 text-slate-400 border-slate-700'
                    }`}
                  >
                    Venda
                  </button>
                </div>
              </div>

              {/* Ticker */}
              <div>
                <label className="block text-xs font-semibold text-slate-400 uppercase mb-1">
                  Ticker (Código do Ativo)
                </label>
                <input
                  type="text"
                  required
                  placeholder="Ex: PETR4, VALE3, MXRF11"
                  value={orderTicker}
                  onChange={(e) => setOrderTicker(e.target.value.toUpperCase())}
                  className="w-full px-3 py-2 bg-slate-800 border border-slate-700 rounded-lg text-sm text-white uppercase font-bold tracking-wider focus:outline-none focus:ring-1 focus:ring-purple-500"
                />
              </div>

              {/* Quantidade */}
              <div>
                <label className="block text-xs font-semibold text-slate-400 uppercase mb-1">
                  Quantidade de Cotas / Ações
                </label>
                <input
                  type="number"
                  step="0.0001"
                  required
                  placeholder="Ex: 100"
                  value={orderQuantity}
                  onChange={(e) => setOrderQuantity(e.target.value)}
                  className="w-full px-3 py-2 bg-slate-800 border border-slate-700 rounded-lg text-sm text-white focus:outline-none focus:ring-1 focus:ring-purple-500"
                />
              </div>

              {/* Preço Unitário */}
              <div>
                <label className="block text-xs font-semibold text-slate-400 uppercase mb-1">
                  Preço Unitário Pago (R$)
                </label>
                <input
                  type="number"
                  step="0.01"
                  required
                  placeholder="Ex: 35.00"
                  value={orderPrice}
                  onChange={(e) => setOrderPrice(e.target.value)}
                  className="w-full px-3 py-2 bg-slate-800 border border-slate-700 rounded-lg text-sm text-white focus:outline-none focus:ring-1 focus:ring-purple-500"
                />
              </div>

              {/* Taxas */}
              <div>
                <label className="block text-xs font-semibold text-slate-400 uppercase mb-1">
                  Taxas / Corretagem / B3 (R$)
                </label>
                <input
                  type="number"
                  step="0.01"
                  value={orderFees}
                  onChange={(e) => setOrderFees(e.target.value)}
                  className="w-full px-3 py-2 bg-slate-800 border border-slate-700 rounded-lg text-sm text-white focus:outline-none focus:ring-1 focus:ring-purple-500"
                />
              </div>

              {/* Data */}
              <div>
                <label className="block text-xs font-semibold text-slate-400 uppercase mb-1">
                  Data da Operação
                </label>
                <input
                  type="date"
                  required
                  value={orderDate}
                  onChange={(e) => setOrderDate(e.target.value)}
                  className="w-full px-3 py-2 bg-slate-800 border border-slate-700 rounded-lg text-sm text-white focus:outline-none focus:ring-1 focus:ring-purple-500"
                />
              </div>

              {/* Prévia do Total */}
              {orderQuantity && orderPrice && (
                <div className="p-3 rounded-lg bg-slate-850 border border-slate-800 text-xs text-slate-300 flex justify-between">
                  <span>Total Estimado da Operação:</span>
                  <span className="font-bold text-white">
                    {formatCurrency(
                      parseFloat(orderQuantity) * parseFloat(orderPrice) +
                        (orderType === 'BUY' ? parseFloat(orderFees || '0') : -parseFloat(orderFees || '0'))
                    )}
                  </span>
                </div>
              )}

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
                  className="px-5 py-2 text-sm font-semibold text-white bg-purple-600 hover:bg-purple-500 rounded-lg shadow-md transition-all cursor-pointer disabled:opacity-50"
                >
                  {isSubmitting ? 'Salvando...' : 'Confirmar Ordem'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
