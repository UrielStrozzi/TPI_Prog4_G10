/**
 * app.js — Lógica principal de la aplicación
 * Cada sección tiene su propio objeto con init() y render()
 */

const App = (() => {

  // ─── DASHBOARD ─────────────────────────────────────────────
  const Dashboard = {
    async init() {
      await this.loadStats();
      await this.loadRecentAuctions();
    },

    async loadStats() {
      try {
        const stats = await API.StatsService.get();
        document.getElementById('stat-subastas').textContent = stats.totalSubastas ?? 0;
        document.getElementById('stat-activas').textContent  = stats.subastasActivas ?? 0;
        document.getElementById('stat-pujas').textContent    = stats.totalPujas ?? 0;
        document.getElementById('stat-monto').textContent    = UI.fmt.money(stats.montoTotal ?? 0).replace('$\u202F', '');
        document.getElementById('stat-usuarios').textContent = stats.totalUsuarios ?? 0;
        document.getElementById('stat-disputas').textContent = stats.disputasPendientes ?? 0;
      } catch(e) {
        UI.Toast.error('Error', 'No se pudieron cargar las estadísticas.');
      }
    },

    async loadRecentAuctions() {
      const tbody = document.getElementById('recent-auctions-body');
      if (!tbody) return;
      UI.setLoading('recent-auctions-body', true, 4);

      try {
        const data = await API.SubastaService.listar({ page: 0, size: 5 });
        const list = data.content || data;

        if (!list.length) {
          tbody.innerHTML = `<tr><td colspan="6">
            <div class="empty-state" style="padding:2rem">
              <div class="empty-state-icon">🏷️</div>
              <div class="empty-state-title">Sin subastas aún</div>
            </div></td></tr>`;
          return;
        }

        tbody.innerHTML = list.map(s => `
          <tr>
            <td class="td-mono">#${s.id}</td>
            <td class="td-title" title="${s.productoNombre}">${s.productoNombre}</td>
            <td>${UI.estadoBadge(s.estado)}</td>
            <td class="td-money">${UI.fmt.money(s.montoActual ?? s.precioBase)}</td>
            <td class="text-mono text-secondary">${s.totalPujas ?? 0}</td>
            <td>
              <div class="td-actions">
                <button class="btn btn-ghost btn-sm btn-icon" title="Ver pujas" onclick="App.Pujas.showForSubasta(${s.id}, '${s.productoNombre}')">📋</button>
                ${s.estado === 'BORRADOR' ? `<button class="btn btn-success btn-sm" onclick="App.Subastas.publicar(${s.id})">Publicar</button>` : ''}
                ${(s.estado === 'PUBLICADA' || s.estado === 'ACTIVA') ? `<button class="btn btn-danger btn-sm btn-icon" title="Cancelar" onclick="App.Subastas.confirmarCancelar(${s.id}, '${s.productoNombre}')">✕</button>` : ''}
              </div>
            </td>
          </tr>`).join('');

      } catch(e) {
        tbody.innerHTML = `<tr><td colspan="6" style="text-align:center;padding:2rem;color:var(--text-muted)">Error al cargar datos</td></tr>`;
      }
    }
  };

  // ─── PRODUCTOS ─────────────────────────────────────────────
  const Productos = {
    list: [],
    categorias: [],

    async init() {
      await this.loadCategorias();
      await this.render();
      this.bindFilters();
    },

    async loadCategorias() {
      try {
        const data = await API.CategoriaService.listar();
        console.log("DEBUG: Datos recibidos de Categorias:", data); // Esto nos dirá si llegan los datos
        
        // Si data es el array directo, perfecto. Si es un objeto, quizás debas usar data.content
        this.categorias = Array.isArray(data) ? data : (data.content || []); 
      } catch(e) { 
        console.error("Error al cargar categorías:", e);
        this.categorias = []; 
      }
    },

    async openFormNuevo() {
      // 1. Cargamos las categorías forzosamente al abrir
      await this.loadCategorias();
      
      // 2. Verificamos si tenemos datos
      console.log("Categorías disponibles antes de abrir:", this.categorias);

      UI.Drawer.open({
        title: 'Nuevo producto',
        subtitle: 'Completá los datos del producto a subastar',
        saveLabel: 'Crear producto',
        bodyHtml: this.formHtml(),
        onSubmit: () => this.submitNuevo(),
      });
    },

    async render(params = {}) {
      const tbody = document.getElementById('productos-body');
      if (!tbody) return;
      UI.setLoading('productos-body', true, 5);

      try {
        const data = await API.ProductoService.listar(params);
        this.list = data.content ?? data;

        document.getElementById('productos-count').textContent =
          `${this.list.length} producto${this.list.length !== 1 ? 's' : ''}`;

        if (!this.list.length) {
          tbody.innerHTML = `<tr><td colspan="6">
            <div class="empty-state">
              <div class="empty-state-icon">📦</div>
              <div class="empty-state-title">Sin productos</div>
              <div class="empty-state-desc">Agregá el primero para poder crear subastas.</div>
              <button class="btn btn-primary" onclick="App.Productos.openFormNuevo()">+ Nuevo producto</button>
            </div></td></tr>`;
          return;
        }

        tbody.innerHTML = this.list.map(p => `
          <tr>
            <td class="td-mono">#${p.id}</td>
            <td class="td-title" title="${p.nombre}">${p.nombre}</td>
            <td>${UI.condicionBadge(p.condicion)}</td>
            <td class="text-secondary text-sm">${p.categoriaNombre ?? '—'}</td>
            <td class="text-muted text-sm">${p.vendedorUsername}</td>
            <td>
              <div class="td-actions">
                <button class="btn btn-secondary btn-sm" onclick="App.Productos.openFormEditar(${p.id})">✏️ Editar</button>
                <button class="btn btn-ghost btn-sm btn-icon" title="Crear subasta desde este producto" onclick="App.Subastas.openFormNuevo(${p.id})">🏷️</button>
                <button class="btn btn-danger btn-sm btn-icon" title="Eliminar" onclick="App.Productos.confirmarEliminar(${p.id}, '${p.nombre.replace(/'/g, "\\'")}')">🗑️</button>
              </div>
            </td>
          </tr>`).join('');

      } catch(e) {
        tbody.innerHTML = `<tr><td colspan="6" style="text-align:center;padding:2rem;color:var(--text-muted)">Error al cargar productos</td></tr>`;
      }
    },

    bindFilters() {
      const searchInput = document.getElementById('productos-search');
      const catFilter   = document.getElementById('productos-cat-filter');

      let debounceTimer;
      const doFilter = () => {
        const q   = searchInput?.value?.trim();
        const cat = catFilter?.value;
        this.render({ q: q || undefined, categoriaId: cat || undefined });
      };

      searchInput?.addEventListener('input', () => {
        clearTimeout(debounceTimer);
        debounceTimer = setTimeout(doFilter, 350);
      });

      catFilter?.addEventListener('change', doFilter);

      // Poblar select de categorias en filtro
      if (catFilter) {
        catFilter.innerHTML = `<option value="">Todas las categorías</option>` +
          this.categorias.map(c => `<option value="${c.id}">${c.nombre}</option>`).join('');
      }
    },

    catOptions(selected = '') {
      return `<option value="">Seleccioná una categoría</option>` +
        this.categorias.map(c => `<option value="${c.id}" ${c.id == selected ? 'selected' : ''}>${c.nombre}</option>`).join('');
    },

    openFormNuevo() {
      UI.Drawer.open({
        title: 'Nuevo producto',
        subtitle: 'Completá los datos del producto a subastar',
        saveLabel: 'Crear producto',
        bodyHtml: this.formHtml(),
        onSubmit: () => this.submitNuevo(),
      });
    },

    openFormEditar(id) {
      const p = this.list.find(p => p.id === id);
      if (!p) return;
      UI.Drawer.open({
        title: 'Editar producto',
        subtitle: `#${p.id} · ${p.nombre}`,
        saveLabel: 'Guardar cambios',
        bodyHtml: this.formHtml(p),
        onSubmit: () => this.submitEditar(id),
      });
    },

    formHtml(p = {}) {
      return `
        <div class="form-section-title">Información básica</div>

        <div class="form-group">
          <label class="form-label">Título del producto <span class="required">*</span></label>
          <input name="nombre" class="form-control" type="text" value="${p.nombre ?? ''}" placeholder="Ej: MacBook Pro 14 pulgadas M3 2023" maxlength="200">
          <div class="form-error">El título es requerido.</div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label class="form-label">Categoría <span class="required">*</span></label>
            <select name="categoriaId" class="form-control">
              ${this.catOptions(p.categoriaId)}
            </select>
            <div class="form-error">Seleccioná una categoría.</div>
          </div>
          <div class="form-group">
            <label class="form-label">Condición <span class="required">*</span></label>
            <select name="condicion" class="form-control">
              <option value="NUEVO"           ${p.condicion === 'NUEVO'           ? 'selected' : ''}>Nuevo</option>
              <option value="USADO"           ${p.condicion === 'USADO'           ? 'selected' : ''}>Usado</option>
              <option value="REACONDICIONADO" ${p.condicion === 'REACONDICIONADO' ? 'selected' : ''}>Reacondicionado</option>
            </select>
          </div>
        </div>

        <div class="form-group">
          <label class="form-label">Descripción</label>
          <textarea name="descripcion" class="form-control" rows="4" placeholder="Describí el estado, características destacadas, accesorios incluidos...">${p.descripcion ?? ''}</textarea>
          <div class="form-hint">Cuanto más detallada, más confianza generás en los compradores.</div>
        </div>`;
    },

    async submitNuevo() {
      UI.Drawer.clearErrors();
      const nombre      = UI.Drawer.getField('nombre');
      const categoriaId = UI.Drawer.getField('categoriaId');
      const condicion   = UI.Drawer.getField('condicion');
      const descripcion = UI.Drawer.getField('descripcion');

      let valid = true;
      if (!nombre) { UI.Drawer.setFieldError('nombre', 'El título es requerido.'); valid = false; }
      if (!categoriaId) { UI.Drawer.setFieldError('categoriaId', 'Seleccioná una categoría.'); valid = false; }
      if (!valid) throw { data: 'Corregí los campos marcados.' };

      await API.ProductoService.crear({ nombre, categoriaId, condicion, descripcion });
      UI.Drawer.close();
      UI.Toast.success('Producto creado', `"${nombre}" fue agregado correctamente.`);
      await this.render();
    },

    async submitEditar(id) {
      UI.Drawer.clearErrors();
      const nombre      = UI.Drawer.getField('nombre');
      const categoriaId = UI.Drawer.getField('categoriaId');
      const condicion   = UI.Drawer.getField('condicion');
      const descripcion = UI.Drawer.getField('descripcion');

      if (!nombre) { UI.Drawer.setFieldError('nombre', 'El título es requerido.'); throw { data: '' }; }

      await API.ProductoService.editar(id, { nombre, categoriaId, condicion, descripcion });
      UI.Drawer.close();
      UI.Toast.success('Producto actualizado', 'Los cambios fueron guardados.');
      await this.render();
    },

    confirmarEliminar(id, nombre) {
      UI.Modal.open({
        iconType: 'danger', iconEmoji: '🗑️',
        title: 'Eliminar producto',
        desc: `¿Estás seguro de eliminar <strong>"${nombre}"</strong>? Esta acción no se puede deshacer.`,
        confirmLabel: 'Sí, eliminar', confirmClass: 'btn-danger',
        onConfirm: async () => {
          await API.ProductoService.eliminar(id);
          UI.Toast.success('Eliminado', `"${nombre}" fue eliminado.`);
          await this.render();
        }
      });
    },
  };

  // ─── SUBASTAS ──────────────────────────────────────────────
  const Subastas = {
    list: [],

    async init() {
      await this.render();
      this.bindFilters();
    },

    async render(params = {}) {
      const tbody = document.getElementById('subastas-body');
      if (!tbody) return;
      UI.setLoading('subastas-body', true, 5);

      try {
        const data = await API.SubastaService.listar(params);
        this.list = data.content ?? data;

        document.getElementById('subastas-count').textContent =
          `${this.list.length} subasta${this.list.length !== 1 ? 's' : ''}`;

        if (!this.list.length) {
          tbody.innerHTML = `<tr><td colspan="8">
            <div class="empty-state">
              <div class="empty-state-icon">🏷️</div>
              <div class="empty-state-title">Sin subastas</div>
              <div class="empty-state-desc">Creá la primera subasta desde un producto existente.</div>
              <button class="btn btn-primary" onclick="App.Subastas.openFormNuevo()">+ Nueva subasta</button>
            </div></td></tr>`;
          return;
        }

        tbody.innerHTML = this.list.map((s, i) => `
        <tr>
          <td class="td-mono">#${s.id}</td>
          <td class="td-title" title="${s.productoNombre ?? ''}">${s.productoNombre ?? '—'}</td>
          <td>${UI.estadoBadge(s.estado)}</td>
          <td class="td-money">${UI.fmt.money(s.montoActual ?? s.precioBase)}</td>
          <td class="text-mono text-secondary text-sm">${s.totalPujas ?? 0}</td>
          <td>${s.estado === 'ACTIVA'
                  ? `<span id="timer-${s.id}" class="live-timer">...</span>`
                  : `<span class="text-muted text-sm">${UI.fmt.date(s.fechaFin)}</span>`}
          </td>
          <td class="text-muted text-sm">${s.vendedorUsername ?? '—'}</td>
          <td>
            <div class="td-actions">
              ${s.estado === 'BORRADOR'
                  ? `<button class="btn btn-success btn-sm" onclick="App.Subastas.publicar(${s.id})">Publicar</button>`
                  : ''}
              <button class="btn btn-ghost btn-sm btn-icon" title="Ver pujas"
                onclick="App.Pujas.showForSubasta(${s.id}, '${(s.productoNombre ?? '').replace(/'/g, "\\'")}')">📋</button>
              <button class="btn btn-ghost btn-sm btn-icon" title="Historial de estados"
                onclick="App.Subastas.showHistorial(${s.id}, '${(s.productoNombre ?? '').replace(/'/g, "\\'")}')">🕐</button>
              ${(s.estado === 'PUBLICADA' || s.estado === 'ACTIVA' || s.estado === 'BORRADOR')
                  ? `<button class="btn btn-danger btn-sm btn-icon" title="Cancelar"
                    onclick="App.Subastas.confirmarCancelar(${s.id}, '${(s.productoNombre ?? '').replace(/'/g, "\\'")}')">✕</button>`
                  : ''}
            </div>
          </td>
        </tr>`).join('');

        // Timer — usar fechaFin en lugar de fechaFin
        this.list.forEach(s => {
          if (s.estado === 'ACTIVA') {
            UI.Timers.start(`timer-${s.id}`, s.fechaFin);
          }
        });

      } catch(e) {
        tbody.innerHTML = `<tr><td colspan="8" style="text-align:center;padding:2rem;color:var(--text-muted)">Error al cargar subastas</td></tr>`;
      }
    },

    bindFilters() {
      const searchInput = document.getElementById('subastas-search');
      const estadoFilter = document.getElementById('subastas-estado-filter');
      let debounce;
      const doFilter = () => {
        const q = searchInput?.value?.trim();
        const estado = estadoFilter?.value;
        this.render({ q: q || undefined, estado: estado || undefined });
      };
      searchInput?.addEventListener('input', () => { clearTimeout(debounce); debounce = setTimeout(doFilter, 350); });
      estadoFilter?.addEventListener('change', doFilter);
    },

    openFormNuevo(preselectedProductoId = null) {
      const productos = API.MOCK.productos;
      const categorias = API.MOCK.categorias;

      // Fecha default: inicio en 1h, cierre en 4 días
      const defaultInicio = new Date(Date.now() + 3600000);
      const defaultCierre = new Date(Date.now() + 3600000 * 97);
      const toLocal = (d) => new Date(d.getTime() - d.getTimezoneOffset()*60000).toISOString().slice(0,16);

      UI.Drawer.open({
        title: 'Nueva subasta',
        subtitle: 'Configurá todos los parámetros de la subasta',
        saveLabel: 'Crear subasta',
        bodyHtml: `
          <div class="form-section-title">Producto a subastar</div>

          <div class="form-group">
            <label class="form-label">Producto <span class="required">*</span></label>
            <select name="productoId" class="form-control" id="sb-producto-select">
              <option value="">Seleccioná un producto...</option>
              ${productos.map(p => `<option value="${p.id}" ${p.id == preselectedProductoId ? 'selected' : ''}>${p.nombre} (${p.condicion})</option>`).join('')}
            </select>
            <div class="form-error">Seleccioná un producto.</div>
            <div class="form-hint">¿No ves el producto? Crealo primero desde la sección Productos.</div>
          </div>

          <div class="form-group">
            <label class="form-label">Categoría <span class="required">*</span></label>
            <select name="categoriaId" class="form-control">
              <option value="">Seleccioná una categoría</option>
              ${categorias.map(c => `<option value="${c.id}">${c.nombre}</option>`).join('')}
            </select>
            <div class="form-error">Seleccioná una categoría.</div>
          </div>

          <hr class="form-divider">
          <div class="form-section-title">Configuración económica</div>

          <div class="form-row">
            <div class="form-group">
              <label class="form-label">Precio base <span class="required">*</span></label>
              <div class="input-group">
                <span class="input-prefix">$</span>
                <input name="precioBase" class="form-control" type="number" min="1" step="0.01" placeholder="0.00">
              </div>
              <div class="form-error">Ingresá un precio válido mayor a 0.</div>
              <div class="form-hint">Oferta mínima para la primera puja.</div>
            </div>
            <div class="form-group">
              <label class="form-label">Incremento mínimo <span class="required">*</span></label>
              <div class="input-group">
                <span class="input-prefix">$</span>
                <input name="incrementoMinimo" class="form-control" type="number" min="1" step="0.01" placeholder="0.00">
              </div>
              <div class="form-error">Ingresá un incremento válido mayor a 0.</div>
              <div class="form-hint">Diferencia mínima entre pujas consecutivas.</div>
            </div>
          </div>

          <hr class="form-divider">
          <div class="form-section-title">Horario de la subasta (UTC — el servidor lo maneja)</div>

          <div class="form-row">
            <div class="form-group">
              <label class="form-label">Fecha y hora de inicio <span class="required">*</span></label>
              <input name="fechaInicio" class="form-control" type="datetime-local" value="${toLocal(defaultInicio)}">
              <div class="form-error">La fecha de inicio debe ser futura.</div>
            </div>
            <div class="form-group">
              <label class="form-label">Fecha y hora de cierre <span class="required">*</span></label>
              <input name="fechaFin" class="form-control" type="datetime-local" value="${toLocal(defaultCierre)}">
              <div class="form-error">El cierre debe ser posterior al inicio.</div>
            </div>
          </div>

          <hr class="form-divider">
          <div class="form-section-title">Descripción adicional</div>
          <div class="form-group">
            <label class="form-label">Descripción de la subasta</label>
            <textarea name="descripcion" class="form-control" rows="3" placeholder="Condiciones, métodos de pago, envío, etc."></textarea>
          </div>

          <div style="background:var(--accent-dim);border:1px solid var(--accent);border-radius:var(--radius-sm);padding:var(--space-3) var(--space-4);font-size:var(--text-xs);color:var(--accent);margin-top:var(--space-4);">
            ℹ️ La subasta se crea en estado <strong>BORRADOR</strong>. Deberás publicarla manualmente para que esté visible. El sistema la activará y cerrará automáticamente según las fechas configuradas.
          </div>`,
        onSubmit: () => this.submitNuevo(),
      });
    },

    async submitNuevo() {
      UI.Drawer.clearErrors();

      const productoId      = UI.Drawer.getField('productoId');
      const categoriaId     = UI.Drawer.getField('categoriaId');
      const precioBase      = UI.Drawer.getField('precioBase');
      const incrementoMin   = UI.Drawer.getField('incrementoMinimo');
      const fechaInicio     = UI.Drawer.getField('fechaInicio');
      const fechaFin     = UI.Drawer.getField('fechaFin');
      const descripcion     = UI.Drawer.getField('descripcion');

      let valid = true;
      if (!productoId) { UI.Drawer.setFieldError('productoId', 'Seleccioná un producto.'); valid = false; }
      if (!categoriaId) { UI.Drawer.setFieldError('categoriaId', 'Seleccioná una categoría.'); valid = false; }
      const errPrecio = UI.Validate.positiveNumber(precioBase, 'Precio base');
      if (errPrecio) { UI.Drawer.setFieldError('precioBase', errPrecio); valid = false; }
      const errIncr = UI.Validate.positiveNumber(incrementoMin, 'Incremento mínimo');
      if (errIncr) { UI.Drawer.setFieldError('incrementoMinimo', errIncr); valid = false; }
      const errInicio = UI.Validate.futureDate(fechaInicio, 'Fecha de inicio');
      if (errInicio) { UI.Drawer.setFieldError('fechaInicio', errInicio); valid = false; }
      const errCierre = UI.Validate.dateAfter(fechaInicio, fechaFin);
      if (errCierre) { UI.Drawer.setFieldError('fechaFin', errCierre); valid = false; }
      if (!valid) throw { data: 'Corregí los campos marcados.' };

      await API.SubastaService.crear({
        productoId, categoriaId, descripcion,
        precioBase: parseFloat(precioBase),
        incrementoMinimo: parseFloat(incrementoMin),
        fechaInicio: new Date(fechaInicio).toISOString(),
        fechaFin: new Date(fechaFin).toISOString(),
      });

      UI.Drawer.close();
      UI.Toast.success('Subasta creada en borrador', 'Podés publicarla cuando estés listo.');
      await this.render();
    },

    async publicar(id) {
      UI.Modal.open({
        iconType: 'success', iconEmoji: '📢',
        title: 'Publicar subasta',
        desc: 'La subasta pasará a estado <strong>Publicada</strong> y será visible para todos los usuarios. El sistema la activará automáticamente cuando llegue la fecha de inicio.',
        confirmLabel: 'Publicar ahora', confirmClass: 'btn-success',
        onConfirm: async () => {
          await API.SubastaService.publicar(id);
          UI.Toast.success('Subasta publicada', 'Se activará automáticamente en la fecha configurada.');
          await this.render();
          await Dashboard.loadRecentAuctions();
        }
      });
    },

    confirmarCancelar(id, nombre) {
      UI.Modal.open({
        iconType: 'danger', iconEmoji: '⛔',
        title: 'Cancelar subasta',
        desc: `¿Cancelar la subasta de <strong>"${nombre}"</strong>? Ingresá el motivo:`,
        confirmLabel: 'Cancelar subasta', confirmClass: 'btn-danger',
        extraHtml: `<div class="modal-textarea"><textarea id="motivo-cancelacion" class="form-control" rows="3" placeholder="Motivo de cancelación (requerido)..." style="margin-top:var(--space-3)"></textarea></div>`,
        onConfirm: async () => {
          const motivo = document.getElementById('motivo-cancelacion')?.value?.trim();
          if (!motivo) { UI.Toast.error('Motivo requerido', 'Ingresá el motivo de cancelación.'); throw { data: '' }; }
          await API.SubastaService.cancelar(id, motivo);
          UI.Toast.warning('Subasta cancelada', nombre);
          await this.render();
        }
      });
    },

    async showHistorial(id, nombre) {
      try {
        const historial = await API.SubastaService.historial(id);

        UI.Drawer.open({
          title: 'Historial de estados',
          subtitle: nombre,
          saveLabel: null,
          bodyHtml: `
            <div style="display:flex;flex-direction:column;gap:0;">
              ${historial.length === 0
                ? '<div class="empty-state"><div class="empty-state-title">Sin historial</div></div>'
                : historial.map((h, i) => `
                  <div style="display:flex;gap:var(--space-4);padding:var(--space-4) 0;${i < historial.length-1 ? 'border-bottom:1px solid var(--border-subtle)' : ''}">
                    <div style="display:flex;flex-direction:column;align-items:center;gap:0;flex-shrink:0">
                      <div style="width:32px;height:32px;border-radius:50%;background:var(--bg-elevated);border:2px solid var(--border-default);display:flex;align-items:center;justify-content:center;font-size:14px;">
                        ${h.usuarioUsername ? '👤' : '🤖'}
                      </div>
                      ${i < historial.length-1 ? '<div style="width:2px;flex:1;background:var(--border-subtle);margin-top:4px;min-height:20px;"></div>' : ''}
                    </div>
                    <div>
                      <div style="display:flex;align-items:center;gap:var(--space-2);flex-wrap:wrap;margin-bottom:4px;">
                        ${h.estadoAnterior ? UI.estadoBadge(h.estadoAnterior) + ' <span style="color:var(--text-muted);font-size:12px;">→</span>' : ''}
                        ${UI.estadoBadge(h.estadoNuevo)}
                      </div>
                      <div style="font-size:var(--text-xs);color:var(--text-muted);">
                        ${h.usuarioUsername ? `<strong style="color:var(--text-secondary)">${h.usuarioUsername}</strong>` : '<em>Sistema (automático)</em>'}
                        · ${UI.fmt.date(h.fecha)}
                      </div>
                      ${h.motivo ? `<div style="font-size:var(--text-sm);color:var(--text-secondary);margin-top:4px;">"${h.motivo}"</div>` : ''}
                    </div>
                  </div>`).join('')}
            </div>`,
          onSubmit: null,
        });

        // Ocultar botón guardar
        document.getElementById('drawer-save-btn').style.display = 'none';
        document.getElementById('drawer-cancel-btn').textContent = 'Cerrar';

      } catch(e) {
        UI.Toast.error('Error', 'No se pudo cargar el historial.');
      }
    },
  };

  // ─── PUJAS ─────────────────────────────────────────────────
  const Pujas = {
    async showForSubasta(id, nombre) {
      try {
        const pujas = await API.PujaService.listarPorSubasta(id);

        UI.Drawer.open({
          title: 'Historial de pujas',
          subtitle: nombre,
          bodyHtml: `
            ${pujas.length === 0
              ? '<div class="empty-state"><div class="empty-state-icon">📋</div><div class="empty-state-title">Sin pujas registradas</div></div>'
              : `<div class="table-wrap">
                  <table>
                    <thead>
                      <tr>
                        <th>#</th>
                        <th>Ofertante</th>
                        <th>Monto</th>
                        <th>Fecha y hora</th>
                        <th>Estado</th>
                      </tr>
                    </thead>
                    <tbody>
                      ${pujas.map((p, i) => `
                        <tr>
                          <td class="td-mono">${pujas.length - i}</td>
                          <td style="font-weight:${i===0?'700':'400'};color:${i===0?'var(--accent)':'inherit'}">
                            ${i===0 ? '🏆 ' : ''}${p.usuarioUsername}
                          </td>
                          <td class="td-money" style="color:${i===0?'var(--accent)':'inherit'}">${UI.fmt.money(p.monto)}</td>
                          <td class="td-mono">${UI.fmt.date(p.fechaHora)}</td>
                          <td><span class="badge" style="background:var(--success-dim);color:var(--success);border-color:var(--success)">✓</span></td>
                        </tr>`).join('')}
                    </tbody>
                  </table>
                </div>`}`,
          onSubmit: null,
        });

        document.getElementById('drawer-save-btn').style.display = 'none';
        document.getElementById('drawer-cancel-btn').textContent = 'Cerrar';

      } catch(e) {
        UI.Toast.error('Error', 'No se pudieron cargar las pujas.');
      }
    },

    async initMisPujas() {
      const tbody = document.getElementById('mis-pujas-body');
      if (!tbody) return;
      UI.setLoading('mis-pujas-body', true, 4);

      try {
        const pujas = await API.PujaService.misPujas();

        if (!pujas.length) {
          tbody.innerHTML = `<tr><td colspan="5">
            <div class="empty-state">
              <div class="empty-state-icon">📋</div>
              <div class="empty-state-title">Sin pujas registradas</div>
              <div class="empty-state-desc">Tus pujas aparecerán acá una vez que ofertés en una subasta activa.</div>
            </div></td></tr>`;
          return;
        }

        tbody.innerHTML = pujas.map(p => `
          <tr>
            <td class="td-mono">#${p.id}</td>
            <td class="td-money">${UI.fmt.money(p.monto)}</td>
            <td class="td-mono text-muted">${UI.fmt.date(p.fechaHora)}</td>
            <td class="td-mono text-muted">Subasta #${p.subastaId}</td>
            <td><span class="badge" style="background:var(--success-dim);color:var(--success);border-color:var(--success)">Confirmada</span></td>
          </tr>`).join('');

      } catch(e) {
        tbody.innerHTML = `<tr><td colspan="5" style="text-align:center;color:var(--text-muted);padding:2rem">Error al cargar pujas</td></tr>`;
      }
    }
  };

  // ─── USUARIOS ──────────────────────────────────────────────
  const Usuarios = {
    list: [],

    async init() {
      await this.render();
      this.bindFilters();
    },

    async render(params = {}) {
      const tbody = document.getElementById('usuarios-body');
      if (!tbody) return;
      UI.setLoading('usuarios-body', true, 5);

      try {
        this.list = await API.UsuarioService.listar();
        let filtered = [...this.list];

        if (params.q) {
          const q = params.q.toLowerCase();
          filtered = filtered.filter(u => u.username.toLowerCase().includes(q) || u.email.toLowerCase().includes(q));
        }
        if (params.estado === 'bloqueado') filtered = filtered.filter(u => u.bloqueado);
        if (params.estado === 'activo')    filtered = filtered.filter(u => !u.bloqueado);

        document.getElementById('usuarios-count').textContent = `${filtered.length} usuario${filtered.length !== 1 ? 's' : ''}`;

        if (!filtered.length) {
          tbody.innerHTML = `<tr><td colspan="6"><div class="empty-state"><div class="empty-state-title">Sin resultados</div></div></td></tr>`;
          return;
        }

        tbody.innerHTML = filtered.map(u => `
          <tr>
            <td class="td-mono">#${u.id}</td>
            <td>
              <div style="display:flex;align-items:center;gap:var(--space-3);">
                <div style="width:32px;height:32px;border-radius:50%;background:var(--accent-dim);border:1px solid var(--accent);display:flex;align-items:center;justify-content:center;font-size:13px;font-weight:700;color:var(--accent);flex-shrink:0;">
                  ${(u.nombre?.[0] ?? u.username[0]).toUpperCase()}
                </div>
                <div>
                  <div style="font-weight:600;">${u.username}</div>
                  <div style="font-size:var(--text-xs);color:var(--text-muted);">${u.nombre ?? ''} ${u.apellido ?? ''}</div>
                </div>
              </div>
            </td>
            <td class="text-muted text-sm">${u.email}</td>
            <td>
              <div style="display:flex;gap:4px;flex-wrap:wrap;">
                ${(u.roles ?? []).map(r => UI.rolBadge(r)).join('')}
              </div>
            </td>
            <td>
              ${u.bloqueado
                ? `<span class="badge badge-cancelada">Bloqueado</span>`
                : `<span class="badge badge-activa">Activo</span>`}
            </td>
            <td>
              <div class="td-actions">
                ${u.bloqueado
                  ? `<button class="btn btn-success btn-sm" onclick="App.Usuarios.desbloquear(${u.id}, '${u.username}')">✓ Desbloquear</button>`
                  : `<button class="btn btn-danger btn-sm" onclick="App.Usuarios.confirmarBloquear(${u.id}, '${u.username}')">⊘ Bloquear</button>`}
              </div>
            </td>
          </tr>`).join('');

      } catch(e) {
        tbody.innerHTML = `<tr><td colspan="6" style="text-align:center;color:var(--text-muted);padding:2rem">Error al cargar usuarios</td></tr>`;
      }
    },

    bindFilters() {
      const search = document.getElementById('usuarios-search');
      const estado = document.getElementById('usuarios-estado-filter');
      let debounce;
      const doFilter = () => {
        this.render({ q: search?.value?.trim(), estado: estado?.value });
      };
      search?.addEventListener('input', () => { clearTimeout(debounce); debounce = setTimeout(doFilter, 350); });
      estado?.addEventListener('change', doFilter);
    },

    confirmarBloquear(id, username) {
      UI.Modal.open({
        iconType: 'danger', iconEmoji: '⊘',
        title: `Bloquear a ${username}`,
        desc: 'El usuario no podrá iniciar sesión ni realizar pujas. Ingresá el motivo:',
        confirmLabel: 'Bloquear usuario', confirmClass: 'btn-danger',
        extraHtml: `<div class="modal-textarea"><textarea id="motivo-bloqueo" class="form-control" rows="2" placeholder="Motivo del bloqueo..." style="margin-top:var(--space-3)"></textarea></div>`,
        onConfirm: async () => {
          const motivo = document.getElementById('motivo-bloqueo')?.value?.trim();
          if (!motivo) { UI.Toast.error('Motivo requerido', 'Ingresá el motivo del bloqueo.'); throw { data: '' }; }
          await API.UsuarioService.bloquear(id, motivo);
          UI.Toast.warning('Usuario bloqueado', username);
          await this.render();
        }
      });
    },

    async desbloquear(id, username) {
      UI.Modal.open({
        iconType: 'success', iconEmoji: '✓',
        title: `Desbloquear a ${username}`,
        desc: '¿Confirmás que querés restaurar el acceso a este usuario?',
        confirmLabel: 'Desbloquear', confirmClass: 'btn-success',
        onConfirm: async () => {
          await API.UsuarioService.desbloquear(id);
          UI.Toast.success('Usuario desbloqueado', username);
          await this.render();
        }
      });
    },
  };

  // ─── INIT GLOBAL ───────────────────────────────────────────
  async function init() {
    // Inicializar componentes globales
    UI.Modal.init();
    UI.Drawer.init();
    UI.initSidebarToggle();

    // Navegación por sidebar
    document.querySelectorAll('.sidebar-nav-item[data-page]').forEach(item => {
      item.addEventListener('click', async () => {
        const page = item.dataset.page;
        UI.setActivePage(page);

        // Restaurar drawer-save-btn si fue ocultado
        const saveBtn = document.getElementById('drawer-save-btn');
        if (saveBtn) saveBtn.style.display = '';

        // Cargar datos de la sección activada
        if (page === 'page-dashboard')  await Dashboard.init();
        if (page === 'page-productos')  await Productos.init();
        if (page === 'page-subastas')   await Subastas.init();
        if (page === 'page-pujas')      await Pujas.initMisPujas();
        if (page === 'page-usuarios')   await Usuarios.init();
      });
    });

    // Botones "nuevo" en page headers
    document.getElementById('btn-nuevo-producto')?.addEventListener('click', () => Productos.openFormNuevo());
    document.getElementById('btn-nueva-subasta')?.addEventListener('click', () => Subastas.openFormNuevo());

    // Cargar dashboard por defecto
    await Dashboard.init();
  }

  return { init, Dashboard, Productos, Subastas, Pujas, Usuarios };

})();

// Arrancar cuando el DOM esté listo
document.addEventListener('DOMContentLoaded', () => App.init());
