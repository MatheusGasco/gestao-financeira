
import React, { useState, useEffect, useRef, useCallback } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { authApi } from '../services/api';
import {
  TrendingUp,
  Lock,
  Mail,
  ArrowRight,
  AlertCircle,
  Eye,
  EyeOff
} from 'lucide-react';

interface LoginPageProps {
  onSwitchToRegister: () => void;
  onForgotPassword: () => void;
}

interface GoogleCredentialResponse {
  credential: string;
}

declare global {
  interface Window {
    google?: {
      accounts: {
        id: {
          initialize: (config: {
            client_id: string;
            callback: (response: GoogleCredentialResponse) => void;
          }) => void;
          renderButton: (
            element: HTMLElement,
            options: {
              theme: string;
              size: string;
              text: string;
              shape: string;
              width: number;
            }
          ) => void;
        };
      };
    };
  }
}

export const LoginPage: React.FC<LoginPageProps> = ({
  onSwitchToRegister,
  onForgotPassword
}) => {
  const { login } = useAuth();

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [isGoogleLoading, setIsGoogleLoading] = useState(false);

  const googleButtonRef = useRef<HTMLDivElement>(null);

  const googleClientId = import.meta.env.VITE_GOOGLE_CLIENT_ID as string | undefined;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setIsLoading(true);

    try {
      const response = await authApi.login(email, password);
      login(response.token, response.user);
    } catch (err: any) {
      setError(
        err.response?.data?.message ||
        'Falha ao autenticar. Verifique suas credenciais.'
      );
    } finally {
      setIsLoading(false);
    }
  };

  const handleGoogleCredential = useCallback(async (credential: string) => {
    setError('');
    setIsGoogleLoading(true);

    try {
      const response = await authApi.googleLogin(credential);
      login(response.token, response.user);
    } catch (err: any) {
      setError(
        err.response?.data?.message ||
        'Não foi possível entrar com o Google. Tente novamente.'
      );
    } finally {
      setIsGoogleLoading(false);
    }
  }, [login]);

  useEffect(() => {
    if (!googleClientId) {
      setError('Login com Google não configurado. Verifique o arquivo .env.local.');
      return;
    }

    let attempts = 0;

    const interval = window.setInterval(() => {
      attempts++;

      if (window.google?.accounts?.id && googleButtonRef.current) {
        window.clearInterval(interval);

        window.google.accounts.id.initialize({
          client_id: googleClientId,
          callback: (response) => {
            if (!response.credential) {
              setError('O Google não retornou uma credencial válida.');
              return;
            }

            void handleGoogleCredential(response.credential);
          }
        });

        window.google.accounts.id.renderButton(
          googleButtonRef.current,
          {
            theme: 'outline',
            size: 'large',
            text: 'continue_with',
            shape: 'pill',
            width: 320
          }
        );
      }

      if (attempts >= 100) {
        window.clearInterval(interval);
        setError('Não foi possível carregar o login do Google. Verifique sua conexão.');
      }
    }, 100);

    return () => {
      window.clearInterval(interval);
    };
  }, [googleClientId, handleGoogleCredential]);

  return (
    <div className="min-h-screen bg-slate-950 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
      <div className="sm:mx-auto sm:w-full sm:max-w-md">
        <div className="flex justify-center">
          <div className="w-14 h-14 rounded-2xl bg-gradient-to-tr from-emerald-500 to-blue-600 flex items-center justify-center shadow-xl shadow-emerald-500/20">
            <TrendingUp className="w-8 h-8 text-white" />
          </div>
        </div>

        <h2 className="mt-6 text-center text-3xl font-extrabold text-white">
          Acesse sua Carteira
        </h2>

        <p className="mt-2 text-center text-sm text-slate-400">
          Gestão de finanças pessoais e investimentos da B3
        </p>
      </div>

      <div className="mt-8 sm:mx-auto sm:w-full sm:max-w-md">
        <div className="bg-slate-900 py-8 px-4 shadow-2xl border border-slate-800 sm:rounded-2xl sm:px-10">

          {error && (
            <div className="mb-4 bg-rose-500/10 border border-rose-500/20 rounded-xl p-3 flex items-center gap-3 text-rose-400 text-sm">
              <AlertCircle className="w-5 h-5 flex-shrink-0" />
              <span>{error}</span>
            </div>
          )}

          <form className="space-y-6" onSubmit={handleSubmit}>
            <div>
              <label className="block text-sm font-medium text-slate-300">
                E-mail
              </label>

              <div className="mt-1 relative rounded-md shadow-sm">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <Mail className="h-5 w-5 text-slate-500" />
                </div>

                <input
                  type="email"
                  required
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="seu@email.com"
                  className="block w-full pl-10 pr-3 py-2.5 bg-slate-800/80 border border-slate-700 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent text-sm"
                />
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-slate-300">
                Senha
              </label>

              <div className="mt-1 relative rounded-md shadow-sm">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <Lock className="h-5 w-5 text-slate-500" />
                </div>

                <input
                  type={showPassword ? 'text' : 'password'}
                  required
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="••••••••"
                  className="block w-full pl-10 pr-10 py-2.5 bg-slate-800/80 border border-slate-700 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent text-sm"
                />

                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute inset-y-0 right-0 pr-3 flex items-center text-slate-400 hover:text-white cursor-pointer"
                  aria-label={showPassword ? 'Ocultar senha' : 'Mostrar senha'}
                >
                  {showPassword
                    ? <EyeOff className="h-5 w-5" />
                    : <Eye className="h-5 w-5" />}
                </button>
              </div>
            </div>

            <div className="flex justify-end -mt-3">
              <button
                type="button"
                onClick={onForgotPassword}
                className="text-sm font-medium text-blue-400 hover:text-blue-300 cursor-pointer"
              >
                Esqueci minha senha
              </button>
            </div>

            <div>
              <button
                type="submit"
                disabled={isLoading || isGoogleLoading}
                className="w-full flex justify-center items-center gap-2 py-3 px-4 border border-transparent rounded-xl shadow-lg shadow-blue-500/20 text-sm font-semibold text-white bg-gradient-to-r from-blue-600 to-emerald-600 hover:from-blue-500 hover:to-emerald-500 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 transition-all cursor-pointer"
              >
                {isLoading ? 'Entrando...' : 'Entrar na Plataforma'}
                <ArrowRight className="w-4 h-4" />
              </button>
            </div>
          </form>

          <div className="relative my-6">
            <div className="absolute inset-0 flex items-center">
              <div className="w-full border-t border-slate-700" />
            </div>

            <div className="relative flex justify-center text-sm">
              <span className="px-3 bg-slate-900 text-slate-500">
                ou continue com
              </span>
            </div>
          </div>

          <div className="flex justify-center">
            <div
              ref={googleButtonRef}
              className={isGoogleLoading ? 'pointer-events-none opacity-50' : ''}
            />
          </div>

          {isGoogleLoading && (
            <p className="mt-3 text-center text-sm text-slate-400">
              Entrando com Google...
            </p>
          )}

          <div className="mt-6 text-center">
            <button
              type="button"
              onClick={onSwitchToRegister}
              className="text-sm font-medium text-blue-400 hover:text-blue-300 cursor-pointer"
            >
              Não tem uma conta? Crie gratuitamente
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
