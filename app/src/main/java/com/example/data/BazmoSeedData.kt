package com.example.data

object BazmoSeedData {
    fun notifications(): List<NotificationEntity> = listOf(
        NotificationEntity(
            id = "nt1",
            title = "خوش‌آمدگویی صنف موبایل بازمو",
            body = "همکاران گرامی در بازار علاءالدین و جمهوری، بخش درخواست تخصصی قطعات و استعلام لحظه‌ای فعال شد.",
            type = "اطلاعیه صنفی",
            province = "تهران",
            city = "تهران",
            region = "علاءالدین و جمهوری",
            tierFilter = "همه",
            sectionFilter = "همه بخش‌ها",
            date = "امروز",
            readBy = emptyList(),
            published = true,
            sortOrder = 1000L
        )
    )

    val stores = listOf(
        StoreEntity("st1", "موبایل پایتخت", "تهران", "تهران", "علاءالدین و جمهوری", "021-66701122", "تهران، پاساژ علاءالدین، طبقه ۲، پلاک ۲۱۴", "۱۰ تا ۲۰:۳۰", "۴.۹", "mobile", "", true),
        StoreEntity("st2", "تکنو استور چارسو", "تهران", "تهران", "چارسو و فردوسی", "021-88994455", "تهران، پاساژ چارسو، واحد ۳۰۵", "۱۰:۳۰ تا ۲۱", "۴.۸", "mobile", "", true),
        StoreEntity("st3", "سیب طلایی اصفهان", "اصفهان", "اصفهان", "فردوسی و بازار موبایل", "031-32204411", "اصفهان، خیابان فردوسی، بازار موبایل", "۹:۳۰ تا ۲۰", "۴.۷", "mobile", "", true),
        StoreEntity("st4", "همراه توس مشهد", "خراسان رضوی", "مشهد", "احمدآباد", "051-37651100", "مشهد، احمدآباد، مجتمع موبایل پارس", "۱۰ تا ۲۱", "۴.۸", "mobile", "", true),
        StoreEntity("st5", "جانبی سنتر تهران", "تهران", "تهران", "علاءالدین و جمهوری", "021-66750011", "تهران، جمهوری، پاساژ علاءالدین", "۹:۳۰ تا ۲۰", "۴.۹", "acc", "", true),
        StoreEntity("st6", "اکسسوری لوکس", "اصفهان", "اصفهان", "چهارباغ بالا", "031-36612233", "اصفهان، چهارباغ بالا، مجتمع کاویان", "۱۰ تا ۲۱", "۴.۶", "acc", "", true),
        StoreEntity("st7", "گجت پلاس شیراز", "فارس", "شیراز", "ملاصدرا", "071-32304455", "شیراز، ملاصدرا، مرکز تجاری پارس", "۱۰ تا ۲۱:۳۰", "۴.۸", "both", "", true),
        StoreEntity("st8", "البرز دیجیتال", "البرز", "کرج", "طالقانی", "026-32218899", "کرج، طالقانی شمالی، برج آذر", "۱۰ تا ۲۱", "۴.۷", "both", "", true)
    )

    val mobiles = listOf(
        MobileEntity("m1", "Apple", "iPhone 15 Pro Max 256GB", "حافظه ۲۵۶ | پارت نامبر ZAA | تیتانیوم", "📱", 1, true),
        MobileEntity("m2", "Apple", "iPhone 13 128GB CH", "حافظه ۱۲۸ | دوسیم‌کارت | رجیستر رسمی", "📱", 2, true),
        MobileEntity("m3", "Samsung", "Galaxy S24 Ultra 256GB", "رم ۱۲ | قلم S-Pen | 5G", "📲", 3, true),
        MobileEntity("m4", "Samsung", "Galaxy A55 5G 256GB", "رم ۸ | نمایشگر Super AMOLED 120Hz", "📲", 4, true),
        MobileEntity("m5", "Xiaomi", "Redmi Note 13 Pro 5G", "حافظه ۲۵۶ | دوربین ۲۰۰ مگاپیکسل", "📳", 5, true),
        MobileEntity("m6", "Xiaomi", "Xiaomi 14 Ultra 512GB", "رم ۱۶ | لنز لایکا | شارژ ۹۰ وات", "📳", 6, true),
        MobileEntity("m7", "Honor", "Honor 90 5G 256GB", "رم ۱۲ | شارژر ۶۶ وات", "📱", 7, true)
    )

    val storeMobiles = listOf(
        StoreMobileEntity("sm1", "st1", "m1", 78500000L, "۱۸ ماه گارانتی شرکتی + گلس و قاب مگ‌سیف هدیه", true),
        StoreMobileEntity("sm2", "st2", "m1", 77900000L, "رجیستر شده + گارانتی مدیاپردازش + کابل اصلی", true),
        StoreMobileEntity("sm3", "st1", "m3", 66200000L, "۱۸ ماه گارانتی داریا + آداپتور ۴۵ وات هدیه", true),
        StoreMobileEntity("sm4", "st2", "m4", 21400000L, "گارانتی هماهنگ + گلس سرامیکی رایگان", true),
        StoreMobileEntity("sm5", "st1", "m5", 16800000L, "پک گلوبال + ۱۸ ماه گارانتی + قاب فابریک", true),
        StoreMobileEntity("sm6", "st3", "m1", 79100000L, "گارانتی رسمی + پاوربانک اشانتیون", true),
        StoreMobileEntity("sm7", "st3", "m3", 66800000L, "پک ویتنام + ۱۸ ماه گارانتی + گلس UV", true),
        StoreMobileEntity("sm8", "st4", "m5", 16600000L, "تحویل فوری مشهد + گارانتی ۱۸ ماهه", true),
        StoreMobileEntity("sm9", "st7", "m2", 42500000L, "۱۸ ماه گارانتی + رجیستر شده + قاب سیلیکونی", true),
        StoreMobileEntity("sm10", "st8", "m4", 21200000L, "پک ویتنام + گارانتی معتبر", true),
        StoreMobileEntity("sm11", "st2", "m6", 69900000L, "گارانتی ۱۸ ماهه + کیت عکاسی اورجینال", true),
        StoreMobileEntity("sm12", "st1", "m7", 19900000L, "۱۸ ماه گارانتی + هندزفری هدیه", true)
    )

    val accessories = listOf(
        AccessoryEntity("ac1", "قاب و گارد", "قاب سیلیکونی اصلی آیفون ۱۵ پرو", "st5", 480000L, "داخل مخمل، پشتیبانی کامل از مگ‌سیف", true),
        AccessoryEntity("ac2", "شارژر و کابل", "آداپتور ۲۵ وات اصلی سامسونگ", "st5", 950000L, "اورجینال به شرط استعلام بارکد", true),
        AccessoryEntity("ac3", "هندزفری و ایرپاد", "ایرپاد پرو ۲ تایپ‌سی اپل", "st6", 11200000L, "نسخه اصلی + گارانتی تعویض ۷ روزه", true),
        AccessoryEntity("ac4", "پاوربانک", "پاوربانک ۲۰۰۰۰ انکر فست شارژ", "st7", 2450000L, "خروجی ۲۲.۵ وات، نمایشگر دیجیتال", true),
        AccessoryEntity("ac5", "گلس و محافظ", "گلس پرایوسی سرامیکی شیائومی", "st8", 220000L, "ضد ضربه و نشکن لبه منحنی", true),
        AccessoryEntity("ac6", "شارژر و کابل", "کابل تایپ‌سی به تایپ‌سی ۶۰ وات باسئوس", "st5", 340000L, "کنفی ۱.۵ متری مقاوم", true)
    )

    val swProblems = listOf(
        SwProblemEntity("p1", "آنلاک شبکه و FRP", "🔓", true),
        SwProblemEntity("p2", "ترمیم سریال و شبکه", "📡", true),
        SwProblemEntity("p3", "فلش و نصب رام", "⚡", true),
        SwProblemEntity("p4", "اپل‌آیدی و آیکلود", "🍏", true),
        SwProblemEntity("p5", "روت و کاستوم ریکاوری", "🛠️", true),
        SwProblemEntity("p6", "رفع بوت‌لوپ و هاردبریک", "🔄", true)
    )

    fun tutorials(adminUid: String): List<TutorialEntity> = listOf(
        TutorialEntity("t1", "p1", "آموزش جامع حذف FRP جدیدترین گوشی‌های سامسونگ اندروید ۱۵", "مهندس رضایی", "۴.۹", 1200000L, "ویدئو HD + پشتیبانی ۳ ماهه تلگرام + فایل‌های تست‌پوینت", "تهران", "approved", adminUid),
        TutorialEntity("t2", "p2", "دوره صفر تا صد ترمیم سریال شیائومی مدیاتک و کوالکام", "استاد کریمی", "۴.۸", 1850000L, "آموزش آنلاین + ابزارهای اختصاصی + لایسنس تمرینی", "اصفهان", "approved", adminUid),
        TutorialEntity("t3", "p4", "رفع خطاهای اپل‌آیدی، نات‌اکتیو و بکاپ‌گیری تخصصی iOS", "آکادمی آی‌پلاس", "۴.۷", 980000L, "جزوه PDF + ۷ ساعت ویدئو + گروه رفع اشکال", "شیراز", "approved", adminUid),
        TutorialEntity("t4", "p3", "فلش تخصصی گوشی‌های آنر و هواوی با فایل‌های بردفرمور", "مهندس محمدی", "۴.۹", 850000L, "فایل‌های تست‌شده + پشتیبانی مستقیم", "مشهد", "approved", adminUid)
    )

    fun boxes(adminUid: String): List<BoxEntity> = listOf(
        BoxEntity("bx1", "باکس Z3X Pro فول اکتیو سامسونگ + الجی", 14500000L, "ابزار تعمیرات پارس", "تهران", "در حد نو با کابل‌ها", "approved", adminUid),
        BoxEntity("bx2", "دانگل UMT Pro (اکانت ۱ ساله کامل)", 5800000L, "تک‌باکس اصفهان", "اصفهان", "آکبند پلمپ", "approved", adminUid),
        BoxEntity("bx3", "باکس Pandora اصلی با اکتیویشن کامل", 19200000L, "دیجیتال تولز", "مشهد", "نو", "approved", adminUid)
    )

    val hwCategories = listOf(
        HwCategoryEntity("hc1", "تاچ و ال‌سی‌دی", "🖥️", true),
        HwCategoryEntity("hc2", "باتری اصلی", "🔋", true),
        HwCategoryEntity("hc3", "آی‌سی و هارد", "🧠", true),
        HwCategoryEntity("hc4", "فلت و سوکت شارژ", "🔌", true),
        HwCategoryEntity("hc5", "دوربین و لنز", "📷", true),
        HwCategoryEntity("hc6", "شاسی و درب پشت", "🛡️", true)
    )

    fun hwAds(adminUid: String): List<HwAdEntity> = listOf(
        HwAdEntity("ad1", "تاچ و ال‌سی‌دی", "ال‌سی‌دی اورجینال روکاری iPhone 13 Pro", "Apple", "iPhone 13 Pro", "A2638 / Global", 13800000L, "قطعه سنتر علاءالدین", "09121112233", "تهران", "تهران", "علاءالدین و جمهوری", adminUid, "مدیریت بازمو", "approved", "روکاری تضمینی بدون خط و خش و بدون تعویض گلس، تست‌شده روی برد مرجع", "hw", 4000L),
        HwAdEntity("ad2", "باتری اصلی", "باتری اصلی سامسونگ A54 (EB-BA546ABY)", "Samsung", "Galaxy A54", "SM-A546E", 1150000L, "تامین قطعه سپاهان", "09132223344", "اصفهان", "اصفهان", "فردوسی و بازار موبایل", adminUid, "مدیریت بازمو", "approved", "تاریخ تولید جدید با ۶ ماه ضمانت سلامت و سایکل صفر", "hw", 3000L),
        HwAdEntity("ad3", "آی‌سی و هارد", "آی‌سی تغذیه PM8150 کوالکام آکبند", "Xiaomi", "Poco X3 Pro", "PM8150-002", 480000L, "آی‌سی مارکت", "09123334455", "تهران", "تهران", "علاءالدین و جمهوری", adminUid, "مدیریت بازمو", "approved", "ریبال‌شده کارخانه تست‌شده ۱۰۰٪", "hw", 2000L),
        HwAdEntity("ad4", "فلت و سوکت شارژ", "برد شارژ اصلی Redmi Note 12 4G", "Xiaomi", "Redmi Note 12", "23021RAAEG", 390000L, "همراه قطعه", "09154445566", "مشهد", "خراسان رضوی", "احمدآباد", adminUid, "مدیریت بازمو", "approved", "فول آی‌سی شارژ فست فعال با میکروفون اصلی", "hw", 1000L)
    )

    fun swRequests(adminUid: String): List<SwRequestEntity> = listOf(
        SwRequestEntity("sr1", "نیاز به فایل دامپ و آموزش رفع هنگ روی لوگو A34", adminUid, "همکار آزمایشی", "تهران", "در حال بررسی توسط تیم نرم‌افزار", "", true, 1000L)
    )

    fun forumPosts(adminUid: String, now: Long = System.currentTimeMillis()): List<ForumPostEntity> = listOf(
        ForumPostEntity(
            id = "fp_sw1",
            board = "software",
            flair = "FRP و آنلاک",
            author = "مهندس_امیر",
            authorUid = adminUid,
            authorBadge = "استاد کار",
            city = "تهران",
            deviceTag = "Samsung A55 (Binary 4)",
            pinned = true,
            verifiedSolution = true,
            title = "روش تضمینی و بدون باز کردن درب پشت برای FRP سامسونگ‌های اندروید ۱۵ باینری جدید",
            body = "سلام همکاران گرامی. امروز روی ۳ دستگاه A55 و S24 با پچ امنیتی جدید تست کردیم. از طریق متد MTP جدید در Chimera و UnlockTool بدون نیاز به ISP در کمتر از ۴۰ ثانیه باز میشه. درایور رسمی سامسونگ حتماً آپدیت باشه و از کابل کوتاه اصلی استفاده کنید.",
            votes = 48,
            upvotedBy = emptyList(),
            downvotedBy = emptyList(),
            savedBy = emptyList(),
            createdAtMs = now - 7_200_000L,
            timeLabel = "۲ ساعت پیش",
            comments = listOf(
                ForumComment(
                    id = "c1",
                    author = "موبایل_شیراز",
                    badge = "تعمیرکار تایید شده",
                    text = "روی A35 باینری ۳ هم تست کردم عالی جواب داد. ممنون از اشتراک تجربه!",
                    votes = 14,
                    upvotedBy = emptyList(),
                    downvotedBy = emptyList(),
                    timeLabel = "۱ ساعت پیش",
                    replies = listOf(
                        ForumReply("r1", "مهندس_امیر", "استاد کار", "روی سری M هم همین متد جوابگو هست.", 6, emptyList(), emptyList(), "۴۵ دقیقه پیش")
                    )
                )
            ),
            published = true
        ),
        ForumPostEntity(
            id = "fp_sw2",
            board = "software",
            flair = "رام و فلش",
            author = "رضا_فلشر",
            authorUid = adminUid,
            authorBadge = "تعمیرکار تایید شده",
            city = "مشهد",
            deviceTag = "Redmi Note 12 4G",
            pinned = false,
            verifiedSolution = false,
            title = "خطای Status BROM CMD Fail هنگام فلش گوشی‌های مدیاتک جدید و راه‌حل قطعی",
            body = "اگر موقع فلش شیائومی با SP Flash Tool به ارور BROM برخوردید، ابتدا از تب Auth Bypass پورت را آزاد کنید و بلافاصله با نگه‌داشتن ولوم بالا و پایین فلش را استارت بزنید.",
            votes = 29,
            createdAtMs = now - 36_000_000L,
            timeLabel = "۱۰ ساعت پیش",
            comments = emptyList(),
            published = true
        ),
        ForumPostEntity(
            id = "fp_hw1",
            board = "hardware",
            flair = "آی‌سی و برد",
            author = "استاد_حسینی",
            authorUid = adminUid,
            authorBadge = "استاد کار",
            city = "تهران",
            deviceTag = "Poco X3 Pro / Vayu",
            pinned = true,
            verifiedSolution = true,
            title = "تجربه قطعی رفع خاموشی ناگهانی Poco X3 Pro (ریبال CPU و رم با شابلون 0.12)",
            body = "در سری Vayu اغلب به دلیل دمای بالا، زیر پایه رم یا CPU قلع‌مردگی رخ میده و گوشی روی جریان ۸۰ میلی‌آمپر ثابت میمونه. با خمیر قلع ۱۸۳ درجه و دمای هیتر ۳۴۰ درجه (باد ۳۵) بهترین نتیجه رو گرفتم.",
            votes = 64,
            createdAtMs = now - 5_400_000L,
            timeLabel = "۹۰ دقیقه پیش",
            comments = listOf(
                ForumComment(
                    id = "hc1",
                    author = "برد_تعمیر_اصفهان",
                    badge = "تعمیرکار تایید شده",
                    text = "کاملاً درسته. حتماً موقع چسب‌برداری اطراف CPU مراقب مقاومت‌های مسیر I2C باشید.",
                    votes = 21,
                    timeLabel = "۵۰ دقیقه پیش",
                    replies = emptyList()
                )
            ),
            published = true
        ),
        ForumPostEntity(
            id = "fp_hw2",
            board = "hardware",
            flair = "عیب‌یابی جریان",
            author = "الکترونیک_پلاس",
            authorUid = adminUid,
            authorBadge = "تعمیرکار تایید شده",
            city = "اصفهان",
            deviceTag = "iPhone 13 Pro",
            pinned = false,
            verifiedSolution = false,
            title = "جریان‌کشی ۱۱۰ میلی‌آمپر قبل از پاور در آیفون ۱۳ پرو بعد از ضربه",
            body = "گوشی بعد از ضربه روشن نمیشه و با منبع تغذیه قبل از زدن کلید پاور ۱۱۰ میلی‌آمپر ثابته. دوربین حرارتی روی برد بالایی خط VDD_MAIN رو داغ نشون میده. راهنمایی می‌فرمایید؟",
            votes = 19,
            createdAtMs = now - 25_000_000L,
            timeLabel = "۷ ساعت پیش",
            comments = emptyList(),
            published = true
        )
    )
}
