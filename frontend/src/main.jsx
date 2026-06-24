import React, { useEffect, useMemo, useState } from 'react';
import { createRoot } from 'react-dom/client';
import './styles.css';

const API_URL = (import.meta.env.VITE_API_URL || '/api').replace(/\/$/, '');
const PAYMENT_OPTIONS = ['DINHEIRO', 'PIX', 'DEBITO', 'CREDITO', 'BOLETO', 'TRANSFERENCIA', 'OUTRO'];
const STORAGE_KEY = 'assistenteFinanceiroUsuario';

const money = new Intl.NumberFormat('pt-BR', {
  style: 'currency',
  currency: 'BRL',
});

const monthOptions = [
  { value: 1, label: 'Janeiro' },
  { value: 2, label: 'Fevereiro' },
  { value: 3, label: 'Marco' },
  { value: 4, label: 'Abril' },
  { value: 5, label: 'Maio' },
  { value: 6, label: 'Junho' },
  { value: 7, label: 'Julho' },
  { value: 8, label: 'Agosto' },
  { value: 9, label: 'Setembro' },
  { value: 10, label: 'Outubro' },
  { value: 11, label: 'Novembro' },
  { value: 12, label: 'Dezembro' },
];

const navItems = [
  { id: 'dashboard', label: 'Dashboard', icon: 'grid' },
  { id: 'perfil', label: 'Perfil', icon: 'user' },
  { id: 'categorias', label: 'Categorias', icon: 'tag' },
  { id: 'chat', label: 'WhatsApp', icon: 'chat' },
  { id: 'lancamentos', label: 'Lancamentos', icon: 'list' },
  { id: 'simulador', label: 'Simulador', icon: 'calc' },
];

function currentDashboardPeriod() {
  const today = new Date();
  return {
    year: today.getFullYear(),
    month: today.getMonth() + 1,
  };
}

function formatDashboardPeriod(period) {
  const month = monthOptions.find((option) => option.value === Number(period.month));
  return `${month?.label ?? 'Mes'} de ${period.year}`;
}

function App() {
  const [usuario, setUsuario] = useStoredUser();
  const [activeView, setActiveView] = useState('dashboard');
  const [dashboardPeriod, setDashboardPeriod] = useState(currentDashboardPeriod);
  const [dashboard, setDashboard] = useState(null);
  const [categories, setCategories] = useState([]);
  const [launches, setLaunches] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');

  async function loadInitialData(options = {}) {
    if (!usuario) {
      return;
    }
    const showLoading = options.showLoading ?? true;
    if (showLoading) {
      setLoading(true);
    }
    setError('');
    try {
      const userId = usuario.usuarioId;
      const [dashboardData, categoryData, launchData] = await Promise.all([
        getJson(`/dashboard/mensal?usuarioId=${userId}&ano=${dashboardPeriod.year}&mes=${dashboardPeriod.month}`),
        getJson(`/categorias?usuarioId=${userId}`),
        getJson(`/lancamentos?usuarioId=${userId}`).catch(() => []),
      ]);
      setDashboard(dashboardData);
      setCategories(categoryData);
      setLaunches(Array.isArray(launchData) ? launchData : []);
    } catch (err) {
      setError(err.message);
    } finally {
      if (showLoading) {
        setLoading(false);
      }
    }
  }

  async function refreshAfterChange(message) {
    await loadInitialData({ showLoading: false });
    setNotice(message);
    window.setTimeout(() => setNotice(''), 3200);
  }

  function logout() {
    setUsuario(null);
    setDashboard(null);
    setCategories([]);
    setLaunches([]);
    setActiveView('dashboard');
  }

  useEffect(() => {
    loadInitialData();
  }, [usuario?.usuarioId, dashboardPeriod.year, dashboardPeriod.month]);

  if (!usuario) {
    return <AuthScreen onAuthenticated={setUsuario} />;
  }

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">
          <div className="brand-mark">AF</div>
          <div>
            <strong>Assistente Financeiro</strong>
            <span>{usuario.nome}</span>
          </div>
        </div>

        <nav className="nav-list" aria-label="Navegacao principal">
          {navItems.map((item) => (
            <button
              className={activeView === item.id ? 'nav-button active' : 'nav-button'}
              type="button"
              key={item.id}
              onClick={() => setActiveView(item.id)}
              title={item.label}
            >
              <Icon name={item.icon} />
              <span>{item.label}</span>
            </button>
          ))}
        </nav>
      </aside>

      <main className="content">
        <header className="topbar">
          <div>
            <p className="eyebrow">MVP conectado ao Spring Boot</p>
            <h1>{titleFor(activeView)}</h1>
          </div>
          <div className="topbar-actions">
            <button className="ghost-button" type="button" onClick={() => loadInitialData()}>
              <Icon name="refresh" />
              Atualizar
            </button>
            <button className="ghost-button" type="button" onClick={logout}>
              <Icon name="logout" />
              Sair
            </button>
          </div>
        </header>

        {error && <Notice type="error" text={error} />}
        {notice && <Notice type="success" text={notice} />}
        {loading && <Notice text="Carregando dados do backend..." />}

        {!loading && activeView === 'dashboard' && (
          <Dashboard
            dashboard={dashboard}
            period={dashboardPeriod}
            onPeriodChange={setDashboardPeriod}
            onOpenChat={() => setActiveView('chat')}
          />
        )}
        {!loading && activeView === 'perfil' && (
          <Profile
            usuario={usuario}
            onProfileLoaded={setUsuario}
            onProfileSaved={(updatedUser) => {
              setUsuario(updatedUser);
              setNotice('Perfil atualizado com sucesso.');
              window.setTimeout(() => setNotice(''), 3200);
            }}
          />
        )}
        {!loading && activeView === 'categorias' && (
          <Categories
            categories={categories}
            usuario={usuario}
            onChanged={refreshAfterChange}
          />
        )}
        {!loading && activeView === 'chat' && (
          <Chat
            usuario={usuario}
            onMessageProcessed={() => refreshAfterChange('Dashboard e lancamentos atualizados.')}
          />
        )}
        {!loading && activeView === 'lancamentos' && (
          <Launches
            launches={launches}
            categories={categories}
            usuario={usuario}
            onChanged={refreshAfterChange}
          />
        )}
        {!loading && activeView === 'simulador' && <Simulator usuario={usuario} />}
      </main>
    </div>
  );
}

function useStoredUser() {
  const [usuario, setUsuarioState] = useState(() => {
    const stored = window.localStorage.getItem(STORAGE_KEY);
    return stored ? JSON.parse(stored) : null;
  });

  function setUsuario(nextUser) {
    setUsuarioState(nextUser);
    if (nextUser) {
      window.localStorage.setItem(STORAGE_KEY, JSON.stringify(nextUser));
    } else {
      window.localStorage.removeItem(STORAGE_KEY);
    }
  }

  return [usuario, setUsuario];
}

function AuthScreen({ onAuthenticated }) {
  const [mode, setMode] = useState('login');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [form, setForm] = useState({
    nome: '',
    email: '',
    senha: '',
    telefoneWhatsapp: '',
  });

  function updateField(field, value) {
    setForm((current) => ({ ...current, [field]: value }));
  }

  async function submit(event) {
    event.preventDefault();
    setLoading(true);
    setError('');

    try {
      const payload = mode === 'login'
        ? { email: form.email, senha: form.senha }
        : form;
      const data = await postJson(mode === 'login' ? '/auth/login' : '/auth/register', payload);
      onAuthenticated(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="auth-screen">
      <section className="auth-panel">
        <div>
          <p className="eyebrow">Assistente Financeiro</p>
          <h1>{mode === 'login' ? 'Entrar' : 'Criar conta'}</h1>
        </div>

        {error && <Notice type="error" text={error} />}

        <form className="auth-form" onSubmit={submit}>
          {mode === 'register' && (
            <label>
              Nome
              <input required value={form.nome} onChange={(event) => updateField('nome', event.target.value)} />
            </label>
          )}
          <label>
            Email
            <input required type="email" value={form.email} onChange={(event) => updateField('email', event.target.value)} />
          </label>
          <label>
            Senha
            <input required type="password" value={form.senha} onChange={(event) => updateField('senha', event.target.value)} />
          </label>
          {mode === 'register' && (
            <label>
              WhatsApp
              <input
                required
                value={form.telefoneWhatsapp}
                onChange={(event) => updateField('telefoneWhatsapp', event.target.value)}
                placeholder="5534999999999"
              />
            </label>
          )}
          <button className="primary-button" type="submit" disabled={loading}>
            <Icon name="save" />
            {loading ? 'Enviando...' : mode === 'login' ? 'Entrar' : 'Cadastrar'}
          </button>
        </form>

        <button
          className="ghost-button"
          type="button"
          onClick={() => {
            setError('');
            setMode(mode === 'login' ? 'register' : 'login');
          }}
        >
          {mode === 'login' ? 'Criar nova conta' : 'Ja tenho conta'}
        </button>
      </section>
    </main>
  );
}

async function getJson(path) {
  const response = await fetch(`${API_URL}${path}`);
  if (!response.ok) {
    throw new Error(await errorMessage(response, 'Nao foi possivel conectar ao backend. Confira a configuracao da API.'));
  }
  return response.json();
}

async function postJson(path, body) {
  const response = await fetch(`${API_URL}${path}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  });
  if (!response.ok) {
    throw new Error(await errorMessage(response, 'O backend recusou a requisicao. Verifique os dados enviados.'));
  }
  return response.json();
}

async function putJson(path, body) {
  const response = await fetch(`${API_URL}${path}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  });
  if (!response.ok) {
    throw new Error(await errorMessage(response, 'Nao foi possivel salvar as alteracoes. Confira os campos e tente novamente.'));
  }
  return response.json();
}

async function deleteJson(path, resource = 'registro') {
  const response = await fetch(`${API_URL}${path}`, {
    method: 'DELETE',
  });
  if (!response.ok) {
    throw new Error(await errorMessage(response, `Nao foi possivel excluir o ${resource}. Tente novamente.`));
  }
}

async function errorMessage(response, fallback) {
  try {
    const data = await response.json();
    return data.detalhes?.[0] ?? data.message ?? fallback;
  } catch {
    return fallback;
  }
}

function Dashboard({ dashboard, period, onPeriodChange, onOpenChat }) {
  const chartData = dashboard?.gastosPorCategoria ?? [];
  const periodLabel = formatDashboardPeriod(period);

  return (
    <section className="screen-grid">
      <div className="metrics-row">
        <Metric title="Entradas do mes" value={money.format(dashboard?.totalEntradas ?? 0)} tone="income" />
        <Metric title="Saidas do mes" value={money.format(dashboard?.totalSaidas ?? 0)} tone="expense" />
        <Metric title="Saldo mensal" value={money.format(dashboard?.saldoMensal ?? 0)} tone="balance" />
      </div>

      <div className="dashboard-layout">
        <section className="panel chart-panel">
          <div className="panel-heading">
            <div>
              <p className="eyebrow">{periodLabel}</p>
              <h2>Gastos por categoria</h2>
            </div>
            <div className="period-controls">
              <label>
                Mes
                <select
                  value={period.month}
                  onChange={(event) => onPeriodChange((current) => ({
                    ...current,
                    month: Number(event.target.value),
                  }))}
                >
                  {monthOptions.map((month) => (
                    <option key={month.value} value={month.value}>
                      {month.label}
                    </option>
                  ))}
                </select>
              </label>
              <label>
                Ano
                <input
                  type="number"
                  min="2020"
                  max="2100"
                  value={period.year}
                  onChange={(event) => onPeriodChange((current) => ({
                    ...current,
                    year: Number(event.target.value),
                  }))}
                />
              </label>
            </div>
          </div>
          <CategoryChart data={chartData} />
        </section>

        <section className="panel assistant-panel">
          <div>
            <p className="eyebrow">WhatsApp simulado</p>
            <h2>Registre gastos em linguagem natural</h2>
          </div>
          <div className="suggestions">
            <span>gastei 35 reais com lanche</span>
            <span>saida 50 transporte</span>
            <span>qual meu saldo?</span>
          </div>
          <button className="primary-button" type="button" onClick={onOpenChat}>
            <Icon name="chat" />
            Abrir conversa
          </button>
        </section>
      </div>
    </section>
  );
}

function Profile({ usuario, onProfileLoaded, onProfileSaved }) {
  const [form, setForm] = useState({
    nome: usuario.nome ?? '',
    email: usuario.email ?? '',
    telefoneWhatsapp: usuario.telefoneWhatsapp ?? '',
  });
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState('');

  useEffect(() => {
    let active = true;
    setLoading(true);
    setMessage('');
    getJson(`/usuarios/${usuario.usuarioId}`)
      .then((data) => {
        if (!active) {
          return;
        }
        setForm({
          nome: data.nome ?? '',
          email: data.email ?? '',
          telefoneWhatsapp: data.telefoneWhatsapp ?? '',
        });
        onProfileLoaded(data);
      })
      .catch((err) => {
        if (active) {
          setMessage(err.message);
        }
      })
      .finally(() => {
        if (active) {
          setLoading(false);
        }
      });
    return () => {
      active = false;
    };
  }, [usuario.usuarioId]);

  function updateField(field, value) {
    setForm((current) => ({ ...current, [field]: value }));
  }

  async function saveProfile(event) {
    event.preventDefault();
    setSaving(true);
    setMessage('');

    try {
      const telefoneNormalizado = normalizeWhatsappPhone(form.telefoneWhatsapp);
      if (!/^55\d{10,11}$/.test(telefoneNormalizado)) {
        throw new Error('Telefone WhatsApp deve comecar com 55 e ter 12 ou 13 digitos, ou ter DDD + numero com 10 ou 11 digitos.');
      }
      const updatedUser = await putJson(`/usuarios/${usuario.usuarioId}`, form);
      setForm({
        nome: updatedUser.nome ?? '',
        email: updatedUser.email ?? '',
        telefoneWhatsapp: updatedUser.telefoneWhatsapp ?? '',
      });
      onProfileSaved(updatedUser);
    } catch (err) {
      setMessage(err.message);
    } finally {
      setSaving(false);
    }
  }

  return (
    <section className="profile-layout">
      <form className="panel form-panel profile-form" onSubmit={saveProfile}>
        <div>
          <p className="eyebrow">Dados da conta</p>
          <h2>Perfil do usuario</h2>
        </div>
        {message && <Notice type="error" text={message} />}
        {loading && <Notice text="Carregando perfil..." />}
        <label>
          Nome
          <input required value={form.nome} onChange={(event) => updateField('nome', event.target.value)} />
        </label>
        <label>
          Email
          <input required type="email" value={form.email} onChange={(event) => updateField('email', event.target.value)} />
        </label>
        <label>
          WhatsApp
          <input
            required
            value={form.telefoneWhatsapp}
            onChange={(event) => updateField('telefoneWhatsapp', event.target.value)}
            placeholder="5534997895652"
          />
        </label>
        <div className="form-actions">
          <button className="primary-button" type="submit" disabled={saving || loading}>
            <Icon name="save" />
            {saving ? 'Salvando...' : 'Salvar perfil'}
          </button>
        </div>
      </form>

      <aside className="panel profile-summary">
        <p className="eyebrow">WhatsApp ativo</p>
        <strong>{form.telefoneWhatsapp || '-'}</strong>
        <span>Este numero sera usado na conversa simulada e no webhook Twilio.</span>
      </aside>
    </section>
  );
}

function Metric({ title, value, tone }) {
  return (
    <article className={`metric ${tone}`}>
      <span>{title}</span>
      <strong>{value}</strong>
    </article>
  );
}

function CategoryChart({ data }) {
  const max = Math.max(...data.map((item) => Number(item.total)), 0);

  if (!data.length) {
    return <EmptyState text="Ainda nao ha gastos por categoria para este periodo." />;
  }

  return (
    <div className="chart-list">
      {data.map((item, index) => {
        const value = Number(item.total);
        const width = max > 0 ? Math.max((value / max) * 100, 8) : 8;
        return (
          <div className="chart-row" key={item.categoriaId ?? item.categoriaNome}>
            <div className="chart-label">
              <span className="dot" style={{ background: chartColor(index) }} />
              <strong>{item.categoriaNome}</strong>
              <span>{money.format(value)}</span>
            </div>
            <div className="bar-track">
              <div
                className="bar-fill"
                style={{ width: `${width}%`, background: chartColor(index) }}
              />
            </div>
          </div>
        );
      })}
    </div>
  );
}

function Categories({ categories, usuario, onChanged }) {
  const [editing, setEditing] = useState(null);
  const [message, setMessage] = useState('');
  const [saving, setSaving] = useState(false);
  const [form, setForm] = useState({ nome: '', cor: '#0f766e' });

  function startCreate() {
    setEditing(null);
    setForm({ nome: '', cor: '#0f766e' });
    setMessage('');
  }

  function startEdit(category) {
    setEditing(category);
    setForm({ nome: category.nome, cor: category.cor || '#0f766e' });
    setMessage('');
  }

  async function saveCategory(event) {
    event.preventDefault();
    setSaving(true);
    setMessage('');
    const payload = { ...form, usuarioId: usuario.usuarioId };

    try {
      if (editing) {
        await putJson(`/categorias/${editing.id}`, payload);
        await onChanged('Categoria atualizada com sucesso.');
      } else {
        await postJson('/categorias', payload);
        await onChanged('Categoria criada com sucesso.');
      }
      startCreate();
    } catch (err) {
      setMessage(err.message);
    } finally {
      setSaving(false);
    }
  }

  async function removeCategory(category) {
    const confirmed = window.confirm(`Excluir a categoria "${category.nome}"?`);
    if (!confirmed) {
      return;
    }
    try {
      await deleteJson(`/categorias/${category.id}`, 'categoria');
      await onChanged('Categoria excluida com sucesso.');
      startCreate();
    } catch (err) {
      setMessage(err.message);
    }
  }

  return (
    <section className="categories-layout">
      <form className="panel form-panel" onSubmit={saveCategory}>
        <div>
          <p className="eyebrow">{editing ? 'Edicao' : 'Cadastro'}</p>
          <h2>{editing ? 'Editar categoria' : 'Nova categoria'}</h2>
        </div>
        {message && <Notice type="error" text={message} />}
        <label>
          Nome
          <input required value={form.nome} onChange={(event) => setForm((current) => ({ ...current, nome: event.target.value }))} />
        </label>
        <label>
          Cor
          <input required type="color" value={form.cor} onChange={(event) => setForm((current) => ({ ...current, cor: event.target.value }))} />
        </label>
        <div className="form-actions">
          <button className="primary-button" type="submit" disabled={saving}>
            <Icon name="save" />
            {saving ? 'Salvando...' : 'Salvar categoria'}
          </button>
          {editing && (
            <button className="ghost-button" type="button" onClick={startCreate}>
              Cancelar
            </button>
          )}
        </div>
      </form>

      <section className="category-grid">
        {!categories.length && <EmptyState text="Nenhuma categoria encontrada para o usuario logado." />}
        {categories.map((category) => (
          <article className="category-card" key={category.id}>
            <span className="category-swatch" style={{ background: category.cor || '#25d366' }} />
            <div>
              <strong>{category.nome}</strong>
              <span>Usuario #{category.usuarioId}</span>
            </div>
            <div className="category-actions">
              <button className="icon-button" type="button" onClick={() => startEdit(category)} title="Editar categoria">
                <Icon name="edit" />
              </button>
              <button className="icon-button danger" type="button" onClick={() => removeCategory(category)} title="Excluir categoria">
                <Icon name="trash" />
              </button>
            </div>
          </article>
        ))}
      </section>
    </section>
  );
}

function Chat({ usuario, onMessageProcessed }) {
  const [message, setMessage] = useState('gastei 35 reais com lanche');
  const [sending, setSending] = useState(false);
  const [chat, setChat] = useState([
    {
      from: 'api',
      text: 'Ola! Envie uma mensagem como: gastei 35 reais com lanche, recebi 2025 salario ou qual meu saldo?',
    },
  ]);

  async function sendMessage(event) {
    event.preventDefault();
    const cleanMessage = message.trim();
    if (!cleanMessage) {
      return;
    }

    setChat((current) => [...current, { from: 'user', text: cleanMessage }]);
    setMessage('');
    setSending(true);

    try {
      const response = await postJson('/webhook/whatsapp', {
        telefone: usuario.telefoneWhatsapp,
        mensagem: cleanMessage,
      });
      setChat((current) => [
        ...current,
        {
          from: 'api',
          text: response.resposta,
          meta: `${response.intencao} - ${response.status}`,
        },
      ]);
      onMessageProcessed();
    } catch (err) {
      setChat((current) => [...current, { from: 'api error', text: err.message }]);
    } finally {
      setSending(false);
    }
  }

  return (
    <section className="chat-layout">
      <div className="phone-frame">
        <div className="phone-header">
          <div className="avatar">AF</div>
          <div>
            <strong>Assistente Financeiro</strong>
            <span>{usuario.telefoneWhatsapp}</span>
          </div>
        </div>

        <div className="messages">
          {chat.map((item, index) => (
            <div className={`bubble ${item.from}`} key={`${item.from}-${index}`}>
              <p>{item.text}</p>
              {item.meta && <small>{item.meta}</small>}
            </div>
          ))}
        </div>

        <form className="chat-form" onSubmit={sendMessage}>
          <input
            aria-label="Mensagem WhatsApp"
            value={message}
            onChange={(event) => setMessage(event.target.value)}
            placeholder="Digite sua mensagem"
          />
          <button className="send-button" type="submit" disabled={sending} title="Enviar">
            <Icon name="send" />
          </button>
        </form>
      </div>

      <aside className="panel examples-panel">
        <p className="eyebrow">Frases para testar</p>
        {['gastei 35 reais com lanche', 'saida 50 transporte', 'recebi 2025 salario', 'qual meu saldo?'].map((text) => (
          <button type="button" key={text} onClick={() => setMessage(text)}>
            {text}
          </button>
        ))}
      </aside>
    </section>
  );
}

function Launches({ launches, categories, usuario, onChanged }) {
  const [showForm, setShowForm] = useState(false);
  const [editing, setEditing] = useState(null);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState('');
  const [form, setForm] = useState(emptyLaunchForm(categories));

  const sortedLaunches = useMemo(
    () => [...launches].sort((a, b) => `${b.data}-${b.id}`.localeCompare(`${a.data}-${a.id}`)),
    [launches]
  );

  function openNewForm() {
    setEditing(null);
    setForm(emptyLaunchForm(categories));
    setMessage('');
    setShowForm(true);
  }

  function openEditForm(launch) {
    setEditing(launch);
    setForm({
      descricao: launch.descricao ?? '',
      valor: String(launch.valor ?? ''),
      data: launch.data ?? today(),
      tipo: launch.tipo ?? 'SAIDA',
      formaPagamento: launch.formaPagamento ?? 'OUTRO',
      categoriaId: String(launch.categoriaId ?? categories[0]?.id ?? ''),
      observacao: '',
    });
    setMessage('');
    setShowForm(true);
  }

  function updateField(field, value) {
    setForm((current) => ({ ...current, [field]: value }));
  }

  async function saveLaunch(event) {
    event.preventDefault();
    setSaving(true);
    setMessage('');

    try {
      const payload = launchPayload(form, usuario.usuarioId);
      if (editing) {
        await putJson(`/lancamentos/${editing.id}`, payload);
        await onChanged('Lancamento atualizado com sucesso.');
      } else {
        await postJson('/lancamentos', payload);
        await onChanged('Lancamento cadastrado com sucesso.');
      }
      setShowForm(false);
      setEditing(null);
      setForm(emptyLaunchForm(categories));
    } catch (err) {
      setMessage(err.message);
    } finally {
      setSaving(false);
    }
  }

  async function removeLaunch(launch) {
    const confirmed = window.confirm(`Excluir o lancamento "${launch.descricao}"?`);
    if (!confirmed) {
      return;
    }

    setMessage('');
    try {
      await deleteJson(`/lancamentos/${launch.id}`, 'lancamento');
      await onChanged('Lancamento excluido com sucesso.');
    } catch (err) {
      setMessage(err.message);
    }
  }

  return (
    <section className="launches-screen">
      <div className="launches-toolbar">
        <div>
          <p className="eyebrow">Controle manual</p>
          <h2>Lancamentos financeiros</h2>
        </div>
        <button className="primary-button" type="button" onClick={openNewForm}>
          <Icon name="plus" />
          Novo lancamento
        </button>
      </div>

      {message && <Notice type="error" text={message} />}

      {showForm && (
        <form className="panel launch-form" onSubmit={saveLaunch}>
          <div className="form-title-row">
            <div>
              <p className="eyebrow">{editing ? 'Edicao' : 'Cadastro'}</p>
              <h3>{editing ? 'Editar lancamento' : 'Novo lancamento'}</h3>
            </div>
            <button className="ghost-button" type="button" onClick={() => setShowForm(false)}>
              Cancelar
            </button>
          </div>

          <div className="form-grid">
            <label>
              Descricao
              <input required value={form.descricao} onChange={(event) => updateField('descricao', event.target.value)} placeholder="Ex.: Lanche" />
            </label>
            <label>
              Valor
              <input required type="number" min="0.01" step="0.01" value={form.valor} onChange={(event) => updateField('valor', event.target.value)} />
            </label>
            <label>
              Data
              <input required type="date" value={form.data} onChange={(event) => updateField('data', event.target.value)} />
            </label>
            <label>
              Tipo
              <select value={form.tipo} onChange={(event) => updateField('tipo', event.target.value)}>
                <option value="SAIDA">SAIDA</option>
                <option value="ENTRADA">ENTRADA</option>
              </select>
            </label>
            <label>
              Forma de pagamento
              <select value={form.formaPagamento} onChange={(event) => updateField('formaPagamento', event.target.value)}>
                {PAYMENT_OPTIONS.map((option) => (
                  <option key={option} value={option}>{option}</option>
                ))}
              </select>
            </label>
            <label>
              Categoria
              <select required value={form.categoriaId} onChange={(event) => updateField('categoriaId', event.target.value)}>
                <option value="" disabled>Selecione</option>
                {categories.map((category) => (
                  <option key={category.id} value={category.id}>{category.nome}</option>
                ))}
              </select>
            </label>
            <label className="wide-field">
              Observacao
              <textarea rows="3" value={form.observacao} onChange={(event) => updateField('observacao', event.target.value)} placeholder="Detalhes adicionais" />
            </label>
          </div>

          <div className="form-actions">
            <button className="primary-button" type="submit" disabled={saving}>
              <Icon name="save" />
              {saving ? 'Salvando...' : 'Salvar lancamento'}
            </button>
          </div>
        </form>
      )}

      <section className="panel table-panel">
        <div className="table-header">
          <strong>Ultimos lancamentos</strong>
          <span>{sortedLaunches.length} registros</span>
        </div>
        {!sortedLaunches.length ? (
          <EmptyState text="Nenhum lancamento encontrado. Cadastre o primeiro pelo botao Novo lancamento." />
        ) : (
          <div className="responsive-table">
            <table>
              <thead>
                <tr>
                  <th>Descricao</th>
                  <th>Categoria</th>
                  <th>Tipo</th>
                  <th>Pagamento</th>
                  <th>Data</th>
                  <th>Valor</th>
                  <th>Acoes</th>
                </tr>
              </thead>
              <tbody>
                {sortedLaunches.map((item) => (
                  <tr key={item.id}>
                    <td>{item.descricao}</td>
                    <td>{item.categoriaNome}</td>
                    <td>
                      <span className={`type-pill ${item.tipo === 'ENTRADA' ? 'income' : 'expense'}`}>
                        {item.tipo}
                      </span>
                    </td>
                    <td>{item.formaPagamento}</td>
                    <td>{formatDate(item.data)}</td>
                    <td>{money.format(Number(item.valor))}</td>
                    <td>
                      <div className="row-actions">
                        <button className="icon-button" type="button" onClick={() => openEditForm(item)} title="Editar">
                          <Icon name="edit" />
                        </button>
                        <button className="icon-button danger" type="button" onClick={() => removeLaunch(item)} title="Excluir">
                          <Icon name="trash" />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </section>
  );
}

function emptyLaunchForm(categories) {
  return {
    descricao: '',
    valor: '',
    data: today(),
    tipo: 'SAIDA',
    formaPagamento: 'OUTRO',
    categoriaId: String(categories[0]?.id ?? ''),
    observacao: '',
  };
}

function launchPayload(form, usuarioId) {
  const descricao = form.observacao.trim()
    ? `${form.descricao.trim()} - ${form.observacao.trim()}`
    : form.descricao.trim();

  return {
    descricao,
    valor: Number(form.valor),
    data: form.data,
    tipo: form.tipo,
    formaPagamento: form.formaPagamento,
    usuarioId,
    categoriaId: Number(form.categoriaId),
  };
}

function today() {
  return new Date().toISOString().slice(0, 10);
}

function formatDate(value) {
  if (!value) {
    return '-';
  }
  const [year, month, day] = value.split('-');
  return `${day}/${month}/${year}`;
}

function normalizeWhatsappPhone(value) {
  const digits = String(value ?? '')
    .replace(/whatsapp:/i, '')
    .replace(/\+/g, '')
    .replace(/\s/g, '')
    .replace(/[().-]/g, '')
    .trim();
  if (/^\d{10,11}$/.test(digits)) {
    return `55${digits}`;
  }
  return digits;
}

function Simulator({ usuario }) {
  const [form, setForm] = useState({
    descricao: 'Notebook para estudos',
    valor: '2500',
    formaPagamento: 'CREDITO',
    quantidadeParcelas: '5',
    anoImpactado: '2026',
    mesImpactado: '5',
  });
  const [result, setResult] = useState(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  function updateField(field, value) {
    setForm((current) => ({ ...current, [field]: value }));
  }

  async function submit(event) {
    event.preventDefault();
    setLoading(true);
    setError('');
    setResult(null);

    try {
      const data = await postJson('/simulacoes', {
        descricao: form.descricao,
        valor: Number(form.valor),
        formaPagamento: form.formaPagamento,
        quantidadeParcelas: Number(form.quantidadeParcelas),
        anoImpactado: Number(form.anoImpactado),
        mesImpactado: Number(form.mesImpactado),
        usuarioId: usuario.usuarioId,
      });
      setResult(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <section className="simulator-layout">
      <form className="panel form-panel" onSubmit={submit}>
        <label>
          Descricao
          <input value={form.descricao} onChange={(event) => updateField('descricao', event.target.value)} />
        </label>
        <label>
          Valor
          <input type="number" min="1" step="0.01" value={form.valor} onChange={(event) => updateField('valor', event.target.value)} />
        </label>
        <label>
          Forma de pagamento
          <select value={form.formaPagamento} onChange={(event) => updateField('formaPagamento', event.target.value)}>
            <option>CREDITO</option>
            <option>PIX</option>
            <option>DEBITO</option>
            <option>DINHEIRO</option>
            <option>BOLETO</option>
          </select>
        </label>
        <div className="form-row">
          <label>
            Parcelas
            <input type="number" min="1" value={form.quantidadeParcelas} onChange={(event) => updateField('quantidadeParcelas', event.target.value)} />
          </label>
          <label>
            Mes
            <input type="number" min="1" max="12" value={form.mesImpactado} onChange={(event) => updateField('mesImpactado', event.target.value)} />
          </label>
          <label>
            Ano
            <input type="number" min="2024" value={form.anoImpactado} onChange={(event) => updateField('anoImpactado', event.target.value)} />
          </label>
        </div>
        <button className="primary-button" type="submit" disabled={loading}>
          <Icon name="calc" />
          Simular compra
        </button>
      </form>

      <section className="panel result-panel">
        {error && <Notice type="error" text={error} />}
        {!error && !result && <EmptyState text="Preencha os dados para calcular o impacto da compra no saldo mensal." />}
        {result && (
          <>
            <p className="eyebrow">{result.saldoSuficiente ? 'Saldo suficiente' : 'Atencao ao saldo'}</p>
            <h2>{result.mensagem}</h2>
            <div className="result-values">
              <span>Antes <strong>{money.format(Number(result.saldoAntes))}</strong></span>
              <span>Depois <strong>{money.format(Number(result.saldoDepois))}</strong></span>
            </div>
          </>
        )}
      </section>
    </section>
  );
}

function Notice({ text, type = 'info' }) {
  return <div className={`notice ${type}`}>{text}</div>;
}

function EmptyState({ text }) {
  return (
    <div className="empty-state">
      <Icon name="info" />
      <p>{text}</p>
    </div>
  );
}

function Icon({ name }) {
  const icons = {
    grid: 'M4 4h7v7H4z M13 4h7v7h-7z M4 13h7v7H4z M13 13h7v7h-7z',
    tag: 'M20 10l-10 10-7-7V3h10l7 7z M7 7h.01',
    user: 'M20 21a8 8 0 0 0-16 0 M12 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8z',
    chat: 'M4 5h16v11H8l-4 4z',
    list: 'M8 6h13 M8 12h13 M8 18h13 M3 6h.01 M3 12h.01 M3 18h.01',
    calc: 'M6 3h12v18H6z M8 7h8 M9 12h.01 M12 12h.01 M15 12h.01 M9 16h.01 M12 16h.01 M15 16h.01',
    refresh: 'M20 12a8 8 0 1 1-2.34-5.66 M20 4v6h-6',
    send: 'M22 2L11 13 M22 2l-7 20-4-9-9-4z',
    info: 'M12 2a10 10 0 1 0 0 20 10 10 0 0 0 0-20z M12 10v6 M12 7h.01',
    plus: 'M12 5v14 M5 12h14',
    save: 'M5 3h14l2 2v16H3V3h2z M7 3v7h10V3 M7 21v-8h10v8',
    edit: 'M12 20h9 M16.5 3.5a2.12 2.12 0 0 1 3 3L7 19l-4 1 1-4 12.5-12.5z',
    trash: 'M3 6h18 M8 6V4h8v2 M6 6l1 15h10l1-15 M10 11v6 M14 11v6',
    logout: 'M10 17l5-5-5-5 M15 12H3 M21 3v18h-6',
  };

  return (
    <svg viewBox="0 0 24 24" aria-hidden="true">
      <path d={icons[name]} />
    </svg>
  );
}

function chartColor(index) {
  return ['#0f766e', '#2563eb', '#c2410c', '#7c3aed', '#be123c', '#15803d'][index % 6];
}

function titleFor(view) {
  return {
    dashboard: 'Dashboard financeiro',
    perfil: 'Perfil',
    categorias: 'Categorias',
    chat: 'Chat WhatsApp simulado',
    lancamentos: 'Lancamentos',
    simulador: 'Simulador de compra',
  }[view];
}

createRoot(document.getElementById('root')).render(<App />);
