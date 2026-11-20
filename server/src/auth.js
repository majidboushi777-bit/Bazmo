const crypto = require('crypto');
const jwt = require('jsonwebtoken');
const { OAuth2Client } = require('google-auth-library');
const { pool, mapUser } = require('./db');
const bundledGoogleCerts = require('./googleCerts.json');
const {
  SUPER_ADMIN_EMAILS,
  DEFAULT_GOOGLE_CLIENT_IDS,
  normalizeEmail,
  normalizeTier
} = require('./constants');

const googleClient = new OAuth2Client();
let cachedJwtSecret = null;
let cachedJwks = Array.isArray(bundledGoogleCerts?.keys) ? [...bundledGoogleCerts.keys] : [];
let lastCertFetchAttemptMs = 0;

async function getJwtSecret() {
  if (process.env.JWT_SECRET && process.env.JWT_SECRET.length >= 16) {
    return process.env.JWT_SECRET;
  }
  if (cachedJwtSecret) return cachedJwtSecret;

  const { rows } = await pool.query("SELECT value FROM app_config WHERE key = 'jwt_secret'");
  if (rows.length > 0 && rows[0].value) {
    cachedJwtSecret = rows[0].value;
    return cachedJwtSecret;
  }

  const generated = crypto.randomBytes(48).toString('hex');
  await pool.query(
    `INSERT INTO app_config (key, value) VALUES ('jwt_secret', $1)
     ON CONFLICT (key) DO NOTHING`,
    [generated]
  );
  const check = await pool.query("SELECT value FROM app_config WHERE key = 'jwt_secret'");
  cachedJwtSecret = check.rows[0].value;
  return cachedJwtSecret;
}

function mergeJwks(existing, incoming) {
  const byKid = new Map();
  for (const k of existing || []) {
    if (k && k.kid) byKid.set(k.kid, k);
  }
  for (const k of incoming || []) {
    if (k && k.kid) byKid.set(k.kid, k);
  }
  return Array.from(byKid.values());
}

async function loadCachedJwksFromDb() {
  try {
    const { rows } = await pool.query("SELECT value FROM app_config WHERE key = 'google_jwks_cache'");
    if (rows.length > 0 && rows[0].value) {
      const parsed = JSON.parse(rows[0].value);
      if (Array.isArray(parsed)) {
        cachedJwks = mergeJwks(cachedJwks, parsed);
      }
    }
  } catch {
    // ignore cache read failures (e.g. unit tests)
  }
  return cachedJwks;
}

async function saveCachedJwksToDb(keys) {
  try {
    await pool.query(
      `INSERT INTO app_config (key, value) VALUES ('google_jwks_cache', $1)
       ON CONFLICT (key) DO UPDATE SET value = EXCLUDED.value`,
      [JSON.stringify(keys)]
    );
  } catch {
    // ignore cache write failures
  }
}

async function refreshGoogleJwksIfPossible(force = false) {
  const now = Date.now();
  if (!force && now - lastCertFetchAttemptMs < 5 * 60 * 1000) {
    return cachedJwks;
  }
  lastCertFetchAttemptMs = now;

  const endpoints = [
    process.env.GOOGLE_CERTS_URL,
    'https://www.googleapis.com/oauth2/v3/certs'
  ].filter(Boolean);

  for (const url of endpoints) {
    try {
      const controller = new AbortController();
      const timer = setTimeout(() => controller.abort(), 5000);
      const res = await fetch(url, { signal: controller.signal });
      clearTimeout(timer);
      if (!res.ok) continue;
      const data = await res.json();
      if (Array.isArray(data?.keys) && data.keys.length > 0) {
        cachedJwks = mergeJwks(cachedJwks, data.keys);
        await saveCachedJwksToDb(cachedJwks);
        return cachedJwks;
      }
    } catch {
      // ignore blocked/timeout endpoint and try next/fallback
    }
  }
  return loadCachedJwksFromDb();
}

function verifyIdTokenAgainstJwks(idToken, jwks, allowedAudiences = DEFAULT_GOOGLE_CLIENT_IDS) {
  const decoded = jwt.decode(idToken, { complete: true });
  const kid = decoded?.header?.kid;
  if (!kid) {
    throw new Error('توکن گوگل فاقد شناسه کلید (kid) است.');
  }
  const jwk = (jwks || []).find((k) => k && k.kid === kid);
  if (!jwk) {
    return null;
  }
  const publicKey = crypto.createPublicKey({ key: jwk, format: 'jwk' });
  const payload = jwt.verify(idToken, publicKey, {
    algorithms: ['RS256'],
    audience: allowedAudiences,
    issuer: ['accounts.google.com', 'https://accounts.google.com'],
    clockTolerance: 120
  });
  return payload;
}

async function verifyGoogleIdToken(idToken, customJwks = null) {
  let payload = null;

  // 1) Try local JWK verification first if matching key is already cached/bundled
  const currentKeys = customJwks || (await loadCachedJwksFromDb());
  try {
    payload = verifyIdTokenAgainstJwks(idToken, currentKeys);
  } catch (err) {
    if (err?.name === 'TokenExpiredError') {
      throw new Error('توکن ورود گوگل منقضی شده است. لطفاً دوباره وارد شوید.');
    }
    if (err?.name === 'JsonWebTokenError') {
      throw new Error(`توکن گوگل معتبر نیست: ${err.message}`);
    }
    throw err;
  }

  // 2) If kid wasn't in bundled/cached keys, refresh certs (GOOGLE_CERTS_URL or googleapis)
  if (!payload && !customJwks) {
    const refreshed = await refreshGoogleJwksIfPossible(true);
    payload = verifyIdTokenAgainstJwks(idToken, refreshed);
  }

  // 3) Final fallback via google-auth-library if Google certs URL wasn't JWK-formatted
  if (!payload && !customJwks) {
    try {
      const ticket = await googleClient.verifyIdToken({
        idToken,
        audience: DEFAULT_GOOGLE_CLIENT_IDS
      });
      payload = ticket.getPayload();
    } catch {
      throw new Error(
        'امکان دریافت کلیدهای عمومی گوگل از سرور وبلاینز وجود ندارد (محدودیت IP گوگل روی سرور). لطفاً متغیر GOOGLE_CERTS_URL را تنظیم کنید یا با پشتیبانی هماهنگ کنید.'
      );
    }
  }

  if (!payload || !payload.sub || !payload.email) {
    throw new Error('اطلاعات توکن گوگل ناقص است.');
  }
  return {
    uid: String(payload.sub),
    email: normalizeEmail(payload.email),
    emailVerified: Boolean(payload.email_verified),
    displayName: String(payload.name || payload.email.split('@')[0] || 'همکار بازمو')
  };
}

function isSuperAdminEmail(email, emailVerified) {
  return Boolean(emailVerified) && SUPER_ADMIN_EMAILS.has(normalizeEmail(email));
}

async function ensureUserProfile({ uid, email, displayName, emailVerified }) {
  const normEmail = normalizeEmail(email);
  const isAdmin = isSuperAdminEmail(normEmail, emailVerified);

  const existingById = await pool.query('SELECT * FROM users WHERE id = $1 LIMIT 1', [uid]);
  if (existingById.rows.length > 0) {
    const user = mapUser(existingById.rows[0]);
    if (!user.active && !isAdmin) {
      return { isAdmin, status: 'blocked', user: null };
    }
    return { isAdmin, status: 'ready', user };
  }

  if (isAdmin) {
    const cleanName = (displayName || 'مدیر بازمو').trim().slice(0, 120) || 'مدیر بازمو';
    const existingAdminByEmail = await pool.query('SELECT id, password_hash FROM users WHERE LOWER(email) = $1 LIMIT 1', [
      normEmail
    ]);
    const prevHash = existingAdminByEmail.rows[0]?.password_hash || '';
    await pool.query(
      `INSERT INTO users (id, uid, email, full_name, phone, tier, province, city, region, active, created_date_label, password_hash)
       VALUES ($1,$2,$3,$4,'','ادمین','تهران','تهران','علاءالدین و جمهوری',TRUE,'امروز',$5)
       ON CONFLICT (id) DO UPDATE SET
         email = EXCLUDED.email,
         tier = 'ادمین',
         active = TRUE,
         password_hash = CASE WHEN users.password_hash <> '' THEN users.password_hash ELSE EXCLUDED.password_hash END,
         updated_at = NOW()`,
      [uid, uid, normEmail, cleanName, prevHash]
    );
    if (existingAdminByEmail.rows[0]?.id && existingAdminByEmail.rows[0].id !== uid) {
      await pool.query('DELETE FROM users WHERE id = $1', [existingAdminByEmail.rows[0].id]);
    }
    const { rows } = await pool.query('SELECT * FROM users WHERE id = $1', [uid]);
    return { isAdmin: true, status: 'ready', user: mapUser(rows[0]) };
  }

  if (!normEmail || !emailVerified) {
    return { isAdmin: false, status: 'not_invited', user: null };
  }

  const inviteRes = await pool.query('SELECT * FROM user_invites WHERE email = $1 LIMIT 1', [normEmail]);
  if (inviteRes.rows.length === 0) {
    const emailUserRes = await pool.query('SELECT * FROM users WHERE LOWER(email) = $1 LIMIT 1', [normEmail]);
    if (emailUserRes.rows.length === 0) {
      return { isAdmin: false, status: 'not_invited', user: null };
    }
    const existingRow = emailUserRes.rows[0];
    const existingEmailUser = mapUser(existingRow);
    if (!existingEmailUser.active) {
      return { isAdmin: false, status: 'blocked', user: null };
    }
    const claimedUser = {
      ...existingEmailUser,
      id: uid,
      uid,
      tier: normalizeTier(existingEmailUser.tier)
    };
    await pool.query(
      `INSERT INTO users (id, uid, email, full_name, phone, tier, province, city, region, active, created_date_label, password_hash)
       VALUES ($1,$2,$3,$4,$5,$6,$7,$8,$9,TRUE,$10,$11)`,
      [
        uid,
        uid,
        normEmail,
        claimedUser.fullName || displayName || 'همکار بازمو',
        claimedUser.phone,
        claimedUser.tier,
        claimedUser.province || 'تهران',
        claimedUser.city || 'تهران',
        claimedUser.region || 'همه مناطق',
        claimedUser.createdDateLabel || 'امروز',
        existingRow.password_hash || ''
      ]
    );
    if (existingEmailUser.id && existingEmailUser.id !== uid) {
      await pool.query('DELETE FROM users WHERE id = $1', [existingEmailUser.id]);
    }
    const { rows } = await pool.query('SELECT * FROM users WHERE id = $1', [uid]);
    return { isAdmin: false, status: 'ready', user: mapUser(rows[0]) };
  }

  const invite = inviteRes.rows[0];
  if (!invite.active) {
    return { isAdmin: false, status: 'blocked', user: null };
  }

  const existingEmailRow = await pool.query('SELECT password_hash FROM users WHERE LOWER(email) = $1 AND password_hash <> \'\' LIMIT 1', [
    normEmail
  ]);
  const preservedHash = existingEmailRow.rows[0]?.password_hash || '';

  const assignedTier = normalizeTier(invite.tier);
  const fullName = String(invite.full_name || displayName || 'همکار بازمو').trim().slice(0, 120);
  await pool.query(
    `INSERT INTO users (id, uid, email, full_name, phone, tier, province, city, region, active, created_date_label, password_hash)
     VALUES ($1,$2,$3,$4,$5,$6,$7,$8,$9,TRUE,$10,$11)
     ON CONFLICT (id) DO UPDATE SET
       tier = EXCLUDED.tier,
       active = TRUE,
       password_hash = CASE WHEN users.password_hash <> '' THEN users.password_hash ELSE EXCLUDED.password_hash END,
       updated_at = NOW()`,
    [
      uid,
      uid,
      normEmail,
      fullName,
      String(invite.phone || '').slice(0, 40),
      assignedTier,
      String(invite.province || 'تهران').slice(0, 80),
      String(invite.city || 'تهران').slice(0, 80),
      String(invite.region || 'همه مناطق').slice(0, 80),
      String(invite.created_date_label || 'امروز').slice(0, 40),
      preservedHash
    ]
  );

  await pool.query(
    `UPDATE user_invites SET claimed_by_uid = $1, updated_at = NOW() WHERE email = $2`,
    [uid, normEmail]
  );

  if (invite.source_user_doc_id && invite.source_user_doc_id !== uid) {
    await pool.query('DELETE FROM users WHERE id = $1', [invite.source_user_doc_id]);
  }
  await pool.query("DELETE FROM users WHERE LOWER(email) = $1 AND id <> $2 AND (id LIKE 'u_%' OR id LIKE 'web_%')", [
    normEmail,
    uid
  ]);

  const { rows } = await pool.query('SELECT * FROM users WHERE id = $1', [uid]);
  return { isAdmin: false, status: 'ready', user: mapUser(rows[0]) };
}

async function signSessionToken({ uid, email, displayName, emailVerified }) {
  const secret = await getJwtSecret();
  return jwt.sign(
    { uid, email: normalizeEmail(email), displayName, emailVerified: Boolean(emailVerified) },
    secret,
    { expiresIn: '180d' }
  );
}

async function authenticateMiddleware(req, res, next) {
  const header = req.headers.authorization || '';
  const queryToken = typeof req.query?.token === 'string' ? req.query.token.trim() : '';
  const token = header.startsWith('Bearer ') ? header.slice(7).trim() : queryToken;
  if (!token) {
    return res.status(401).json({ error: 'لطفاً ابتدا وارد حساب کاربری شوید.' });
  }
  try {
    const secret = await getJwtSecret();
    const decoded = jwt.verify(token, secret);
    req.auth = {
      uid: String(decoded.uid),
      email: normalizeEmail(decoded.email),
      displayName: String(decoded.displayName || ''),
      emailVerified: Boolean(decoded.emailVerified)
    };
    return next();
  } catch {
    return res.status(401).json({ error: 'نشست کاربری منقضی شده است. لطفاً دوباره وارد شوید.' });
  }
}

async function resolveLiveRole(req, res, next) {
  try {
    const isAdmin = isSuperAdminEmail(req.auth.email, req.auth.emailVerified);
    const { rows } = await pool.query('SELECT * FROM users WHERE id = $1 LIMIT 1', [req.auth.uid]);
    const liveUser = rows[0] ? mapUser(rows[0]) : null;
    if (!isAdmin && (!liveUser || !liveUser.active)) {
      return res.status(403).json({ error: 'دسترسی به این بخش مجاز نیست یا حساب شما مسدود شده است.' });
    }
    req.isAdmin = isAdmin;
    req.liveUser = liveUser;
    req.userTier = isAdmin ? 'ادمین' : liveUser.tier;
    return next();
  } catch (err) {
    return next(err);
  }
}

function hashPassword(password) {
  const salt = crypto.randomBytes(16).toString('hex');
  const derived = crypto.scryptSync(String(password), salt, 64).toString('hex');
  return `${salt}:${derived}`;
}

function hashPasswordAsync(password) {
  return new Promise((resolve, reject) => {
    const salt = crypto.randomBytes(16).toString('hex');
    crypto.scrypt(String(password), salt, 64, (err, derived) => {
      if (err) return reject(err);
      return resolve(`${salt}:${derived.toString('hex')}`);
    });
  });
}

function verifyPassword(password, storedHash) {
  if (!storedHash || !storedHash.includes(':')) return false;
  const [salt, expectedHex] = storedHash.split(':');
  if (!salt || !expectedHex) return false;
  try {
    const derived = crypto.scryptSync(String(password), salt, 64);
    const expected = Buffer.from(expectedHex, 'hex');
    if (derived.length !== expected.length) return false;
    return crypto.timingSafeEqual(derived, expected);
  } catch {
    return false;
  }
}

function verifyPasswordAsync(password, storedHash) {
  if (!storedHash || !storedHash.includes(':')) return Promise.resolve(false);
  const [salt, expectedHex] = storedHash.split(':');
  if (!salt || !expectedHex) return Promise.resolve(false);
  return new Promise((resolve) => {
    crypto.scrypt(String(password), salt, 64, (err, derived) => {
      if (err) return resolve(false);
      try {
        const expected = Buffer.from(expectedHex, 'hex');
        if (derived.length !== expected.length) return resolve(false);
        return resolve(crypto.timingSafeEqual(derived, expected));
      } catch {
        return resolve(false);
      }
    });
  });
}

async function authenticateWithPassword(emailInput, passwordInput) {
  const normEmail = normalizeEmail(emailInput);
  const password = String(passwordInput || '');
  if (!normEmail || !password) {
    throw new Error('ایمیل و رمز عبور الزامی است.');
  }

  const isAdmin = SUPER_ADMIN_EMAILS.has(normEmail);
  const envAdminPass = String(process.env.ADMIN_PASSWORD || '');

  const { rows } = await pool.query('SELECT * FROM users WHERE LOWER(email) = $1 LIMIT 1', [normEmail]);
  const existingRow = rows[0] || null;

  let authenticated = false;
  if (existingRow?.password_hash && (await verifyPasswordAsync(password, existingRow.password_hash))) {
    authenticated = true;
  } else if (isAdmin && envAdminPass && password.length === envAdminPass.length) {
    authenticated = crypto.timingSafeEqual(Buffer.from(password), Buffer.from(envAdminPass));
  }

  if (!authenticated) {
    throw new Error('ایمیل یا رمز عبور اشتباه است، یا رمز عبور وب برای این حساب تنظیم نشده است.');
  }

  const uid = existingRow?.id || `web_${crypto.createHash('sha256').update(normEmail).digest('hex').slice(0, 16)}`;
  const displayName = existingRow?.full_name || (isAdmin ? 'مدیر بازمو' : normEmail.split('@')[0]);

  return {
    uid,
    email: normEmail,
    emailVerified: true,
    displayName
  };
}

function requireAdmin(req, res, next) {
  if (!req.isAdmin) {
    return res.status(403).json({ error: 'دسترسی فقط برای مدیر کل مجاز است.' });
  }
  return next();
}

module.exports = {
  verifyGoogleIdToken,
  verifyIdTokenAgainstJwks,
  refreshGoogleJwksIfPossible,
  ensureUserProfile,
  signSessionToken,
  authenticateMiddleware,
  resolveLiveRole,
  requireAdmin,
  isSuperAdminEmail,
  hashPassword,
  hashPasswordAsync,
  verifyPassword,
  verifyPasswordAsync,
  authenticateWithPassword
};
