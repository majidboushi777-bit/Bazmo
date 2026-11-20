const SUPER_ADMIN_EMAILS = new Set(
  [
    'majidboushi777@gmail.com',
    'majid.boushi77@gmail.com',
    ...(process.env.ADMIN_EMAILS || '')
      .split(',')
      .map((s) => s.trim().toLowerCase())
      .filter(Boolean)
  ]
);

const GOOGLE_WEB_CLIENT_ID = String(process.env.GOOGLE_WEB_CLIENT_ID || '').trim();

const GOOGLE_WEB_ALLOWED_ORIGINS = (process.env.GOOGLE_WEB_ALLOWED_ORIGINS || '')
  .split(',')
  .map((s) => s.trim().replace(/\/+$/, ''))
  .filter(Boolean);

const DEFAULT_GOOGLE_CLIENT_IDS = [
  '662650837611-9aj50eu39lpkvfv0328rgcba07b6nuu4.apps.googleusercontent.com',
  ...(GOOGLE_WEB_CLIENT_ID ? [GOOGLE_WEB_CLIENT_ID] : []),
  ...(process.env.GOOGLE_CLIENT_ID || '')
    .split(',')
    .map((s) => s.trim())
    .filter(Boolean)
];

const ALL_ROLES = [
  'تعمیرکار',
  'نرم\u200cافزارکار',
  'فروشنده موبایل',
  'فروشنده جانبی',
  'عمده\u200cفروش موبایل',
  'عمده\u200cفروش جانبی',
  'قطعه‌فروش'
];

const STORE_CREATOR_ROLES = new Set([
  'عمده\u200cفروش موبایل',
  'عمده\u200cفروش جانبی',
  'قطعه‌فروش'
]);

function normalizeRoleText(val) {
  return String(val || '')
    .replace(/\u200c/g, '')
    .replace(/ي/g, 'ی')
    .replace(/ك/g, 'ک')
    .replace(/\s+/g, '')
    .trim();
}

function normalizeEmail(email) {
  return String(email || '').trim().toLowerCase();
}

function isValidEmailFormat(email) {
  const norm = normalizeEmail(email);
  return norm.length >= 5 && norm.length <= 200 && /^[^/\s@]+@[^/\s@]+\.[^/\s@]+$/.test(norm);
}

function normalizeTier(tier) {
  if (ALL_ROLES.includes(tier)) return tier;
  const norm = normalizeRoleText(tier);
  const found = ALL_ROLES.find((r) => normalizeRoleText(r) === norm);
  return found || 'تعمیرکار';
}

function defaultStoreTypeForRole(tier) {
  const canon = normalizeTier(tier);
  if (canon === 'عمده\u200cفروش موبایل') return 'mobile';
  if (canon === 'عمده\u200cفروش جانبی') return 'acc';
  if (canon === 'قطعه‌فروش') return 'hw';
  return 'both';
}

function canCreateStore(tier, isAdmin = false) {
  if (isAdmin) return true;
  const norm = normalizeRoleText(tier);
  for (const r of STORE_CREATOR_ROLES) {
    if (normalizeRoleText(r) === norm) return true;
  }
  return false;
}

function canCreateStoreInSection(tier, isAdmin, section) {
  if (isAdmin) return true;
  if (!canCreateStore(tier, false)) return false;
  const canon = normalizeTier(tier);
  const allowed = defaultStoreTypeForRole(canon);
  return allowed === section;
}

function generateUid(prefix = '') {
  const chars = 'abcdefghijklmnopqrstuvwxyz0123456789';
  let suffix = '';
  for (let i = 0; i < 6; i++) {
    suffix += chars[Math.floor(Math.random() * chars.length)];
  }
  return `${prefix}${suffix}`;
}

module.exports = {
  SUPER_ADMIN_EMAILS,
  DEFAULT_GOOGLE_CLIENT_IDS,
  GOOGLE_WEB_CLIENT_ID,
  GOOGLE_WEB_ALLOWED_ORIGINS,
  ALL_ROLES,
  STORE_CREATOR_ROLES,
  normalizeRoleText,
  normalizeEmail,
  isValidEmailFormat,
  normalizeTier,
  defaultStoreTypeForRole,
  canCreateStore,
  canCreateStoreInSection,
  generateUid
};
