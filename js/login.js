/**
 * =============================================================
 * SUBASTAS ONLINE — auth.js
 * Maneja Login y Registro · Preparado para Spring Boot
 * =============================================================
 *
 * INTEGRACIÓN SPRING BOOT
 * ─────────────────────────
 * Ajustá API_BASE_URL a tu servidor.
 * Los endpoints esperados son:
 *   POST /api/auth/login     → { token, user }
 *   POST /api/auth/register  → { message } | 201
 *   GET  /api/users/check-username?username=X → { available: bool }
 *
 * El token JWT se guarda en localStorage como "auth_token".
 * Podés cambiar el storage (sessionStorage, cookie HttpOnly) según tu estrategia.
 * =============================================================
 */

/* ── Configuración global ─────────────────────────────── */
const CONFIG = {
  API_BASE_URL: "http://localhost:8080/api",   // ← ajustá a tu servidor
  TOKEN_KEY:    "sb_token",
  USER_KEY:     "sb_user",
  REDIRECT_AFTER_LOGIN:    "/index.html",
  REDIRECT_AFTER_REGISTER: null, // null = auto-login y redirige
};

/* ── Helpers de API ───────────────────────────────────── */

/**
 * Realiza un fetch a la API con headers JSON.
 * @param {string} path  - Ruta relativa, ej. "/auth/login"
 * @param {object} opts  - Opciones para fetch (method, body, etc.)
 * @returns {Promise<{ ok: boolean, status: number, data: any }>}
 */
async function apiFetch(path, opts = {}) {
  const url = `${CONFIG.API_BASE_URL}${path}`;
  const token = localStorage.getItem(CONFIG.TOKEN_KEY);

  const headers = {
    "Content-Type": "application/json",
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
    ...(opts.headers || {}),
  };

  try {
    const res = await fetch(url, { ...opts, headers });
    let data = null;
    const contentType = res.headers.get("Content-Type") || "";
    if (contentType.includes("application/json")) {
      data = await res.json();
    } else {
      data = await res.text();
    }
    return { ok: res.ok, status: res.status, data };
  } catch (err) {
    console.error("[apiFetch] Error de red:", err);
    return { ok: false, status: 0, data: { message: "Sin conexión al servidor." } };
  }
}

/* ── Storage de sesión ────────────────────────────────── */
function saveSession(token, user) {
  localStorage.setItem(CONFIG.TOKEN_KEY, token);
  if (user) localStorage.setItem(CONFIG.USER_KEY, JSON.stringify(user));
}

function clearSession() {
  localStorage.removeItem(CONFIG.TOKEN_KEY);
  localStorage.removeItem(CONFIG.USER_KEY);
}

function isAuthenticated() {
  return !!localStorage.getItem(CONFIG.TOKEN_KEY);
}

/* ── Validaciones ─────────────────────────────────────── */
const Validate = {
  email(val) {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(val.trim())
      ? null
      : "Ingresá un correo válido.";
  },
  password(val) {
    if (val.length < 8) return "Mínimo 8 caracteres.";
    return null;
  },
  passwordMatch(p1, p2) {
    return p1 === p2 ? null : "Las contraseñas no coinciden.";
  },
  required(val, label = "Este campo") {
    return val.trim() ? null : `${label} es obligatorio.`;
  },
  username(val) {
    if (!val.trim()) return "El nombre de usuario es obligatorio.";
    if (val.length < 3) return "Mínimo 3 caracteres.";
    if (!/^[a-zA-Z0-9_.-]+$/.test(val)) return "Solo letras, números, _, . y -";
    return null;
  },
};

/* ── UI helpers ───────────────────────────────────────── */
function setError(inputEl, errorEl, msg) {
  if (msg) {
    inputEl.classList.add("is-error");
    inputEl.classList.remove("is-valid");
    errorEl.textContent = msg;
  } else {
    inputEl.classList.remove("is-error");
    inputEl.classList.add("is-valid");
    errorEl.textContent = "";
  }
}

function clearFieldState(inputEl, errorEl) {
  inputEl.classList.remove("is-error", "is-valid");
  if (errorEl) errorEl.textContent = "";
}

function showAlert(el, msg, type = "error") {
  el.textContent = msg;
  el.className = `alert alert--${type}`;
  el.classList.remove("hidden");
  el.scrollIntoView({ behavior: "smooth", block: "nearest" });
}

function hideAlert(el) {
  el.classList.add("hidden");
  el.textContent = "";
}

function setLoading(btn, loading) {
  const label   = btn.querySelector(".btn-label");
  const spinner = btn.querySelector(".btn-spinner");
  btn.disabled  = loading;
  if (loading) {
    label.classList.add("hidden");
    spinner.classList.remove("hidden");
  } else {
    label.classList.remove("hidden");
    spinner.classList.add("hidden");
  }
}

/* ── Fortaleza de contraseña ──────────────────────────── */
function evaluatePasswordStrength(password) {
  let score = 0;
  if (password.length >= 8)  score++;
  if (password.length >= 12) score++;
  if (/[A-Z]/.test(password) && /[a-z]/.test(password)) score++;
  if (/\d/.test(password)) score++;
  if (/[^A-Za-z0-9]/.test(password)) score++;

  if (score <= 2) return { level: "weak",   label: "Débil" };
  if (score <= 3) return { level: "medium",  label: "Media" };
  return           { level: "strong", label: "Fuerte" };
}

/* ── Tab switcher ─────────────────────────────────────── */
function initTabs() {
  const tabs   = document.querySelectorAll(".auth-tab");
  const panels = document.querySelectorAll(".auth-form-panel");

  function activateTab(targetName) {
    tabs.forEach(t => {
      const isActive = t.dataset.target === targetName;
      t.classList.toggle("auth-tab--active", isActive);
      t.setAttribute("aria-selected", isActive);
    });
    panels.forEach(p => {
      const isActive = p.id === `panel-${targetName}`;
      p.classList.toggle("active", isActive);
    });
  }

  // Tab buttons
  tabs.forEach(tab => {
    tab.addEventListener("click", () => activateTab(tab.dataset.target));
  });

  // "Switch" links inside forms
  document.querySelectorAll("[data-switch]").forEach(btn => {
    btn.addEventListener("click", () => activateTab(btn.dataset.switch));
  });
}

/* ── Toggle password visibility ──────────────────────── */
function initPasswordToggles() {
  document.querySelectorAll(".toggle-pass").forEach(btn => {
    btn.addEventListener("click", () => {
      const input = document.getElementById(btn.dataset.target);
      const isText = input.type === "text";
      input.type = isText ? "password" : "text";
      btn.setAttribute("aria-label", isText ? "Mostrar contraseña" : "Ocultar contraseña");
      btn.querySelector(".eye-icon").textContent = isText ? "👁" : "🙈";
    });
  });
}

/* ── Username availability check (debounced) ─────────── */
let usernameDebounceTimer = null;

function initUsernameCheck() {
  const input  = document.getElementById("reg-username");
  const status = document.getElementById("username-status");
  const errorEl = document.getElementById("reg-username-error");
  if (!input) return;

  input.addEventListener("input", () => {
    const val = input.value.trim();
    clearTimeout(usernameDebounceTimer);
    status.textContent = "";
    status.className   = "field-status";

    if (!val || val.length < 3) return;

    usernameDebounceTimer = setTimeout(async () => {
      status.textContent = "…";
      // BIEN — endpoint correcto
      const { ok, data } = await apiFetch(
        `/auth/check-username?username=${encodeURIComponent(val)}`
      );
      if (ok && data?.available === true) {
        status.textContent = "✓ Disponible";
        status.className   = "field-status available";
        clearFieldState(input, errorEl);
        input.classList.add("is-valid");
      } else if (ok && data?.available === false) {
        status.textContent = "✗ Ocupado";
        status.className   = "field-status taken";
        setError(input, errorEl, "Ese nombre de usuario ya está en uso.");
      } else {
        // Si el backend no está disponible, ignoramos el check silenciosamente
        status.textContent = "";
      }
    }, 500);
  });
}

/* ── Password strength meter ──────────────────────────── */
function initPasswordStrength() {
  const input  = document.getElementById("reg-password");
  const fill   = document.getElementById("pass-strength-fill");
  const label  = document.getElementById("pass-strength-label");
  if (!input) return;

  input.addEventListener("input", () => {
    if (!input.value) {
      fill.className  = "pass-strength__fill";
      label.className = "pass-strength__label";
      fill.style.width = "0";
      label.textContent = "";
      return;
    }
    const { level, label: txt } = evaluatePasswordStrength(input.value);
    fill.className  = `pass-strength__fill ${level}`;
    label.className = `pass-strength__label ${level}`;
    label.textContent = txt;
  });
}

/* ── LOGIN ────────────────────────────────────────────── */
function initLoginForm() {
  const form      = document.getElementById("loginForm");
  const emailInp  = document.getElementById("login-email");
  const passInp   = document.getElementById("login-password");
  const emailErr  = document.getElementById("login-email-error");
  const passErr   = document.getElementById("login-password-error");
  const serverErr = document.getElementById("login-server-error");
  const serverOk  = document.getElementById("login-server-success");
  const btn       = document.getElementById("loginBtn");

  if (!form) return;

  // Validación — cambiar Validate.email por Validate.required
  // porque ahora acepta username también, no solo email
  emailInp.addEventListener("blur", () =>
    setError(emailInp, emailErr, Validate.required(emailInp.value, "El usuario o email"))
  );

  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    hideAlert(serverErr);
    hideAlert(serverOk);

    // Client-side validation
    const loginError = Validate.required(emailInp.value, "El usuario o email");
    const passError = Validate.password(passInp.value);
    setError(emailInp, emailErr, loginError);
    setError(passInp, passErr, passError);
    if (loginError || passError) return;

    // Build payload — "login" coincide con LoginRequest.java
    const payload = {
      login: emailInp.value.trim(),   // no toLowerCase — username puede tener mayúsculas
      password: passInp.value,
    };

    /* ── Llamada al backend ──
     * POST /api/auth/login
     * Body: { email, password }
     * Respuesta esperada 200: { token: string, user: { id, username, email, role } }
     * Respuesta 401/403: { message: string }
     */
    const { ok, status, data } = await apiFetch("/auth/login", {
      method: "POST",
      body: JSON.stringify(payload),
    });

    setLoading(btn, false);

    if (ok && data?.token) {
      saveSession(data.token, data.user || null);
      showAlert(serverOk, "¡Acceso correcto! Redirigiendo…", "success");
      setTimeout(() => {
        window.location.href = CONFIG.REDIRECT_AFTER_LOGIN;
      }, 1000);
    } else {
      const msg =
        data?.message ||
        (status === 401 ? "Credenciales incorrectas." : "Error del servidor. Intentá más tarde.");
      showAlert(serverErr, msg, "error");
    }
  });
}

/* ── REGISTER ─────────────────────────────────────────── */
function initRegisterForm() {
  const form       = document.getElementById("registerForm");
  const nombreInp  = document.getElementById("reg-nombre");
  const apellInp   = document.getElementById("reg-apellido");
  const userInp    = document.getElementById("reg-username");
  const emailInp   = document.getElementById("reg-email");
  const passInp    = document.getElementById("reg-password");
  const confInp    = document.getElementById("reg-confirm");
  const termsChk   = document.getElementById("reg-terms");

  const nombreErr  = document.getElementById("reg-nombre-error");
  const apellErr   = document.getElementById("reg-apellido-error");
  const userErr    = document.getElementById("reg-username-error");
  const emailErr   = document.getElementById("reg-email-error");
  const passErr    = document.getElementById("reg-password-error");
  const confErr    = document.getElementById("reg-confirm-error");
  const termsErr   = document.getElementById("reg-terms-error");
  const serverErr  = document.getElementById("register-server-error");
  const serverOk   = document.getElementById("register-server-success");
  const btn        = document.getElementById("registerBtn");

  if (!form) return;

  // Blur validations
  nombreInp.addEventListener("blur", () =>
    setError(nombreInp, nombreErr, Validate.required(nombreInp.value, "El nombre"))
  );
  apellInp.addEventListener("blur", () =>
    setError(apellInp, apellErr, Validate.required(apellInp.value, "El apellido"))
  );
  userInp.addEventListener("blur", () =>
    setError(userInp, userErr, Validate.username(userInp.value))
  );
  emailInp.addEventListener("blur", () =>
    setError(emailInp, emailErr, Validate.email(emailInp.value))
  );
  passInp.addEventListener("blur", () =>
    setError(passInp, passErr, Validate.password(passInp.value))
  );
  confInp.addEventListener("blur", () =>
    setError(confInp, confErr, Validate.passwordMatch(passInp.value, confInp.value))
  );

  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    hideAlert(serverErr);
    hideAlert(serverOk);

    // Full client validation
    const errors = {
      nombre:   Validate.required(nombreInp.value, "El nombre"),
      apellido: Validate.required(apellInp.value, "El apellido"),
      username: Validate.username(userInp.value),
      email:    Validate.email(emailInp.value),
      password: Validate.password(passInp.value),
      confirm:  Validate.passwordMatch(passInp.value, confInp.value),
      terms:    termsChk.checked ? null : "Debés aceptar los términos.",
    };

    setError(nombreInp, nombreErr, errors.nombre);
    setError(apellInp,  apellErr,  errors.apellido);
    setError(userInp,   userErr,   errors.username);
    setError(emailInp,  emailErr,  errors.email);
    setError(passInp,   passErr,   errors.password);
    setError(confInp,   confErr,   errors.confirm);
    termsErr.textContent = errors.terms || "";

    const hasError = Object.values(errors).some(Boolean);
    if (hasError) return;

    // Build payload
    const payload = {
      nombre: nombreInp.value.trim(),
      apellido: apellInp.value.trim(),
      username: userInp.value.trim(),
      email: emailInp.value.trim().toLowerCase(),
      password: passInp.value,
    };

    setLoading(btn, true);

    /* ── Llamada al backend ──
     * POST /api/auth/register
     * Body: { firstName, lastName, username, email, password }
     * Respuesta esperada 201: { message: "Usuario creado", user: { id, username, email } }
     * Respuesta 409 (conflicto): { message: "El email ya está registrado" }
     * Respuesta 400 (validación): { message: string, errors?: { field: string }[] }
     */
    const { ok, status, data } = await apiFetch("/auth/registro", {
      method: "POST",
      body: JSON.stringify(payload),
    });

    setLoading(btn, false);

    if (ok && (status === 200 || status === 201)) {
      showAlert(serverOk, "¡Cuenta creada exitosamente! Podés iniciar sesión.", "success");
      form.reset();
      // Limpiar indicadores visuales
      [nombreInp, apellInp, userInp, emailInp, passInp, confInp].forEach(el =>
        el.classList.remove("is-valid", "is-error")
      );
      document.getElementById("pass-strength-fill").className = "pass-strength__fill";
      document.getElementById("pass-strength-label").textContent = "";

      // Auto-cambiar al tab de login tras 2 s
      setTimeout(() => {
        document.querySelector('[data-target="login"]').click();
      }, 2000);

    } else {
      const msg =
        data?.message ||
        (status === 409 ? "El correo o usuario ya está registrado." : "Error al registrar. Intentá más tarde.");
      showAlert(serverErr, msg, "error");
    }
  });
}

/* ── Init ─────────────────────────────────────────────── */
document.addEventListener("DOMContentLoaded", () => {
  // Redirigir si ya está autenticado
  if (isAuthenticated()) {
    window.location.href = CONFIG.REDIRECT_AFTER_LOGIN;
    return;
  }

  initTabs();
  initPasswordToggles();
  initUsernameCheck();
  initPasswordStrength();
  initLoginForm();
  initRegisterForm();
});

/* ── Exportar para uso externo (opcional) ─────────────── */
// Si usás modules (type="module"), podés exportar lo que necesites:
// export { apiFetch, saveSession, clearSession, isAuthenticated, CONFIG };