const { pool, ensureSchemaReady } = require('./db');

const BOOTSTRAP_ADMIN_EMAIL = 'majid.boushi77@gmail.com';
const BOOTSTRAP_ADMIN_HASH =
  'af476af2a2209576ced140ad0ba428ad:60b69d7338dce8db8d0590fac7a5d327d7e5c5a40a6d5b18ae25561df0b1e3d15fa91d2665f8ca4aeb50836d50396c876476519c1f07e32c9d2ab915792af8d6';

const IRAN_MARKET_REGIONS = [
  { province: 'تهران', city: 'تهران', region: 'علاءالدین و جمهوری', areaCode: '021', street: 'خیابان جمهوری، پاساژ علاءالدین' },
  { province: 'تهران', city: 'تهران', region: 'چارسو و فردوسی', areaCode: '021', street: 'تقاطع حافظ، مجتمع چارسو' },
  { province: 'تهران', city: 'تهران', region: 'ونک و جردن', areaCode: '021', street: 'میدان ونک، برج نگار' },
  { province: 'تهران', city: 'تهران', region: 'تجریش', areaCode: '021', street: 'میدان تجریش، پاساژ قائم' },
  { province: 'تهران', city: 'تهران', region: 'صادقیه', areaCode: '021', street: 'فلکه دوم صادقیه، مجتمع گلدیس' },
  { province: 'تهران', city: 'تهران', region: 'پاسداران', areaCode: '021', street: 'خیابان پاسداران، مرکز خرید پریس' },
  { province: 'تهران', city: 'شهریار', region: 'مرکز شهر', areaCode: '021', street: 'خیابان ولیعصر شهریار، پاساژ الماس' },
  { province: 'تهران', city: 'شهریار', region: 'اندیشه', areaCode: '021', street: 'فاز یک اندیشه، بلوار دنیا مالی' },
  { province: 'تهران', city: 'اسلامشهر', region: 'مرکز شهر', areaCode: '021', street: 'خیابان باغ فیض، پاساژ میلاد نور' },
  { province: 'تهران', city: 'اسلامشهر', region: 'قائمیه', areaCode: '021', street: 'شهرک قائمیه، خیابان اصلی' },
  { province: 'تهران', city: 'ورامین', region: 'مرکز شهر', areaCode: '021', street: 'میدان امام خمینی، پاساژ گلستان' },

  { province: 'اصفهان', city: 'اصفهان', region: 'فردوسی و بازار موبایل', areaCode: '031', street: 'خیابان فردوسی، بازار بزرگ موبایل' },
  { province: 'اصفهان', city: 'اصفهان', region: 'چهارباغ بالا', areaCode: '031', street: 'چهارباغ بالا، مجتمع کاویان' },
  { province: 'اصفهان', city: 'اصفهان', region: 'ملکشهر', areaCode: '031', street: 'خیابان رباط سوم، مجتمع نور' },
  { province: 'اصفهان', city: 'اصفهان', region: 'سپاهانشهر', areaCode: '031', street: 'بلوار غدیر، مجتمع تجاری سپاهان' },
  { province: 'اصفهان', city: 'کاشان', region: 'مرکز شهر', areaCode: '031', street: 'خیابان بهشتی، بازار موبایل کاشان' },
  { province: 'اصفهان', city: 'نجف\u200cآباد', areaCode: '031', region: 'مرکز شهر', street: 'خیابان امام خمینی، پاساژ ولیعصر' },

  { province: 'خراسان رضوی', city: 'مشهد', region: 'احمدآباد', areaCode: '051', street: 'بلوار احمدآباد، مجتمع موبایل پارس' },
  { province: 'خراسان رضوی', city: 'مشهد', region: 'سجاد', areaCode: '051', street: 'بلوار سجاد، برج تجاری تابان' },
  { province: 'خراسان رضوی', city: 'مشهد', region: 'شهدا و سعدی', areaCode: '051', street: 'خیابان سعدی، مجتمع تجاری سعدی' },
  { province: 'خراسان رضوی', city: 'مشهد', region: 'وکیل\u200cآباد', areaCode: '051', street: 'بلوار وکیل‌آباد، مجتمع آرمیتاژ' },
  { province: 'خراسان رضوی', city: 'نیشابور', region: 'مرکز شهر', areaCode: '051', street: 'خیابان امام، پاساژ مهدوی' },
  { province: 'خراسان رضوی', city: 'سبزوار', region: 'مرکز شهر', areaCode: '051', street: 'خیابان بیهق، پاساژ آریا' },

  { province: 'فارس', city: 'شیراز', region: 'ملاصدرا', areaCode: '071', street: 'خیابان ملاصدرا، مرکز تجاری پارس' },
  { province: 'فارس', city: 'شیراز', region: 'معالی\u200cآباد', areaCode: '071', street: 'بلوار معالی‌آباد، مجتمع آفتاب' },
  { province: 'فارس', city: 'شیراز', region: 'داریوش و زند', areaCode: '071', street: 'خیابان داریوش، بازار موبایل فارس' },
  { province: 'فارس', city: 'مرودشت', region: 'مرکز شهر', areaCode: '071', street: 'خیابان انقلاب، پاساژ ستاره' },

  { province: 'آذربایجان شرقی', city: 'تبریز', region: 'شهناز', areaCode: '041', street: 'خیابان شریعتی (شهناز)، بازار موبایل کریستال' },
  { province: 'آذربایجان شرقی', city: 'تبریز', region: 'ولیعصر', areaCode: '041', street: 'کوی ولیعصر، برج شهر' },
  { province: 'آذربایجان شرقی', city: 'تبریز', region: 'آبرسان', areaCode: '041', street: 'چهارراه آبرسان، مرکز تجاری اسکان' },
  { province: 'آذربایجان شرقی', city: 'مراغه', region: 'مرکز شهر', areaCode: '041', street: 'خیابان خواجه‌نصیر، پاساژ صدف' },

  { province: 'البرز', city: 'کرج', region: 'طالقانی', areaCode: '026', street: 'خیابان طالقانی شمالی، برج آذر' },
  { province: 'البرز', city: 'کرج', region: 'گوهردشت', areaCode: '026', street: 'فلکه اول گوهردشت، مجتمع نیکامال' },
  { province: 'البرز', city: 'کرج', region: 'مهرشهر', areaCode: '026', street: 'بلوار ارم مهرشهر، مرکز تجاری شقایق' },
  { province: 'البرز', city: 'کرج', region: 'فردیس', areaCode: '026', street: 'فلکه دوم فردیس، پاساژ طلا' },

  { province: 'مازندران', city: 'ساری', region: 'فرهنگ', areaCode: '011', street: 'خیابان فرهنگ، مجتمع دنیای موبایل' },
  { province: 'مازندران', city: 'ساری', region: 'قارن', areaCode: '011', street: 'خیابان قارن، پاساژ الهیه' },
  { province: 'مازندران', city: 'بابل', region: 'مدرس', areaCode: '011', street: 'خیابان مدرس، مجتمع شهریار' },
  { province: 'مازندران', city: 'آمل', region: 'هراز', areaCode: '011', street: 'بلوار هراز، پاساژ آفتاب' },

  { province: 'خوزستان', city: 'اهواز', region: 'نادری', areaCode: '061', street: 'خیابان نادری، پاساژ کارون' },
  { province: 'خوزستان', city: 'اهواز', region: 'کیانپارس', areaCode: '061', street: 'کیانپارس، خیابان پهلوان، مجتمع برج' },
  { province: 'خوزستان', city: 'اهواز', region: 'زیتون کارمندی', areaCode: '061', street: 'زیتون کارمندی، فلکه سنگر، مرکز سیتی‌سنتر' },
  { province: 'خوزستان', city: 'دزفول', region: 'شریعتی', areaCode: '061', street: 'خیابان شریعتی، پاساژ امین' },

  { province: 'گیلان', city: 'رشت', region: 'مطهری', areaCode: '013', street: 'خیابان مطهری، بازار بزرگ موبایل رشت' },
  { province: 'گیلان', city: 'رشت', region: 'گلسار', areaCode: '013', street: 'بلوار گلسار، برج گلسار' },
  { province: 'گیلان', city: 'رشت', region: 'شهرداری', areaCode: '013', street: 'میدان شهرداری، پاساژ مهرداد' },

  { province: 'قم', city: 'قم', region: 'صفائیه', areaCode: '025', street: 'خیابان صفائیه، مجتمع تجاری مروارید' },
  { province: 'قم', city: 'قم', region: 'باجک', areaCode: '025', street: 'خیابان باجک، پاساژ عصر جدید' },
  { province: 'قم', city: 'قم', region: 'بلوار امین', areaCode: '025', street: 'بلوار امین، مجتمع شهر دیجیتال' }
];

const SEED_SW_PROBLEMS = [
  ['p1', 'آنلاک شبکه و FRP', '🔓', true],
  ['p2', 'ترمیم سریال و شبکه', '📡', true],
  ['p3', 'فلش و نصب رام', '⚡', true],
  ['p4', 'اپل‌آیدی و آیکلود', '🍏', true],
  ['p5', 'روت و کاستوم ریکاوری', '🛠️', true],
  ['p6', 'رفع بوت‌لوپ و هاردبریک', '🔄', true]
];

const SEED_HW_CATEGORIES = [
  ['hc1', 'تاچ و ال‌سی‌دی', '🖥️', true],
  ['hc2', 'باتری اصلی', '🔋', true],
  ['hc3', 'آی‌سی و هارد', '🧠', true],
  ['hc4', 'فلت و سوکت شارژ', '🔌', true],
  ['hc5', 'دوربین و لنز', '📷', true],
  ['hc6', 'شاسی و درب پشت', '🛡️', true]
];

const SEED_MOBILES = [
  ['m1', 'Apple', 'iPhone 15 Pro Max 256GB', 'حافظه ۲۵۶ | پارت نامبر ZAA | تیتانیوم طبیعی', '📱', 1, true, true],
  ['m2', 'Apple', 'iPhone 13 128GB CH', 'حافظه ۱۲۸ | دوسیم‌کارت | رجیستر رسمی شرکتی', '📱', 2, true, true],
  ['m3', 'Samsung', 'Galaxy S24 Ultra 256GB', 'رم ۱۲ | قلم S-Pen | پک ویتنام 5G', '📲', 3, true, true],
  ['m4', 'Samsung', 'Galaxy A55 5G 256GB', 'رم ۸ | نمایشگر Super AMOLED 120Hz', '📲', 4, true, true],
  ['m5', 'Xiaomi', 'Redmi Note 13 Pro 5G', 'حافظه ۲۵۶ | دوربین ۲۰۰ مگاپیکسل | شارژ ۶۷ وات', '📳', 5, true, true],
  ['m6', 'Xiaomi', 'Xiaomi 14 Ultra 512GB', 'رم ۱۶ | لنز لایکا | شارژ ۹۰ وات', '📳', 6, true, true],
  ['m7', 'Honor', 'Honor 90 5G 256GB', 'رم ۱۲ | شارژر ۶۶ وات | نمایشگر خمیده', '📱', 7, true, true],
  ['m8', 'Samsung', 'Galaxy A35 5G 128GB', 'رم ۸ | ضدآب IP67 | گارانتی ۱۸ ماهه', '📲', 8, true, true],
  ['m9', 'Xiaomi', 'Poco X6 Pro 5G 512GB', 'رم ۱۲ | پردازنده Dimensity 8300 Ultra', '📳', 9, true, true],
  ['m10', 'Apple', 'iPhone 16 Pro 256GB', 'پارت ZAA | چیپ A18 Pro | دکمه کمرا کنترل', '📱', 10, true, true]
];

const MOBILE_BASE_PRICES = {
  m1: 78500000,
  m2: 42800000,
  m3: 66200000,
  m4: 21400000,
  m5: 16800000,
  m6: 69900000,
  m7: 19900000,
  m8: 16200000,
  m9: 21900000,
  m10: 84500000
};

const STORE_PREFIXES_MOBILE = ['موبایل پایتخت', 'تکنو استور', 'سیب طلایی', 'همراه برتر', 'آوا موبایل', 'دیجیتال پلاس'];
const STORE_PREFIXES_SUPPLY = ['جانبی و قطعه سنتر', 'اکسسوری لوکس', 'قطعه پارت', 'تامین همراه', 'آی‌سی و جانبی', 'گجت مارکت'];

const ACCESSORY_TEMPLATES = [
  { cat: 'قاب و گارد', name: 'قاب سیلیکونی مگ‌سیف اصلی آیفون ۱۵ پرو', price: 480000, desc: 'داخل مخمل نرم، حلقه مگ‌سیف قوی و ضدزردی' },
  { cat: 'شارژر و کابل', name: 'آداپتور ۲۵ وات اورجینال تایپ‌سی سامسونگ', price: 950000, desc: 'نسخه اصلی پلمپ به شرط استعلام بارکد و سریال' },
  { cat: 'هندزفری و ایرپاد', name: 'ایرپاد پرو ۲ تایپ‌سی اپل اورجینال', price: 11200000, desc: 'نسخه اصلی + ۷ روز مهلت تست و تایید اصالت' },
  { cat: 'پاوربانک', name: 'پاوربانک ۲۰۰۰۰ انکر فست شارژ ۲۲.۵ وات', price: 2450000, desc: 'نمایشگر درصد شارژ دیجیتال + کابل دو سر تایپ‌سی' },
  { cat: 'گلس و محافظ', name: 'گلس سرامیکی پرایوسی تمام‌چسب سری S و iPhone', price: 220000, desc: 'ضد ضربه نشکن با پوشش ضد اثر انگشت' },
  { cat: 'شارژر و کابل', name: 'کابل تایپ‌سی ۶۰ وات کنفی باسئوس ۱.۵ متری', price: 340000, desc: 'مناسب شارژ سریع گوشی و لپ‌تاپ با روکش بافته‌شده' },
  { cat: 'ساعت و بند', name: 'ساعت هوشمند شیائومی Watch S3 بند چرمی', price: 6850000, desc: 'صفحه AMOLED + مکالمه مستقیم + گارانتی ۱۸ ماهه' }
];

const HW_AD_TEMPLATES = [
  {
    cat: 'تاچ و ال‌سی‌دی',
    title: 'ال‌سی‌دی اورجینال روکاری iPhone 13 Pro',
    brand: 'Apple',
    model: 'iPhone 13 Pro',
    series: 'A2638 / Global',
    price: 13800000,
    section: 'hw',
    desc: 'روکاری تضمینی بدون خط و خش و بدون تعویض گلس، تست‌شده روی برد مرجع'
  },
  {
    cat: 'باتری اصلی',
    title: 'باتری اصلی سامسونگ Galaxy A54 / A55 شرکتی',
    brand: 'Samsung',
    model: 'Galaxy A54',
    series: 'EB-BA546ABY',
    price: 1150000,
    section: 'hw',
    desc: 'تاریخ تولید جدید ۲۰۲۶ با ۶ ماه ضمانت سلامت و سایکل صفر'
  },
  {
    cat: 'آی‌سی و هارد',
    title: 'آی‌سی تغذیه PM8150 کوالکام آکبند ریبال کارخانه',
    brand: 'Xiaomi',
    model: 'Poco X3 Pro',
    series: 'PM8150-002',
    price: 480000,
    section: 'hw',
    desc: 'تست‌شده ۱۰۰٪ مناسب رفع خاموشی ناگهانی و مشکلات ولتاژ'
  },
  {
    cat: 'فلت و سوکت شارژ',
    title: 'برد شارژ اصلی اورجینال Redmi Note 12 / 13',
    brand: 'Xiaomi',
    model: 'Redmi Note 12',
    series: '23021RAAEG',
    price: 390000,
    section: 'hw',
    desc: 'فول آی‌سی شارژ سریع + میکروفون HD فابریک'
  },
  {
    cat: 'دوربین و لنز',
    title: 'ماژول دوربین اصلی iPhone 14 Pro تضمینی',
    brand: 'Apple',
    model: 'iPhone 14 Pro',
    series: 'A2890 / Original',
    price: 8900000,
    section: 'hw',
    desc: 'بدون لرزش OIS و خطای پاپ‌آپ با مهلت تست ۴۸ ساعته'
  },
  {
    cat: 'آگهی همکار موبایل',
    title: 'فروش همکاری پک کامل iPhone 13 CH تعداد ۳ عددی',
    brand: 'Apple',
    model: 'iPhone 13',
    series: 'CH/A Active',
    price: 41900000,
    section: 'mobile',
    desc: 'تحویل حضوری در محل یا ارسال با تیپاکس بیمه شده، قیمت همکاری مقطوع'
  },
  {
    cat: 'هندزفری و گجت',
    title: 'پخش عمده ایرپاد طرح اصلی درجه A+ با گارانتی تعویض',
    brand: 'Accessory',
    model: 'Pro 2 ANC',
    series: 'Master Batch',
    price: 1290000,
    section: 'acc',
    desc: 'مناسب ویترین مغازه‌داران با حاشیه سود عالی و پلمپ شرکتی'
  }
];

async function setDemoVisibility(visible) {
  await pool.query(
    `INSERT INTO app_config (key, value) VALUES ('show_demo_data', $1)
     ON CONFLICT (key) DO UPDATE SET value = EXCLUDED.value`,
    [visible ? 'true' : 'false']
  );
}

async function getDemoVisibility() {
  const { rows } = await pool.query("SELECT value FROM app_config WHERE key = 'show_demo_data' LIMIT 1");
  if (rows.length === 0) return true;
  return rows[0].value !== 'false';
}

async function ensureBaseCategories(client) {
  for (const sp of SEED_SW_PROBLEMS) {
    await client.query(
      `INSERT INTO sw_problems (id, title, icon_emoji, published, is_demo)
       VALUES ($1,$2,$3,$4,FALSE) ON CONFLICT (id) DO NOTHING`,
      sp
    );
  }
  for (const hc of SEED_HW_CATEGORIES) {
    await client.query(
      `INSERT INTO hw_categories (id, name, icon_emoji, published, is_demo)
       VALUES ($1,$2,$3,$4,FALSE) ON CONFLICT (id) DO NOTHING`,
      hc
    );
  }
}

async function clearDemoDataOnly(client) {
  const demoTables = [
    'store_mobiles',
    'accessories',
    'stores',
    'mobiles',
    'tutorials',
    'sw_requests',
    'boxes',
    'hw_ads',
    'hw_requests',
    'notifications',
    'forum_posts',
    'desk_rentals',
    'desk_requests'
  ];
  for (const t of demoTables) {
    await client.query(`DELETE FROM ${t} WHERE is_demo = TRUE`);
  }
}

async function batchInsert(client, sqlPrefix, rows, colCount, chunkSize = 20) {
  if (!rows.length) return;
  for (let start = 0; start < rows.length; start += chunkSize) {
    const slice = rows.slice(start, start + chunkSize);
    const values = [];
    const placeholders = [];
    let p = 1;
    for (const r of slice) {
      const group = [];
      for (let c = 0; c < colCount; c++) {
        group.push(`$${p++}`);
        values.push(r[c]);
      }
      placeholders.push(`(${group.join(',')})`);
    }
    await client.query(`${sqlPrefix} VALUES ${placeholders.join(',')} ON CONFLICT (id) DO NOTHING`, values);
    if (start + chunkSize < rows.length) {
      await new Promise((resolve) => setImmediate(resolve));
    }
  }
}

let activeDemoSeedPromise = null;

async function writeDemoDataset(adminUid = 'system_admin') {
  if (activeDemoSeedPromise) {
    return activeDemoSeedPromise;
  }
  activeDemoSeedPromise = runDemoSeedTransaction(adminUid).finally(() => {
    activeDemoSeedPromise = null;
  });
  return activeDemoSeedPromise;
}

async function runDemoSeedTransaction(adminUid = 'system_admin') {
  await ensureSchemaReady();
  const now = Date.now();
  const client = await pool.connect();
  try {
    await client.query('BEGIN');
    await ensureBaseCategories(client);
    await clearDemoDataOnly(client);

    const storesRows = [];
    const storeMobileRows = [];
    const accessoryRows = [];
    const hwAdRows = [];
    const deskRentalRows = [];
    const deskRequestRows = [];

    const mobileIds = SEED_MOBILES.map((m) => m[0]);

    IRAN_MARKET_REGIONS.forEach((loc, idx) => {
      const n = idx + 1;
      const stMobId = `st_dm_${n}_1`;
      const stSupId = `st_dm_${n}_2`;
      const mobPrefix = STORE_PREFIXES_MOBILE[idx % STORE_PREFIXES_MOBILE.length];
      const supPrefix = STORE_PREFIXES_SUPPLY[idx % STORE_PREFIXES_SUPPLY.length];
      const regShort = loc.region === 'مرکز شهر' ? loc.city : loc.region;

      const phone1 = `${loc.areaCode}-${3200 + (idx % 600)}${10 + (idx % 89)}`;
      const phone2 = `${loc.areaCode}-${3500 + (idx % 450)}${15 + (idx % 80)}`;
      const mobPhone = `091${(idx % 9) + 1}${String(1100000 + idx * 7193).slice(0, 7)}`;

      storesRows.push([
        stMobId,
        `${mobPrefix} ${regShort}`,
        loc.province,
        loc.city,
        loc.region,
        phone1,
        `${loc.city}، ${loc.street}، پلاک ${10 + (idx % 45)}`,
        '۱۰ تا ۲۰:۳۰',
        idx % 3 === 0 ? '۴.۹' : '۴.۸',
        idx % 4 === 0 ? 'both' : 'mobile',
        '',
        'approved',
        true,
        true
      ]);

      storesRows.push([
        stSupId,
        `${supPrefix} ${regShort}`,
        loc.province,
        loc.city,
        loc.region,
        phone2,
        `${loc.city}، ${loc.street}، واحد ${20 + (idx % 35)}`,
        '۹:۳۰ تا ۲۰',
        idx % 2 === 0 ? '۴.۸' : '۴.۷',
        idx % 2 === 0 ? 'both' : 'hw',
        '',
        'approved',
        true,
        true
      ]);

      // 3 mobile price offers per mobile-selling store across every single region
      for (let k = 0; k < 3; k++) {
        const mId = mobileIds[(idx + k * 2) % mobileIds.length];
        const baseP = MOBILE_BASE_PRICES[mId] || 25000000;
        const delta = ((idx % 5) - 2) * 150000 + k * 50000;
        storeMobileRows.push([
          `sm_dm_${n}_${k}`,
          stMobId,
          mId,
          baseP + delta,
          k === 0 ? '۱۸ ماه گارانتی شرکتی + رجیستر رسمی و گلس هدیه' : 'پک اصلی پلمپ + ارسال فوری همکار',
          true,
          true
        ]);
      }

      // 2 accessories per region
      for (let k = 0; k < 2; k++) {
        const tpl = ACCESSORY_TEMPLATES[(idx + k) % ACCESSORY_TEMPLATES.length];
        const price = tpl.price + ((idx % 4) - 1) * 15000;
        accessoryRows.push([
          `ac_dm_${n}_${k}`,
          tpl.cat,
          `${tpl.name}`,
          stSupId,
          price,
          `${tpl.desc} — موجودی انبار ${loc.city}`,
          true,
          true
        ]);
      }

      // 2 realistic classified ads per region (HW / Mobile / Acc mix)
      for (let k = 0; k < 2; k++) {
        const tpl = HW_AD_TEMPLATES[(idx + k * 3) % HW_AD_TEMPLATES.length];
        hwAdRows.push([
          `ad_dm_${n}_${k}`,
          tpl.cat,
          `${tpl.title} (${regShort})`,
          tpl.brand,
          tpl.model,
          tpl.series,
          tpl.price + ((idx % 3) - 1) * 50000,
          `${supPrefix} ${regShort}`,
          mobPhone,
          loc.city,
          loc.province,
          loc.region,
          adminUid,
          `${supPrefix} ${regShort}`,
          'approved',
          `${tpl.desc} — تحویل در ${loc.region}`,
          tpl.section,
          now - (idx * 2 + k) * 600000,
          true
        ]);
      }

      // 1 desk rental per region rotating across all 4 categories
      const deskCats = ['تعمیرات سخت‌افزار', 'نرم‌افزار و فلش', 'فروش موبایل', 'لوازم جانبی'];
      const deskCat = deskCats[idx % deskCats.length];
      const coopModels = [
        'اجاره ماهانه ثابت + مشتری حضوری پاساژ',
        'همکاری درصدی ۶۰/۴۰ بدون رهن سنگین',
        'اجاره روزانه یا ماهانه با تجهیزات کامل',
        'شراکت فضای ویترین و تعمیرات تخصصی'
      ];
      const equipMap = {
        'تعمیرات سخت‌افزار': 'لوپ سه‌چشمی، هیتر و هویه Quick 861DW، منبع تغذیه ۳۰ ولت، پیش‌گرم‌کن برد',
        'نرم‌افزار و فلش': 'سیستم پرقدرت + میز کار استاندارد + اشتراک دانگل‌ها و اینترنت پرسرعت فیبر نوری',
        'فروش موبایل': 'ویترین شیشه‌ای نورپردازی‌شده در بر پاساژ + میز مذاکره و کارتخوان مغازه',
        'لوازم جانبی': 'رگال دیواری و استند شیشه‌ای اختصاصی + فضای انبار مجزا'
      };
      deskRentalRows.push([
        `dr_dm_${n}`,
        `اجاره میز ${deskCat} در ${loc.region} (${loc.city})`,
        deskCat,
        `${mobPrefix} ${regShort}`,
        loc.province,
        loc.city,
        loc.region,
        deskCat === 'فروش موبایل' ? 350000 : 250000,
        (6 + (idx % 6)) * 1000000,
        coopModels[idx % coopModels.length],
        equipMap[deskCat],
        mobPhone,
        adminUid,
        `مدیریت ${mobPrefix}`,
        'approved',
        `میز کار مجهز و تمیز داخل مغازه پررفت‌وآمد ${loc.street}. آماده عقد قرارداد همکاری با همکار متعهد و بااخلاق.`,
        true,
        now - idx * 900000
      ]);
    });

    // Desk cooperation proposals from specialists looking for a bench
    const reqSamples = [
      {
        title: 'تعمیرکار تخصصی آیفون و برد هستم — دنبال میز درصدی یا اجاره‌ای',
        category: 'تعمیرات سخت‌افزار',
        name: 'مهندس حسینی',
        phone: '09124455667',
        province: 'تهران',
        city: 'تهران',
        region: 'علاءالدین و جمهوری',
        budget: 9000000,
        proposal: 'کلیه تجهیزات از جمله لوپ مکانیک، هیتر کوئیک و منبع تغذیه همراه خودم هست. پیشنهاد همکاری ۵۰/۵۰ یا اجاره ماهانه تا ۹ میلیون با مغازه‌داران علاءالدین و چارسو.'
      },
      {
        title: 'نرم‌افزارکار حرفه‌ای با کلیه باکس‌ها و اکانت‌ها',
        category: 'نرم‌افزار و فلش',
        name: 'امیرمحمد رضایی',
        phone: '09135566778',
        province: 'اصفهان',
        city: 'اصفهان',
        region: 'فردوسی و بازار موبایل',
        budget: 6500000,
        proposal: 'لایسنس فعال Chimera، UnlockTool، UMT و Pandora دارم. برای مغازه‌داران خیابان فردوسی کار نرم‌افزار مشتریان مغازه را با تخفیف ویژه انجام می‌دهم.'
      },
      {
        title: 'فروشنده باسابقه جانبی و گلس — درخواست ویترین و میز اشتراکی',
        category: 'لوازم جانبی',
        name: 'رضا کریمی',
        phone: '09153344556',
        province: 'خراسان رضوی',
        city: 'مشهد',
        region: 'احمدآباد',
        budget: 7500000,
        proposal: 'پخش و نصب گلس و لوازم جانبی اورجینال را با موجودی کامل خودم می‌آورم و برای مغازه‌های موبایل‌فروشی یا تعمیراتی درصدی هم کار می‌کنم.'
      },
      {
        title: 'متقاضی میز فروش موبایل کارکرده و آکبند با سرمایه مستقل',
        category: 'فروش موبایل',
        name: 'سعید محمدی',
        phone: '09172233445',
        province: 'فارس',
        city: 'شیراز',
        region: 'ملاصدرا',
        budget: 12000000,
        proposal: 'آماده همکاری با فروشگاه‌های معتبر ملاصدرا و داریوش به‌صورت اجاره میز + کارمزد فروش در فاکتور.'
      },
      {
        title: 'تعمیرکار تعویض گلس و فلت — نیازمند فضای ۵ متری در کرج',
        category: 'تعمیرات سخت‌افزار',
        name: 'نوید باقری',
        phone: '09126677889',
        province: 'البرز',
        city: 'کرج',
        region: 'طالقانی',
        budget: 7000000,
        proposal: 'دستگاه لمینیت و حباب‌گیر همراه دارم؛ همکاری فوری با مغازه‌های تعمیراتی و قطعاتی طالقانی و گوهردشت.'
      },
      {
        title: 'همکار فنی نرم‌افزار و اپل‌آیدی در تبریز',
        category: 'نرم‌افزار و فلش',
        name: 'پویا شریفی',
        phone: '09141122334',
        province: 'آذربایجان شرقی',
        city: 'تبریز',
        region: 'شهناز',
        budget: 5500000,
        proposal: 'رفع خطاهای iOS، بکاپ‌گیری، فلش شیائومی و سامسونگ؛ پیشنهاد اجاره میز ثابت یا شراکت سود.'
      }
    ];
    reqSamples.forEach((rq, i) => {
      deskRequestRows.push([
        `drq_dm_${i + 1}`,
        rq.title,
        rq.category,
        rq.name,
        rq.phone,
        rq.province,
        rq.city,
        rq.region,
        rq.budget,
        rq.proposal,
        adminUid,
        'open',
        true,
        now - i * 1200000
      ]);
    });

    await batchInsert(
      client,
      `INSERT INTO mobiles (id, brand, model, specs, img_emoji, sort_order, published, is_demo)`,
      SEED_MOBILES,
      8
    );
    await batchInsert(
      client,
      `INSERT INTO stores (id, name, province, city, region, phone, address, hours, rating, type, owner_id, status, published, is_demo)`,
      storesRows,
      14
    );
    await batchInsert(
      client,
      `INSERT INTO store_mobiles (id, store_id, mobile_id, price, info, published, is_demo)`,
      storeMobileRows,
      7
    );
    await batchInsert(
      client,
      `INSERT INTO accessories (id, cat, name, store_id, price, description, published, is_demo)`,
      accessoryRows,
      8
    );
    await batchInsert(
      client,
      `INSERT INTO hw_ads (id, cat, title, brand, model, series, price, seller, phone, city, province, region, owner_id, owner_name, status, description, section, sort_order, is_demo)`,
      hwAdRows,
      19
    );
    await batchInsert(
      client,
      `INSERT INTO desk_rentals (id, title, category, store_name, province, city, region, rent_daily, rent_monthly, cooperation_model, equipment, phone, owner_id, owner_name, status, description, is_demo, sort_order)`,
      deskRentalRows,
      18
    );
    await batchInsert(
      client,
      `INSERT INTO desk_requests (id, title, category, requester_name, requester_phone, province, city, region, proposed_budget, proposal_text, owner_id, status, is_demo, sort_order)`,
      deskRequestRows,
      14
    );

    const tutorials = [
      ['t1', 'p1', 'آموزش جامع حذف FRP جدیدترین گوشی‌های سامسونگ اندروید ۱۵ و ۱۶', 'مهندس رضایی', '۴.۹', 1200000, 'ویدئو HD + پشتیبانی ۳ ماهه + فایل‌های تست‌پوینت', 'تهران', 'approved', adminUid, true],
      ['t2', 'p2', 'دوره صفر تا صد ترمیم سریال شیائومی مدیاتک و کوالکام', 'استاد کریمی', '۴.۸', 1850000, 'آموزش عملی + ابزارهای اختصاصی + لایسنس تمرینی', 'اصفهان', 'approved', adminUid, true],
      ['t3', 'p4', 'رفع خطاهای اپل‌آیدی، نات‌اکتیو و بکاپ‌گیری تخصصی iOS 18', 'آکادمی آی‌پلاس', '۴.۷', 980000, 'جزوه PDF + ۷ ساعت ویدئو + گروه رفع اشکال', 'شیراز', 'approved', adminUid, true],
      ['t4', 'p3', 'فلش تخصصی گوشی‌های آنر و هواوی با فایل‌های بردفرمور', 'مهندس محمدی', '۴.۹', 850000, 'فایل‌های تست‌شده + پشتیبانی مستقیم', 'مشهد', 'approved', adminUid, true],
      ['t5', 'p6', 'آنبریک و جیتگ هارد UFS با باکس EasyJTAG Plus و Medusa Pro 2', 'استاد توکلی', '۴.۹', 2400000, 'فایل‌های دامپ کامل + نقشه ISP پین‌اوت', 'تبریز', 'approved', adminUid, true],
      ['t6', 'p5', 'روت امن با Magisk و KernelSU بدون دستکاری بیس‌باند', 'آکادمی اندروید کرج', '۴.۸', 750000, 'پکیج ماژول‌های کاربردی + پشتیبانی ۲ ماهه', 'کرج', 'approved', adminUid, true]
    ];
    await batchInsert(
      client,
      `INSERT INTO tutorials (id, problem_id, title, teacher, rating, price, terms, city, status, owner_id, is_demo)`,
      tutorials,
      11
    );

    const boxes = [
      ['bx1', 'باکس Z3X Pro فول اکتیو سامسونگ + ال‌جی', 14500000, 'ابزار تعمیرات پارس', 'تهران', 'در حد نو با کابل‌ها', 'approved', adminUid, true],
      ['bx2', 'دانگل UMT Pro (اکانت ۱ ساله کامل)', 5800000, 'تک‌باکس اصفهان', 'اصفهان', 'آکبند پلمپ', 'approved', adminUid, true],
      ['bx3', 'باکس Pandora اصلی با اکتیویشن کامل', 19200000, 'دیجیتال تولز', 'مشهد', 'نو', 'approved', adminUid, true],
      ['bx4', 'باکس EasyJTAG Plus همراه با آداپتور UFS BGA-254', 32500000, 'ابزار تخصصی شیراز', 'شیراز', 'کاملاً سالم و کم‌کار', 'approved', adminUid, true],
      ['bx5', 'دوربین حرارتی Qianli SuperCam X برای عیب‌یابی برد', 21800000, 'آذر تولز تبریز', 'تبریز', 'آکبند با پایه', 'approved', adminUid, true]
    ];
    await batchInsert(
      client,
      `INSERT INTO boxes (id, name, price, seller, city, condition, status, owner_id, is_demo)`,
      boxes,
      9
    );

    const swReqs = [
      ['sr1', 'نیاز به فایل دامپ و آموزش رفع هنگ روی لوگو Galaxy A34 باینری ۴', adminUid, 'همکار آزمایشی', 'تهران', 'پاسخ داده شده', 'رام ۴ فایله با باینری همسان دانلود کنید و تیک BL و AP را همزمان بزنید.', true, now - 100000, true],
      ['sr2', 'درخواست فایل EFS تست‌شده Redmi Note 11 بدون پرش بیس‌باند', adminUid, 'موبایل‌پلاس اصفهان', 'اصفهان', 'پاسخ داده شده', 'فایل NVData تست‌شده در بخش آموزش‌های ترمیم سریال بارگذاری شد.', true, now - 200000, true],
      ['sr3', 'راهنمای رفع خطای iTunes Error 4013 روی آیفون ۱۲ پرو', adminUid, 'تعمیرگاه پارس شیراز', 'شیراز', 'در حال بررسی توسط تیم نرم‌افزار', '', true, now - 300000, true]
    ];
    await batchInsert(
      client,
      `INSERT INTO sw_requests (id, title, user_id, user_name, city, status, reply, published, sort_order, is_demo)`,
      swReqs,
      10
    );

    const hwReqs = [
      ['hr1', 'Apple', 'iPhone 13 Pro', 'تاچ و ال‌سی‌دی', 'A2638', 'تهران', adminUid, 'فنی علاءالدین', 'پاسخ داده شده توسط ادمین', 'در موجودی فروشگاه قطعه سنتر علاءالدین با قیمت ۱۳,۸۰۰,۰۰۰ موجود است.', now - 150000, true],
      ['hr2', 'Xiaomi', 'Poco X3 Pro', 'آی‌سی و هارد', 'PM8150', 'اصفهان', adminUid, 'تعمیرگاه نقش جهان', 'در حال پیگیری توسط همکاران', 'استعلام قیمت ارسال شد.', now - 250000, true]
    ];
    await batchInsert(
      client,
      `INSERT INTO hw_requests (id, brand, model, part_type, series, city, user_id, user_name, status, note, sort_order, is_demo)`,
      hwReqs,
      12
    );

    const notifications = [
      [
        'nt_demo_1',
        'خوش‌آمدگویی شبکه سراسری بازمو',
        'همکاران گرامی در سراسر کشور، بخش اجاره میز کار و پنل مدیریت فروشگاه هم‌اکنون فعال شد.',
        'اطلاعیه صنفی',
        'همه استان‌ها',
        'همه شهرها',
        'همه مناطق',
        'همه',
        'همه بخش‌ها',
        'امروز',
        JSON.stringify([]),
        true,
        now,
        true
      ],
      [
        'nt_demo_2',
        'فروش ویژه قطعات و تاچ آیفون در علاءالدین و فردوسی',
        'لیست قیمت همکاری ال‌سی‌دی روکاری و باتری شرکتی با گارانتی ۶ ماهه بروزرسانی شد.',
        'فرصت خرید ویژه',
        'تهران',
        'تهران',
        'علاءالدین و جمهوری',
        'همه',
        'سخت‌افزار',
        'امروز',
        JSON.stringify([]),
        true,
        now - 50000,
        true
      ]
    ];
    await batchInsert(
      client,
      `INSERT INTO notifications (id, title, body, type, province, city, region, tier_filter, section_filter, date, read_by, published, sort_order, is_demo)`,
      notifications,
      14
    );

    const forumPosts = [
      [
        'fp_sw1', 'software', 'FRP و آنلاک', 'مهندس_امیر', adminUid, 'استاد کار', 'تهران', 'Samsung A55 (Binary 4)',
        true, true,
        'روش تضمینی و بدون باز کردن درب پشت برای FRP سامسونگ‌های اندروید ۱۵ باینری جدید',
        'سلام همکاران گرامی. امروز روی ۳ دستگاه A55 و S24 با پچ امنیتی جدید تست کردیم. از طریق متد MTP جدید در Chimera و UnlockTool بدون نیاز به ISP در کمتر از ۴۰ ثانیه باز میشه. درایور رسمی سامسونگ حتماً آپدیت باشه و از کابل کوتاه اصلی استفاده کنید.',
        48, JSON.stringify([]), JSON.stringify([]), JSON.stringify([]), now - 7200000, '۲ ساعت پیش',
        JSON.stringify([
          {
            id: 'c1',
            author: 'موبایل_شیراز',
            badge: 'تعمیرکار تایید شده',
            text: 'روی A35 باینری ۳ هم تست کردم عالی جواب داد. ممنون از اشتراک تجربه!',
            votes: 14,
            upvotedBy: [],
            downvotedBy: [],
            timeLabel: '۱ ساعت پیش',
            replies: [
              { id: 'r1', author: 'مهندس_امیر', badge: 'استاد کار', text: 'روی سری M هم همین متد جوابگو هست.', votes: 6, upvotedBy: [], downvotedBy: [], timeLabel: '۴۵ دقیقه پیش' }
            ]
          },
          {
            id: 'c2',
            author: 'فنی_مشهد',
            badge: 'نرم‌افزارکار',
            text: 'حتماً آنتی‌ویروس ویندوز خاموش باشه که پورت مودم قطع نشه.',
            votes: 9,
            upvotedBy: [],
            downvotedBy: [],
            timeLabel: '۳۵ دقیقه پیش',
            replies: []
          }
        ]),
        true,
        true
      ],
      [
        'fp_sw2', 'software', 'رام و فلش', 'رضا_فلشر', adminUid, 'تعمیرکار تایید شده', 'مشهد', 'Redmi Note 12 4G',
        false, false,
        'خطای Status BROM CMD Fail هنگام فلش گوشی‌های مدیاتک جدید و راه‌حل قطعی',
        'اگر موقع فلش شیائومی با SP Flash Tool به ارور BROM برخوردید، ابتدا از تب Auth Bypass پورت را آزاد کنید و بلافاصله با نگه‌داشتن ولوم بالا و پایین فلش را استارت بزنید.',
        29, JSON.stringify([]), JSON.stringify([]), JSON.stringify([]), now - 36000000, '۱۰ ساعت پیش',
        JSON.stringify([
          {
            id: 'c3',
            author: 'نرم‌افزار_تبریز',
            badge: 'همکار بازمو',
            text: 'درایور LibUSB Filter رو هم نصب کنین روی ویندوز ۱۱ خیلی راحت می‌شناسه.',
            votes: 11,
            upvotedBy: [],
            downvotedBy: [],
            timeLabel: '۶ ساعت پیش',
            replies: []
          }
        ]),
        true,
        true
      ],
      [
        'fp_hw1', 'hardware', 'آی‌سی و برد', 'استاد_حسینی', adminUid, 'استاد کار', 'تهران', 'Poco X3 Pro / Vayu',
        true, true,
        'تجربه قطعی رفع خاموشی ناگهانی Poco X3 Pro (ریبال CPU و رم با شابلون 0.12)',
        'در سری Vayu اغلب به دلیل دمای بالا، زیر پایه رم یا CPU قلع‌مردگی رخ میده و گوشی روی جریان ۸۰ میلی‌آمپر ثابت میمونه. با خمیر قلع ۱۸۳ درجه و دمای هیتر ۳۴۰ درجه (باد ۳۵) بهترین نتیجه رو گرفتم.',
        64, JSON.stringify([]), JSON.stringify([]), JSON.stringify([]), now - 5400000, '۹۰ دقیقه پیش',
        JSON.stringify([
          {
            id: 'hc1',
            author: 'برد_تعمیر_اصفهان',
            badge: 'تعمیرکار تایید شده',
            text: 'کاملاً درسته. حتماً موقع چسب‌برداری اطراف CPU مراقب مقاومت‌های مسیر I2C باشید.',
            votes: 21,
            upvotedBy: [],
            downvotedBy: [],
            timeLabel: '۵۰ دقیقه پیش',
            replies: [
              { id: 'hr1', author: 'استاد_حسینی', badge: 'استاد کار', text: 'دقیقاً، مخصوصاً خط تغذیه VDDQ که بسیار حساسه.', votes: 8, upvotedBy: [], downvotedBy: [], timeLabel: '۳۰ دقیقه پیش' }
            ]
          }
        ]),
        true,
        true
      ],
      [
        'fp_hw2', 'hardware', 'عیب‌یابی جریان', 'الکترونیک_پلاس', adminUid, 'تعمیرکار تایید شده', 'اصفهان', 'iPhone 13 Pro',
        false, false,
        'جریان‌کشی ۱۱۰ میلی‌آمپر قبل از پاور در آیفون ۱۳ پرو بعد از ضربه',
        'گوشی بعد از ضربه روشن نمیشه و با منبع تغذیه قبل از زدن کلید پاور ۱۱۰ میلی‌آمپر ثابته. دوربین حرارتی روی برد بالایی خط VDD_MAIN رو داغ نشون میده. راهنمایی می‌فرمایید؟',
        19, JSON.stringify([]), JSON.stringify([]), JSON.stringify([]), now - 25000000, '۷ ساعت پیش',
        JSON.stringify([
          {
            id: 'hc2',
            author: 'آی‌سی_سنتر_تهران',
            badge: 'استاد کار',
            text: 'با ولتاژ ۱.۸ ولت و آمپر محدود به ۱.۵، خازن کنار کانکتور باطری و اطراف آی‌سی شارژ را با دوربین حرارتی یا رزین چک کنید.',
            votes: 17,
            upvotedBy: [],
            downvotedBy: [],
            timeLabel: '۴ ساعت پیش',
            replies: []
          }
        ]),
        true,
        true
      ],
      [
        'fp_hw3', 'hardware', 'شارژ و باتری', 'تعمیرات_ساری', adminUid, 'تعمیرکار', 'ساری', 'Samsung A54 / A34',
        false, true,
        'رفع خطای دمای پایین باتری (Thermistor) در سری A سامسونگ بدون تعویض برد اصلی',
        'بیشتر مواقع مشکل از مقاومت NTC روی برد پایینی شارژ یا سولفاته شدن پایه کانکتور FPC بین فلت و مین‌برد هست. قبل از تعویض آی‌سی شارژ حتماً مقاومت ۴۷ کیلواهم NTC رو تست بگیرید.',
        37, JSON.stringify([]), JSON.stringify([]), JSON.stringify([]), now - 48000000, 'دیروز',
        JSON.stringify([]),
        true,
        true
      ]
    ];
    await batchInsert(
      client,
      `INSERT INTO forum_posts (id, board, flair, author, author_uid, author_badge, city, device_tag, pinned, verified_solution, title, body, votes, upvoted_by, downvoted_by, saved_by, created_at_ms, time_label, comments, published, is_demo)`,
      forumPosts,
      21
    );

    await client.query(
      `INSERT INTO app_config (key, value) VALUES ('show_demo_data', 'true')
       ON CONFLICT (key) DO UPDATE SET value = EXCLUDED.value`
    );

    await client.query('COMMIT');
    return {
      stores: storesRows.length,
      storeMobiles: storeMobileRows.length,
      accessories: accessoryRows.length,
      hwAds: hwAdRows.length,
      deskRentals: deskRentalRows.length,
      deskRequests: deskRequestRows.length
    };
  } catch (err) {
    await client.query('ROLLBACK');
    throw err;
  } finally {
    client.release();
  }
}

async function writeSeedCatalog(adminUid = 'system_admin') {
  return writeDemoDataset(adminUid);
}

async function seedIfEmpty(adminUid = 'system_admin') {
  const { rows } = await pool.query('SELECT COUNT(*)::int AS cnt FROM stores');
  if (rows[0].cnt === 0) {
    await writeDemoDataset(adminUid);
    return true;
  }
  const client = await pool.connect();
  try {
    await ensureBaseCategories(client);
  } finally {
    client.release();
  }
  return false;
}

async function resetAllToDefaults(adminUid = 'system_admin') {
  const tables = [
    'stores',
    'mobiles',
    'store_mobiles',
    'accessories',
    'sw_problems',
    'tutorials',
    'sw_requests',
    'boxes',
    'hw_categories',
    'hw_ads',
    'hw_requests',
    'notifications',
    'forum_posts',
    'desk_rentals',
    'desk_requests'
  ];
  for (const t of tables) {
    await pool.query(`DELETE FROM ${t}`);
  }
  await writeDemoDataset(adminUid);
}

async function ensureBootstrapAdminPassword() {
  const existing = await pool.query('SELECT id, password_hash FROM users WHERE LOWER(email) = $1 LIMIT 1', [
    BOOTSTRAP_ADMIN_EMAIL
  ]);
  if (existing.rows.length === 0) {
    const adminId = 'web_majid_boushi77';
    await pool.query(
      `INSERT INTO users (id, uid, email, full_name, phone, tier, province, city, region, active, created_date_label, password_hash)
       VALUES ($1,$2,$3,'مدیریت بازمو','','ادمین','تهران','تهران','علاءالدین و جمهوری',TRUE,'امروز',$4)
       ON CONFLICT (id) DO NOTHING`,
      [adminId, adminId, BOOTSTRAP_ADMIN_EMAIL, BOOTSTRAP_ADMIN_HASH]
    );
  } else if (!existing.rows[0].password_hash) {
    await pool.query(
      `UPDATE users SET password_hash = $1, tier = 'ادمین', active = TRUE, updated_at = NOW()
       WHERE id = $2 AND password_hash = ''`,
      [BOOTSTRAP_ADMIN_HASH, existing.rows[0].id]
    );
  }
}

module.exports = {
  BOOTSTRAP_ADMIN_EMAIL,
  BOOTSTRAP_ADMIN_HASH,
  IRAN_MARKET_REGIONS,
  ensureBootstrapAdminPassword,
  seedIfEmpty,
  resetAllToDefaults,
  writeSeedCatalog,
  writeDemoDataset,
  setDemoVisibility,
  getDemoVisibility
};
