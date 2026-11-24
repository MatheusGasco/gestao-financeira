
import React, { useState } from 'react';
import { useAuth } from '../../contexts/AuthContext';
import { api } from '../../services/api';
import {
  LayoutDashboard,
  Receipt,
  TrendingUp,
  Wallet,
  LogOut,
  User as UserIcon,
  BriefcaseBusiness,
  ChevronDown,
  Sun,
  Moon,
  Headphones,
  X,
  Send,
  CheckCircle,
  AlertCircle,
} from 'lucide-react';

interface NavbarProps {
  currentTab: string;
  onTabChange: (tab: string) => void;
  theme: 'dark' | 'light';
  onToggleTheme: () => void;
}

export const Navbar: React.FC<NavbarProps> = ({
  currentTab,
  onTabChange,
  theme,
  onToggleTheme,
}) => {
  const { user, logout } = useAuth();

  const [menuOpen, setMenuOpen] = useState(false);
  const [supportOpen, setSupportOpen] = useState(false);
  const [supportType, setSupportType] = useState<'SUGESTAO' | 'RECLAMACAO'>('SUGESTAO');
  const [subject, setSubject] = useState('');
  const [message, setMessage] = useState('');
  const [sending, setSending] = useState(false);
  const [success, setSuccess] = useState('');
  const [error, setError] = useState('');

  const navItems = [
    { id: 'dashboard', label: 'Dashboard', icon: LayoutDashboard },
    { id: 'transactions', label: 'Transações', icon: Receipt },
    { id: 'investments', label: 'Investimentos', icon: TrendingUp },
    { id: 'accounts', label: 'Contas', icon: Wallet },
    { id: 'consultoria', label: 'Consultoria', icon: BriefcaseBusiness },
  ];

  const openSupport = () => {
    setMenuOpen(false);
    setSupportOpen(true);
    setSuccess('');
    setError('');
  };

  const closeSupport = () => {
    if (sending) return;

    setSupportOpen(false);
    setSubject('');
    setMessage('');
    setSupportType('SUGESTAO');
    setSuccess('');
    setError('');
  };

  const handleSupportSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    setSending(true);
    setSuccess('');
    setError('');

    try {
      const response = await api.post('/support', {
        type: supportType,
        subject: subject.trim(),
        message: message.trim(),
      });

      setSuccess(
        response.data?.message ||
        'Mensagem enviada com sucesso! Obrigado pelo contato.'
      );

      setSubject('');
      setMessage('');
    } catch (err: any) {
      setError(
        err.response?.data?.message ||
        'Não foi possível enviar sua mensagem. Tente novamente.'
      );
    } finally {
      setSending(false);
    }
  };

  return (
    <>
      <header className="sticky top-0 z-40 w-full border-b border-slate-800 bg-slate-900/90 backdrop-blur-md">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between h-16">
            <div className="flex items-center gap-8">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-emerald-500 to-blue-600 flex items-center justify-center shadow-lg shadow-emerald-500/20">
                  <TrendingUp className="w-5 h-5 text-white" />
                </div>

                <div>
                  <span className="text-lg font-bold bg-gradient-to-r from-white via-slate-200 to-emerald-400 bg-clip-text text-transparent">
                    FinanTrack
                  </span>

                  <span className="hidden sm:inline-block ml-2 text-xs uppercase tracking-wider font-semibold text-emerald-400 bg-emerald-950/60 px-2 py-0.5 rounded border border-emerald-800/40">
                    B3 & Finanças
                  </span>
                </div>
              </div>

              <nav className="hidden md:flex space-x-1">
                {navItems.map((item) => {
                  const Icon = item.icon;
                  const active = currentTab === item.id;

                  return (
                    <button
                      key={item.id}
                      onClick={() => onTabChange(item.id)}
                      className={`flex items-center gap-2 px-3 py-2 rounded-lg text-sm font-medium transition-all ${
                        active
                          ? 'bg-blue-600/20 text-blue-400 border border-blue-500/30 shadow-sm'
                          : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/60'
                      }`}
                    >
                      <Icon className="w-4 h-4" />
                      {item.label}
                    </button>
                  );
                })}
              </nav>
            </div>

            <div className="flex items-center gap-3">
              <div className="relative">
                <button
                  type="button"
                  onClick={() => setMenuOpen(!menuOpen)}
                  aria-expanded={menuOpen}
                  className="flex items-center gap-2 bg-slate-800/60 hover:bg-slate-800 px-3 py-2 rounded-lg border border-slate-700/60 transition-colors"
                >
                  <UserIcon className="w-4 h-4 text-emerald-400" />

                  <span className="text-sm font-medium text-slate-200 max-w-[150px] sm:max-w-none truncate">
                    {user?.name?.trim().split(/\s+/)[0]}
                  </span>

                  <ChevronDown
                    className={`w-4 h-4 text-slate-400 transition-transform ${
                      menuOpen ? 'rotate-180' : ''
                    }`}
                  />
                </button>

                {menuOpen && (
                  <div className="absolute right-0 mt-2 w-64 bg-slate-800 border border-slate-700 rounded-xl shadow-2xl overflow-hidden z-50">
                    <div className="px-4 py-3 border-b border-slate-700">
                      <p className="text-sm font-semibold text-slate-100 truncate">
                        {user?.name}
                      </p>
                      <p className="text-xs text-slate-400 truncate">
                        {user?.email}
                      </p>
                    </div>

                    <button
                      type="button"
                      onClick={() => {
                        onToggleTheme();
                        setMenuOpen(false);
                      }}
                      className="w-full flex items-center gap-3 px-4 py-3 text-sm text-slate-200 hover:bg-slate-700/60 transition-colors"
                    >
                      {theme === 'dark' ? (
                        <Sun className="w-4 h-4 text-amber-400" />
                      ) : (
                        <Moon className="w-4 h-4 text-blue-400" />
                      )}

                      <span className="flex-1 text-left">
                        Tema da aplicação
                      </span>

                      <span className="text-xs text-slate-400">
                        {theme === 'dark' ? 'Escuro' : 'Claro'}
                      </span>
                    </button>

                    <button
                      type="button"
                      onClick={openSupport}
                      className="w-full flex items-center gap-3 px-4 py-3 text-sm text-slate-200 hover:bg-slate-700/60 transition-colors"
                    >
                      <Headphones className="w-4 h-4 text-emerald-400" />
                      Suporte
                    </button>
                  </div>
                )}
              </div>

              <button
                onClick={logout}
                title="Sair da conta"
                className="p-2 text-slate-400 hover:text-rose-400 hover:bg-rose-500/10 rounded-lg transition-colors"
              >
                <LogOut className="w-5 h-5" />
              </button>
            </div>
          </div>

          <div className="md:hidden flex justify-around py-2 border-t border-slate-800/60">
            {navItems.map((item) => {
              const Icon = item.icon;
              const active = currentTab === item.id;

              return (
                <button
                  key={item.id}
                  onClick={() => onTabChange(item.id)}
                  className={`flex flex-col items-center gap-1 py-1 px-2 rounded-lg text-xs font-medium ${
                    active ? 'text-blue-400' : 'text-slate-400'
                  }`}
                >
                  <Icon className="w-4 h-4" />
                  {item.label}
                </button>
              );
            })}
          </div>
        </div>
      </header>

      {supportOpen && (
        <div
          className="fixed inset-0 z-[100] flex items-center justify-center bg-black/70 backdrop-blur-sm p-4"
          onMouseDown={(event) => {
            if (event.target === event.currentTarget) closeSupport();
          }}
        >
          <div className="w-full max-w-lg bg-slate-900 border border-slate-700 rounded-2xl shadow-2xl overflow-hidden">
            <div className="flex items-center justify-between px-6 py-5 border-b border-slate-800">
              <div>
                <h2 className="text-lg font-bold text-slate-100">
                  Suporte FinanTrack
                </h2>
                <p className="text-sm text-slate-400 mt-1">
                  Envie uma sugestão ou reclamação.
                </p>
              </div>

              <button
                type="button"
                onClick={closeSupport}
                disabled={sending}
                className="p-2 rounded-lg text-slate-400 hover:text-slate-100 hover:bg-slate-800 transition-colors disabled:opacity-50"
                aria-label="Fechar suporte"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <form onSubmit={handleSupportSubmit} className="p-6 space-y-5">
              <div>
                <label className="block text-sm font-medium text-slate-300 mb-2">
                  Tipo de contato
                </label>

                <div className="grid grid-cols-2 gap-3">
                  <button
                    type="button"
                    onClick={() => setSupportType('SUGESTAO')}
                    className={`py-2.5 px-3 rounded-lg border text-sm font-medium transition-colors ${
                      supportType === 'SUGESTAO'
                        ? 'bg-emerald-600/20 border-emerald-500 text-emerald-400'
                        : 'bg-slate-800 border-slate-700 text-slate-300 hover:bg-slate-700'
                    }`}
                  >
                    Sugestão
                  </button>

                  <button
                    type="button"
                    onClick={() => setSupportType('RECLAMACAO')}
                    className={`py-2.5 px-3 rounded-lg border text-sm font-medium transition-colors ${
                      supportType === 'RECLAMACAO'
                        ? 'bg-rose-600/20 border-rose-500 text-rose-400'
                        : 'bg-slate-800 border-slate-700 text-slate-300 hover:bg-slate-700'
                    }`}
                  >
                    Reclamação
                  </button>
                </div>
              </div>

              <div>
                <label
                  htmlFor="support-subject"
                  className="block text-sm font-medium text-slate-300 mb-2"
                >
                  Assunto
                </label>

                <input
                  id="support-subject"
                  type="text"
                  value={subject}
                  onChange={(event) => setSubject(event.target.value)}
                  maxLength={120}
                  required
                  placeholder="Informe o assunto"
                  className="w-full px-4 py-3 rounded-lg bg-slate-800 border border-slate-700 text-slate-100 placeholder:text-slate-500 focus:outline-none focus:ring-2 focus:ring-emerald-500"
                />
              </div>

              <div>
                <label
                  htmlFor="support-message"
                  className="block text-sm font-medium text-slate-300 mb-2"
                >
                  Mensagem
                </label>

                <textarea
                  id="support-message"
                  value={message}
                  onChange={(event) => setMessage(event.target.value)}
                  maxLength={3000}
                  required
                  rows={5}
                  placeholder="Descreva sua sugestão ou reclamação..."
                  className="w-full px-4 py-3 rounded-lg bg-slate-800 border border-slate-700 text-slate-100 placeholder:text-slate-500 focus:outline-none focus:ring-2 focus:ring-emerald-500 resize-y"
                />

                <p className="text-xs text-slate-500 text-right mt-1">
                  {message.length}/3000
                </p>
              </div>

              {success && (
                <div className="flex items-start gap-2 p-3 rounded-lg bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 text-sm">
                  <CheckCircle className="w-5 h-5 flex-shrink-0" />
                  <span>{success}</span>
                </div>
              )}

              {error && (
                <div className="flex items-start gap-2 p-3 rounded-lg bg-rose-500/10 border border-rose-500/20 text-rose-400 text-sm">
                  <AlertCircle className="w-5 h-5 flex-shrink-0" />
                  <span>{error}</span>
                </div>
              )}

              <button
                type="submit"
                disabled={sending || !subject.trim() || !message.trim()}
                className="w-full flex items-center justify-center gap-2 py-3 px-4 rounded-xl text-sm font-semibold text-white bg-gradient-to-r from-emerald-600 to-blue-600 hover:from-emerald-500 hover:to-blue-500 disabled:opacity-50 disabled:cursor-not-allowed transition-all"
              >
                <Send className="w-4 h-4" />
                {sending ? 'Enviando...' : 'Enviar mensagem'}
              </button>

              <p className="text-xs text-slate-500 text-center">
                Sua mensagem será enviada diretamente à equipe do FinanTrack.
              </p>
            </form>
          </div>
        </div>
      )}
    </>
  );
};
