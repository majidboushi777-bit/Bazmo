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
    { id: 'notifs', label: '🔔 اعلان‌ها' },
    { id: 'ads', label: '📢 آگهی‌ها' },
    { id: 'requests', label: '📦 سفارش‌ها' },
    { id: 'catalog', label: '🗂️ کاتالوگ' }
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
    adminNotifProv: 'همه استان‌ها',
    adminNotifCity: 'همه شهرها',
    adminNotifRegion: 'همه مناطق',
    adminNotifTier: 'همه',
    // Runtime
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
    isSeedingDemo: false
  };

  function normalizeZwnj(val) {
    return String(val || '')
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
      n === 'همه' ||
      n === 'همهاستانها' ||
      n === 'همهشهرها' ||
      n === 'همهمناطق' ||
      n === 'همهبخشها'
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

  function canCreateStore() {
    if (!state.session) return false;
    return state.session.isAdmin || STORE_CREATOR_ROLES.has(getUserTier());
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
    if (region && !isAllScope(region)) {
      const exactReg = items.filter((item) => {
        const l = getLoc(item);
        return normalizeZwnj(l.city) === normalizeZwnj(city) && normalizeZwnj(l.region) === normalizeZwnj(region);
      });
      if (exactReg.length) return { list: exactReg, scope: `${city} (${region})` };
    }
    const exactCity = items.filter((item) => normalizeZwnj(getLoc(item).city) === normalizeZwnj(city));
    if (exactCity.length) return { list: exactCity, scope: `شهر ${city}` };

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
        logoutLocal();
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
      if (res.status === 401) logoutLocal();
      throw new Error(data.error || `خطای سرور (${res.status})`);
    }
    const nextEtag = res.headers.get('ETag');
    if (isAsciiEtag(nextEtag)) state.stateEtag = nextEtag;
    return data;
  }

  function logoutLocal() {
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
    const provMatch = isAllScope(n.province) || normalizeZwnj(n.province) === normalizeZwnj(state.province);
    const cityMatch = isAllScope(n.city) || normalizeZwnj(n.city) === normalizeZwnj(state.city);
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

  async function loadAppState(silent = false, forceRefresh = false) {
    if (!state.token) return;
    if (forceRefresh) state.stateEtag = '';
    if (!silent) {
      state.loading = true;
      render();
    }
    try {
      const [sessionRes, dbRes] = await Promise.all([api('/api/auth/session'), fetchStateWithEtag()]);
      if (!dbRes && state.db) {
        state.session = sessionRes;
        return;
      }
      const prevNotifIds = new Set((state.db?.notifications || []).map((n) => n.id));
      state.session = sessionRes;
      state.db = dbRes;
      if (sessionRes.user) {
        if (!state.hasSyncedOnce) {
          state.province = sessionRes.user.province || 'تهران';
          state.city = sessionRes.user.city || 'تهران';
          state.region = sessionRes.user.region || 'همه مناطق';
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
        await registerBackgroundNotifSync();
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
    await initServiceWorker();
    try {
      state.config = await api('/api/config/public');
    } catch {}
    if (state.token) {
      await loadAppState(false);
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
    return `
      <div class="login-shell">
        <div class="login-hero-bg"></div>
        <div class="login-container">
          <div class="brand-badge">
            <svg width="38" height="38" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
              <path d="M17 1.01L7 1c-1.1 0-2 .9-2 2v18c0 1.1.9 2 2 2h10c1.1 0 2-.9 2-2V3c0-1.1-.9-1.99-2-1.99zM17 19H7V5h10v14z"/>
            </svg>
          </div>
          <h1 class="brand-title">بازمو</h1>
          <p class="brand-sub">شبکه یکپارچه و رسمی صنف موبایل ایران</p>

          <div class="card login-glass-card">
            <h2 style="margin:0 0 4px;font-size:18px;text-align:center;color:var(--navy-deep);font-weight:800;">ورود همکاران صنف موبایل</h2>
            <p class="muted" style="text-align:center;margin:0 0 16px;">ورود به حساب‌های ثبت‌نام‌شده با ایمیل و رمز عبور</p>

            ${state.loginError ? `<div class="alert-error">${esc(state.loginError)}</div>` : ''}
            ${state.loginSuccess ? `<div class="alert-success">${esc(state.loginSuccess)}</div>` : ''}

            <form id="password-login-form">
              <div class="form-group">
                <label class="form-label" for="login-email">ایمیل (نام کاربری)</label>
                <div class="glass-input-wrap">
                  <input id="login-email" class="glass-input" type="email" dir="ltr" placeholder="name@example.com" value="${esc(state.loginEmail)}" ${state.isLoggingIn ? 'disabled' : ''} required />
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
          localStorage.setItem('bazmo_web_token', state.token);
          state.hasSyncedOnce = false;
          await loadAppState(false);
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
      regBtn.onclick = openRegistrationModal;
    }
  }

  function openRegistrationModal() {
    let selProv = 'تهران';
    let selCity = 'تهران';
    const draw = () => {
      const cities = Object.keys(LOCATIONS[selProv] || {});
      if (!cities.includes(selCity)) selCity = cities[0] || '';
      const regions = LOCATIONS[selProv]?.[selCity] || ['همه مناطق'];

      showModal(`
        <h3 style="margin-top:0;">درخواست عضویت در بازمو</h3>
        <div class="form-group">
          <label class="form-label">ایمیل معتبر جهت ورود</label>
          <input id="reg-email" class="input" type="email" dir="ltr" placeholder="name@example.com" />
        </div>
        <div class="form-group">
          <label class="form-label">رمز عبور انتخابی (جهت ورود پس از تایید، حداقل ۶ کاراکتر)</label>
          <input id="reg-pass" class="input" type="password" dir="ltr" />
        </div>
        <div class="form-group">
          <label class="form-label">نام و نام خانوادگی</label>
          <input id="reg-name" class="input" placeholder="مثلاً: علی رضایی" />
        </div>
        <div class="form-group">
          <label class="form-label">شماره تماس / واتساپ</label>
          <input id="reg-phone" class="input" dir="ltr" placeholder="0912..." />
        </div>
        <div class="form-group">
          <label class="form-label">نقش صنفی درخواستی</label>
          <select id="reg-tier" class="select">
            ${ALL_ROLES.map((r) => `<option value="${esc(r)}">${esc(r)}</option>`).join('')}
          </select>
        </div>
        <div class="grid" style="grid-template-columns:repeat(3,1fr);gap:8px;margin-bottom:12px;">
          <div>
            <label class="form-label">استان</label>
            <select id="reg-prov" class="select">
              ${Object.keys(LOCATIONS).map((p) => `<option value="${esc(p)}" ${p === selProv ? 'selected' : ''}>${esc(p)}</option>`).join('')}
            </select>
          </div>
          <div>
            <label class="form-label">شهر</label>
            <select id="reg-city" class="select">
              ${cities.map((c) => `<option value="${esc(c)}" ${c === selCity ? 'selected' : ''}>${esc(c)}</option>`).join('')}
            </select>
          </div>
          <div>
            <label class="form-label">منطقه</label>
            <select id="reg-reg" class="select">
              ${regions.map((r) => `<option value="${esc(r)}">${esc(r)}</option>`).join('')}
            </select>
          </div>
        </div>
        <div class="form-group">
          <label class="form-label">توضیحات / نام فروشگاه (اختیاری)</label>
          <textarea id="reg-note" class="textarea" rows="2"></textarea>
        </div>
        <div class="row space-between">
          <button type="button" class="btn btn-outline" onclick="window.__closeModal()">انصراف</button>
          <button type="button" id="reg-submit" class="btn btn-primary">ارسال درخواست</button>
        </div>
      `);

      document.getElementById('reg-prov').onchange = (e) => {
        selProv = e.target.value;
        selCity = Object.keys(LOCATIONS[selProv] || {})[0] || '';
        draw();
      };
      document.getElementById('reg-city').onchange = (e) => {
        selCity = e.target.value;
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
    let title = 'حساب کاربری نیاز به تایید دارد';
    let body = 'حساب شما هنوز فعال‌سازی نشده است. می‌توانید درخواست عضویت بفرستید.';
    if (st === 'inactive') {
      title = 'حساب کاربری غیرفعال است';
      body = 'دسترسی حساب شما توسط مدیر سیستم غیرفعال شده است. برای پیگیری با پشتیبانی تماس بگیرید.';
    }
    return `
      <div class="login-shell">
        <div class="login-container">
          <div class="card">
            <h2 style="margin-top:0;">${esc(title)}</h2>
            <p class="muted">${esc(state.session.email)}</p>
            <p>${esc(body)}</p>
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
              <span class="glass-pill-text">${esc(state.city)}${!isAllScope(state.region) ? ` · ${esc(state.region)}` : ''}</span>
            </button>
          </div>
          <nav class="header-segmented" aria-label="بخش‌های اصلی">
            ${USER_TABS.map((t) => `<button class="seg-tab ${state.userTab === t.id ? 'active' : ''}" data-utab="${t.id}">${t.label}</button>`).join('')}
          </nav>
        ` : `
          <nav class="header-segmented admin-tabs" id="admin-tabs-strip" aria-label="منوهای مدیریت">
            ${ADMIN_TABS.map((t) => `<button class="seg-tab ${state.adminTab === t.id ? 'active' : ''}" data-atab="${t.id}">${t.label}</button>`).join('')}
          </nav>
          <div class="swipe-hint">← برای جابه‌جایی بین بخش‌ها به راست و چپ بکشید →</div>
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
            <option value="15000000" ${state.mobileBudgetMax === 15000000 ? 'selected' : ''}>زیر ۱۵ میلیون</option>
            <option value="35000000" ${state.mobileBudgetMax === 35000000 ? 'selected' : ''}>زیر ۳۵ میلیون</option>
            <option value="65000000" ${state.mobileBudgetMax === 65000000 ? 'selected' : ''}>زیر ۶۵ میلیون</option>
          </select>
          <button id="mob-post-ad-btn" class="btn btn-gold btn-sm">+ ثبت آگهی</button>
          ${canCreateStore() ? `<button id="mob-add-store-btn" class="btn btn-navy btn-sm">+ ثبت فروشگاه</button>` : ''}
        </div>
      </div>

      <div class="chip-row">
        ${brands.map((b) => `<button class="chip ${state.mobileBrand === b ? 'active' : ''}" data-mbrand="${esc(b)}">${esc(b)}</button>`).join('')}
      </div>

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

    if (!swAccess) {
      return `
        <div class="warn-box">
          <div style="font-size:15px;font-weight:900;color:#92400e;">⚠️ بخش تخصصی نرم‌افزار و انجمن نرم‌افزارکاران قفل است</div>
          <p style="margin:6px 0 0;">دسترسی به آموزش‌های تخصصی، باکس‌ها، فایل‌های تست‌پوینت و انجمن نرم‌افزار فقط برای نقش <strong>نرم‌افزارکار</strong> مجاز است.</p>
          <p class="muted" style="margin:4px 0 0;">نقش فعلی شما: <strong>${esc(getUserTier())}</strong></p>
        </div>
        <div class="section-header-title"><span>سرفصل‌های بخش نرم‌افزار (نمای قفل‌شده)</span></div>
        <div class="grid">
          ${problems.map((p) => `
            <div class="card" style="opacity:0.62;">
              <div style="font-size:28px;">${esc(p.iconEmoji)}</div>
              <h4 style="margin:8px 0 4px;">🔒 ${esc(p.title)}</h4>
              <span class="muted">نیازمند نقش نرم‌افزارکار</span>
            </div>
          `).join('')}
        </div>
      `;
    }

    const q = state.swSearch.trim().toLowerCase();

    return `
      <div class="sub-tabs">
        <button class="sub-tab ${state.swSection === 'home' || state.swSection === 'detail' ? 'active' : ''}" data-swsec="home">آموزش‌ها و حل مشکل</button>
        <button class="sub-tab ${state.swSection === 'boxes' ? 'active' : ''}" data-swsec="boxes">باکس و دانگل</button>
        <button class="sub-tab ${state.swSection === 'req' ? 'active' : ''}" data-swsec="req">درخواست راهنما</button>
        <button class="sub-tab" data-swsec="forum">💬 انجمن نرم‌افزار</button>
      </div>

      ${state.swSection === 'home' ? `
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

      ${state.swSection === 'detail' ? `
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
          ${canCreateStore() ? `<button id="acc-add-store-btn" class="btn btn-navy btn-sm">+ ثبت فروشگاه</button>` : ''}
        </div>
      </div>

      <div class="chip-row">
        ${cats.map((c) => `<button class="chip ${state.accCat === c ? 'active' : ''}" data-acat="${esc(c)}">${esc(c)}</button>`).join('')}
      </div>

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

      ${state.hwSection === 'home' ? `
        <div class="row space-between" style="margin-bottom:10px;">
          <input id="hw-search" class="input toolbar-search" placeholder="جستجوی قطعه، آی‌سی، فلت یا مدل گوشی..." value="${esc(state.hwSearch)}" />
          <div class="row">
            <button id="hw-new-ad-btn" class="btn btn-primary btn-sm">+ ثبت آگهی قطعه</button>
            ${canCreateStore() ? `<button id="hw-add-store-btn" class="btn btn-navy btn-sm">+ ثبت فروشگاه</button>` : ''}
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
    const orders = db.orders || [];
    const revenue = orders.reduce((s, o) => s + Number(o.total || 0), 0);

    if (state.adminTab === 'dashboard') {
      const demoVisible = db.showDemoData !== false;
      const pendingStoresCount = (db.stores || []).filter((s) => s.status === 'pending').length;
      const pendingDesksCount = (db.deskRentals || []).filter((d) => d.status === 'pending').length;
      return `
        <div class="stat-card" style="background:linear-gradient(135deg,#0f172a,#1e3a5f);color:#fff;margin-bottom:12px;">
          <div style="font-size:12px;color:#bfdbfe;">مجموع ارزش گردش سفارش‌های بازمو</div>
          <div style="font-size:26px;font-weight:900;margin-top:4px;">${formatToman(revenue)}</div>
        </div>
        <div class="demo-toggle-card">
          <div class="row space-between">
            <div style="flex:1;min-width:200px;">
              <strong style="color:var(--navy-deep);">نمایش داده‌های نمونه کل ایران (موقت)</strong>
              <div class="muted" style="margin-top:3px;">
                اطلاعات واقعی ثبت‌شده توسط کاربران و مدیر همیشه حفظ می‌شوند. با این دکمه فقط داده‌های نمونه برای دمو نمایش داده یا مخفی می‌شوند.
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
        <div class="grid" style="margin-bottom:14px;">
          <div class="stat-card"><div class="muted">کل کاربران سیستم</div><div class="stat-num">${toPersianNum(users.length)}</div></div>
          <div class="stat-card"><div class="muted">درخواست‌های عضویت منتظر</div><div class="stat-num" style="color:var(--gold);">${toPersianNum(pendingRegs.length)}</div></div>
          <div class="stat-card"><div class="muted">آگهی و فروشگاه منتظر تایید</div><div class="stat-num" style="color:var(--danger);">${toPersianNum(pendingAds.length + pendingStoresCount + pendingDesksCount)}</div></div>
          <div class="stat-card"><div class="muted">کل سفارش‌ها</div><div class="stat-num">${toPersianNum(orders.length)}</div></div>
          <div class="stat-card"><div class="muted">فروشگاه‌ها و بنکداران</div><div class="stat-num">${toPersianNum((db.stores || []).length)}</div></div>
          <div class="stat-card"><div class="muted">میزهای کار و درخواست‌ها</div><div class="stat-num">${toPersianNum((db.deskRentals || []).length + (db.deskRequests || []).length)}</div></div>
        </div>
        <div class="card">
          <h4 style="margin-top:0;">دسترسی سریع مدیریت</h4>
          <div class="row">
            <button class="btn btn-primary btn-sm" data-goto-atab="users">👥 بررسی کاربران (${toPersianNum(pendingRegs.length)} منتظر)</button>
            <button class="btn btn-gold btn-sm" data-goto-atab="notifs">🔔 ارسال اعلان منطقه‌ای</button>
            <button class="btn btn-navy btn-sm" data-goto-atab="ads">📢 تایید آگهی/فروشگاه/میز (${toPersianNum(pendingAds.length + pendingStoresCount + pendingDesksCount)})</button>
            <button class="btn btn-outline btn-sm" data-goto-atab="requests">📦 پیگیری سفارش‌ها</button>
          </div>
        </div>
      `;
    }

    if (state.adminTab === 'users') {
      const q = state.adminUserSearch.trim().toLowerCase();
      const filteredUsers = users.filter((u) => {
        const searchOk = !q || u.fullName.toLowerCase().includes(q) || (u.email || '').toLowerCase().includes(q) || (u.phone || '').includes(q);
        const tierOk = isAllScope(state.adminUserTier) || u.tier === state.adminUserTier;
        const stOk = state.adminUserStatus === 'all' || (state.adminUserStatus === 'active' && u.active) || (state.adminUserStatus === 'blocked' && !u.active);
        const provOk = isAllScope(state.adminUserProvince) || normalizeZwnj(u.province) === normalizeZwnj(state.adminUserProvince);
        return searchOk && tierOk && stOk && provOk;
      });

      return `
        ${pendingRegs.length ? `
          <div class="card" style="margin-bottom:12px;border:2px solid var(--gold);background:#fffbeb;">
            <h4 style="margin-top:0;">درخواست‌های عضویت در انتظار تایید (${toPersianNum(pendingRegs.length)})</h4>
            ${pendingRegs.map((r) => `
              <div class="card" style="margin-bottom:8px;">
                <div class="row space-between">
                  <div>
                    <strong>${esc(r.fullName)}</strong>
                    <div class="muted">${esc(r.email)} · ${esc(r.phone)}</div>
                    <div class="muted">${esc(r.province)} - ${esc(r.city)} (${esc(r.region)})</div>
                  </div>
                  <div class="row">
                    <select class="select" style="width:auto;" id="reg-tier-${esc(r.id)}">
                      ${ALL_ROLES.map((role) => `<option value="${esc(role)}" ${role === r.requestedTier ? 'selected' : ''}>${esc(role)}</option>`).join('')}
                    </select>
                    <button class="btn btn-success btn-sm" data-approve-reg="${esc(r.id)}">✓ تایید</button>
                    <button class="btn btn-danger btn-sm" data-reject-reg="${esc(r.id)}">✕ رد</button>
                  </div>
                </div>
              </div>
            `).join('')}
          </div>
        ` : ''}

        <div class="row space-between" style="margin-bottom:10px;">
          <input id="au-search" class="input toolbar-search" placeholder="جستجو بر اساس نام، ایمیل یا تلفن..." value="${esc(state.adminUserSearch)}" />
          <div class="row">
            <select id="au-tier" class="select" style="width:auto;">
              <option value="همه نقش‌ها">همه نقش‌ها</option>
              ${ALL_ROLES.map((r) => `<option value="${esc(r)}" ${state.adminUserTier === r ? 'selected' : ''}>${esc(r)}</option>`).join('')}
            </select>
            <select id="au-status" class="select" style="width:auto;">
              <option value="all" ${state.adminUserStatus === 'all' ? 'selected' : ''}>همه وضعیت‌ها</option>
              <option value="active" ${state.adminUserStatus === 'active' ? 'selected' : ''}>فعال</option>
              <option value="blocked" ${state.adminUserStatus === 'blocked' ? 'selected' : ''}>مسدود</option>
            </select>
            <select id="au-prov" class="select" style="width:auto;">
              <option value="همه استان‌ها">همه استان‌ها</option>
              ${Object.keys(LOCATIONS).map((p) => `<option value="${esc(p)}" ${state.adminUserProvince === p ? 'selected' : ''}>${esc(p)}</option>`).join('')}
            </select>
            <button id="au-add-btn" class="btn btn-primary btn-sm">+ افزودن / ویرایش کاربر</button>
          </div>
        </div>

        <div class="grid">
          ${filteredUsers.map((u) => `
            <div class="card">
              <div class="row space-between">
                <strong>${esc(u.fullName)}</strong>
                <span class="badge ${u.active ? 'badge-green' : 'badge-red'}">${u.active ? 'فعال' : 'مسدود'}</span>
              </div>
              <div class="muted">${esc(u.email)} · ${esc(u.phone)}</div>
              <div class="muted">${esc(u.province)} - ${esc(u.city)} (${esc(u.region)})</div>
              <div class="row space-between" style="margin-top:10px;">
                <select class="select" style="width:auto;" data-change-tier="${esc(u.id)}" data-email="${esc(u.email)}">
                  ${ALL_ROLES.map((r) => `<option value="${esc(r)}" ${u.tier === r ? 'selected' : ''}>${esc(r)}</option>`).join('')}
                </select>
                <div class="row">
                  <button class="btn btn-outline btn-sm" data-set-pass="${esc(u.id)}" data-email="${esc(u.email)}">🔑 رمز وب</button>
                  <button class="btn ${u.active ? 'btn-danger' : 'btn-success'} btn-sm" data-toggle-active="${esc(u.id)}" data-next="${!u.active}">${u.active ? 'مسدود' : 'فعال'}</button>
                  <button class="btn btn-outline btn-sm" data-del-user="${esc(u.id)}" data-email="${esc(u.email)}">حذف</button>
                </div>
              </div>
            </div>
          `).join('')}
        </div>
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

      return `
        <div class="card" style="margin-bottom:14px;">
          <h4 style="margin-top:0;">ارسال اعلان منطقه‌ای و صنفی</h4>
          <div class="form-group"><input id="notif-title" class="input" placeholder="عنوان اعلان" /></div>
          <div class="form-group"><textarea id="notif-body" class="textarea" rows="3" placeholder="متن اعلان..."></textarea></div>
          <div class="grid" style="grid-template-columns:repeat(auto-fit,minmax(140px,1fr));gap:8px;margin-bottom:10px;">
            <select id="notif-prov" class="select">
              <option value="همه استان‌ها">همه استان‌ها</option>
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
      return `
        <div class="section-header-title"><span>📢 مدیریت آگهی‌های سخت‌افزار، موبایل و جانبی</span></div>
        <div class="grid" style="margin-bottom:14px;">
          ${ads.map((a) => `
            <div class="card">
              <div class="row space-between">
                <span class="badge badge-blue">${a.section === 'mobile' ? 'موبایل' : a.section === 'acc' ? 'جانبی' : 'سخت‌افزار'} · ${esc(a.cat)}</span>
                <span class="badge ${a.status === 'approved' ? 'badge-green' : a.status === 'rejected' ? 'badge-red' : 'badge-gold'}">${a.status === 'approved' ? 'تاییدشده' : a.status === 'rejected' ? 'ردشده' : 'در انتظار'}</span>
              </div>
              <h4 style="margin:8px 0 4px;">${esc(a.title)}</h4>
              <div class="muted">${esc(a.seller)} · ${esc(a.city)} · ${formatToman(a.price)}</div>
              <div class="row" style="margin-top:8px;">
                ${a.status !== 'approved' ? `<button class="btn btn-success btn-sm" data-hw-status="${esc(a.id)}:approved">✓ تایید</button>` : ''}
                ${a.status !== 'rejected' ? `<button class="btn btn-gold btn-sm" data-hw-status="${esc(a.id)}:rejected">✕ رد</button>` : ''}
                <button class="btn btn-danger btn-sm" data-del-hw="${esc(a.id)}">حذف</button>
              </div>
            </div>
          `).join('')}
        </div>

        <div class="section-header-title"><span>🎓 تایید آموزش‌ها و باکس‌ها</span></div>
        <div class="grid" style="margin-bottom:14px;">
          ${tuts.map((t) => `
            <div class="card">
              <div class="row space-between">
                <strong>آموزش: ${esc(t.title)}</strong>
                <span class="badge ${t.status === 'approved' ? 'badge-green' : 'badge-gold'}">${t.status}</span>
              </div>
              <div class="muted">${esc(t.teacher)} · ${formatToman(t.price)}</div>
              <div class="row" style="margin-top:8px;">
                ${t.status !== 'approved' ? `<button class="btn btn-success btn-sm" data-app-tut="${esc(t.id)}">تایید</button>` : ''}
                <button class="btn btn-danger btn-sm" data-del-tut="${esc(t.id)}">حذف</button>
              </div>
            </div>
          `).join('')}
          ${boxes.map((b) => `
            <div class="card">
              <div class="row space-between">
                <strong>باکس: ${esc(b.name)}</strong>
                <span class="badge ${b.status === 'approved' ? 'badge-green' : 'badge-gold'}">${b.status}</span>
              </div>
              <div class="muted">${esc(b.seller)} · ${formatToman(b.price)}</div>
              <div class="row" style="margin-top:8px;">
                ${b.status !== 'approved' ? `<button class="btn btn-success btn-sm" data-app-box="${esc(b.id)}">تایید</button>` : ''}
                <button class="btn btn-danger btn-sm" data-del-box="${esc(b.id)}">حذف</button>
              </div>
            </div>
          `).join('')}
        </div>

        <div class="section-header-title">
          <span>🏪 فروشگاه‌های ثبت‌شده (${toPersianNum((db.stores || []).length)})</span>
          <button id="admin-add-store-btn" class="btn btn-primary btn-sm">+ ثبت فروشگاه جدید</button>
        </div>
        <div class="grid" style="margin-bottom:14px;">
          ${(db.stores || []).map((s) => `
            <div class="card">
              <div class="row space-between">
                <strong>${esc(s.name)}</strong>
                <span class="badge ${s.status === 'approved' ? 'badge-green' : s.status === 'rejected' ? 'badge-red' : 'badge-gold'}">${s.status === 'approved' ? 'تاییدشده' : s.status === 'rejected' ? 'ردشده' : 'در انتظار تایید'}</span>
              </div>
              <div class="muted">${esc(s.province)} · ${esc(s.city)} (${esc(s.region)}) · ${esc(storeTypeLabel(s.type))}</div>
              <div class="muted">${esc(s.phone)} · ساعات: ${esc(s.hours)}</div>
              <div class="row" style="margin-top:8px;">
                ${s.status !== 'approved' ? `<button class="btn btn-success btn-sm" data-store-status="${esc(s.id)}:approved">✓ تایید فروشگاه</button>` : ''}
                ${s.status !== 'rejected' ? `<button class="btn btn-gold btn-sm" data-store-status="${esc(s.id)}:rejected">✕ رد</button>` : ''}
                <button class="btn btn-danger btn-sm" data-del-store="${esc(s.id)}">حذف</button>
              </div>
            </div>
          `).join('')}
        </div>

        <div class="section-header-title"><span>🪑 آگهی‌های اجاره میز کار (${toPersianNum((db.deskRentals || []).length)})</span></div>
        <div class="grid">
          ${(db.deskRentals || []).map((d) => `
            <div class="card">
              <div class="row space-between">
                <span class="badge badge-blue">${esc(d.category)}</span>
                <span class="badge ${d.status === 'approved' ? 'badge-green' : d.status === 'rejected' ? 'badge-red' : 'badge-gold'}">${d.status === 'approved' ? 'تاییدشده' : d.status === 'rejected' ? 'ردشده' : 'در انتظار تایید'}</span>
              </div>
              <h4 style="margin:6px 0 4px;">${esc(d.title)}</h4>
              <div class="muted">${esc(d.storeName)} · ${esc(d.city)} (${esc(d.region)})</div>
              <div class="row" style="margin-top:8px;">
                ${d.status !== 'approved' ? `<button class="btn btn-success btn-sm" data-desk-status="${esc(d.id)}:approved">✓ تایید میز</button>` : ''}
                ${d.status !== 'rejected' ? `<button class="btn btn-gold btn-sm" data-desk-status="${esc(d.id)}:rejected">✕ رد</button>` : ''}
                <button class="btn btn-danger btn-sm" data-del-desk-rental="${esc(d.id)}">حذف</button>
              </div>
            </div>
          `).join('')}
        </div>
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
    let prov = state.province;
    let city = state.city;
    const draw = () => {
      const cities = Object.keys(LOCATIONS[prov] || {});
      if (!cities.includes(city)) city = cities[0] || '';
      const regions = LOCATIONS[prov]?.[city] || ['همه مناطق'];
      showModal(`
        <h3 style="margin-top:0;">انتخاب محدوده جغرافیایی</h3>
        <div class="form-group">
          <label class="form-label">استان</label>
          <select id="loc-prov" class="select">
            ${Object.keys(LOCATIONS).map((p) => `<option value="${esc(p)}" ${p === prov ? 'selected' : ''}>${esc(p)}</option>`).join('')}
          </select>
        </div>
        <div class="form-group">
          <label class="form-label">شهر</label>
          <select id="loc-city" class="select">
            ${cities.map((c) => `<option value="${esc(c)}" ${c === city ? 'selected' : ''}>${esc(c)}</option>`).join('')}
          </select>
        </div>
        <div class="form-group">
          <label class="form-label">منطقه</label>
          <select id="loc-reg" class="select">
            ${regions.map((r) => `<option value="${esc(r)}" ${r === state.region ? 'selected' : ''}>${esc(r)}</option>`).join('')}
          </select>
        </div>
        <div class="row space-between">
          <button class="btn btn-outline" onclick="window.__closeModal()">انصراف</button>
          <button id="loc-save" class="btn btn-primary">تایید محدوده</button>
        </div>
      `);
      document.getElementById('loc-prov').onchange = (e) => {
        prov = e.target.value;
        city = Object.keys(LOCATIONS[prov] || {})[0] || '';
        draw();
      };
      document.getElementById('loc-city').onchange = (e) => {
        city = e.target.value;
        draw();
      };
      document.getElementById('loc-save').onclick = () => {
        state.province = document.getElementById('loc-prov').value;
        state.city = document.getElementById('loc-city').value;
        state.region = document.getElementById('loc-reg').value;
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
    const perm = 'Notification' in window ? Notification.permission : 'unsupported';
    showModal(`
      <div class="row space-between" style="margin-bottom:10px;">
        <h3 style="margin:0;">اعلان‌ها و پیام‌های مدیریت (${toPersianNum(visible.length)})</h3>
        ${perm !== 'granted' && perm !== 'unsupported' ? `<button id="modal-req-notif" class="btn btn-gold btn-sm">🔔 فعال‌سازی پس‌زمینه</button>` : ''}
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
    showModal(`
      <div class="row space-between" style="margin-bottom:8px;">
        <h3 style="margin:0;">پروفایل همکار</h3>
        <span class="badge badge-blue">${esc(getUserTier())}</span>
      </div>
      <div class="muted" style="margin-bottom:12px;">نام کاربری (ایمیل): ${esc(state.session.email)}</div>
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
            phone: document.getElementById('prof-phone').value.trim(),
            province: state.province,
            city: state.city,
            region: state.region
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

  function openRegisterStoreModal() {
    let prov = state.province;
    let city = state.city;
    const defaultType = getUserTier() === 'عمده‌فروش موبایل' ? 'mobile'
      : getUserTier() === 'عمده‌فروش جانبی' ? 'acc'
      : getUserTier() === 'قطعه‌فروش' ? 'hw' : 'both';

    const draw = () => {
      const cities = Object.keys(LOCATIONS[prov] || {});
      if (!cities.includes(city)) city = cities[0] || '';
      const regions = LOCATIONS[prov]?.[city] || ['همه مناطق'];
      showModal(`
        <h3 style="margin-top:0;">🏪 ثبت فروشگاه جدید در بازمو</h3>
        <div class="form-group"><input id="st-name" class="input" placeholder="نام فروشگاه / بنکداری" /></div>
        <div class="form-group">
          <label class="form-label">حوزه فعالیت فروشگاه</label>
          <select id="st-type" class="select">
            <option value="mobile" ${defaultType === 'mobile' ? 'selected' : ''}>فروشگاه موبایل</option>
            <option value="acc" ${defaultType === 'acc' ? 'selected' : ''}>فروشگاه لوازم جانبی</option>
            <option value="hw" ${defaultType === 'hw' ? 'selected' : ''}>فروشگاه قطعات سخت‌افزار</option>
            <option value="both" ${defaultType === 'both' ? 'selected' : ''}>موبایل و جانبی</option>
          </select>
        </div>
        <div class="grid" style="grid-template-columns:repeat(3,1fr);gap:8px;margin-bottom:10px;">
          <select id="st-prov" class="select">${Object.keys(LOCATIONS).map((p) => `<option value="${esc(p)}" ${p === prov ? 'selected' : ''}>${esc(p)}</option>`).join('')}</select>
          <select id="st-city" class="select">${cities.map((c) => `<option value="${esc(c)}" ${c === city ? 'selected' : ''}>${esc(c)}</option>`).join('')}</select>
          <select id="st-reg" class="select">${regions.map((r) => `<option value="${esc(r)}">${esc(r)}</option>`).join('')}</select>
        </div>
        <div class="form-group"><input id="st-phone" class="input" dir="ltr" placeholder="تلفن تماس" value="${esc(state.session.user?.phone || '')}" /></div>
        <div class="form-group"><input id="st-addr" class="input" placeholder="آدرس دقیق پاساژ / واحد" /></div>
        <div class="form-group"><input id="st-hours" class="input" placeholder="ساعات کاری (مثلاً ۱۰ تا ۲۰)" value="۱۰ تا ۲۰" /></div>
        <div class="row space-between">
          <button class="btn btn-outline" onclick="window.__closeModal()">انصراف</button>
          <button id="st-save" class="btn btn-primary">ثبت فروشگاه</button>
        </div>
      `);
      document.getElementById('st-prov').onchange = (e) => { prov = e.target.value; city = Object.keys(LOCATIONS[prov] || {})[0] || ''; draw(); };
      document.getElementById('st-city').onchange = (e) => { city = e.target.value; draw(); };
      document.getElementById('st-save').onclick = async () => {
        const name = document.getElementById('st-name').value.trim();
        if (!name) return toast('نام فروشگاه الزامی است.');
        try {
          await api('/api/stores', {
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
          closeModal();
          toast(state.session.isAdmin ? 'فروشگاه ثبت و فعال شد.' : 'فروشگاه ثبت شد و پس از تایید مدیر در پنل «مدیریت فروشگاه من» قابل مدیریت کامل است.');
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
        <div style="max-height:260px;overflow-y:auto;background:#f8fafc;border-radius:12px;padding:10px;margin-bottom:10px;">
          ${msgs.map((m) => `
            <div style="margin-bottom:8px;text-align:${m.senderUid === state.session.uid ? 'left' : 'right'};">
              <span class="badge ${m.senderUid === state.session.uid ? 'badge-blue' : 'badge-gold'}">${esc(m.senderName)}</span>
              <div style="display:inline-block;background:#fff;border:1px solid var(--border);border-radius:10px;padding:6px 10px;margin-top:2px;">${esc(m.text)}</div>
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
    let prov = 'تهران';
    let city = 'تهران';
    const draw = () => {
      const cities = Object.keys(LOCATIONS[prov] || {});
      if (!cities.includes(city)) city = cities[0] || '';
      const regions = LOCATIONS[prov]?.[city] || ['همه مناطق'];
      showModal(`
        <h3 style="margin-top:0;">افزودن / ویرایش کاربر</h3>
        <div class="form-group"><input id="au-email" class="input" type="email" dir="ltr" placeholder="ایمیل کاربر (الزامی)" /></div>
        <div class="form-group"><input id="au-pass" class="input" type="password" dir="ltr" placeholder="رمز عبور ورود (اختیاری - حداقل ۶ کاراکتر)" /></div>
        <div class="form-group"><input id="au-name" class="input" placeholder="نام و نام خانوادگی" /></div>
        <div class="form-group"><input id="au-phone" class="input" dir="ltr" placeholder="شماره تماس" /></div>
        <div class="form-group">
          <select id="au-role" class="select">${ALL_ROLES.map((r) => `<option value="${esc(r)}">${esc(r)}</option>`).join('')}</select>
        </div>
        <div class="grid" style="grid-template-columns:repeat(3,1fr);gap:8px;margin-bottom:10px;">
          <select id="au-m-prov" class="select">${Object.keys(LOCATIONS).map((p) => `<option value="${esc(p)}" ${p === prov ? 'selected' : ''}>${esc(p)}</option>`).join('')}</select>
          <select id="au-m-city" class="select">${cities.map((c) => `<option value="${esc(c)}" ${c === city ? 'selected' : ''}>${esc(c)}</option>`).join('')}</select>
          <select id="au-m-reg" class="select">${regions.map((r) => `<option value="${esc(r)}">${esc(r)}</option>`).join('')}</select>
        </div>
        <div class="row space-between">
          <button class="btn btn-outline" onclick="window.__closeModal()">انصراف</button>
          <button id="au-save" class="btn btn-primary">ذخیره کاربر</button>
        </div>
      `);
      document.getElementById('au-m-prov').onchange = (e) => { prov = e.target.value; city = Object.keys(LOCATIONS[prov] || {})[0] || ''; draw(); };
      document.getElementById('au-m-city').onchange = (e) => { city = e.target.value; draw(); };
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

  function bindSwipeNavigation() {
    const mainEl = document.getElementById('main-swipe-area');
    if (!mainEl) return;
    let startX = 0;
    let startY = 0;
    mainEl.addEventListener('touchstart', (e) => {
      if (!e.changedTouches?.length) return;
      startX = e.changedTouches[0].clientX;
      startY = e.changedTouches[0].clientY;
    }, { passive: true });
    mainEl.addEventListener('touchend', (e) => {
      if (!e.changedTouches?.length) return;
      const dx = e.changedTouches[0].clientX - startX;
      const dy = e.changedTouches[0].clientY - startY;
      if (Math.abs(dx) < 60 || Math.abs(dx) < Math.abs(dy) * 1.4) return;
      const isAdminMode = Boolean(state.session?.isAdmin && state.mode === 'admin');
      if (isAdminMode) {
        const idx = ADMIN_TABS.findIndex((t) => t.id === state.adminTab);
        const nextIdx = dx > 0 ? idx - 1 : idx + 1;
        if (nextIdx >= 0 && nextIdx < ADMIN_TABS.length) {
          state.adminTab = ADMIN_TABS[nextIdx].id;
          render();
        }
      } else {
        const nextTab = dx > 0 ? state.userTab - 1 : state.userTab + 1;
        if (nextTab >= 0 && nextTab <= 3) {
          state.userTab = nextTab;
          render();
        }
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
    let kind = initialKind || (state.deskSubTab === 'requests' ? 'request' : 'rental');
    let prov = state.province;
    let city = state.city;
    const draw = () => {
      const cities = Object.keys(LOCATIONS[prov] || {});
      if (!cities.includes(city)) city = cities[0] || '';
      const regions = LOCATIONS[prov]?.[city] || ['همه مناطق'];
      const isRental = kind === 'rental';
      showModal(`
        <h3 style="margin-top:0;">${isRental ? '🪑 ثبت میز کار برای اجاره' : '🤝 ثبت درخواست یا پیشنهاد میز'}</h3>
        <div class="sub-tabs" style="margin-bottom:10px;">
          <button type="button" class="sub-tab ${isRental ? 'active' : ''}" id="desk-kind-rental">🪑 میز برای اجاره دارم</button>
          <button type="button" class="sub-tab ${!isRental ? 'active' : ''}" id="desk-kind-request">🤝 متقاضی میز / پیشنهاد همکاری</button>
        </div>
        <div class="form-group"><input id="dk-title" class="input" placeholder="${isRental ? 'عنوان (مثلاً اجاره میز تعمیرات برد در علاءالدین)' : 'عنوان (مثلاً تعمیرکار آیفون هستم - دنبال میز درصدی)'}" /></div>
        <div class="form-group">
          <select id="dk-cat" class="select">
            <option value="تعمیرات سخت‌افزار">تعمیرات سخت‌افزار</option>
            <option value="نرم‌افزار و فلش">نرم‌افزار و فلش</option>
            <option value="فروش موبایل">فروش موبایل</option>
            <option value="لوازم جانبی">لوازم جانبی</option>
          </select>
        </div>
        ${isRental ? `<div class="form-group"><input id="dk-store" class="input" placeholder="نام مغازه / پاساژ" /></div>` : ''}
        <div class="grid" style="grid-template-columns:repeat(3,1fr);gap:8px;margin-bottom:10px;">
          <select id="dk-prov" class="select">${Object.keys(LOCATIONS).map((p) => `<option value="${esc(p)}" ${p === prov ? 'selected' : ''}>${esc(p)}</option>`).join('')}</select>
          <select id="dk-city" class="select">${cities.map((c) => `<option value="${esc(c)}" ${c === city ? 'selected' : ''}>${esc(c)}</option>`).join('')}</select>
          <select id="dk-reg" class="select">${regions.map((r) => `<option value="${esc(r)}">${esc(r)}</option>`).join('')}</select>
        </div>
        ${isRental ? `
          <div class="grid" style="grid-template-columns:1fr 1fr;gap:8px;margin-bottom:10px;">
            <input id="dk-daily" class="input" type="number" dir="ltr" placeholder="اجاره روزانه (تومان)" />
            <input id="dk-monthly" class="input" type="number" dir="ltr" placeholder="اجاره ماهانه (تومان)" />
          </div>
          <div class="form-group"><input id="dk-coop" class="input" placeholder="مدل همکاری (مثلاً ماهانه ثابت یا درصدی ۶۰/۴۰)" /></div>
          <div class="form-group"><input id="dk-equip" class="input" placeholder="تجهیزات موجود (لوپ، هیتر، سیستم، ویترین...)" /></div>
          <div class="form-group"><input id="dk-phone" class="input" dir="ltr" placeholder="تلفن تماس" value="${esc(state.session.user?.phone || '')}" /></div>
          <div class="form-group"><textarea id="dk-desc" class="textarea" rows="2" placeholder="شرایط و توضیحات تکمیلی..."></textarea></div>
        ` : `
          <div class="form-group"><input id="dk-budget" class="input" type="number" dir="ltr" placeholder="سقف بودجه ماهانه پیشنهادی (تومان - اختیاری)" /></div>
          <div class="form-group"><input id="dk-phone" class="input" dir="ltr" placeholder="شماره تماس" value="${esc(state.session.user?.phone || '')}" /></div>
          <div class="form-group"><textarea id="dk-prop" class="textarea" rows="3" placeholder="تخصص، تجهیزات همراه و پیشنهاد همکاری برای مغازه‌داران..."></textarea></div>
        `}
        <div class="row space-between">
          <button type="button" class="btn btn-outline" onclick="window.__closeModal()">انصراف</button>
          <button type="button" id="dk-save" class="btn btn-primary">${isRental ? '✓ ثبت میز کار' : '✓ ثبت درخواست همکاری'}</button>
        </div>
      `);
      document.getElementById('desk-kind-rental').onclick = () => { kind = 'rental'; draw(); };
      document.getElementById('desk-kind-request').onclick = () => { kind = 'request'; draw(); };
      document.getElementById('dk-prov').onchange = (e) => { prov = e.target.value; city = Object.keys(LOCATIONS[prov] || {})[0] || ''; draw(); };
      document.getElementById('dk-city').onchange = (e) => { city = e.target.value; draw(); };
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
        <div style="max-height:260px;overflow-y:auto;background:#f8fafc;border-radius:12px;padding:10px;margin-bottom:10px;">
          ${msgs.map((m) => `
            <div style="margin-bottom:8px;text-align:${m.senderUid === state.session.uid ? 'left' : 'right'};">
              <span class="badge ${m.senderUid === state.session.uid ? 'badge-blue' : 'badge-gold'}">${esc(m.senderName)}</span>
              <div style="display:inline-block;background:#fff;border:1px solid var(--border);border-radius:10px;padding:6px 10px;margin-top:2px;">${esc(m.text)}</div>
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

    const activeEl = document.activeElement;
    const preserveFocusIds = new Set(['mob-search', 'sw-search', 'acc-search', 'hw-search', 'forum-search', 'au-search', 'desk-search', 'login-email', 'login-password']);
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

    if (activeId) {
      const nextEl = document.getElementById(activeId);
      if (nextEl && !nextEl.disabled) {
        nextEl.focus({ preventScroll: true });
        if (selStart !== null && nextEl.setSelectionRange) {
          try { nextEl.setSelectionRange(selStart, selEnd ?? selStart); } catch {}
        }
      }
    }
  }

  function bindHeaderEvents() {
    const logoutBtn = document.getElementById('hdr-logout-btn');
    if (logoutBtn) logoutBtn.onclick = () => { logoutLocal(); render(); };

    const modeBtn = document.getElementById('hdr-mode-btn');
    if (modeBtn) modeBtn.onclick = () => { state.mode = state.mode === 'admin' ? 'user' : 'admin'; render(); };

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
        render();
      };
    });

    const qaDeskBtn = document.getElementById('qa-desk-btn');
    if (qaDeskBtn) qaDeskBtn.onclick = () => { state.showDeskRentals = !state.showDeskRentals; state.showMyStore = false; render(); };
    const qaMyStoreBtn = document.getElementById('qa-mystore-btn');
    if (qaMyStoreBtn) qaMyStoreBtn.onclick = () => { state.showMyStore = !state.showMyStore; state.showDeskRentals = false; render(); };

    document.querySelectorAll('[data-atab]').forEach((b) => {
      b.onclick = () => {
        state.adminTab = b.dataset.atab;
        render();
      };
    });

    const activeAdminTab = document.querySelector('#admin-tabs-strip .seg-tab.active');
    if (activeAdminTab && activeAdminTab.scrollIntoView) {
      activeAdminTab.scrollIntoView({ behavior: 'smooth', inline: 'center', block: 'nearest' });
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
    if (msCreate) msCreate.onclick = openRegisterStoreModal;
    const msEmptyCreate = document.getElementById('mystore-empty-create');
    if (msEmptyCreate) msEmptyCreate.onclick = openRegisterStoreModal;
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
    if (mobAddStore) mobAddStore.onclick = openRegisterStoreModal;

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
    if (accAddStore) accAddStore.onclick = openRegisterStoreModal;

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
    if (hwAddStore) hwAddStore.onclick = openRegisterStoreModal;

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
      b.onclick = () => { state.adminTab = b.dataset.gotoAtab; render(); };
    });
    const auSearch = document.getElementById('au-search');
    if (auSearch) auSearch.oninput = (e) => debouncedRenderSearch(() => { state.adminUserSearch = e.target.value; });
    const auTier = document.getElementById('au-tier');
    if (auTier) auTier.onchange = (e) => { state.adminUserTier = e.target.value; render(); };
    const auStatus = document.getElementById('au-status');
    if (auStatus) auStatus.onchange = (e) => { state.adminUserStatus = e.target.value; render(); };
    const auProv = document.getElementById('au-prov');
    if (auProv) auProv.onchange = (e) => { state.adminUserProvince = e.target.value; render(); };
    const auAddBtn = document.getElementById('au-add-btn');
    if (auAddBtn) auAddBtn.onclick = openAdminUserModal;

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
    if (adminAddStore) adminAddStore.onclick = openRegisterStoreModal;
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
