/**
 * api.js — Capa de servicio HTTP
 * ============================================================
 * ARQUITECTURA:
 *   Todos los endpoints del backend Spring Boot están mapeados aquí.
 *   Para conectar al backend real, solo cambiar BASE_URL y
 *   descomentar la lógica de fetch real en cada método.
 *
 * MODO ACTUAL: MOCK (datos hardcoded para desarrollo sin backend)
 * MODO PRODUCCIÓN: cambiar USE_MOCK = false
 * ============================================================
 */

// ─── CONFIGURACIÓN ───────────────────────────────────────────
const API_CONFIG = {
  BASE_URL: 'http://localhost:8080/api',   // URL de Spring Boot
  USE_MOCK: false,                           // false = conectar al backend real
  TIMEOUT: 8000,                            // ms
  DEFAULT_HEADERS: {
    'Content-Type': 'application/json',
    'Accept': 'application/json',
  },
};

// ─── TOKEN JWT ───────────────────────────────────────────────
const Auth = {
  getToken()         { return localStorage.getItem('sb_token'); },
  setToken(t)        { localStorage.setItem('sb_token', t); },
  removeToken()      { localStorage.removeItem('sb_token'); },
  getUser()          { return JSON.parse(localStorage.getItem('sb_user') || 'null'); },
  setUser(u)         { localStorage.setItem('sb_user', JSON.stringify(u)); },
  isLoggedIn()       { return !!this.getToken(); },
  hasRole(role)      { const u = this.getUser(); return u?.roles?.includes(role); },
};

// ─── HTTP CLIENT BASE ─────────────────────────────────────────
async function request(method, path, body = null) {
  const url = `${API_CONFIG.BASE_URL}${path}`;
  const token = Auth.getToken();

  const headers = { ...API_CONFIG.DEFAULT_HEADERS };
  if (token) headers['Authorization'] = `Bearer ${token}`;

  const opts = { method, headers };
  if (body) opts.body = JSON.stringify(body);

  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), API_CONFIG.TIMEOUT);
  opts.signal = controller.signal;

  try {
    const res = await fetch(url, opts);
    clearTimeout(timer);

    // 401: token expirado → limpiar sesión
    if (res.status === 401) {
      Auth.removeToken();
      Auth.removeUser?.();
      window.dispatchEvent(new CustomEvent('auth:expired'));
    }

    const data = res.headers.get('content-type')?.includes('application/json')
      ? await res.json()
      : await res.text();

    if (!res.ok) throw { status: res.status, data };
    return data;

  } catch (err) {
    clearTimeout(timer);
    if (err.name === 'AbortError') throw { status: 0, data: 'Timeout — sin respuesta del servidor' };
    throw err;
  }
}

const http = {
  get:    (path)        => request('GET',    path),
  post:   (path, body)  => request('POST',   path, body),
  put:    (path, body)  => request('PUT',    path, body),
  patch:  (path, body)  => request('PATCH',  path, body),
  delete: (path)        => request('DELETE', path),
};

// ─── MOCK DATA ────────────────────────────────────────────────
const MOCK = {
  categorias: [
    { id: 1, nombre: 'Electrónica',       slug: 'electronica' },
    { id: 2, nombre: 'Ropa y Moda',       slug: 'ropa-moda' },
    { id: 3, nombre: 'Hogar',             slug: 'hogar' },
    { id: 4, nombre: 'Deportes',          slug: 'deportes' },
    { id: 5, nombre: 'Arte y Antigüedades', slug: 'arte-antiguedades' },
    { id: 6, nombre: 'Automotores',       slug: 'automotores' },
    { id: 7, nombre: 'Otros',             slug: 'otros' },
  ],

  productos: [
    { id: 1, titulo: 'MacBook Pro 14" M3', descripcion: 'Excelente estado, 16GB RAM.', condicion: 'USADO', categoriaId: 1, categoriaNombre: 'Electrónica', vendedorUsername: 'carlos_seller', createdAt: '2025-06-10T14:22:00Z' },
    { id: 2, titulo: 'Silla Gamer ErgoMax Pro', descripcion: 'Nunca usada, caja original.', condicion: 'NUEVO', categoriaId: 3, categoriaNombre: 'Hogar', vendedorUsername: 'maria_s', createdAt: '2025-06-12T09:00:00Z' },
    { id: 3, titulo: 'Guitarra Fender Stratocaster 1978', descripcion: 'Coleccionable original USA.', condicion: 'USADO', categoriaId: 5, categoriaNombre: 'Arte y Antigüedades', vendedorUsername: 'carlos_seller', createdAt: '2025-06-08T18:45:00Z' },
    { id: 4, titulo: 'iPhone 15 Pro Max 256GB', descripcion: 'Titanio negro, batería 98%.', condicion: 'REACONDICIONADO', categoriaId: 1, categoriaNombre: 'Electrónica', vendedorUsername: 'techseller01', createdAt: '2025-06-14T11:30:00Z' },
    { id: 5, titulo: 'Monitor LG UltraWide 34"', descripcion: 'QHD 144Hz. Panel IPS.', condicion: 'NUEVO', categoriaId: 1, categoriaNombre: 'Electrónica', vendedorUsername: 'maria_s', createdAt: '2025-06-15T08:00:00Z' },
  ],

  subastas: [
    { id: 1, productoNombre: 'MacBook Pro 14" M3', productoId: 1, vendedorUsername: 'carlos_seller', categoriaId: 1, categoriaNombre: 'Electrónica', precioBase: 150000, incrementoMinimo: 5000, montoActual: 178000, estado: 'ACTIVA', fechaInicio: new Date(Date.now() - 3600000*2).toISOString(), fechaFin: new Date(Date.now() + 3600000*22).toISOString(), totalPujas: 7, ganadorUsername: 'jperez' },
    { id: 2, productoNombre: 'Silla Gamer ErgoMax Pro', productoId: 2, vendedorUsername: 'maria_s', categoriaId: 3, categoriaNombre: 'Hogar', precioBase: 45000, incrementoMinimo: 2000, montoActual: null, estado: 'PUBLICADA', fechaInicio: new Date(Date.now() + 3600000*3).toISOString(), fechaFin: new Date(Date.now() + 3600000*75).toISOString(), totalPujas: 0, ganadorUsername: null },
    { id: 3, productoNombre: 'Guitarra Fender Stratocaster 1978', productoId: 3, vendedorUsername: 'carlos_seller', categoriaId: 5, categoriaNombre: 'Arte', precioBase: 320000, incrementoMinimo: 10000, montoActual: 385000, estado: 'ADJUDICADA', fechaInicio: new Date(Date.now() - 3600000*50).toISOString(), fechaFin: new Date(Date.now() - 3600000*2).toISOString(), totalPujas: 12, ganadorUsername: 'collector99', fechaAdjudicacion: new Date(Date.now() - 3600000*2).toISOString() },
    { id: 4, productoNombre: 'iPhone 15 Pro Max 256GB', productoId: 4, vendedorUsername: 'techseller01', categoriaId: 1, categoriaNombre: 'Electrónica', precioBase: 200000, incrementoMinimo: 8000, montoActual: null, estado: 'BORRADOR', fechaInicio: new Date(Date.now() + 3600000*48).toISOString(), fechaFin: new Date(Date.now() + 3600000*120).toISOString(), totalPujas: 0, ganadorUsername: null },
    { id: 5, productoNombre: 'Monitor LG UltraWide 34"', productoId: 5, vendedorUsername: 'maria_s', categoriaId: 1, categoriaNombre: 'Electrónica', precioBase: 80000, incrementoMinimo: 3000, montoActual: null, estado: 'CANCELADA', fechaInicio: new Date(Date.now() - 3600000*10).toISOString(), fechaFin: new Date(Date.now() + 3600000*38).toISOString(), totalPujas: 0, ganadorUsername: null },
  ],

  usuarios: [
    { id: 1, username: 'admin_root', email: 'admin@subastas.com', nombre: 'Admin', apellido: 'Sistema', roles: ['ADMIN'], bloqueado: false, createdAt: '2025-01-01T00:00:00Z' },
    { id: 2, username: 'carlos_seller', email: 'carlos@mail.com', nombre: 'Carlos', apellido: 'López', roles: ['USER','SELLER'], bloqueado: false, createdAt: '2025-03-10T10:00:00Z' },
    { id: 3, username: 'jperez', email: 'juan@mail.com', nombre: 'Juan', apellido: 'Pérez', roles: ['USER'], bloqueado: false, createdAt: '2025-04-01T08:00:00Z' },
    { id: 4, username: 'maria_s', email: 'maria@mail.com', nombre: 'María', apellido: 'Sosa', roles: ['USER','SELLER'], bloqueado: false, createdAt: '2025-04-15T12:00:00Z' },
    { id: 5, username: 'collector99', email: 'col@mail.com', nombre: 'Roberto', apellido: 'Díaz', roles: ['USER'], bloqueado: true, motivoBloqueo: 'Comportamiento sospechoso en pujas.', createdAt: '2025-05-01T09:00:00Z' },
  ],

  pujas: [
    { id: 1, subastaId: 1, usuarioUsername: 'jperez',    monto: 155000, fechaHora: new Date(Date.now()-3600000*1.5).toISOString(), estado: 'CONFIRMADA' },
    { id: 2, subastaId: 1, usuarioUsername: 'collector99', monto: 162000, fechaHora: new Date(Date.now()-3600000*1.2).toISOString(), estado: 'CONFIRMADA' },
    { id: 3, subastaId: 1, usuarioUsername: 'jperez',    monto: 170000, fechaHora: new Date(Date.now()-3600000).toISOString(), estado: 'CONFIRMADA' },
    { id: 4, subastaId: 1, usuarioUsername: 'techseller01', monto: 178000, fechaHora: new Date(Date.now()-1800000).toISOString(), estado: 'CONFIRMADA' },
  ],

  stats: {
    totalSubastas: 5,
    subastasActivas: 1,
    totalPujas: 23,
    montoTotal: 563000,
    usuariosActivos: 4,
    disputasPendientes: 0,
  },
};

// ─── DELAY MOCK (simula latencia de red) ─────────────────────
const delay = (ms = 300) => new Promise(r => setTimeout(r, ms));

// ─── API SERVICES ─────────────────────────────────────────────

/**
 * AUTH
 * POST /api/auth/register
 * POST /api/auth/login
 */
const AuthService = {
  async login(credentials) {
    if (API_CONFIG.USE_MOCK) {
      await delay(400);
      const user = MOCK.usuarios[0]; // Simula login como ADMIN
      const token = 'mock_jwt_token_' + Date.now();
      Auth.setToken(token);
      Auth.setUser(user);
      return { token, user };
    }
    // ── REAL ──
    const data = await http.post('/auth/login', credentials);
    Auth.setToken(data.token);
    Auth.setUser(data.user);
    return data;
  },

  logout() {
    Auth.removeToken();                  // Borra 'auth_token'
    localStorage.removeItem('sb_user'); // Borra 'auth_user' de raíz
    window.location.replace('login.html'); // Te saca derecho al login
  },
};

/**
 * CATEGORIAS
 * GET    /api/categorias          → listar
 * POST   /api/categorias          → crear (ADMIN)
 * DELETE /api/categorias/{id}     → eliminar (ADMIN)
 */
const CategoriaService = {
  async listar() {
    if (API_CONFIG.USE_MOCK) { await delay(200); return [...MOCK.categorias]; }
    return http.get('/categorias');
  },

  async crear(data) {
    if (API_CONFIG.USE_MOCK) {
      await delay(350);
      const nueva = { id: Date.now(), ...data };
      MOCK.categorias.push(nueva);
      return nueva;
    }
    return http.post('/categorias', data);
  },
};

/**
 * PRODUCTOS
 * GET    /api/productos            → listar (paginado)
 * GET    /api/productos/{id}       → detalle
 * POST   /api/productos            → crear (SELLER)
 * PUT    /api/productos/{id}       → editar (SELLER dueño)
 * DELETE /api/productos/{id}       → eliminar soft (SELLER/ADMIN)
 */
const ProductoService = {
  async listar(params = {}) {
    if (API_CONFIG.USE_MOCK) {
      await delay(280);
      let list = [...MOCK.productos];
      if (params.categoriaId) list = list.filter(p => p.categoriaId === +params.categoriaId);
      if (params.q) list = list.filter(p => p.titulo.toLowerCase().includes(params.q.toLowerCase()));
      return { content: list, totalElements: list.length, totalPages: 1 };
    }
    const qs = new URLSearchParams(params).toString();
    return http.get(`/productos${qs ? '?' + qs : ''}`);
  },

  async crear(data) {
    if (API_CONFIG.USE_MOCK) {
      await delay(400);
      const nuevo = { id: Date.now(), ...data, createdAt: new Date().toISOString(), vendedorUsername: Auth.getUser()?.username };
      MOCK.productos.push(nuevo);
      MOCK.stats.totalSubastas; // no incrementar aquí
      return nuevo;
    }
    return http.post('/productos', data);
  },

  async editar(id, data) {
    if (API_CONFIG.USE_MOCK) {
      await delay(350);
      const idx = MOCK.productos.findIndex(p => p.id === id);
      if (idx === -1) throw { status: 404, data: 'Producto no encontrado' };
      MOCK.productos[idx] = { ...MOCK.productos[idx], ...data };
      return MOCK.productos[idx];
    }
    return http.put(`/productos/${id}`, data);
  },

  async eliminar(id) {
    if (API_CONFIG.USE_MOCK) {
      await delay(300);
      const idx = MOCK.productos.findIndex(p => p.id === id);
      if (idx !== -1) MOCK.productos.splice(idx, 1);
      return {};
    }
    return http.delete(`/productos/${id}`);
  },
};

/**
 * SUBASTAS
 * GET    /api/subastas             → listar (filtros: estado, categoría, q)
 * GET    /api/subastas/{id}        → detalle
 * POST   /api/subastas             → crear BORRADOR (SELLER)
 * PUT    /api/subastas/{id}        → editar en BORRADOR (SELLER)
 * POST   /api/subastas/{id}/publicar   → BORRADOR → PUBLICADA
 * POST   /api/subastas/{id}/cancelar   → cancelar (SELLER sin pujas / ADMIN)
 * GET    /api/subastas/{id}/historial   → historial de estados
 */
const SubastaService = {
  async listar(params = {}) {
    if (API_CONFIG.USE_MOCK) {
      await delay(300);
      let list = [...MOCK.subastas];
      if (params.estado) list = list.filter(s => s.estado === params.estado);
      if (params.categoriaId) list = list.filter(s => s.categoriaId === +params.categoriaId);
      if (params.q) list = list.filter(s => s.productoNombre.toLowerCase().includes(params.q.toLowerCase()));
      return { content: list, totalElements: list.length };
    }
    const qs = new URLSearchParams(params).toString();
    return http.get(`/subastas${qs ? '?' + qs : ''}`);
  },

  async crear(data) {
    if (API_CONFIG.USE_MOCK) {
      await delay(450);
      const prod = MOCK.productos.find(p => p.id === +data.productoId);
      const cat  = MOCK.categorias.find(c => c.id === +data.categoriaId);
      const nueva = {
        id: Date.now(),
        productoNombre: prod?.titulo || 'Producto',
        productoId: +data.productoId,
        vendedorUsername: Auth.getUser()?.username,
        categoriaId: +data.categoriaId,
        categoriaNombre: cat?.nombre || '',
        precioBase: parseFloat(data.precioBase),
        incrementoMinimo: parseFloat(data.incrementoMinimo),
        descripcion: data.descripcion,
        montoActual: null,
        estado: 'BORRADOR',
        fechaInicio: data.fechaInicio,
        fechaFin: data.fechaFin,
        totalPujas: 0,
        ganadorUsername: null,
      };
      MOCK.subastas.unshift(nueva);
      MOCK.stats.totalSubastas++;
      return nueva;
    }
    return http.post('/subastas', data);
  },

  async publicar(id) {
    if (API_CONFIG.USE_MOCK) {
      await delay(300);
      const s = MOCK.subastas.find(s => s.id === id);
      if (s) s.estado = 'PUBLICADA';
      return s;
    }
    return http.post(`/subastas/${id}/publicar`);
  },

  async cancelar(id, motivo) {
    if (API_CONFIG.USE_MOCK) {
      await delay(350);
      const s = MOCK.subastas.find(s => s.id === id);
      if (s) { s.estado = 'CANCELADA'; s.motivoCancelacion = motivo; }
      return s;
    }
    return http.post(`/subastas/${id}/cancelar`, { motivo });
  },

  async historial(id) {
    if (API_CONFIG.USE_MOCK) {
      await delay(250);
      return [
        { id: 1, estadoAnterior: null, estadoNuevo: 'BORRADOR', usuarioUsername: 'carlos_seller', motivo: null, fecha: new Date(Date.now()-3600000*5).toISOString() },
        { id: 2, estadoAnterior: 'BORRADOR', estadoNuevo: 'PUBLICADA', usuarioUsername: 'carlos_seller', motivo: null, fecha: new Date(Date.now()-3600000*4).toISOString() },
        { id: 3, estadoAnterior: 'PUBLICADA', estadoNuevo: 'ACTIVA', usuarioUsername: null, motivo: 'Inicio automático', fecha: new Date(Date.now()-3600000*2).toISOString() },
      ];
    }
    return http.get(`/subastas/${id}/historial`);
  },
};

/**
 * PUJAS
 * POST   /api/subastas/{id}/pujas      → registrar puja (USER, transaccional)
 * GET    /api/subastas/{id}/pujas      → historial (SELLER/ADMIN según permisos)
 * GET    /api/pujas/mias               → mis pujas (autenticado)
 */
const PujaService = {
  async registrar(subastaId, monto) {
    if (API_CONFIG.USE_MOCK) {
      await delay(500);
      const s = MOCK.subastas.find(s => s.id === subastaId);
      if (!s || s.estado !== 'ACTIVA') throw { status: 400, data: 'La subasta no está activa.' };
      const minimo = s.montoActual ? s.montoActual + s.incrementoMinimo : s.precioBase;
      if (monto < minimo) throw { status: 400, data: `El monto mínimo es $${minimo.toLocaleString('es-AR')}.` };
      const puja = { id: Date.now(), subastaId, usuarioUsername: Auth.getUser()?.username, monto, fechaHora: new Date().toISOString(), estado: 'CONFIRMADA' };
      MOCK.pujas.push(puja);
      s.montoActual = monto;
      s.totalPujas++;
      return puja;
    }
    return http.post(`/subastas/${subastaId}/pujas`, { monto });
  },

  async listarPorSubasta(subastaId) {
    if (API_CONFIG.USE_MOCK) {
      await delay(250);
      return MOCK.pujas.filter(p => p.subastaId === subastaId).sort((a,b) => new Date(b.fechaHora)-new Date(a.fechaHora));
    }
    return http.get(`/subastas/${subastaId}/pujas`);
  },

  async misPujas() {
    if (API_CONFIG.USE_MOCK) {
      await delay(250);
      const me = Auth.getUser()?.username;
      return MOCK.pujas.filter(p => p.usuarioUsername === me);
    }
    return http.get('/pujas/mias');
  },
};

/**
 * USUARIOS
 * GET    /api/usuarios             → listar (ADMIN)
 * PUT    /api/usuarios/{id}/bloquear   → bloquear (ADMIN)
 * PUT    /api/usuarios/{id}/desbloquear → desbloquear (ADMIN)
 * PUT    /api/usuarios/{id}/roles      → asignar roles (ADMIN)
 */
const UsuarioService = {
  async listar() {
    if (API_CONFIG.USE_MOCK) { await delay(280); return [...MOCK.usuarios]; }
    return http.get('/usuarios');
  },

  async bloquear(id, motivo) {
    if (API_CONFIG.USE_MOCK) {
      await delay(300);
      const u = MOCK.usuarios.find(u => u.id === id);
      if (u) { u.bloqueado = true; u.motivoBloqueo = motivo; }
      return u;
    }
    return http.put(`/usuarios/${id}/bloquear`, { motivo });
  },

  async desbloquear(id) {
    if (API_CONFIG.USE_MOCK) {
      await delay(300);
      const u = MOCK.usuarios.find(u => u.id === id);
      if (u) { u.bloqueado = false; u.motivoBloqueo = null; }
      return u;
    }
    return http.put(`/usuarios/${id}/desbloquear`);
  },
};

/**
 * ESTADÍSTICAS / DASHBOARD
 * GET /api/admin/stats
 */
const StatsService = {
  async get() {
    if (API_CONFIG.USE_MOCK) {
      await delay(200);
      return {
        ...MOCK.stats,
        subastasActivas: MOCK.subastas.filter(s => s.estado === 'ACTIVA').length,
        totalSubastas: MOCK.subastas.length,
        totalUsuarios: MOCK.usuarios.length,
      };
    }
    return http.get('/admin/stats');
  },
};

// Exportar todo al scope global (alternativa: usar módulos ES con <script type="module">)
window.API = { Auth, AuthService, CategoriaService, ProductoService, SubastaService, PujaService, UsuarioService, StatsService, MOCK };
