const express = require('express');
const db = require('./db');
const {
  verifyGoogleIdToken,
  ensureUserProfile,
  signSessionToken,
  authenticateMiddleware,
  resolveLiveRole,
  requireAdmin,
  hashPasswordAsync,
  authenticateWithPassword
} = require('./auth');
const { seedIfEmpty, resetAllToDefaults, writeDemoDataset, setDemoVisibility, getDemoVisibility } = require('./seed');
const {
  DEFAULT_GOOGLE_CLIENT_IDS,
  GOOGLE_WEB_CLIENT_ID,
  GOOGLE_WEB_ALLOWED_ORIGINS,
  STORE_CREATOR_ROLES,
  normalizeEmail,
  isValidEmailFormat,
  normalizeTier,
  generateUid
} = require('./constants');

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

function canAccessForumBoard(userTier, isAdmin, board) {
  if (isAdmin) return true;
  if (board === 'hardware') return userTier === 'تعمیرکار';
  if (board === 'software') return userTier === 'نرم\u200cافزارکار';
  return false;
}

function createApiRouter(broadcastSync = () => {}, sseClients = new Set(), getStateVersion = () => 1) {
  const router = express.Router();

  async function verifyStoreOwner(req, storeId, requireApproved = true) {
    if (req.isAdmin) return { ok: true, store: null };
    if (!storeId) return { ok: false, error: 'فروشگاه مشخص نشده است.' };
    const { rows } = await db.pool.query('SELECT * FROM stores WHERE id = $1 LIMIT 1', [String(storeId)]);
    if (rows.length === 0) return { ok: false, error: 'فروشگاه یافت نشد.' };
    const st = rows[0];
    if (st.owner_id !== req.auth.uid) return { ok: false, error: 'شما مالک این فروشگاه نیستید.' };
    if (requireApproved && st.status !== 'approved') {
      return { ok: false, error: 'فروشگاه شما هنوز توسط مدیریت تایید نشده است.' };
    }
    return { ok: true, store: st };
  }

  // ==================== AUTH & REGISTRATION ====================

  router.post('/auth/google', async (req, res, next) => {
    try {
      const idToken = String(req.body?.idToken || '').trim();
      if (!idToken) {
        return res.status(400).json({ error: 'توکن گوگل ارسال نشده است.' });
      }
      const identity = await verifyGoogleIdToken(idToken);
      const profileStatus = await ensureUserProfile(identity);
      const token = await signSessionToken(identity);
      if (profileStatus.isAdmin) {
        await seedIfEmpty(identity.uid);
      }
      return res.json({
        token,
        uid: identity.uid,
        email: identity.email,
        displayName: identity.displayName,
        emailVerified: identity.emailVerified,
        isAdmin: profileStatus.isAdmin,
        status: profileStatus.status,
        user: profileStatus.user
      });
    } catch (err) {
      const msg = String(err?.message || '');
      if (
        msg.includes('توکن') ||
        msg.includes('Wrong number of segments') ||
        msg.includes('Token used too late') ||
        msg.includes('Wrong recipient') ||
        msg.includes('Invalid token')
      ) {
        return res.status(401).json({ error: msg || 'توکن گوگل معتبر نیست. لطفاً دوباره وارد شوید.' });
      }
      return next(err);
    }
  });

  router.get('/config/public', (_req, res) => {
    return res.json({
      googleClientId: GOOGLE_WEB_CLIENT_ID || DEFAULT_GOOGLE_CLIENT_IDS[0],
      googleWebSignInEnabled: process.env.ENABLE_GOOGLE_WEB_SIGNIN === 'true',
      allowedGoogleWebOrigins: GOOGLE_WEB_ALLOWED_ORIGINS,
      hasAdminPasswordLogin: true
    });
  });

  router.post('/auth/login', async (req, res, next) => {
    try {
      const identity = await authenticateWithPassword(req.body?.email, req.body?.password);
      const profileStatus = await ensureUserProfile(identity);
      const token = await signSessionToken(identity);
      if (profileStatus.isAdmin) {
        await seedIfEmpty(identity.uid);
      }
      return res.json({
        token,
        uid: identity.uid,
        email: identity.email,
        displayName: identity.displayName,
        emailVerified: identity.emailVerified,
        isAdmin: profileStatus.isAdmin,
        status: profileStatus.status,
        user: profileStatus.user
      });
    } catch (err) {
      return res.status(401).json({ error: err?.message || 'ورود ناموفق بود.' });
    }
  });

  router.get('/auth/session', authenticateMiddleware, async (req, res, next) => {
    try {
      const profileStatus = await ensureUserProfile(req.auth);
      if (profileStatus.isAdmin) {
        await seedIfEmpty(req.auth.uid);
      }
      const refreshedToken = await signSessionToken(req.auth);
      return res.json({
        token: refreshedToken,
        uid: req.auth.uid,
        email: req.auth.email,
        displayName: req.auth.displayName,
        emailVerified: req.auth.emailVerified,
        isAdmin: profileStatus.isAdmin,
        status: profileStatus.status,
        user: profileStatus.user
      });
    } catch (err) {
      return next(err);
    }
  });

  router.post('/registration-requests', async (req, res, next) => {
    try {
      const email = normalizeEmail(req.body?.email).slice(0, 200);
      const fullName = String(req.body?.fullName || '').trim().slice(0, 120);
      const phone = String(req.body?.phone || '').trim().slice(0, 40);
      const requestedTier = normalizeTier(req.body?.requestedTier);
      const province = String(req.body?.province || 'تهران').trim().slice(0, 80) || 'تهران';
      const city = String(req.body?.city || 'تهران').trim().slice(0, 80) || 'تهران';
      const region = String(req.body?.region || 'همه مناطق').trim().slice(0, 80) || 'همه مناطق';
      const note = String(req.body?.note || '').trim().slice(0, 400);

      if (!fullName) {
        return res.status(400).json({ error: 'لطفاً نام و نام خانوادگی خود را وارد کنید.' });
      }
      if (!isValidEmailFormat(email)) {
        return res.status(400).json({ error: 'لطفاً یک ایمیل معتبر وارد کنید.' });
      }
      if (phone.length < 5) {
        return res.status(400).json({ error: 'شماره تماس معتبر برای احراز هویت الزامی است.' });
      }

      const id = String(req.body?.id || '').trim() || generateUid('reg_');
      const rawPassword = String(req.body?.password || '');
      if (rawPassword && (rawPassword.length < 6 || rawPassword.length > 128)) {
        return res.status(400).json({ error: 'رمز عبور انتخابی باید بین ۶ تا ۱۲۸ کاراکتر باشد.' });
      }
      const passwordHash = rawPassword ? await hashPasswordAsync(rawPassword) : '';

      await db.pool.query(
        `INSERT INTO registration_requests (id, email, full_name, phone, requested_tier, province, city, region, note, status, password_hash)
         VALUES ($1,$2,$3,$4,$5,$6,$7,$8,$9,'pending',$10)
         ON CONFLICT (id) DO UPDATE SET
           email = EXCLUDED.email,
           full_name = EXCLUDED.full_name,
           phone = EXCLUDED.phone,
           requested_tier = EXCLUDED.requested_tier,
           province = EXCLUDED.province,
           city = EXCLUDED.city,
           region = EXCLUDED.region,
           note = EXCLUDED.note,
           password_hash = CASE WHEN EXCLUDED.password_hash <> '' THEN EXCLUDED.password_hash ELSE registration_requests.password_hash END,
           status = 'pending',
           updated_at = NOW()`,
        [id, email, fullName, phone, requestedTier, province, city, region, note, passwordHash]
      );

      broadcastSync(['registration_requests']);
      return res.status(201).json({ ok: true, id });
    } catch (err) {
      return next(err);
    }
  });

  // ==================== AUTHENTICATED STATE ====================

  router.use(authenticateMiddleware, resolveLiveRole);

  router.get('/events', (req, res) => {
    res.setHeader('Content-Type', 'text/event-stream; charset=utf-8');
    res.setHeader('Cache-Control', 'no-cache, no-transform');
    res.setHeader('Connection', 'keep-alive');
    res.setHeader('X-Accel-Buffering', 'no');
    if (typeof res.flushHeaders === 'function') res.flushHeaders();
    res.write(`event: connected\ndata: ${JSON.stringify({ type: 'connected', ts: Date.now() })}\n\n`);
    sseClients.add(res);
    req.on('close', () => {
      sseClients.delete(res);
    });
  });

  router.get('/state', async (req, res, next) => {
    try {
      const uid = req.auth.uid;
      const isAdmin = req.isAdmin;
      if (isAdmin) {
        await seedIfEmpty(uid);
      }

      const showDemoData = await getDemoVisibility();
      const etag = `"v${getStateVersion()}-${isAdmin ? 'A' : normalizeZwnj(req.userTier)}-${showDemoData ? '1' : '0'}-${uid}"`;
      if (req.headers['if-none-match'] === etag) {
        res.setHeader('ETag', etag);
        res.setHeader('Cache-Control', 'no-cache');
        return res.status(304).end();
      }

      const demoClause = showDemoData ? '' : 'AND is_demo = FALSE';
      const demoWhere = showDemoData ? '' : 'WHERE is_demo = FALSE';

      let ownerStoreNames = [];
      if (!isAdmin) {
        const ownStoresRes = await db.pool.query(
          `SELECT name FROM stores WHERE owner_id = $1 AND status = 'approved' ${demoClause}`,
          [uid]
        );
        ownerStoreNames = ownStoresRes.rows.map((r) => r.name).filter(Boolean);
      }

      const queries = {
        users: isAdmin
          ? db.pool.query('SELECT * FROM users ORDER BY full_name ASC')
          : db.pool.query('SELECT * FROM users WHERE id = $1', [uid]),
        registrationRequests: isAdmin
          ? db.pool.query("SELECT * FROM registration_requests ORDER BY (status <> 'pending') ASC, created_at DESC")
          : Promise.resolve({ rows: [] }),
        notifications: isAdmin
          ? db.pool.query(`SELECT * FROM notifications ${demoWhere} ORDER BY sort_order DESC`)
          : db.pool.query(`SELECT * FROM notifications WHERE published = TRUE ${demoClause} ORDER BY sort_order DESC`),
        stores: isAdmin
          ? db.pool.query(`SELECT * FROM stores ${demoWhere} ORDER BY id ASC`)
          : db.pool.query(
              `SELECT * FROM stores WHERE ((published = TRUE AND status = 'approved') OR owner_id = $1) ${demoClause} ORDER BY id ASC`,
              [uid]
            ),
        mobiles: isAdmin
          ? db.pool.query(`SELECT * FROM mobiles ${demoWhere} ORDER BY sort_order ASC`)
          : db.pool.query(`SELECT * FROM mobiles WHERE published = TRUE ${demoClause} ORDER BY sort_order ASC`),
        storeMobiles: isAdmin
          ? db.pool.query(`SELECT * FROM store_mobiles ${demoWhere} ORDER BY price ASC`)
          : db.pool.query(`SELECT * FROM store_mobiles WHERE published = TRUE ${demoClause} ORDER BY price ASC`),
        accessories: isAdmin
          ? db.pool.query(`SELECT * FROM accessories ${demoWhere} ORDER BY id ASC`)
          : db.pool.query(`SELECT * FROM accessories WHERE published = TRUE ${demoClause} ORDER BY id ASC`),
        inquiries: isAdmin
          ? db.pool.query('SELECT * FROM inquiries ORDER BY sort_order DESC')
          : ownerStoreNames.length > 0
            ? db.pool.query('SELECT * FROM inquiries WHERE user_id = $1 OR store_name = ANY($2::text[]) ORDER BY sort_order DESC', [
                uid,
                ownerStoreNames
              ])
            : db.pool.query('SELECT * FROM inquiries WHERE user_id = $1 ORDER BY sort_order DESC', [uid]),
        swProblems: isAdmin
          ? db.pool.query('SELECT * FROM sw_problems ORDER BY id ASC')
          : db.pool.query('SELECT * FROM sw_problems WHERE published = TRUE ORDER BY id ASC'),
        tutorials: isAdmin
          ? db.pool.query(`SELECT * FROM tutorials ${demoWhere} ORDER BY id ASC`)
          : req.userTier === 'نرم\u200cافزارکار'
            ? db.pool.query(
                `SELECT * FROM tutorials WHERE (status = 'approved' OR owner_id = $1) ${demoClause} ORDER BY id ASC`,
                [uid]
              )
            : Promise.resolve({ rows: [] }),
        swRequests: isAdmin
          ? db.pool.query(`SELECT * FROM sw_requests ${demoWhere} ORDER BY sort_order DESC`)
          : db.pool.query(`SELECT * FROM sw_requests WHERE published = TRUE ${demoClause} ORDER BY sort_order DESC`),
        boxes: isAdmin
          ? db.pool.query(`SELECT * FROM boxes ${demoWhere} ORDER BY id ASC`)
          : db.pool.query(
              `SELECT * FROM boxes WHERE (status = 'approved' OR owner_id = $1) ${demoClause} ORDER BY id ASC`,
              [uid]
            ),
        hwCategories: isAdmin
          ? db.pool.query('SELECT * FROM hw_categories ORDER BY id ASC')
          : db.pool.query('SELECT * FROM hw_categories WHERE published = TRUE ORDER BY id ASC'),
        hwAds: isAdmin
          ? db.pool.query(`SELECT * FROM hw_ads ${demoWhere} ORDER BY sort_order DESC`)
          : db.pool.query(
              `SELECT * FROM hw_ads WHERE (status = 'approved' OR owner_id = $1) ${demoClause} ORDER BY sort_order DESC`,
              [uid]
            ),
        hwRequests: isAdmin
          ? db.pool.query(`SELECT * FROM hw_requests ${demoWhere} ORDER BY sort_order DESC`)
          : db.pool.query(
              `SELECT * FROM hw_requests WHERE user_id = $1 ${demoClause} ORDER BY sort_order DESC`,
              [uid]
            ),
        orders: isAdmin
          ? db.pool.query('SELECT * FROM orders ORDER BY sort_order DESC')
          : ownerStoreNames.length > 0
            ? db.pool.query(
                `SELECT * FROM orders
                 WHERE user_id = $1
                    OR EXISTS (SELECT 1 FROM jsonb_array_elements(items) elem WHERE elem->>'storeName' = ANY($2::text[]))
                 ORDER BY sort_order DESC`,
                [uid, ownerStoreNames]
              )
            : db.pool.query('SELECT * FROM orders WHERE user_id = $1 ORDER BY sort_order DESC', [uid]),
        chatThreads: isAdmin
          ? db.pool.query('SELECT * FROM chats')
          : db.pool.query('SELECT * FROM chats WHERE participants @> $1::jsonb', [JSON.stringify([uid])]),
        forumPosts: isAdmin
          ? db.pool.query(`SELECT * FROM forum_posts ${demoWhere} ORDER BY created_at_ms DESC`)
          : req.userTier === 'تعمیرکار'
            ? db.pool.query(
                `SELECT * FROM forum_posts WHERE published = TRUE AND board = 'hardware' ${demoClause} ORDER BY created_at_ms DESC`
              )
            : req.userTier === 'نرم\u200cافزارکار'
              ? db.pool.query(
                  `SELECT * FROM forum_posts WHERE published = TRUE AND board = 'software' ${demoClause} ORDER BY created_at_ms DESC`
                )
              : Promise.resolve({ rows: [] }),
        deskRentals: isAdmin
          ? db.pool.query(`SELECT * FROM desk_rentals ${demoWhere} ORDER BY sort_order DESC`)
          : db.pool.query(
              `SELECT * FROM desk_rentals WHERE (status = 'approved' OR owner_id = $1) ${demoClause} ORDER BY sort_order DESC`,
              [uid]
            ),
        deskRequests: db.pool.query(`SELECT * FROM desk_requests ${demoWhere} ORDER BY sort_order DESC`)
      };

      const keys = Object.keys(queries);
      const results = await Promise.all(Object.values(queries));
      const raw = {};
      keys.forEach((k, idx) => {
        raw[k] = results[idx].rows;
      });

      res.setHeader('ETag', etag);
      res.setHeader('Cache-Control', 'no-cache');
      return res.json({
        showDemoData,
        users: raw.users.map(db.mapUser),
        registrationRequests: raw.registrationRequests.map(db.mapRegistrationRequest),
        notifications: raw.notifications.map(db.mapNotification),
        stores: raw.stores.map(db.mapStore),
        mobiles: raw.mobiles.map(db.mapMobile),
        storeMobiles: raw.storeMobiles.map(db.mapStoreMobile),
        accessories: raw.accessories.map(db.mapAccessory),
        inquiries: raw.inquiries.map(db.mapInquiry),
        swProblems: raw.swProblems.map(db.mapSwProblem),
        tutorials: raw.tutorials.map(db.mapTutorial),
        swRequests: raw.swRequests.map(db.mapSwRequest),
        boxes: raw.boxes.map(db.mapBox),
        hwCategories: raw.hwCategories.map(db.mapHwCategory),
        hwAds: raw.hwAds.map(db.mapHwAd),
        hwRequests: raw.hwRequests.map(db.mapHwRequest),
        orders: raw.orders.map(db.mapOrder),
        chatThreads: raw.chatThreads.map(db.mapChat),
        forumPosts: raw.forumPosts.map(db.mapForumPost),
        deskRentals: raw.deskRentals.map(db.mapDeskRental),
        deskRequests: raw.deskRequests.map(db.mapDeskRequest)
      });
    } catch (err) {
      return next(err);
    }
  });

  router.get('/notifications/unread', async (req, res, next) => {
    try {
      const uid = req.auth.uid;
      const province = String(req.query?.province || req.liveUser?.province || 'تهران').trim();
      const city = String(req.query?.city || req.liveUser?.city || 'تهران').trim();
      const region = String(req.query?.region || req.liveUser?.region || 'همه مناطق').trim();
      const userTier = req.isAdmin ? 'ادمین' : (req.liveUser?.tier || req.userTier);
      const showDemoData = await getDemoVisibility();

      const { rows } = await db.pool.query(
        `SELECT * FROM notifications WHERE published = TRUE ${showDemoData ? '' : 'AND is_demo = FALSE'} ORDER BY sort_order DESC`
      );
      const matching = rows
        .map(db.mapNotification)
        .filter((n) => {
          const readBy = Array.isArray(n.readBy) ? n.readBy : [];
          if (readBy.includes(uid)) return false;
          const provMatch = isAllScope(n.province) || normalizeZwnj(n.province) === normalizeZwnj(province);
          const cityMatch = isAllScope(n.city) || normalizeZwnj(n.city) === normalizeZwnj(city);
          const regMatch = isAllScope(n.region) || isAllScope(region) || normalizeZwnj(n.region) === normalizeZwnj(region);
          const tierMatch =
            req.isAdmin || isAllScope(n.tierFilter) || normalizeZwnj(n.tierFilter) === normalizeZwnj(userTier);
          return provMatch && cityMatch && regMatch && tierMatch;
        });
      return res.json({ notifications: matching });
    } catch (err) {
      return next(err);
    }
  });

  router.patch('/users/me', async (req, res, next) => {
    try {
      const fullName = String(req.body?.fullName || '').trim().slice(0, 120);
      if (!fullName) {
        return res.status(400).json({ error: 'نام و نام خانوادگی الزامی است.' });
      }
      const phone = String(req.body?.phone || '').trim().slice(0, 40);
      const province = String(req.body?.province || 'تهران').trim().slice(0, 80) || 'تهران';
      const city = String(req.body?.city || 'تهران').trim().slice(0, 80) || 'تهران';
      const region = String(req.body?.region || 'همه مناطق').trim().slice(0, 80) || 'همه مناطق';

      await db.pool.query(
        `UPDATE users
         SET full_name = $1, phone = $2, province = $3, city = $4, region = $5, updated_at = NOW()
         WHERE id = $6`,
        [fullName, phone, province, city, region, req.auth.uid]
      );
      broadcastSync(['users']);
      return res.json({ ok: true });
    } catch (err) {
      return next(err);
    }
  });

  router.post('/users/me/password', async (req, res, next) => {
    try {
      const password = String(req.body?.password || '');
      if (password.length < 6 || password.length > 128) {
        return res.status(400).json({ error: 'رمز عبور باید بین ۶ تا ۱۲۸ کاراکتر باشد.' });
      }
      const hashed = await hashPasswordAsync(password);
      await db.pool.query('UPDATE users SET password_hash = $1, updated_at = NOW() WHERE id = $2', [
        hashed,
        req.auth.uid
      ]);
      return res.json({ ok: true });
    } catch (err) {
      return next(err);
    }
  });

  // ==================== ADMIN USER & SYSTEM ROUTES ====================

  router.get('/admin/users', requireAdmin, async (_req, res, next) => {
    try {
      const { rows } = await db.pool.query('SELECT * FROM users ORDER BY full_name ASC');
      return res.json({ users: rows.map(db.mapUser) });
    } catch (err) {
      return next(err);
    }
  });

  router.post('/admin/users', requireAdmin, async (req, res, next) => {
    const client = await db.pool.connect();
    try {
      const normEmail = normalizeEmail(req.body?.email).slice(0, 200);
      const fullName = String(req.body?.fullName || '').trim().slice(0, 120);
      const phone = String(req.body?.phone || '').trim().slice(0, 40);
      const tier = normalizeTier(req.body?.tier);
      const province = String(req.body?.province || 'تهران').trim().slice(0, 80) || 'تهران';
      const city = String(req.body?.city || 'تهران').trim().slice(0, 80) || 'تهران';
      const region = String(req.body?.region || 'همه مناطق').trim().slice(0, 80) || 'همه مناطق';
      const createdDateLabel = String(req.body?.createdDateLabel || 'امروز').trim().slice(0, 40) || 'امروز';
      const fallbackId = String(req.body?.id || req.body?.uid || '').trim() || generateUid('u_');
      const rawPassword = String(req.body?.password || '');

      if (!fullName) {
        return res.status(400).json({ error: 'نام و نام خانوادگی الزامی است.' });
      }
      if (rawPassword && (rawPassword.length < 6 || rawPassword.length > 128)) {
        return res.status(400).json({ error: 'رمز عبور باید بین ۶ تا ۱۲۸ کاراکتر باشد.' });
      }
      const passwordHash = rawPassword ? await hashPasswordAsync(rawPassword) : '';

      await client.query('BEGIN');

      let existingRow = null;
      if (isValidEmailFormat(normEmail)) {
        const existingRes = await client.query('SELECT * FROM users WHERE LOWER(email) = $1 LIMIT 1', [normEmail]);
        existingRow = existingRes.rows[0] || null;
      }

      const targetId = existingRow ? existingRow.id : fallbackId;
      const targetUid = existingRow ? existingRow.uid || targetId : targetId;

      if (existingRow) {
        await client.query(
          `UPDATE users
           SET full_name = $1,
               phone = $2,
               tier = $3,
               province = $4,
               city = $5,
               region = $6,
               active = TRUE,
               password_hash = CASE WHEN $7 <> '' THEN $7 ELSE password_hash END,
               updated_at = NOW()
           WHERE id = $8`,
          [fullName, phone, tier, province, city, region, passwordHash, targetId]
        );
      } else {
        await client.query(
          `INSERT INTO users (id, uid, email, full_name, phone, tier, province, city, region, active, created_date_label, password_hash)
           VALUES ($1,$2,$3,$4,$5,$6,$7,$8,$9,TRUE,$10,$11)`,
          [targetId, targetUid, normEmail, fullName, phone, tier, province, city, region, createdDateLabel, passwordHash]
        );
      }

      if (isValidEmailFormat(normEmail)) {
        await client.query(
          `INSERT INTO user_invites (email, full_name, phone, tier, province, city, region, active, created_date_label, source_user_doc_id, claimed_by_uid)
           VALUES ($1,$2,$3,$4,$5,$6,$7,TRUE,$8,$9,$10)
           ON CONFLICT (email) DO UPDATE SET
             full_name = EXCLUDED.full_name,
             phone = EXCLUDED.phone,
             tier = EXCLUDED.tier,
             province = EXCLUDED.province,
             city = EXCLUDED.city,
             region = EXCLUDED.region,
             active = TRUE,
             updated_at = NOW()`,
          [
            normEmail,
            fullName,
            phone,
            tier,
            province,
            city,
            region,
            createdDateLabel,
            targetId.startsWith('u_') ? targetId : '',
            targetId.startsWith('u_') ? '' : targetId
          ]
        );
      }

      const { rows } = await client.query('SELECT * FROM users WHERE id = $1', [targetId]);
      await client.query('COMMIT');
      broadcastSync(['users']);
      return res.status(existingRow ? 200 : 201).json({ user: db.mapUser(rows[0]) });
    } catch (err) {
      await client.query('ROLLBACK');
      return next(err);
    } finally {
      client.release();
    }
  });

  router.patch('/admin/users/:id/active', requireAdmin, async (req, res, next) => {
    const client = await db.pool.connect();
    try {
      const targetId = String(req.params.id || '').trim();
      const active = Boolean(req.body?.active);
      const userPayload = req.body?.user || {};
      const normEmail = normalizeEmail(req.body?.email || userPayload.email);

      await client.query('BEGIN');

      const matched = await client.query(
        `SELECT id FROM users WHERE id = $1 OR uid = $1 OR ($2 <> '' AND LOWER(email) = $2)`,
        [targetId, normEmail]
      );

      if (matched.rows.length === 0 && targetId) {
        const cleanName = String(userPayload.fullName || 'همکار بازمو').trim().slice(0, 120) || 'همکار بازمو';
        const tier = normalizeTier(userPayload.tier);
        const province = String(userPayload.province || 'تهران').trim().slice(0, 80) || 'تهران';
        const city = String(userPayload.city || 'تهران').trim().slice(0, 80) || 'تهران';
        const region = String(userPayload.region || 'همه مناطق').trim().slice(0, 80) || 'همه مناطق';
        await client.query(
          `INSERT INTO users (id, uid, email, full_name, phone, tier, province, city, region, active, created_date_label)
           VALUES ($1,$2,$3,$4,$5,$6,$7,$8,$9,$10,$11)`,
          [
            targetId,
            String(userPayload.uid || targetId),
            normEmail,
            cleanName,
            String(userPayload.phone || '').slice(0, 40),
            tier,
            province,
            city,
            region,
            active,
            String(userPayload.createdDateLabel || 'امروز').slice(0, 40)
          ]
        );
      } else {
        await client.query(
          `UPDATE users SET active = $1, updated_at = NOW()
           WHERE id = $2 OR uid = $2 OR ($3 <> '' AND LOWER(email) = $3)`,
          [active, targetId, normEmail]
        );
      }

      if (isValidEmailFormat(normEmail)) {
        const cleanName = String(userPayload.fullName || 'همکار بازمو').trim().slice(0, 120) || 'همکار بازمو';
        const tier = normalizeTier(userPayload.tier);
        const province = String(userPayload.province || 'تهران').trim().slice(0, 80) || 'تهران';
        const city = String(userPayload.city || 'تهران').trim().slice(0, 80) || 'تهران';
        const region = String(userPayload.region || 'همه مناطق').trim().slice(0, 80) || 'همه مناطق';

        const inv = await client.query('SELECT email FROM user_invites WHERE email = $1', [normEmail]);
        if (inv.rows.length > 0) {
          await client.query('UPDATE user_invites SET active = $1, updated_at = NOW() WHERE email = $2', [
            active,
            normEmail
          ]);
        } else if (active) {
          await client.query(
            `INSERT INTO user_invites (email, full_name, phone, tier, province, city, region, active, created_date_label, source_user_doc_id, claimed_by_uid)
             VALUES ($1,$2,$3,$4,$5,$6,$7,TRUE,$8,$9,$10)`,
            [
              normEmail,
              cleanName,
              String(userPayload.phone || '').slice(0, 40),
              tier,
              province,
              city,
              region,
              String(userPayload.createdDateLabel || 'امروز').slice(0, 40),
              targetId.startsWith('u_') ? targetId : '',
              targetId.startsWith('u_') ? '' : targetId
            ]
          );
        }
      }

      await client.query('COMMIT');
      broadcastSync(['users']);
      return res.json({ ok: true });
    } catch (err) {
      await client.query('ROLLBACK');
      return next(err);
    } finally {
      client.release();
    }
  });

  router.patch('/admin/users/:id/tier', requireAdmin, async (req, res, next) => {
    const client = await db.pool.connect();
    try {
      const targetId = String(req.params.id || '').trim();
      const newTier = normalizeTier(req.body?.tier);
      const normEmail = normalizeEmail(req.body?.email);

      await client.query('BEGIN');
      await client.query(
        `UPDATE users SET tier = $1, updated_at = NOW()
         WHERE id = $2 OR uid = $2 OR ($3 <> '' AND LOWER(email) = $3)`,
        [newTier, targetId, normEmail]
      );
      if (isValidEmailFormat(normEmail)) {
        await client.query('UPDATE user_invites SET tier = $1, updated_at = NOW() WHERE email = $2', [
          newTier,
          normEmail
        ]);
      }
      await client.query('COMMIT');
      broadcastSync(['users']);
      return res.json({ ok: true });
    } catch (err) {
      await client.query('ROLLBACK');
      return next(err);
    } finally {
      client.release();
    }
  });

  router.patch('/admin/users/:id/password', requireAdmin, async (req, res, next) => {
    try {
      const targetId = String(req.params.id || '').trim();
      const password = String(req.body?.password || '');
      const normEmail = normalizeEmail(req.body?.email);
      if (password.length < 6 || password.length > 128) {
        return res.status(400).json({ error: 'رمز عبور باید بین ۶ تا ۱۲۸ کاراکتر باشد.' });
      }
      const hashed = await hashPasswordAsync(password);
      await db.pool.query(
        `UPDATE users SET password_hash = $1, updated_at = NOW()
         WHERE id = $2 OR uid = $2 OR ($3 <> '' AND LOWER(email) = $3)`,
        [hashed, targetId, normEmail]
      );
      return res.json({ ok: true });
    } catch (err) {
      return next(err);
    }
  });

  router.delete('/admin/users/:id', requireAdmin, async (req, res, next) => {
    const client = await db.pool.connect();
    try {
      const targetId = String(req.params.id || '').trim();
      const queryEmail = normalizeEmail(req.query?.email || req.body?.email);

      await client.query('BEGIN');
      const existing = await client.query('SELECT email FROM users WHERE id = $1 LIMIT 1', [targetId]);
      const resolvedEmail = queryEmail || normalizeEmail(existing.rows[0]?.email);

      await client.query('DELETE FROM users WHERE id = $1 OR uid = $1', [targetId]);
      if (isValidEmailFormat(resolvedEmail)) {
        await client.query('DELETE FROM user_invites WHERE email = $1', [resolvedEmail]);
      }
      await client.query('COMMIT');
      broadcastSync(['users']);
      return res.json({ ok: true });
    } catch (err) {
      await client.query('ROLLBACK');
      return next(err);
    } finally {
      client.release();
    }
  });

  router.post('/admin/registration-requests/:id/approve', requireAdmin, async (req, res, next) => {
    const client = await db.pool.connect();
    try {
      const reqId = String(req.params.id || '').trim();
      const regRes = await client.query('SELECT * FROM registration_requests WHERE id = $1 LIMIT 1', [reqId]);
      const reqRow = regRes.rows[0] || req.body?.request;
      if (!reqRow) {
        return res.status(404).json({ error: 'درخواست عضویت یافت نشد.' });
      }

      const normEmail = normalizeEmail(reqRow.email);
      const assignedTier = normalizeTier(req.body?.assignedTier || reqRow.requested_tier || reqRow.requestedTier);
      const fullName = String(reqRow.full_name || reqRow.fullName || 'همکار بازمو').trim().slice(0, 120);
      const phone = String(reqRow.phone || '').trim().slice(0, 40);
      const province = String(reqRow.province || 'تهران').trim().slice(0, 80) || 'تهران';
      const city = String(reqRow.city || 'تهران').trim().slice(0, 80) || 'تهران';
      const region = String(reqRow.region || 'همه مناطق').trim().slice(0, 80) || 'همه مناطق';

      await client.query('BEGIN');

      const existingUserRes = isValidEmailFormat(normEmail)
        ? await client.query('SELECT * FROM users WHERE LOWER(email) = $1 LIMIT 1', [normEmail])
        : { rows: [] };
      const existingUser = existingUserRes.rows[0] || null;
      const targetId = existingUser ? existingUser.id : generateUid('u_');

      const overridePass = String(req.body?.password || '');
      const inheritedHash =
        overridePass.length >= 6 && overridePass.length <= 128
          ? await hashPasswordAsync(overridePass)
          : String(reqRow.password_hash || '');

      if (existingUser) {
        await client.query(
          `UPDATE users
           SET active = TRUE,
               tier = $1,
               full_name = COALESCE(NULLIF($2, ''), full_name),
               phone = COALESCE(NULLIF($3, ''), phone),
               province = COALESCE(NULLIF($4, ''), province),
               city = COALESCE(NULLIF($5, ''), city),
               region = COALESCE(NULLIF($6, ''), region),
               password_hash = CASE WHEN users.password_hash <> '' THEN users.password_hash ELSE $7 END,
               updated_at = NOW()
           WHERE id = $8`,
          [assignedTier, fullName, phone, province, city, region, inheritedHash, targetId]
        );
      } else {
        await client.query(
          `INSERT INTO users (id, uid, email, full_name, phone, tier, province, city, region, active, created_date_label, password_hash)
           VALUES ($1,$2,$3,$4,$5,$6,$7,$8,$9,TRUE,'امروز',$10)`,
          [targetId, targetId, normEmail, fullName, phone, assignedTier, province, city, region, inheritedHash]
        );
      }

      if (isValidEmailFormat(normEmail)) {
        await client.query(
          `INSERT INTO user_invites (email, full_name, phone, tier, province, city, region, active, created_date_label, source_user_doc_id, claimed_by_uid)
           VALUES ($1,$2,$3,$4,$5,$6,$7,TRUE,'امروز',$8,$9)
           ON CONFLICT (email) DO UPDATE SET
             full_name = EXCLUDED.full_name,
             phone = EXCLUDED.phone,
             tier = EXCLUDED.tier,
             province = EXCLUDED.province,
             city = EXCLUDED.city,
             region = EXCLUDED.region,
             active = TRUE,
             updated_at = NOW()`,
          [
            normEmail,
            fullName,
            phone,
            assignedTier,
            province,
            city,
            region,
            targetId.startsWith('u_') ? targetId : '',
            targetId.startsWith('u_') ? '' : targetId
          ]
        );
      }

      await client.query(
        `UPDATE registration_requests
         SET status = 'approved', requested_tier = $1, updated_at = NOW()
         WHERE id = $2`,
        [assignedTier, reqId]
      );

      await client.query('COMMIT');
      broadcastSync(['users', 'registration_requests']);
      return res.json({ ok: true });
    } catch (err) {
      await client.query('ROLLBACK');
      return next(err);
    } finally {
      client.release();
    }
  });

  router.post('/admin/registration-requests/:id/reject', requireAdmin, async (req, res, next) => {
    try {
      const reqId = String(req.params.id || '').trim();
      await db.pool.query(
        `UPDATE registration_requests SET status = 'rejected', updated_at = NOW() WHERE id = $1`,
        [reqId]
      );
      broadcastSync(['registration_requests']);
      return res.json({ ok: true });
    } catch (err) {
      return next(err);
    }
  });

  router.post('/admin/reset-defaults', requireAdmin, async (req, res, next) => {
    try {
      await db.ensureSchemaReady();
      await resetAllToDefaults(req.auth.uid);
      broadcastSync(['all']);
      return res.json({ ok: true });
    } catch (err) {
      return next(err);
    }
  });

  router.post('/admin/demo-visibility', requireAdmin, async (req, res, next) => {
    try {
      await db.ensureSchemaReady();
      const visible = Boolean(req.body?.visible);
      await setDemoVisibility(visible);
      broadcastSync(['all']);
      return res.json({ ok: true, showDemoData: visible });
    } catch (err) {
      return next(err);
    }
  });

  router.post('/admin/seed-demo', requireAdmin, async (req, res, next) => {
    try {
      await db.ensureSchemaReady();
      const counts = await writeDemoDataset(req.auth.uid);
      broadcastSync(['all']);
      return res.json({ ok: true, counts });
    } catch (err) {
      return next(err);
    }
  });

  // ==================== NOTIFICATIONS ====================

  router.post('/notifications', requireAdmin, async (req, res, next) => {
    try {
      const n = req.body || {};
      const id = String(n.id || generateUid('nt_'));
      await db.pool.query(
        `INSERT INTO notifications (id, title, body, type, province, city, region, tier_filter, section_filter, date, read_by, published, sort_order)
         VALUES ($1,$2,$3,$4,$5,$6,$7,$8,$9,$10,$11::jsonb,TRUE,$12)`,
        [
          id,
          String(n.title || ''),
          String(n.body || ''),
          String(n.type || ''),
          String(n.province || 'همه استان‌ها'),
          String(n.city || 'همه شهرها'),
          String(n.region || 'همه مناطق'),
          String(n.tierFilter || 'همه'),
          String(n.sectionFilter || 'همه بخش‌ها'),
          String(n.date || 'هم‌اکنون'),
          JSON.stringify(Array.isArray(n.readBy) ? n.readBy : []),
          Number(n.sortOrder || Date.now())
        ]
      );
      broadcastSync(['notifications']);
      return res.status(201).json({ ok: true, id });
    } catch (err) {
      return next(err);
    }
  });

  router.post('/notifications/:id/read', async (req, res, next) => {
    try {
      const id = String(req.params.id || '');
      const uid = req.auth.uid;
      await db.pool.query(
        `UPDATE notifications
         SET read_by = CASE
           WHEN read_by @> $1::jsonb THEN read_by
           ELSE read_by || $1::jsonb
         END
         WHERE id = $2`,
        [JSON.stringify([uid]), id]
      );
      broadcastSync(['notifications']);
      return res.json({ ok: true });
    } catch (err) {
      return next(err);
    }
  });

  router.delete('/notifications/:id', requireAdmin, async (req, res, next) => {
    try {
      await db.pool.query('DELETE FROM notifications WHERE id = $1', [req.params.id]);
      broadcastSync(['notifications']);
      return res.json({ ok: true });
    } catch (err) {
      return next(err);
    }
  });

  // ==================== STORES & MOBILES & ACCESSORIES ====================

  router.post('/stores', async (req, res, next) => {
    try {
      if (!req.isAdmin && !STORE_CREATOR_ROLES.has(req.userTier)) {
        return res.status(403).json({
          error: 'فقط عمده‌فروش موبایل، عمده‌فروش جانبی و قطعه‌فروش امکان ثبت فروشگاه دارند.'
        });
      }
      const s = req.body || {};
      const id = String(s.id || generateUid('st_'));
      const ownerId = req.isAdmin ? String(s.ownerId || req.auth.uid) : req.auth.uid;
      const status = req.isAdmin ? String(s.status || 'approved') : 'pending';
      const published = status === 'approved';
      await db.pool.query(
        `INSERT INTO stores (id, name, province, city, region, phone, address, hours, rating, type, owner_id, status, published, is_demo)
         VALUES ($1,$2,$3,$4,$5,$6,$7,$8,$9,$10,$11,$12,$13,FALSE)`,
        [
          id,
          String(s.name || '').trim().slice(0, 140),
          String(s.province || 'تهران'),
          String(s.city || 'تهران'),
          String(s.region || 'همه مناطق'),
          String(s.phone || ''),
          String(s.address || ''),
          String(s.hours || '۱۰ تا ۲۰'),
          String(s.rating || '۵.۰'),
          String(s.type || 'both'),
          ownerId,
          status,
          published
        ]
      );
      broadcastSync(['stores']);
      return res.status(201).json({ ok: true, id, status });
    } catch (err) {
      return next(err);
    }
  });

  router.patch('/stores/:id/status', requireAdmin, async (req, res, next) => {
    try {
      const status = String(req.body?.status || 'approved') === 'rejected' ? 'rejected' : 'approved';
      const published = status === 'approved';
      await db.pool.query('UPDATE stores SET status = $1, published = $2 WHERE id = $3', [
        status,
        published,
        req.params.id
      ]);
      broadcastSync(['stores']);
      return res.json({ ok: true, status });
    } catch (err) {
      return next(err);
    }
  });

  router.patch('/stores/:id', async (req, res, next) => {
    try {
      const check = await verifyStoreOwner(req, req.params.id, false);
      if (!check.ok) return res.status(403).json({ error: check.error });
      const s = req.body || {};
      await db.pool.query(
        `UPDATE stores
         SET name = COALESCE(NULLIF($1, ''), name),
             province = COALESCE(NULLIF($2, ''), province),
             city = COALESCE(NULLIF($3, ''), city),
             region = COALESCE(NULLIF($4, ''), region),
             phone = $5,
             address = $6,
             hours = $7,
             type = COALESCE(NULLIF($8, ''), type)
         WHERE id = $9`,
        [
          String(s.name || '').trim().slice(0, 140),
          String(s.province || '').trim(),
          String(s.city || '').trim(),
          String(s.region || '').trim(),
          String(s.phone ?? check.store?.phone ?? ''),
          String(s.address ?? check.store?.address ?? ''),
          String(s.hours ?? check.store?.hours ?? '۱۰ تا ۲۰'),
          String(s.type || '').trim(),
          req.params.id
        ]
      );
      broadcastSync(['stores']);
      return res.json({ ok: true });
    } catch (err) {
      return next(err);
    }
  });

  router.delete('/stores/:id', requireAdmin, async (req, res, next) => {
    try {
      await db.pool.query('DELETE FROM stores WHERE id = $1', [req.params.id]);
      broadcastSync(['stores']);
      return res.json({ ok: true });
    } catch (err) {
      return next(err);
    }
  });

  router.post('/mobiles', requireAdmin, async (req, res, next) => {
    const client = await db.pool.connect();
    try {
      const m = req.body?.mobile || {};
      const o = req.body?.offer || null;
      const mId = String(m.id || generateUid('m_'));

      await client.query('BEGIN');
      await client.query(
        `INSERT INTO mobiles (id, brand, model, specs, img_emoji, sort_order, published, is_demo)
         VALUES ($1,$2,$3,$4,$5,$6,TRUE,FALSE)`,
        [
          mId,
          String(m.brand || ''),
          String(m.model || ''),
          String(m.specs || ''),
          String(m.imgEmoji || '📱'),
          Number(m.sortOrder || 0)
        ]
      );
      if (o) {
        const oId = String(o.id || generateUid('sm_'));
        await client.query(
          `INSERT INTO store_mobiles (id, store_id, mobile_id, price, info, published, is_demo)
           VALUES ($1,$2,$3,$4,$5,TRUE,FALSE)`,
          [oId, String(o.storeId || 'st1'), mId, Number(o.price || 0), String(o.info || '')]
        );
      }
      await client.query('COMMIT');
      broadcastSync(['mobiles', 'store_mobiles']);
      return res.status(201).json({ ok: true, id: mId });
    } catch (err) {
      await client.query('ROLLBACK');
      return next(err);
    } finally {
      client.release();
    }
  });

  router.post('/store-mobiles', async (req, res, next) => {
    const client = await db.pool.connect();
    try {
      const body = req.body || {};
      const storeId = String(body.storeId || '').trim();
      const check = await verifyStoreOwner(req, storeId, true);
      if (!check.ok) return res.status(403).json({ error: check.error });

      await client.query('BEGIN');
      let mobileId = String(body.mobileId || '').trim();
      const newMobile = body.newMobile || null;
      if (!mobileId && newMobile && String(newMobile.model || '').trim()) {
        mobileId = String(newMobile.id || generateUid('m_'));
        await client.query(
          `INSERT INTO mobiles (id, brand, model, specs, img_emoji, sort_order, published, is_demo)
           VALUES ($1,$2,$3,$4,$5,$6,TRUE,FALSE)`,
          [
            mobileId,
            String(newMobile.brand || 'سایر'),
            String(newMobile.model || '').trim(),
            String(newMobile.specs || ''),
            String(newMobile.imgEmoji || '📱'),
            Number(newMobile.sortOrder || Date.now())
          ]
        );
      }
      if (!mobileId) {
        await client.query('ROLLBACK');
        return res.status(400).json({ error: 'مدل گوشی مشخص نشده است.' });
      }
      const offerId = String(body.id || generateUid('sm_'));
      await client.query(
        `INSERT INTO store_mobiles (id, store_id, mobile_id, price, info, published, is_demo)
         VALUES ($1,$2,$3,$4,$5,TRUE,FALSE)`,
        [offerId, storeId, mobileId, Number(body.price || 0), String(body.info || '')]
      );
      await client.query('COMMIT');
      broadcastSync(['mobiles', 'store_mobiles']);
      return res.status(201).json({ ok: true, id: offerId, mobileId });
    } catch (err) {
      await client.query('ROLLBACK');
      return next(err);
    } finally {
      client.release();
    }
  });

  router.patch('/store-mobiles/:id', async (req, res, next) => {
    try {
      const { rows } = await db.pool.query('SELECT * FROM store_mobiles WHERE id = $1 LIMIT 1', [req.params.id]);
      if (rows.length === 0) return res.status(404).json({ error: 'آیتم یافت نشد.' });
      const check = await verifyStoreOwner(req, rows[0].store_id, true);
      if (!check.ok) return res.status(403).json({ error: check.error });
      await db.pool.query('UPDATE store_mobiles SET price = $1, info = $2 WHERE id = $3', [
        Number(req.body?.price ?? rows[0].price),
        String(req.body?.info ?? rows[0].info),
        req.params.id
      ]);
      broadcastSync(['store_mobiles']);
      return res.json({ ok: true });
    } catch (err) {
      return next(err);
    }
  });

  router.delete('/store-mobiles/:id', async (req, res, next) => {
    try {
      const { rows } = await db.pool.query('SELECT store_id FROM store_mobiles WHERE id = $1 LIMIT 1', [req.params.id]);
      if (rows.length === 0) return res.status(404).json({ error: 'آیتم یافت نشد.' });
      const check = await verifyStoreOwner(req, rows[0].store_id, true);
      if (!check.ok) return res.status(403).json({ error: check.error });
      await db.pool.query('DELETE FROM store_mobiles WHERE id = $1', [req.params.id]);
      broadcastSync(['store_mobiles']);
      return res.json({ ok: true });
    } catch (err) {
      return next(err);
    }
  });

  router.post('/accessories', async (req, res, next) => {
    try {
      const a = req.body || {};
      const storeId = String(a.storeId || '').trim();
      const check = await verifyStoreOwner(req, storeId, true);
      if (!check.ok) return res.status(403).json({ error: check.error });
      const id = String(a.id || generateUid('ac_'));
      await db.pool.query(
        `INSERT INTO accessories (id, cat, name, store_id, price, description, published, is_demo)
         VALUES ($1,$2,$3,$4,$5,$6,TRUE,FALSE)`,
        [
          id,
          String(a.cat || 'قاب و گارد'),
          String(a.name || ''),
          storeId,
          Number(a.price || 0),
          String(a.desc || '')
        ]
      );
      broadcastSync(['accessories']);
      return res.status(201).json({ ok: true, id });
    } catch (err) {
      return next(err);
    }
  });

  router.patch('/accessories/:id', async (req, res, next) => {
    try {
      const { rows } = await db.pool.query('SELECT * FROM accessories WHERE id = $1 LIMIT 1', [req.params.id]);
      if (rows.length === 0) return res.status(404).json({ error: 'کالای جانبی یافت نشد.' });
      const check = await verifyStoreOwner(req, rows[0].store_id, true);
      if (!check.ok) return res.status(403).json({ error: check.error });
      const a = req.body || {};
      await db.pool.query(
        `UPDATE accessories SET cat = $1, name = $2, price = $3, description = $4 WHERE id = $5`,
        [
          String(a.cat ?? rows[0].cat),
          String(a.name ?? rows[0].name),
          Number(a.price ?? rows[0].price),
          String(a.desc ?? rows[0].description),
          req.params.id
        ]
      );
      broadcastSync(['accessories']);
      return res.json({ ok: true });
    } catch (err) {
      return next(err);
    }
  });

  router.delete('/accessories/:id', async (req, res, next) => {
    try {
      const { rows } = await db.pool.query('SELECT store_id FROM accessories WHERE id = $1 LIMIT 1', [req.params.id]);
      if (rows.length === 0) return res.status(404).json({ error: 'کالای جانبی یافت نشد.' });
      const check = await verifyStoreOwner(req, rows[0].store_id, true);
      if (!check.ok) return res.status(403).json({ error: check.error });
      await db.pool.query('DELETE FROM accessories WHERE id = $1', [req.params.id]);
      broadcastSync(['accessories']);
      return res.json({ ok: true });
    } catch (err) {
      return next(err);
    }
  });

  router.post('/inquiries', async (req, res, next) => {
    try {
      const q = req.body || {};
      const id = String(q.id || generateUid('inq_'));
      await db.pool.query(
        `INSERT INTO inquiries (id, acc_id, acc_name, store_name, user_id, user_name, status, date, sort_order)
         VALUES ($1,$2,$3,$4,$5,$6,$7,$8,$9)`,
        [
          id,
          String(q.accId || ''),
          String(q.accName || ''),
          String(q.storeName || ''),
          req.auth.uid,
          String(q.userName || req.liveUser?.fullName || ''),
          String(q.status || 'ارسال شده به فروشنده'),
          String(q.date || 'امروز'),
          Number(q.sortOrder || Date.now())
        ]
      );
      broadcastSync(['inquiries']);
      return res.status(201).json({ ok: true, id });
    } catch (err) {
      return next(err);
    }
  });

  router.patch('/inquiries/:id/status', async (req, res, next) => {
    try {
      if (!req.isAdmin) {
        const inqRes = await db.pool.query('SELECT store_name FROM inquiries WHERE id = $1 LIMIT 1', [req.params.id]);
        if (inqRes.rows.length === 0) return res.status(404).json({ error: 'استعلام یافت نشد.' });
        const ownMatch = await db.pool.query(
          "SELECT 1 FROM stores WHERE owner_id = $1 AND status = 'approved' AND name = $2 LIMIT 1",
          [req.auth.uid, inqRes.rows[0].store_name]
        );
        if (ownMatch.rows.length === 0) {
          return res.status(403).json({ error: 'فقط مدیر یا صاحب فروشگاه مربوطه مجاز است.' });
        }
      }
      await db.pool.query('UPDATE inquiries SET status = $1 WHERE id = $2', [
        String(req.body?.status || ''),
        req.params.id
      ]);
      broadcastSync(['inquiries']);
      return res.json({ ok: true });
    } catch (err) {
      return next(err);
    }
  });

  // ==================== SOFTWARE SECTION ====================

  router.post('/tutorials', async (req, res, next) => {
    try {
      if (!req.isAdmin && req.userTier !== 'نرم\u200cافزارکار') {
        return res.status(403).json({ error: 'ثبت آموزش فقط برای نقش نرم‌افزارکار مجاز است.' });
      }
      const t = req.body || {};
      const id = String(t.id || generateUid('tut_'));
      const status = req.isAdmin ? String(t.status || 'approved') : 'pending';
      await db.pool.query(
        `INSERT INTO tutorials (id, problem_id, title, teacher, rating, price, terms, city, status, owner_id)
         VALUES ($1,$2,$3,$4,$5,$6,$7,$8,$9,$10)`,
        [
          id,
          String(t.problemId || 'p1'),
          String(t.title || ''),
          String(t.teacher || ''),
          String(t.rating || '۵.۰'),
          Number(t.price || 0),
          String(t.terms || ''),
          String(t.city || ''),
          status,
          req.auth.uid
        ]
      );
      broadcastSync(['tutorials']);
      return res.status(201).json({ ok: true, id });
    } catch (err) {
      return next(err);
    }
  });

  router.post('/tutorials/:id/approve', requireAdmin, async (req, res, next) => {
    try {
      await db.pool.query("UPDATE tutorials SET status = 'approved' WHERE id = $1", [req.params.id]);
      broadcastSync(['tutorials']);
      return res.json({ ok: true });
    } catch (err) {
      return next(err);
    }
  });

  router.delete('/tutorials/:id', async (req, res, next) => {
    try {
      if (req.isAdmin) {
        await db.pool.query('DELETE FROM tutorials WHERE id = $1', [req.params.id]);
      } else {
        const result = await db.pool.query('DELETE FROM tutorials WHERE id = $1 AND owner_id = $2', [
          req.params.id,
          req.auth.uid
        ]);
        if (result.rowCount === 0) return res.status(403).json({ error: 'دسترسی مجاز نیست.' });
      }
      broadcastSync(['tutorials']);
      return res.json({ ok: true });
    } catch (err) {
      return next(err);
    }
  });

  router.post('/sw-requests', async (req, res, next) => {
    try {
      const r = req.body || {};
      const id = String(r.id || generateUid('sr_'));
      await db.pool.query(
        `INSERT INTO sw_requests (id, title, user_id, user_name, city, status, reply, published, sort_order)
         VALUES ($1,$2,$3,$4,$5,$6,$7,TRUE,$8)`,
        [
          id,
          String(r.title || ''),
          req.auth.uid,
          String(r.userName || ''),
          String(r.city || ''),
          String(r.status || 'در انتظار بررسی ادمین'),
          String(r.reply || ''),
          Number(r.sortOrder || Date.now())
        ]
      );
      broadcastSync(['sw_requests']);
      return res.status(201).json({ ok: true, id });
    } catch (err) {
      return next(err);
    }
  });

  router.post('/sw-requests/:id/reply', requireAdmin, async (req, res, next) => {
    try {
      await db.pool.query("UPDATE sw_requests SET reply = $1, status = 'پاسخ داده شده' WHERE id = $2", [
        String(req.body?.reply || ''),
        req.params.id
      ]);
      broadcastSync(['sw_requests']);
      return res.json({ ok: true });
    } catch (err) {
      return next(err);
    }
  });

  router.post('/boxes', async (req, res, next) => {
    try {
      const b = req.body || {};
      const id = String(b.id || generateUid('bx_'));
      const status = req.isAdmin ? String(b.status || 'approved') : 'pending';
      await db.pool.query(
        `INSERT INTO boxes (id, name, price, seller, city, condition, status, owner_id)
         VALUES ($1,$2,$3,$4,$5,$6,$7,$8)`,
        [
          id,
          String(b.name || ''),
          Number(b.price || 0),
          String(b.seller || ''),
          String(b.city || ''),
          String(b.condition || ''),
          status,
          req.auth.uid
        ]
      );
      broadcastSync(['boxes']);
      return res.status(201).json({ ok: true, id });
    } catch (err) {
      return next(err);
    }
  });

  router.post('/boxes/:id/approve', requireAdmin, async (req, res, next) => {
    try {
      await db.pool.query("UPDATE boxes SET status = 'approved' WHERE id = $1", [req.params.id]);
      broadcastSync(['boxes']);
      return res.json({ ok: true });
    } catch (err) {
      return next(err);
    }
  });

  router.delete('/boxes/:id', async (req, res, next) => {
    try {
      if (req.isAdmin) {
        await db.pool.query('DELETE FROM boxes WHERE id = $1', [req.params.id]);
      } else {
        const result = await db.pool.query('DELETE FROM boxes WHERE id = $1 AND owner_id = $2', [
          req.params.id,
          req.auth.uid
        ]);
        if (result.rowCount === 0) return res.status(403).json({ error: 'دسترسی مجاز نیست.' });
      }
      broadcastSync(['boxes']);
      return res.json({ ok: true });
    } catch (err) {
      return next(err);
    }
  });

  // ==================== HARDWARE ADS & REQUESTS ====================

  router.post('/hw-ads', async (req, res, next) => {
    try {
      const a = req.body || {};
      const id = String(a.id || generateUid('ad_'));
      const status = req.isAdmin ? String(a.status || 'approved') : 'pending';
      await db.pool.query(
        `INSERT INTO hw_ads (id, cat, title, brand, model, series, price, seller, phone, city, province, region, owner_id, owner_name, status, description, section, sort_order)
         VALUES ($1,$2,$3,$4,$5,$6,$7,$8,$9,$10,$11,$12,$13,$14,$15,$16,$17,$18)`,
        [
          id,
          String(a.cat || 'سایر'),
          String(a.title || ''),
          String(a.brand || ''),
          String(a.model || ''),
          String(a.series || ''),
          Number(a.price || 0),
          String(a.seller || ''),
          String(a.phone || ''),
          String(a.city || ''),
          String(a.province || ''),
          String(a.region || ''),
          req.auth.uid,
          String(a.ownerName || ''),
          status,
          String(a.desc || ''),
          String(a.section || 'hw'),
          Number(a.sortOrder || Date.now())
        ]
      );
      broadcastSync(['hw_ads']);
      return res.status(201).json({ ok: true, id });
    } catch (err) {
      return next(err);
    }
  });

  router.patch('/hw-ads/:id/price', async (req, res, next) => {
    try {
      const price = Number(req.body?.price || 0);
      if (req.isAdmin) {
        await db.pool.query('UPDATE hw_ads SET price = $1 WHERE id = $2', [price, req.params.id]);
      } else {
        const result = await db.pool.query('UPDATE hw_ads SET price = $1 WHERE id = $2 AND owner_id = $3', [
          price,
          req.params.id,
          req.auth.uid
        ]);
        if (result.rowCount === 0) return res.status(403).json({ error: 'دسترسی مجاز نیست.' });
      }
      broadcastSync(['hw_ads']);
      return res.json({ ok: true });
    } catch (err) {
      return next(err);
    }
  });

  router.patch('/hw-ads/:id/status', requireAdmin, async (req, res, next) => {
    try {
      await db.pool.query('UPDATE hw_ads SET status = $1 WHERE id = $2', [
        String(req.body?.status || 'approved'),
        req.params.id
      ]);
      broadcastSync(['hw_ads']);
      return res.json({ ok: true });
    } catch (err) {
      return next(err);
    }
  });

  router.delete('/hw-ads/:id', async (req, res, next) => {
    try {
      if (req.isAdmin) {
        await db.pool.query('DELETE FROM hw_ads WHERE id = $1', [req.params.id]);
      } else {
        const result = await db.pool.query('DELETE FROM hw_ads WHERE id = $1 AND owner_id = $2', [
          req.params.id,
          req.auth.uid
        ]);
        if (result.rowCount === 0) return res.status(403).json({ error: 'دسترسی مجاز نیست.' });
      }
      broadcastSync(['hw_ads']);
      return res.json({ ok: true });
    } catch (err) {
      return next(err);
    }
  });

  router.post('/hw-requests', async (req, res, next) => {
    try {
      const r = req.body || {};
      const id = String(r.id || generateUid('hr_'));
      await db.pool.query(
        `INSERT INTO hw_requests (id, brand, model, part_type, series, city, user_id, user_name, status, note, sort_order)
         VALUES ($1,$2,$3,$4,$5,$6,$7,$8,$9,$10,$11)`,
        [
          id,
          String(r.brand || ''),
          String(r.model || ''),
          String(r.partType || ''),
          String(r.series || ''),
          String(r.city || ''),
          req.auth.uid,
          String(r.userName || ''),
          String(r.status || ''),
          String(r.note || ''),
          Number(r.sortOrder || Date.now())
        ]
      );
      broadcastSync(['hw_requests']);
      return res.status(201).json({ ok: true, id });
    } catch (err) {
      return next(err);
    }
  });

  router.post('/hw-requests/:id/reply', requireAdmin, async (req, res, next) => {
    try {
      await db.pool.query(
        "UPDATE hw_requests SET note = $1, status = 'پاسخ داده شده توسط ادمین' WHERE id = $2",
        [String(req.body?.note || ''), req.params.id]
      );
      broadcastSync(['hw_requests']);
      return res.json({ ok: true });
    } catch (err) {
      return next(err);
    }
  });

  // ==================== ORDERS ====================

  router.post('/orders', async (req, res, next) => {
    try {
      const o = req.body || {};
      const id = String(o.id || generateUid('ORD-'));
      await db.pool.query(
        `INSERT INTO orders (id, user_id, user_name, items, total, delivery_type, customer, status, date, city, sort_order)
         VALUES ($1,$2,$3,$4::jsonb,$5,$6,$7::jsonb,$8,$9,$10,$11)`,
        [
          id,
          req.auth.uid,
          String(o.userName || ''),
          JSON.stringify(Array.isArray(o.items) ? o.items : []),
          Number(o.total || 0),
          String(o.deliveryType || 'پیک'),
          JSON.stringify(o.customer || {}),
          String(o.status || 'ارسال شده به فروشگاه'),
          String(o.date || 'امروز'),
          String(o.city || ''),
          Number(o.sortOrder || Date.now())
        ]
      );
      broadcastSync(['orders']);
      return res.status(201).json({ ok: true, id });
    } catch (err) {
      return next(err);
    }
  });

  router.patch('/orders/:id/status', requireAdmin, async (req, res, next) => {
    try {
      await db.pool.query('UPDATE orders SET status = $1 WHERE id = $2', [
        String(req.body?.status || ''),
        req.params.id
      ]);
      broadcastSync(['orders']);
      return res.json({ ok: true });
    } catch (err) {
      return next(err);
    }
  });

  router.delete('/orders/:id', requireAdmin, async (req, res, next) => {
    try {
      await db.pool.query('DELETE FROM orders WHERE id = $1', [req.params.id]);
      broadcastSync(['orders']);
      return res.json({ ok: true });
    } catch (err) {
      return next(err);
    }
  });

  // ==================== CHATS ====================

  router.post('/chats/:threadId/messages', async (req, res, next) => {
    try {
      const threadId = String(req.params.threadId || '').trim();
      const { adId, buyerUid, sellerUid, newMessage } = req.body || {};
      if (!newMessage || !newMessage.text) {
        return res.status(400).json({ error: 'متن پیام الزامی است.' });
      }

      const existing = await db.pool.query('SELECT * FROM chats WHERE id = $1 LIMIT 1', [threadId]);
      const msgPayload = {
        senderUid: req.auth.uid,
        senderName: String(newMessage.senderName || req.liveUser?.fullName || 'همکار بازمو'),
        text: String(newMessage.text).trim().slice(0, 2000),
        sentAtMs: Number(newMessage.sentAtMs || Date.now())
      };

      if (existing.rows.length === 0) {
        const bUid = req.isAdmin ? String(buyerUid || req.auth.uid) : req.auth.uid;
        const sUid = String(sellerUid || req.auth.uid);
        const participants = Array.from(new Set([bUid, sUid]));
        await db.pool.query(
          `INSERT INTO chats (id, ad_id, buyer_uid, seller_uid, participants, messages)
           VALUES ($1,$2,$3,$4,$5::jsonb,$6::jsonb)`,
          [threadId, String(adId || ''), bUid, sUid, JSON.stringify(participants), JSON.stringify([msgPayload])]
        );
      } else {
        const row = existing.rows[0];
        const participants = Array.isArray(row.participants) ? row.participants : [];
        if (!req.isAdmin && !participants.includes(req.auth.uid)) {
          return res.status(403).json({ error: 'دسترسی به این گفتگو مجاز نیست.' });
        }
        const updatedMessages = [...(Array.isArray(row.messages) ? row.messages : []), msgPayload];
        await db.pool.query('UPDATE chats SET messages = $1::jsonb, updated_at = NOW() WHERE id = $2', [
          JSON.stringify(updatedMessages),
          threadId
        ]);
      }

      broadcastSync(['chats']);
      return res.json({ ok: true });
    } catch (err) {
      return next(err);
    }
  });

  // ==================== FORUMS ====================

  router.post('/forum-posts', async (req, res, next) => {
    try {
      const p = req.body || {};
      const board = String(p.board || 'software');
      if (!canAccessForumBoard(req.userTier, req.isAdmin, board)) {
        return res.status(403).json({ error: 'شما مجاز به ارسال پست در این انجمن نیستید.' });
      }
      const id = String(p.id || generateUid('fp_'));
      await db.pool.query(
        `INSERT INTO forum_posts (id, board, flair, author, author_uid, author_badge, city, device_tag, pinned, verified_solution, title, body, votes, upvoted_by, downvoted_by, saved_by, created_at_ms, time_label, comments, published)
         VALUES ($1,$2,$3,$4,$5,$6,$7,$8,$9,$10,$11,$12,$13,$14::jsonb,$15::jsonb,$16::jsonb,$17,$18,$19::jsonb,TRUE)`,
        [
          id,
          board,
          String(p.flair || ''),
          String(p.author || ''),
          req.auth.uid,
          String(p.authorBadge || ''),
          String(p.city || ''),
          String(p.deviceTag || ''),
          req.isAdmin ? Boolean(p.pinned) : false,
          req.isAdmin ? Boolean(p.verifiedSolution) : false,
          String(p.title || ''),
          String(p.body || ''),
          Number(p.votes ?? 1),
          JSON.stringify(Array.isArray(p.upvotedBy) ? p.upvotedBy : [req.auth.uid]),
          JSON.stringify(Array.isArray(p.downvotedBy) ? p.downvotedBy : []),
          JSON.stringify(Array.isArray(p.savedBy) ? p.savedBy : []),
          Number(p.createdAtMs || Date.now()),
          String(p.timeLabel || 'هم‌اکنون'),
          JSON.stringify(Array.isArray(p.comments) ? p.comments : [])
        ]
      );
      broadcastSync(['forum_posts']);
      return res.status(201).json({ ok: true, id });
    } catch (err) {
      return next(err);
    }
  });

  router.patch('/forum-posts/:id/interaction', async (req, res, next) => {
    try {
      const existing = await db.pool.query('SELECT board FROM forum_posts WHERE id = $1 LIMIT 1', [req.params.id]);
      if (existing.rows.length === 0) {
        return res.status(404).json({ error: 'پست یافت نشد.' });
      }
      if (!canAccessForumBoard(req.userTier, req.isAdmin, existing.rows[0].board)) {
        return res.status(403).json({ error: 'دسترسی به این انجمن مجاز نیست.' });
      }
      const { votes, upvotedBy, downvotedBy, savedBy, comments } = req.body || {};
      await db.pool.query(
        `UPDATE forum_posts
         SET votes = $1,
             upvoted_by = $2::jsonb,
             downvoted_by = $3::jsonb,
             saved_by = $4::jsonb,
             comments = $5::jsonb
         WHERE id = $6`,
        [
          Number(votes || 0),
          JSON.stringify(Array.isArray(upvotedBy) ? upvotedBy : []),
          JSON.stringify(Array.isArray(downvotedBy) ? downvotedBy : []),
          JSON.stringify(Array.isArray(savedBy) ? savedBy : []),
          JSON.stringify(Array.isArray(comments) ? comments : []),
          req.params.id
        ]
      );
      broadcastSync(['forum_posts']);
      return res.json({ ok: true });
    } catch (err) {
      return next(err);
    }
  });

  router.patch('/forum-posts/:id/moderation', requireAdmin, async (req, res, next) => {
    try {
      await db.pool.query(
        'UPDATE forum_posts SET pinned = $1, verified_solution = $2 WHERE id = $3',
        [Boolean(req.body?.pinned), Boolean(req.body?.verifiedSolution), req.params.id]
      );
      broadcastSync(['forum_posts']);
      return res.json({ ok: true });
    } catch (err) {
      return next(err);
    }
  });

  router.delete('/forum-posts/:id', async (req, res, next) => {
    try {
      if (req.isAdmin) {
        await db.pool.query('DELETE FROM forum_posts WHERE id = $1', [req.params.id]);
      } else {
        const result = await db.pool.query('DELETE FROM forum_posts WHERE id = $1 AND author_uid = $2', [
          req.params.id,
          req.auth.uid
        ]);
        if (result.rowCount === 0) return res.status(403).json({ error: 'دسترسی مجاز نیست.' });
      }
      broadcastSync(['forum_posts']);
      return res.json({ ok: true });
    } catch (err) {
      return next(err);
    }
  });

  // ==================== DESK RENTALS & COOPERATION REQUESTS ====================

  router.post('/desk-rentals', async (req, res, next) => {
    try {
      const d = req.body || {};
      const id = String(d.id || generateUid('dr_'));
      const status = req.isAdmin ? String(d.status || 'approved') : 'pending';
      await db.pool.query(
        `INSERT INTO desk_rentals (id, title, category, store_name, province, city, region, rent_daily, rent_monthly, cooperation_model, equipment, phone, owner_id, owner_name, status, description, is_demo, sort_order)
         VALUES ($1,$2,$3,$4,$5,$6,$7,$8,$9,$10,$11,$12,$13,$14,$15,$16,FALSE,$17)`,
        [
          id,
          String(d.title || '').trim().slice(0, 180),
          String(d.category || 'تعمیرات سخت‌افزار'),
          String(d.storeName || ''),
          String(d.province || 'تهران'),
          String(d.city || 'تهران'),
          String(d.region || 'همه مناطق'),
          Number(d.rentDaily || 0),
          Number(d.rentMonthly || 0),
          String(d.cooperationModel || ''),
          String(d.equipment || ''),
          String(d.phone || ''),
          req.auth.uid,
          String(d.ownerName || req.liveUser?.fullName || ''),
          status,
          String(d.desc || ''),
          Number(d.sortOrder || Date.now())
        ]
      );
      broadcastSync(['desk_rentals']);
      return res.status(201).json({ ok: true, id, status });
    } catch (err) {
      return next(err);
    }
  });

  router.patch('/desk-rentals/:id/status', requireAdmin, async (req, res, next) => {
    try {
      const status = String(req.body?.status || 'approved');
      await db.pool.query('UPDATE desk_rentals SET status = $1 WHERE id = $2', [status, req.params.id]);
      broadcastSync(['desk_rentals']);
      return res.json({ ok: true });
    } catch (err) {
      return next(err);
    }
  });

  router.delete('/desk-rentals/:id', async (req, res, next) => {
    try {
      if (req.isAdmin) {
        await db.pool.query('DELETE FROM desk_rentals WHERE id = $1', [req.params.id]);
      } else {
        const result = await db.pool.query('DELETE FROM desk_rentals WHERE id = $1 AND owner_id = $2', [
          req.params.id,
          req.auth.uid
        ]);
        if (result.rowCount === 0) return res.status(403).json({ error: 'دسترسی مجاز نیست.' });
      }
      broadcastSync(['desk_rentals']);
      return res.json({ ok: true });
    } catch (err) {
      return next(err);
    }
  });

  router.post('/desk-requests', async (req, res, next) => {
    try {
      const r = req.body || {};
      const id = String(r.id || generateUid('drq_'));
      await db.pool.query(
        `INSERT INTO desk_requests (id, title, category, requester_name, requester_phone, province, city, region, proposed_budget, proposal_text, owner_id, status, is_demo, sort_order)
         VALUES ($1,$2,$3,$4,$5,$6,$7,$8,$9,$10,$11,'open',FALSE,$12)`,
        [
          id,
          String(r.title || '').trim().slice(0, 180),
          String(r.category || 'تعمیرات سخت‌افزار'),
          String(r.requesterName || req.liveUser?.fullName || 'همکار بازمو'),
          String(r.requesterPhone || req.liveUser?.phone || ''),
          String(r.province || 'تهران'),
          String(r.city || 'تهران'),
          String(r.region || 'همه مناطق'),
          Number(r.proposedBudget || 0),
          String(r.proposalText || ''),
          req.auth.uid,
          Number(r.sortOrder || Date.now())
        ]
      );
      broadcastSync(['desk_requests']);
      return res.status(201).json({ ok: true, id });
    } catch (err) {
      return next(err);
    }
  });

  router.patch('/desk-requests/:id/status', async (req, res, next) => {
    try {
      const status = String(req.body?.status || 'open');
      if (req.isAdmin) {
        await db.pool.query('UPDATE desk_requests SET status = $1 WHERE id = $2', [status, req.params.id]);
      } else {
        const result = await db.pool.query('UPDATE desk_requests SET status = $1 WHERE id = $2 AND owner_id = $3', [
          status,
          req.params.id,
          req.auth.uid
        ]);
        if (result.rowCount === 0) return res.status(403).json({ error: 'دسترسی مجاز نیست.' });
      }
      broadcastSync(['desk_requests']);
      return res.json({ ok: true });
    } catch (err) {
      return next(err);
    }
  });

  router.delete('/desk-requests/:id', async (req, res, next) => {
    try {
      if (req.isAdmin) {
        await db.pool.query('DELETE FROM desk_requests WHERE id = $1', [req.params.id]);
      } else {
        const result = await db.pool.query('DELETE FROM desk_requests WHERE id = $1 AND owner_id = $2', [
          req.params.id,
          req.auth.uid
        ]);
        if (result.rowCount === 0) return res.status(403).json({ error: 'دسترسی مجاز نیست.' });
      }
      broadcastSync(['desk_requests']);
      return res.json({ ok: true });
    } catch (err) {
      return next(err);
    }
  });

  return router;
}

module.exports = {
  createApiRouter,
  canAccessForumBoard
};
