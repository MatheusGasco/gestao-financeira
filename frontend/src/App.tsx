
import React, { useState, useEffect } from 'react';
import { AuthProvider, useAuth } from './contexts/AuthContext';
import { authApi } from './services/api';

import { Navbar } from './components/layout/Navbar';
import { LoginPage } from './pages/LoginPage';
import { RegisterPage } from './pages/RegisterPage';
import { DashboardPage } from './pages/DashboardPage';
import { TransactionsPage } from './pages/TransactionsPage';
import { InvestmentsPage } from './pages/InvestmentsPage';
import { AccountsPage } from './pages/AccountsPage';
import { ConsultoriaPage } from './pages/ConsultoriaPage';
import { PasswordResetPage } from './pages/PasswordResetPage';

import {
  TrendingUp,
  CheckCircle,
  AlertCircle,
  Mail,
  ArrowRight
} from 'lucide-react';

const AppContent: React.FC = () => {
  const { isAuthenticated, isLoading } = useAuth();
  
  const [theme, setTheme] = useState<'dark' | 'light'>(() => {
  return (localStorage.getItem('finantrack-theme') as 'dark' | 'light') || 'dark';
});

useEffect(() => {
  document.documentElement.setAttribute('data-theme', theme);
  localStorage.setItem('finantrack-theme', theme);
}, [theme]);

const handleToggleTheme = () => {
  setTheme((current) => current === 'dark' ? 'light' : 'dark');
};

  const [authView, setAuthView] = useState<
    'login' | 'register' | 'password-reset'
  >('login');

  const [resetToken] = useState<string | null>(() => {
    return new URLSearchParams(window.location.search).get('resetToken');
  });

  const [currentTab, setCurrentTab] = useState<string>('dashboard');

  const [verificationToken] = useState<string | null>(() => {
    return new URLSearchParams(window.location.search).get('token');
  });

  const [verificationLoading, setVerificationLoading] = useState(false);
  const [verificationSuccess, setVerificationSuccess] = useState(false);
  const [verificationError, setVerificationError] = useState('');

  const handleVerifyEmail = async () => {
    if (!verificationToken) {
      setVerificationError('Token de confirmação não encontrado.');
      return;
    }

    setVerificationLoading(true);
    setVerificationError('');

    try {
      await authApi.verifyEmail(verificationToken);

      setVerificationSuccess(true);

      window.history.replaceState(
        {},
        document.title,
        window.location.pathname
      );
    } catch (err: any) {
      setVerificationError(
        err.response?.data?.message ||
        'Não foi possível confirmar seu e-mail. Tente novamente.'
      );
    } finally {
      setVerificationLoading(false);
    }
  };

  const handleBackToLogin = () => {
    setAuthView('login');

    window.history.replaceState(
      {},
      document.title,
      window.location.pathname
    );

    window.location.reload();
  };

  if (verificationToken) {
    return (
      <div className="min-h-screen bg-slate-950 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
        <div className="sm:mx-auto sm:w-full sm:max-w-md">
          <div className="flex justify-center">
            <div className="w-14 h-14 rounded-2xl bg-gradient-to-tr from-emerald-500 to-blue-600 flex items-center justify-center shadow-xl shadow-emerald-500/20">
              <TrendingUp className="w-8 h-8 text-white" />
            </div>
          </div>

          <h2 className="mt-6 text-center text-3xl font-extrabold text-white">
            Confirmação de e-mail
          </h2>

          <p className="mt-2 text-center text-sm text-slate-400">
            Confirme seu endereço de e-mail para liberar o acesso ao FinanTrack.
          </p>
        </div>

        <div className="mt-8 sm:mx-auto sm:w-full sm:max-w-md">
          <div className="bg-slate-900 py-8 px-6 shadow-2xl border border-slate-800 sm:rounded-2xl sm:px-10 text-center">
            {verificationSuccess ? (
              <>
                <CheckCircle className="w-16 h-16 text-emerald-400 mx-auto mb-5" />

                <h3 className="text-xl font-semibold text-white mb-3">
                  E-mail confirmado!
                </h3>

                <p className="text-sm text-slate-400 mb-6">
                  Sua conta foi confirmada com sucesso. Agora você já pode fazer login no FinanTrack.
                </p>

                <button
                  type="button"
                  onClick={handleBackToLogin}
                  className="w-full flex justify-center items-center gap-2 py-3 px-4 rounded-xl text-sm font-semibold text-white bg-gradient-to-r from-emerald-600 to-blue-600 hover:from-emerald-500 hover:to-blue-500 transition-all cursor-pointer"
                >
                  Ir para o login
                  <ArrowRight className="w-4 h-4" />
                </button>
              </>
            ) : (
              <>
                <Mail className="w-16 h-16 text-blue-400 mx-auto mb-5" />

                <p className="text-sm text-slate-300 mb-6">
                  Clique no botão abaixo para confirmar seu endereço de e-mail.
                </p>

                {verificationError && (
                  <div className="mb-5 bg-rose-500/10 border border-rose-500/20 rounded-xl p-3 flex items-center gap-3 text-rose-400 text-sm text-left">
                    <AlertCircle className="w-5 h-5 flex-shrink-0" />
                    <span>{verificationError}</span>
                  </div>
                )}

                <button
                  type="button"
                  onClick={handleVerifyEmail}
                  disabled={verificationLoading}
                  className="w-full flex justify-center items-center gap-2 py-3 px-4 rounded-xl text-sm font-semibold text-white bg-gradient-to-r from-emerald-600 to-blue-600 hover:from-emerald-500 hover:to-blue-500 disabled:opacity-50 transition-all cursor-pointer"
                >
                  {verificationLoading ? 'Confirmando...' : 'Confirmar e-mail'}
                  <CheckCircle className="w-4 h-4" />
                </button>

                <button
                  type="button"
                  onClick={handleBackToLogin}
                  className="mt-5 text-sm font-medium text-blue-400 hover:text-blue-300 cursor-pointer"
                >
                  Voltar para o login
                </button>
              </>
            )}
          </div>
        </div>
      </div>
    );
  }

  if (resetToken) {
    return (
      <PasswordResetPage
        token={resetToken}
        onBackToLogin={handleBackToLogin}
      />
    );
  }

  if (isLoading) {
    return (
      <div className="min-h-screen bg-slate-950 flex items-center justify-center">
        <div className="flex flex-col items-center gap-3">
          <div className="w-10 h-10 border-4 border-blue-500 border-t-transparent rounded-full animate-spin"></div>
          <p className="text-slate-400 text-sm">
            Carregando FinanTrack...
          </p>
        </div>
      </div>
    );
  }

  if (!isAuthenticated) {
    if (authView === 'password-reset') {
      return (
        <PasswordResetPage
          token={null}
          onBackToLogin={() => setAuthView('login')}
        />
      );
    }

    return authView === 'login' ? (
      <LoginPage
        onSwitchToRegister={() => setAuthView('register')}
        onForgotPassword={() => setAuthView('password-reset')}
      />
    ) : (
      <RegisterPage
        onSwitchToLogin={() => setAuthView('login')}
      />
    );
  }

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col font-sans">
      <Navbar
  currentTab={currentTab}
  onTabChange={setCurrentTab}
  theme={theme}
  onToggleTheme={handleToggleTheme}
/>

      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 pt-8">
        {currentTab === 'dashboard' && <DashboardPage />}

        {currentTab === 'transactions' && <TransactionsPage />}

        {currentTab === 'investments' && <InvestmentsPage />}

        {currentTab === 'accounts' && <AccountsPage />}

        {currentTab === 'consultoria' && <ConsultoriaPage />}
      </main>
    </div>
  );
};

export const App: React.FC = () => {
  return (
    <AuthProvider>
      <AppContent />
    </AuthProvider>
  );
};

export default App;
