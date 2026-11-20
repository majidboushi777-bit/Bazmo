(function () {
  const ALL_ROLES = [
    'تعمیرکار',
    'نرم‌افزارکار',
    'فروشنده موبایل',
    'فروشنده جانبی',
    'عمده‌فروش موبایل',
    'عمده‌فروش جانبی',
    'قطعه‌فروش'
  ];
  const STORE_CREATOR_ROLES = new Set([
    'عمده‌فروش موبایل',
    'عمده‌فروش جانبی',
    'قطعه‌فروش'
  ]);
  const ADMIN_TABS = [
    { id: 'dashboard', label: '📊 داشبورد' },
    { id: 'users', label: '👥 کاربران' },
    { id: 'notifs', label: '🔔 نوتیفیکیشن' },
    { id: 'ads', label: '🛡️ آگهی و فروشگاه' },
    { id: 'requests', label: '📦 سفارش و استعلام' },
    { id: 'catalog', label: '🧩 کالا و انجمن' }
  ];
  const USER_TABS = [
    { id: 0, label: 'موبایل' },
    { id: 1, label: 'نرم‌افزار' },
    { id: 2, label: 'جانبی' },
    { id: 3, label: 'سخت‌افزار' }
  ];
  const LOCATIONS = {
    'تهران': {
      'تهران': ['همه مناطق', 'علاءالدین و جمهوری', 'چارسو و فردوسی', 'ونک و جردن', 'تجریش', 'صادقیه', 'پاسداران'],
      'شهریار': ['همه مناطق', 'مرکز شهر', 'اندیشه'],
      'اسلامشهر': ['همه مناطق', 'مرکز شهر', 'قائمیه'],
      'ورامین': ['همه مناطق', 'مرکز شهر']
    },
    'اصفهان': {
      'اصفهان': ['همه مناطق', 'فردوسی و بازار موبایل', 'چهارباغ بالا', 'ملکشهر', 'سپاهانشهر'],
      'کاشان': ['همه مناطق', 'مرکز شهر'],
      'نجف‌آباد': ['همه مناطق', 'مرکز شهر']
    },
    'خراسان رضوی': {
      'مشهد': ['همه مناطق', 'احمدآباد', 'سجاد', 'شهدا و سعدی', 'وکیل‌آباد'],
      'نیشابور': ['همه مناطق', 'مرکز شهر'],
      'سبزوار': ['همه مناطق', 'مرکز شهر']
    },
    'فارس': {
      'شیراز': ['همه مناطق', 'ملاصدرا', 'معالی‌آباد', 'داریوش و زند'],
      'مرودشت': ['همه مناطق', 'مرکز شهر']
    },
    'آذربایجان شرقی': {
      'تبریز': ['همه مناطق', 'شهناز', 'ولیعصر', 'آبرسان'],
      'مراغه': ['همه مناطق', 'مرکز شهر']
    },
    'البرز': {
      'کرج': ['همه مناطق', 'طالقانی', 'گوهردشت', 'مهرشهر', 'فردیس']
    },
    'مازندران': {
      'ساری': ['همه مناطق', 'فرهنگ', 'قارن'],
      'بابل': ['همه مناطق', 'مدرس'],
      'آمل': ['همه مناطق', 'هراز']
    },
    'خوزستان': {
      'اهواز': ['همه مناطق', 'نادری', 'کیانپارس', 'زیتون کارمندی'],
      'دزفول': ['همه مناطق', 'شریعتی']
    },
    'گیلان': {
      'رشت': ['همه مناطق', 'مطهری', 'گلسار', 'شهرداری']
    },
    'قم': {
      'قم': ['همه مناطق', 'صفائیه', 'باجک', 'بلوار امین']
    }
  };
  const FORUM_FLAIRS_HW = ['عیب‌یابی جریان', 'آی‌سی و برد', 'تاچ و ال‌سی‌دی', 'شارژ و باتری', 'ابزار و هیتر'];
  const FORUM_FLAIRS_SW = ['آنلاک و قفل‌گشایی', 'اپل‌آیدی و آیکلود', 'تست‌پوینت و ترمیم', 'فایل فلش', 'آموزش و راهنما'];
  const ORDER_STATUSES = [
    'ارسال شده به فروشگاه',
    'تایید و در حال آماده‌سازی',
    'تحویل پیک شد',
    'تحویل حضوری تکمیل شد'
  ];

  const state = {
    token: localStorage.getItem('bazmo_web_token') || '',
    session: null,
    db: null,
    config: { hasAdminPasswordLogin: true },
    loading: true,
    isLoggingIn: false,
    loginEmail: '',
    loginPassword: '',
    showLoginPassword: false,
    loginError: '',
    loginSuccess: '',
    mode: 'user', // 'user' | 'admin'
    userTab: 0, // 0 mobile, 1 sw, 2 acc, 3 hw
    adminTab: 'dashboard',
    province: 'تهران',
    city: 'تهران',
    region: 'علاءالدین و جمهوری',
    // Mobile
    mobileSearch: '',
    mobileBrand: 'همه',
    mobileBudgetMax: 0,
    mobileDetailId: '',
    storeDetailId: '',
    // Software
    swSection: 'home', // 'home' | 'detail' | 'boxes' | 'req' | 'forum'
    swProblemId: '',
    swSearch: '',
    // Accessories
    accCat: 'همه',
    accSearch: '',
    accFilterStoreId: '',
    // Hardware
    hwCat: 'همه',
    hwSearch: '',
    hwSection: 'home', // 'home' | 'detail' | 'my_ads' | 'special_req' | 'forum'
    hwSelectedAdId: '',
    // Forum
    forumBoard: 'hardware',
    forumSort: 'hot', // 'hot' | 'new' | 'top'
    forumFlair: 'همه',
    forumSearch: '',
    forumThreadId: '',
    // Admin filters
    adminUserSearch: '',
    adminUserTier: 'همه نقش‌ها',
    adminUserStatus: 'all',
    adminUserProvince: 'همه استان‌ها',
    adminUserCity: 'همه شهرها',
    adminUserSubTab: 'users', // 'users' | 'requests'
    adminAdsTile: 'hw', // 'hw' | 'mobile' | 'acc' | 'stores' | 'desks' | 'tutorials' | 'boxes' | 'banners'
    adminAdsSearch: '',
    adminAdsFilter: 'all', // 'all' | 'pending'
    adminAdsShowAll: false,
    adminBannerSection: 'mobile', // 'mobile' | 'software' | 'acc' | 'hw'
    adminBannerProv: 'همه استان‌ها',
    adminBannerCity: 'همه شهرها',
    adminBannerRegion: 'همه مناطق',
    adminBannerDrafts: {},
    adminNotifProv: 'همه استان‌ها',
    adminNotifCity: 'همه شهرها',
    adminNotifRegion: 'همه مناطق',
    adminNotifTier: 'همه',
    adminNotifType: 'اطلاعیه صنفی',
    prevLocation: { province: 'تهران', city: 'تهران', region: 'علاءالدین و جمهوری' },
    theme: localStorage.getItem('bazmo_theme') || (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'),
    // Runtime
    savedAccount: (() => {
      try {
        const raw = localStorage.getItem('bazmo_web_saved_account');
        return raw ? JSON.parse(raw) : null;
      } catch {
        return null;
      }
    })(),
    cart: [],
    ws: null,
    wsOpen: false,
    sse: null,
    swReg: null,
    bgPollTimer: null,
    stateEtag: '',
    searchDebounceTimer: null,
    hasSyncedOnce: false,
    notifiedIds: new Set(JSON.parse(localStorage.getItem('bazmo_notified_ids') || '[]')),
    // New sections
    showDeskRentals: false,
    deskSubTab: 'rentals', // 'rentals' | 'requests'
    deskCat: 'همه',
    deskSearch: '',
    showMyStore: false,
    myStoreId: '',
    myStoreSubTab: 'products', // 'products' | 'inquiries' | 'orders' | 'settings'
    isSeedingDemo: false,
    _resetScrollNext: false
  };

  function isDarkTheme() {
    return state.theme === 'dark';
  }

  function applyTheme(nextTheme) {
    const t = nextTheme === 'dark' ? 'dark' : 'light';
    state.theme = t;
    document.documentElement.setAttribute('data-theme', t);
    try { localStorage.setItem('bazmo_theme', t); } catch {}
    const meta = document.getElementById('theme-color-meta');
    if (meta) meta.setAttribute('content', t === 'dark' ? '#0B192E' : '#102542');
  }

  function toggleTheme() {
    applyTheme(isDarkTheme() ? 'light' : 'dark');
    render();
  }

  function themeSvgIcon(dark) {
    return dark
      ? `<svg viewBox="0 0 24 24" fill="currentColor"><path d="M6.76 4.84l-1.8-1.79-1.41 1.41 1.79 1.79 1.42-1.41zM4 10.5H1v2h3v-2zm9-9.95h-2V3.5h2V.55zm7.45 3.91l-1.41-1.41-1.79 1.79 1.41 1.41 1.79-1.79zm-3.21 13.7l1.79 1.8 1.41-1.41-1.8-1.79-1.4 1.4zM20 10.5v2h3v-2h-3zm-8-5c-3.31 0-6 2.69-6 6s2.69 6 6 6 6-2.69 6-6-2.69-6-6-6zm-1 16.95h2V19.5h-2v2.95zm-7.45-3.91l1.41 1.41 1.79-1.8-1.41-1.41-1.79 1.8z"/></svg>`
      : `<svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 3a9 9 0 109 9c0-.46-.04-.92-.1-1.36a5.389 5.389 0 01-4.4 2.26 5.403 5.403 0 01-3.14-9.8c-.44-.06-.9-.1-1.36-.1z"/></svg>`;
  }

  function toEnglishDigits(val) {
    return String(val || '')
      .replace(/[۰-۹]/g, (d) => String('۰۱۲۳۴۵۶۷۸۹'.indexOf(d)))
      .replace(/[٠-٩]/g, (d) => String('٠١٢٣٤٥٦٧٨٩'.indexOf(d)));
  }

  function normalizeZwnj(val) {
    return toEnglishDigits(val)
      .replace(/\u200c/g, '')
      .replace(/ي/g, 'ی')
      .replace(/ك/g, 'ک')
      .replace(/\s+/g, '')
      .trim();
  }

  function isAllScope(val) {
    const n = normalizeZwnj(val);
    return (
      !n ||
      n === 'all' ||
      n === 'همه' ||
      n === 'همهاستانها' ||
      n === 'همهشهرها' ||
      n === 'همهشهرهایاستان' ||
      n === 'همهمناطق' ||
      n === 'همهمناطقشهر' ||
      n === 'همهبخشها' ||
      n === 'همهنقشها' ||
      n === 'همهوضعیتها' ||
      n === 'همهکشور' ||
      n === 'کلکشور' ||
      n === 'سراسرکشور'
    );
  }

  function esc(val) {
    return String(val ?? '')
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;');
  }

  function toPersianNum(n) {
    return String(n ?? 0).replace(/\d/g, (d) => '۰۱۲۳۴۵۶۷۸۹'[Number(d)]);
  }

  function formatToman(num) {
    const n = Number(num || 0);
    return n.toLocaleString('fa-IR') + ' تومان';
  }

  function toast(msg) {
    const root = document.getElementById('toast-root');
    if (!root) return;
    const el = document.createElement('div');
    el.className = 'toast';
    el.textContent = msg;
    root.appendChild(el);
    setTimeout(() => el.remove(), 3400);
  }

  function storeTypeLabel(type) {
    if (type === 'mobile') return 'موبایل';
    if (type === 'acc') return 'لوازم جانبی';
    if (type === 'hw') return 'قطعات سخت‌افزار';
    return 'موبایل و جانبی';
  }

  function defaultStoreTypeForRole(tier) {
    const norm = normalizeZwnj(tier);
    if (norm === normalizeZwnj('عمده‌فروش موبایل')) return 'mobile';
    if (norm === normalizeZwnj('عمده‌فروش جانبی')) return 'acc';
    if (norm === normalizeZwnj('قطعه‌فروش')) return 'hw';
    return 'both';
  }

  function canCreateStore() {
    if (!state.session) return false;
    if (state.session.isAdmin) return true;
    const norm = normalizeZwnj(getUserTier());
    for (const r of STORE_CREATOR_ROLES) {
      if (normalizeZwnj(r) === norm) return true;
    }
    return false;
  }

  function canCreateStoreInSection(section) {
    if (!state.session) return false;
    if (state.session.isAdmin) return true;
    if (!canCreateStore()) return false;
    return defaultStoreTypeForRole(getUserTier()) === section;
  }

  function canAccessSoftwareBoard() {
    if (!state.session) return false;
    return state.session.isAdmin || getUserTier() === 'نرم‌افزارکار';
  }

  function canAccessHardwareBoard() {
    if (!state.session) return false;
    return state.session.isAdmin || getUserTier() === 'تعمیرکار';
  }

  function filterByLocation(items, province, city, region, getLoc) {
    if (isAllScope(province)) {
      return { list: items, scope: 'سراسر کشور' };
    }
    if (!isAllScope(city)) {
      if (region && !isAllScope(region)) {
        const exactReg = items.filter((item) => {
          const l = getLoc(item);
          return normalizeZwnj(l.city) === normalizeZwnj(city) && normalizeZwnj(l.region) === normalizeZwnj(region);
        });
        if (exactReg.length) return { list: exactReg, scope: `${city} (${region})` };
      }
      const exactCity = items.filter((item) => normalizeZwnj(getLoc(item).city) === normalizeZwnj(city));
      if (exactCity.length) return { list: exactCity, scope: `شهر ${city}` };
    }
    const exactProv = items.filter((item) => normalizeZwnj(getLoc(item).province) === normalizeZwnj(province));
    if (exactProv.length) return { list: exactProv, scope: `استان ${province}` };

    return { list: items, scope: 'سراسر کشور' };
  }

  async function api(path, options = {}) {
    const headers = Object.assign({ 'Content-Type': 'application/json' }, options.headers || {});
    if (state.token) headers.Authorization = `Bearer ${state.token}`;
    const timeoutMs = Number(options.timeoutMs || 45000);
    const controller = typeof AbortController !== 'undefined' ? new AbortController() : null;
    const timer = controller ? setTimeout(() => controller.abort(), timeoutMs) : null;
    let res;
    try {
      res = await fetch(path, {
        method: options.method || 'GET',
        headers,
        body: options.body ? JSON.stringify(options.body) : undefined,
        signal: controller ? controller.signal : undefined
      });
    } catch (err) {
      if (err && err.name === 'AbortError') {
        throw new Error('زمان پاسخگویی سرور به پایان رسید. لطفاً چند لحظه دیگر دوباره تلاش کنید.');
      }
      throw new Error('خطا در برقراری ارتباط با سرور بازمو.');
    } finally {
      if (timer) clearTimeout(timer);
    }
    const data = await res.json().catch(() => ({}));
    if (!res.ok) {
      if (res.status === 401 && path !== '/api/auth/login') {
        logoutLocal(true);
      }
      if (res.status === 404 && !data.error) {
        throw new Error('این سرویس روی سرور یافت نشد (۴۰۴). لطفاً نسخه جدید سرور را مستقر کنید.');
      }
      throw new Error(data.error || `خطا در انجام عملیات روی سرور (کد ${res.status})`);
    }
    return data;
  }

  async function fetchStateWithEtag() {
    const headers = {};
    if (state.token) headers.Authorization = `Bearer ${state.token}`;
    const isAsciiEtag = (v) => typeof v === 'string' && v.length > 0 && /^[\x20-\x7e]+$/.test(v);
    if (isAsciiEtag(state.stateEtag) && state.db) {
      headers['If-None-Match'] = state.stateEtag;
    } else if (!isAsciiEtag(state.stateEtag)) {
      state.stateEtag = '';
    }
    const res = await fetch('/api/state', { method: 'GET', headers });
    if (res.status === 304) return null;
    const data = await res.json().catch(() => ({}));
    if (!res.ok) {
      if (res.status === 401) logoutLocal(true);
      throw new Error(data.error || `خطای سرور (${res.status})`);
    }
    const nextEtag = res.headers.get('ETag');
    if (isAsciiEtag(nextEtag)) state.stateEtag = nextEtag;
    return data;
  }

  function saveAccountForQuickLogin(session, token, extra = {}) {
    if (!session || !token) return;
    const prev = state.savedAccount || {};
    const next = {
      uid: session.uid || prev.uid || '',
      email: session.email || prev.email || '',
      displayName: session.user?.fullName || session.displayName || prev.displayName || 'همکار بازمو',
      isAdmin: Boolean(session.isAdmin),
      token,
      webauthnCredId: extra.webauthnCredId !== undefined ? extra.webauthnCredId : (prev.uid === session.uid ? prev.webauthnCredId || '' : ''),
      savedAt: Date.now()
    };
    state.savedAccount = next;
    try {
      localStorage.setItem('bazmo_web_saved_account', JSON.stringify(next));
    } catch {}
  }

  function writeCachedAppState(session, dbData) {
    if (!session || !dbData || !session.uid) return;
    try {
      localStorage.setItem(
        'bazmo_web_cached_state',
        JSON.stringify({ uid: session.uid, session, db: dbData, ts: Date.now() })
      );
    } catch {}
  }

  function readCachedAppState(uidHint = '') {
    try {
      const raw = localStorage.getItem('bazmo_web_cached_state');
      if (!raw) return null;
      const parsed = JSON.parse(raw);
      if (!parsed?.session || !parsed?.db) return null;
      if (uidHint && parsed.uid !== uidHint) return null;
      return parsed;
    } catch {
      return null;
    }
  }

  function logoutLocal(forgetSaved = false) {
    state.token = '';
    state.session = null;
    state.db = null;
    state.stateEtag = '';
    state.wsOpen = false;
    state.showDeskRentals = false;
    state.showMyStore = false;
    state.hasSyncedOnce = false;
    state.cart = [];
    localStorage.removeItem('bazmo_web_token');
    if (forgetSaved) {
      state.savedAccount = null;
      localStorage.removeItem('bazmo_web_saved_account');
      localStorage.removeItem('bazmo_web_cached_state');
    }
    if (state.ws) {
      try { state.ws.close(); } catch {}
      state.ws = null;
    }
    if (state.sse) {
      try { state.sse.close(); } catch {}
      state.sse = null;
    }
    if (state.bgPollTimer) {
      clearInterval(state.bgPollTimer);
      state.bgPollTimer = null;
    }
    syncSessionWithServiceWorker(null);
  }

  function bufferToBase64Url(buf) {
    const bytes = new Uint8Array(buf);
    let bin = '';
    for (let i = 0; i < bytes.length; i++) bin += String.fromCharCode(bytes[i]);
    return btoa(bin).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
  }

  function base64UrlToBuffer(str) {
    const pad = '='.repeat((4 - (str.length % 4)) % 4);
    const b64 = (str + pad).replace(/-/g, '+').replace(/_/g, '/');
    const bin = atob(b64);
    const arr = new Uint8Array(bin.length);
    for (let i = 0; i < bin.length; i++) arr[i] = bin.charCodeAt(i);
    return arr.buffer;
  }

  async function enrollBrowserBiometric() {
    if (!window.PublicKeyCredential || !navigator.credentials?.create || !state.session) {
      toast('مرورگر فعلی از اثر انگشت WebAuthn پشتیبانی نمی‌کند؛ ورود سریع همچنان فعال است.');
      return false;
    }
    try {
      const challenge = crypto.getRandomValues(new Uint8Array(32));
      const userId = new TextEncoder().encode(String(state.session.uid || 'bazmo_user'));
      const cred = await navigator.credentials.create({
        publicKey: {
          challenge,
          rp: { name: 'بازمو', id: location.hostname },
          user: {
            id: userId,
            name: state.session.email || 'user@bazmo.ir',
            displayName: state.session.user?.fullName || state.session.displayName || 'همکار بازمو'
          },
          pubKeyCredParams: [{ type: 'public-key', alg: -7 }, { type: 'public-key', alg: -257 }],
          authenticatorSelection: { authenticatorAttachment: 'platform', userVerification: 'required' },
          timeout: 45000
        }
      });
      if (cred && cred.rawId) {
        const credId = bufferToBase64Url(cred.rawId);
        saveAccountForQuickLogin(state.session, state.token, { webauthnCredId: credId });
        toast('قفل اثر انگشت / بیومتریک مرورگر برای ورود بعدی فعال شد ✓');
        return true;
      }
    } catch {
      toast('ثبت اثر انگشت لغو شد یا در این دستگاه در دسترس نیست.');
    }
    return false;
  }

  async function verifyBrowserBiometricIfEnrolled() {
    const saved = state.savedAccount;
    if (!saved?.webauthnCredId) return true;
    if (!window.PublicKeyCredential || !navigator.credentials?.get) return true;
    const challenge = crypto.getRandomValues(new Uint8Array(32));
    await navigator.credentials.get({
      publicKey: {
        challenge,
        rpId: location.hostname,
        allowCredentials: [{ type: 'public-key', id: base64UrlToBuffer(saved.webauthnCredId) }],
        userVerification: 'required',
        timeout: 45000
      }
    });
    return true;
  }

  function persistNotifiedIds() {
    const arr = Array.from(state.notifiedIds).slice(-150);
    state.notifiedIds = new Set(arr);
    localStorage.setItem('bazmo_notified_ids', JSON.stringify(arr));
  }

  function getUserTier() {
    if (!state.session) return 'تعمیرکار';
    if (state.session.isAdmin) return 'ادمین';
    return state.session.user?.tier || 'تعمیرکار';
  }

  function isNotifVisibleToCurrentUser(n) {
    if (!n || !state.session) return false;
    const provMatch = isAllScope(n.province) || isAllScope(state.province) || normalizeZwnj(n.province) === normalizeZwnj(state.province);
    const cityMatch = isAllScope(n.city) || isAllScope(state.city) || normalizeZwnj(n.city) === normalizeZwnj(state.city);
    const regMatch = isAllScope(n.region) || isAllScope(state.region) || normalizeZwnj(n.region) === normalizeZwnj(state.region);
    const tierMatch = state.session.isAdmin || isAllScope(n.tierFilter) || normalizeZwnj(n.tierFilter) === normalizeZwnj(getUserTier());
    return provMatch && cityMatch && regMatch && tierMatch;
  }

  function getVisibleNotifications() {
    if (!state.db?.notifications) return [];
    return state.db.notifications.filter(isNotifVisibleToCurrentUser);
  }

  function getUnreadNotificationCount() {
    const uid = state.session?.uid;
    if (!uid) return 0;
    return getVisibleNotifications().filter((n) => !(n.readBy || []).includes(uid)).length;
  }

  function syncSessionWithServiceWorker(sessionPayload, markIds = []) {
    try {
      if (!('serviceWorker' in navigator)) return;
      const msg = sessionPayload
        ? { type: 'SYNC_SESSION', session: sessionPayload, markNotifiedIds: markIds }
        : { type: 'CLEAR_SESSION' };
      if (navigator.serviceWorker.controller) {
        navigator.serviceWorker.controller.postMessage(msg);
      }
      navigator.serviceWorker.ready
        .then((reg) => {
          if (reg.active) reg.active.postMessage(msg);
        })
        .catch(() => {});
    } catch {}
  }

  function buildSwSessionPayload() {
    if (!state.token || !state.session?.uid) return null;
    return {
      token: state.token,
      uid: state.session.uid,
      tier: getUserTier(),
      province: state.province,
      city: state.city,
      region: state.region
    };
  }

  async function initServiceWorker() {
    if (!('serviceWorker' in navigator)) return;
    try {
      const reg = await navigator.serviceWorker.register('/sw.js');
      state.swReg = reg;
      if (state.token && state.session?.uid) {
        syncSessionWithServiceWorker(buildSwSessionPayload(), Array.from(state.notifiedIds));
        await registerBackgroundNotifSync(reg);
      }
      navigator.serviceWorker.addEventListener('message', (event) => {
        if (event.data?.type === 'OPEN_NOTIFICATIONS' && state.session && state.db) {
          openNotificationsModal();
        }
      });
    } catch {}
  }

  async function registerBackgroundNotifSync(reg) {
    const registration = reg || state.swReg;
    if (!registration) return;
    try {
      if ('periodicSync' in registration) {
        try {
          await registration.periodicSync.register('bazmo-notif-sync', { minInterval: 15 * 60 * 1000 });
        } catch {}
      }
      if ('sync' in registration) {
        try {
          await registration.sync.register('bazmo-notif-sync');
        } catch {}
      }
    } catch {}
  }

  async function showBrowserNotification(title, body, id) {
    if (!('Notification' in window) || Notification.permission !== 'granted') return;
    try {
      const opts = {
        body: body || '',
        tag: `bazmo_${id}`,
        renotify: true,
        dir: 'rtl',
        lang: 'fa'
      };
      if (state.swReg && state.swReg.showNotification) {
        await state.swReg.showNotification(title || 'اعلان جدید بازمو', opts);
      } else {
        const notif = new Notification(title || 'اعلان جدید بازمو', opts);
        notif.onclick = () => {
          window.focus();
          notif.close();
          if (state.session && state.db) openNotificationsModal();
        };
      }
    } catch {}
  }

  function dispatchWebNotifications(notificationsList, isInitialSync) {
    if (!state.session?.uid) return;
    const uid = state.session.uid;
    const visible = (notificationsList || []).filter(isNotifVisibleToCurrentUser);
    const unread = visible.filter((n) => !(n.readBy || []).includes(uid));

    if (isInitialSync && state.notifiedIds.size === 0) {
      unread.forEach((n) => state.notifiedIds.add(n.id));
      persistNotifiedIds();
      syncSessionWithServiceWorker(buildSwSessionPayload(), Array.from(state.notifiedIds));
      return;
    }

    const fresh = unread.filter((n) => !state.notifiedIds.has(n.id));
    if (fresh.length === 0) return;

    fresh.forEach((n) => state.notifiedIds.add(n.id));
    persistNotifiedIds();
    syncSessionWithServiceWorker(buildSwSessionPayload(), fresh.map((n) => n.id));

    fresh.slice(0, 3).forEach((n) => {
      toast(`🔔 ${n.title}`);
      showBrowserNotification(n.title, n.body, n.id);
    });
  }

  async function requestWebNotifications() {
    if (!('Notification' in window)) {
      toast('مرورگر شما از نوتیفیکیشن وب پشتیبانی نمی‌کند.');
      return;
    }
    try {
      const perm = await Notification.requestPermission();
      if (perm === 'granted') {
        toast('اعلان‌های پس‌زمینه بازمو فعال شدند 🔔');
        syncSessionWithServiceWorker(buildSwSessionPayload(), Array.from(state.notifiedIds));
        await registerBackgroundNotifSync();
        await pollBackgroundNotifications();
      } else if (perm === 'denied') {
        toast('دسترسی اعلان توسط مرورگر مسدود است. از تنظیمات سایت مرورگر مجوز را فعال کنید.');
      }
      render();
    } catch {
      toast('خطا در فعال‌سازی اعلان مرورگر');
    }
  }

  async function pollBackgroundNotifications() {
    if (!state.token || !state.session) return;
    try {
      const params = new URLSearchParams({
        province: state.province,
        city: state.city,
        region: state.region
      });
      const data = await api(`/api/notifications/unread?${params.toString()}`);
      if (Array.isArray(data.notifications)) {
        dispatchWebNotifications(data.notifications, false);
      }
    } catch {}
  }

  function startBackgroundNotifPolling() {
    if (state.bgPollTimer) clearInterval(state.bgPollTimer);
    const intervalMs = state.wsOpen ? 60000 : 22000;
    state.bgPollTimer = setInterval(() => {
      if (state.token && state.session && document.visibilityState === 'visible') {
        pollBackgroundNotifications();
      }
    }, intervalMs);
  }

  function connectWebSocket() {
    if (!state.token || state.ws) return;
    const proto = location.protocol === 'https:' ? 'wss:' : 'ws:';
    const ws = new WebSocket(`${proto}//${location.host}/ws?token=${encodeURIComponent(state.token)}`);
    ws.onopen = () => {
      state.wsOpen = true;
      if (state.sse) {
        try { state.sse.close(); } catch {}
        state.sse = null;
      }
      startBackgroundNotifPolling();
    };
    ws.onmessage = async (ev) => {
      try {
        const msg = JSON.parse(ev.data);
        if (msg.type === 'sync') {
          await loadAppState(true);
        }
      } catch {}
    };
    ws.onerror = () => {
      state.wsOpen = false;
    };
    ws.onclose = () => {
      state.ws = null;
      state.wsOpen = false;
      if (state.token) {
        connectEventStream();
        startBackgroundNotifPolling();
        setTimeout(connectWebSocket, 4000);
      }
    };
    state.ws = ws;
  }

  function connectEventStream() {
    if (!state.token || state.sse || state.wsOpen || typeof EventSource === 'undefined') return;
    try {
      const es = new EventSource(`/api/events?token=${encodeURIComponent(state.token)}`);
      es.addEventListener('sync', async () => {
        await loadAppState(true);
      });
      es.onerror = () => {
        try { es.close(); } catch {}
        state.sse = null;
        if (state.token && !state.wsOpen) {
          setTimeout(connectEventStream, 6000);
        }
      };
      state.sse = es;
    } catch {}
  }

  async function loadAppState(silent = false, forceRefresh = false, skipSessionFetch = false) {
    if (!state.token) return;
    if (forceRefresh) state.stateEtag = '';
    if (!silent) {
      state.loading = true;
      render();
    }
    try {
      const [sessionRes, dbRes] = await Promise.all([
        skipSessionFetch && state.session ? Promise.resolve(state.session) : api('/api/auth/session'),
        fetchStateWithEtag()
      ]);
      if (!dbRes && state.db) {
        state.session = sessionRes;
        saveAccountForQuickLogin(sessionRes, state.token);
        return;
      }
      const prevNotifIds = new Set((state.db?.notifications || []).map((n) => n.id));
      state.session = sessionRes;
      state.db = dbRes;
      saveAccountForQuickLogin(sessionRes, state.token);
      if (dbRes) writeCachedAppState(sessionRes, dbRes);
      if (sessionRes.user) {
        if (!state.hasSyncedOnce) {
          state.province = sessionRes.user.province || 'تهران';
          state.city = sessionRes.user.city || 'تهران';
          state.region = sessionRes.user.region || 'همه مناطق';
          if (!isAllScope(state.province)) {
            state.prevLocation = { province: state.province, city: state.city, region: state.region };
          }
          if (getUserTier() === 'نرم‌افزارکار') state.forumBoard = 'software';
        }
      }
      if (state.session.status === 'ready') {
        const isFirst = !state.hasSyncedOnce;
        dispatchWebNotifications(dbRes.notifications || [], isFirst);
        if (!isFirst && dbRes.notifications) {
          const added = dbRes.notifications.filter((n) => !prevNotifIds.has(n.id));
          if (added.length) dispatchWebNotifications(added, false);
        }
        state.hasSyncedOnce = true;
        syncSessionWithServiceWorker(buildSwSessionPayload(), Array.from(state.notifiedIds));
        registerBackgroundNotifSync();
        startBackgroundNotifPolling();
      }
      connectWebSocket();
    } catch (e) {
      if (!silent) toast(e.message);
    } finally {
      state.loading = false;
      render();
    }
  }

  async function init() {
    initServiceWorker();
    api('/api/config/public')
      .then((cfg) => { state.config = cfg; })
      .catch(() => {});

    if (state.token) {
      const cached = readCachedAppState(state.savedAccount?.uid || '');
      if (cached) {
        state.session = cached.session;
        state.db = cached.db;
        state.province = cached.session.user?.province || state.province;
        state.city = cached.session.user?.city || state.city;
        state.region = cached.session.user?.region || state.region;
        state.loading = false;
        render();
        loadAppState(true);
      } else {
        await loadAppState(false);
      }
      if (location.search.includes('openNotifications=1') && state.session && state.db) {
        openNotificationsModal();
      }
    } else {
      state.loading = false;
      render();
    }
  }

  document.addEventListener('visibilitychange', () => {
    if (document.visibilityState === 'visible' && state.token && state.session) {
      loadAppState(true);
      pollBackgroundNotifications();
    } else if (document.visibilityState === 'hidden' && state.token && state.session) {
      syncSessionWithServiceWorker(buildSwSessionPayload(), Array.from(state.notifiedIds));
    }
  });

  // ==================== LOGIN & REGISTRATION SCREENS ====================

  function renderLoginScreen() {
    const dark = isDarkTheme();
    const saved = state.savedAccount;
    return `
      <div class="login-shell">
        <div class="login-hero-bg"></div>
        <div class="login-container">
          <div class="login-topbar">
            <button type="button" id="login-theme-btn" class="login-theme-btn" aria-label="تغییر حالت روشن یا تاریک">
              <span>${dark ? '☀️ حالت روشن' : '🌙 حالت تاریک'}</span>
            </button>
          </div>
          <div class="brand-badge">
            <svg width="38" height="38" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
              <path d="M17 1.01L7 1c-1.1 0-2 .9-2 2v18c0 1.1.9 2 2 2h10c1.1 0 2-.9 2-2V3c0-1.1-.9-1.99-2-1.99zM17 19H7V5h10v14z"/>
            </svg>
          </div>
          <h1 class="brand-title">بازمو</h1>
          <p class="brand-sub">شبکه یکپارچه و رسمی صنف موبایل ایران</p>

          ${saved && saved.token ? `
            <div class="saved-account-card">
              <div class="row space-between" style="margin-bottom:8px;">
                <div>
                  <div style="font-size:14px;font-weight:900;color:var(--navy-deep);">👋 خوش آمدید، ${esc(saved.displayName || 'همکار بازمو')}</div>
                  <div class="muted" dir="ltr" style="text-align:right;">${esc(saved.email)}</div>
                </div>
                <button type="button" id="forget-saved-account-btn" class="btn btn-outline btn-sm">تغییر حساب</button>
              </div>
              <button type="button" id="biometric-quick-login-btn" class="btn-biometric-glass" ${state.isLoggingIn ? 'disabled' : ''}>
                <svg width="21" height="21" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
                  <path d="M17.81 4.47c-.08 0-.16-.02-.23-.06C15.66 3.42 14 3 12.01 3c-1.98 0-3.86.47-5.57 1.41-.24.13-.54.04-.68-.2-.13-.24-.04-.55.2-.68C7.82 2.52 9.86 2 12.01 2c2.13 0 3.99.47 6.03 1.52.25.13.34.43.21.67-.09.18-.26.28-.44.28zM3.5 9.72c-.1 0-.2-.03-.29-.09-.23-.16-.28-.47-.12-.7.99-1.4 2.25-2.5 3.75-3.27C9.98 4.04 14 4.03 17.15 5.65c1.5.77 2.76 1.86 3.75 3.25.16.22.11.54-.12.7-.23.16-.54.11-.7-.12-.9-1.26-2.04-2.25-3.39-2.94-2.87-1.47-6.54-1.47-9.4.01-1.36.7-2.5 1.7-3.4 2.96-.08.14-.23.21-.39.21zm6.25 12.07c-.13 0-.26-.05-.35-.15-.87-.87-1.34-1.43-2.01-2.64-.69-1.23-1.05-2.73-1.05-4.34 0-2.97 2.54-5.39 5.66-5.39s5.66 2.42 5.66 5.39c0 .28-.22.5-.5.5s-.5-.22-.5-.5c0-2.42-2.09-4.39-4.66-4.39-2.57 0-4.66 1.97-4.66 4.39 0 1.44.32 2.77.93 3.85.64 1.15 1.08 1.64 1.85 2.42.19.2.19.51 0 .71-.11.1-.24.15-.37.15zm7.17-1.85c-1.19 0-2.24-.3-3.1-.89-1.49-1.01-2.38-2.65-2.38-4.39 0-.28.22-.5.5-.5s.5.22.5.5c0 1.41.72 2.74 1.94 3.56.71.48 1.54.71 2.54.71.24 0 .64-.03 1.04-.1.27-.05.53.13.58.41.05.27-.13.53-.41.58-.57.11-1.07.12-1.21.12zM14.91 22c-.04 0-.09-.01-.13-.02-1.59-.44-2.63-1.03-3.72-2.1-1.4-1.39-2.17-3.24-2.17-5.22 0-1.62 1.38-2.94 3.08-2.94 1.7 0 3.08 1.32 3.08 2.94 0 1.07.93 1.94 2.08 1.94s2.08-.87 2.08-1.94c0-3.77-3.25-6.83-7.25-6.83-2.84 0-5.44 1.58-6.61 4.03-.39.81-.59 1.76-.59 2.8 0 .78.07 2.01.67 3.61.1.26-.03.55-.29.64-.26.1-.55-.04-.64-.29-.49-1.31-.73-2.61-.73-3.96 0-1.2.23-2.29.68-3.24 1.33-2.79 4.28-4.6 7.51-4.6 4.55 0 8.25 3.51 8.25 7.83 0 1.62-1.38 2.94-3.08 2.94s-3.08-1.32-3.08-2.94c0-1.07-.93-1.94-2.08-1.94s-2.08.87-2.08 1.94c0 1.71.66 3.31 1.87 4.51.95.94 1.86 1.46 3.27 1.85.27.07.42.35.35.61-.05.23-.26.38-.47.38z"/>
                </svg>
                <span>${saved.webauthnCredId ? 'ورود سریع با اثر انگشت' : 'ورود سریع با حساب ذخیره‌شده / اثر انگشت'}</span>
              </button>
            </div>
          ` : ''}

          <div class="card login-glass-card">
            <h2 style="margin:0 0 4px;font-size:18px;text-align:center;color:var(--navy-deep);font-weight:800;">ورود همکاران صنف موبایل</h2>
            <p class="muted" style="text-align:center;margin:0 0 16px;">ورود به حساب‌های ثبت‌نام‌شده با ایمیل و رمز عبور</p>

            ${state.loginError ? `<div class="alert-error">${esc(state.loginError)}</div>` : ''}
            ${state.loginSuccess ? `<div class="alert-success">${esc(state.loginSuccess)}</div>` : ''}

            <form id="password-login-form">
              <div class="form-group">
                <label class="form-label" for="login-email">ایمیل (نام کاربری)</label>
                <div class="glass-input-wrap">
                  <input id="login-email" class="glass-input" type="email" dir="ltr" placeholder="name@example.com" value="${esc(state.loginEmail || saved?.email || '')}" ${state.isLoggingIn ? 'disabled' : ''} required />
                </div>
              </div>
              <div class="form-group">
                <label class="form-label" for="login-password">رمز عبور</label>
                <div class="glass-input-wrap">
                  <input id="login-password" class="glass-input" type="${state.showLoginPassword ? 'text' : 'password'}" dir="ltr" placeholder="••••••••" value="${esc(state.loginPassword)}" ${state.isLoggingIn ? 'disabled' : ''} required />
                  <button type="button" id="toggle-pass-btn" class="glass-eye-btn" aria-label="نمایش یا مخفی کردن رمز عبور" title="نمایش یا مخفی کردن رمز عبور">
                    ${state.showLoginPassword
                      ? `<svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor"><path d="M12 7c2.76 0 5 2.24 5 5 0 .65-.13 1.26-.36 1.83l2.92 2.92c1.51-1.26 2.7-2.89 3.43-4.75-1.73-4.39-6-7.5-11-7.5-1.4 0-2.74.25-3.98.7l2.16 2.16C10.74 7.13 11.35 7 12 7zM2 4.27l2.28 2.28.46.46C3.08 8.3 1.78 10.02 1 12c1.73 4.39 6 7.5 11 7.5 1.55 0 3.03-.3 4.38-.84l.42.42L19.73 22 21 20.73 3.27 3 2 4.27zM7.53 9.8l1.55 1.55c-.05.21-.08.43-.08.65 0 1.66 1.34 3 3 3 .22 0 .44-.03.65-.08l1.55 1.55c-.67.33-1.41.53-2.2.53-2.76 0-5-2.24-5-5 0-.79.2-1.53.53-2.2zm4.31-.78l3.15 3.15.02-.16c0-1.66-1.34-3-3-3l-.17.01z"/></svg>`
                      : `<svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor"><path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"/></svg>`}
                  </button>
                </div>
              </div>
              <button type="submit" class="btn-apple-glass" ${state.isLoggingIn ? 'disabled' : ''}>
                ${state.isLoggingIn
                  ? '<span class="btn-spinner"></span><span>در حال احراز هویت...</span>'
                  : '<span>ورود به سیستم بازمو</span>'}
              </button>
            </form>

            <button id="open-reg-modal-btn" class="btn btn-gold btn-block" style="margin-top:14px;" ${state.isLoggingIn ? 'disabled' : ''}>ثبت درخواست عضویت جدید</button>
          </div>
        </div>
      </div>
    `;
  }

  function bindLoginEvents() {
    const emailInput = document.getElementById('login-email');
    const passInput = document.getElementById('login-password');
    if (emailInput) {
      emailInput.oninput = (e) => {
        state.loginEmail = e.target.value;
      };
    }
    if (passInput) {
      passInput.oninput = (e) => {
        state.loginPassword = e.target.value;
      };
    }
    const togglePassBtn = document.getElementById('toggle-pass-btn');
    if (togglePassBtn) {
      togglePassBtn.onclick = () => {
        state.loginEmail = document.getElementById('login-email')?.value || state.loginEmail;
        state.loginPassword = document.getElementById('login-password')?.value || state.loginPassword;
        state.showLoginPassword = !state.showLoginPassword;
        render();
      };
    }
    const quickLoginBtn = document.getElementById('biometric-quick-login-btn');
    if (quickLoginBtn && state.savedAccount?.token) {
      quickLoginBtn.onclick = async () => {
        state.loginError = '';
        state.loginSuccess = '';
        state.isLoggingIn = true;
        render();
        try {
          await verifyBrowserBiometricIfEnrolled();
          state.token = state.savedAccount.token;
          localStorage.setItem('bazmo_web_token', state.token);
          state.hasSyncedOnce = false;
          const cached = readCachedAppState(state.savedAccount.uid);
          if (cached) {
            state.session = cached.session;
            state.db = cached.db;
            state.province = cached.session.user?.province || state.province;
            state.city = cached.session.user?.city || state.city;
            state.region = cached.session.user?.region || state.region;
            state.isLoggingIn = false;
            render();
            loadAppState(true);
            return;
          }
          await loadAppState(false);
        } catch (err) {
          state.token = '';
          localStorage.removeItem('bazmo_web_token');
          state.loginError = err?.name === 'NotAllowedError'
            ? 'احراز هویت اثر انگشت تایید نشد.'
            : (err?.message || 'نشست ذخیره‌شده منقضی شده است. لطفاً با رمز عبور وارد شوید.');
        } finally {
          state.isLoggingIn = false;
          render();
        }
      };
    }
    const forgetBtn = document.getElementById('forget-saved-account-btn');
    if (forgetBtn) {
      forgetBtn.onclick = () => {
        logoutLocal(true);
        state.loginEmail = '';
        render();
      };
    }
    const form = document.getElementById('password-login-form');
    if (form) {
      form.onsubmit = async (e) => {
        e.preventDefault();
        state.loginEmail = (document.getElementById('login-email')?.value || '').trim();
        state.loginPassword = document.getElementById('login-password')?.value || '';
        state.loginError = '';
        state.loginSuccess = '';
        state.isLoggingIn = true;
        render();
        try {
          const data = await api('/api/auth/login', {
            method: 'POST',
            body: { email: state.loginEmail, password: state.loginPassword }
          });
          state.token = data.token;
          state.session = data;
          localStorage.setItem('bazmo_web_token', state.token);
          saveAccountForQuickLogin(data, data.token);
          state.hasSyncedOnce = false;
          await loadAppState(false, false, true);
        } catch (err) {
          state.loginError = err.message;
        } finally {
          state.isLoggingIn = false;
          render();
        }
      };
    }
    const regBtn = document.getElementById('open-reg-modal-btn');
    if (regBtn) {
      regBtn.onclick = () => openRegistrationModal();
    }
    const themeBtn = document.getElementById('login-theme-btn');
    if (themeBtn) {
      themeBtn.onclick = toggleTheme;
    }
  }

  function openRegistrationModal(prefill = null) {
    const form = {
      email: prefill?.email || '',
      pass: '',
      name: prefill?.fullName || '',
      phone: '',
      tier: ALL_ROLES[0],
      prov: 'تهران',
      city: 'تهران',
      reg: 'همه مناطق',
      note: ''
    };
    const syncDomToForm = () => {
      const g = (id) => document.getElementById(id)?.value;
      if (g('reg-email') !== undefined) form.email = g('reg-email');
      if (g('reg-pass') !== undefined) form.pass = g('reg-pass');
      if (g('reg-name') !== undefined) form.name = g('reg-name');
      if (g('reg-phone') !== undefined) form.phone = g('reg-phone');
      if (g('reg-tier') !== undefined) form.tier = g('reg-tier');
      if (g('reg-note') !== undefined) form.note = g('reg-note');
      if (g('reg-reg') !== undefined) form.reg = g('reg-reg');
    };
    const draw = () => {
      const cities = Object.keys(LOCATIONS[form.prov] || {});
      if (!cities.includes(form.city)) form.city = cities[0] || '';
      const regions = LOCATIONS[form.prov]?.[form.city] || ['همه مناطق'];
      if (!regions.includes(form.reg)) form.reg = regions[0] || 'همه مناطق';

      showModal(`
        <h3 style="margin-top:0;">درخواست عضویت در بازمو</h3>
        <div class="warn-box" style="text-align:right;">
          <strong>⚠️ اطلاعیه مهم احراز هویت و تایید حساب همکاران</strong>
          <div style="margin-top:4px;font-size:12px;line-height:1.7;">
            پس از ثبت درخواست عضویت، تیم پشتیبانی بازمو شماره تماس و اطلاعات صنفی شما را بررسی کرده و حساب شما را فعال می‌کند.
          </div>
        </div>
        <div class="grid" style="grid-template-columns:repeat(3,1fr);gap:8px;margin-bottom:12px;">
          <div>
            <label class="form-label">استان</label>
            <select id="reg-prov" class="select">
              ${Object.keys(LOCATIONS).map((p) => `<option value="${esc(p)}" ${p === form.prov ? 'selected' : ''}>${esc(p)}</option>`).join('')}
            </select>
          </div>
          <div>
            <label class="form-label">شهر</label>
            <select id="reg-city" class="select">
              ${cities.map((c) => `<option value="${esc(c)}" ${c === form.city ? 'selected' : ''}>${esc(c)}</option>`).join('')}
            </select>
          </div>
          <div>
            <label class="form-label">منطقه</label>
            <select id="reg-reg" class="select">
              ${regions.map((r) => `<option value="${esc(r)}" ${r === form.reg ? 'selected' : ''}>${esc(r)}</option>`).join('')}
            </select>
          </div>
        </div>
        <div class="form-group">
          <label class="form-label">ایمیل معتبر جهت ورود</label>
          <input id="reg-email" class="input" type="email" dir="ltr" placeholder="name@example.com" value="${esc(form.email)}" />
        </div>
        <div class="form-group">
          <label class="form-label">رمز عبور انتخابی (جهت ورود پس از تایید، حداقل ۶ کاراکتر)</label>
          <input id="reg-pass" class="input" type="password" dir="ltr" value="${esc(form.pass)}" />
        </div>
        <div class="form-group">
          <label class="form-label">نام و نام خانوادگی</label>
          <input id="reg-name" class="input" placeholder="مثلاً: علی رضایی" value="${esc(form.name)}" />
        </div>
        <div class="form-group">
          <label class="form-label">شماره تماس / واتساپ</label>
          <input id="reg-phone" class="input" dir="ltr" placeholder="0912..." value="${esc(form.phone)}" />
        </div>
        <div class="form-group">
          <label class="form-label">نقش صنفی درخواستی</label>
          <select id="reg-tier" class="select">
            ${ALL_ROLES.map((r) => `<option value="${esc(r)}" ${r === form.tier ? 'selected' : ''}>${esc(r)}</option>`).join('')}
          </select>
        </div>
        <div class="form-group">
          <label class="form-label">توضیحات / نام فروشگاه (اختیاری)</label>
          <textarea id="reg-note" class="textarea" rows="2">${esc(form.note)}</textarea>
        </div>
        <div class="row space-between">
          <button type="button" class="btn btn-outline" onclick="window.__closeModal()">انصراف</button>
          <button type="button" id="reg-submit" class="btn btn-primary">ارسال درخواست</button>
        </div>
      `);

      document.getElementById('reg-prov').onchange = (e) => {
        syncDomToForm();
        form.prov = e.target.value;
        form.city = Object.keys(LOCATIONS[form.prov] || {})[0] || '';
        form.reg = 'همه مناطق';
        draw();
      };
      document.getElementById('reg-city').onchange = (e) => {
        syncDomToForm();
        form.city = e.target.value;
        form.reg = 'همه مناطق';
        draw();
      };
      document.getElementById('reg-submit').onclick = async () => {
        const payload = {
          email: document.getElementById('reg-email').value.trim(),
          password: document.getElementById('reg-pass').value,
          fullName: document.getElementById('reg-name').value.trim(),
          phone: document.getElementById('reg-phone').value.trim(),
          requestedTier: document.getElementById('reg-tier').value,
          province: document.getElementById('reg-prov').value,
          city: document.getElementById('reg-city').value,
          region: document.getElementById('reg-reg').value,
          note: document.getElementById('reg-note').value.trim()
        };
        try {
          await api('/api/registration-requests', { method: 'POST', body: payload });
          closeModal();
          state.loginSuccess = 'درخواست عضویت شما با موفقیت ثبت شد. پس از تایید مدیر می‌توانید وارد شوید.';
          state.loginError = '';
          render();
        } catch (err) {
          toast(err.message);
        }
      };
    };
    draw();
  }

  function renderAccessGateScreen() {
    const st = state.session.status;
    const dark = isDarkTheme();
    const isBlocked = st === 'inactive' || st === 'blocked';
    const isUnregistered = st === 'not_registered' || st === 'not_invited';
    let title = 'حساب کاربری نیاز به تایید دارد';
    let body = 'حساب شما هنوز فعال‌سازی نشده است. می‌توانید درخواست عضویت بفرستید.';
    if (isBlocked) {
      title = 'حساب کاربری غیرفعال است';
      body = 'دسترسی حساب شما توسط مدیر سیستم غیرفعال شده است. برای پیگیری با پشتیبانی تماس بگیرید.';
    }
    return `
      <div class="login-shell">
        <div class="login-hero-bg"></div>
        <div class="login-container">
          <div class="login-topbar">
            <button type="button" id="gate-theme-btn" class="login-theme-btn" aria-label="تغییر حالت روشن یا تاریک">
              <span>${dark ? '☀️ حالت روشن' : '🌙 حالت تاریک'}</span>
            </button>
          </div>
          <div class="card">
            <h2 style="margin-top:0;">${esc(title)}</h2>
            <p class="muted">${esc(state.session.email)}</p>
            <p>${esc(body)}</p>
            ${isUnregistered ? `
              <button id="gate-register-btn" class="btn btn-gold btn-block" style="margin-bottom:10px;">📝 ثبت درخواست عضویت و احراز هویت</button>
            ` : ''}
            <div class="row space-between">
              <button id="gate-refresh" class="btn btn-primary">بررسی مجدد وضعیت</button>
              <button id="gate-logout" class="btn btn-outline">خروج</button>
            </div>
          </div>
        </div>
      </div>
    `;
  }

  // ==================== HEADER & BANNER ====================

  function renderHeader() {
    const fullName = state.session.user?.fullName || state.session.displayName || 'کاربر بازمو';
    const tier = getUserTier();
    const isAdmin = Boolean(state.session.isAdmin);
    const isAdminMode = isAdmin && state.mode === 'admin';
    const unreadNotifs = getUnreadNotificationCount();
    const cartCount = state.cart.reduce((s, x) => s + Number(x.qty || 1), 0);
    const pendingRegCount = (state.db?.registrationRequests || []).filter((r) => r.status === 'pending').length;

    const notifPermission = 'Notification' in window ? Notification.permission : 'unsupported';
    const notifColorStyle = notifPermission === 'granted' ? 'color:#6ee7b7;' : '';

    return `
      <header class="app-header ${isAdminMode ? 'admin-header' : ''}">
        <div class="header-row-1">
          <div class="header-brand">
            <div class="header-logo">
              <svg viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
                <path d="M17 1.01L7 1c-1.1 0-2 .9-2 2v18c0 1.1.9 2 2 2h10c1.1 0 2-.9 2-2V3c0-1.1-.9-1.99-2-1.99zM17 19H7V5h10v14z"/>
              </svg>
            </div>
            <div style="min-width:0;">
              <div class="header-title">${isAdminMode ? 'بازمو · پنل مدیریت' : 'بازمو'}</div>
              <div class="header-sub">${isAdminMode ? 'مدیریت یکپارچه شبکه موبایل ایران' : 'شبکه صنف موبایل ایران'}</div>
            </div>
          </div>

          <div class="header-actions">
            ${!isAdminMode ? `
              <button class="icon-btn" id="hdr-orders-btn" title="سفارش‌ها" aria-label="سفارش‌ها">
                <svg viewBox="0 0 24 24" fill="currentColor"><path d="M21 16.5c0 .38-.21.71-.53.88l-7.9 4.44c-.16.12-.36.18-.57.18-.21 0-.41-.06-.57-.18l-7.9-4.44A.991.991 0 013 16.5v-9c0-.38.21-.71.53-.88l7.9-4.44c.16-.12.36-.18.57-.18.21 0 .41.06.57.18l7.9 4.44c.32.17.53.5.53.88v9zM12 4.15L6.04 7.5 12 10.85l5.96-3.35L12 4.15z"/></svg>
              </button>
              <button class="icon-btn" id="hdr-cart-btn" title="سبد خرید" aria-label="سبد خرید">
                <svg viewBox="0 0 24 24" fill="currentColor"><path d="M7 18c-1.1 0-1.99.9-1.99 2S5.9 22 7 22s2-.9 2-2-.9-2-2-2zM1 2v2h2l3.6 7.59-1.35 2.45c-.16.28-.25.61-.25.96 0 1.1.9 2 2 2h12v-2H7.42c-.14 0-.25-.11-.25-.25l.03-.12.9-1.63h7.45c.75 0 1.41-.41 1.75-1.03l3.58-6.49A1.003 1.003 0 0020 4H5.21l-.94-2H1zm16 16c-1.1 0-1.99.9-1.99 2s.89 2 1.99 2 2-.9 2-2-.9-2-2-2z"/></svg>
                ${cartCount > 0 ? `<span class="icon-badge gold">${toPersianNum(cartCount)}</span>` : ''}
              </button>
            ` : ''}
            <button class="icon-btn" id="hdr-notif-btn" title="اعلان‌ها" aria-label="اعلان‌ها" style="${notifColorStyle}">
              <svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 22c1.1 0 2-.9 2-2h-4c0 1.1.89 2 2 2zm6-6v-5c0-3.07-1.64-5.64-4.5-6.32V4c0-.83-.67-1.5-1.5-1.5s-1.5.67-1.5 1.5v.68C7.63 5.36 6 7.92 6 11v5l-2 2v1h16v-1l-2-2z"/></svg>
              ${unreadNotifs > 0 ? `<span class="icon-badge">${toPersianNum(unreadNotifs)}</span>` : ''}
            </button>
            ${isAdmin ? `
              <button class="icon-btn gold" id="hdr-mode-btn" title="${isAdminMode ? 'نمای کاربر' : 'پنل مدیریت'}" aria-label="تغییر حالت">
                ${isAdminMode
                  ? `<svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/></svg>`
                  : `<svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4zm0 10.99h7c-.53 4.12-3.28 7.79-7 8.94V12H5V6.3l7-3.11v8.8z"/></svg>`}
                ${!isAdminMode && pendingRegCount > 0 ? `<span class="icon-badge">${toPersianNum(pendingRegCount)}</span>` : ''}
              </button>
            ` : ''}
            <button class="icon-btn" id="hdr-theme-btn" title="${isDarkTheme() ? 'حالت روشن' : 'حالت تاریک'}" aria-label="تغییر حالت روشن یا تاریک">
              ${themeSvgIcon(isDarkTheme())}
            </button>
            <button class="icon-btn" id="hdr-logout-btn" title="خروج از حساب" aria-label="خروج">
              <svg viewBox="0 0 24 24" fill="currentColor"><path d="M17 7l-1.41 1.41L18.17 11H8v2h10.17l-2.58 2.58L17 17l5-5zM4 5h8V3H4c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h8v-2H4V5z"/></svg>
            </button>
          </div>
        </div>

        ${!isAdminMode ? `
          <div class="header-row-2">
            <button class="glass-pill profile-pill" id="hdr-profile-btn">
              <svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 3c1.66 0 3 1.34 3 3s-1.34 3-3 3-3-1.34-3-3 1.34-3 3-3zm0 14.2c-2.5 0-4.71-1.28-6-3.22.03-1.99 4-3.08 6-3.08 1.99 0 5.97 1.09 6 3.08-1.29 1.94-3.5 3.22-6 3.22z"/></svg>
              <span class="glass-pill-text">${esc(fullName)}</span>
              <span class="glass-role-tag">${esc(tier)}</span>
            </button>
            <button class="glass-pill location-pill" id="hdr-location-btn">
              <svg viewBox="0 0 24 24" fill="#FFD54F"><path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z"/></svg>
              <span class="glass-pill-text">${esc(
                isAllScope(state.province)
                  ? 'همه کشور'
                  : isAllScope(state.city)
                    ? state.province
                    : `${state.city}${!isAllScope(state.region) ? ` · ${state.region}` : ''}`
              )}</span>
            </button>
          </div>
          <nav class="header-segmented" aria-label="بخش‌های اصلی">
            ${USER_TABS.map((t) => `<button class="seg-tab ${state.userTab === t.id ? 'active' : ''}" data-utab="${t.id}">${t.label}</button>`).join('')}
          </nav>
          <div class="swipe-hint">← برای جابه‌جایی بین منوها به راست و چپ بکشید →</div>
        ` : `
          <nav class="header-segmented admin-tabs" id="admin-tabs-strip" aria-label="منوهای مدیریت">
            ${ADMIN_TABS.map((t) => `<button class="seg-tab ${state.adminTab === t.id ? 'active' : ''}" data-atab="${t.id}">${t.label}</button>`).join('')}
          </nav>
          <div class="swipe-hint">← برای جابه‌جایی بین منوها به راست و چپ بکشید →</div>
        `}
      </header>
    `;
  }

  function renderNotifPermissionBanner() {
    if (!('Notification' in window)) return '';
    if (Notification.permission === 'default') {
      return `
        <div class="notif-banner prompt">
          <span>🔔 برای دریافت اعلان‌های فوری و پیام‌های پس‌زمینه (حتی هنگام بسته بودن تب)، مجوز اعلان را فعال کنید.</span>
          <button class="btn btn-navy btn-sm" id="enable-bg-notif-btn">فعال‌سازی</button>
        </div>
      `;
    }
    if (Notification.permission === 'denied') {
      return `
        <div class="notif-banner denied">
          <span>🔕 اعلان‌های مرورگر مسدود هستند. برای دریافت هشدارهای پس‌زمینه از تنظیمات مرورگر اجازه دهید.</span>
        </div>
      `;
    }
    return '';
  }

  // ==================== PROMOTIONAL BANNERS HELPER ====================

  function compressImageFileToBase64(file, maxWidth = 960, quality = 0.82) {
    return new Promise((resolve, reject) => {
      if (!file) return reject(new Error('فایلی انتخاب نشده است.'));
      const reader = new FileReader();
      reader.onerror = () => reject(new Error('خطا در خواندن فایل تصویر'));
      reader.onload = () => {
        const img = new Image();
        img.onerror = () => reject(new Error('فرمت تصویر پشتیبانی نمی‌شود.'));
        img.onload = () => {
          const scale = img.width > maxWidth ? maxWidth / img.width : 1;
          const w = Math.max(1, Math.round(img.width * scale));
          const h = Math.max(1, Math.round(img.height * scale));
          const canvas = document.createElement('canvas');
          canvas.width = w;
          canvas.height = h;
          const ctx = canvas.getContext('2d');
          ctx.drawImage(img, 0, 0, w, h);
          const dataUrl = canvas.toDataURL('image/jpeg', quality);
          const commaIdx = dataUrl.indexOf(',');
          resolve({
            imageMime: 'image/jpeg',
            imageBase64: commaIdx >= 0 ? dataUrl.slice(commaIdx + 1) : dataUrl,
            previewUrl: dataUrl
          });
        };
        img.src = reader.result;
      };
      reader.readAsDataURL(file);
    });
  }

  function normalizeBannerScope(prov, city, reg) {
    if (isAllScope(prov)) {
      return { province: 'همه استان‌ها', city: 'همه شهرها', region: 'همه مناطق' };
    }
    if (isAllScope(city)) {
      return { province: prov, city: 'همه شهرها', region: 'همه مناطق' };
    }
    if (isAllScope(reg)) {
      return { province: prov, city, region: 'همه مناطق' };
    }
    return { province: prov, city, region: reg };
  }

  function bannerDraftKey(section, slotIdx, loc) {
    const s = normalizeBannerScope(loc.province, loc.city, loc.region);
    return `${section}|${slotIdx}|${normalizeZwnj(s.province)}|${isAllScope(s.city) ? 'all' : normalizeZwnj(s.city)}|${isAllScope(s.region) ? 'all' : normalizeZwnj(s.region)}`;
  }

  function findBannerForExactScope(banners, section, slotIdx, loc) {
    const target = normalizeBannerScope(loc.province, loc.city, loc.region);
    return (banners || []).find((b) => {
      if (b.section !== section || Number(b.slotIndex) !== slotIdx) return false;
      const bLoc = normalizeBannerScope(b.province, b.city, b.region);
      return (
        normalizeZwnj(bLoc.province) === normalizeZwnj(target.province) &&
        normalizeZwnj(bLoc.city) === normalizeZwnj(target.city) &&
        normalizeZwnj(bLoc.region) === normalizeZwnj(target.region)
      );
    }) || null;
  }

  function resolvePromoBannerSlots(section) {
    const active = (state.db?.banners || []).filter((b) => b.section === section && b.hasImage);
    const uProv = state.province;
    const uCity = state.city;
    const uReg = state.region;
    const provAll = isAllScope(uProv);
    const cityAll = isAllScope(uCity);
    const regAll = isAllScope(uReg);

    return [0, 1, 2, 3].map((slotIdx) => {
      const candidates = active.filter((b) => Number(b.slotIndex) === slotIdx);
      if (!candidates.length) return null;
      if (!provAll && !cityAll && !regAll) {
        const regHit = candidates.find(
          (b) =>
            normalizeZwnj(b.province) === normalizeZwnj(uProv) &&
            normalizeZwnj(b.city) === normalizeZwnj(uCity) &&
            normalizeZwnj(b.region) === normalizeZwnj(uReg)
        );
        if (regHit) return regHit;
      }
      if (!provAll && !cityAll) {
        const cityHit = candidates.find(
          (b) =>
            normalizeZwnj(b.province) === normalizeZwnj(uProv) &&
            normalizeZwnj(b.city) === normalizeZwnj(uCity) &&
            isAllScope(b.region)
        );
        if (cityHit) return cityHit;
      }
      if (!provAll) {
        const provHit = candidates.find(
          (b) =>
            normalizeZwnj(b.province) === normalizeZwnj(uProv) &&
            isAllScope(b.city) &&
            isAllScope(b.region)
        );
        if (provHit) return provHit;
      }
      return candidates.find((b) => isAllScope(b.province)) || null;
    });
  }

  function renderPromoBanners(section) {
    const slots = resolvePromoBannerSlots(section);
    return `
      <div class="promo-banner-grid">
        ${slots.map((b, slotIdx) => {
          if (!b) {
            return `
              <button type="button" class="promo-banner-tile promo-banner-empty" data-promo-empty="${slotIdx}" aria-label="اینجا جای تبلیغات شماست">
                <span class="promo-banner-empty-icon">📢</span>
                <span class="promo-banner-empty-title">اینجا جای تبلیغات شماست</span>
                <span class="promo-banner-empty-sub">جایگاه تبلیغاتی ${toPersianNum(slotIdx + 1)}</span>
              </button>
            `;
          }
          const tagLabel = b.linkType === 'url' ? '🌐 لینک سایت' : b.linkType === 'store' ? '🏪 فروشگاه' : '';
          const v = b.updatedAt ? `?v=${encodeURIComponent(b.updatedAt)}` : '';
          return `
            <button type="button" class="promo-banner-tile" data-promo-banner="${esc(b.id)}" aria-label="${esc(b.title || 'بنر تبلیغاتی')}">
              <img class="promo-banner-img" src="/api/banners/${encodeURIComponent(b.id)}/image${v}" alt="${esc(b.title || 'بنر تبلیغاتی بازمو')}" loading="lazy" />
              ${(b.title || tagLabel) ? `
                <div class="promo-banner-overlay">
                  <span class="promo-banner-title">${esc(b.title || 'پیشنهاد ویژه')}</span>
                  ${tagLabel ? `<span class="promo-banner-tag">${tagLabel}</span>` : ''}
                </div>
              ` : ''}
            </button>
          `;
        }).join('')}
      </div>
    `;
  }

  // ==================== USER TABS: 0. MOBILE ====================

  function renderMobileTab() {
    if (state.storeDetailId) {
      return renderStoreDetailView();
    }
    if (state.mobileDetailId) {
      return renderMobileDetailView();
    }

    const stores = state.db.stores || [];
    const mobiles = state.db.mobiles || [];
    const storeMobiles = state.db.storeMobiles || [];

    const filteredStoresRes = filterByLocation(
      stores.filter((s) => s.type === 'mobile' || s.type === 'both'),
      state.province,
      state.city,
      state.region,
      (s) => s
    );

    const brandAliases = {
      'اپل': ['اپل', 'apple'],
      'سامسونگ': ['سامسونگ', 'samsung'],
      'شیائومی': ['شیائومی', 'xiaomi'],
      'آنر': ['آنر', 'honor']
    };
    const brands = ['همه', 'اپل', 'سامسونگ', 'شیائومی', 'آنر'];
    const filteredMobiles = mobiles.filter((m) => {
      const q = state.mobileSearch.trim().toLowerCase();
      const mBrandLower = String(m.brand || '').toLowerCase();
      const brandOk =
        state.mobileBrand === 'همه' ||
        (brandAliases[state.mobileBrand] || [state.mobileBrand.toLowerCase()]).some((b) => mBrandLower.includes(b));
      const searchOk = !q || m.model.toLowerCase().includes(q) || mBrandLower.includes(q);
      if (!brandOk || !searchOk) return false;
      if (state.mobileBudgetMax > 0) {
        const prices = storeMobiles.filter((sm) => sm.mobileId === m.id).map((sm) => Number(sm.price));
        const minP = prices.length ? Math.min(...prices) : 0;
        if (minP === 0 || minP > state.mobileBudgetMax) return false;
      }
      return true;
    });

    const userMobileAds = (state.db.hwAds || []).filter(
      (a) => a.section === 'mobile' && (a.status === 'approved' || a.ownerId === state.session.uid)
    );

    return `
      <div class="row space-between" style="margin-bottom:10px;">
        <input id="mob-search" class="input toolbar-search" placeholder="جستجوی مدل گوشی (مثلاً iPhone 15 یا A55)..." value="${esc(state.mobileSearch)}" />
        <div class="row">
          <select id="mob-budget" class="select" style="width:auto;min-width:160px;">
            <option value="0" ${state.mobileBudgetMax === 0 ? 'selected' : ''}>همه قیمت‌ها</option>
            <option value="25000000" ${state.mobileBudgetMax === 25000000 ? 'selected' : ''}>تا ۲۵ میلیون تومان</option>
            <option value="50000000" ${state.mobileBudgetMax === 50000000 ? 'selected' : ''}>تا ۵۰ میلیون تومان</option>
            <option value="85000000" ${state.mobileBudgetMax === 85000000 ? 'selected' : ''}>تا ۸۵ میلیون تومان</option>
          </select>
          <button id="mob-post-ad-btn" class="btn btn-gold btn-sm">+ ثبت آگهی</button>
          ${canCreateStoreInSection('mobile') ? `<button id="mob-add-store-btn" class="btn btn-navy btn-sm">+ ثبت فروشگاه</button>` : ''}
        </div>
      </div>

      <div class="chip-row">
        ${brands.map((b) => `<button class="chip ${state.mobileBrand === b ? 'active' : ''}" data-mbrand="${esc(b)}">${esc(b)}</button>`).join('')}
      </div>

      ${renderPromoBanners('mobile')}

      <div class="section-header-title">
        <span>🏪 عمده‌فروشان و بنکداران (${esc(filteredStoresRes.scope)})</span>
        <span class="badge badge-blue">${toPersianNum(filteredStoresRes.list.length)} فروشگاه</span>
      </div>
      <div class="store-hscroll">
        ${filteredStoresRes.list.map((s) => `
          <div class="store-hcard" data-open-store="${esc(s.id)}" style="cursor:pointer;">
            <div style="font-weight:800;font-size:14px;">${esc(s.name)}</div>
            <div class="muted">${esc(s.province)} · ${esc(s.city)} (${esc(s.region)})</div>
            <div class="row space-between" style="margin-top:8px;">
              <span class="badge badge-gold">★ ${esc(s.rating)}</span>
              <span class="badge badge-blue">${esc(storeTypeLabel(s.type))}</span>
            </div>
          </div>
        `).join('')}
      </div>

      <div class="section-header-title">
        <span>📱 لیست قیمت و مقایسه موبایل‌ها</span>
        <span class="muted">${toPersianNum(filteredMobiles.length)} مدل</span>
      </div>
      <div class="grid">
        ${filteredMobiles.map((m) => {
          const offers = storeMobiles.filter((sm) => sm.mobileId === m.id);
          const minPrice = offers.length ? Math.min(...offers.map((o) => Number(o.price))) : 0;
          return `
            <div class="card" data-open-mobile="${esc(m.id)}" style="cursor:pointer;">
              <div class="row space-between">
                <div>
                  <span class="badge badge-blue">${esc(m.brand)}</span>
                  <h3 style="margin:6px 0 4px;font-size:16px;">${esc(m.imgEmoji)} ${esc(m.model)}</h3>
                  <div class="muted">${esc(m.specs)}</div>
                </div>
              </div>
              <div class="row space-between" style="margin-top:12px;padding-top:10px;border-top:1px solid var(--border);">
                <div class="price-tag">${minPrice ? 'شروع از ' + formatToman(minPrice) : 'استعلام قیمت'}</div>
                <span class="badge badge-green">${toPersianNum(offers.length)} فروشنده</span>
              </div>
            </div>
          `;
        }).join('')}
      </div>

      ${userMobileAds.length ? `
        <div class="section-header-title" style="margin-top:18px;">
          <span>📣 آگهی‌های ثبت‌شده همکاران در بخش موبایل</span>
          <span class="badge badge-gold">${toPersianNum(userMobileAds.length)} آگهی</span>
        </div>
        <div class="grid">
          ${userMobileAds.map((a) => `
            <div class="card">
              <div class="row space-between">
                <span class="badge badge-blue">${esc(a.cat)}</span>
                ${a.status === 'pending' ? '<span class="badge badge-gold">در انتظار تایید</span>' : '<span class="badge badge-green">تاییدشده</span>'}
              </div>
              <h4 style="margin:8px 0 4px;">${esc(a.title)}</h4>
              <div class="muted">${esc(a.seller)} · ${esc(a.city)} (${esc(a.region || 'همه مناطق')})</div>
              ${a.desc ? `<p class="muted" style="margin:6px 0 0;">${esc(a.desc)}</p>` : ''}
              <div class="row space-between" style="margin-top:10px;">
                <span class="price-tag">${formatToman(a.price)}</span>
                <div class="row">
                  ${a.phone ? `<a class="btn btn-outline btn-sm" href="tel:${esc(a.phone)}">📞 تماس</a>` : ''}
                  <button class="btn btn-navy btn-sm" data-chat-ad="${esc(a.id)}">💬 چت / جزئیات</button>
                  <button class="btn btn-primary btn-sm" data-buy-hw="${esc(a.id)}">خرید</button>
                </div>
              </div>
            </div>
          `).join('')}
        </div>
      ` : ''}
    `;
  }

  function renderMobileDetailView() {
    const m = (state.db.mobiles || []).find((x) => x.id === state.mobileDetailId);
    if (!m) {
      state.mobileDetailId = '';
      return renderMobileTab();
    }
    const offers = (state.db.storeMobiles || [])
      .filter((sm) => sm.mobileId === m.id)
      .sort((a, b) => Number(a.price) - Number(b.price));

    return `
      <div class="row space-between" style="margin-bottom:12px;">
        <button id="mob-back-btn" class="btn btn-outline btn-sm">→ بازگشت به لیست موبایل</button>
        <span class="badge badge-blue">${esc(m.brand)}</span>
      </div>
      <div class="card" style="margin-bottom:12px;background:var(--navy-soft);border-color:var(--navy);">
        <h2 style="margin:0 0 6px;">${esc(m.imgEmoji)} ${esc(m.model)}</h2>
        <div class="muted">${esc(m.specs)}</div>
      </div>
      <div class="section-header-title"><span>مقایسه قیمت فروشندگان (${toPersianNum(offers.length)})</span></div>
      <div class="grid">
        ${offers.map((o, idx) => {
          const store = (state.db.stores || []).find((s) => s.id === o.storeId);
          return `
            <div class="card">
              <div class="row space-between">
                <strong>${esc(store?.name || 'فروشگاه همکار')}</strong>
                ${idx === 0 ? '<span class="badge badge-green">ارزان‌ترین قیمت</span>' : ''}
              </div>
              <div class="muted" style="margin-top:4px;">${esc(store?.city || '')} (${esc(store?.region || '')}) · ${esc(o.info)}</div>
              <div class="row space-between" style="margin-top:10px;">
                <span class="price-tag">${formatToman(o.price)}</span>
                <div class="row">
                  ${store ? `<button class="btn btn-outline btn-sm" data-open-store="${esc(store.id)}">مشاهده فروشگاه</button>` : ''}
                  <button class="btn btn-primary btn-sm" data-buy-offer="${esc(o.id)}">افزودن به سبد</button>
                </div>
              </div>
            </div>
          `;
        }).join('')}
      </div>
    `;
  }

  function renderStoreDetailView() {
    const s = (state.db.stores || []).find((x) => x.id === state.storeDetailId);
    if (!s) {
      state.storeDetailId = '';
      return renderMobileTab();
    }
    const offers = (state.db.storeMobiles || []).filter((sm) => sm.storeId === s.id);
    const accs = (state.db.accessories || []).filter((a) => a.storeId === s.id);
    return `
      <div class="row space-between" style="margin-bottom:12px;">
        <button id="store-back-btn" class="btn btn-outline btn-sm">→ بازگشت</button>
        <span class="badge badge-gold">★ ${esc(s.rating)}</span>
      </div>
      <div class="card" style="margin-bottom:12px;">
        <h2 style="margin:0 0 6px;">🏪 ${esc(s.name)}</h2>
        <div class="muted">📍 ${esc(s.province)} · ${esc(s.city)} (${esc(s.region)})</div>
        <div class="muted" style="margin-top:4px;">آدرس: ${esc(s.address || 'ثبت نشده')}</div>
        <div class="muted" style="margin-top:4px;">ساعات کاری: ${esc(s.hours)} | نوع: ${esc(storeTypeLabel(s.type))}</div>
        ${s.phone ? `<a class="btn btn-primary btn-sm" style="margin-top:10px;" href="tel:${esc(s.phone)}">📞 تماس مستقیم: ${esc(s.phone)}</a>` : ''}
      </div>

      ${offers.length ? `
        <div class="section-header-title"><span>📱 گوشی‌های موجود در این فروشگاه</span></div>
        <div class="grid">
          ${offers.map((o) => {
            const m = (state.db.mobiles || []).find((x) => x.id === o.mobileId);
            return `
              <div class="card">
                <strong>${esc(m?.brand || '')} ${esc(m?.model || 'گوشی')}</strong>
                <div class="muted">${esc(o.info)}</div>
                <div class="row space-between" style="margin-top:8px;">
                  <span class="price-tag">${formatToman(o.price)}</span>
                  <button class="btn btn-primary btn-sm" data-buy-offer="${esc(o.id)}">افزودن به سبد</button>
                </div>
              </div>
            `;
          }).join('')}
        </div>
      ` : ''}

      ${accs.length ? `
        <div class="section-header-title"><span>🎧 لوازم جانبی این فروشگاه</span></div>
        <div class="grid">
          ${accs.map((a) => `
            <div class="card">
              <span class="badge badge-blue">${esc(a.cat)}</span>
              <h4 style="margin:6px 0 4px;">${esc(a.name)}</h4>
              <div class="muted">${esc(a.desc)}</div>
              <div class="row space-between" style="margin-top:8px;">
                <span class="price-tag">${formatToman(a.price)}</span>
                <button class="btn btn-primary btn-sm" data-buy-acc="${esc(a.id)}">خرید عمده</button>
              </div>
            </div>
          `).join('')}
        </div>
      ` : ''}
    `;
  }

  // ==================== USER TABS: 1. SOFTWARE ====================

  function renderSoftwareTab() {
    if (state.swSection === 'forum') {
      return renderForumView('software', () => {
        state.swSection = 'home';
        render();
      });
    }

    const swAccess = canAccessSoftwareBoard();
    const problems = state.db.swProblems || [];
    const tutorials = (state.db.tutorials || []).filter((t) => t.status === 'approved' || t.ownerId === state.session.uid);
    const boxes = (state.db.boxes || []).filter((b) => b.status === 'approved' || b.ownerId === state.session.uid);
    const swRequests = state.db.swRequests || [];
    const q = state.swSearch.trim().toLowerCase();

    return `
      <div class="sub-tabs">
        <button class="sub-tab ${state.swSection === 'home' || state.swSection === 'detail' ? 'active' : ''}" data-swsec="home">${swAccess ? 'آموزش‌ها و حل مشکل' : '🔒 آموزش‌ها (نرم‌افزارکار)'}</button>
        <button class="sub-tab ${state.swSection === 'boxes' ? 'active' : ''}" data-swsec="boxes">🧰 باکس و دانگل</button>
        <button class="sub-tab ${state.swSection === 'req' ? 'active' : ''}" data-swsec="req">📋 درخواست‌ها</button>
        <button class="sub-tab" data-swsec="forum">${swAccess ? '💬 انجمن نرم‌افزار' : '🔒 انجمن نرم‌افزار'}</button>
      </div>

      ${renderPromoBanners('software')}

      ${!swAccess && (state.swSection === 'home' || state.swSection === 'detail') ? `
        <div class="warn-box">
          <div style="font-size:15px;font-weight:900;">🔒 آموزش‌ها و انجمن تخصصی نرم‌افزار فقط برای نقش «نرم‌افزارکار» فعال است</div>
          <p style="margin:6px 0 0;">بخش <strong>درخواست‌ها</strong> و <strong>باکس و دانگل</strong> برای همه همکاران (نقش فعلی: ${esc(getUserTier())}) آزاد است.</p>
          <div class="row" style="justify-content:center;margin-top:10px;">
            <button class="btn btn-primary btn-sm" data-swsec="req">📋 ثبت / مشاهده درخواست‌ها</button>
            <button class="btn btn-navy btn-sm" data-swsec="boxes">🧰 خرید و فروش باکس‌ها</button>
          </div>
        </div>
        <div class="section-header-title"><span>کاشی مشکلات نرم‌افزاری (نمای تخصصی)</span></div>
        <div class="grid" style="grid-template-columns:repeat(auto-fill,minmax(165px,1fr));">
          ${problems.map((p) => `
            <div class="card" style="opacity:0.68;text-align:center;">
              <div style="font-size:28px;">${esc(p.iconEmoji)}</div>
              <h4 style="margin:8px 0 4px;font-size:13.5px;">🔒 ${esc(p.title)}</h4>
              <span class="muted">نیازمند نقش نرم‌افزارکار</span>
            </div>
          `).join('')}
        </div>
      ` : ''}

      ${swAccess && state.swSection === 'home' ? `
        <div class="row space-between" style="margin-bottom:12px;">
          <input id="sw-search" class="input toolbar-search" placeholder="جستجوی مشکل یا عنوان آموزش (مثلاً اپل‌آیدی، شیائومی، FRP)..." value="${esc(state.swSearch)}" />
          <button id="sw-add-tut-btn" class="btn btn-primary btn-sm">+ ثبت آموزش جدید</button>
        </div>

        <div class="grid" style="grid-template-columns:repeat(auto-fill,minmax(165px,1fr));margin-bottom:14px;">
          ${problems.map((p) => {
            const cnt = tutorials.filter((t) => t.problemId === p.id).length;
            return `
              <div class="card" data-open-swp="${esc(p.id)}" style="cursor:pointer;text-align:center;">
                <div style="font-size:30px;">${esc(p.iconEmoji)}</div>
                <h4 style="margin:8px 0 4px;font-size:13.5px;">${esc(p.title)}</h4>
                <span class="badge badge-blue">${toPersianNum(cnt)} آموزش</span>
              </div>
            `;
          }).join('')}
        </div>

        <div class="section-header-title"><span>همه آموزش‌های تخصصی</span></div>
        <div class="grid">
          ${tutorials
            .filter((t) => !q || t.title.toLowerCase().includes(q) || t.teacher.toLowerCase().includes(q))
            .map((t) => renderTutorialCard(t))
            .join('')}
        </div>
      ` : ''}

      ${swAccess && state.swSection === 'detail' ? `
        <button class="btn btn-outline btn-sm" id="sw-back-home" style="margin-bottom:10px;">→ بازگشت به دسته‌بندی مشکلات</button>
        <div class="section-header-title">
          <span>${esc(problems.find((p) => p.id === state.swProblemId)?.title || 'آموزش‌های مرتبط')}</span>
          <button id="sw-add-tut-btn" class="btn btn-primary btn-sm">+ ثبت آموزش در این بخش</button>
        </div>
        <div class="grid">
          ${tutorials
            .filter((t) => !state.swProblemId || t.problemId === state.swProblemId)
            .map((t) => renderTutorialCard(t))
            .join('') || '<div class="card muted">آموزشی در این بخش ثبت نشده است.</div>'}
        </div>
      ` : ''}

      ${state.swSection === 'boxes' ? `
        <div class="row space-between" style="margin-bottom:10px;">
          <input id="sw-search" class="input toolbar-search" placeholder="جستجوی باکس و دانگل..." value="${esc(state.swSearch)}" />
          <button id="sw-add-box-btn" class="btn btn-primary btn-sm">+ ثبت آگهی باکس / دانگل</button>
        </div>
        <div class="grid">
          ${boxes
            .filter((b) => !q || b.name.toLowerCase().includes(q) || b.seller.toLowerCase().includes(q))
            .map((b) => `
              <div class="card">
                <div class="row space-between">
                  <strong>🧰 ${esc(b.name)}</strong>
                  ${b.status === 'pending' ? '<span class="badge badge-gold">در انتظار تایید</span>' : '<span class="badge badge-green">فعال</span>'}
                </div>
                <div class="muted" style="margin-top:4px;">فروشنده: ${esc(b.seller)} · شهر: ${esc(b.city)}</div>
                <div class="muted">وضعیت: ${esc(b.condition)}</div>
                <div class="row space-between" style="margin-top:10px;">
                  <span class="price-tag">${formatToman(b.price)}</span>
                  <div class="row">
                    ${b.ownerId === state.session.uid ? `<button class="btn btn-danger btn-sm" data-del-box="${esc(b.id)}">حذف</button>` : ''}
                    <button class="btn btn-primary btn-sm" data-buy-box="${esc(b.id)}">خرید باکس</button>
                  </div>
                </div>
              </div>
            `).join('')}
        </div>
      ` : ''}

      ${state.swSection === 'req' ? `
        <div class="card" style="margin-bottom:12px;">
          <h4 style="margin-top:0;">ثبت درخواست آموزش یا فایل جدید</h4>
          <div class="row">
            <input id="sw-req-title" class="input" style="flex:1;" placeholder="عنوان مشکل، مدل گوشی یا فایل مورد نیاز..." />
            <button id="sw-req-send" class="btn btn-primary">ارسال به مدیریت</button>
          </div>
        </div>
        <div class="grid">
          ${swRequests.map((r) => `
            <div class="card">
              <div class="row space-between">
                <strong>${esc(r.title)}</strong>
                <span class="badge ${r.reply ? 'badge-green' : 'badge-gold'}">${esc(r.status)}</span>
              </div>
              <div class="muted">${esc(r.userName)} · ${esc(r.city)}</div>
              ${r.reply ? `<div class="alert-success" style="margin-top:8px;margin-bottom:0;text-align:right;">پاسخ ادمین: ${esc(r.reply)}</div>` : ''}
            </div>
          `).join('')}
        </div>
      ` : ''}
    `;
  }

  function renderTutorialCard(t) {
    return `
      <div class="card">
        <div class="row space-between">
          <strong>🎓 ${esc(t.title)}</strong>
          ${t.status === 'pending' ? '<span class="badge badge-gold">در انتظار تایید</span>' : '<span class="badge badge-gold">★ ' + esc(t.rating) + '</span>'}
        </div>
        <div class="muted" style="margin-top:4px;">مدرس: ${esc(t.teacher)} · شهر: ${esc(t.city)}</div>
        <div class="muted">شرایط: ${esc(t.terms)}</div>
        <div class="row space-between" style="margin-top:10px;">
          <span class="price-tag">${formatToman(t.price)}</span>
          <div class="row">
            ${t.ownerId === state.session.uid ? `<button class="btn btn-danger btn-sm" data-del-tut="${esc(t.id)}">حذف</button>` : ''}
            <button class="btn btn-primary btn-sm" data-buy-tut="${esc(t.id)}">خرید دوره</button>
          </div>
        </div>
      </div>
    `;
  }

  // ==================== USER TABS: 2. ACCESSORIES ====================

  function renderAccessoriesTab() {
    const stores = state.db.stores || [];
    const accs = state.db.accessories || [];
    const inquiries = state.db.inquiries || [];
    const accStoresRes = filterByLocation(
      stores.filter((s) => s.type === 'acc' || s.type === 'both'),
      state.province,
      state.city,
      state.region,
      (s) => s
    );
    const cats = ['همه', 'کابل و آداپتور', 'قاب و گارد', 'گلس و محافظ', 'هندزفری و ساعت'];
    const filtered = accs.filter((a) => {
      const catOk = state.accCat === 'همه' || a.cat === state.accCat;
      const storeOk = !state.accFilterStoreId || a.storeId === state.accFilterStoreId;
      const q = state.accSearch.trim().toLowerCase();
      const qOk = !q || a.name.toLowerCase().includes(q) || a.desc.toLowerCase().includes(q);
      return catOk && storeOk && qOk;
    });
    const accUserAds = (state.db.hwAds || []).filter(
      (a) => a.section === 'acc' && (a.status === 'approved' || a.ownerId === state.session.uid)
    );

    return `
      <div class="row space-between" style="margin-bottom:10px;">
        <input id="acc-search" class="input toolbar-search" placeholder="جستجوی لوازم جانبی (گارد، گلس، شارژر)..." value="${esc(state.accSearch)}" />
        <div class="row">
          <button id="acc-post-ad-btn" class="btn btn-gold btn-sm">+ ثبت آگهی</button>
          ${canCreateStoreInSection('acc') ? `<button id="acc-add-store-btn" class="btn btn-navy btn-sm">+ ثبت فروشگاه</button>` : ''}
        </div>
      </div>

      <div class="chip-row">
        ${cats.map((c) => `<button class="chip ${state.accCat === c ? 'active' : ''}" data-acat="${esc(c)}">${esc(c)}</button>`).join('')}
      </div>

      ${renderPromoBanners('acc')}

      <div class="section-header-title">
        <span>🏪 پخش‌کنندگان و بنکداران جانبی (${esc(accStoresRes.scope)})</span>
        ${state.accFilterStoreId ? `<button id="acc-clear-store-filter" class="btn btn-outline btn-sm">نمایش همه فروشگاه‌ها</button>` : ''}
      </div>
      <div class="store-hscroll">
        ${accStoresRes.list.map((s) => {
          const isSel = state.accFilterStoreId === s.id;
          return `
            <div class="store-hcard" data-acc-store="${esc(s.id)}" style="cursor:pointer;${isSel ? 'border:2px solid var(--navy);background:var(--navy-soft);' : ''}">
              <div style="font-weight:800;font-size:14px;">${esc(s.name)}</div>
              <div class="muted">${esc(s.province)} · ${esc(s.city)} (${esc(s.region)})</div>
              <div class="row space-between" style="margin-top:8px;">
                <span class="badge badge-gold">★ ${esc(s.rating)}</span>
                <span class="muted">${isSel ? '✓ فیلتر فعال' : 'فیلتر محصولات'}</span>
              </div>
            </div>
          `;
        }).join('')}
      </div>

      <div class="section-header-title"><span>محصولات و لوازم جانبی</span></div>
      <div class="grid">
        ${filtered.map((a) => {
          const st = stores.find((s) => s.id === a.storeId);
          return `
            <div class="card">
              <span class="badge badge-blue">${esc(a.cat)}</span>
              <h3 style="margin:8px 0 4px;font-size:15px;">${esc(a.name)}</h3>
              <div class="muted">${esc(a.desc)}</div>
              <div class="muted" style="margin-top:4px;">تامین‌کننده: ${esc(st?.name || 'بنکدار جانبی')} (${esc(st?.city || '')})</div>
              <div class="row space-between" style="margin-top:10px;">
                <span class="price-tag">${formatToman(a.price)}</span>
                <div class="row">
                  <button class="btn btn-outline btn-sm" data-inquire-acc="${esc(a.id)}">استعلام موجودی</button>
                  <button class="btn btn-primary btn-sm" data-buy-acc="${esc(a.id)}">خرید عمده</button>
                </div>
              </div>
            </div>
          `;
        }).join('')}
      </div>

      ${inquiries.length ? `
        <div class="section-header-title" style="margin-top:18px;"><span>📋 استعلام‌های ثبت‌شده شما</span></div>
        <div class="grid">
          ${inquiries.map((q) => `
            <div class="card">
              <div class="row space-between">
                <strong>${esc(q.accName)}</strong>
                <span class="badge ${q.status.includes('ناموجود') ? 'badge-red' : q.status.includes('موجود') ? 'badge-green' : 'badge-gold'}">${esc(q.status)}</span>
              </div>
              <div class="muted">${esc(q.storeName)} · ${esc(q.date)}</div>
            </div>
          `).join('')}
        </div>
      ` : ''}

      ${accUserAds.length ? `
        <div class="section-header-title" style="margin-top:18px;"><span>📣 آگهی‌های همکاران در بخش جانبی</span></div>
        <div class="grid">
          ${accUserAds.map((a) => `
            <div class="card">
              <div class="row space-between">
                <span class="badge badge-blue">${esc(a.cat)}</span>
                ${a.status === 'pending' ? '<span class="badge badge-gold">در انتظار تایید</span>' : '<span class="badge badge-green">تاییدشده</span>'}
              </div>
              <h4 style="margin:8px 0 4px;">${esc(a.title)}</h4>
              <div class="muted">${esc(a.seller)} · ${esc(a.city)}</div>
              <div class="row space-between" style="margin-top:10px;">
                <span class="price-tag">${formatToman(a.price)}</span>
                <button class="btn btn-primary btn-sm" data-buy-hw="${esc(a.id)}">خرید</button>
              </div>
            </div>
          `).join('')}
        </div>
      ` : ''}
    `;
  }

  // ==================== USER TABS: 3. HARDWARE ====================

  function renderHardwareTab() {
    if (state.hwSection === 'forum') {
      return renderForumView('hardware', () => {
        state.hwSection = 'home';
        render();
      });
    }

    const ads = state.db.hwAds || [];
    const cats = ['همه', ...(state.db.hwCategories || []).map((c) => c.name)];
    const hwCategories = state.db.hwCategories || [];
    const hwStoresRes = filterByLocation(
      (state.db.stores || []).filter((s) => s.type === 'hw' || s.type === 'both'),
      state.province,
      state.city,
      state.region,
      (s) => s
    );

    if (state.hwSection === 'detail' && state.hwSelectedAdId) {
      const ad = ads.find((x) => x.id === state.hwSelectedAdId);
      if (ad) {
        return `
          <button class="btn btn-outline btn-sm" id="hw-back-home" style="margin-bottom:10px;">→ بازگشت به بازار قطعات</button>
          <div class="card">
            <div class="row space-between">
              <span class="badge badge-blue">${esc(ad.cat)}</span>
              ${ad.status === 'approved' ? '<span class="badge badge-green">✓ تاییدشده</span>' : '<span class="badge badge-gold">در انتظار تایید</span>'}
            </div>
            <h2 style="margin:10px 0 6px;">${esc(ad.title)}</h2>
            <div class="muted">برند/مدل: ${esc(ad.brand)} ${esc(ad.model)} (${esc(ad.series)})</div>
            <div class="muted">فروشنده: ${esc(ad.seller)} · ${esc(ad.province)} - ${esc(ad.city)} (${esc(ad.region || 'همه مناطق')})</div>
            <p style="margin:12px 0;">${esc(ad.desc || 'بدون توضیحات تکمیلی')}</p>
            <div class="row space-between" style="padding-top:10px;border-top:1px solid var(--border);">
              <span class="price-tag" style="font-size:18px;">${formatToman(ad.price)}</span>
              <div class="row">
                ${ad.phone ? `<a class="btn btn-outline" href="tel:${esc(ad.phone)}">📞 تماس مستقیم</a>` : ''}
                <button class="btn btn-navy" data-chat-ad="${esc(ad.id)}">💬 چت با فروشنده</button>
                <button class="btn btn-primary" data-buy-hw="${esc(ad.id)}">🛒 افزودن به سبد</button>
              </div>
            </div>
          </div>
        `;
      }
    }

    const myAds = ads.filter((a) => a.ownerId === state.session.uid);
    const hwRequests = state.db.hwRequests || [];
    const visibleAds = ads.filter(
      (a) =>
        (a.section === 'hw' || !a.section) &&
        (a.status === 'approved' || a.ownerId === state.session.uid) &&
        (state.hwCat === 'همه' || a.cat === state.hwCat) &&
        (!state.hwSearch.trim() ||
          a.title.toLowerCase().includes(state.hwSearch.toLowerCase()) ||
          a.model.toLowerCase().includes(state.hwSearch.toLowerCase()))
    );
    const locAdsRes = filterByLocation(visibleAds, state.province, state.city, state.region, (a) => a);

    return `
      <div class="sub-tabs">
        <button class="sub-tab ${state.hwSection === 'home' ? 'active' : ''}" data-hwsec="home">بازار قطعات</button>
        <button class="sub-tab ${state.hwSection === 'my_ads' ? 'active' : ''}" data-hwsec="my_ads">آگهی‌های من (${toPersianNum(myAds.length)})</button>
        <button class="sub-tab ${state.hwSection === 'special_req' ? 'active' : ''}" data-hwsec="special_req">درخواست قطعه</button>
        <button class="sub-tab" data-hwsec="forum">💬 انجمن سخت‌افزار</button>
      </div>

      ${renderPromoBanners('hw')}

      ${state.hwSection === 'home' ? `
        <div class="row space-between" style="margin-bottom:10px;">
          <input id="hw-search" class="input toolbar-search" placeholder="جستجوی قطعه، آی‌سی، فلت یا مدل گوشی..." value="${esc(state.hwSearch)}" />
          <div class="row">
            <button id="hw-new-ad-btn" class="btn btn-primary btn-sm">+ ثبت آگهی قطعه</button>
            ${canCreateStoreInSection('hw') ? `<button id="hw-add-store-btn" class="btn btn-navy btn-sm">+ ثبت فروشگاه قطعات</button>` : ''}
          </div>
        </div>

        <div class="section-header-title">
          <span>🏪 فروشگاه‌های قطعات سخت‌افزار (${esc(hwStoresRes.scope)})</span>
        </div>
        <div class="store-hscroll">
          ${hwStoresRes.list.map((s) => `
            <div class="store-hcard" data-open-store="${esc(s.id)}" style="cursor:pointer;">
              <div style="font-weight:800;font-size:14px;">${esc(s.name)}</div>
              <div class="muted">${esc(s.city)} (${esc(s.region)})</div>
              <div class="row space-between" style="margin-top:8px;">
                <span class="badge badge-gold">★ ${esc(s.rating)}</span>
                <span class="badge badge-blue">${esc(storeTypeLabel(s.type))}</span>
              </div>
            </div>
          `).join('')}
        </div>

        <div class="grid" style="grid-template-columns:repeat(auto-fill,minmax(130px,1fr));margin-bottom:12px;">
          ${hwCategories.map((c) => `
            <div class="card" data-hcat="${esc(c.name)}" style="cursor:pointer;text-align:center;padding:10px;${state.hwCat === c.name ? 'border:2px solid var(--navy);background:var(--navy-soft);' : ''}">
              <div style="font-size:22px;">${esc(c.iconEmoji)}</div>
              <div style="font-size:12px;font-weight:800;margin-top:4px;">${esc(c.name)}</div>
            </div>
          `).join('')}
        </div>

        <div class="chip-row">
          ${cats.map((c) => `<button class="chip ${state.hwCat === c ? 'active' : ''}" data-hcat="${esc(c)}">${esc(c)}</button>`).join('')}
        </div>

        <div class="section-header-title">
          <span>آگهی‌های سخت‌افزار (${esc(locAdsRes.scope)})</span>
          <span class="muted">${toPersianNum(locAdsRes.list.length)} آگهی</span>
        </div>
        <div class="grid">
          ${locAdsRes.list.map((a) => `
            <div class="card">
              <div class="row space-between">
                <span class="badge badge-blue">${esc(a.cat)}</span>
                ${a.status === 'pending' ? '<span class="badge badge-gold">در انتظار تایید</span>' : '<span class="badge badge-green">فعال</span>'}
              </div>
              <h3 style="margin:8px 0 4px;font-size:15px;">${esc(a.title)}</h3>
              <div class="muted">${esc(a.brand)} ${esc(a.model)} · ${esc(a.series)}</div>
              <div class="muted">${esc(a.seller)} · ${esc(a.city)} (${esc(a.region || 'همه مناطق')})</div>
              <div class="row space-between" style="margin-top:10px;">
                <span class="price-tag">${formatToman(a.price)}</span>
                <div class="row">
                  <button class="btn btn-outline btn-sm" data-open-hw="${esc(a.id)}">جزئیات</button>
                  <button class="btn btn-navy btn-sm" data-chat-ad="${esc(a.id)}">چت</button>
                  <button class="btn btn-primary btn-sm" data-buy-hw="${esc(a.id)}">خرید</button>
                </div>
              </div>
            </div>
          `).join('')}
        </div>
      ` : ''}

      ${state.hwSection === 'my_ads' ? `
        <div class="row space-between" style="margin-bottom:10px;">
          <h4 style="margin:0;">آگهی‌های ثبت‌شده توسط شما</h4>
          <button id="hw-new-ad-btn" class="btn btn-primary btn-sm">+ ثبت آگهی جدید</button>
        </div>
        <div class="grid">
          ${myAds.map((a) => `
            <div class="card">
              <div class="row space-between">
                <span class="badge badge-blue">${esc(a.section === 'mobile' ? 'موبایل' : a.section === 'acc' ? 'جانبی' : 'سخت‌افزار')} · ${esc(a.cat)}</span>
                <span class="badge ${a.status === 'approved' ? 'badge-green' : a.status === 'rejected' ? 'badge-red' : 'badge-gold'}">${a.status === 'approved' ? 'تاییدشده' : a.status === 'rejected' ? 'ردشده' : 'در انتظار تایید'}</span>
              </div>
              <h4 style="margin:8px 0 4px;">${esc(a.title)}</h4>
              <div class="price-tag">${formatToman(a.price)}</div>
              <div class="row" style="margin-top:10px;">
                <button class="btn btn-outline btn-sm" data-edit-hw-price="${esc(a.id)}">✏️ ویرایش قیمت</button>
                <button class="btn btn-danger btn-sm" data-del-hw="${esc(a.id)}">حذف</button>
              </div>
            </div>
          `).join('') || '<div class="card muted">هنوز آگهی‌ای ثبت نکرده‌اید.</div>'}
        </div>
      ` : ''}

      ${state.hwSection === 'special_req' ? `
        <div class="card" style="margin-bottom:12px;">
          <h4 style="margin-top:0;">درخواست قطعه کمیاب + جستجوی هوشمند در موجودی بازار</h4>
          <div class="grid" style="grid-template-columns:repeat(auto-fit,minmax(160px,1fr));gap:8px;margin-bottom:8px;">
            <input id="hr-brand" class="input" placeholder="برند (مثلاً اپل)" />
            <input id="hr-model" class="input" placeholder="مدل (مثلاً iPhone 14 Pro Max)" />
            <input id="hr-part" class="input" placeholder="قطعه (مثلاً آی‌سی یا ال‌سی‌دی)" />
            <input id="hr-series" class="input" placeholder="سریال / کیفیت (اختیاری)" />
          </div>
          <button id="hr-submit" class="btn btn-primary">ثبت درخواست و جستجوی هوشمند</button>
        </div>
        <div class="grid">
          ${hwRequests.map((r) => `
            <div class="card">
              <div class="row space-between">
                <strong>${esc(r.brand)} ${esc(r.model)} - ${esc(r.partType)}</strong>
                <span class="badge ${r.note ? 'badge-green' : 'badge-gold'}">${esc(r.status)}</span>
              </div>
              <div class="muted">سریال/پارت: ${esc(r.series || '-')} · ${esc(r.city)}</div>
              ${r.note ? `<div class="alert-success" style="margin-top:8px;margin-bottom:0;text-align:right;">پاسخ همکار / ادمین: ${esc(r.note)}</div>` : ''}
            </div>
          `).join('')}
        </div>
      ` : ''}
    `;
  }

  // ==================== FORUM VIEW (1:1 REDDIT-STYLE) ====================

  function renderForumView(board, onBack) {
    state.forumBoard = board;
    const isHw = board === 'hardware';
    const hasAccess = isHw ? canAccessHardwareBoard() : canAccessSoftwareBoard();
    const flairs = ['همه', ...(isHw ? FORUM_FLAIRS_HW : FORUM_FLAIRS_SW)];
    const allPosts = (state.db.forumPosts || []).filter((p) => p.board === board);

    if (!hasAccess) {
      return `
        <button class="btn btn-outline btn-sm" id="forum-back-btn" style="margin-bottom:10px;">→ بازگشت</button>
        <div class="warn-box">
          <h4>🔒 دسترسی محدود</h4>
          <p>این انجمن مخصوص نقش <strong>${isHw ? 'تعمیرکار' : 'نرم‌افزارکار'}</strong> است.</p>
        </div>
      `;
    }

    if (state.forumThreadId) {
      const post = allPosts.find((p) => p.id === state.forumThreadId);
      if (!post) {
        state.forumThreadId = '';
      } else {
        const uid = state.session.uid;
        const isUp = (post.upvotedBy || []).includes(uid);
        const isDown = (post.downvotedBy || []).includes(uid);
        const isSaved = (post.savedBy || []).includes(uid);
        return `
          <div class="row space-between" style="margin-bottom:10px;">
            <button class="btn btn-outline btn-sm" id="forum-thread-back">→ بازگشت به لیست بحث‌ها</button>
            <button class="btn btn-outline btn-sm" data-forum-save="${esc(post.id)}">${isSaved ? '🔖 ذخیره‌شده' : '📑 ذخیره پست'}</button>
          </div>
          <div class="card" style="margin-bottom:12px;">
            <div class="row space-between">
              <div class="row">
                <span class="badge badge-blue">${esc(post.flair)}</span>
                ${post.pinned ? '<span class="badge badge-gold">📌 پین‌شده</span>' : ''}
                ${post.verifiedSolution ? '<span class="badge badge-green">✓ راه‌حل تاییدشده</span>' : ''}
              </div>
              <span class="muted">${esc(post.timeLabel)}</span>
            </div>
            <h2 style="margin:10px 0 6px;">${esc(post.title)}</h2>
            <div class="muted" style="margin-bottom:10px;">نویسنده: ${esc(post.author)} (${esc(post.authorBadge)}) · ${esc(post.city)} ${post.deviceTag ? '· 📱 ' + esc(post.deviceTag) : ''}</div>
            <p style="margin:0 0 12px;white-space:pre-line;">${esc(post.body)}</p>
            <div class="row">
              <button class="btn btn-sm ${isUp ? 'btn-primary' : 'btn-outline'}" data-forum-vote="up:${esc(post.id)}">▲</button>
              <strong>${toPersianNum(post.votes)}</strong>
              <button class="btn btn-sm ${isDown ? 'btn-danger' : 'btn-outline'}" data-forum-vote="down:${esc(post.id)}">▼</button>
              <span class="muted" style="margin-right:8px;">💬 ${toPersianNum((post.comments || []).length)} دیدگاه</span>
            </div>
          </div>

          <div class="card" style="margin-bottom:12px;">
            <div class="row">
              <input id="forum-comment-input" class="input" style="flex:1;" placeholder="پاسخ یا تجربه خودتان را بنویسید..." />
              <button id="forum-comment-send" class="btn btn-primary" data-post-id="${esc(post.id)}">ارسال پاسخ</button>
            </div>
          </div>

          <div class="section-header-title"><span>دیدگاه‌ها (${toPersianNum((post.comments || []).length)})</span></div>
          ${(post.comments || []).map((c) => {
            const cUp = (c.upvotedBy || []).includes(uid);
            return `
              <div class="card" style="margin-bottom:10px;">
                <div class="row space-between">
                  <strong>${esc(c.author)} <span class="badge badge-blue">${esc(c.badge)}</span></strong>
                  <span class="muted">${esc(c.timeLabel)}</span>
                </div>
                <p style="margin:6px 0;">${esc(c.body)}</p>
                <div class="row space-between">
                  <div class="row">
                    <button class="btn btn-sm ${cUp ? 'btn-primary' : 'btn-outline'}" data-comment-vote="${esc(post.id)}:${esc(c.id)}">▲ ${toPersianNum(c.votes)}</button>
                    <button class="btn btn-outline btn-sm" data-reply-toggle="${esc(c.id)}">↩ پاسخ</button>
                  </div>
                </div>
                <div id="reply-box-${esc(c.id)}" style="display:none;margin-top:8px;">
                  <div class="row">
                    <input id="reply-input-${esc(c.id)}" class="input" style="flex:1;" placeholder="پاسخ به ${esc(c.author)}..." />
                    <button class="btn btn-navy btn-sm" data-send-reply="${esc(post.id)}:${esc(c.id)}">ارسال</button>
                  </div>
                </div>
                ${(c.replies || []).map((rep) => `
                  <div class="forum-reply">
                    <div class="row space-between">
                      <strong style="font-size:12px;">${esc(rep.author)} <span class="badge badge-blue">${esc(rep.badge)}</span></strong>
                      <span class="muted">${esc(rep.timeLabel)}</span>
                    </div>
                    <div style="font-size:13px;margin-top:4px;">${esc(rep.body)}</div>
                  </div>
                `).join('')}
              </div>
            `;
          }).join('') || '<div class="card muted">اولین پاسخ را شما ثبت کنید.</div>'}
        `;
      }
    }

    const q = state.forumSearch.trim().toLowerCase();
    let posts = allPosts.filter((p) => {
      const fOk = state.forumFlair === 'همه' || p.flair === state.forumFlair;
      const qOk = !q || p.title.toLowerCase().includes(q) || p.body.toLowerCase().includes(q) || (p.deviceTag || '').toLowerCase().includes(q);
      return fOk && qOk;
    });

    posts.sort((a, b) => {
      if (a.pinned !== b.pinned) return a.pinned ? -1 : 1;
      if (state.forumSort === 'new') return Number(b.createdAtMs || 0) - Number(a.createdAtMs || 0);
      if (state.forumSort === 'top') return Number(b.votes || 0) - Number(a.votes || 0);
      // hot score
      return (Number(b.votes || 0) + (b.comments || []).length * 2) - (Number(a.votes || 0) + (a.comments || []).length * 2);
    });

    return `
      <div class="row space-between" style="margin-bottom:10px;">
        <button class="btn btn-outline btn-sm" id="forum-back-btn">→ بازگشت</button>
        <strong>${isHw ? '💬 انجمن سخت‌افزار تعمیرکاران' : '💬 انجمن تخصصی نرم‌افزارکاران'}</strong>
        <button class="btn btn-primary btn-sm" id="forum-new-post-btn">+ ارسال پست</button>
      </div>

      <div class="row space-between" style="margin-bottom:10px;">
        <input id="forum-search" class="input toolbar-search" placeholder="جستجو در موضوعات، مدل گوشی یا خطاها..." value="${esc(state.forumSearch)}" />
        <div class="row">
          <button class="chip ${state.forumSort === 'hot' ? 'active' : ''}" data-fsort="hot">🔥 داغ</button>
          <button class="chip ${state.forumSort === 'new' ? 'active' : ''}" data-fsort="new">🕒 جدید</button>
          <button class="chip ${state.forumSort === 'top' ? 'active' : ''}" data-fsort="top">⭐ برترین‌ها</button>
        </div>
      </div>

      <div class="chip-row">
        ${flairs.map((fl) => `<button class="chip ${state.forumFlair === fl ? 'active' : ''}" data-fflair="${esc(fl)}">${esc(fl)}</button>`).join('')}
      </div>

      <div class="grid">
        ${posts.map((p) => `
          <div class="card">
            <div class="row space-between">
              <div class="row">
                <span class="badge badge-blue">${esc(p.flair)}</span>
                ${p.pinned ? '<span class="badge badge-gold">📌 پین</span>' : ''}
                ${p.verifiedSolution ? '<span class="badge badge-green">✓ حل‌شده</span>' : ''}
              </div>
              <span class="muted">${esc(p.timeLabel)}</span>
            </div>
            <h3 style="margin:8px 0 4px;font-size:15px;cursor:pointer;" data-open-thread="${esc(p.id)}">${esc(p.title)}</h3>
            <div class="muted">${esc(p.author)} (${esc(p.authorBadge)}) · ${esc(p.city)} ${p.deviceTag ? '· ' + esc(p.deviceTag) : ''}</div>
            <p class="muted" style="margin:6px 0 10px;">${esc(p.body.slice(0, 140))}${p.body.length > 140 ? '...' : ''}</p>
            <div class="row space-between">
              <div class="row">
                <button class="btn btn-sm ${(p.upvotedBy || []).includes(state.session.uid) ? 'btn-primary' : 'btn-outline'}" data-forum-vote="up:${esc(p.id)}">▲ ${toPersianNum(p.votes)}</button>
                <button class="btn btn-sm ${(p.downvotedBy || []).includes(state.session.uid) ? 'btn-danger' : 'btn-outline'}" data-forum-vote="down:${esc(p.id)}">▼</button>
              </div>
              <button class="btn btn-outline btn-sm" data-open-thread="${esc(p.id)}">💬 ${toPersianNum((p.comments || []).length)} دیدگاه</button>
            </div>
          </div>
        `).join('') || '<div class="card muted">موردی در انجمن یافت نشد.</div>'}
      </div>
    `;
  }

  // ==================== ADMIN SHELL (6 TABS + FULL FEATURE PARITY) ====================

  function renderAdminShell() {
    const db = state.db;
    const users = db.users || [];
    const reqs = db.registrationRequests || [];
    const pendingRegs = reqs.filter((r) => r.status === 'pending');
    const ads = db.hwAds || [];
    const pendingAds = ads.filter((a) => a.status === 'pending');
    const tuts = db.tutorials || [];
    const boxes = db.boxes || [];
    const stores = db.stores || [];
    const deskRentals = db.deskRentals || [];
    const orders = db.orders || [];
    const banners = db.banners || [];
    const revenue = orders.reduce((s, o) => s + Number(o.total || 0), 0);

    if (state.adminTab === 'dashboard') {
      const demoVisible = db.showDemoData !== false;
      const pendingStoresCount = stores.filter((s) => s.status === 'pending').length;
      const pendingDesksCount = deskRentals.filter((d) => d.status === 'pending').length;
      const pendingTutsCount = tuts.filter((t) => t.status === 'pending').length;
      const pendingBoxesCount = boxes.filter((b) => b.status === 'pending').length;
      const totalPendingContent = pendingAds.length + pendingStoresCount + pendingDesksCount + pendingTutsCount + pendingBoxesCount;

      return `
        <div class="admin-dash-hero">
          <div class="row space-between" style="align-items:flex-start;">
            <div>
              <div style="font-size:12px;color:#BAE6FD;font-weight:700;">داشبورد هوشمند مدیریت بازمو</div>
              <div style="font-size:26px;font-weight:900;margin-top:4px;">${formatToman(revenue)}</div>
              <div style="font-size:11.5px;color:#E0F2FE;margin-top:4px;">مجموع گردش مالی ثبت‌شده در ${toPersianNum(orders.length)} سفارش</div>
            </div>
            <button type="button" class="btn btn-gold btn-sm" data-goto-atab="ads" data-goto-adsec="banners">🖼️ مدیریت بنرهای تبلیغاتی</button>
          </div>
        </div>

        <div class="demo-toggle-card">
          <div class="row space-between">
            <div style="flex:1;min-width:200px;">
              <strong style="color:var(--navy-deep);">نمایش داده‌های نمونه کل ایران (موقت)</strong>
              <div class="muted" style="margin-top:3px;">
                اطلاعات واقعی کاربران و مدیر همیشه حفظ می‌شوند. با این دکمه فقط داده‌های نمونه دمو نمایش داده یا مخفی می‌شوند.
              </div>
            </div>
            <div class="row">
              <button id="admin-toggle-demo-btn" class="toggle-switch-btn ${demoVisible ? 'on' : 'off'}" ${state.isSeedingDemo ? 'disabled' : ''}>
                ${demoVisible ? '✓ نمایش: روشن' : '✕ نمایش: مخفی'}
              </button>
              <button id="admin-seed-demo-btn" class="btn btn-outline btn-sm" ${state.isSeedingDemo ? 'disabled' : ''}>
                ${state.isSeedingDemo ? '⏳ در حال ساخت نمونه...' : '🔄 ساخت / بروزرسانی نمونه ۴۸ منطقه'}
              </button>
            </div>
          </div>
        </div>

        <div class="admin-dash-grid">
          <button type="button" class="admin-dash-tile" data-goto-atab="users" data-goto-user-sub="users">
            <div class="admin-dash-tile-top">
              <span class="admin-dash-icon">👥</span>
              <span class="badge badge-blue">مشاهده همه ←</span>
            </div>
            <div>
              <div class="muted">کل کاربران سیستم</div>
              <div class="stat-num">${toPersianNum(users.length)}</div>
            </div>
          </button>

          <button type="button" class="admin-dash-tile" data-goto-atab="users" data-goto-user-sub="requests">
            <div class="admin-dash-tile-top">
              <span class="admin-dash-icon" style="background:var(--gold-soft);color:var(--gold-text);">📝</span>
              <span class="badge ${pendingRegs.length ? 'badge-gold' : 'badge-green'}">${pendingRegs.length ? 'نیاز به بررسی ←' : 'به‌روز'}</span>
            </div>
            <div>
              <div class="muted">درخواست‌های عضویت منتظر</div>
              <div class="stat-num" style="color:var(--gold);">${toPersianNum(pendingRegs.length)}</div>
            </div>
          </button>

          <button type="button" class="admin-dash-tile" data-goto-atab="ads" data-goto-adsec="hw">
            <div class="admin-dash-tile-top">
              <span class="admin-dash-icon" style="background:#FEF2F2;color:var(--danger);">📢</span>
              <span class="badge ${totalPendingContent ? 'badge-red' : 'badge-green'}">${totalPendingContent ? 'بررسی کاشی‌ها ←' : 'همه تاییدشده'}</span>
            </div>
            <div>
              <div class="muted">آگهی، فروشگاه و میز منتظر</div>
              <div class="stat-num" style="color:var(--danger);">${toPersianNum(totalPendingContent)}</div>
            </div>
          </button>

          <button type="button" class="admin-dash-tile" data-goto-atab="requests">
            <div class="admin-dash-tile-top">
              <span class="admin-dash-icon">📦</span>
              <span class="badge badge-blue">پیگیری ←</span>
            </div>
            <div>
              <div class="muted">کل سفارش‌ها و استعلام‌ها</div>
              <div class="stat-num">${toPersianNum(orders.length)}</div>
            </div>
          </button>

          <button type="button" class="admin-dash-tile" data-goto-atab="ads" data-goto-adsec="stores">
            <div class="admin-dash-tile-top">
              <span class="admin-dash-icon">🏪</span>
              <span class="badge ${pendingStoresCount ? 'badge-gold' : 'badge-blue'}">${pendingStoresCount ? `${toPersianNum(pendingStoresCount)} منتظر ←` : 'مدیریت ←'}</span>
            </div>
            <div>
              <div class="muted">فروشگاه‌ها و بنکداران</div>
              <div class="stat-num">${toPersianNum(stores.length)}</div>
            </div>
          </button>

          <button type="button" class="admin-dash-tile" data-goto-atab="ads" data-goto-adsec="desks">
            <div class="admin-dash-tile-top">
              <span class="admin-dash-icon">🪑</span>
              <span class="badge ${pendingDesksCount ? 'badge-gold' : 'badge-blue'}">${pendingDesksCount ? `${toPersianNum(pendingDesksCount)} منتظر ←` : 'مشاهده ←'}</span>
            </div>
            <div>
              <div class="muted">میزهای کار و همکاری</div>
              <div class="stat-num">${toPersianNum(deskRentals.length + (db.deskRequests || []).length)}</div>
            </div>
          </button>
        </div>

        <div class="card">
          <h4 style="margin-top:0;">دسترسی سریع عملیات مدیریت</h4>
          <div class="row">
            <button class="btn btn-primary btn-sm" data-goto-atab="users" data-goto-user-sub="requests">📝 درخواست‌های عضویت (${toPersianNum(pendingRegs.length)})</button>
            <button class="btn btn-gold btn-sm" data-goto-atab="notifs">🔔 ارسال اعلان منطقه‌ای</button>
            <button class="btn btn-navy btn-sm" data-goto-atab="ads" data-goto-adsec="banners">🖼️ ۴ بنر تبلیغاتی</button>
            <button class="btn btn-outline btn-sm" data-goto-atab="catalog">⚙️ کاتالوگ و انجمن‌ها</button>
          </div>
        </div>
      `;
    }

    if (state.adminTab === 'users') {
      const qRaw = state.adminUserSearch.trim().toLowerCase();
      const qNorm = normalizeZwnj(qRaw);
      const uProvAll = isAllScope(state.adminUserProvince);
      const uCities = uProvAll
        ? ['همه شهرها']
        : ['همه شهرها', ...Object.keys(LOCATIONS[state.adminUserProvince] || {})];
      const filteredUsers = users.filter((u) => {
        const searchOk =
          !qNorm ||
          normalizeZwnj(u.fullName).includes(qNorm) ||
          normalizeZwnj(u.email).includes(qNorm) ||
          normalizeZwnj(u.phone).includes(qNorm) ||
          normalizeZwnj(u.city).includes(qNorm) ||
          normalizeZwnj(u.province).includes(qNorm) ||
          normalizeZwnj(u.tier).includes(qNorm) ||
          (u.fullName || '').toLowerCase().includes(qRaw) ||
          (u.email || '').toLowerCase().includes(qRaw);
        const tierOk = isAllScope(state.adminUserTier) || normalizeZwnj(u.tier) === normalizeZwnj(state.adminUserTier);
        const stOk =
          state.adminUserStatus === 'all' ||
          isAllScope(state.adminUserStatus) ||
          (state.adminUserStatus === 'active' && u.active !== false) ||
          (state.adminUserStatus === 'blocked' && u.active === false);
        const provOk = isAllScope(state.adminUserProvince) || normalizeZwnj(u.province) === normalizeZwnj(state.adminUserProvince);
        const cityOk = uProvAll || isAllScope(state.adminUserCity) || normalizeZwnj(u.city) === normalizeZwnj(state.adminUserCity);
        return searchOk && tierOk && stOk && provOk && cityOk;
      });
      const sortedReqs = [...reqs].sort((a, b) => {
        if (a.status === 'pending' && b.status !== 'pending') return -1;
        if (b.status === 'pending' && a.status !== 'pending') return 1;
        return Number(b.createdAtMs || 0) - Number(a.createdAtMs || 0);
      });

      return `
        <div class="sub-tabs" style="margin-bottom:12px;">
          <button type="button" class="sub-tab ${state.adminUserSubTab === 'users' ? 'active' : ''}" data-au-sub="users">
            👥 کاربران سیستم (${toPersianNum(users.length)})
          </button>
          <button type="button" class="sub-tab ${state.adminUserSubTab === 'requests' ? 'active' : ''}" data-au-sub="requests">
            📝 درخواست‌های عضویت (${toPersianNum(pendingRegs.length)} در انتظار / ${toPersianNum(reqs.length)} کل)
          </button>
        </div>

        ${state.adminUserSubTab === 'requests' ? `
          <div class="section-header-title">
            <span>📝 درخواست‌های عضویت همکاران</span>
            <button id="au-add-btn" class="btn btn-primary btn-sm">+ ثبت مستقیم کاربر جدید</button>
          </div>
          <div class="grid">
            ${sortedReqs.map((r) => `
              <div class="card ${r.status === 'pending' ? 'pending-reg-card' : ''}" style="margin-bottom:0;">
                <div class="row space-between">
                  <strong>${esc(r.fullName || 'بدون نام')}</strong>
                  <span class="badge ${r.status === 'pending' ? 'badge-gold' : r.status === 'approved' ? 'badge-green' : 'badge-red'}">
                    ${r.status === 'pending' ? '⏳ در انتظار تایید' : r.status === 'approved' ? '✓ تاییدشده' : '✕ ردشده'}
                  </span>
                </div>
                <div class="muted" style="margin-top:4px;">${esc(r.email)} · ${esc(r.phone || '-')}</div>
                <div class="muted">📍 ${esc(r.province)} - ${esc(r.city)} (${esc(r.region || 'همه مناطق')})</div>
                ${r.note ? `<div class="muted" style="margin-top:4px;">توضیح: ${esc(r.note)}</div>` : ''}
                ${r.status === 'pending' ? `
                  <div class="row" style="margin-top:10px;">
                    <select class="select" style="flex:1;min-width:140px;" id="reg-tier-${esc(r.id)}">
                      ${ALL_ROLES.map((role) => `<option value="${esc(role)}" ${normalizeZwnj(role) === normalizeZwnj(r.requestedTier) ? 'selected' : ''}>${esc(role)}</option>`).join('')}
                    </select>
                    <button class="btn btn-success btn-sm" data-approve-reg="${esc(r.id)}">✓ تایید</button>
                    <button class="btn btn-danger btn-sm" data-reject-reg="${esc(r.id)}">✕ رد</button>
                  </div>
                ` : `<div class="muted" style="margin-top:8px;">نقش درخواستی: ${esc(r.requestedTier || '-')}</div>`}
              </div>
            `).join('') || '<div class="card muted">درخواست عضویتی ثبت نشده است.</div>'}
          </div>
        ` : `
          <div class="row space-between" style="margin-bottom:10px;">
            <input id="au-search" class="input toolbar-search" placeholder="جستجو بر اساس نام، ایمیل، تلفن یا شهر..." value="${esc(state.adminUserSearch)}" />
            <div class="row">
              <select id="au-tier" class="select" style="width:auto;">
                <option value="همه نقش‌ها" ${isAllScope(state.adminUserTier) ? 'selected' : ''}>همه نقش‌ها</option>
                ${ALL_ROLES.map((r) => `<option value="${esc(r)}" ${normalizeZwnj(state.adminUserTier) === normalizeZwnj(r) ? 'selected' : ''}>${esc(r)}</option>`).join('')}
              </select>
              <select id="au-status" class="select" style="width:auto;">
                <option value="all" ${state.adminUserStatus === 'all' ? 'selected' : ''}>همه وضعیت‌ها</option>
                <option value="active" ${state.adminUserStatus === 'active' ? 'selected' : ''}>فعال</option>
                <option value="blocked" ${state.adminUserStatus === 'blocked' ? 'selected' : ''}>مسدود</option>
              </select>
              <select id="au-prov" class="select" style="width:auto;">
                <option value="همه استان‌ها" ${state.adminUserProvince === 'همه استان‌ها' ? 'selected' : ''}>همه استان‌ها</option>
                <option value="همه کشور" ${state.adminUserProvince === 'همه کشور' ? 'selected' : ''}>همه کشور</option>
                ${Object.keys(LOCATIONS).map((p) => `<option value="${esc(p)}" ${state.adminUserProvince === p ? 'selected' : ''}>${esc(p)}</option>`).join('')}
              </select>
              ${!uProvAll ? `
                <select id="au-city" class="select" style="width:auto;">
                  ${uCities.map((c) => `<option value="${esc(c)}" ${state.adminUserCity === c ? 'selected' : ''}>${esc(c)}</option>`).join('')}
                </select>
              ` : ''}
              <button id="au-add-btn" class="btn btn-primary btn-sm">+ افزودن / ویرایش کاربر</button>
            </div>
          </div>

          <div class="muted" style="margin-bottom:8px;">
            نمایش ${toPersianNum(filteredUsers.length)} از ${toPersianNum(users.length)} کاربر
            ${filteredUsers.length !== users.length ? `<button type="button" id="au-reset-filters-inline" class="btn btn-outline btn-sm" style="margin-right:8px;">پاک کردن فیلترها</button>` : ''}
          </div>

          <div class="grid">
            ${filteredUsers.map((u) => `
              <div class="card">
                <div class="row space-between">
                  <strong>${esc(u.fullName || 'بدون نام')}</strong>
                  <span class="badge ${u.active !== false ? 'badge-green' : 'badge-red'}">${u.active !== false ? 'فعال' : 'مسدود'}</span>
                </div>
                <div class="muted">${esc(u.email)} · ${esc(u.phone || '-')}</div>
                <div class="muted">📍 ${esc(u.province || 'تهران')} - ${esc(u.city || 'تهران')} (${esc(u.region || 'همه مناطق')})</div>
                <div class="row space-between" style="margin-top:10px;">
                  <select class="select" style="width:auto;" data-change-tier="${esc(u.id)}" data-email="${esc(u.email)}">
                    ${ALL_ROLES.map((r) => `<option value="${esc(r)}" ${normalizeZwnj(u.tier) === normalizeZwnj(r) ? 'selected' : ''}>${esc(r)}</option>`).join('')}
                  </select>
                  <div class="row">
                    <button class="btn btn-outline btn-sm" data-edit-user-loc="${esc(u.id)}">📍 آدرس</button>
                    <button class="btn btn-outline btn-sm" data-set-pass="${esc(u.id)}" data-email="${esc(u.email)}">🔑 رمز وب</button>
                    <button class="btn ${u.active !== false ? 'btn-danger' : 'btn-success'} btn-sm" data-toggle-active="${esc(u.id)}" data-next="${u.active === false}">${u.active !== false ? 'مسدود' : 'فعال'}</button>
                    <button class="btn btn-outline btn-sm" data-del-user="${esc(u.id)}" data-email="${esc(u.email)}">حذف</button>
                  </div>
                </div>
              </div>
            `).join('') || `
              <div class="card muted" style="text-align:center;">
                <div style="margin-bottom:8px;">کاربری با فیلتر انتخاب‌شده یافت نشد.</div>
                <button type="button" id="au-reset-filters-btn" class="btn btn-outline btn-sm">پاک کردن فیلترها و نمایش همه کاربران</button>
              </div>
            `}
          </div>
        `}
      `;
    }

    if (state.adminTab === 'notifs') {
      const nCities = isAllScope(state.adminNotifProv) ? ['همه شهرها'] : ['همه شهرها', ...Object.keys(LOCATIONS[state.adminNotifProv] || {})];
      const nRegions = (isAllScope(state.adminNotifProv) || isAllScope(state.adminNotifCity))
        ? ['همه مناطق']
        : (LOCATIONS[state.adminNotifProv]?.[state.adminNotifCity] || ['همه مناطق']);
      const targetCount = users.filter((u) => {
        const pOk = isAllScope(state.adminNotifProv) || normalizeZwnj(u.province) === normalizeZwnj(state.adminNotifProv);
        const cOk = isAllScope(state.adminNotifCity) || normalizeZwnj(u.city) === normalizeZwnj(state.adminNotifCity);
        const rOk = isAllScope(state.adminNotifRegion) || normalizeZwnj(u.region) === normalizeZwnj(state.adminNotifRegion);
        const tOk = isAllScope(state.adminNotifTier) || normalizeZwnj(u.tier) === normalizeZwnj(state.adminNotifTier);
        return pOk && cOk && rOk && tOk;
      }).length;

      const notifTypes = ['اطلاعیه صنفی', 'فرصت خرید ویژه', 'هشدار و موجودی قطعه', 'آپدیت نرم‌افزار و باکس'];
      return `
        <div class="card" style="margin-bottom:14px;">
          <h4 style="margin-top:0;">ارسال اعلان منطقه‌ای و صنفی</h4>
          <div class="form-group">
            <select id="notif-type" class="select">
              ${notifTypes.map((nt) => `<option value="${esc(nt)}" ${state.adminNotifType === nt ? 'selected' : ''}>نوع اعلان: ${esc(nt)}</option>`).join('')}
            </select>
          </div>
          <div class="form-group"><input id="notif-title" class="input" placeholder="عنوان اعلان" /></div>
          <div class="form-group"><textarea id="notif-body" class="textarea" rows="3" placeholder="متن اعلان..."></textarea></div>
          <div class="grid" style="grid-template-columns:repeat(auto-fit,minmax(140px,1fr));gap:8px;margin-bottom:10px;">
            <select id="notif-prov" class="select">
              <option value="همه استان‌ها" ${state.adminNotifProv === 'همه استان‌ها' ? 'selected' : ''}>همه استان‌ها</option>
              <option value="همه کشور" ${state.adminNotifProv === 'همه کشور' ? 'selected' : ''}>همه کشور</option>
              ${Object.keys(LOCATIONS).map((p) => `<option value="${esc(p)}" ${state.adminNotifProv === p ? 'selected' : ''}>${esc(p)}</option>`).join('')}
            </select>
            <select id="notif-city" class="select">
              ${nCities.map((c) => `<option value="${esc(c)}" ${state.adminNotifCity === c ? 'selected' : ''}>${esc(c)}</option>`).join('')}
            </select>
            <select id="notif-region" class="select">
              ${nRegions.map((r) => `<option value="${esc(r)}" ${state.adminNotifRegion === r ? 'selected' : ''}>${esc(r)}</option>`).join('')}
            </select>
            <select id="notif-tier" class="select">
              <option value="همه">همه نقش‌ها</option>
              ${ALL_ROLES.map((r) => `<option value="${esc(r)}" ${state.adminNotifTier === r ? 'selected' : ''}>${esc(r)}</option>`).join('')}
            </select>
          </div>
          <div class="row space-between">
            <span class="badge badge-blue">تعداد کاربران هدف: ${toPersianNum(targetCount)} نفر</span>
            <button id="notif-send-btn" class="btn btn-primary">ارسال اعلان به این محدوده</button>
          </div>
        </div>
        <div class="grid">
          ${(db.notifications || []).map((n) => `
            <div class="card">
              <div class="row space-between">
                <strong>🔔 ${esc(n.title)}</strong>
                <button class="btn btn-danger btn-sm" data-del-notif="${esc(n.id)}">حذف</button>
              </div>
              <p style="margin:6px 0;">${esc(n.body)}</p>
              <div class="muted">محدوده: ${esc(n.province)} · ${esc(n.city)} (${esc(n.region || 'همه مناطق')}) · نقش: ${esc(n.tierFilter)}</div>
            </div>
          `).join('')}
        </div>
      `;
    }

    if (state.adminTab === 'ads') {
      const hwSectionAds = ads.filter((a) => !a.section || a.section === 'hw');
      const mobileSectionAds = ads.filter((a) => a.section === 'mobile');
      const accSectionAds = ads.filter((a) => a.section === 'acc');

      const tileDefs = [
        { id: 'hw', icon: '🔧', label: 'آگهی‌های سخت‌افزار', total: hwSectionAds.length, pending: hwSectionAds.filter((a) => a.status === 'pending').length },
        { id: 'mobile', icon: '📱', label: 'آگهی‌های موبایل', total: mobileSectionAds.length, pending: mobileSectionAds.filter((a) => a.status === 'pending').length },
        { id: 'acc', icon: '🎧', label: 'آگهی‌های جانبی', total: accSectionAds.length, pending: accSectionAds.filter((a) => a.status === 'pending').length },
        { id: 'stores', icon: '🏪', label: 'فروشگاه‌ها', total: stores.length, pending: stores.filter((s) => s.status === 'pending').length },
        { id: 'desks', icon: '🪑', label: 'میز کار و همکاری', total: deskRentals.length, pending: deskRentals.filter((d) => d.status === 'pending').length },
        { id: 'tutorials', icon: '🎓', label: 'آموزش‌ها', total: tuts.length, pending: tuts.filter((t) => t.status === 'pending').length },
        { id: 'boxes', icon: '🧰', label: 'باکس و دانگل', total: boxes.length, pending: boxes.filter((b) => b.status === 'pending').length },
        { id: 'banners', icon: '🖼️', label: '۴ بنر تبلیغاتی', total: banners.filter((b) => b.hasImage).length, pending: 0, isBannerTile: true }
      ];

      const renderStatusBadge = (st) => {
        if (st === 'approved') return '<span class="badge badge-green">تاییدشده</span>';
        if (st === 'rejected') return '<span class="badge badge-red">ردشده</span>';
        return '<span class="badge badge-gold">در انتظار تایید</span>';
      };

      let sectionBodyHtml = '';
      if (state.adminAdsTile === 'banners') {
        const secLabels = [
          { id: 'mobile', label: '📱 بخش موبایل' },
          { id: 'software', label: '💻 بخش نرم‌افزار' },
          { id: 'acc', label: '🎧 بخش جانبی' },
          { id: 'hw', label: '🔧 بخش سخت‌افزار' }
        ];
        const curSec = state.adminBannerSection || 'mobile';
        const curScope = normalizeBannerScope(state.adminBannerProv, state.adminBannerCity, state.adminBannerRegion);
        const curProvAll = isAllScope(curScope.province);
        const bCities = curProvAll ? ['همه شهرها'] : ['همه شهرها', ...Object.keys(LOCATIONS[curScope.province] || {})];
        const bRegions = (curProvAll || isAllScope(curScope.city))
          ? ['همه مناطق']
          : (LOCATIONS[curScope.province]?.[curScope.city] || ['همه مناطق']);
        const approvedStores = stores.filter((s) => s.status === 'approved');
        const configuredScopesMap = new Map();
        banners.filter((b) => b.section === curSec && b.hasImage).forEach((b) => {
          const bl = normalizeBannerScope(b.province, b.city, b.region);
          const sKey = `${bl.province}|${bl.city}|${bl.region}`;
          const label = isAllScope(bl.province)
            ? '🌍 کل کشور'
            : isAllScope(bl.city)
              ? `📍 استان ${bl.province}`
              : isAllScope(bl.region)
                ? `📍 ${bl.province} · ${bl.city}`
                : `📍 ${bl.city} (${bl.region})`;
          const prev = configuredScopesMap.get(sKey) || { ...bl, label, count: 0 };
          prev.count += 1;
          configuredScopesMap.set(sKey, prev);
        });
        const activeScopeLabel = isAllScope(curScope.province)
          ? '🌍 کل کشور (نمایش در سراسر ایران)'
          : isAllScope(curScope.city)
            ? `📍 استان ${curScope.province} (تمام شهرها)`
            : isAllScope(curScope.region)
              ? `📍 ${curScope.province} ← شهر ${curScope.city} (تمام مناطق)`
              : `📍 ${curScope.province} ← ${curScope.city} ← ${curScope.region}`;

        sectionBodyHtml = `
          <div class="section-header-title">
            <span>🖼️ مدیریت ۴ کاشی بنر تبلیغاتی هر بخش</span>
            <span class="muted">تعیین جداگانه برای کل کشور، استان، شهر یا منطقه</span>
          </div>
          <div class="sub-tabs" style="margin-bottom:10px;">
            ${secLabels.map((s) => `<button type="button" class="sub-tab ${curSec === s.id ? 'active' : ''}" data-banner-sec="${esc(s.id)}">${esc(s.label)}</button>`).join('')}
          </div>
          <div class="card" style="margin-bottom:12px;">
            <div class="row space-between" style="margin-bottom:8px;">
              <strong>🎯 محدوده جغرافیایی بنرها</strong>
              <button type="button" id="banner-scope-all-btn" class="btn btn-sm ${curProvAll ? 'btn-primary' : 'btn-outline'}">🌍 کل کشور</button>
            </div>
            <div class="grid" style="grid-template-columns:repeat(auto-fit,minmax(140px,1fr));gap:8px;">
              <select id="banner-scope-prov" class="select">
                <option value="همه استان‌ها" ${curProvAll ? 'selected' : ''}>همه استان‌ها (کل کشور)</option>
                ${Object.keys(LOCATIONS).map((p) => `<option value="${esc(p)}" ${curScope.province === p ? 'selected' : ''}>استان ${esc(p)}</option>`).join('')}
              </select>
              <select id="banner-scope-city" class="select" ${curProvAll ? 'disabled' : ''}>
                ${bCities.map((c) => `<option value="${esc(c)}" ${curScope.city === c ? 'selected' : ''}>${esc(c)}</option>`).join('')}
              </select>
              <select id="banner-scope-region" class="select" ${curProvAll || isAllScope(curScope.city) ? 'disabled' : ''}>
                ${bRegions.map((r) => `<option value="${esc(r)}" ${curScope.region === r ? 'selected' : ''}>${esc(r)}</option>`).join('')}
              </select>
            </div>
            <div class="muted" style="margin-top:8px;">محدوده در حال ویرایش: <strong>${esc(activeScopeLabel)}</strong></div>
            ${configuredScopesMap.size > 0 ? `
              <div class="banner-scope-pill-row">
                ${Array.from(configuredScopesMap.values()).map((sc) => {
                  const isCur = sc.province === curScope.province && sc.city === curScope.city && sc.region === curScope.region;
                  return `<button type="button" class="btn btn-sm ${isCur ? 'btn-primary' : 'btn-outline'}" data-jump-banner-scope="${esc(sc.province)}|${esc(sc.city)}|${esc(sc.region)}">${esc(sc.label)} (${toPersianNum(sc.count)})</button>`;
                }).join('')}
              </div>
            ` : ''}
          </div>
          <div class="grid">
            ${[0, 1, 2, 3].map((slotIdx) => {
              const existing = findBannerForExactScope(banners, curSec, slotIdx, curScope);
              const dKey = bannerDraftKey(curSec, slotIdx, curScope);
              const draft = state.adminBannerDrafts[dKey] || {};
              const hasDraftImg = Boolean(draft.previewUrl && draft.imageBase64);
              const curTitle = draft.title !== undefined ? draft.title : (existing?.title || '');
              const lType = draft.linkType !== undefined ? draft.linkType : (existing?.linkType || 'none');
              const curLinkTarget = draft.linkTarget !== undefined ? draft.linkTarget : (existing?.linkTarget || '');
              return `
                <div class="card">
                  <div class="row space-between" style="margin-bottom:8px;">
                    <strong>بنر شماره ${toPersianNum(slotIdx + 1)}</strong>
                    ${hasDraftImg
                      ? `<span class="badge badge-gold">تصویر جدید آماده ذخیره</span>`
                      : existing?.hasImage
                        ? `<span class="badge badge-green">تصویر فعال</span>`
                        : `<span class="badge badge-blue">خالی</span>`}
                  </div>
                  ${hasDraftImg ? `
                    <img src="${draft.previewUrl}"
                         alt="پیش‌نمایش بنر جدید ${slotIdx + 1}"
                         style="width:100%;aspect-ratio:16/9;object-fit:cover;border-radius:12px;margin-bottom:8px;border:1.5px solid var(--navy);" />
                  ` : existing?.hasImage ? `
                    <img src="/api/banners/${encodeURIComponent(existing.id)}/image?v=${encodeURIComponent(existing.updatedAt || 0)}"
                         alt="پیش‌نمایش بنر ${slotIdx + 1}"
                         style="width:100%;aspect-ratio:16/9;object-fit:cover;border-radius:12px;margin-bottom:8px;border:1px solid var(--border);" />
                  ` : `
                    <div class="promo-banner-tile promo-banner-empty" style="margin-bottom:8px;cursor:default;">
                      <span class="promo-banner-empty-icon">📢</span>
                      <span class="promo-banner-empty-title">اینجا جای تبلیغات شماست</span>
                      <span class="promo-banner-empty-sub">هنوز تصویری برای این محدوده بارگذاری نشده است</span>
                    </div>
                  `}
                  <div class="form-group">
                    <label class="form-label">انتخاب تصویر جدید (PNG / JPG / WebP)</label>
                    <input type="file" accept="image/*" class="input" data-banner-file="${slotIdx}" id="banner-file-${slotIdx}" />
                  </div>
                  <div class="form-group">
                    <label class="form-label">عنوان روی کاشی (اختیاری)</label>
                    <input id="banner-title-${slotIdx}" data-banner-title="${slotIdx}" class="input" placeholder="مثلاً جشنواره تعویض ال‌سی‌دی..." value="${esc(curTitle)}" />
                  </div>
                  <div class="form-group">
                    <label class="form-label">عملیات هنگام کلیک روی بنر</label>
                    <select id="banner-linktype-${slotIdx}" class="select" data-banner-linktype="${slotIdx}">
                      <option value="none" ${lType === 'none' ? 'selected' : ''}>فقط نمایش بنر (بدون لینک)</option>
                      <option value="url" ${lType === 'url' ? 'selected' : ''}>لینک به سایت / صفحه وب خارجی</option>
                      <option value="store" ${lType === 'store' ? 'selected' : ''}>لینک به فروشگاه داخلی داخل بازمو</option>
                    </select>
                  </div>
                  <div class="form-group" id="banner-url-wrap-${slotIdx}" style="display:${lType === 'url' ? 'block' : 'none'};">
                    <label class="form-label">آدرس سایت (https://...)</label>
                    <input id="banner-url-${slotIdx}" data-banner-url="${slotIdx}" class="input" dir="ltr" placeholder="https://example.com" value="${lType === 'url' ? esc(curLinkTarget) : ''}" />
                  </div>
                  <div class="form-group" id="banner-store-wrap-${slotIdx}" style="display:${lType === 'store' ? 'block' : 'none'};">
                    <label class="form-label">انتخاب فروشگاه هدف در بازمو</label>
                    <select id="banner-store-${slotIdx}" data-banner-store="${slotIdx}" class="select">
                      <option value="">-- انتخاب فروشگاه --</option>
                      ${approvedStores.map((st) => `<option value="${esc(st.id)}" ${lType === 'store' && curLinkTarget === st.id ? 'selected' : ''}>${esc(st.name)} (${esc(st.city)})</option>`).join('')}
                    </select>
                  </div>
                  <div class="row space-between">
                    ${existing ? `<button type="button" class="btn btn-danger btn-sm" data-del-banner="${esc(existing.id)}" data-del-dkey="${esc(dKey)}">حذف بنر</button>` : '<span></span>'}
                    <button type="button" class="btn btn-primary btn-sm" data-save-banner="${slotIdx}">💾 ذخیره بنر</button>
                  </div>
                </div>
              `;
            }).join('')}
          </div>
        `;
      } else {
        const q = state.adminAdsSearch.trim().toLowerCase();
        const stFilter = state.adminAdsFilter || 'all';
        let sourceList = [];
        if (state.adminAdsTile === 'hw') sourceList = hwSectionAds;
        else if (state.adminAdsTile === 'mobile') sourceList = mobileSectionAds;
        else if (state.adminAdsTile === 'acc') sourceList = accSectionAds;
        else if (state.adminAdsTile === 'stores') sourceList = stores;
        else if (state.adminAdsTile === 'desks') sourceList = deskRentals;
        else if (state.adminAdsTile === 'tutorials') sourceList = tuts;
        else if (state.adminAdsTile === 'boxes') sourceList = boxes;

        const filteredList = sourceList.filter((item) => {
          if (stFilter === 'pending' && item.status !== 'pending') return false;
          if (!q) return true;
          const hay = [item.title, item.name, item.seller, item.teacher, item.city, item.cat, item.storeName, item.phone].filter(Boolean).join(' ').toLowerCase();
          return hay.includes(q);
        });
        const sortedList = [...filteredList].sort((a, b) => {
          if (a.status === 'pending' && b.status !== 'pending') return -1;
          if (b.status === 'pending' && a.status !== 'pending') return 1;
          return Number(b.createdAtMs || 0) - Number(a.createdAtMs || 0);
        });
        const RECENT_LIMIT = 8;
        const visibleList = state.adminAdsShowAll ? sortedList : sortedList.slice(0, RECENT_LIMIT);
        const curTileDef = tileDefs.find((t) => t.id === state.adminAdsTile) || tileDefs[0];

        sectionBodyHtml = `
          <div class="section-header-title">
            <span>${curTileDef.icon} ${esc(curTileDef.label)} (${toPersianNum(filteredList.length)} مورد)</span>
            ${state.adminAdsTile === 'stores' ? `<button id="admin-add-store-btn" class="btn btn-primary btn-sm">+ ثبت فروشگاه جدید</button>` : ''}
          </div>
          <div class="row space-between" style="margin-bottom:10px;">
            <input id="admin-ads-search" class="input toolbar-search" placeholder="جستجو در عنوان، فروشنده، شهر یا دسته‌بندی..." value="${esc(state.adminAdsSearch)}" />
            <div class="row">
              <button type="button" class="chip ${stFilter === 'all' ? 'active' : ''}" data-ad-filter="all">همه (${toPersianNum(sourceList.length)})</button>
              <button type="button" class="chip ${stFilter === 'pending' ? 'active' : ''}" data-ad-filter="pending">در انتظار (${toPersianNum(sourceList.filter((x) => x.status === 'pending').length)})</button>
            </div>
          </div>

          <div class="grid" style="margin-bottom:12px;">
            ${visibleList.map((item) => {
              if (state.adminAdsTile === 'stores') {
                return `
                  <div class="card">
                    <div class="row space-between">
                      <strong>${esc(item.name)}</strong>
                      ${renderStatusBadge(item.status)}
                    </div>
                    <div class="muted">${esc(item.province)} · ${esc(item.city)} (${esc(item.region)}) · ${esc(storeTypeLabel(item.type))}</div>
                    <div class="muted">${esc(item.phone)} · ساعات: ${esc(item.hours)}</div>
                    <div class="row" style="margin-top:8px;">
                      ${item.status !== 'approved' ? `<button class="btn btn-success btn-sm" data-store-status="${esc(item.id)}:approved">✓ تایید فروشگاه</button>` : ''}
                      ${item.status !== 'rejected' ? `<button class="btn btn-gold btn-sm" data-store-status="${esc(item.id)}:rejected">✕ رد</button>` : ''}
                      <button class="btn btn-danger btn-sm" data-del-store="${esc(item.id)}">حذف</button>
                    </div>
                  </div>
                `;
              }
              if (state.adminAdsTile === 'desks') {
                return `
                  <div class="card">
                    <div class="row space-between">
                      <span class="badge badge-blue">${esc(item.category)}</span>
                      ${renderStatusBadge(item.status)}
                    </div>
                    <h4 style="margin:6px 0 4px;">${esc(item.title)}</h4>
                    <div class="muted">${esc(item.storeName)} · ${esc(item.city)} (${esc(item.region)})</div>
                    <div class="row" style="margin-top:8px;">
                      ${item.status !== 'approved' ? `<button class="btn btn-success btn-sm" data-desk-status="${esc(item.id)}:approved">✓ تایید میز</button>` : ''}
                      ${item.status !== 'rejected' ? `<button class="btn btn-gold btn-sm" data-desk-status="${esc(item.id)}:rejected">✕ رد</button>` : ''}
                      <button class="btn btn-danger btn-sm" data-del-desk-rental="${esc(item.id)}">حذف</button>
                    </div>
                  </div>
                `;
              }
              if (state.adminAdsTile === 'tutorials') {
                return `
                  <div class="card">
                    <div class="row space-between">
                      <strong>آموزش: ${esc(item.title)}</strong>
                      ${renderStatusBadge(item.status)}
                    </div>
                    <div class="muted">${esc(item.teacher)} · ${formatToman(item.price)}</div>
                    <div class="row" style="margin-top:8px;">
                      ${item.status !== 'approved' ? `<button class="btn btn-success btn-sm" data-app-tut="${esc(item.id)}">✓ تایید</button>` : ''}
                      <button class="btn btn-danger btn-sm" data-del-tut="${esc(item.id)}">حذف</button>
                    </div>
                  </div>
                `;
              }
              if (state.adminAdsTile === 'boxes') {
                return `
                  <div class="card">
                    <div class="row space-between">
                      <strong>باکس: ${esc(item.name)}</strong>
                      ${renderStatusBadge(item.status)}
                    </div>
                    <div class="muted">${esc(item.seller)} · ${formatToman(item.price)}</div>
                    <div class="row" style="margin-top:8px;">
                      ${item.status !== 'approved' ? `<button class="btn btn-success btn-sm" data-app-box="${esc(item.id)}">✓ تایید</button>` : ''}
                      <button class="btn btn-danger btn-sm" data-del-box="${esc(item.id)}">حذف</button>
                    </div>
                  </div>
                `;
              }
              return `
                <div class="card">
                  <div class="row space-between">
                    <span class="badge badge-blue">${esc(item.cat)}</span>
                    ${renderStatusBadge(item.status)}
                  </div>
                  <h4 style="margin:8px 0 4px;">${esc(item.title)}</h4>
                  <div class="muted">${esc(item.seller)} · ${esc(item.city)} · ${formatToman(item.price)}</div>
                  <div class="row" style="margin-top:8px;">
                    ${item.status !== 'approved' ? `<button class="btn btn-success btn-sm" data-hw-status="${esc(item.id)}:approved">✓ تایید</button>` : ''}
                    ${item.status !== 'rejected' ? `<button class="btn btn-gold btn-sm" data-hw-status="${esc(item.id)}:rejected">✕ رد</button>` : ''}
                    <button class="btn btn-danger btn-sm" data-del-hw="${esc(item.id)}">حذف</button>
                  </div>
                </div>
              `;
            }).join('') || '<div class="card muted">موردی در این بخش یافت نشد.</div>'}
          </div>

          ${filteredList.length > RECENT_LIMIT ? `
            <div style="text-align:center;">
              <button type="button" id="admin-ads-show-all-btn" class="btn btn-outline btn-sm">
                ${state.adminAdsShowAll ? `نمایش فقط ${toPersianNum(RECENT_LIMIT)} مورد اخیر` : `نمایش همه (${toPersianNum(filteredList.length)} مورد)`}
              </button>
            </div>
          ` : ''}
        `;
      }

      return `
        <div class="section-header-title">
          <span>📢 بخش‌های آگهی، فروشگاه و تبلیغات</span>
          <span class="muted">برای بررسی و جستجو روی هر کاشی بزنید</span>
        </div>
        <div class="admin-ads-tiles-grid">
          ${tileDefs.map((t) => `
            <button type="button" class="admin-ads-tile ${state.adminAdsTile === t.id ? 'active' : ''}" data-ad-tile="${esc(t.id)}">
              ${t.isBannerTile
                ? `<span class="admin-ads-tile-badge zero">${toPersianNum(t.total)}</span>`
                : `<span class="admin-ads-tile-badge ${t.pending === 0 ? 'zero' : ''}">${t.pending > 0 ? toPersianNum(t.pending) : '✓'}</span>`}
              <div style="font-size:24px;">${t.icon}</div>
              <div style="font-size:13px;font-weight:900;color:var(--navy-deep);">${esc(t.label)}</div>
              <div class="muted">${t.isBannerTile ? `${toPersianNum(t.total)} بنر فعال` : `${toPersianNum(t.total)} مورد · ${toPersianNum(t.pending)} منتظر`}</div>
            </button>
          `).join('')}
        </div>
        ${sectionBodyHtml}
      `;
    }

    if (state.adminTab === 'requests') {
      return `
        <div class="section-header-title"><span>📦 سفارش‌های ثبت‌شده (${toPersianNum(orders.length)})</span></div>
        <div class="grid" style="margin-bottom:14px;">
          ${orders.map((o) => `
            <div class="card">
              <div class="row space-between">
                <strong>${esc(o.id)} · ${esc(o.userName)}</strong>
                <button class="btn btn-danger btn-sm" data-del-order="${esc(o.id)}">حذف</button>
              </div>
              <div class="muted">${formatToman(o.total)} · ${esc(o.deliveryType)} · ${esc(o.city)} (${esc(o.date)})</div>
              <div style="margin-top:8px;">
                <select class="select" data-order-status="${esc(o.id)}">
                  ${ORDER_STATUSES.map((st) => `<option value="${esc(st)}" ${o.status === st ? 'selected' : ''}>${esc(st)}</option>`).join('')}
                </select>
              </div>
            </div>
          `).join('') || '<div class="card muted">سفارشی ثبت نشده است.</div>'}
        </div>

        <div class="section-header-title"><span>📋 استعلام‌های موجودی جانبی (${toPersianNum((db.inquiries || []).length)})</span></div>
        <div class="grid" style="margin-bottom:14px;">
          ${(db.inquiries || []).map((q) => `
            <div class="card">
              <div class="row space-between">
                <strong>${esc(q.accName)}</strong>
                <span class="badge badge-blue">${esc(q.status)}</span>
              </div>
              <div class="muted">${esc(q.userName)} · ${esc(q.storeName)}</div>
              <div class="row" style="margin-top:8px;">
                <button class="btn btn-success btn-sm" data-inq-status="${esc(q.id)}:موجود شد - آماده ثبت سفارش">✓ موجود شد</button>
                <button class="btn btn-danger btn-sm" data-inq-status="${esc(q.id)}:فعلاً ناموجود">✕ ناموجود</button>
              </div>
            </div>
          `).join('')}
        </div>

        <div class="section-header-title"><span>🛠️ درخواست‌های قطعه کمیاب و راهنمای نرم‌افزار</span></div>
        <div class="grid">
          ${(db.hwRequests || []).map((r) => `
            <div class="card">
              <strong>قطعه: ${esc(r.brand)} ${esc(r.model)} - ${esc(r.partType)}</strong>
              <div class="muted">${esc(r.userName)} · ${esc(r.city)} · ${esc(r.status)}</div>
              <div class="row" style="margin-top:8px;">
                <input class="input" style="flex:1;" id="hr-reply-in-${esc(r.id)}" placeholder="پاسخ / موجودی اعلامی..." value="${esc(r.note || '')}" />
                <button class="btn btn-primary btn-sm" data-reply-hr="${esc(r.id)}">ثبت پاسخ</button>
              </div>
            </div>
          `).join('')}
          ${(db.swRequests || []).map((r) => `
            <div class="card">
              <strong>نرم‌افزار: ${esc(r.title)}</strong>
              <div class="muted">${esc(r.userName)} · ${esc(r.city)} · ${esc(r.status)}</div>
              <div class="row" style="margin-top:8px;">
                <input class="input" style="flex:1;" id="sr-reply-in-${esc(r.id)}" placeholder="لینک فایل یا راهنما..." value="${esc(r.reply || '')}" />
                <button class="btn btn-primary btn-sm" data-reply-sr="${esc(r.id)}">ثبت پاسخ</button>
              </div>
            </div>
          `).join('')}
        </div>
      `;
    }

    // Catalog tab
    return `
      <div class="grid" style="margin-bottom:14px;">
        <div class="card">
          <h4 style="margin-top:0;">📱 افزودن مدل موبایل + قیمت</h4>
          <div class="form-group"><input id="cat-m-brand" class="input" placeholder="برند (اپل، سامسونگ...)" /></div>
          <div class="form-group"><input id="cat-m-model" class="input" placeholder="مدل گوشی" /></div>
          <div class="form-group"><input id="cat-m-specs" class="input" placeholder="مشخصات (مثلاً 256GB · 5G)" /></div>
          <div class="form-group"><input id="cat-m-price" class="input" type="number" dir="ltr" placeholder="قیمت پایه (تومان)" /></div>
          <button id="cat-add-mobile" class="btn btn-primary btn-sm">افزودن موبایل</button>
        </div>

        <div class="card">
          <h4 style="margin-top:0;">🎧 افزودن کالای جانبی</h4>
          <div class="form-group">
            <select id="cat-a-cat" class="select">
              <option value="کابل و آداپتور">کابل و آداپتور</option>
              <option value="قاب و گارد">قاب و گارد</option>
              <option value="گلس و محافظ">گلس و محافظ</option>
              <option value="هندزفری و ساعت">هندزفری و ساعت</option>
            </select>
          </div>
          <div class="form-group"><input id="cat-a-name" class="input" placeholder="نام محصول جانبی" /></div>
          <div class="form-group"><input id="cat-a-price" class="input" type="number" dir="ltr" placeholder="قیمت عمده (تومان)" /></div>
          <div class="form-group"><input id="cat-a-desc" class="input" placeholder="توضیحات کوتاه" /></div>
          <button id="cat-add-acc" class="btn btn-primary btn-sm">افزودن کالای جانبی</button>
        </div>
      </div>

      <div class="section-header-title"><span>💬 مدیریت پست‌های انجمن‌ها (${toPersianNum((db.forumPosts || []).length)})</span></div>
      <div class="grid" style="margin-bottom:14px;">
        ${(db.forumPosts || []).map((p) => `
          <div class="card">
            <div class="row space-between">
              <span class="badge badge-blue">${p.board === 'hardware' ? 'سخت‌افزار' : 'نرم‌افزار'} · ${esc(p.flair)}</span>
              <span class="muted">${esc(p.author)}</span>
            </div>
            <h4 style="margin:6px 0;">${esc(p.title)}</h4>
            <div class="row">
              <button class="btn btn-outline btn-sm" data-fmod="${esc(p.id)}:pinned:${!p.pinned}">${p.pinned ? 'لغو پین' : '📌 پین'}</button>
              <button class="btn btn-outline btn-sm" data-fmod="${esc(p.id)}:verified:${!p.verifiedSolution}">${p.verifiedSolution ? 'لغو تایید' : '✓ راه‌حل تاییدشده'}</button>
              <button class="btn btn-danger btn-sm" data-del-fpost="${esc(p.id)}">حذف</button>
            </div>
          </div>
        `).join('')}
      </div>

      <div class="card" style="border-color:var(--danger);">
        <h4 style="margin-top:0;color:var(--danger);">بازنشانی داده‌های نمونه (Reset)</h4>
        <p class="muted">بازگرداندن داده‌های اولیه و پیش‌فرض کارخانه بدون پاک کردن دسترسی مدیران.</p>
        <button id="cat-reset-btn" class="btn btn-danger btn-sm">بازنشانی داده‌ها به پیش‌فرض</button>
      </div>
    `;
  }

  // ==================== MODALS ====================

  function showModal(html) {
    const root = document.getElementById('modal-root');
    root.innerHTML = `<div class="modal-backdrop" id="modal-bg"><div class="modal-box"><div class="modal-handle"></div>${html}</div></div>`;
    document.getElementById('modal-bg').onclick = (e) => {
      if (e.target.id === 'modal-bg') closeModal();
    };
  }

  function closeModal() {
    const root = document.getElementById('modal-root');
    if (root) root.innerHTML = '';
  }
  window.__closeModal = closeModal;

  function openLocationModal() {
    let prov = isAllScope(state.province) ? 'همه کشور' : state.province;
    let city = isAllScope(prov) ? 'همه شهرها' : state.city;
    let reg = isAllScope(prov) || isAllScope(city) ? 'همه مناطق' : state.region;
    const draw = () => {
      const provOptions = ['همه کشور', ...Object.keys(LOCATIONS)];
      const allCountry = isAllScope(prov);
      const cities = allCountry ? ['همه شهرها'] : ['همه شهرها', ...Object.keys(LOCATIONS[prov] || {})];
      if (!cities.includes(city)) city = cities[0] || 'همه شهرها';
      const regions = allCountry || isAllScope(city)
        ? ['همه مناطق']
        : (LOCATIONS[prov]?.[city] || ['همه مناطق']);
      if (!regions.includes(reg)) reg = regions[0] || 'همه مناطق';
      showModal(`
        <h3 style="margin-top:0;">انتخاب محدوده جغرافیایی</h3>
        <button id="loc-all-country" class="btn btn-gold btn-block" style="margin-bottom:12px;">🌍 نمایش همه کشور (سراسر ایران)</button>
        <div class="form-group">
          <label class="form-label">استان</label>
          <select id="loc-prov" class="select">
            ${provOptions.map((p) => `<option value="${esc(p)}" ${p === prov ? 'selected' : ''}>${esc(p)}</option>`).join('')}
          </select>
        </div>
        <div class="form-group">
          <label class="form-label">شهر</label>
          <select id="loc-city" class="select" ${allCountry ? 'disabled' : ''}>
            ${cities.map((c) => `<option value="${esc(c)}" ${c === city ? 'selected' : ''}>${esc(c)}</option>`).join('')}
          </select>
        </div>
        <div class="form-group">
          <label class="form-label">منطقه</label>
          <select id="loc-reg" class="select" ${allCountry || isAllScope(city) ? 'disabled' : ''}>
            ${regions.map((r) => `<option value="${esc(r)}" ${r === reg ? 'selected' : ''}>${esc(r)}</option>`).join('')}
          </select>
        </div>
        <div class="row space-between">
          <button class="btn btn-outline" onclick="window.__closeModal()">انصراف</button>
          <button id="loc-save" class="btn btn-primary">تایید محدوده</button>
        </div>
      `);
      document.getElementById('loc-all-country').onclick = () => {
        state.province = 'همه کشور';
        state.city = 'همه شهرها';
        state.region = 'همه مناطق';
        syncSessionWithServiceWorker(buildSwSessionPayload());
        closeModal();
        toast('نمایش روی همه کشور تنظیم شد.');
        render();
      };
      document.getElementById('loc-prov').onchange = (e) => {
        prov = e.target.value;
        if (isAllScope(prov)) {
          city = 'همه شهرها';
          reg = 'همه مناطق';
        } else {
          city = Object.keys(LOCATIONS[prov] || {})[0] || 'همه شهرها';
          reg = 'همه مناطق';
        }
        draw();
      };
      document.getElementById('loc-city').onchange = (e) => {
        city = e.target.value;
        reg = 'همه مناطق';
        draw();
      };
      document.getElementById('loc-reg').onchange = (e) => {
        reg = e.target.value;
      };
      document.getElementById('loc-save').onclick = () => {
        state.province = document.getElementById('loc-prov').value;
        state.city = document.getElementById('loc-city').value;
        state.region = document.getElementById('loc-reg').value;
        if (!isAllScope(state.province)) {
          state.prevLocation = { province: state.province, city: state.city, region: state.region };
        }
        syncSessionWithServiceWorker(buildSwSessionPayload());
        closeModal();
        render();
      };
    };
    draw();
  }

  function openNotificationsModal() {
    const visible = getVisibleNotifications();
    const uid = state.session.uid;
    const unreadVisible = visible.filter((n) => !(n.readBy || []).includes(uid));
    const perm = 'Notification' in window ? Notification.permission : 'unsupported';
    showModal(`
      <div class="row space-between" style="margin-bottom:10px;">
        <h3 style="margin:0;">اعلان‌ها و پیام‌های مدیریت (${toPersianNum(visible.length)})</h3>
        <div class="row">
          <button id="modal-refresh-notif" class="btn btn-outline btn-sm">🔄 بروزرسانی</button>
          ${unreadVisible.length > 0 ? `<button id="modal-read-all-notif" class="btn btn-primary btn-sm">✓ خواندن همه</button>` : ''}
          ${perm !== 'granted' && perm !== 'unsupported' ? `<button id="modal-req-notif" class="btn btn-gold btn-sm">🔔 پس‌زمینه</button>` : ''}
        </div>
      </div>
      ${visible.map((n) => {
        const read = (n.readBy || []).includes(uid);
        return `
          <div class="card" style="margin-bottom:8px;${read ? '' : 'border-color:var(--navy);background:var(--navy-soft);'}">
            <div class="row space-between">
              <strong>${esc(n.title)}</strong>
              <span class="muted">${esc(n.date)}</span>
            </div>
            <p style="margin:6px 0;">${esc(n.body)}</p>
            <div class="row space-between">
              <span class="muted">${esc(n.province)} · ${esc(n.city)}</span>
              ${!read ? `<button class="btn btn-primary btn-sm" data-read-notif="${esc(n.id)}">خوانده شد</button>` : '<span class="badge badge-green">خوانده شده</span>'}
            </div>
          </div>
        `;
      }).join('') || '<p class="muted">اعلانی برای محدوده فعلی شما ثبت نشده است.</p>'}
      <button class="btn btn-outline btn-block" onclick="window.__closeModal()">بستن</button>
    `);
    const refreshBtn = document.getElementById('modal-refresh-notif');
    if (refreshBtn) {
      refreshBtn.onclick = async () => {
        await loadAppState(true, true);
        openNotificationsModal();
      };
    }
    const readAllBtn = document.getElementById('modal-read-all-notif');
    if (readAllBtn) {
      readAllBtn.onclick = async () => {
        readAllBtn.disabled = true;
        try {
          await Promise.all(
            unreadVisible.map((n) => api(`/api/notifications/${n.id}/read`, { method: 'POST' }).catch(() => {}))
          );
          await loadAppState(true, true);
          openNotificationsModal();
        } catch (err) {
          toast(err.message);
        }
      };
    }
    const reqBtn = document.getElementById('modal-req-notif');
    if (reqBtn) reqBtn.onclick = requestWebNotifications;
    document.querySelectorAll('[data-read-notif]').forEach((b) => {
      b.onclick = async () => {
        await api(`/api/notifications/${b.dataset.readNotif}/read`, { method: 'POST' });
        await loadAppState(true);
        openNotificationsModal();
      };
    });
  }

  function openCartModal() {
    let deliveryType = 'پیک';
    const draw = () => {
      const total = state.cart.reduce((s, x) => s + Number(x.price) * Number(x.qty), 0);
      showModal(`
        <h3 style="margin-top:0;">🛒 سبد خرید شما (${toPersianNum(state.cart.length)})</h3>
        ${state.cart.map((c, idx) => `
          <div class="card" style="margin-bottom:8px;">
            <div class="row space-between">
              <div>
                <strong>${esc(c.title)}</strong>
                <div class="muted">${esc(c.storeName)} · ${formatToman(c.price)}</div>
              </div>
              <div class="row">
                <button class="btn btn-outline btn-sm" data-cart-dec="${idx}">-</button>
                <strong>${toPersianNum(c.qty)}</strong>
                <button class="btn btn-outline btn-sm" data-cart-inc="${idx}">+</button>
                <button class="btn btn-danger btn-sm" data-cart-del="${idx}">×</button>
              </div>
            </div>
          </div>
        `).join('') || '<p class="muted">سبد خرید خالی است.</p>'}
        ${state.cart.length ? `
          <div class="row" style="margin:10px 0;">
            <button class="chip ${deliveryType === 'پیک' ? 'active' : ''}" id="deliv-courier">🛵 ارسال با پیک</button>
            <button class="chip ${deliveryType === 'حضوری' ? 'active' : ''}" id="deliv-pickup">🏪 تحویل حضوری</button>
          </div>
          ${deliveryType === 'پیک' ? `
            <div class="form-group"><input id="ord-cust-name" class="input" placeholder="نام تحویل‌گیرنده" value="${esc(state.session.user?.fullName || '')}" /></div>
            <div class="form-group"><input id="ord-cust-phone" class="input" dir="ltr" placeholder="شماره تماس" value="${esc(state.session.user?.phone || '')}" /></div>
            <div class="form-group"><input id="ord-cust-addr" class="input" placeholder="آدرس دقیق ارسال..." /></div>
          ` : ''}
          <div class="row space-between" style="margin:12px 0;">
            <strong>جمع کل قابل پرداخت:</strong>
            <strong class="price-tag">${formatToman(total)}</strong>
          </div>
          <button id="cart-checkout" class="btn btn-primary btn-block">ثبت نهایی سفارش</button>
        ` : ''}
      `);
      if (state.cart.length) {
        document.getElementById('deliv-courier').onclick = () => { deliveryType = 'پیک'; draw(); };
        document.getElementById('deliv-pickup').onclick = () => { deliveryType = 'حضوری'; draw(); };
        document.querySelectorAll('[data-cart-dec]').forEach((b) => {
          b.onclick = () => {
            const idx = Number(b.dataset.cartDec);
            if (state.cart[idx].qty > 1) state.cart[idx].qty -= 1;
            else state.cart.splice(idx, 1);
            draw();
            render();
          };
        });
        document.querySelectorAll('[data-cart-inc]').forEach((b) => {
          b.onclick = () => {
            state.cart[Number(b.dataset.cartInc)].qty += 1;
            draw();
            render();
          };
        });
        document.querySelectorAll('[data-cart-del]').forEach((b) => {
          b.onclick = () => {
            state.cart.splice(Number(b.dataset.cartDel), 1);
            draw();
            render();
          };
        });
        document.getElementById('cart-checkout').onclick = async () => {
          const customer = deliveryType === 'پیک'
            ? {
                name: document.getElementById('ord-cust-name').value.trim(),
                phone: document.getElementById('ord-cust-phone').value.trim(),
                address: document.getElementById('ord-cust-addr').value.trim()
              }
            : { name: state.session.user?.fullName || 'حضوری', phone: state.session.user?.phone || '' };
          if (deliveryType === 'پیک' && !customer.address) {
            return toast('لطفاً آدرس ارسال با پیک را وارد کنید.');
          }
          await api('/api/orders', {
            method: 'POST',
            body: {
              userName: state.session.user?.fullName || state.session.displayName,
              items: state.cart,
              total,
              deliveryType,
              customer,
              city: state.city
            }
          });
          state.cart = [];
          closeModal();
          toast('سفارش شما با موفقیت ثبت شد.');
          await loadAppState(true);
        };
      }
    };
    draw();
  }

  function orderStepIndex(status) {
    if (status.includes('تکمیل') || status.includes('تحویل شد')) return 3;
    if (status.includes('پیک')) return 2;
    if (status.includes('تایید') || status.includes('آماده')) return 1;
    return 0;
  }

  function openOrdersModal() {
    const orders = state.db.orders || [];
    showModal(`
      <h3 style="margin-top:0;">📦 سفارش‌های من (${toPersianNum(orders.length)})</h3>
      ${orders.map((o) => {
        const step = orderStepIndex(o.status || '');
        return `
          <div class="card" style="margin-bottom:10px;">
            <div class="row space-between">
              <strong>${esc(o.id)}</strong>
              <span class="badge badge-blue">${esc(o.deliveryType)}</span>
            </div>
            <div class="stepper-wrap">
              ${['ثبت شد', 'آماده‌سازی', 'ارسال', 'تحویل'].map((lbl, idx) => `
                <div class="step-node ${idx <= step ? 'done' : ''}">
                  <div class="step-dot">${idx <= step ? '✓' : toPersianNum(idx + 1)}</div>
                  <span>${lbl}</span>
                </div>
              `).join('')}
            </div>
            <div class="row space-between">
              <span class="price-tag">${formatToman(o.total)}</span>
              <span class="muted">${esc(o.status)} · ${esc(o.date)}</span>
            </div>
          </div>
        `;
      }).join('') || '<p class="muted">هنوز سفارشی ثبت نکرده‌اید.</p>'}
      <button class="btn btn-outline btn-block" onclick="window.__closeModal()">بستن</button>
    `);
  }

  function openProfileModal() {
    const u = state.session.user || {};
    const canBio = Boolean(window.PublicKeyCredential);
    showModal(`
      <div class="row space-between" style="margin-bottom:8px;">
        <h3 style="margin:0;">پروفایل همکار</h3>
        <span class="badge badge-blue">${esc(getUserTier())}</span>
      </div>
      <div class="muted" style="margin-bottom:12px;">نام کاربری (ایمیل): ${esc(state.session.email)}</div>
      <div class="locked-address-box">
        <div style="font-size:12px;font-weight:800;color:var(--navy-deep);">🔒 نشانی صنفی ثبت‌شده (فقط قابل ویرایش توسط مدیریت)</div>
        <div class="muted" style="margin-top:4px;">${esc(u.province || 'تهران')} · ${esc(u.city || 'تهران')} (${esc(u.region || 'همه مناطق')})</div>
      </div>
      <div class="form-group">
        <label class="form-label">نام و نام خانوادگی</label>
        <input id="prof-name" class="input" value="${esc(u.fullName || '')}" />
      </div>
      <div class="form-group">
        <label class="form-label">شماره تماس</label>
        <input id="prof-phone" class="input" dir="ltr" value="${esc(u.phone || '')}" />
      </div>
      <div class="form-group">
        <label class="form-label">تغییر رمز عبور حساب (اختیاری)</label>
        <input id="prof-pass" class="input" type="password" dir="ltr" placeholder="حداقل ۶ کاراکتر" />
      </div>
      ${canBio ? `
        <button type="button" id="prof-bio-btn" class="btn btn-outline btn-block" style="margin-bottom:10px;">
          🔐 ${state.savedAccount?.webauthnCredId ? 'اثر انگشت مرورگر فعال است (به‌روزرسانی)' : 'فعال‌سازی ورود با اثر انگشت مرورگر'}
        </button>
      ` : ''}
      ${canCreateStore() ? `
        <div class="row" style="margin-bottom:10px;">
          <button id="prof-manage-store-btn" class="btn btn-navy" style="flex:1;">🏪 مدیریت فروشگاه من</button>
          <button id="prof-add-store-btn" class="btn btn-gold" style="flex:1;">+ ثبت فروشگاه جدید</button>
        </div>
      ` : ''}
      <div class="row space-between">
        <button class="btn btn-outline" onclick="window.__closeModal()">بستن</button>
        <button id="prof-save" class="btn btn-primary">ذخیره پروفایل</button>
      </div>
    `);
    const bioBtn = document.getElementById('prof-bio-btn');
    if (bioBtn) {
      bioBtn.onclick = async () => {
        try {
          await enrollBrowserBiometric();
          toast('ورود با اثر انگشت / قفل دستگاه با موفقیت فعال شد.');
          openProfileModal();
        } catch (err) {
          toast(err?.message || 'مرورگر از ثبت اثر انگشت پشتیبانی نکرد.');
        }
      };
    }
    const mgBtn = document.getElementById('prof-manage-store-btn');
    if (mgBtn) mgBtn.onclick = () => { closeModal(); state.showMyStore = true; state.showDeskRentals = false; render(); };
    const stBtn = document.getElementById('prof-add-store-btn');
    if (stBtn) stBtn.onclick = openRegisterStoreModal;
    document.getElementById('prof-save').onclick = async () => {
      try {
        await api('/api/users/me', {
          method: 'PATCH',
          body: {
            fullName: document.getElementById('prof-name').value.trim(),
            phone: document.getElementById('prof-phone').value.trim()
          }
        });
        const newPass = document.getElementById('prof-pass').value;
        if (newPass) {
          await api('/api/users/me/password', { method: 'POST', body: { password: newPass } });
        }
        closeModal();
        toast('پروفایل با موفقیت به‌روزرسانی شد.');
        await loadAppState(true);
      } catch (err) {
        toast(err.message);
      }
    };
  }

  function openRegisterStoreModal(requestedSection = null) {
    const isAdminUser = Boolean(state.session?.isAdmin);
    const roleSection = defaultStoreTypeForRole(getUserTier());
    if (!canCreateStore()) {
      return toast('ثبت فروشگاه فقط برای عمده‌فروش موبایل، عمده‌فروش جانبی و قطعه‌فروش مجاز است.');
    }
    if (!isAdminUser && requestedSection && requestedSection !== roleSection) {
      return toast('هر نقش فقط در بخش تخصصی خودش امکان ثبت فروشگاه دارد.');
    }
    const defaultType = isAdminUser
      ? (['mobile', 'acc', 'hw', 'both'].includes(requestedSection) ? requestedSection : 'both')
      : roleSection;
    const form = {
      prov: isAllScope(state.province) ? 'تهران' : state.province,
      city: isAllScope(state.city) ? 'تهران' : state.city,
      reg: isAllScope(state.region) ? 'همه مناطق' : state.region,
      name: '',
      type: defaultType,
      phone: state.session.user?.phone || '',
      addr: '',
      hours: '۱۰ تا ۲۰'
    };
    const syncDomToForm = () => {
      const g = (id) => document.getElementById(id)?.value;
      if (g('st-name') !== undefined) form.name = g('st-name');
      if (g('st-type') !== undefined) form.type = g('st-type');
      if (g('st-phone') !== undefined) form.phone = g('st-phone');
      if (g('st-addr') !== undefined) form.addr = g('st-addr');
      if (g('st-hours') !== undefined) form.hours = g('st-hours');
      if (g('st-reg') !== undefined) form.reg = g('st-reg');
    };

    const draw = () => {
      const cities = Object.keys(LOCATIONS[form.prov] || {});
      if (!cities.includes(form.city)) form.city = cities[0] || '';
      const regions = LOCATIONS[form.prov]?.[form.city] || ['همه مناطق'];
      if (!regions.includes(form.reg)) form.reg = regions[0] || 'همه مناطق';
      showModal(`
        <h3 style="margin-top:0;">🏪 ثبت فروشگاه جدید در بازمو</h3>
        <div class="badge badge-green" style="margin-bottom:10px;">دسترسی بخش: ${esc(storeTypeLabel(form.type))} (${esc(getUserTier())})</div>
        <div class="grid" style="grid-template-columns:repeat(3,1fr);gap:8px;margin-bottom:10px;">
          <select id="st-prov" class="select">${Object.keys(LOCATIONS).map((p) => `<option value="${esc(p)}" ${p === form.prov ? 'selected' : ''}>${esc(p)}</option>`).join('')}</select>
          <select id="st-city" class="select">${cities.map((c) => `<option value="${esc(c)}" ${c === form.city ? 'selected' : ''}>${esc(c)}</option>`).join('')}</select>
          <select id="st-reg" class="select">${regions.map((r) => `<option value="${esc(r)}" ${r === form.reg ? 'selected' : ''}>${esc(r)}</option>`).join('')}</select>
        </div>
        <div class="form-group"><input id="st-name" class="input" placeholder="نام فروشگاه / بنکداری" value="${esc(form.name)}" /></div>
        ${isAdminUser ? `
          <div class="form-group">
            <label class="form-label">حوزه فعالیت فروشگاه</label>
            <select id="st-type" class="select">
              <option value="mobile" ${form.type === 'mobile' ? 'selected' : ''}>فروشگاه موبایل</option>
              <option value="acc" ${form.type === 'acc' ? 'selected' : ''}>فروشگاه لوازم جانبی</option>
              <option value="hw" ${form.type === 'hw' ? 'selected' : ''}>فروشگاه قطعات سخت‌افزار</option>
              <option value="both" ${form.type === 'both' ? 'selected' : ''}>موبایل و جانبی</option>
            </select>
          </div>
        ` : `<input type="hidden" id="st-type" value="${esc(form.type)}" />`}
        <div class="form-group"><input id="st-phone" class="input" dir="ltr" placeholder="تلفن تماس" value="${esc(form.phone)}" /></div>
        <div class="form-group"><input id="st-addr" class="input" placeholder="آدرس دقیق پاساژ / واحد" value="${esc(form.addr)}" /></div>
        <div class="form-group"><input id="st-hours" class="input" placeholder="ساعات کاری (مثلاً ۱۰ تا ۲۰)" value="${esc(form.hours)}" /></div>
        <div class="row space-between">
          <button class="btn btn-outline" onclick="window.__closeModal()">انصراف</button>
          <button id="st-save" class="btn btn-primary">ثبت فروشگاه</button>
        </div>
      `);
      document.getElementById('st-prov').onchange = (e) => { syncDomToForm(); form.prov = e.target.value; form.city = Object.keys(LOCATIONS[form.prov] || {})[0] || ''; form.reg = 'همه مناطق'; draw(); };
      document.getElementById('st-city').onchange = (e) => { syncDomToForm(); form.city = e.target.value; form.reg = 'همه مناطق'; draw(); };
      document.getElementById('st-save').onclick = async () => {
        const name = document.getElementById('st-name').value.trim();
        if (!name) return toast('نام فروشگاه الزامی است.');
        try {
          const res = await api('/api/stores', {
            method: 'POST',
            body: {
              name,
              type: document.getElementById('st-type').value,
              province: document.getElementById('st-prov').value,
              city: document.getElementById('st-city').value,
              region: document.getElementById('st-reg').value,
              phone: document.getElementById('st-phone').value.trim(),
              address: document.getElementById('st-addr').value.trim(),
              hours: document.getElementById('st-hours').value.trim()
            }
          });
          if (res?.store && state.db) {
            state.db.stores = [res.store, ...(state.db.stores || []).filter((x) => x.id !== res.store.id)];
          }
          closeModal();
          toast(state.session.isAdmin ? 'فروشگاه ثبت و فعال شد.' : 'فروشگاه ثبت شد و جهت تایید به مدیریت ارسال گردید.');
          await loadAppState(true);
        } catch (err) {
          toast(err.message);
        }
      };
    };
    draw();
  }

  function openNewAdModal(section = 'hw') {
    const defaultCat = section === 'mobile' ? 'گوشی موبایل' : section === 'acc' ? 'لوازم جانبی' : 'تاچ و ال‌سی‌دی';
    showModal(`
      <h3 style="margin-top:0;">ثبت آگهی جدید (${section === 'mobile' ? 'بخش موبایل' : section === 'acc' ? 'بخش جانبی' : 'بخش سخت‌افزار'})</h3>
      <div class="form-group"><input id="nad-cat" class="input" placeholder="دسته‌بندی" value="${esc(defaultCat)}" /></div>
      <div class="form-group"><input id="nad-title" class="input" placeholder="عنوان آگهی" /></div>
      <div class="form-group"><input id="nad-brand" class="input" placeholder="برند (مثلاً اپل / سامسونگ)" /></div>
      <div class="form-group"><input id="nad-model" class="input" placeholder="مدل دقیق" /></div>
      <div class="form-group"><input id="nad-series" class="input" placeholder="سری / پارت / وضعیت" /></div>
      <div class="form-group"><input id="nad-price" class="input" type="number" dir="ltr" placeholder="قیمت (تومان)" /></div>
      <div class="form-group"><textarea id="nad-desc" class="textarea" rows="2" placeholder="توضیحات آگهی..."></textarea></div>
      <div class="row space-between">
        <button class="btn btn-outline" onclick="window.__closeModal()">انصراف</button>
        <button id="nad-save" class="btn btn-primary">ثبت آگهی</button>
      </div>
    `);
    document.getElementById('nad-save').onclick = async () => {
      const title = document.getElementById('nad-title').value.trim();
      const price = Number(document.getElementById('nad-price').value || 0);
      if (!title || price <= 0) return toast('عنوان و قیمت معتبر الزامی است.');
      await api('/api/hw-ads', {
        method: 'POST',
        body: {
          section,
          cat: document.getElementById('nad-cat').value.trim() || defaultCat,
          title,
          brand: document.getElementById('nad-brand').value.trim(),
          model: document.getElementById('nad-model').value.trim(),
          series: document.getElementById('nad-series').value.trim(),
          price,
          seller: state.session.user?.fullName || state.session.displayName,
          phone: state.session.user?.phone || '',
          province: state.province,
          city: state.city,
          region: state.region,
          ownerName: state.session.user?.fullName || '',
          desc: document.getElementById('nad-desc').value.trim()
        }
      });
      closeModal();
      toast('آگهی ثبت شد و پس از تایید مدیر نمایش داده می‌شود.');
      await loadAppState(true);
    };
  }

  function openNewTutorialModal() {
    const problems = state.db.swProblems || [];
    showModal(`
      <h3 style="margin-top:0;">ثبت آموزش تخصصی جدید</h3>
      <div class="form-group">
        <select id="tut-prob" class="select">
          ${problems.map((p) => `<option value="${esc(p.id)}" ${p.id === state.swProblemId ? 'selected' : ''}>${esc(p.title)}</option>`).join('')}
        </select>
      </div>
      <div class="form-group"><input id="tut-title" class="input" placeholder="عنوان آموزش" /></div>
      <div class="form-group"><input id="tut-price" class="input" type="number" dir="ltr" placeholder="هزینه دوره (تومان)" /></div>
      <div class="form-group"><input id="tut-terms" class="input" placeholder="توضیحات / شرایط (مثلاً ویدیویی + پشتیبانی)" /></div>
      <div class="row space-between">
        <button class="btn btn-outline" onclick="window.__closeModal()">انصراف</button>
        <button id="tut-save" class="btn btn-primary">ثبت آموزش</button>
      </div>
    `);
    document.getElementById('tut-save').onclick = async () => {
      try {
        await api('/api/tutorials', {
          method: 'POST',
          body: {
            problemId: document.getElementById('tut-prob').value,
            title: document.getElementById('tut-title').value.trim(),
            teacher: state.session.user?.fullName || 'مدرس همکار',
            price: Number(document.getElementById('tut-price').value || 0),
            terms: document.getElementById('tut-terms').value.trim(),
            city: state.city
          }
        });
        closeModal();
        toast('آموزش ثبت شد.');
        await loadAppState(true);
      } catch (err) {
        toast(err.message);
      }
    };
  }

  function openNewBoxModal() {
    showModal(`
      <h3 style="margin-top:0;">ثبت آگهی باکس و دانگل</h3>
      <div class="form-group"><input id="box-name" class="input" placeholder="نام باکس یا دانگل" /></div>
      <div class="form-group"><input id="box-price" class="input" type="number" dir="ltr" placeholder="قیمت (تومان)" /></div>
      <div class="form-group"><input id="box-cond" class="input" placeholder="وضعیت (مثلاً کارکرده با اعتبار فعال)" /></div>
      <div class="row space-between">
        <button class="btn btn-outline" onclick="window.__closeModal()">انصراف</button>
        <button id="box-save" class="btn btn-primary">ثبت باکس</button>
      </div>
    `);
    document.getElementById('box-save').onclick = async () => {
      await api('/api/boxes', {
        method: 'POST',
        body: {
          name: document.getElementById('box-name').value.trim(),
          price: Number(document.getElementById('box-price').value || 0),
          seller: state.session.user?.fullName || 'همکار',
          city: state.city,
          condition: document.getElementById('box-cond').value.trim()
        }
      });
      closeModal();
      toast('آگهی باکس ثبت شد.');
      await loadAppState(true);
    };
  }

  function openNewForumPostModal(board) {
    const flairs = board === 'hardware' ? FORUM_FLAIRS_HW : FORUM_FLAIRS_SW;
    showModal(`
      <h3 style="margin-top:0;">ارسال پست جدید در انجمن ${board === 'hardware' ? 'سخت‌افزار' : 'نرم‌افزار'}</h3>
      <div class="form-group">
        <select id="fp-flair" class="select">
          ${flairs.map((f) => `<option value="${esc(f)}">${esc(f)}</option>`).join('')}
        </select>
      </div>
      <div class="form-group"><input id="fp-title" class="input" placeholder="عنوان دقیق مشکل یا پرسش..." /></div>
      <div class="form-group"><input id="fp-device" class="input" placeholder="مدل دستگاه (مثلاً iPhone 13 Pro)" /></div>
      <div class="form-group"><textarea id="fp-body" class="textarea" rows="4" placeholder="شرح کامل مشکل، مراحل تست‌شده و جریان‌کشی..."></textarea></div>
      <div class="row space-between">
        <button class="btn btn-outline" onclick="window.__closeModal()">انصراف</button>
        <button id="fp-save" class="btn btn-primary">انتشار پست</button>
      </div>
    `);
    document.getElementById('fp-save').onclick = async () => {
      const title = document.getElementById('fp-title').value.trim();
      const body = document.getElementById('fp-body').value.trim();
      if (!title || !body) return toast('عنوان و متن پست الزامی است.');
      await api('/api/forum-posts', {
        method: 'POST',
        body: {
          board,
          flair: document.getElementById('fp-flair').value,
          author: state.session.user?.fullName || state.session.displayName,
          authorBadge: getUserTier(),
          city: state.city,
          deviceTag: document.getElementById('fp-device').value.trim(),
          title,
          body
        }
      });
      closeModal();
      toast('پست در انجمن منتشر شد.');
      await loadAppState(true);
    };
  }

  function openChatModal(adId) {
    const ad = (state.db.hwAds || []).find((a) => a.id === adId);
    if (!ad) return;
    const threadId = `${ad.id}__${state.session.uid}`;
    const drawChat = () => {
      const thread = (state.db.chatThreads || []).find((c) => c.id === threadId || (c.adId === ad.id && (c.participants || []).includes(state.session.uid)));
      const msgs = thread?.messages || [];
      showModal(`
        <h3 style="margin-top:0;">💬 گفتگو درباره: ${esc(ad.title)}</h3>
        <div class="muted" style="margin-bottom:8px;">فروشنده: ${esc(ad.seller)}</div>
        <div class="chat-thread-box">
          ${msgs.map((m) => `
            <div style="margin-bottom:8px;text-align:${m.senderUid === state.session.uid ? 'left' : 'right'};">
              <span class="badge ${m.senderUid === state.session.uid ? 'badge-blue' : 'badge-gold'}">${esc(m.senderName)}</span>
              <div><div class="chat-bubble ${m.senderUid === state.session.uid ? 'mine' : ''}">${esc(m.text)}</div></div>
            </div>
          `).join('') || '<p class="muted">هنوز پیامی ارسال نشده است.</p>'}
        </div>
        <div class="row">
          <input id="chat-msg-in" class="input" style="flex:1;" placeholder="پیام خود را بنویسید..." />
          <button id="chat-msg-send" class="btn btn-primary">ارسال</button>
        </div>
      `);
      document.getElementById('chat-msg-send').onclick = async () => {
        const text = document.getElementById('chat-msg-in').value.trim();
        if (!text) return;
        await api(`/api/chats/${encodeURIComponent(thread?.id || threadId)}/messages`, {
          method: 'POST',
          body: {
            adId: ad.id,
            buyerUid: state.session.uid,
            sellerUid: ad.ownerId || state.session.uid,
            newMessage: { senderName: state.session.user?.fullName || 'همکار', text }
          }
        });
        await loadAppState(true);
        drawChat();
      };
    };
    drawChat();
  }

  function openAdminUserModal() {
    const form = {
      prov: 'تهران',
      city: 'تهران',
      reg: 'همه مناطق',
      email: '',
      pass: '',
      name: '',
      phone: '',
      role: ALL_ROLES[0]
    };
    const syncDomToForm = () => {
      const g = (id) => document.getElementById(id)?.value;
      if (g('au-email') !== undefined) form.email = g('au-email');
      if (g('au-pass') !== undefined) form.pass = g('au-pass');
      if (g('au-name') !== undefined) form.name = g('au-name');
      if (g('au-phone') !== undefined) form.phone = g('au-phone');
      if (g('au-role') !== undefined) form.role = g('au-role');
      if (g('au-m-reg') !== undefined) form.reg = g('au-m-reg');
    };
    const draw = () => {
      const cities = Object.keys(LOCATIONS[form.prov] || {});
      if (!cities.includes(form.city)) form.city = cities[0] || '';
      const regions = LOCATIONS[form.prov]?.[form.city] || ['همه مناطق'];
      if (!regions.includes(form.reg)) form.reg = regions[0] || 'همه مناطق';
      showModal(`
        <h3 style="margin-top:0;">افزودن / ویرایش کاربر</h3>
        <div class="form-label" style="margin-bottom:6px;">📍 ابتدا آدرس و محدوده صنفی کاربر را انتخاب کنید</div>
        <div class="grid" style="grid-template-columns:repeat(3,1fr);gap:8px;margin-bottom:12px;">
          <select id="au-m-prov" class="select">${Object.keys(LOCATIONS).map((p) => `<option value="${esc(p)}" ${p === form.prov ? 'selected' : ''}>${esc(p)}</option>`).join('')}</select>
          <select id="au-m-city" class="select">${cities.map((c) => `<option value="${esc(c)}" ${c === form.city ? 'selected' : ''}>${esc(c)}</option>`).join('')}</select>
          <select id="au-m-reg" class="select">${regions.map((r) => `<option value="${esc(r)}" ${r === form.reg ? 'selected' : ''}>${esc(r)}</option>`).join('')}</select>
        </div>
        <div class="form-group"><input id="au-email" class="input" type="email" dir="ltr" placeholder="ایمیل کاربر (الزامی)" value="${esc(form.email)}" /></div>
        <div class="form-group"><input id="au-pass" class="input" type="password" dir="ltr" placeholder="رمز عبور ورود (اختیاری - حداقل ۶ کاراکتر)" value="${esc(form.pass)}" /></div>
        <div class="form-group"><input id="au-name" class="input" placeholder="نام و نام خانوادگی" value="${esc(form.name)}" /></div>
        <div class="form-group"><input id="au-phone" class="input" dir="ltr" placeholder="شماره تماس" value="${esc(form.phone)}" /></div>
        <div class="form-group">
          <select id="au-role" class="select">${ALL_ROLES.map((r) => `<option value="${esc(r)}" ${r === form.role ? 'selected' : ''}>${esc(r)}</option>`).join('')}</select>
        </div>
        <div class="row space-between">
          <button class="btn btn-outline" onclick="window.__closeModal()">انصراف</button>
          <button id="au-save" class="btn btn-primary">ذخیره کاربر</button>
        </div>
      `);
      document.getElementById('au-m-prov').onchange = (e) => {
        syncDomToForm();
        form.prov = e.target.value;
        form.city = Object.keys(LOCATIONS[form.prov] || {})[0] || '';
        form.reg = 'همه مناطق';
        draw();
      };
      document.getElementById('au-m-city').onchange = (e) => {
        syncDomToForm();
        form.city = e.target.value;
        form.reg = 'همه مناطق';
        draw();
      };
      document.getElementById('au-save').onclick = async () => {
        try {
          await api('/api/admin/users', {
            method: 'POST',
            body: {
              email: document.getElementById('au-email').value.trim(),
              password: document.getElementById('au-pass').value,
              fullName: document.getElementById('au-name').value.trim(),
              phone: document.getElementById('au-phone').value.trim(),
              tier: document.getElementById('au-role').value,
              province: document.getElementById('au-m-prov').value,
              city: document.getElementById('au-m-city').value,
              region: document.getElementById('au-m-reg').value
            }
          });
          closeModal();
          toast('کاربر ذخیره شد.');
          await loadAppState(true);
        } catch (err) {
          toast(err.message);
        }
      };
    };
    draw();
  }

  function openAdminEditLocationModal(u) {
    const form = {
      prov: LOCATIONS[u.province] ? u.province : 'تهران',
      city: u.city || 'تهران',
      reg: u.region || 'همه مناطق',
      fullName: u.fullName || '',
      phone: u.phone || ''
    };
    const syncDomToForm = () => {
      const g = (id) => document.getElementById(id)?.value;
      if (g('aul-name') !== undefined) form.fullName = g('aul-name');
      if (g('aul-phone') !== undefined) form.phone = g('aul-phone');
      if (g('aul-reg') !== undefined) form.reg = g('aul-reg');
    };
    const draw = () => {
      const cities = Object.keys(LOCATIONS[form.prov] || {});
      if (!cities.includes(form.city)) form.city = cities[0] || '';
      const regions = LOCATIONS[form.prov]?.[form.city] || ['همه مناطق'];
      if (!regions.includes(form.reg)) form.reg = regions[0] || 'همه مناطق';
      showModal(`
        <h3 style="margin-top:0;">📍 ویرایش آدرس و مشخصات کاربر</h3>
        <div class="muted" style="margin-bottom:10px;">${esc(u.email)}</div>
        <div class="grid" style="grid-template-columns:repeat(3,1fr);gap:8px;margin-bottom:12px;">
          <select id="aul-prov" class="select">${Object.keys(LOCATIONS).map((p) => `<option value="${esc(p)}" ${p === form.prov ? 'selected' : ''}>${esc(p)}</option>`).join('')}</select>
          <select id="aul-city" class="select">${cities.map((c) => `<option value="${esc(c)}" ${c === form.city ? 'selected' : ''}>${esc(c)}</option>`).join('')}</select>
          <select id="aul-reg" class="select">${regions.map((r) => `<option value="${esc(r)}" ${r === form.reg ? 'selected' : ''}>${esc(r)}</option>`).join('')}</select>
        </div>
        <div class="form-group"><input id="aul-name" class="input" placeholder="نام و نام خانوادگی" value="${esc(form.fullName)}" /></div>
        <div class="form-group"><input id="aul-phone" class="input" dir="ltr" placeholder="شماره تماس" value="${esc(form.phone)}" /></div>
        <div class="row space-between">
          <button class="btn btn-outline" onclick="window.__closeModal()">انصراف</button>
          <button id="aul-save" class="btn btn-primary">ذخیره آدرس</button>
        </div>
      `);
      document.getElementById('aul-prov').onchange = (e) => {
        syncDomToForm();
        form.prov = e.target.value;
        form.city = Object.keys(LOCATIONS[form.prov] || {})[0] || '';
        form.reg = 'همه مناطق';
        draw();
      };
      document.getElementById('aul-city').onchange = (e) => {
        syncDomToForm();
        form.city = e.target.value;
        form.reg = 'همه مناطق';
        draw();
      };
      document.getElementById('aul-save').onclick = async () => {
        try {
          await api(`/api/admin/users/${encodeURIComponent(u.id)}/location`, {
            method: 'PATCH',
            body: {
              email: u.email,
              province: document.getElementById('aul-prov').value,
              city: document.getElementById('aul-city').value,
              region: document.getElementById('aul-reg').value,
              fullName: document.getElementById('aul-name').value.trim(),
              phone: document.getElementById('aul-phone').value.trim()
            }
          });
          closeModal();
          toast('آدرس کاربر به‌روزرسانی شد.');
          await loadAppState(true);
        } catch (err) {
          toast(err.message);
        }
      };
    };
    draw();
  }

  // ==================== FORUM MUTATION HELPER ====================

  async function mutateForumPost(postId, updater) {
    const post = (state.db.forumPosts || []).find((p) => p.id === postId);
    if (!post) return;
    const next = JSON.parse(JSON.stringify(post));
    updater(next);
    await api(`/api/forum-posts/${postId}/interaction`, {
      method: 'PATCH',
      body: {
        votes: next.votes,
        upvotedBy: next.upvotedBy,
        downvotedBy: next.downvotedBy,
        savedBy: next.savedBy,
        comments: next.comments
      }
    });
    await loadAppState(true);
  }

  // ==================== RENDER & EVENT BINDINGS ====================

  let lastScrolledAdminTab = '';
  let lastWheelSwipeAt = 0;

  function stepShellTab(delta) {
    if (!delta) return;
    const isAdminMode = Boolean(state.session?.isAdmin && state.mode === 'admin');
    if (isAdminMode) {
      const idx = ADMIN_TABS.findIndex((t) => t.id === state.adminTab);
      const nextIdx = idx + delta;
      if (nextIdx >= 0 && nextIdx < ADMIN_TABS.length) {
        state.adminTab = ADMIN_TABS[nextIdx].id;
        state._resetScrollNext = true;
        render();
      }
    } else if (!state.showDeskRentals && !state.showMyStore && !state.mobileDetailId && !state.storeDetailId) {
      const nextTab = state.userTab + delta;
      if (nextTab >= 0 && nextTab <= 3) {
        state.userTab = nextTab;
        state.forumThreadId = '';
        state._resetScrollNext = true;
        render();
      }
    }
  }

  let activeHScrollDrag = null;
  window.addEventListener('mousemove', (e) => {
    if (!activeHScrollDrag) return;
    const dx = e.clientX - activeHScrollDrag.startX;
    if (Math.abs(dx) > 5) activeHScrollDrag.moved = true;
    if (activeHScrollDrag.moved) {
      activeHScrollDrag.el.scrollLeft = activeHScrollDrag.startScrollLeft - dx;
    }
  });
  window.addEventListener('mouseup', () => {
    if (!activeHScrollDrag) return;
    const { el, moved } = activeHScrollDrag;
    activeHScrollDrag = null;
    if (moved && el) {
      el.dataset.dragMoved = '1';
      setTimeout(() => { delete el.dataset.dragMoved; }, 80);
    }
  });

  function bindHorizontalScrollers() {
    document.querySelectorAll('.store-hscroll, #admin-tabs-strip').forEach((el) => {
      if (el.dataset.hscrollBound === '1') return;
      el.dataset.hscrollBound = '1';
      el.addEventListener(
        'wheel',
        (e) => {
          if (el.scrollWidth <= el.clientWidth + 2) return;
          if (Math.abs(e.deltaY) > Math.abs(e.deltaX) && Math.abs(e.deltaY) > 0) {
            e.preventDefault();
            el.scrollLeft -= e.deltaY;
          }
        },
        { passive: false }
      );

      el.addEventListener('mousedown', (e) => {
        if (e.button !== 0) return;
        activeHScrollDrag = {
          el,
          moved: false,
          startX: e.clientX,
          startScrollLeft: el.scrollLeft
        };
      });

      el.addEventListener('click', (e) => {
        if (el.dataset.dragMoved === '1') {
          e.preventDefault();
          e.stopPropagation();
        }
      }, true);
    });
  }

  function bindSwipeNavigation() {
    const mainEl = document.getElementById('main-swipe-area');
    if (!mainEl) return;
    let startX = 0;
    let startY = 0;
    let ignoreSwipe = false;

    const shouldIgnoreTarget = (target) =>
      Boolean(
        target &&
        typeof target.closest === 'function' &&
        target.closest('.store-hscroll, .header-segmented.admin-tabs, input, select, textarea, button, a, [data-no-swipe]')
      );

    if (window.PointerEvent) {
      let activePointerId = null;
      mainEl.addEventListener('pointerdown', (e) => {
        if (e.pointerType !== 'touch' && e.pointerType !== 'pen') return;
        ignoreSwipe = shouldIgnoreTarget(e.target);
        activePointerId = e.pointerId;
        startX = e.clientX;
        startY = e.clientY;
      }, { passive: true });
      mainEl.addEventListener('pointerup', (e) => {
        if (ignoreSwipe || e.pointerId !== activePointerId) return;
        activePointerId = null;
        const dx = e.clientX - startX;
        const dy = e.clientY - startY;
        if (Math.abs(dx) < 55 || Math.abs(dx) < Math.abs(dy) * 1.35) return;
        // In RTL HorizontalPager, swiping right (dx > 0) reveals next tab on the left (+1)
        stepShellTab(dx > 0 ? 1 : -1);
      }, { passive: true });
      mainEl.addEventListener('pointercancel', () => {
        activePointerId = null;
      }, { passive: true });
    } else {
      mainEl.addEventListener('touchstart', (e) => {
        if (!e.changedTouches?.length) return;
        ignoreSwipe = shouldIgnoreTarget(e.target);
        startX = e.changedTouches[0].clientX;
        startY = e.changedTouches[0].clientY;
      }, { passive: true });
      mainEl.addEventListener('touchend', (e) => {
        if (ignoreSwipe || !e.changedTouches?.length) return;
        const dx = e.changedTouches[0].clientX - startX;
        const dy = e.changedTouches[0].clientY - startY;
        if (Math.abs(dx) < 55 || Math.abs(dx) < Math.abs(dy) * 1.35) return;
        stepShellTab(dx > 0 ? 1 : -1);
      }, { passive: true });
    }

    // Trackpad 2-finger horizontal swipe support on laptop/desktop
    mainEl.addEventListener('wheel', (e) => {
      if (shouldIgnoreTarget(e.target)) return;
      if (Math.abs(e.deltaX) > 42 && Math.abs(e.deltaX) > Math.abs(e.deltaY) * 1.8) {
        const now = Date.now();
        if (now - lastWheelSwipeAt < 450) return;
        lastWheelSwipeAt = now;
        stepShellTab(e.deltaX < 0 ? 1 : -1);
      }
    }, { passive: true });
  }

  // ==================== DESK RENTAL & COOPERATION VIEW ====================

  function getDeskRentalsFiltered() {
    const all = (state.db.deskRentals || []).filter((d) => d.status === 'approved' || d.ownerId === state.session.uid || state.session.isAdmin);
    const q = state.deskSearch.trim().toLowerCase();
    const catOk = all.filter((d) => {
      const cOk = state.deskCat === 'همه' || d.category === state.deskCat;
      const sOk = !q || d.title.toLowerCase().includes(q) || d.storeName.toLowerCase().includes(q) || d.equipment.toLowerCase().includes(q);
      return cOk && sOk;
    });
    return filterByLocation(catOk, state.province, state.city, state.region, (d) => d);
  }

  function getDeskRequestsFiltered() {
    const all = state.db.deskRequests || [];
    const q = state.deskSearch.trim().toLowerCase();
    const catOk = all.filter((r) => {
      const cOk = state.deskCat === 'همه' || r.category === state.deskCat;
      const sOk = !q || r.title.toLowerCase().includes(q) || r.proposalText.toLowerCase().includes(q) || r.requesterName.toLowerCase().includes(q);
      return cOk && sOk;
    });
    return filterByLocation(catOk, state.province, state.city, state.region, (r) => r);
  }

  function renderDeskRentalView() {
    const deskCats = ['همه', 'تعمیرات سخت‌افزار', 'نرم‌افزار و فلش', 'فروش موبایل', 'لوازم جانبی'];
    const rentalsRes = getDeskRentalsFiltered();
    const requestsRes = getDeskRequestsFiltered();

    return `
      <div class="row space-between" style="margin-bottom:10px;">
        <button id="desk-back-btn" class="btn btn-outline btn-sm">→ بازگشت</button>
        <strong style="font-size:15px;">🪑 اجاره میز کار و همکاری صنفی</strong>
      </div>

      <div class="sub-tabs">
        <button class="sub-tab ${state.deskSubTab === 'rentals' ? 'active' : ''}" data-desksub="rentals">میزهای آماده اجاره (${toPersianNum(rentalsRes.list.length)})</button>
        <button class="sub-tab ${state.deskSubTab === 'requests' ? 'active' : ''}" data-desksub="requests">درخواست و پیشنهاد همکاری (${toPersianNum(requestsRes.list.length)})</button>
      </div>

      <div class="row space-between" style="margin-bottom:10px;">
        <input id="desk-search" class="input toolbar-search" placeholder="جستجو در میزها، تجهیزات یا پیشنهادها..." value="${esc(state.deskSearch)}" />
        <button id="desk-add-btn" class="btn btn-primary btn-sm">+ ثبت میز کار یا درخواست همکاری</button>
      </div>

      <div class="chip-row">
        ${deskCats.map((c) => `<button class="chip ${state.deskCat === c ? 'active' : ''}" data-deskcat="${esc(c)}">${esc(c)}</button>`).join('')}
      </div>

      ${state.deskSubTab === 'rentals' ? `
        <div class="section-header-title">
          <span>میزهای کار موجود (${esc(rentalsRes.scope)})</span>
          <span class="badge badge-blue">${toPersianNum(rentalsRes.list.length)} مورد</span>
        </div>
        <div class="grid">
          ${rentalsRes.list.map((d) => `
            <div class="card">
              <div class="row space-between">
                <span class="badge badge-blue">${esc(d.category)}</span>
                <span class="badge ${d.status === 'approved' ? 'badge-green' : 'badge-gold'}">${d.status === 'approved' ? 'تاییدشده' : 'در انتظار تایید'}</span>
              </div>
              <h3 style="margin:8px 0 4px;font-size:15px;">${esc(d.title)}</h3>
              <div class="muted">🏪 ${esc(d.storeName || 'فروشگاه همکار')} · 📍 ${esc(d.city)} (${esc(d.region)})</div>
              ${d.cooperationModel ? `<div style="margin-top:6px;font-size:12.5px;font-weight:700;color:var(--navy);">🤝 ${esc(d.cooperationModel)}</div>` : ''}
              ${d.equipment ? `<div class="muted" style="margin-top:4px;">🔧 تجهیزات: ${esc(d.equipment)}</div>` : ''}
              ${d.desc ? `<p class="muted" style="margin:6px 0 0;">${esc(d.desc)}</p>` : ''}
              <div class="row space-between" style="margin-top:10px;padding-top:8px;border-top:1px solid var(--border);">
                <div>
                  ${Number(d.rentMonthly) > 0 ? `<div class="price-tag">${formatToman(d.rentMonthly)} / ماه</div>` : '<div class="price-tag">توافقی / درصدی</div>'}
                  ${Number(d.rentDaily) > 0 ? `<div class="muted">روزانه: ${formatToman(d.rentDaily)}</div>` : ''}
                </div>
                <div class="row">
                  ${d.phone ? `<a class="btn btn-outline btn-sm" href="tel:${esc(d.phone)}">📞 تماس</a>` : ''}
                  <button class="btn btn-navy btn-sm" data-chat-generic="${esc(d.id)}|${esc(d.title)}|${esc(d.storeName || d.ownerName)}|${esc(d.ownerId)}">💬 چت</button>
                  ${(d.ownerId === state.session.uid || state.session.isAdmin) ? `<button class="btn btn-danger btn-sm" data-del-desk-rental="${esc(d.id)}">حذف</button>` : ''}
                </div>
              </div>
            </div>
          `).join('') || '<div class="card muted">در این دسته‌بندی میزی ثبت نشده است.</div>'}
        </div>
      ` : `
        <div class="section-header-title">
          <span>پیشنهادها و درخواست‌های همکاری (${esc(requestsRes.scope)})</span>
          <span class="badge badge-gold">${toPersianNum(requestsRes.list.length)} پیشنهاد</span>
        </div>
        <div class="grid">
          ${requestsRes.list.map((r) => `
            <div class="card">
              <div class="row space-between">
                <span class="badge badge-blue">${esc(r.category)}</span>
                <span class="badge ${r.status === 'matched' ? 'badge-green' : r.status === 'closed' ? 'badge-red' : 'badge-gold'}">${r.status === 'matched' ? 'توافق شده' : r.status === 'closed' ? 'بسته شده' : 'در جستجوی میز'}</span>
              </div>
              <h3 style="margin:8px 0 4px;font-size:15px;">${esc(r.title)}</h3>
              <div class="muted">👤 ${esc(r.requesterName)} · 📍 ${esc(r.city)} (${esc(r.region)})</div>
              <p style="margin:8px 0;font-size:13px;">${esc(r.proposalText)}</p>
              <div class="row space-between" style="padding-top:8px;border-top:1px solid var(--border);">
                <span class="price-tag">${Number(r.proposedBudget) > 0 ? 'سقف بودجه: ' + formatToman(r.proposedBudget) : 'پیشنهاد درصدی / توافقی'}</span>
                <div class="row">
                  ${r.requesterPhone ? `<a class="btn btn-outline btn-sm" href="tel:${esc(r.requesterPhone)}">📞 تماس</a>` : ''}
                  <button class="btn btn-navy btn-sm" data-chat-generic="${esc(r.id)}|${esc(r.title)}|${esc(r.requesterName)}|${esc(r.ownerId)}">💬 چت</button>
                  ${(r.ownerId === state.session.uid || state.session.isAdmin) ? `<button class="btn btn-danger btn-sm" data-del-desk-req="${esc(r.id)}">حذف</button>` : ''}
                </div>
              </div>
            </div>
          `).join('') || '<div class="card muted">درخواستی ثبت نشده است.</div>'}
        </div>
      `}
    `;
  }

  function openNewDeskModal(initialKind) {
    const form = {
      kind: initialKind || (state.deskSubTab === 'requests' ? 'request' : 'rental'),
      prov: isAllScope(state.province) ? 'تهران' : state.province,
      city: isAllScope(state.city) ? 'تهران' : state.city,
      reg: isAllScope(state.region) ? 'همه مناطق' : state.region,
      title: '',
      cat: 'تعمیرات سخت‌افزار',
      store: '',
      daily: '',
      monthly: '',
      coop: '',
      equip: '',
      phone: state.session.user?.phone || '',
      desc: '',
      budget: '',
      prop: ''
    };
    const syncDomToForm = () => {
      const g = (id) => document.getElementById(id)?.value;
      if (g('dk-title') !== undefined) form.title = g('dk-title');
      if (g('dk-cat') !== undefined) form.cat = g('dk-cat');
      if (g('dk-store') !== undefined) form.store = g('dk-store');
      if (g('dk-reg') !== undefined) form.reg = g('dk-reg');
      if (g('dk-daily') !== undefined) form.daily = g('dk-daily');
      if (g('dk-monthly') !== undefined) form.monthly = g('dk-monthly');
      if (g('dk-coop') !== undefined) form.coop = g('dk-coop');
      if (g('dk-equip') !== undefined) form.equip = g('dk-equip');
      if (g('dk-phone') !== undefined) form.phone = g('dk-phone');
      if (g('dk-desc') !== undefined) form.desc = g('dk-desc');
      if (g('dk-budget') !== undefined) form.budget = g('dk-budget');
      if (g('dk-prop') !== undefined) form.prop = g('dk-prop');
    };
    const draw = () => {
      const cities = Object.keys(LOCATIONS[form.prov] || {});
      if (!cities.includes(form.city)) form.city = cities[0] || '';
      const regions = LOCATIONS[form.prov]?.[form.city] || ['همه مناطق'];
      if (!regions.includes(form.reg)) form.reg = regions[0] || 'همه مناطق';
      const isRental = form.kind === 'rental';
      const cats = ['تعمیرات سخت‌افزار', 'نرم‌افزار و فلش', 'فروش موبایل', 'لوازم جانبی'];
      showModal(`
        <h3 style="margin-top:0;">${isRental ? '🪑 ثبت میز کار برای اجاره' : '🤝 ثبت درخواست یا پیشنهاد میز'}</h3>
        <div class="sub-tabs" style="margin-bottom:10px;">
          <button type="button" class="sub-tab ${isRental ? 'active' : ''}" id="desk-kind-rental">🪑 میز برای اجاره دارم</button>
          <button type="button" class="sub-tab ${!isRental ? 'active' : ''}" id="desk-kind-request">🤝 متقاضی میز / پیشنهاد همکاری</button>
        </div>
        <div class="grid" style="grid-template-columns:repeat(3,1fr);gap:8px;margin-bottom:10px;">
          <select id="dk-prov" class="select">${Object.keys(LOCATIONS).map((p) => `<option value="${esc(p)}" ${p === form.prov ? 'selected' : ''}>${esc(p)}</option>`).join('')}</select>
          <select id="dk-city" class="select">${cities.map((c) => `<option value="${esc(c)}" ${c === form.city ? 'selected' : ''}>${esc(c)}</option>`).join('')}</select>
          <select id="dk-reg" class="select">${regions.map((r) => `<option value="${esc(r)}" ${r === form.reg ? 'selected' : ''}>${esc(r)}</option>`).join('')}</select>
        </div>
        <div class="form-group"><input id="dk-title" class="input" placeholder="${isRental ? 'عنوان (مثلاً اجاره میز تعمیرات برد در علاءالدین)' : 'عنوان (مثلاً تعمیرکار آیفون هستم - دنبال میز درصدی)'}" value="${esc(form.title)}" /></div>
        <div class="form-group">
          <select id="dk-cat" class="select">
            ${cats.map((c) => `<option value="${esc(c)}" ${c === form.cat ? 'selected' : ''}>${esc(c)}</option>`).join('')}
          </select>
        </div>
        ${isRental ? `<div class="form-group"><input id="dk-store" class="input" placeholder="نام مغازه / پاساژ" value="${esc(form.store)}" /></div>` : ''}
        ${isRental ? `
          <div class="grid" style="grid-template-columns:1fr 1fr;gap:8px;margin-bottom:10px;">
            <input id="dk-daily" class="input" type="number" dir="ltr" placeholder="اجاره روزانه (تومان)" value="${esc(form.daily)}" />
            <input id="dk-monthly" class="input" type="number" dir="ltr" placeholder="اجاره ماهانه (تومان)" value="${esc(form.monthly)}" />
          </div>
          <div class="form-group"><input id="dk-coop" class="input" placeholder="مدل همکاری (مثلاً ماهانه ثابت یا درصدی ۶۰/۴۰)" value="${esc(form.coop)}" /></div>
          <div class="form-group"><input id="dk-equip" class="input" placeholder="تجهیزات موجود (لوپ، هیتر، سیستم، ویترین...)" value="${esc(form.equip)}" /></div>
          <div class="form-group"><input id="dk-phone" class="input" dir="ltr" placeholder="تلفن تماس" value="${esc(form.phone)}" /></div>
          <div class="form-group"><textarea id="dk-desc" class="textarea" rows="2" placeholder="شرایط و توضیحات تکمیلی...">${esc(form.desc)}</textarea></div>
        ` : `
          <div class="form-group"><input id="dk-budget" class="input" type="number" dir="ltr" placeholder="سقف بودجه ماهانه پیشنهادی (تومان - اختیاری)" value="${esc(form.budget)}" /></div>
          <div class="form-group"><input id="dk-phone" class="input" dir="ltr" placeholder="شماره تماس" value="${esc(form.phone)}" /></div>
          <div class="form-group"><textarea id="dk-prop" class="textarea" rows="3" placeholder="تخصص، تجهیزات همراه و پیشنهاد همکاری برای مغازه‌داران...">${esc(form.prop)}</textarea></div>
        `}
        <div class="row space-between">
          <button type="button" class="btn btn-outline" onclick="window.__closeModal()">انصراف</button>
          <button type="button" id="dk-save" class="btn btn-primary">${isRental ? '✓ ثبت میز کار' : '✓ ثبت درخواست همکاری'}</button>
        </div>
      `);
      document.getElementById('desk-kind-rental').onclick = () => { syncDomToForm(); form.kind = 'rental'; draw(); };
      document.getElementById('desk-kind-request').onclick = () => { syncDomToForm(); form.kind = 'request'; draw(); };
      document.getElementById('dk-prov').onchange = (e) => { syncDomToForm(); form.prov = e.target.value; form.city = Object.keys(LOCATIONS[form.prov] || {})[0] || ''; form.reg = 'همه مناطق'; draw(); };
      document.getElementById('dk-city').onchange = (e) => { syncDomToForm(); form.city = e.target.value; form.reg = 'همه مناطق'; draw(); };
      document.getElementById('dk-save').onclick = async () => {
        const title = document.getElementById('dk-title').value.trim();
        if (!title) return toast('عنوان آگهی الزامی است.');
        try {
          if (isRental) {
            await api('/api/desk-rentals', {
              method: 'POST',
              body: {
                title,
                category: document.getElementById('dk-cat').value,
                storeName: document.getElementById('dk-store').value.trim(),
                province: document.getElementById('dk-prov').value,
                city: document.getElementById('dk-city').value,
                region: document.getElementById('dk-reg').value,
                rentDaily: Number(document.getElementById('dk-daily').value || 0),
                rentMonthly: Number(document.getElementById('dk-monthly').value || 0),
                cooperationModel: document.getElementById('dk-coop').value.trim(),
                equipment: document.getElementById('dk-equip').value.trim(),
                phone: document.getElementById('dk-phone').value.trim(),
                desc: document.getElementById('dk-desc').value.trim()
              }
            });
            closeModal();
            toast('آگهی میز کار ثبت شد و پس از تایید مدیریت نمایش داده می‌شود.');
          } else {
            const proposalText = document.getElementById('dk-prop').value.trim();
            if (!proposalText) return toast('شرح پیشنهاد همکاری الزامی است.');
            await api('/api/desk-requests', {
              method: 'POST',
              body: {
                title,
                category: document.getElementById('dk-cat').value,
                province: document.getElementById('dk-prov').value,
                city: document.getElementById('dk-city').value,
                region: document.getElementById('dk-reg').value,
                proposedBudget: Number(document.getElementById('dk-budget').value || 0),
                requesterPhone: document.getElementById('dk-phone').value.trim(),
                proposalText
              }
            });
            closeModal();
            toast('درخواست و پیشنهاد همکاری شما ثبت شد.');
          }
          await loadAppState(true);
        } catch (err) {
          toast(err.message || 'خطا در ثبت اطلاعات روی سرور.');
        }
      };
    };
    draw();
  }

  function openGenericChatModal(adId, title, partnerName, sellerUid) {
    const threadId = `${adId}__${state.session.uid}`;
    const drawChat = () => {
      const thread = (state.db.chatThreads || []).find((c) => c.id === threadId || (c.adId === adId && (c.participants || []).includes(state.session.uid)));
      const msgs = thread?.messages || [];
      showModal(`
        <h3 style="margin-top:0;">💬 ${esc(title)}</h3>
        <div class="muted" style="margin-bottom:8px;">مخاطب: ${esc(partnerName || 'همکار')}</div>
        <div class="chat-thread-box">
          ${msgs.map((m) => `
            <div style="margin-bottom:8px;text-align:${m.senderUid === state.session.uid ? 'left' : 'right'};">
              <span class="badge ${m.senderUid === state.session.uid ? 'badge-blue' : 'badge-gold'}">${esc(m.senderName)}</span>
              <div><div class="chat-bubble ${m.senderUid === state.session.uid ? 'mine' : ''}">${esc(m.text)}</div></div>
            </div>
          `).join('') || '<p class="muted">هنوز پیامی ارسال نشده است.</p>'}
        </div>
        <div class="row">
          <input id="gchat-msg-in" class="input" style="flex:1;" placeholder="پیام خود را بنویسید..." />
          <button id="gchat-msg-send" class="btn btn-primary">ارسال</button>
        </div>
      `);
      document.getElementById('gchat-msg-send').onclick = async () => {
        const text = document.getElementById('gchat-msg-in').value.trim();
        if (!text) return;
        await api(`/api/chats/${encodeURIComponent(thread?.id || threadId)}/messages`, {
          method: 'POST',
          body: {
            adId,
            buyerUid: state.session.uid,
            sellerUid: sellerUid || state.session.uid,
            newMessage: { senderName: state.session.user?.fullName || 'همکار', text }
          }
        });
        await loadAppState(true);
        drawChat();
      };
    };
    drawChat();
  }

  // ==================== MY STORE MANAGEMENT VIEW ====================

  function getMyStores() {
    const uid = state.session?.uid;
    return (state.db?.stores || []).filter((s) => s.ownerId === uid || (state.session?.isAdmin && !s.isDemo));
  }

  function renderMyStoreView() {
    const myStores = getMyStores();
    if (!myStores.some((s) => s.id === state.myStoreId)) {
      state.myStoreId = myStores[0]?.id || '';
    }
    const store = myStores.find((s) => s.id === state.myStoreId) || null;
    const isApproved = store && store.status === 'approved';
    const storeOffers = store ? (state.db.storeMobiles || []).filter((sm) => sm.storeId === store.id) : [];
    const storeAccs = store ? (state.db.accessories || []).filter((a) => a.storeId === store.id) : [];
    const storeInqs = store ? (state.db.inquiries || []).filter((q) => q.storeName === store.name) : [];
    const storeOrders = store
      ? (state.db.orders || []).filter((o) => Array.isArray(o.items) && o.items.some((i) => i.storeName === store.name))
      : [];

    return `
      <div class="row space-between" style="margin-bottom:10px;">
        <button id="mystore-back-btn" class="btn btn-outline btn-sm">→ بازگشت</button>
        <strong style="font-size:15px;">🏪 مدیریت فروشگاه من</strong>
        <button id="mystore-create-btn" class="btn btn-gold btn-sm">+ ثبت فروشگاه جدید</button>
      </div>

      ${!store ? `
        <div class="card" style="text-align:center;">
          <h3>شما هنوز فروشگاهی ثبت نکرده‌اید</h3>
          <p class="muted">با ثبت فروشگاه، پس از تایید مدیریت می‌توانید موجودی گوشی و لوازم جانبی، قیمت همکار، استعلام‌ها و سفارش‌های مشتریان را مدیریت کنید.</p>
          <button id="mystore-empty-create" class="btn btn-primary">+ ثبت اولین فروشگاه</button>
        </div>
      ` : `
        ${myStores.length > 1 ? `
          <div class="chip-row">
            ${myStores.map((s) => `<button class="chip ${s.id === store.id ? 'active' : ''}" data-sel-mystore="${esc(s.id)}">${esc(s.name)}</button>`).join('')}
          </div>
        ` : ''}

        <div class="card" style="margin-bottom:12px;">
          <div class="row space-between">
            <div>
              <h3 style="margin:0 0 4px;">${esc(store.name)}</h3>
              <div class="muted">${esc(store.province)} · ${esc(store.city)} (${esc(store.region)}) · ${esc(storeTypeLabel(store.type))}</div>
            </div>
            <span class="badge ${store.status === 'approved' ? 'badge-green' : store.status === 'rejected' ? 'badge-red' : 'badge-gold'}">
              ${store.status === 'approved' ? '✓ تاییدشده و فعال' : store.status === 'rejected' ? '✕ ردشده' : '⏳ در انتظار تایید مدیریت'}
            </span>
          </div>
          ${!isApproved ? `<div class="warn-box" style="margin-top:10px;margin-bottom:0;">فروشگاه شما پس از تایید توسط ادمین در بازار نمایش داده می‌شود و امکان افزودن محصول فعال خواهد شد.</div>` : ''}
        </div>

        <div class="sub-tabs">
          <button class="sub-tab ${state.myStoreSubTab === 'products' ? 'active' : ''}" data-mssub="products">📦 محصولات (${toPersianNum(storeOffers.length + storeAccs.length)})</button>
          <button class="sub-tab ${state.myStoreSubTab === 'inquiries' ? 'active' : ''}" data-mssub="inquiries">📋 استعلام‌ها (${toPersianNum(storeInqs.length)})</button>
          <button class="sub-tab ${state.myStoreSubTab === 'orders' ? 'active' : ''}" data-mssub="orders">🧾 سفارش‌ها (${toPersianNum(storeOrders.length)})</button>
          <button class="sub-tab ${state.myStoreSubTab === 'settings' ? 'active' : ''}" data-mssub="settings">⚙️ مشخصات</button>
        </div>

        ${state.myStoreSubTab === 'products' ? `
          ${isApproved ? `
            <div class="row" style="margin-bottom:12px;">
              <button id="ms-add-mob-btn" class="btn btn-primary btn-sm">+ افزودن گوشی / قیمت</button>
              <button id="ms-add-acc-btn" class="btn btn-navy btn-sm">+ افزودن کالای جانبی</button>
              <button id="ms-add-hw-btn" class="btn btn-gold btn-sm">+ ثبت آگهی قطعه</button>
            </div>
          ` : ''}
          <div class="section-header-title"><span>📱 لیست گوشی‌های این فروشگاه (${toPersianNum(storeOffers.length)})</span></div>
          <div class="grid" style="margin-bottom:12px;">
            ${storeOffers.map((o) => {
              const m = (state.db.mobiles || []).find((x) => x.id === o.mobileId);
              return `
                <div class="card">
                  <strong>${esc(m?.brand || '')} ${esc(m?.model || 'گوشی')}</strong>
                  <div class="muted">${esc(o.info)}</div>
                  <div class="row space-between" style="margin-top:8px;">
                    <span class="price-tag">${formatToman(o.price)}</span>
                    <div class="row">
                      <button class="btn btn-outline btn-sm" data-ms-edit-sm="${esc(o.id)}">✏️ قیمت</button>
                      <button class="btn btn-danger btn-sm" data-ms-del-sm="${esc(o.id)}">حذف</button>
                    </div>
                  </div>
                </div>
              `;
            }).join('') || '<div class="card muted">گوشی‌ای برای این فروشگاه ثبت نشده است.</div>'}
          </div>
          <div class="section-header-title"><span>🎧 لوازم جانبی این فروشگاه (${toPersianNum(storeAccs.length)})</span></div>
          <div class="grid">
            ${storeAccs.map((a) => `
              <div class="card">
                <span class="badge badge-blue">${esc(a.cat)}</span>
                <h4 style="margin:6px 0 4px;">${esc(a.name)}</h4>
                <div class="muted">${esc(a.desc)}</div>
                <div class="row space-between" style="margin-top:8px;">
                  <span class="price-tag">${formatToman(a.price)}</span>
                  <div class="row">
                    <button class="btn btn-outline btn-sm" data-ms-edit-acc="${esc(a.id)}">✏️ قیمت</button>
                    <button class="btn btn-danger btn-sm" data-ms-del-acc="${esc(a.id)}">حذف</button>
                  </div>
                </div>
              </div>
            `).join('') || '<div class="card muted">کالای جانبی ثبت نشده است.</div>'}
          </div>
        ` : ''}

        ${state.myStoreSubTab === 'inquiries' ? `
          <div class="grid">
            ${storeInqs.map((q) => `
              <div class="card">
                <div class="row space-between">
                  <strong>${esc(q.accName)}</strong>
                  <span class="badge ${q.status.includes('ناموجود') ? 'badge-red' : q.status.includes('موجود') ? 'badge-green' : 'badge-gold'}">${esc(q.status)}</span>
                </div>
                <div class="muted">خریدار: ${esc(q.userName)} · ${esc(q.date)}</div>
                <div class="row" style="margin-top:8px;">
                  <button class="btn btn-success btn-sm" data-inq-status="${esc(q.id)}:موجود شد - آماده ثبت سفارش">✓ موجود شد</button>
                  <button class="btn btn-danger btn-sm" data-inq-status="${esc(q.id)}:فعلاً ناموجود">✕ ناموجود</button>
                </div>
              </div>
            `).join('') || '<div class="card muted">هنوز استعلامی برای فروشگاه شما ارسال نشده است.</div>'}
          </div>
        ` : ''}

        ${state.myStoreSubTab === 'orders' ? `
          <div class="grid">
            ${storeOrders.map((o) => `
              <div class="card">
                <div class="row space-between">
                  <strong>${esc(o.id)} · ${esc(o.userName)}</strong>
                  <span class="badge badge-blue">${esc(o.status)}</span>
                </div>
                <div class="muted">${formatToman(o.total)} · ${esc(o.deliveryType)} · ${esc(o.city)}</div>
              </div>
            `).join('') || '<div class="card muted">سفارشی برای این فروشگاه ثبت نشده است.</div>'}
          </div>
        ` : ''}

        ${state.myStoreSubTab === 'settings' ? `
          <div class="card">
            <h4 style="margin-top:0;">ویرایش مشخصات فروشگاه</h4>
            <div class="form-group"><input id="ms-set-name" class="input" value="${esc(store.name)}" placeholder="نام فروشگاه" /></div>
            <div class="form-group"><input id="ms-set-phone" class="input" dir="ltr" value="${esc(store.phone)}" placeholder="تلفن تماس" /></div>
            <div class="form-group"><input id="ms-set-addr" class="input" value="${esc(store.address)}" placeholder="آدرس دقیق" /></div>
            <div class="form-group"><input id="ms-set-hours" class="input" value="${esc(store.hours)}" placeholder="ساعات کاری" /></div>
            <button id="ms-save-settings" class="btn btn-primary">ذخیره تغییرات</button>
          </div>
        ` : ''}
      `}
    `;
  }

  function openMyStoreAddMobileModal(storeId) {
    const mobiles = state.db.mobiles || [];
    showModal(`
      <h3 style="margin-top:0;">📱 افزودن گوشی به موجودی فروشگاه</h3>
      <div class="form-group">
        <label class="form-label">انتخاب از کاتالوگ (یا مدل جدید بنویسید)</label>
        <select id="msm-pick" class="select">
          <option value="__new__">+ ثبت مدل گوشی جدید</option>
          ${mobiles.map((m) => `<option value="${esc(m.id)}">${esc(m.brand)} ${esc(m.model)}</option>`).join('')}
        </select>
      </div>
      <div id="msm-new-fields">
        <div class="form-group"><input id="msm-brand" class="input" placeholder="برند (مثلاً Apple / Samsung)" /></div>
        <div class="form-group"><input id="msm-model" class="input" placeholder="نام مدل (مثلاً iPhone 15 Pro 256)" /></div>
        <div class="form-group"><input id="msm-specs" class="input" placeholder="مشخصات (پارت، گارانتی، حافظه)" /></div>
      </div>
      <div class="form-group"><input id="msm-price" class="input" type="number" dir="ltr" placeholder="قیمت فروش شما (تومان)" /></div>
      <div class="form-group"><input id="msm-info" class="input" placeholder="توضیح (مثلاً رجیستر رسمی + ۱۸ ماه گارانتی)" /></div>
      <div class="row space-between">
        <button class="btn btn-outline" onclick="window.__closeModal()">انصراف</button>
        <button id="msm-save" class="btn btn-primary">افزودن به فروشگاه</button>
      </div>
    `);
    const sel = document.getElementById('msm-pick');
    const nf = document.getElementById('msm-new-fields');
    sel.onchange = () => { nf.style.display = sel.value === '__new__' ? 'block' : 'none'; };
    document.getElementById('msm-save').onclick = async () => {
      const price = Number(document.getElementById('msm-price').value || 0);
      if (price <= 0) return toast('قیمت معتبر وارد کنید.');
      const isNew = sel.value === '__new__';
      const body = {
        storeId,
        mobileId: isNew ? '' : sel.value,
        price,
        info: document.getElementById('msm-info').value.trim(),
        newMobile: isNew
          ? {
              brand: document.getElementById('msm-brand').value.trim() || 'سایر',
              model: document.getElementById('msm-model').value.trim(),
              specs: document.getElementById('msm-specs').value.trim()
            }
          : null
      };
      if (isNew && !body.newMobile.model) return toast('نام مدل گوشی الزامی است.');
      try {
        await api('/api/store-mobiles', { method: 'POST', body });
        closeModal();
        toast('محصول به فروشگاه اضافه شد.');
        await loadAppState(true);
      } catch (err) {
        toast(err.message);
      }
    };
  }

  function openMyStoreAddAccModal(storeId) {
    showModal(`
      <h3 style="margin-top:0;">🎧 افزودن کالای جانبی به فروشگاه</h3>
      <div class="form-group">
        <select id="msa-cat" class="select">
          <option value="کابل و آداپتور">کابل و آداپتور</option>
          <option value="قاب و گارد">قاب و گارد</option>
          <option value="گلس و محافظ">گلس و محافظ</option>
          <option value="هندزفری و ساعت">هندزفری و ساعت</option>
          <option value="پاوربانک">پاوربانک</option>
        </select>
      </div>
      <div class="form-group"><input id="msa-name" class="input" placeholder="نام کالا" /></div>
      <div class="form-group"><input id="msa-price" class="input" type="number" dir="ltr" placeholder="قیمت عمده (تومان)" /></div>
      <div class="form-group"><input id="msa-desc" class="input" placeholder="توضیحات کالا" /></div>
      <div class="row space-between">
        <button class="btn btn-outline" onclick="window.__closeModal()">انصراف</button>
        <button id="msa-save" class="btn btn-primary">ثبت کالا</button>
      </div>
    `);
    document.getElementById('msa-save').onclick = async () => {
      const name = document.getElementById('msa-name').value.trim();
      const price = Number(document.getElementById('msa-price').value || 0);
      if (!name || price <= 0) return toast('نام و قیمت کالا الزامی است.');
      try {
        await api('/api/accessories', {
          method: 'POST',
          body: {
            storeId,
            cat: document.getElementById('msa-cat').value,
            name,
            price,
            desc: document.getElementById('msa-desc').value.trim()
          }
        });
        closeModal();
        toast('کالای جانبی ثبت شد.');
        await loadAppState(true);
      } catch (err) {
        toast(err.message);
      }
    };
  }

  function render() {
    const app = document.getElementById('app');
    if (!app) return;

    const prevScrollY = window.scrollY || 0;
    const resetScroll = Boolean(state._resetScrollNext);
    state._resetScrollNext = false;

    const activeEl = document.activeElement;
    const preserveFocusIds = new Set(['mob-search', 'sw-search', 'acc-search', 'hw-search', 'forum-search', 'au-search', 'admin-ads-search', 'desk-search', 'login-email', 'login-password']);
    const activeId = activeEl && preserveFocusIds.has(activeEl.id) ? activeEl.id : '';
    const selStart = activeId && typeof activeEl.selectionStart === 'number' ? activeEl.selectionStart : null;
    const selEnd = activeId && typeof activeEl.selectionEnd === 'number' ? activeEl.selectionEnd : null;

    if (state.loading) {
      app.innerHTML = `<div class="loading-screen"><div class="spinner"></div><p>در حال بارگذاری اطلاعات بازمو...</p></div>`;
      return;
    }

    if (!state.token || !state.session) {
      app.innerHTML = renderLoginScreen();
      bindLoginEvents();
      if (activeId) {
        const nextEl = document.getElementById(activeId);
        if (nextEl && !nextEl.disabled) {
          nextEl.focus({ preventScroll: true });
          if (selStart !== null && nextEl.setSelectionRange) {
            try { nextEl.setSelectionRange(selStart, selEnd ?? selStart); } catch {}
          }
        }
      }
      return;
    }

    if (state.session.status !== 'ready') {
      app.innerHTML = renderAccessGateScreen();
      const gateTheme = document.getElementById('gate-theme-btn');
      if (gateTheme) gateTheme.onclick = toggleTheme;
      const gateReg = document.getElementById('gate-register-btn');
      if (gateReg) {
        gateReg.onclick = () =>
          openRegistrationModal({
            email: state.session.email || '',
            fullName: state.session.displayName || ''
          });
      }
      document.getElementById('gate-refresh').onclick = () => loadAppState(false);
      document.getElementById('gate-logout').onclick = () => { logoutLocal(); render(); };
      return;
    }

    if (!state.db) {
      app.innerHTML = `<div class="loading-screen"><div class="spinner"></div><p>در حال همگام‌سازی پایگاه داده...</p></div>`;
      return;
    }

    const isAdminMode = Boolean(state.session.isAdmin && state.mode === 'admin');
    const myStoresCount = getMyStores().length;
    const isAllCountry = isAllScope(state.province);
    let contentHtml = '';
    if (isAdminMode) {
      contentHtml = renderAdminShell();
    } else if (state.showMyStore) {
      contentHtml = renderMyStoreView();
    } else if (state.showDeskRentals) {
      contentHtml = renderDeskRentalView();
    } else if (state.userTab === 0) {
      contentHtml = renderMobileTab();
    } else if (state.userTab === 1) {
      contentHtml = renderSoftwareTab();
    } else if (state.userTab === 2) {
      contentHtml = renderAccessoriesTab();
    } else {
      contentHtml = renderHardwareTab();
    }

    const quickBarHtml = !isAdminMode ? `
      <div class="quick-action-bar">
        <button id="qa-all-country-btn" class="quick-pill-btn ${isAllCountry ? 'active' : ''}">
          ${isAllCountry ? '✓ 🌍 همه کشور (فعال)' : '🌍 همه کشور'}
        </button>
        <button id="qa-desk-btn" class="quick-pill-btn ${state.showDeskRentals ? 'active' : ''}">🪑 اجاره میز کار و همکاری (${toPersianNum((state.db.deskRentals || []).length + (state.db.deskRequests || []).length)})</button>
        ${canCreateStore() ? `<button id="qa-mystore-btn" class="quick-pill-btn gold ${state.showMyStore ? 'active' : ''}">🏪 مدیریت فروشگاه من${myStoresCount ? ` (${toPersianNum(myStoresCount)})` : ''}</button>` : ''}
      </div>
    ` : '';

    app.innerHTML = `
      <div class="app-shell">
        ${renderHeader()}
        <main class="main-content tab-animate" id="main-swipe-area">
          ${renderNotifPermissionBanner()}
          ${quickBarHtml}
          ${contentHtml}
        </main>
      </div>
    `;

    bindHeaderEvents();
    bindContentEvents();
    bindSwipeNavigation();
    bindHorizontalScrollers();

    if (activeId) {
      const nextEl = document.getElementById(activeId);
      if (nextEl && !nextEl.disabled) {
        nextEl.focus({ preventScroll: true });
        if (selStart !== null && nextEl.setSelectionRange) {
          try { nextEl.setSelectionRange(selStart, selEnd ?? selStart); } catch {}
        }
      }
    }

    requestAnimationFrame(() => {
      if (resetScroll) window.scrollTo(0, 0);
      else if (prevScrollY > 0) window.scrollTo(0, prevScrollY);
    });
  }

  function bindHeaderEvents() {
    const logoutBtn = document.getElementById('hdr-logout-btn');
    if (logoutBtn) logoutBtn.onclick = () => { logoutLocal(); render(); };

    const themeBtn = document.getElementById('hdr-theme-btn');
    if (themeBtn) themeBtn.onclick = toggleTheme;

    const modeBtn = document.getElementById('hdr-mode-btn');
    if (modeBtn) modeBtn.onclick = () => { state.mode = state.mode === 'admin' ? 'user' : 'admin'; state._resetScrollNext = true; render(); };

    const notifBtn = document.getElementById('hdr-notif-btn');
    if (notifBtn) notifBtn.onclick = openNotificationsModal;

    const cartBtn = document.getElementById('hdr-cart-btn');
    if (cartBtn) cartBtn.onclick = openCartModal;

    const ordersBtn = document.getElementById('hdr-orders-btn');
    if (ordersBtn) ordersBtn.onclick = openOrdersModal;

    const profileBtn = document.getElementById('hdr-profile-btn');
    if (profileBtn) profileBtn.onclick = openProfileModal;

    const locBtn = document.getElementById('hdr-location-btn');
    if (locBtn) locBtn.onclick = openLocationModal;

    const bgNotifBtn = document.getElementById('enable-bg-notif-btn');
    if (bgNotifBtn) bgNotifBtn.onclick = requestWebNotifications;

    document.querySelectorAll('[data-utab]').forEach((b) => {
      b.onclick = () => {
        state.userTab = Number(b.dataset.utab);
        state.showDeskRentals = false;
        state.showMyStore = false;
        state.mobileDetailId = '';
        state.storeDetailId = '';
        state.forumThreadId = '';
        state._resetScrollNext = true;
        render();
      };
    });

    const qaAllCountryBtn = document.getElementById('qa-all-country-btn');
    if (qaAllCountryBtn) {
      qaAllCountryBtn.onclick = () => {
        if (isAllScope(state.province)) {
          const prev = state.prevLocation || { province: 'تهران', city: 'تهران', region: 'علاءالدین و جمهوری' };
          state.province = prev.province || 'تهران';
          state.city = prev.city || 'تهران';
          state.region = prev.region || 'همه مناطق';
          toast(`محدوده روی ${state.city} تنظیم شد.`);
        } else {
          state.prevLocation = { province: state.province, city: state.city, region: state.region };
          state.province = 'همه کشور';
          state.city = 'همه شهرها';
          state.region = 'همه مناطق';
          toast('نمایش آگهی‌ها و فروشگاه‌های همه کشور (سراسر ایران) فعال شد.');
        }
        syncSessionWithServiceWorker(buildSwSessionPayload());
        render();
      };
    }

    const qaDeskBtn = document.getElementById('qa-desk-btn');
    if (qaDeskBtn) qaDeskBtn.onclick = () => { state.showDeskRentals = !state.showDeskRentals; state.showMyStore = false; state._resetScrollNext = true; render(); };
    const qaMyStoreBtn = document.getElementById('qa-mystore-btn');
    if (qaMyStoreBtn) qaMyStoreBtn.onclick = () => { state.showMyStore = !state.showMyStore; state.showDeskRentals = false; state._resetScrollNext = true; render(); };

    document.querySelectorAll('[data-atab]').forEach((b) => {
      b.onclick = () => {
        state.adminTab = b.dataset.atab;
        state._resetScrollNext = true;
        render();
      };
    });

    if (state.adminTab !== lastScrolledAdminTab) {
      lastScrolledAdminTab = state.adminTab;
      const activeAdminTab = document.querySelector('#admin-tabs-strip .seg-tab.active');
      if (activeAdminTab && activeAdminTab.scrollIntoView) {
        activeAdminTab.scrollIntoView({ behavior: 'auto', inline: 'center', block: 'nearest' });
      }
    }
  }

  function debouncedRenderSearch(assignFn) {
    assignFn();
    if (state.searchDebounceTimer) clearTimeout(state.searchDebounceTimer);
    state.searchDebounceTimer = setTimeout(() => render(), 180);
  }

  function addToCartItem(item) {
    const existing = state.cart.find((c) => c.id === item.id);
    if (existing) existing.qty += 1;
    else state.cart.push(Object.assign({ qty: 1 }, item));
    toast(`«${item.title}» به سبد خرید اضافه شد.`);
    render();
  }

  function bindContentEvents() {
    // Desk Rental events
    const deskBack = document.getElementById('desk-back-btn');
    if (deskBack) deskBack.onclick = () => { state.showDeskRentals = false; render(); };
    document.querySelectorAll('[data-desksub]').forEach((b) => {
      b.onclick = () => { state.deskSubTab = b.dataset.desksub; render(); };
    });
    document.querySelectorAll('[data-deskcat]').forEach((b) => {
      b.onclick = () => { state.deskCat = b.dataset.deskcat; render(); };
    });
    const deskSearch = document.getElementById('desk-search');
    if (deskSearch) deskSearch.oninput = (e) => debouncedRenderSearch(() => { state.deskSearch = e.target.value; });
    const addDeskBtn = document.getElementById('desk-add-btn');
    if (addDeskBtn) addDeskBtn.onclick = () => openNewDeskModal();
    document.querySelectorAll('[data-chat-generic]').forEach((b) => {
      b.onclick = () => {
        const [adId, title, partner, sellerUid] = b.dataset.chatGeneric.split('|');
        openGenericChatModal(adId, title, partner, sellerUid);
      };
    });
    document.querySelectorAll('[data-del-desk-rental]').forEach((b) => {
      b.onclick = async () => { await api(`/api/desk-rentals/${b.dataset.delDeskRental}`, { method: 'DELETE' }); await loadAppState(true); };
    });
    document.querySelectorAll('[data-del-desk-req]').forEach((b) => {
      b.onclick = async () => { await api(`/api/desk-requests/${b.dataset.delDeskReq}`, { method: 'DELETE' }); await loadAppState(true); };
    });

    // My Store events
    const msBack = document.getElementById('mystore-back-btn');
    if (msBack) msBack.onclick = () => { state.showMyStore = false; render(); };
    const msCreate = document.getElementById('mystore-create-btn');
    if (msCreate) msCreate.onclick = () => openRegisterStoreModal();
    const msEmptyCreate = document.getElementById('mystore-empty-create');
    if (msEmptyCreate) msEmptyCreate.onclick = () => openRegisterStoreModal();
    document.querySelectorAll('[data-sel-mystore]').forEach((b) => {
      b.onclick = () => { state.myStoreId = b.dataset.selMystore; render(); };
    });
    document.querySelectorAll('[data-mssub]').forEach((b) => {
      b.onclick = () => { state.myStoreSubTab = b.dataset.mssub; render(); };
    });
    const msAddMob = document.getElementById('ms-add-mob-btn');
    if (msAddMob) msAddMob.onclick = () => openMyStoreAddMobileModal(state.myStoreId);
    const msAddAcc = document.getElementById('ms-add-acc-btn');
    if (msAddAcc) msAddAcc.onclick = () => openMyStoreAddAccModal(state.myStoreId);
    const msAddHw = document.getElementById('ms-add-hw-btn');
    if (msAddHw) msAddHw.onclick = () => openNewAdModal('hw');
    document.querySelectorAll('[data-ms-del-sm]').forEach((b) => {
      b.onclick = async () => { await api(`/api/store-mobiles/${b.dataset.msDelSm}`, { method: 'DELETE' }); await loadAppState(true); };
    });
    document.querySelectorAll('[data-ms-edit-sm]').forEach((b) => {
      b.onclick = () => {
        const sm = (state.db.storeMobiles || []).find((x) => x.id === b.dataset.msEditSm);
        showModal(`
          <h3 style="margin-top:0;">ویرایش قیمت گوشی</h3>
          <div class="form-group"><input id="ms-p-in" class="input" type="number" dir="ltr" value="${Number(sm?.price || 0)}" /></div>
          <div class="form-group"><input id="ms-info-in" class="input" value="${esc(sm?.info || '')}" placeholder="توضیحات گارانتی" /></div>
          <div class="row space-between">
            <button class="btn btn-outline" onclick="window.__closeModal()">انصراف</button>
            <button id="ms-p-save" class="btn btn-primary">ذخیره</button>
          </div>
        `);
        document.getElementById('ms-p-save').onclick = async () => {
          await api(`/api/store-mobiles/${b.dataset.msEditSm}`, {
            method: 'PATCH',
            body: { price: Number(document.getElementById('ms-p-in').value || 0), info: document.getElementById('ms-info-in').value.trim() }
          });
          closeModal();
          await loadAppState(true);
        };
      };
    });
    document.querySelectorAll('[data-ms-del-acc]').forEach((b) => {
      b.onclick = async () => { await api(`/api/accessories/${b.dataset.msDelAcc}`, { method: 'DELETE' }); await loadAppState(true); };
    });
    document.querySelectorAll('[data-ms-edit-acc]').forEach((b) => {
      b.onclick = () => {
        const ac = (state.db.accessories || []).find((x) => x.id === b.dataset.msEditAcc);
        showModal(`
          <h3 style="margin-top:0;">ویرایش قیمت کالای جانبی</h3>
          <div class="form-group"><input id="msa-p-in" class="input" type="number" dir="ltr" value="${Number(ac?.price || 0)}" /></div>
          <div class="row space-between">
            <button class="btn btn-outline" onclick="window.__closeModal()">انصراف</button>
            <button id="msa-p-save" class="btn btn-primary">ذخیره</button>
          </div>
        `);
        document.getElementById('msa-p-save').onclick = async () => {
          await api(`/api/accessories/${b.dataset.msEditAcc}`, {
            method: 'PATCH',
            body: { price: Number(document.getElementById('msa-p-in').value || 0) }
          });
          closeModal();
          await loadAppState(true);
        };
      };
    });
    const msSaveSet = document.getElementById('ms-save-settings');
    if (msSaveSet) {
      msSaveSet.onclick = async () => {
        await api(`/api/stores/${state.myStoreId}`, {
          method: 'PATCH',
          body: {
            name: document.getElementById('ms-set-name').value.trim(),
            phone: document.getElementById('ms-set-phone').value.trim(),
            address: document.getElementById('ms-set-addr').value.trim(),
            hours: document.getElementById('ms-set-hours').value.trim()
          }
        });
        toast('مشخصات فروشگاه ذخیره شد.');
        await loadAppState(true);
      };
    }

    document.querySelectorAll('[data-promo-banner]').forEach((el) => {
      el.onclick = () => {
        const b = (state.db?.banners || []).find((x) => x.id === el.dataset.promoBanner);
        if (!b) return;
        if (b.linkType === 'url' && b.linkTarget) {
          window.open(b.linkTarget, '_blank', 'noopener,noreferrer');
        } else if (b.linkType === 'store' && b.linkTarget) {
          state.showDeskRentals = false;
          state.showMyStore = false;
          state.mobileDetailId = '';
          state.userTab = 0;
          state.storeDetailId = b.linkTarget;
          state._resetScrollNext = true;
          render();
        }
      };
    });
    document.querySelectorAll('[data-promo-empty]').forEach((el) => {
      el.onclick = () => {
        toast('اینجا جای تبلیغات شماست؛ جهت ثبت بنر با مدیریت بازمو تماس بگیرید.');
      };
    });

    // Mobile tab events
    const mobSearch = document.getElementById('mob-search');
    if (mobSearch) mobSearch.oninput = (e) => debouncedRenderSearch(() => { state.mobileSearch = e.target.value; });
    const mobBudget = document.getElementById('mob-budget');
    if (mobBudget) mobBudget.onchange = (e) => { state.mobileBudgetMax = Number(e.target.value || 0); render(); };
    document.querySelectorAll('[data-mbrand]').forEach((b) => {
      b.onclick = () => { state.mobileBrand = b.dataset.mbrand; render(); };
    });
    document.querySelectorAll('[data-open-mobile]').forEach((el) => {
      el.onclick = () => { state.mobileDetailId = el.dataset.openMobile; render(); };
    });
    document.querySelectorAll('[data-open-store]').forEach((el) => {
      el.onclick = () => { state.storeDetailId = el.dataset.openStore; state.userTab = 0; render(); };
    });
    const mobBack = document.getElementById('mob-back-btn');
    if (mobBack) mobBack.onclick = () => { state.mobileDetailId = ''; render(); };
    const storeBack = document.getElementById('store-back-btn');
    if (storeBack) storeBack.onclick = () => { state.storeDetailId = ''; render(); };
    const mobPostAd = document.getElementById('mob-post-ad-btn');
    if (mobPostAd) mobPostAd.onclick = () => openNewAdModal('mobile');
    const mobAddStore = document.getElementById('mob-add-store-btn');
    if (mobAddStore) mobAddStore.onclick = () => openRegisterStoreModal('mobile');

    document.querySelectorAll('[data-buy-offer]').forEach((b) => {
      b.onclick = () => {
        const o = (state.db.storeMobiles || []).find((x) => x.id === b.dataset.buyOffer);
        if (!o) return;
        const m = (state.db.mobiles || []).find((x) => x.id === o.mobileId);
        const s = (state.db.stores || []).find((x) => x.id === o.storeId);
        addToCartItem({ id: o.id, title: m?.model || 'گوشی موبایل', price: o.price, storeName: s?.name || 'بازمو' });
      };
    });

    // Software tab events
    document.querySelectorAll('[data-swsec]').forEach((b) => {
      b.onclick = () => { state.swSection = b.dataset.swsec; state.swProblemId = ''; state.forumThreadId = ''; render(); };
    });
    const swSearch = document.getElementById('sw-search');
    if (swSearch) swSearch.oninput = (e) => debouncedRenderSearch(() => { state.swSearch = e.target.value; });
    document.querySelectorAll('[data-open-swp]').forEach((el) => {
      el.onclick = () => { state.swProblemId = el.dataset.openSwp; state.swSection = 'detail'; render(); };
    });
    const swBack = document.getElementById('sw-back-home');
    if (swBack) swBack.onclick = () => { state.swSection = 'home'; state.swProblemId = ''; render(); };
    const swAddTut = document.getElementById('sw-add-tut-btn');
    if (swAddTut) swAddTut.onclick = openNewTutorialModal;
    const swAddBox = document.getElementById('sw-add-box-btn');
    if (swAddBox) swAddBox.onclick = openNewBoxModal;

    document.querySelectorAll('[data-buy-tut]').forEach((b) => {
      b.onclick = () => {
        const t = (state.db.tutorials || []).find((x) => x.id === b.dataset.buyTut);
        if (t) addToCartItem({ id: t.id, title: t.title, price: t.price, storeName: t.teacher });
      };
    });
    document.querySelectorAll('[data-buy-box]').forEach((b) => {
      b.onclick = () => {
        const bx = (state.db.boxes || []).find((x) => x.id === b.dataset.buyBox);
        if (bx) addToCartItem({ id: bx.id, title: bx.name, price: bx.price, storeName: bx.seller });
      };
    });
    const swReqSend = document.getElementById('sw-req-send');
    if (swReqSend) {
      swReqSend.onclick = async () => {
        const title = document.getElementById('sw-req-title').value.trim();
        if (!title) return toast('عنوان درخواست را وارد کنید.');
        await api('/api/sw-requests', {
          method: 'POST',
          body: { title, userName: state.session.user?.fullName || state.session.displayName, city: state.city }
        });
        toast('درخواست نرم‌افزاری ثبت شد.');
        await loadAppState(true);
      };
    }

    // Accessories tab events
    const accSearch = document.getElementById('acc-search');
    if (accSearch) accSearch.oninput = (e) => debouncedRenderSearch(() => { state.accSearch = e.target.value; });
    document.querySelectorAll('[data-acat]').forEach((b) => {
      b.onclick = () => { state.accCat = b.dataset.acat; render(); };
    });
    document.querySelectorAll('[data-acc-store]').forEach((el) => {
      el.onclick = () => {
        const sid = el.dataset.accStore;
        state.accFilterStoreId = state.accFilterStoreId === sid ? '' : sid;
        render();
      };
    });
    const clrStore = document.getElementById('acc-clear-store-filter');
    if (clrStore) clrStore.onclick = () => { state.accFilterStoreId = ''; render(); };
    const accPostAd = document.getElementById('acc-post-ad-btn');
    if (accPostAd) accPostAd.onclick = () => openNewAdModal('acc');
    const accAddStore = document.getElementById('acc-add-store-btn');
    if (accAddStore) accAddStore.onclick = () => openRegisterStoreModal('acc');

    document.querySelectorAll('[data-buy-acc]').forEach((b) => {
      b.onclick = () => {
        const a = (state.db.accessories || []).find((x) => x.id === b.dataset.buyAcc);
        if (!a) return;
        const st = (state.db.stores || []).find((s) => s.id === a.storeId);
        addToCartItem({ id: a.id, title: a.name, price: a.price, storeName: st?.name || 'بنکدار جانبی' });
      };
    });
    document.querySelectorAll('[data-inquire-acc]').forEach((b) => {
      b.onclick = async () => {
        const a = (state.db.accessories || []).find((x) => x.id === b.dataset.inquireAcc);
        if (!a) return;
        const st = (state.db.stores || []).find((s) => s.id === a.storeId);
        await api('/api/inquiries', {
          method: 'POST',
          body: { accId: a.id, accName: a.name, storeName: st?.name || 'فروشگاه', userName: state.session.user?.fullName || '' }
        });
        toast('استعلام موجودی به فروشنده ارسال شد.');
        await loadAppState(true);
      };
    });

    // Hardware tab events
    document.querySelectorAll('[data-hwsec]').forEach((b) => {
      b.onclick = () => { state.hwSection = b.dataset.hwsec; state.forumThreadId = ''; render(); };
    });
    const hwSearch = document.getElementById('hw-search');
    if (hwSearch) hwSearch.oninput = (e) => debouncedRenderSearch(() => { state.hwSearch = e.target.value; });
    document.querySelectorAll('[data-hcat]').forEach((b) => {
      b.onclick = () => { state.hwCat = b.dataset.hcat; render(); };
    });
    document.querySelectorAll('[data-open-hw]').forEach((b) => {
      b.onclick = () => { state.hwSelectedAdId = b.dataset.openHw; state.hwSection = 'detail'; render(); };
    });
    const hwBack = document.getElementById('hw-back-home');
    if (hwBack) hwBack.onclick = () => { state.hwSection = 'home'; state.hwSelectedAdId = ''; render(); };
    const hwNewAd = document.getElementById('hw-new-ad-btn');
    if (hwNewAd) hwNewAd.onclick = () => openNewAdModal('hw');
    const hwAddStore = document.getElementById('hw-add-store-btn');
    if (hwAddStore) hwAddStore.onclick = () => openRegisterStoreModal('hw');

    document.querySelectorAll('[data-buy-hw]').forEach((b) => {
      b.onclick = () => {
        const a = (state.db.hwAds || []).find((x) => x.id === b.dataset.buyHw);
        if (a) addToCartItem({ id: a.id, title: a.title, price: a.price, storeName: a.seller });
      };
    });
    document.querySelectorAll('[data-chat-ad]').forEach((b) => {
      b.onclick = () => openChatModal(b.dataset.chatAd);
    });
    document.querySelectorAll('[data-edit-hw-price]').forEach((b) => {
      b.onclick = () => {
        const adId = b.dataset.editHwPrice;
        const ad = (state.db.hwAds || []).find((x) => x.id === adId);
        showModal(`
          <h3 style="margin-top:0;">ویرایش قیمت آگهی</h3>
          <div class="form-group"><input id="edit-p-in" class="input" type="number" dir="ltr" value="${Number(ad?.price || 0)}" /></div>
          <div class="row space-between">
            <button class="btn btn-outline" onclick="window.__closeModal()">انصراف</button>
            <button id="edit-p-save" class="btn btn-primary">ذخیره قیمت</button>
          </div>
        `);
        document.getElementById('edit-p-save').onclick = async () => {
          const price = Number(document.getElementById('edit-p-in').value || 0);
          if (price <= 0) return toast('قیمت معتبر نیست.');
          await api(`/api/hw-ads/${adId}/price`, { method: 'PATCH', body: { price } });
          closeModal();
          toast('قیمت به‌روزرسانی شد.');
          await loadAppState(true);
        };
      };
    });

    const hrSubmit = document.getElementById('hr-submit');
    if (hrSubmit) {
      hrSubmit.onclick = async () => {
        const brand = document.getElementById('hr-brand').value.trim();
        const model = document.getElementById('hr-model').value.trim();
        const partType = document.getElementById('hr-part').value.trim();
        const series = document.getElementById('hr-series').value.trim();
        if (!model || !partType) return toast('مدل و نام قطعه الزامی است.');

        const matches = (state.db.hwAds || []).filter(
          (a) =>
            a.status === 'approved' &&
            (a.model.toLowerCase().includes(model.toLowerCase()) || a.title.toLowerCase().includes(model.toLowerCase())) &&
            (a.title.toLowerCase().includes(partType.toLowerCase()) || a.cat.toLowerCase().includes(partType.toLowerCase()))
        );

        await api('/api/hw-requests', {
          method: 'POST',
          body: {
            brand: brand || 'نامشخص',
            model,
            partType,
            series,
            city: state.city,
            userName: state.session.user?.fullName || '',
            status: matches.length ? 'موجودی هوشمند یافت شد' : 'در حال پیگیری در شبکه'
          }
        });
        await loadAppState(true);

        if (matches.length) {
          showModal(`
            <h3 style="margin-top:0;color:var(--success);">✨ موجودی متناظر در بازار یافت شد!</h3>
            <p class="muted">درخواست شما ثبت شد و ${toPersianNum(matches.length)} آگهی مرتبط هم‌اکنون موجود است:</p>
            ${matches.map((m) => `
              <div class="card" style="margin-bottom:8px;">
                <strong>${esc(m.title)}</strong>
                <div class="muted">${esc(m.seller)} · ${esc(m.city)} · ${formatToman(m.price)}</div>
              </div>
            `).join('')}
            <button class="btn btn-primary btn-block" onclick="window.__closeModal()">متوجه شدم</button>
          `);
        } else {
          toast('درخواست قطعه ثبت و به تامین‌کنندگان ارسال شد.');
        }
      };
    }

    // Forum events
    const forumBack = document.getElementById('forum-back-btn');
    if (forumBack) {
      forumBack.onclick = () => {
        if (state.hwSection === 'forum') state.hwSection = 'home';
        if (state.swSection === 'forum') state.swSection = 'home';
        render();
      };
    }
    const threadBack = document.getElementById('forum-thread-back');
    if (threadBack) threadBack.onclick = () => { state.forumThreadId = ''; render(); };
    const fSearch = document.getElementById('forum-search');
    if (fSearch) fSearch.oninput = (e) => debouncedRenderSearch(() => { state.forumSearch = e.target.value; });
    document.querySelectorAll('[data-fsort]').forEach((b) => {
      b.onclick = () => { state.forumSort = b.dataset.fsort; render(); };
    });
    document.querySelectorAll('[data-fflair]').forEach((b) => {
      b.onclick = () => { state.forumFlair = b.dataset.fflair; render(); };
    });
    const newFPost = document.getElementById('forum-new-post-btn');
    if (newFPost) newFPost.onclick = () => openNewForumPostModal(state.forumBoard);
    document.querySelectorAll('[data-open-thread]').forEach((el) => {
      el.onclick = () => { state.forumThreadId = el.dataset.openThread; render(); };
    });
    document.querySelectorAll('[data-forum-save]').forEach((b) => {
      b.onclick = () => {
        const uid = state.session.uid;
        mutateForumPost(b.dataset.forumSave, (p) => {
          const s = new Set(p.savedBy || []);
          if (s.has(uid)) s.delete(uid); else s.add(uid);
          p.savedBy = Array.from(s);
        });
      };
    });
    document.querySelectorAll('[data-forum-vote]').forEach((b) => {
      b.onclick = () => {
        const [dir, postId] = b.dataset.forumVote.split(':');
        const uid = state.session.uid;
        mutateForumPost(postId, (p) => {
          const ups = new Set(p.upvotedBy || []);
          const downs = new Set(p.downvotedBy || []);
          let votes = Number(p.votes || 0);
          if (dir === 'up') {
            if (ups.has(uid)) { ups.delete(uid); votes -= 1; }
            else {
              if (downs.has(uid)) { downs.delete(uid); votes += 1; }
              ups.add(uid); votes += 1;
            }
          } else {
            if (downs.has(uid)) { downs.delete(uid); votes += 1; }
            else {
              if (ups.has(uid)) { ups.delete(uid); votes -= 1; }
              downs.add(uid); votes -= 1;
            }
          }
          p.votes = votes;
          p.upvotedBy = Array.from(ups);
          p.downvotedBy = Array.from(downs);
        });
      };
    });
    const sendCommentBtn = document.getElementById('forum-comment-send');
    if (sendCommentBtn) {
      sendCommentBtn.onclick = () => {
        const text = document.getElementById('forum-comment-input').value.trim();
        if (!text) return;
        const postId = sendCommentBtn.dataset.postId;
        mutateForumPost(postId, (p) => {
          p.comments = p.comments || [];
          p.comments.push({
            id: 'c_' + Date.now(),
            author: state.session.user?.fullName || state.session.displayName,
            badge: getUserTier(),
            timeLabel: 'هم‌اکنون',
            body: text,
            votes: 1,
            upvotedBy: [state.session.uid],
            replies: []
          });
        });
      };
    }
    document.querySelectorAll('[data-comment-vote]').forEach((b) => {
      b.onclick = () => {
        const [postId, cid] = b.dataset.commentVote.split(':');
        const uid = state.session.uid;
        mutateForumPost(postId, (p) => {
          const c = (p.comments || []).find((x) => x.id === cid);
          if (!c) return;
          const ups = new Set(c.upvotedBy || []);
          if (ups.has(uid)) { ups.delete(uid); c.votes = Math.max(0, Number(c.votes || 0) - 1); }
          else { ups.add(uid); c.votes = Number(c.votes || 0) + 1; }
          c.upvotedBy = Array.from(ups);
        });
      };
    });
    document.querySelectorAll('[data-reply-toggle]').forEach((b) => {
      b.onclick = () => {
        const box = document.getElementById(`reply-box-${b.dataset.replyToggle}`);
        if (box) box.style.display = box.style.display === 'none' ? 'block' : 'none';
      };
    });
    document.querySelectorAll('[data-send-reply]').forEach((b) => {
      b.onclick = () => {
        const [postId, cid] = b.dataset.sendReply.split(':');
        const input = document.getElementById(`reply-input-${cid}`);
        const text = input?.value.trim();
        if (!text) return;
        mutateForumPost(postId, (p) => {
          const c = (p.comments || []).find((x) => x.id === cid);
          if (!c) return;
          c.replies = c.replies || [];
          c.replies.push({
            id: 'r_' + Date.now(),
            author: state.session.user?.fullName || state.session.displayName,
            badge: getUserTier(),
            timeLabel: 'هم‌اکنون',
            body: text
          });
        });
      };
    });

    // Shared delete actions for tutorials/boxes/hwAds
    document.querySelectorAll('[data-del-tut]').forEach((b) => {
      b.onclick = async () => {
        await api(`/api/tutorials/${b.dataset.delTut}`, { method: 'DELETE' });
        await loadAppState(true);
      };
    });
    document.querySelectorAll('[data-del-box]').forEach((b) => {
      b.onclick = async () => {
        await api(`/api/boxes/${b.dataset.delBox}`, { method: 'DELETE' });
        await loadAppState(true);
      };
    });
    document.querySelectorAll('[data-del-hw]').forEach((b) => {
      b.onclick = async () => {
        await api(`/api/hw-ads/${b.dataset.delHw}`, { method: 'DELETE' });
        await loadAppState(true);
      };
    });

    // Admin events
    document.querySelectorAll('[data-goto-atab]').forEach((b) => {
      b.onclick = () => {
        state.adminTab = b.dataset.gotoAtab;
        if (b.dataset.gotoUserSub) state.adminUserSubTab = b.dataset.gotoUserSub;
        if (b.dataset.gotoAdsec) {
          state.adminAdsTile = b.dataset.gotoAdsec;
          state.adminAdsShowAll = false;
        }
        state._resetScrollNext = true;
        render();
      };
    });
    document.querySelectorAll('[data-au-sub]').forEach((b) => {
      b.onclick = () => { state.adminUserSubTab = b.dataset.auSub; render(); };
    });
    document.querySelectorAll('[data-edit-user-loc]').forEach((b) => {
      b.onclick = () => {
        const u = (state.db.users || []).find((x) => x.id === b.dataset.editUserLoc);
        if (u) openAdminEditLocationModal(u);
      };
    });
    const auSearch = document.getElementById('au-search');
    if (auSearch) auSearch.oninput = (e) => debouncedRenderSearch(() => { state.adminUserSearch = e.target.value; });
    const auTier = document.getElementById('au-tier');
    if (auTier) auTier.onchange = (e) => { state.adminUserTier = e.target.value; render(); };
    const auStatus = document.getElementById('au-status');
    if (auStatus) auStatus.onchange = (e) => { state.adminUserStatus = e.target.value; render(); };
    const auProv = document.getElementById('au-prov');
    if (auProv) {
      auProv.onchange = (e) => {
        state.adminUserProvince = e.target.value;
        state.adminUserCity = 'همه شهرها';
        render();
      };
    }
    const auCity = document.getElementById('au-city');
    if (auCity) auCity.onchange = (e) => { state.adminUserCity = e.target.value; render(); };
    const resetUserFiltersFn = () => {
      state.adminUserSearch = '';
      state.adminUserTier = 'همه نقش‌ها';
      state.adminUserStatus = 'all';
      state.adminUserProvince = 'همه استان‌ها';
      state.adminUserCity = 'همه شهرها';
      render();
    };
    const auResetBtn = document.getElementById('au-reset-filters-btn');
    if (auResetBtn) auResetBtn.onclick = resetUserFiltersFn;
    const auResetInline = document.getElementById('au-reset-filters-inline');
    if (auResetInline) auResetInline.onclick = resetUserFiltersFn;
    const auAddBtn = document.getElementById('au-add-btn');
    if (auAddBtn) auAddBtn.onclick = openAdminUserModal;

    // Admin Ads Section Tiles, Search & Banner Management
    document.querySelectorAll('[data-ad-tile]').forEach((b) => {
      b.onclick = () => {
        state.adminAdsTile = b.dataset.adTile;
        state.adminAdsShowAll = false;
        render();
      };
    });
    const adminAdsSearchEl = document.getElementById('admin-ads-search');
    if (adminAdsSearchEl) {
      adminAdsSearchEl.oninput = (e) => debouncedRenderSearch(() => { state.adminAdsSearch = e.target.value; });
    }
    document.querySelectorAll('[data-ad-filter]').forEach((b) => {
      b.onclick = () => { state.adminAdsFilter = b.dataset.adFilter; render(); };
    });
    const showAllAdsBtn = document.getElementById('admin-ads-show-all-btn');
    if (showAllAdsBtn) {
      showAllAdsBtn.onclick = () => { state.adminAdsShowAll = !state.adminAdsShowAll; render(); };
    }
    document.querySelectorAll('[data-banner-sec]').forEach((b) => {
      b.onclick = () => { state.adminBannerSection = b.dataset.bannerSec; render(); };
    });
    const bannerScopeAllBtn = document.getElementById('banner-scope-all-btn');
    if (bannerScopeAllBtn) {
      bannerScopeAllBtn.onclick = () => {
        state.adminBannerProv = 'همه استان‌ها';
        state.adminBannerCity = 'همه شهرها';
        state.adminBannerRegion = 'همه مناطق';
        render();
      };
    }
    const bannerScopeProv = document.getElementById('banner-scope-prov');
    if (bannerScopeProv) {
      bannerScopeProv.onchange = (e) => {
        state.adminBannerProv = e.target.value;
        state.adminBannerCity = 'همه شهرها';
        state.adminBannerRegion = 'همه مناطق';
        render();
      };
    }
    const bannerScopeCity = document.getElementById('banner-scope-city');
    if (bannerScopeCity) {
      bannerScopeCity.onchange = (e) => {
        state.adminBannerCity = e.target.value;
        state.adminBannerRegion = 'همه مناطق';
        render();
      };
    }
    const bannerScopeReg = document.getElementById('banner-scope-region');
    if (bannerScopeReg) {
      bannerScopeReg.onchange = (e) => {
        state.adminBannerRegion = e.target.value;
        render();
      };
    }
    document.querySelectorAll('[data-jump-banner-scope]').forEach((b) => {
      b.onclick = () => {
        const [p, c, r] = String(b.dataset.jumpBannerScope || '').split('|');
        state.adminBannerProv = p || 'همه استان‌ها';
        state.adminBannerCity = c || 'همه شهرها';
        state.adminBannerRegion = r || 'همه مناطق';
        render();
      };
    });
    const getCurBannerScopeAndKey = (slotIdx) => {
      const curSec = state.adminBannerSection || 'mobile';
      const curScope = normalizeBannerScope(state.adminBannerProv, state.adminBannerCity, state.adminBannerRegion);
      const dKey = bannerDraftKey(curSec, slotIdx, curScope);
      return { curSec, curScope, dKey };
    };
    document.querySelectorAll('[data-banner-file]').forEach((inp) => {
      inp.onchange = async () => {
        const slotIdx = Number(inp.dataset.bannerFile);
        const file = inp.files?.[0];
        if (!file) return;
        try {
          const compressed = await compressImageFileToBase64(file);
          const { dKey } = getCurBannerScopeAndKey(slotIdx);
          const prev = state.adminBannerDrafts[dKey] || {};
          state.adminBannerDrafts[dKey] = {
            ...prev,
            imageMime: compressed.imageMime,
            imageBase64: compressed.imageBase64,
            previewUrl: compressed.previewUrl
          };
          render();
        } catch (err) {
          toast(err.message || 'خطا در خواندن تصویر');
        }
      };
    });
    document.querySelectorAll('[data-banner-title]').forEach((inp) => {
      inp.oninput = (e) => {
        const { dKey } = getCurBannerScopeAndKey(Number(inp.dataset.bannerTitle));
        state.adminBannerDrafts[dKey] = { ...(state.adminBannerDrafts[dKey] || {}), title: e.target.value };
      };
    });
    document.querySelectorAll('[data-banner-url]').forEach((inp) => {
      inp.oninput = (e) => {
        const { dKey } = getCurBannerScopeAndKey(Number(inp.dataset.bannerUrl));
        state.adminBannerDrafts[dKey] = { ...(state.adminBannerDrafts[dKey] || {}), linkTarget: e.target.value };
      };
    });
    document.querySelectorAll('[data-banner-store]').forEach((sel) => {
      sel.onchange = (e) => {
        const { dKey } = getCurBannerScopeAndKey(Number(sel.dataset.bannerStore));
        state.adminBannerDrafts[dKey] = { ...(state.adminBannerDrafts[dKey] || {}), linkTarget: e.target.value };
      };
    });
    document.querySelectorAll('[data-banner-linktype]').forEach((sel) => {
      sel.onchange = () => {
        const slotIdx = Number(sel.dataset.bannerLinktype);
        const { dKey } = getCurBannerScopeAndKey(slotIdx);
        state.adminBannerDrafts[dKey] = {
          ...(state.adminBannerDrafts[dKey] || {}),
          linkType: sel.value,
          linkTarget: ''
        };
        const urlWrap = document.getElementById(`banner-url-wrap-${slotIdx}`);
        const stWrap = document.getElementById(`banner-store-wrap-${slotIdx}`);
        if (urlWrap) urlWrap.style.display = sel.value === 'url' ? 'block' : 'none';
        if (stWrap) stWrap.style.display = sel.value === 'store' ? 'block' : 'none';
      };
    });
    document.querySelectorAll('[data-save-banner]').forEach((b) => {
      b.onclick = async () => {
        const slotIdx = Number(b.dataset.saveBanner);
        const { curSec, curScope, dKey } = getCurBannerScopeAndKey(slotIdx);
        const existing = findBannerForExactScope(state.db?.banners || [], curSec, slotIdx, curScope);
        const draft = state.adminBannerDrafts[dKey] || {};
        const fileEl = document.getElementById(`banner-file-${slotIdx}`);
        const file = fileEl?.files?.[0];
        let imageData = draft.imageBase64 || '';
        let imageMime = draft.imageMime || 'image/jpeg';
        if (!imageData && file) {
          const compressed = await compressImageFileToBase64(file);
          imageData = compressed.imageBase64;
          imageMime = compressed.imageMime;
        }

        const title = (document.getElementById(`banner-title-${slotIdx}`)?.value || '').trim();
        const linkType = document.getElementById(`banner-linktype-${slotIdx}`)?.value || 'none';
        const linkTarget = linkType === 'url'
          ? (document.getElementById(`banner-url-${slotIdx}`)?.value || '').trim()
          : linkType === 'store'
            ? (document.getElementById(`banner-store-${slotIdx}`)?.value || '').trim()
            : '';

        if (!imageData && !existing?.hasImage) {
          return toast('لطفاً ابتدا یک تصویر برای بنر انتخاب کنید.');
        }
        if (linkType === 'url' && !linkTarget) {
          return toast('لطفاً آدرس وب‌سایت مقصد را وارد کنید.');
        }
        if (linkType === 'store' && !linkTarget) {
          return toast('لطفاً فروشگاه مقصد را انتخاب کنید.');
        }
        b.disabled = true;
        try {
          const payload = {
            section: curSec,
            slotIndex: slotIdx,
            province: curScope.province,
            city: curScope.city,
            region: curScope.region,
            title,
            linkType,
            linkTarget,
            keepExistingImage: !imageData && Boolean(existing?.hasImage)
          };
          if (imageData) {
            payload.imageMime = imageMime;
            payload.imageData = imageData;
          }
          const res = await api('/api/admin/banners', { method: 'POST', body: payload });
          if (res?.banner && state.db) {
            const list = state.db.banners || [];
            state.db.banners = [
              ...list.filter((x) => x.id !== res.banner.id),
              res.banner
            ];
          }
          delete state.adminBannerDrafts[dKey];
          toast(`بنر شماره ${toPersianNum(slotIdx + 1)} ذخیره شد.`);
          render();
          loadAppState(true, true).catch(() => {});
        } catch (err) {
          toast(err.message || 'خطا در ذخیره بنر');
        } finally {
          b.disabled = false;
        }
      };
    });
    document.querySelectorAll('[data-del-banner]').forEach((b) => {
      b.onclick = async () => {
        try {
          const id = b.dataset.delBanner;
          const dKey = b.dataset.delDkey;
          await api(`/api/admin/banners/${encodeURIComponent(id)}`, { method: 'DELETE' });
          if (state.db) {
            state.db.banners = (state.db.banners || []).filter((x) => x.id !== id);
          }
          if (dKey) delete state.adminBannerDrafts[dKey];
          toast('بنر حذف شد.');
          render();
          loadAppState(true, true).catch(() => {});
        } catch (err) {
          toast(err.message || 'خطا در حذف بنر');
        }
      };
    });

    document.querySelectorAll('[data-approve-reg]').forEach((b) => {
      b.onclick = async () => {
        const rid = b.dataset.approveReg;
        const assignedTier = document.getElementById(`reg-tier-${rid}`)?.value;
        await api(`/api/admin/registration-requests/${rid}/approve`, { method: 'POST', body: { assignedTier } });
        toast('کاربر تایید و فعال شد.');
        await loadAppState(true);
      };
    });
    document.querySelectorAll('[data-reject-reg]').forEach((b) => {
      b.onclick = async () => {
        await api(`/api/admin/registration-requests/${b.dataset.rejectReg}/reject`, { method: 'POST' });
        toast('درخواست رد شد.');
        await loadAppState(true);
      };
    });
    document.querySelectorAll('[data-change-tier]').forEach((sel) => {
      sel.onchange = async () => {
        await api(`/api/admin/users/${sel.dataset.changeTier}/tier`, {
          method: 'PATCH',
          body: { tier: sel.value, email: sel.dataset.email }
        });
        toast('نقش کاربر تغییر یافت.');
        await loadAppState(true);
      };
    });
    document.querySelectorAll('[data-toggle-active]').forEach((b) => {
      b.onclick = async () => {
        const uid = b.dataset.toggleActive;
        const u = (state.db.users || []).find((x) => x.id === uid);
        await api(`/api/admin/users/${uid}/active`, {
          method: 'PATCH',
          body: { active: b.dataset.next === 'true', user: u }
        });
        await loadAppState(true);
      };
    });
    document.querySelectorAll('[data-set-pass]').forEach((b) => {
      b.onclick = () => {
        const uid = b.dataset.setPass;
        const email = b.dataset.email;
        showModal(`
          <h3 style="margin-top:0;">تعیین رمز عبور وب کاربر</h3>
          <div class="muted" style="margin-bottom:10px;">${esc(email)}</div>
          <div class="form-group"><input id="adm-new-pass" class="input" type="password" dir="ltr" placeholder="حداقل ۶ کاراکتر" /></div>
          <div class="row space-between">
            <button class="btn btn-outline" onclick="window.__closeModal()">انصراف</button>
            <button id="adm-pass-save" class="btn btn-primary">ذخیره رمز عبور</button>
          </div>
        `);
        document.getElementById('adm-pass-save').onclick = async () => {
          try {
            await api(`/api/admin/users/${uid}/password`, {
              method: 'PATCH',
              body: { email, password: document.getElementById('adm-new-pass').value }
            });
            closeModal();
            toast('رمز عبور با موفقیت تنظیم شد.');
          } catch (err) {
            toast(err.message);
          }
        };
      };
    });
    document.querySelectorAll('[data-del-user]').forEach((b) => {
      b.onclick = async () => {
        await api(`/api/admin/users/${b.dataset.delUser}?email=${encodeURIComponent(b.dataset.email || '')}`, { method: 'DELETE' });
        await loadAppState(true);
      };
    });

    // Admin Notifications
    const nType = document.getElementById('notif-type');
    if (nType) nType.onchange = (e) => { state.adminNotifType = e.target.value; };
    const nProv = document.getElementById('notif-prov');
    if (nProv) nProv.onchange = (e) => { state.adminNotifProv = e.target.value; state.adminNotifCity = 'همه شهرها'; state.adminNotifRegion = 'همه مناطق'; render(); };
    const nCity = document.getElementById('notif-city');
    if (nCity) nCity.onchange = (e) => { state.adminNotifCity = e.target.value; state.adminNotifRegion = 'همه مناطق'; render(); };
    const nReg = document.getElementById('notif-region');
    if (nReg) nReg.onchange = (e) => { state.adminNotifRegion = e.target.value; render(); };
    const nTier = document.getElementById('notif-tier');
    if (nTier) nTier.onchange = (e) => { state.adminNotifTier = e.target.value; render(); };
    const nSend = document.getElementById('notif-send-btn');
    if (nSend) {
      nSend.onclick = async () => {
        const title = document.getElementById('notif-title').value.trim();
        const body = document.getElementById('notif-body').value.trim();
        if (!title || !body) return toast('عنوان و متن اعلان الزامی است.');
        await api('/api/notifications', {
          method: 'POST',
          body: {
            title,
            body,
            type: state.adminNotifType || 'اطلاعیه صنفی',
            province: state.adminNotifProv,
            city: state.adminNotifCity,
            region: state.adminNotifRegion,
            tierFilter: state.adminNotifTier
          }
        });
        toast('اعلان با موفقیت ارسال شد.');
        await loadAppState(true);
      };
    }
    document.querySelectorAll('[data-del-notif]').forEach((b) => {
      b.onclick = async () => {
        await api(`/api/notifications/${b.dataset.delNotif}`, { method: 'DELETE' });
        await loadAppState(true);
      };
    });

    // Admin Demo Toggle & Store/Desk Approvals
    const admToggleDemo = document.getElementById('admin-toggle-demo-btn');
    if (admToggleDemo) {
      admToggleDemo.onclick = async () => {
        if (state.isSeedingDemo) return;
        const nextVal = !(state.db.showDemoData !== false);
        try {
          await api('/api/admin/demo-visibility', { method: 'POST', body: { visible: nextVal } });
          toast(nextVal ? 'داده‌های نمونه کل ایران نمایش داده می‌شوند.' : 'داده‌های نمونه مخفی شدند (داده‌های واقعی حفظ شدند).');
          await loadAppState(true, true);
        } catch (err) {
          toast(err.message || 'خطا در تغییر وضعیت نمایش داده‌های نمونه');
        }
      };
    }
    const admSeedDemo = document.getElementById('admin-seed-demo-btn');
    if (admSeedDemo) {
      admSeedDemo.onclick = async () => {
        if (state.isSeedingDemo) return;
        state.isSeedingDemo = true;
        render();
        toast('در حال تولید داده‌های نمونه کل ایران...');
        try {
          await api('/api/admin/seed-demo', { method: 'POST', timeoutMs: 95000 });
          toast('داده‌های نمونه ۴۸ منطقه با موفقیت ساخته شد.');
          await loadAppState(true, true);
        } catch (err) {
          toast(err.message || 'خطا در ساخت داده‌های نمونه روی سرور');
        } finally {
          state.isSeedingDemo = false;
          render();
        }
      };
    }

    // Admin Ads / Stores / Desks
    document.querySelectorAll('[data-hw-status]').forEach((b) => {
      b.onclick = async () => {
        try {
          const [id, st] = b.dataset.hwStatus.split(':');
          await api(`/api/hw-ads/${id}/status`, { method: 'PATCH', body: { status: st } });
          await loadAppState(true);
        } catch (err) {
          toast(err.message || 'خطا در انجام عملیات');
        }
      };
    });
    document.querySelectorAll('[data-store-status]').forEach((b) => {
      b.onclick = async () => {
        try {
          const [id, st] = b.dataset.storeStatus.split(':');
          await api(`/api/stores/${id}/status`, { method: 'PATCH', body: { status: st } });
          toast(st === 'approved' ? 'فروشگاه تایید شد.' : 'فروشگاه رد شد.');
          await loadAppState(true);
        } catch (err) {
          toast(err.message || 'خطا در انجام عملیات');
        }
      };
    });
    document.querySelectorAll('[data-desk-status]').forEach((b) => {
      b.onclick = async () => {
        try {
          const [id, st] = b.dataset.deskStatus.split(':');
          await api(`/api/desk-rentals/${id}/status`, { method: 'PATCH', body: { status: st } });
          toast(st === 'approved' ? 'آگهی میز کار تایید شد.' : 'آگهی میز کار رد شد.');
          await loadAppState(true);
        } catch (err) {
          toast(err.message || 'خطا در انجام عملیات');
        }
      };
    });
    document.querySelectorAll('[data-app-tut]').forEach((b) => {
      b.onclick = async () => { await api(`/api/tutorials/${b.dataset.appTut}/approve`, { method: 'POST' }); await loadAppState(true); };
    });
    document.querySelectorAll('[data-app-box]').forEach((b) => {
      b.onclick = async () => { await api(`/api/boxes/${b.dataset.appBox}/approve`, { method: 'POST' }); await loadAppState(true); };
    });
    const adminAddStore = document.getElementById('admin-add-store-btn');
    if (adminAddStore) adminAddStore.onclick = () => openRegisterStoreModal();
    document.querySelectorAll('[data-del-store]').forEach((b) => {
      b.onclick = async () => { await api(`/api/stores/${b.dataset.delStore}`, { method: 'DELETE' }); await loadAppState(true); };
    });

    // Admin Requests / Orders
    document.querySelectorAll('[data-order-status]').forEach((sel) => {
      sel.onchange = async () => {
        await api(`/api/orders/${sel.dataset.orderStatus}/status`, { method: 'PATCH', body: { status: sel.value } });
        toast('وضعیت سفارش به‌روزرسانی شد.');
        await loadAppState(true);
      };
    });
    document.querySelectorAll('[data-del-order]').forEach((b) => {
      b.onclick = async () => { await api(`/api/orders/${b.dataset.delOrder}`, { method: 'DELETE' }); await loadAppState(true); };
    });
    document.querySelectorAll('[data-inq-status]').forEach((b) => {
      b.onclick = async () => {
        const [id, st] = b.dataset.inqStatus.split(':');
        await api(`/api/inquiries/${id}/status`, { method: 'PATCH', body: { status: st } });
        await loadAppState(true);
      };
    });
    document.querySelectorAll('[data-reply-hr]').forEach((b) => {
      b.onclick = async () => {
        const note = document.getElementById(`hr-reply-in-${b.dataset.replyHr}`)?.value.trim() || '';
        await api(`/api/hw-requests/${b.dataset.replyHr}/reply`, { method: 'POST', body: { note } });
        toast('پاسخ ثبت شد.');
        await loadAppState(true);
      };
    });
    document.querySelectorAll('[data-reply-sr]').forEach((b) => {
      b.onclick = async () => {
        const reply = document.getElementById(`sr-reply-in-${b.dataset.replySr}`)?.value.trim() || '';
        await api(`/api/sw-requests/${b.dataset.replySr}/reply`, { method: 'POST', body: { reply } });
        toast('پاسخ ثبت شد.');
        await loadAppState(true);
      };
    });

    // Admin Catalog
    const addMobBtn = document.getElementById('cat-add-mobile');
    if (addMobBtn) {
      addMobBtn.onclick = async () => {
        const brand = document.getElementById('cat-m-brand').value.trim();
        const model = document.getElementById('cat-m-model').value.trim();
        const specs = document.getElementById('cat-m-specs').value.trim();
        const price = Number(document.getElementById('cat-m-price').value || 0);
        if (!brand || !model) return toast('برند و مدل الزامی است.');
        await api('/api/mobiles', {
          method: 'POST',
          body: {
            mobile: { brand, model, specs, imgEmoji: '📱' },
            price: price > 0 ? { storeId: (state.db.stores || [])[0]?.id || 'st1', price, info: 'موجود در انبار' } : null,
            offer: price > 0 ? { storeId: (state.db.stores || [])[0]?.id || 'st1', price, info: 'موجود در انبار' } : null
          }
        });
        toast('مدل موبایل اضافه شد.');
        await loadAppState(true);
      };
    }
    const addAccBtn = document.getElementById('cat-add-acc');
    if (addAccBtn) {
      addAccBtn.onclick = async () => {
        const cat = document.getElementById('cat-a-cat').value;
        const name = document.getElementById('cat-a-name').value.trim();
        const price = Number(document.getElementById('cat-a-price').value || 0);
        const desc = document.getElementById('cat-a-desc').value.trim();
        if (!name || price <= 0) return toast('نام و قیمت محصول الزامی است.');
        await api('/api/accessories', {
          method: 'POST',
          body: { cat, name, price, desc, storeId: (state.db.stores || [])[0]?.id || 'st5' }
        });
        toast('محصول جانبی اضافه شد.');
        await loadAppState(true);
      };
    }
    document.querySelectorAll('[data-fmod]').forEach((b) => {
      b.onclick = async () => {
        const [id, field, val] = b.dataset.fmod.split(':');
        const p = (state.db.forumPosts || []).find((x) => x.id === id);
        if (!p) return;
        const body = {
          pinned: field === 'pinned' ? val === 'true' : p.pinned,
          verifiedSolution: field === 'verified' ? val === 'true' : p.verifiedSolution
        };
        await api(`/api/forum-posts/${id}/moderation`, { method: 'PATCH', body });
        await loadAppState(true);
      };
    });
    document.querySelectorAll('[data-del-fpost]').forEach((b) => {
      b.onclick = async () => { await api(`/api/forum-posts/${b.dataset.delFpost}`, { method: 'DELETE' }); await loadAppState(true); };
    });
    const resetBtn = document.getElementById('cat-reset-btn');
    if (resetBtn) {
      resetBtn.onclick = () => {
        showModal(`
          <h3 style="margin-top:0;color:var(--danger);">تایید بازنشانی داده‌ها</h3>
          <p>آیا مطمئن هستید که می‌خواهید داده‌های نمونه سیستم را به حالت پیش‌فرض کارخانه بازگردانید؟</p>
          <div class="row space-between">
            <button class="btn btn-outline" onclick="window.__closeModal()">انصراف</button>
            <button id="confirm-reset-yes" class="btn btn-danger">بله، بازنشانی شود</button>
          </div>
        `);
        document.getElementById('confirm-reset-yes').onclick = async () => {
          await api('/api/admin/reset-defaults', { method: 'POST' });
          closeModal();
          toast('داده‌ها به حالت اولیه بازنشانی شدند.');
          await loadAppState(true);
        };
      };
    }
  }

  init();
})();
