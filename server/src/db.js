const { Pool } = require('pg');

function createPool() {
  const connectionString = process.env.DATABASE_URL;
  if (connectionString) {
    const useSsl =
      process.env.DB_SSL === 'true' ||
      connectionString.includes('sslmode=require');
    return new Pool({
      connectionString,
      max: Number(process.env.DB_POOL_MAX || 4),
      idleTimeoutMillis: 15000,
      connectionTimeoutMillis: 15000,
      ssl: useSsl ? { rejectUnauthorized: false } : false
    });
  }

  if (process.env.DB_HOST) {
    return new Pool({
      host: process.env.DB_HOST,
      port: Number(process.env.DB_PORT || 5432),
      user: process.env.DB_USER,
      password: process.env.DB_PASSWORD,
      database: process.env.DB_NAME,
      max: Number(process.env.DB_POOL_MAX || 4),
      idleTimeoutMillis: 15000,
      connectionTimeoutMillis: 15000,
      ssl: process.env.DB_SSL === 'true' ? { rejectUnauthorized: false } : false
    });
  }

  throw new Error(
    'PostgreSQL environment variables (DATABASE_URL or DB_HOST) are not configured. Link a PostgreSQL database in Weblines.'
  );
}

const pool = createPool();

async function initSchema() {
  await pool.query(`
    CREATE TABLE IF NOT EXISTS app_config (
      key TEXT PRIMARY KEY,
      value TEXT NOT NULL
    );

    CREATE TABLE IF NOT EXISTS users (
      id TEXT PRIMARY KEY,
      uid TEXT NOT NULL,
      email TEXT,
      full_name TEXT NOT NULL DEFAULT '',
      phone TEXT NOT NULL DEFAULT '',
      tier TEXT NOT NULL DEFAULT 'تعمیرکار',
      province TEXT NOT NULL DEFAULT 'تهران',
      city TEXT NOT NULL DEFAULT 'تهران',
      region TEXT NOT NULL DEFAULT 'همه مناطق',
      active BOOLEAN NOT NULL DEFAULT TRUE,
      created_date_label TEXT NOT NULL DEFAULT 'امروز',
      created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
      updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
    );
    ALTER TABLE users ADD COLUMN IF NOT EXISTS password_hash TEXT NOT NULL DEFAULT '';
    CREATE INDEX IF NOT EXISTS idx_users_email ON users (LOWER(email));

    CREATE TABLE IF NOT EXISTS user_invites (
      email TEXT PRIMARY KEY,
      full_name TEXT NOT NULL DEFAULT '',
      phone TEXT NOT NULL DEFAULT '',
      tier TEXT NOT NULL DEFAULT 'تعمیرکار',
      province TEXT NOT NULL DEFAULT 'تهران',
      city TEXT NOT NULL DEFAULT 'تهران',
      region TEXT NOT NULL DEFAULT 'همه مناطق',
      active BOOLEAN NOT NULL DEFAULT TRUE,
      created_date_label TEXT NOT NULL DEFAULT 'امروز',
      source_user_doc_id TEXT NOT NULL DEFAULT '',
      claimed_by_uid TEXT NOT NULL DEFAULT '',
      created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
      updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
    );

    CREATE TABLE IF NOT EXISTS registration_requests (
      id TEXT PRIMARY KEY,
      email TEXT NOT NULL,
      full_name TEXT NOT NULL,
      phone TEXT NOT NULL,
      requested_tier TEXT NOT NULL,
      province TEXT NOT NULL,
      city TEXT NOT NULL,
      region TEXT NOT NULL,
      note TEXT NOT NULL DEFAULT '',
      status TEXT NOT NULL DEFAULT 'pending',
      created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
      updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
    );
    ALTER TABLE registration_requests ADD COLUMN IF NOT EXISTS password_hash TEXT NOT NULL DEFAULT '';

    CREATE TABLE IF NOT EXISTS notifications (
      id TEXT PRIMARY KEY,
      title TEXT NOT NULL,
      body TEXT NOT NULL,
      type TEXT NOT NULL,
      province TEXT NOT NULL,
      city TEXT NOT NULL,
      region TEXT NOT NULL,
      tier_filter TEXT NOT NULL,
      section_filter TEXT NOT NULL DEFAULT 'همه بخش‌ها',
      date TEXT NOT NULL DEFAULT 'امروز',
      read_by JSONB NOT NULL DEFAULT '[]'::jsonb,
      published BOOLEAN NOT NULL DEFAULT TRUE,
      sort_order BIGINT NOT NULL DEFAULT 0,
      created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
    );

    CREATE TABLE IF NOT EXISTS stores (
      id TEXT PRIMARY KEY,
      name TEXT NOT NULL,
      province TEXT NOT NULL,
      city TEXT NOT NULL,
      region TEXT NOT NULL,
      phone TEXT NOT NULL DEFAULT '',
      address TEXT NOT NULL DEFAULT '',
      hours TEXT NOT NULL DEFAULT '',
      rating TEXT NOT NULL DEFAULT '۴.۸',
      type TEXT NOT NULL DEFAULT 'both',
      owner_id TEXT NOT NULL DEFAULT '',
      status TEXT NOT NULL DEFAULT 'approved',
      published BOOLEAN NOT NULL DEFAULT TRUE,
      created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
    );
    ALTER TABLE stores ADD COLUMN IF NOT EXISTS status TEXT NOT NULL DEFAULT 'approved';

    CREATE TABLE IF NOT EXISTS mobiles (
      id TEXT PRIMARY KEY,
      brand TEXT NOT NULL,
      model TEXT NOT NULL,
      specs TEXT NOT NULL DEFAULT '',
      img_emoji TEXT NOT NULL DEFAULT '📱',
      sort_order BIGINT NOT NULL DEFAULT 0,
      published BOOLEAN NOT NULL DEFAULT TRUE,
      created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
    );

    CREATE TABLE IF NOT EXISTS store_mobiles (
      id TEXT PRIMARY KEY,
      store_id TEXT NOT NULL,
      mobile_id TEXT NOT NULL,
      price BIGINT NOT NULL,
      info TEXT NOT NULL DEFAULT '',
      published BOOLEAN NOT NULL DEFAULT TRUE,
      created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
    );

    CREATE TABLE IF NOT EXISTS accessories (
      id TEXT PRIMARY KEY,
      cat TEXT NOT NULL,
      name TEXT NOT NULL,
      store_id TEXT NOT NULL,
      price BIGINT NOT NULL,
      description TEXT NOT NULL DEFAULT '',
      published BOOLEAN NOT NULL DEFAULT TRUE,
      created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
    );

    CREATE TABLE IF NOT EXISTS inquiries (
      id TEXT PRIMARY KEY,
      acc_id TEXT NOT NULL,
      acc_name TEXT NOT NULL,
      store_name TEXT NOT NULL,
      user_id TEXT NOT NULL,
      user_name TEXT NOT NULL DEFAULT '',
      status TEXT NOT NULL,
      date TEXT NOT NULL DEFAULT 'امروز',
      sort_order BIGINT NOT NULL DEFAULT 0,
      created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
    );

    CREATE TABLE IF NOT EXISTS sw_problems (
      id TEXT PRIMARY KEY,
      title TEXT NOT NULL,
      icon_emoji TEXT NOT NULL DEFAULT '🔧',
      published BOOLEAN NOT NULL DEFAULT TRUE,
      created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
    );

    CREATE TABLE IF NOT EXISTS tutorials (
      id TEXT PRIMARY KEY,
      problem_id TEXT NOT NULL,
      title TEXT NOT NULL,
      teacher TEXT NOT NULL,
      rating TEXT NOT NULL DEFAULT '۵.۰',
      price BIGINT NOT NULL,
      terms TEXT NOT NULL DEFAULT '',
      city TEXT NOT NULL DEFAULT '',
      status TEXT NOT NULL DEFAULT 'pending',
      owner_id TEXT NOT NULL,
      created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
    );

    CREATE TABLE IF NOT EXISTS sw_requests (
      id TEXT PRIMARY KEY,
      title TEXT NOT NULL,
      user_id TEXT NOT NULL,
      user_name TEXT NOT NULL DEFAULT '',
      city TEXT NOT NULL DEFAULT '',
      status TEXT NOT NULL,
      reply TEXT NOT NULL DEFAULT '',
      published BOOLEAN NOT NULL DEFAULT TRUE,
      sort_order BIGINT NOT NULL DEFAULT 0,
      created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
    );

    CREATE TABLE IF NOT EXISTS boxes (
      id TEXT PRIMARY KEY,
      name TEXT NOT NULL,
      price BIGINT NOT NULL,
      seller TEXT NOT NULL,
      city TEXT NOT NULL DEFAULT '',
      condition TEXT NOT NULL DEFAULT '',
      status TEXT NOT NULL DEFAULT 'pending',
      owner_id TEXT NOT NULL,
      created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
    );

    CREATE TABLE IF NOT EXISTS hw_categories (
      id TEXT PRIMARY KEY,
      name TEXT NOT NULL,
      icon_emoji TEXT NOT NULL DEFAULT '🧩',
      published BOOLEAN NOT NULL DEFAULT TRUE,
      created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
    );

    CREATE TABLE IF NOT EXISTS hw_ads (
      id TEXT PRIMARY KEY,
      cat TEXT NOT NULL,
      title TEXT NOT NULL,
      brand TEXT NOT NULL DEFAULT '',
      model TEXT NOT NULL DEFAULT '',
      series TEXT NOT NULL DEFAULT '',
      price BIGINT NOT NULL,
      seller TEXT NOT NULL,
      phone TEXT NOT NULL DEFAULT '',
      city TEXT NOT NULL,
      province TEXT NOT NULL,
      region TEXT NOT NULL DEFAULT '',
      owner_id TEXT NOT NULL,
      owner_name TEXT NOT NULL DEFAULT '',
      status TEXT NOT NULL DEFAULT 'pending',
      description TEXT NOT NULL DEFAULT '',
      section TEXT NOT NULL DEFAULT 'hw',
      sort_order BIGINT NOT NULL DEFAULT 0,
      created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
    );

    CREATE TABLE IF NOT EXISTS hw_requests (
      id TEXT PRIMARY KEY,
      brand TEXT NOT NULL,
      model TEXT NOT NULL,
      part_type TEXT NOT NULL,
      series TEXT NOT NULL,
      city TEXT NOT NULL DEFAULT '',
      user_id TEXT NOT NULL,
      user_name TEXT NOT NULL DEFAULT '',
      status TEXT NOT NULL,
      note TEXT NOT NULL DEFAULT '',
      sort_order BIGINT NOT NULL DEFAULT 0,
      created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
    );

    CREATE TABLE IF NOT EXISTS orders (
      id TEXT PRIMARY KEY,
      user_id TEXT NOT NULL,
      user_name TEXT NOT NULL DEFAULT '',
      items JSONB NOT NULL DEFAULT '[]'::jsonb,
      total BIGINT NOT NULL,
      delivery_type TEXT NOT NULL DEFAULT 'پیک',
      customer JSONB NOT NULL DEFAULT '{}'::jsonb,
      status TEXT NOT NULL,
      date TEXT NOT NULL DEFAULT 'امروز',
      city TEXT NOT NULL DEFAULT '',
      sort_order BIGINT NOT NULL DEFAULT 0,
      created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
    );

    CREATE TABLE IF NOT EXISTS chats (
      id TEXT PRIMARY KEY,
      ad_id TEXT NOT NULL,
      buyer_uid TEXT NOT NULL,
      seller_uid TEXT NOT NULL,
      participants JSONB NOT NULL DEFAULT '[]'::jsonb,
      messages JSONB NOT NULL DEFAULT '[]'::jsonb,
      created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
      updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
    );

    CREATE TABLE IF NOT EXISTS forum_posts (
      id TEXT PRIMARY KEY,
      board TEXT NOT NULL,
      flair TEXT NOT NULL,
      author TEXT NOT NULL,
      author_uid TEXT NOT NULL,
      author_badge TEXT NOT NULL DEFAULT '',
      city TEXT NOT NULL DEFAULT '',
      device_tag TEXT NOT NULL DEFAULT '',
      pinned BOOLEAN NOT NULL DEFAULT FALSE,
      verified_solution BOOLEAN NOT NULL DEFAULT FALSE,
      title TEXT NOT NULL,
      body TEXT NOT NULL,
      votes INTEGER NOT NULL DEFAULT 1,
      upvoted_by JSONB NOT NULL DEFAULT '[]'::jsonb,
      downvoted_by JSONB NOT NULL DEFAULT '[]'::jsonb,
      saved_by JSONB NOT NULL DEFAULT '[]'::jsonb,
      created_at_ms BIGINT NOT NULL,
      time_label TEXT NOT NULL DEFAULT 'هم‌اکنون',
      comments JSONB NOT NULL DEFAULT '[]'::jsonb,
      published BOOLEAN NOT NULL DEFAULT TRUE,
      created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
    );

    CREATE TABLE IF NOT EXISTS desk_rentals (
      id TEXT PRIMARY KEY,
      title TEXT NOT NULL,
      category TEXT NOT NULL DEFAULT 'تعمیرات سخت‌افزار',
      store_name TEXT NOT NULL DEFAULT '',
      province TEXT NOT NULL DEFAULT 'تهران',
      city TEXT NOT NULL DEFAULT 'تهران',
      region TEXT NOT NULL DEFAULT 'همه مناطق',
      rent_daily BIGINT NOT NULL DEFAULT 0,
      rent_monthly BIGINT NOT NULL DEFAULT 0,
      cooperation_model TEXT NOT NULL DEFAULT '',
      equipment TEXT NOT NULL DEFAULT '',
      phone TEXT NOT NULL DEFAULT '',
      owner_id TEXT NOT NULL DEFAULT '',
      owner_name TEXT NOT NULL DEFAULT '',
      status TEXT NOT NULL DEFAULT 'pending',
      description TEXT NOT NULL DEFAULT '',
      is_demo BOOLEAN NOT NULL DEFAULT FALSE,
      sort_order BIGINT NOT NULL DEFAULT 0,
      created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
    );

    CREATE TABLE IF NOT EXISTS desk_requests (
      id TEXT PRIMARY KEY,
      title TEXT NOT NULL,
      category TEXT NOT NULL DEFAULT 'تعمیرات سخت‌افزار',
      requester_name TEXT NOT NULL DEFAULT '',
      requester_phone TEXT NOT NULL DEFAULT '',
      province TEXT NOT NULL DEFAULT 'تهران',
      city TEXT NOT NULL DEFAULT 'تهران',
      region TEXT NOT NULL DEFAULT 'همه مناطق',
      proposed_budget BIGINT NOT NULL DEFAULT 0,
      proposal_text TEXT NOT NULL DEFAULT '',
      owner_id TEXT NOT NULL DEFAULT '',
      status TEXT NOT NULL DEFAULT 'open',
      is_demo BOOLEAN NOT NULL DEFAULT FALSE,
      sort_order BIGINT NOT NULL DEFAULT 0,
      created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
    );

    ALTER TABLE stores ADD COLUMN IF NOT EXISTS is_demo BOOLEAN NOT NULL DEFAULT FALSE;
    ALTER TABLE mobiles ADD COLUMN IF NOT EXISTS is_demo BOOLEAN NOT NULL DEFAULT FALSE;
    ALTER TABLE store_mobiles ADD COLUMN IF NOT EXISTS is_demo BOOLEAN NOT NULL DEFAULT FALSE;
    ALTER TABLE accessories ADD COLUMN IF NOT EXISTS is_demo BOOLEAN NOT NULL DEFAULT FALSE;
    ALTER TABLE sw_problems ADD COLUMN IF NOT EXISTS is_demo BOOLEAN NOT NULL DEFAULT FALSE;
    ALTER TABLE tutorials ADD COLUMN IF NOT EXISTS is_demo BOOLEAN NOT NULL DEFAULT FALSE;
    ALTER TABLE sw_requests ADD COLUMN IF NOT EXISTS is_demo BOOLEAN NOT NULL DEFAULT FALSE;
    ALTER TABLE boxes ADD COLUMN IF NOT EXISTS is_demo BOOLEAN NOT NULL DEFAULT FALSE;
    ALTER TABLE hw_categories ADD COLUMN IF NOT EXISTS is_demo BOOLEAN NOT NULL DEFAULT FALSE;
    ALTER TABLE hw_ads ADD COLUMN IF NOT EXISTS is_demo BOOLEAN NOT NULL DEFAULT FALSE;
    ALTER TABLE hw_requests ADD COLUMN IF NOT EXISTS is_demo BOOLEAN NOT NULL DEFAULT FALSE;
    ALTER TABLE notifications ADD COLUMN IF NOT EXISTS is_demo BOOLEAN NOT NULL DEFAULT FALSE;
    ALTER TABLE forum_posts ADD COLUMN IF NOT EXISTS is_demo BOOLEAN NOT NULL DEFAULT FALSE;
    ALTER TABLE desk_rentals ADD COLUMN IF NOT EXISTS is_demo BOOLEAN NOT NULL DEFAULT FALSE;
    ALTER TABLE desk_requests ADD COLUMN IF NOT EXISTS is_demo BOOLEAN NOT NULL DEFAULT FALSE;
  `);
}

let schemaInitPromise = null;
async function ensureSchemaReady() {
  if (!schemaInitPromise) {
    schemaInitPromise = initSchema().catch((err) => {
      schemaInitPromise = null;
      throw err;
    });
  }
  return schemaInitPromise;
}

function mapUser(r) {
  return {
    id: r.id,
    uid: r.uid,
    email: r.email || '',
    fullName: r.full_name || '',
    phone: r.phone || '',
    tier: r.tier,
    province: r.province,
    city: r.city,
    region: r.region,
    active: Boolean(r.active),
    createdDateLabel: r.created_date_label || 'امروز',
    createdAtSeconds: r.created_at ? Math.floor(new Date(r.created_at).getTime() / 1000) : 0
  };
}

function mapRegistrationRequest(r) {
  return {
    id: r.id,
    email: r.email,
    fullName: r.full_name,
    phone: r.phone,
    requestedTier: r.requested_tier,
    province: r.province,
    city: r.city,
    region: r.region,
    note: r.note || '',
    status: r.status,
    createdAtSeconds: r.created_at ? Math.floor(new Date(r.created_at).getTime() / 1000) : 0
  };
}

function mapNotification(r) {
  return {
    id: r.id,
    title: r.title,
    body: r.body,
    type: r.type,
    province: r.province,
    city: r.city,
    region: r.region,
    tierFilter: r.tier_filter,
    sectionFilter: r.section_filter,
    date: r.date,
    readBy: Array.isArray(r.read_by) ? r.read_by : [],
    published: Boolean(r.published),
    sortOrder: Number(r.sort_order || 0)
  };
}

function mapStore(r) {
  return {
    id: r.id,
    name: r.name,
    province: r.province,
    city: r.city,
    region: r.region,
    phone: r.phone || '',
    address: r.address || '',
    hours: r.hours || '',
    rating: r.rating || '۴.۸',
    type: r.type || 'both',
    ownerId: r.owner_id || '',
    status: r.status || (r.published ? 'approved' : 'pending'),
    published: Boolean(r.published),
    isDemo: Boolean(r.is_demo)
  };
}

function mapMobile(r) {
  return {
    id: r.id,
    brand: r.brand,
    model: r.model,
    specs: r.specs || '',
    imgEmoji: r.img_emoji || '📱',
    sortOrder: Number(r.sort_order || 0),
    published: Boolean(r.published)
  };
}

function mapStoreMobile(r) {
  return {
    id: r.id,
    storeId: r.store_id,
    mobileId: r.mobile_id,
    price: Number(r.price || 0),
    info: r.info || '',
    published: Boolean(r.published)
  };
}

function mapAccessory(r) {
  return {
    id: r.id,
    cat: r.cat,
    name: r.name,
    storeId: r.store_id,
    price: Number(r.price || 0),
    desc: r.description || '',
    published: Boolean(r.published)
  };
}

function mapInquiry(r) {
  return {
    id: r.id,
    accId: r.acc_id,
    accName: r.acc_name,
    storeName: r.store_name,
    userId: r.user_id,
    userName: r.user_name || '',
    status: r.status,
    date: r.date || 'امروز',
    sortOrder: Number(r.sort_order || 0)
  };
}

function mapSwProblem(r) {
  return {
    id: r.id,
    title: r.title,
    iconEmoji: r.icon_emoji || '🔧',
    published: Boolean(r.published)
  };
}

function mapTutorial(r) {
  return {
    id: r.id,
    problemId: r.problem_id,
    title: r.title,
    teacher: r.teacher,
    rating: r.rating || '۵.۰',
    price: Number(r.price || 0),
    terms: r.terms || '',
    city: r.city || '',
    status: r.status,
    ownerId: r.owner_id
  };
}

function mapSwRequest(r) {
  return {
    id: r.id,
    title: r.title,
    userId: r.user_id,
    userName: r.user_name || '',
    city: r.city || '',
    status: r.status,
    reply: r.reply || '',
    published: Boolean(r.published),
    sortOrder: Number(r.sort_order || 0)
  };
}

function mapBox(r) {
  return {
    id: r.id,
    name: r.name,
    price: Number(r.price || 0),
    seller: r.seller,
    city: r.city || '',
    condition: r.condition || '',
    status: r.status,
    ownerId: r.owner_id
  };
}

function mapHwCategory(r) {
  return {
    id: r.id,
    name: r.name,
    iconEmoji: r.icon_emoji || '🧩',
    published: Boolean(r.published)
  };
}

function mapHwAd(r) {
  return {
    id: r.id,
    cat: r.cat,
    title: r.title,
    brand: r.brand || '',
    model: r.model || '',
    series: r.series || '',
    price: Number(r.price || 0),
    seller: r.seller,
    phone: r.phone || '',
    city: r.city,
    province: r.province,
    region: r.region || '',
    ownerId: r.owner_id,
    ownerName: r.owner_name || '',
    status: r.status,
    desc: r.description || '',
    section: r.section || 'hw',
    sortOrder: Number(r.sort_order || 0)
  };
}

function mapHwRequest(r) {
  return {
    id: r.id,
    brand: r.brand,
    model: r.model,
    partType: r.part_type,
    series: r.series,
    city: r.city || '',
    userId: r.user_id,
    userName: r.user_name || '',
    status: r.status,
    note: r.note || '',
    sortOrder: Number(r.sort_order || 0)
  };
}

function mapOrder(r) {
  return {
    id: r.id,
    userId: r.user_id,
    userName: r.user_name || '',
    items: Array.isArray(r.items) ? r.items : [],
    total: Number(r.total || 0),
    deliveryType: r.delivery_type || 'پیک',
    customer: r.customer || {},
    status: r.status,
    date: r.date || 'امروز',
    city: r.city || '',
    sortOrder: Number(r.sort_order || 0)
  };
}

function mapChat(r) {
  return {
    id: r.id,
    adId: r.ad_id,
    buyerUid: r.buyer_uid,
    sellerUid: r.seller_uid,
    participants: Array.isArray(r.participants) ? r.participants : [],
    messages: Array.isArray(r.messages) ? r.messages : []
  };
}

function mapForumPost(r) {
  return {
    id: r.id,
    board: r.board,
    flair: r.flair,
    author: r.author,
    authorUid: r.author_uid,
    authorBadge: r.author_badge || '',
    city: r.city || '',
    deviceTag: r.device_tag || '',
    pinned: Boolean(r.pinned),
    verifiedSolution: Boolean(r.verified_solution),
    title: r.title,
    body: r.body,
    votes: Number(r.votes || 0),
    upvotedBy: Array.isArray(r.upvoted_by) ? r.upvoted_by : [],
    downvotedBy: Array.isArray(r.downvoted_by) ? r.downvoted_by : [],
    savedBy: Array.isArray(r.saved_by) ? r.saved_by : [],
    createdAtMs: Number(r.created_at_ms || 0),
    timeLabel: r.time_label || 'هم‌اکنون',
    comments: Array.isArray(r.comments) ? r.comments : [],
    published: Boolean(r.published)
  };
}

function mapDeskRental(r) {
  return {
    id: r.id,
    title: r.title,
    category: r.category || 'تعمیرات سخت‌افزار',
    storeName: r.store_name || '',
    province: r.province || 'تهران',
    city: r.city || 'تهران',
    region: r.region || 'همه مناطق',
    rentDaily: Number(r.rent_daily || 0),
    rentMonthly: Number(r.rent_monthly || 0),
    cooperationModel: r.cooperation_model || '',
    equipment: r.equipment || '',
    phone: r.phone || '',
    ownerId: r.owner_id || '',
    ownerName: r.owner_name || '',
    status: r.status || 'pending',
    desc: r.description || '',
    isDemo: Boolean(r.is_demo),
    sortOrder: Number(r.sort_order || 0)
  };
}

function mapDeskRequest(r) {
  return {
    id: r.id,
    title: r.title,
    category: r.category || 'تعمیرات سخت‌افزار',
    requesterName: r.requester_name || '',
    requesterPhone: r.requester_phone || '',
    province: r.province || 'تهران',
    city: r.city || 'تهران',
    region: r.region || 'همه مناطق',
    proposedBudget: Number(r.proposed_budget || 0),
    proposalText: r.proposal_text || '',
    ownerId: r.owner_id || '',
    status: r.status || 'open',
    isDemo: Boolean(r.is_demo),
    sortOrder: Number(r.sort_order || 0)
  };
}

module.exports = {
  pool,
  initSchema,
  ensureSchemaReady,
  mapUser,
  mapRegistrationRequest,
  mapNotification,
  mapStore,
  mapMobile,
  mapStoreMobile,
  mapAccessory,
  mapInquiry,
  mapSwProblem,
  mapTutorial,
  mapSwRequest,
  mapBox,
  mapHwCategory,
  mapHwAd,
  mapHwRequest,
  mapOrder,
  mapChat,
  mapForumPost,
  mapDeskRental,
  mapDeskRequest
};
