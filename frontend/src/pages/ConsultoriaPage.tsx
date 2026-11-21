
import React, { useState } from 'react';
import {
  
  Target,
  CalendarDays,
    MessageCircle,
  ArrowRight,
  User,
  GraduationCap,
  BriefcaseBusiness,
  ShieldCheck,
  Send,
} from 'lucide-react';

export const ConsultoriaPage: React.FC = () => {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [phone, setPhone] = useState('');
  const [message, setMessage] = useState('');

   
  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    const subject = encodeURIComponent(
      'Solicitação de consultoria financeira'
    );

    const body = encodeURIComponent(
      `Nome: ${name}\n` +
      `E-mail: ${email}\n` +
      `Telefone: ${phone}\n` +
      `Mensagem:\n${message}`
    );

    window.location.href = `mailto:?subject=${subject}&body=${body}`;
  };

  return (
    <div className="space-y-12 pb-12">
      {/* Apresentação */}
      <section className="relative overflow-hidden rounded-3xl border border-slate-800 bg-gradient-to-br from-slate-900 via-slate-900 to-blue-950/50 p-7 sm:p-10">
        <div className="absolute -right-20 -top-20 w-72 h-72 rounded-full bg-blue-500/10 blur-3xl" />

        <div className="relative max-w-3xl">
          <span className="inline-flex items-center gap-2 rounded-full border border-emerald-500/30 bg-emerald-500/10 px-3 py-1 text-xs font-semibold text-emerald-400">
            <ShieldCheck className="w-4 h-4" />
            Consultoria financeira
          </span>

          <h1 className="mt-5 text-3xl sm:text-5xl font-bold tracking-tight text-white">
            Suas decisões financeiras com mais
            <span className="text-emerald-400"> estratégia e clareza.</span>
          </h1>

          <p className="mt-5 text-slate-300 leading-relaxed text-base sm:text-lg">
            Planejamento financeiro, organização patrimonial e orientação
            sobre investimentos para ajudar você a tomar decisões mais
            conscientes e alcançar seus objetivos.
          </p>

          <button
            onClick={() =>
              document.getElementById('solicitar-consultoria')?.scrollIntoView({
                behavior: 'smooth',
              })
            }
            className="mt-8 inline-flex items-center gap-2 rounded-xl bg-gradient-to-r from-blue-600 to-emerald-600 px-5 py-3 font-semibold text-white shadow-lg shadow-blue-500/20 transition hover:from-blue-500 hover:to-emerald-500"
          >
            Solicitar consultoria
            <ArrowRight className="w-4 h-4" />
          </button>
        </div>
      </section>

      {/* Perfil profissional */}
      <section className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2 rounded-2xl border border-slate-800 bg-slate-900/70 p-6 sm:p-8">
          <div className="flex items-center gap-3 mb-5">
            <div className="rounded-xl bg-blue-500/10 p-3">
              <User className="w-6 h-6 text-blue-400" />
            </div>
            <div>
              <h2 className="text-xl font-bold text-white">
                Sobre a consultoria
              </h2>
              <p className="text-sm text-slate-400">
                Experiência e conhecimento a serviço das suas finanças
              </p>
            </div>
          </div>

          <p className="text-slate-300 leading-relaxed">
             Me chamo Matheus Gasco, sou formado em Ciências Econômicas e
            pós-graduado em Mercado Financeiro. Criei esta ferramenta para
            ajudar você a cuidar melhor da sua vida financeira, desde a
            organização das contas e dos boletos até o planejamento da sua
            aposentadoria (afinal, nós dois queremos chegar lá o mais rápido
            possível, rs!).
          </p>

          <p className="mt-4 text-slate-300 leading-relaxed">
             Nosso primeiro contato é gratuito! Vamos conversar, entender
            suas necessidades e descobrir juntos como posso ajudar você
            a alcançar seus objetivos financeiros.
          </p>
        </div>

        <div className="rounded-2xl border border-slate-800 bg-slate-900/70 p-6 sm:p-8">
          <GraduationCap className="w-8 h-8 text-emerald-400 mb-4" />
          <h3 className="text-lg font-bold text-white mb-4">
            Formação e experiência
          </h3>

          <div className="space-y-4">
            <div>
              <p className="text-xs uppercase tracking-wider text-slate-500">
                Formação
              </p>
              <p className="mt-1 text-sm text-slate-200">
                Ciências Econômicas
              </p>
            </div>

            <div>
              <p className="text-xs uppercase tracking-wider text-slate-500">
                Especialização
              </p>
              <p className="mt-1 text-sm text-slate-200">
                Pós-graduação em Mercado Financeiro
              </p>
            </div>

            <div>
              <p className="text-xs uppercase tracking-wider text-slate-500">
                Experiência profissional
              </p>
              <p className="mt-1 text-sm text-slate-200">
                Santander e Itaú
              </p>
            </div>

            <div className="flex items-center gap-2 text-xs text-emerald-400">
              <BriefcaseBusiness className="w-4 h-4" />
              Experiência no mercado financeiro
            </div>
          </div>
        </div>
      </section>

      
      {/* Etapas */}
      <section className="rounded-2xl border border-slate-800 bg-slate-900/70 p-6 sm:p-8">
        <h2 className="text-2xl font-bold text-white">
          Como funciona
        </h2>

        <div className="mt-7 grid grid-cols-1 sm:grid-cols-3 gap-6">
          <div>
            <MessageCircle className="w-7 h-7 text-blue-400 mb-3" />
            <h3 className="font-semibold text-white">1. Primeiro contato</h3>
            <p className="mt-2 text-sm text-slate-400">
              Você apresenta suas necessidades e objetivos financeiros.
            </p>
          </div>

          <div>
            <CalendarDays className="w-7 h-7 text-blue-400 mb-3" />
            <h3 className="font-semibold text-white">2. Diagnóstico</h3>
            <p className="mt-2 text-sm text-slate-400">
              Entendemos sua situação e identificamos as oportunidades.
            </p>
          </div>

          <div>
            <Target className="w-7 h-7 text-emerald-400 mb-3" />
            <h3 className="font-semibold text-white">3. Plano de ação</h3>
            <p className="mt-2 text-sm text-slate-400">
              Você recebe orientações e estratégias alinhadas aos seus objetivos.
            </p>
          </div>
        </div>
      </section>

      {/* Solicitação */}
      <section
        id="solicitar-consultoria"
        className="rounded-2xl border border-slate-800 bg-slate-900/70 p-6 sm:p-8"
      >
        <div className="mb-6">
          <h2 className="text-2xl font-bold text-white">
            Solicite uma consultoria
          </h2>
          <p className="mt-2 text-sm text-slate-400">
            Preencha seus dados e descreva como podemos ajudar.
          </p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-5">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-5">
            <div>
              <label className="mb-2 block text-sm text-slate-300">
                Nome completo
              </label>
              <input
                required
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder="Seu nome"
                className="w-full rounded-xl border border-slate-700 bg-slate-950 px-4 py-3 text-sm text-white outline-none focus:border-blue-500"
              />
            </div>

            <div>
              <label className="mb-2 block text-sm text-slate-300">
                E-mail
              </label>
              <input
                required
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="seu@email.com"
                className="w-full rounded-xl border border-slate-700 bg-slate-950 px-4 py-3 text-sm text-white outline-none focus:border-blue-500"
              />
            </div>

            <div>
              <label className="mb-2 block text-sm text-slate-300">
                Telefone / WhatsApp
              </label>
              <input
                value={phone}
                onChange={(e) => setPhone(e.target.value)}
                placeholder="(00) 00000-0000"
                className="w-full rounded-xl border border-slate-700 bg-slate-950 px-4 py-3 text-sm text-white outline-none focus:border-blue-500"
              />
            </div>
          </div>

     
          <div>
            <label className="mb-2 block text-sm text-slate-300">
              Conte um pouco sobre seus objetivos
            </label>
            <textarea
              value={message}
              onChange={(e) => setMessage(e.target.value)}
              rows={4}
              placeholder="Descreva o que você gostaria de organizar ou alcançar..."
              className="w-full resize-y rounded-xl border border-slate-700 bg-slate-950 px-4 py-3 text-sm text-white outline-none focus:border-blue-500"
            />
          </div>

          <button
            type="submit"
            className="inline-flex items-center justify-center gap-2 rounded-xl bg-gradient-to-r from-blue-600 to-emerald-600 px-6 py-3 font-semibold text-white transition hover:from-blue-500 hover:to-emerald-500"
          >
            <Send className="w-4 h-4" />
            Preparar solicitação por e-mail
          </button>

          <p className="text-xs text-slate-500">
            O envio abrirá o aplicativo de e-mail configurado no seu dispositivo.
            O endereço de destino será configurado posteriormente.
          </p>
        </form>
      </section>
    </div>
  );
};

export default ConsultoriaPage;
