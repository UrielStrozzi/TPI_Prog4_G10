/**
 * ui.js — Componentes de UI reutilizables
 * Toasts, Modales, Drawer, Formatters, Timers
 */

// ─── FORMATTERS ───────────────────────────────────────────────
const fmt = {
  money: (n) => n == null ? '—' : '$\u202F' + Number(n).toLocaleString('es-AR', { minimumFractionDigits: 0 }),
  date:  (iso) => {
    if (!iso) return '—';
    return new Date(iso).toLocaleString('es-AR', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit', timeZone: 'America/Argentina/Buenos_Aires' });
  },
  dateShort: (iso) => {
    if (!iso) return '—';
    return new Date(iso).toLocaleDateString('es-AR', { day: '2-digit', month: '2-digit', year: 'numeric' });
  },
  timeAgo: (iso) => {
    const diff = Date.now() - new Date(iso).getTime();
    const m = Math.floor(diff / 60000);
    if (m < 1)  return 'hace un momento';
    if (m < 60) return `hace ${m}m`;
    const h = Math.floor(m / 60);
    if (h < 24) return `hace ${h}h`;
    return `hace ${Math.floor(h / 24)}d`;
  },
};

// ─── BADGE HELPERS ────────────────────────────────────────────
function estadoBadge(estado) {
  const map = {
    BORRADOR:   ['badge-borrador',   'Borrador'],
    PUBLICADA:  ['badge-publicada',  'Publicada'],
    ACTIVA:     ['badge-activa',     'Activa'],
    FINALIZADA: ['badge-finalizada', 'Finalizada'],
    CANCELADA:  ['badge-cancelada',  'Cancelada'],
    ADJUDICADA: ['badge-adjudicada', 'Adjudicada'],
    EN_DISPUTA: ['badge-en_disputa', 'En disputa'],
  };
  const [cls, label] = map[estado] || ['badge-borrador', estado];
  return `<span class="badge ${cls}">${label}</span>`;
}

function condicionBadge(condicion) {
  const map = {
    NUEVO:          ['badge-nuevo', 'Nuevo'],
    USADO:          ['badge-usado', 'Usado'],
    REACONDICIONADO:['badge-reacondicionado', 'Reacond.'],
  };
  const [cls, label] = map[condicion] || ['badge-usado', condicion];
  return `<span class="badge ${cls}">${label}</span>`;
}

function rolBadge(rol) {
  return `<span class="badge badge-role">${rol}</span>`;
}

// ─── TOAST ───────────────────────────────────────────────────
const Toast = (() => {
  let container;

  function getContainer() {
    if (!container) {
      container = document.createElement('div');
      container.className = 'toast-container';
      document.body.appendChild(container);
    }
    return container;
  }

  const icons = { success: '✅', error: '❌', warning: '⚠️', info: 'ℹ️' };

  function show(type, title, msg = '', duration = 4000) {
    const el = document.createElement('div');
    el.className = `toast toast-${type}`;
    el.innerHTML = `
      <span class="toast-icon">${icons[type]}</span>
      <div>
        <div class="toast-title">${title}</div>
        ${msg ? `<div class="toast-msg">${msg}</div>` : ''}
      </div>`;

    getContainer().appendChild(el);

    requestAnimationFrame(() => {
      requestAnimationFrame(() => el.classList.add('show'));
    });

    setTimeout(() => {
      el.classList.remove('show');
      el.classList.add('hide');
      setTimeout(() => el.remove(), 400);
    }, duration);
  }

  return {
    success: (title, msg) => show('success', title, msg),
    error:   (title, msg) => show('error',   title, msg, 5000),
    warning: (title, msg) => show('warning', title, msg),
    info:    (title, msg) => show('info',    title, msg),
  };
})();

// ─── MODAL DE CONFIRMACIÓN ────────────────────────────────────
const Modal = (() => {
  let overlay, modal, onConfirmCb;

  function init() {
    overlay = document.getElementById('modal-overlay');
    modal   = document.getElementById('modal');
    if (!overlay) return;

    overlay.addEventListener('click', (e) => {
      if (e.target === overlay) close();
    });

    document.getElementById('modal-cancel-btn')?.addEventListener('click', close);
    document.getElementById('modal-confirm-btn')?.addEventListener('click', async () => {
      if (typeof onConfirmCb === 'function') {
        const btn = document.getElementById('modal-confirm-btn');
        btn.disabled = true;
        btn.textContent = 'Procesando...';
        try {
          await onConfirmCb();
          close();
        } catch(e) {
          Toast.error('Error', e?.data || 'Ocurrió un error.');
        } finally {
          btn.disabled = false;
        }
      }
    });
  }

  function open({ iconType = 'danger', iconEmoji = '🗑️', title, desc, confirmLabel = 'Confirmar', confirmClass = 'btn-danger', onConfirm, extraHtml = '' }) {
    document.getElementById('modal-icon').className  = `modal-icon ${iconType}`;
    document.getElementById('modal-icon').textContent = iconEmoji;
    document.getElementById('modal-title').textContent = title;
    document.getElementById('modal-desc').innerHTML  = desc;
    document.getElementById('modal-confirm-btn').textContent = confirmLabel;
    document.getElementById('modal-confirm-btn').className = `btn ${confirmClass}`;
    document.getElementById('modal-extra').innerHTML = extraHtml;
    onConfirmCb = onConfirm;
    overlay.classList.add('open');
  }

  function close() {
    overlay?.classList.remove('open');
    onConfirmCb = null;
  }

  return { init, open, close };
})();

// ─── DRAWER ───────────────────────────────────────────────────
const Drawer = (() => {
  let overlay, drawer, currentOnSubmit;

  function init() {
    overlay = document.getElementById('drawer-overlay');
    drawer  = document.getElementById('drawer');
    if (!overlay) return;

    overlay.addEventListener('click', (e) => {
      if (e.target === overlay) close();
    });

    document.getElementById('drawer-close-btn')?.addEventListener('click', close);
    document.getElementById('drawer-cancel-btn')?.addEventListener('click', close);

    document.getElementById('drawer-save-btn')?.addEventListener('click', async () => {
      if (typeof currentOnSubmit === 'function') {
        const btn = document.getElementById('drawer-save-btn');
        const original = btn.textContent;
        btn.disabled = true;
        btn.textContent = 'Guardando...';
        try {
          await currentOnSubmit();
        } catch(e) {
          const msg = typeof e?.data === 'string' ? e.data : (e?.data?.message || 'Ocurrió un error.');
          Toast.error('Error al guardar', msg);
        } finally {
          btn.disabled = false;
          btn.textContent = original;
        }
      }
    });

    document.addEventListener('keydown', (e) => {
      if (e.key === 'Escape') close();
    });
  }

  function open({ title, subtitle = '', saveLabel = 'Guardar', bodyHtml, onSubmit }) {
    document.getElementById('drawer-title').textContent    = title;
    document.getElementById('drawer-subtitle').textContent = subtitle;
    document.getElementById('drawer-save-btn').textContent = saveLabel;
    document.getElementById('drawer-body').innerHTML       = bodyHtml;
    currentOnSubmit = onSubmit;
    overlay.classList.add('open');
    drawer.classList.add('open');
    // Focus al primer input
    setTimeout(() => drawer.querySelector('input, select, textarea')?.focus(), 350);
  }

  function close() {
    overlay?.classList.remove('open');
    drawer?.classList.remove('open');
    currentOnSubmit = null;
  }

  function getField(name) {
    return drawer?.querySelector(`[name="${name}"]`)?.value?.trim();
  }

  function setFieldError(name, msg) {
    const el = drawer?.querySelector(`[name="${name}"]`);
    if (!el) return;
    el.classList.add('error');
    let errEl = el.parentElement.querySelector('.form-error');
    if (!errEl) { errEl = document.createElement('div'); errEl.className = 'form-error'; el.parentElement.appendChild(errEl); }
    errEl.textContent = msg;
    errEl.style.display = 'block';
  }

  function clearErrors() {
    drawer?.querySelectorAll('.form-control').forEach(el => el.classList.remove('error'));
    drawer?.querySelectorAll('.form-error').forEach(el => { el.style.display = 'none'; });
  }

  return { init, open, close, getField, setFieldError, clearErrors };
})();

// ─── TIMERS (countdown) ───────────────────────────────────────
const Timers = (() => {
  const running = new Map();

  function start(elementId, targetIso, onExpire) {
    if (running.has(elementId)) clearInterval(running.get(elementId));

    const el = document.getElementById(elementId);
    if (!el) return;

    const target = new Date(targetIso).getTime();

    function tick() {
      const diff = target - Date.now();
      if (diff <= 0) {
        el.textContent = 'Cerrada';
        el.className = 'live-timer closed';
        clearInterval(running.get(elementId));
        running.delete(elementId);
        if (typeof onExpire === 'function') onExpire();
        return;
      }

      const d = Math.floor(diff / 86400000);
      const h = Math.floor((diff % 86400000) / 3600000);
      const m = Math.floor((diff % 3600000) / 60000);
      const s = Math.floor((diff % 60000) / 1000);

      if (d > 0) el.textContent = `${d}d ${h}h ${m}m`;
      else if (h > 0) el.textContent = `${h}h ${m}m ${s}s`;
      else el.textContent = `${m}m ${s}s`;

      el.className = diff < 3600000 ? 'live-timer urgent' : 'live-timer';
    }

    tick();
    const id = setInterval(tick, 1000);
    running.set(elementId, id);
  }

  function stopAll() {
    running.forEach(id => clearInterval(id));
    running.clear();
  }

  return { start, stopAll };
})();

// ─── VALIDACIONES ─────────────────────────────────────────────
const Validate = {
  required(value, fieldName) {
    if (!value || value.toString().trim() === '') return `${fieldName} es requerido.`;
    return null;
  },
  positiveNumber(value, fieldName) {
    const n = parseFloat(value);
    if (isNaN(n) || n <= 0) return `${fieldName} debe ser un número mayor a 0.`;
    return null;
  },
  futureDate(value, fieldName) {
    if (!value) return `${fieldName} es requerido.`;
    if (new Date(value) <= new Date()) return `${fieldName} debe ser una fecha futura.`;
    return null;
  },
  dateAfter(start, end) {
    if (!start || !end) return null;
    if (new Date(end) <= new Date(start)) return 'La fecha de cierre debe ser posterior a la de inicio.';
    return null;
  },
};

// ─── TABS ─────────────────────────────────────────────────────
function initTabs(containerSelector) {
  const containers = document.querySelectorAll(containerSelector || '[data-tabs]');
  containers.forEach(container => {
    const buttons = container.querySelectorAll('.tab-btn');
    const panels  = container.querySelectorAll('.tab-panel');

    buttons.forEach(btn => {
      btn.addEventListener('click', () => {
        buttons.forEach(b => b.classList.remove('active'));
        panels.forEach(p => p.classList.remove('active'));
        btn.classList.add('active');
        const target = btn.dataset.tab;
        container.querySelector(`#${target}`)?.classList.add('active');
      });
    });
  });
}

// ─── LOADING STATES ───────────────────────────────────────────
function setLoading(containerId, isLoading, skeletonRows = 5) {
  const el = document.getElementById(containerId);
  if (!el) return;
  if (isLoading) {
    const cols = el.closest('table')?.querySelectorAll('thead th').length || 4;
    el.innerHTML = Array.from({ length: skeletonRows }, () => `
      <tr>${Array.from({ length: cols }, () => `
        <td><div style="height:14px;border-radius:4px;background:var(--bg-elevated);animation:pulse-badge 1.5s infinite;"></div></td>
      `).join('')}</tr>
    `).join('');
  }
}

// ─── SIDEBAR TOGGLE (mobile) ──────────────────────────────────
function initSidebarToggle() {
  const toggleBtn = document.getElementById('sidebar-toggle');
  const sidebar   = document.getElementById('app-sidebar');
  if (!toggleBtn || !sidebar) return;

  toggleBtn.addEventListener('click', () => {
    sidebar.classList.toggle('open');
  });

  // Cerrar al hacer click fuera
  document.addEventListener('click', (e) => {
    if (!sidebar.contains(e.target) && !toggleBtn.contains(e.target)) {
      sidebar.classList.remove('open');
    }
  });
}

// ─── NAVIGATION ───────────────────────────────────────────────
function setActivePage(pageId) {
  document.querySelectorAll('.page-section').forEach(p => p.classList.add('hidden'));
  document.getElementById(pageId)?.classList.remove('hidden');

  document.querySelectorAll('.sidebar-nav-item').forEach(item => {
    item.classList.toggle('active', item.dataset.page === pageId);
  });

  Timers.stopAll();
}

// Exportar al scope global
window.UI = { fmt, estadoBadge, condicionBadge, rolBadge, Toast, Modal, Drawer, Timers, Validate, initTabs, setLoading, initSidebarToggle, setActivePage };
