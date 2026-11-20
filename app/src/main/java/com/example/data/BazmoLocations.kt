package com.example.data

import java.text.NumberFormat
import java.util.Locale
import kotlin.random.Random

object BazmoLocations {
    val IRAN_LOCATIONS: Map<String, Map<String, List<String>>> = linkedMapOf(
        "تهران" to linkedMapOf(
            "تهران" to listOf("همه مناطق", "علاءالدین و جمهوری", "چارسو و فردوسی", "ونک و جردن", "تجریش", "صادقیه", "پاسداران"),
            "شهریار" to listOf("همه مناطق", "مرکز شهر", "اندیشه"),
            "اسلامشهر" to listOf("همه مناطق", "مرکز شهر", "قائمیه"),
            "ورامین" to listOf("همه مناطق", "مرکز شهر")
        ),
        "اصفهان" to linkedMapOf(
            "اصفهان" to listOf("همه مناطق", "فردوسی و بازار موبایل", "چهارباغ بالا", "ملکشهر", "سپاهانشهر"),
            "کاشان" to listOf("همه مناطق", "مرکز شهر"),
            "نجف\u200cآباد" to listOf("همه مناطق", "مرکز شهر")
        ),
        "خراسان رضوی" to linkedMapOf(
            "مشهد" to listOf("همه مناطق", "احمدآباد", "سجاد", "شهدا و سعدی", "وکیل\u200cآباد"),
            "نیشابور" to listOf("همه مناطق", "مرکز شهر"),
            "سبزوار" to listOf("همه مناطق", "مرکز شهر")
        ),
        "فارس" to linkedMapOf(
            "شیراز" to listOf("همه مناطق", "ملاصدرا", "معالی\u200cآباد", "داریوش و زند"),
            "مرودشت" to listOf("همه مناطق", "مرکز شهر")
        ),
        "آذربایجان شرقی" to linkedMapOf(
            "تبریز" to listOf("همه مناطق", "شهناز", "ولیعصر", "آبرسان"),
            "مراغه" to listOf("همه مناطق", "مرکز شهر")
        ),
        "البرز" to linkedMapOf(
            "کرج" to listOf("همه مناطق", "طالقانی", "گوهردشت", "مهرشهر", "فردیس")
        ),
        "مازندران" to linkedMapOf(
            "ساری" to listOf("همه مناطق", "فرهنگ", "قارن"),
            "بابل" to listOf("همه مناطق", "مدرس"),
            "آمل" to listOf("همه مناطق", "هراز")
        ),
        "خوزستان" to linkedMapOf(
            "اهواز" to listOf("همه مناطق", "نادری", "کیانپارس", "زیتون کارمندی"),
            "دزفول" to listOf("همه مناطق", "شریعتی")
        ),
        "گیلان" to linkedMapOf(
            "رشت" to listOf("همه مناطق", "مطهری", "گلسار", "شهرداری")
        ),
        "قم" to linkedMapOf(
            "قم" to listOf("همه مناطق", "صفائیه", "باجک", "بلوار امین")
        )
    )

    const val ALL_COUNTRY = "همه کشور"
    const val ALL_CITIES = "همه شهرها"
    const val ALL_REGIONS = "همه مناطق"

    fun provinces(): List<String> = IRAN_LOCATIONS.keys.toList()

    fun provincesWithAll(): List<String> = listOf(ALL_COUNTRY) + provinces()

    fun citiesFor(province: String): List<String> =
        IRAN_LOCATIONS[province]?.keys?.toList() ?: emptyList()

    fun citiesWithAllFor(province: String): List<String> =
        if (isAllScope(province)) listOf(ALL_CITIES) else listOf(ALL_CITIES) + citiesFor(province)

    fun regionsFor(province: String, city: String): List<String> =
        IRAN_LOCATIONS[province]?.get(city) ?: listOf(ALL_REGIONS)

    fun regionsWithAllFor(province: String, city: String): List<String> =
        if (isAllScope(province) || isAllScope(city)) listOf(ALL_REGIONS) else regionsFor(province, city)

    fun toEnglishDigits(value: String?): String {
        val raw = value ?: return ""
        val sb = StringBuilder(raw.length)
        for (ch in raw) {
            when (ch) {
                in '۰'..'۹' -> sb.append(('0'.code + (ch.code - '۰'.code)).toChar())
                in '٠'..'٩' -> sb.append(('0'.code + (ch.code - '٠'.code)).toChar())
                else -> sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun normalizeZwnj(value: String?): String =
        toEnglishDigits(value)
            .replace("\u200c", "")
            .replace("ي", "ی")
            .replace("ك", "ک")
            .replace(Regex("\\s+"), "")
            .trim()

    fun isAllScope(value: String?): Boolean {
        val norm = normalizeZwnj(value)
        return norm.isEmpty() ||
            norm.equals("all", ignoreCase = true) ||
            norm == "همه" ||
            norm == "همهاستانها" ||
            norm == "همهشهرها" ||
            norm == "همهشهرهایاستان" ||
            norm == "همهمناطق" ||
            norm == "همهمناطقشهر" ||
            norm == "همهبخشها" ||
            norm == "همهنقشها" ||
            norm == "همهوضعیتها" ||
            norm == "همهکشور" ||
            norm == "کلکشور" ||
            norm == "سراسرکشور"
    }
}

data class LocationPoint(
    val province: String,
    val city: String,
    val region: String
)

data class LocationFilterResult<T>(
    val list: List<T>,
    val scope: String
)

fun <T> filterByLocation(
    items: List<T>,
    province: String,
    city: String,
    region: String,
    getLoc: (T) -> LocationPoint
): LocationFilterResult<T> {
    if (BazmoLocations.isAllScope(province)) {
        return LocationFilterResult(items, "سراسر کشور")
    }
    val normCity = BazmoLocations.normalizeZwnj(city)
    val normProv = BazmoLocations.normalizeZwnj(province)
    val normReg = BazmoLocations.normalizeZwnj(region)

    if (!BazmoLocations.isAllScope(city)) {
        if (!BazmoLocations.isAllScope(region)) {
            val exactRegion = items.filter {
                val l = getLoc(it)
                BazmoLocations.normalizeZwnj(l.city) == normCity &&
                    BazmoLocations.normalizeZwnj(l.region) == normReg
            }
            if (exactRegion.isNotEmpty()) {
                return LocationFilterResult(exactRegion, "$city ($region)")
            }
        }
        val exactCity = items.filter { BazmoLocations.normalizeZwnj(getLoc(it).city) == normCity }
        if (exactCity.isNotEmpty()) {
            return LocationFilterResult(exactCity, "شهر $city")
        }
    }
    val sameProv = items.filter { BazmoLocations.normalizeZwnj(getLoc(it).province) == normProv }
    if (sameProv.isNotEmpty()) {
        return LocationFilterResult(sameProv, "استان $province")
    }
    return LocationFilterResult(items, "سراسر کشور")
}

fun String.toPersianDigits(): String {
    val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    val sb = StringBuilder(length)
    for (ch in this) {
        if (ch in '0'..'9') sb.append(persianDigits[ch - '0'])
        else sb.append(ch)
    }
    return sb.toString()
}

fun Int.toPersianNumber(): String = this.toString().toPersianDigits()

fun Long.formatToman(): String {
    val formatted = NumberFormat.getNumberInstance(Locale.US).format(this)
    return "${formatted.toPersianDigits()} تومان"
}

fun generateUid(prefix: String): String {
    val chars = "abcdefghijklmnopqrstuvwxyz0123456789"
    val suffix = (1..6).map { chars[Random.nextInt(chars.length)] }.joinToString("")
    return "$prefix$suffix"
}

fun normalizeEmail(email: String): String = email.trim().lowercase(Locale.ROOT)

fun emailToInviteId(email: String): String = normalizeEmail(email)

private val EMAIL_DOC_REGEX = Regex("^[^/\\s@]+@[^/\\s@]+\\.[^/\\s@]+$")

fun isValidEmailFormat(email: String): Boolean {
    val norm = normalizeEmail(email)
    return norm.length in 5..200 && EMAIL_DOC_REGEX.matches(norm)
}

object BazmoRoles {
    const val REPAIR_TECH = "تعمیرکار"
    const val SOFTWARE_TECH = "نرم\u200cافزارکار"
    const val MOBILE_SELLER = "فروشنده موبایل"
    const val ACCESSORY_SELLER = "فروشنده جانبی"
    const val WHOLESALE_MOBILE = "عمده\u200cفروش موبایل"
    const val WHOLESALE_ACCESSORY = "عمده\u200cفروش جانبی"
    const val PART_SELLER = "قطعه‌فروش"

    val ALL_ROLES: List<String> = listOf(
        REPAIR_TECH,
        SOFTWARE_TECH,
        MOBILE_SELLER,
        ACCESSORY_SELLER,
        WHOLESALE_MOBILE,
        WHOLESALE_ACCESSORY,
        PART_SELLER
    )

    val STORE_CREATOR_ROLES: Set<String> = setOf(
        WHOLESALE_MOBILE,
        WHOLESALE_ACCESSORY,
        PART_SELLER
    )

    fun normalizeTier(tier: String): String {
        if (tier in ALL_ROLES) return tier
        val norm = BazmoLocations.normalizeZwnj(tier)
        return ALL_ROLES.find { BazmoLocations.normalizeZwnj(it) == norm } ?: REPAIR_TECH
    }

    fun canCreateStore(tier: String, isAdmin: Boolean): Boolean {
        if (isAdmin) return true
        val norm = BazmoLocations.normalizeZwnj(tier)
        return STORE_CREATOR_ROLES.any { BazmoLocations.normalizeZwnj(it) == norm }
    }

    fun canCreateStoreInSection(tier: String, isAdmin: Boolean, section: String): Boolean {
        if (isAdmin) return true
        if (!canCreateStore(tier, false)) return false
        return defaultStoreTypeForRole(tier) == section
    }

    fun canAccessHardwareForum(tier: String, isAdmin: Boolean): Boolean =
        isAdmin || BazmoLocations.normalizeZwnj(tier) == BazmoLocations.normalizeZwnj(REPAIR_TECH)

    fun canAccessSoftwareSpecialized(tier: String, isAdmin: Boolean): Boolean =
        isAdmin || BazmoLocations.normalizeZwnj(tier) == BazmoLocations.normalizeZwnj(SOFTWARE_TECH)

    fun defaultStoreTypeForRole(tier: String): String = when (normalizeTier(tier)) {
        WHOLESALE_MOBILE -> "mobile"
        WHOLESALE_ACCESSORY -> "acc"
        PART_SELLER -> "hw"
        else -> "both"
    }

    fun storeTypeLabel(type: String): String = when (type) {
        "mobile" -> "موبایل"
        "acc" -> "لوازم جانبی"
        "hw" -> "قطعات سخت‌افزار"
        else -> "موبایل و جانبی"
    }
}

