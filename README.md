# 💰 FinanTrack - Gestão Financeira Pessoal & Carteira B3

Uma plataforma completa, moderna e integrada de **Gestão Financeira Pessoal + Acompanhamento de Investimentos da B3**, permitindo consolidar fluxo de caixa (receitas, despesas, contas) e carteira de ações/FIIs/ETFs com cotações em tempo real.

![Dashboard Preview](https://img.shields.io/badge/Status-Conclu%C3%ADdo-emerald?style=for-the-badge)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.3-brightgreen?style=for-the-badge&logo=springboot)
![Java](https://img.shields.io/badge/Java-21%20LTS-orange?style=for-the-badge&logo=openjdk)
![React](https://img.shields.io/badge/React-18%2B-blue?style=for-the-badge&logo=react)
![TypeScript](https://img.shields.io/badge/TypeScript-5%2B-blue?style=for-the-badge&logo=typescript)
![Tailwind CSS](https://img.shields.io/badge/Tailwind%20CSS-v4-38bdf8?style=for-the-badge&logo=tailwindcss)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?style=for-the-badge&logo=postgresql)

---

## 🎯 Principais Funcionalidades

### 1. Autenticação & Segurança
- Cadastro e login seguro com criptografia **BCrypt** e tokens **JWT (Stateless)**.
- Seed automático de **11 categorias essenciais** (Salário, Rendimentos, Alimentação, Moradia, Transporte, etc.) no primeiro acesso.

### 2. Gestão Financeira & Múltiplas Contas
- Gerenciamento de contas bancárias (Corrente, Poupança, Investimentos, Dinheiro Físico).
- Lançamento de receitas, despesas e transferências entre contas.
- **Recálculo Atômico de Saldos**: alteração ou exclusão de transações reflete instantaneamente nos saldos bancários.

### 3. Acompanhamento de Investimentos & Mercado B3
- **Cotações em tempo real** integradas com a API da **Brapi** (`brapi.dev`) com camada de resiliência e fallback.
- Registro de ordens de **Compra** e **Venda** de ações, FIIs e ETFs.
- **Preço Médio Ponderado (PM)** automático com inclusão de taxas e corretagem.
- **Validação estrita de custódia**: proteção contra vendas descobertas.
- Cálculo de **Lucro/Prejuízo Líquido** e **Rentabilidade Percentual (%)**.

### 4. Dashboard Consolidado
- Visão única do **Patrimônio Total Líquido** (Saldo Disponível + Investimentos a Mercado).
- Gráfico de **Evolução de Fluxo de Caixa** mensal (Receitas vs. Despesas).
- Gráfico de **Alocação de Ativos** por classe (Ações, FIIs, ETFs).
- Feed das últimas transações e posições mais relevantes.

---

## 🛠️ Tecnologias Utilizadas

### Backend
- **Java 21 LTS**
- **Spring Boot 3.4.3**
- **Spring Security 6 (JWT)**
- **Spring Data JPA & Hibernate**
- **PostgreSQL 16**
- **Springdoc OpenAPI (Swagger UI)**

### Frontend
- **React 18**
- **TypeScript**
- **Vite**
- **Tailwind CSS v4**
- **Recharts** (Gráficos interativos)
- **Lucide React** (Ícones modernos)
- **Axios** (Cliente HTTP com interceptors)

---

## 🚀 Como Executar Localmente

### Pré-requisitos
- **Java 21+** instalado
- **Node.js 20+** e **npm**
- **PostgreSQL 16** rodando na porta `5432` com banco `gestao_financeira`

### 1. Configurar Banco de Dados
```sql
CREATE DATABASE gestao_financeira;
```
Configure as credenciais em `backend/src/main/resources/application.yml` ou via variáveis de ambiente (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`).

### 2. Executar o Backend
```bash
cd backend
./mvnw spring-boot:run
```
> API disponível em: `http://localhost:8080`  
> Swagger OpenAPI: `http://localhost:8080/swagger-ui.html`

### 3. Executar o Frontend
```bash
cd frontend
npm install
npm run dev
```
> Acesse no navegador: `http://localhost:5173`

---

## 📄 Especificação Completa

Para a documentação técnica aprofundada (modelagem relacional, fórmulas de precificação e arquitetura), consulte o arquivo [`SPEC_GESTAO_FINANCEIRA.md`](./SPEC_GESTAO_FINANCEIRA.md).
