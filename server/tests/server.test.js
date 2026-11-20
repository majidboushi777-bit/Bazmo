const test = require('node:test');
const assert = require('node:assert/strict');

process.env.DATABASE_URL = process.env.DATABASE_URL || 'postgres://test:test@localhost:5432/bazmo_test';

const crypto = require('node:crypto');
const jwt = require('jsonwebtoken');

const {
  ALL_ROLES,
  STORE_CREATOR_ROLES,
  DEFAULT_GOOGLE_CLIENT_IDS,
  normalizeEmail,
  isValidEmailFormat,
  normalizeTier
} = require('../src/constants');
const {
  canAccessForumBoard,
  buildStateEtag,
  normalizeBannerLocation,
  buildBannerId
} = require('../src/routes');
const {
  verifyGoogleIdToken,
  isSuperAdminEmail,
  hashPassword,
  verifyPassword
} = require('../src/auth');
const { createApp } = require('../src/index');
const { BOOTSTRAP_ADMIN_EMAIL, BOOTSTRAP_ADMIN_HASH } = require('../src/seed');

test('constants enforce 7-role RBAC rules with exact Persian strings', () => {
  assert.equal(ALL_ROLES.length, 7);
  assert.equal(ALL_ROLES.includes('سخت\u200cافزارکار'), false);
  assert.equal(STORE_CREATOR_ROLES.has('عمده\u200cفروش موبایل'), true);
  assert.equal(STORE_CREATOR_ROLES.has('عمده\u200cفروش جانبی'), true);
  assert.equal(STORE_CREATOR_ROLES.has('قطعه‌فروش'), true);
  assert.equal(STORE_CREATOR_ROLES.has('تعمیرکار'), false);

  assert.equal(normalizeTier('invalid_role'), 'تعمیرکار');
  assert.equal(normalizeTier('نرم\u200cافزارکار'), 'نرم\u200cافزارکار');
});

test('email normalization and validation handle whitespace and case', () => {
  assert.equal(normalizeEmail('  Ali.Rezaei@Gmail.COM '), 'ali.rezaei@gmail.com');
  assert.equal(isValidEmailFormat('  Ali.Rezaei@Gmail.COM '), true);
  assert.equal(isValidEmailFormat('bad-email'), false);
});

test('forum board RBAC allows admins and strict matching roles only', () => {
  assert.equal(canAccessForumBoard('تعمیرکار', false, 'hardware'), true);
  assert.equal(canAccessForumBoard('نرم\u200cافزارکار', false, 'hardware'), false);
  assert.equal(canAccessForumBoard('نرم\u200cافزارکار', false, 'software'), true);
  assert.equal(canAccessForumBoard('تعمیرکار', false, 'software'), false);
  assert.equal(canAccessForumBoard('فروشنده موبایل', true, 'hardware'), true);
  assert.equal(canAccessForumBoard('فروشنده موبایل', true, 'software'), true);
});

test('verifyGoogleIdToken verifies RS256 Google ID tokens via local/cached JWKs and recognizes majid.boushi77@gmail.com as super admin', async () => {
  const { publicKey, privateKey } = crypto.generateKeyPairSync('rsa', { modulusLength: 2048 });
  const jwk = publicKey.export({ format: 'jwk' });
  jwk.kid = 'test-google-kid-1';
  jwk.alg = 'RS256';
  jwk.use = 'sig';

  const signedIdToken = jwt.sign(
    {
      sub: 'google-sub-123',
      email: 'Majid.Boushi77@gmail.com',
      email_verified: true,
      name: 'مجید بوشهری'
    },
    privateKey,
    {
      algorithm: 'RS256',
      keyid: 'test-google-kid-1',
      audience: DEFAULT_GOOGLE_CLIENT_IDS[0],
      issuer: 'https://accounts.google.com',
      expiresIn: '1h'
    }
  );

  const identity = await verifyGoogleIdToken(signedIdToken, [jwk]);
  assert.equal(identity.uid, 'google-sub-123');
  assert.equal(identity.email, 'majid.boushi77@gmail.com');
  assert.equal(identity.emailVerified, true);
  assert.equal(isSuperAdminEmail(identity.email, identity.emailVerified), true);
});

test('hashPassword and verifyPassword support salted scrypt password verification', () => {
  const stored = hashPassword('BazmoWeb#2026');
  assert.equal(stored.includes(':'), true);
  assert.equal(verifyPassword('BazmoWeb#2026', stored), true);
  assert.equal(verifyPassword('WrongPass', stored), false);
  assert.equal(verifyPassword('BazmoWeb#2026', ''), false);
  assert.equal(verifyPassword('BazmoWeb#2026', 'malformed'), false);
});

test('createApp serves Web UI at / and public Google Client ID config at /api/config/public', async () => {
  const app = createApp(() => {});
  const server = app.listen(0, '127.0.0.1');
  await new Promise((resolve, reject) => {
    server.once('listening', resolve);
    server.once('error', reject);
  });
  try {
    const addr = server.address();
    const baseUrl = `http://127.0.0.1:${addr.port}`;

    const homeRes = await fetch(`${baseUrl}/`);
    assert.equal(homeRes.status, 200);
    const html = await homeRes.text();
    assert.equal(html.includes('بازمو'), true);

    const cfgRes = await fetch(`${baseUrl}/api/config/public`);
    assert.equal(cfgRes.status, 200);
    const cfg = await cfgRes.json();
    assert.equal(cfg.googleClientId, DEFAULT_GOOGLE_CLIENT_IDS[0]);
    assert.equal(Array.isArray(cfg.allowedGoogleWebOrigins), true);

    const swRes = await fetch(`${baseUrl}/sw.js`);
    assert.equal(swRes.status, 200);
    const swText = await swRes.text();
    assert.equal(swText.includes('notificationclick'), true);
  } finally {
    await new Promise((resolve) => server.close(resolve));
  }
});

test('bootstrap super admin email and salted password hash match expected credentials', () => {
  assert.equal(BOOTSTRAP_ADMIN_EMAIL, 'majid.boushi77@gmail.com');
  assert.equal(verifyPassword('66Gf65B22@', BOOTSTRAP_ADMIN_HASH), true);
  assert.equal(verifyPassword('wrong-password', BOOTSTRAP_ADMIN_HASH), false);
});

test('buildStateEtag always generates pure ASCII HTTP header values across all Persian roles', () => {
  const seen = new Set();
  for (const role of ALL_ROLES) {
    const etag = buildStateEtag(42, false, role, true, 'u_123');
    assert.match(etag, /^"[\x21-\x7e]+"$/);
    seen.add(etag);
  }
  assert.equal(seen.size, ALL_ROLES.length);
  const adminEtag = buildStateEtag(42, true, 'تعمیرکار', true, 'u_123');
  assert.match(adminEtag, /^"[\x21-\x7e]+"$/);
  assert.notEqual(adminEtag, buildStateEtag(42, false, 'تعمیرکار', true, 'u_123'));
  assert.notEqual(
    buildStateEtag(42, false, 'تعمیرکار', true, 'u_123'),
    buildStateEtag(43, false, 'تعمیرکار', true, 'u_123')
  );
});

test('normalizeBannerLocation and buildBannerId scope nationwide vs province/city/region uniquely', () => {
  const nationwide = normalizeBannerLocation('کل کشور', 'تهران', 'علاءالدین و جمهوری');
  assert.deepEqual(nationwide, {
    province: 'همه استان‌ها',
    city: 'همه شهرها',
    region: 'همه مناطق'
  });
  assert.equal(buildBannerId('mobile', 0, nationwide), 'bn_mobile_0');

  const provScope = normalizeBannerLocation('تهران', 'همه شهرهای استان', 'علاءالدین');
  assert.deepEqual(provScope, {
    province: 'تهران',
    city: 'همه شهرها',
    region: 'همه مناطق'
  });

  const cityScope = normalizeBannerLocation('تهران', 'تهران', 'همه مناطق');
  const regScope = normalizeBannerLocation('تهران', 'تهران', 'علاءالدین و جمهوری');
  const regScopeNoZwnj = normalizeBannerLocation('تهران', 'تهران', 'علاءالدین و جمهوری');

  const provId = buildBannerId('mobile', 0, provScope);
  const cityId = buildBannerId('mobile', 0, cityScope);
  const regId = buildBannerId('mobile', 0, regScope);
  assert.match(provId, /^bn_mobile_0_[0-9a-f]{10}$/);
  assert.match(cityId, /^bn_mobile_0_[0-9a-f]{10}$/);
  assert.match(regId, /^bn_mobile_0_[0-9a-f]{10}$/);
  assert.notEqual(provId, cityId);
  assert.notEqual(cityId, regId);
  assert.equal(regId, buildBannerId('mobile', 0, regScopeNoZwnj));
});
