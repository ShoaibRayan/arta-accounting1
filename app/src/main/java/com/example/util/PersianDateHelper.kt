package com.example.util

import java.util.Calendar
import java.util.Locale

enum class AppCalendarType(val code: String, val displayName: String, val description: String) {
    SOLAR_DARI("SOLAR_DARI", "هجری شمسی (دری افغانستان)", "حمل، ثور، جوزا، سرطان، اسد، سنبله..."),
    SOLAR_IRANIAN("SOLAR_IRANIAN", "هجری شمسی (ایرانی)", "فروردین، اردیبهشت، خرداد، تیر، مرداد..."),
    GREGORIAN("GREGORIAN", "میلادی (Gregorian)", "ژانویه، فوریه، مارس، آوریل، مه..."),
    LUNAR_HIJRI("LUNAR_HIJRI", "هجری قمری (Islamic Hijri)", "محرم، صفر، ربیع‌الاول، جمادی‌الاول...")
}

/**
 * Robust, pure Kotlin implementation for multi-calendar support:
 * Solar Hijri (هجری شمسی - دری افغانستان و ایرانی), Gregorian (میلادی), and Islamic Lunar (هجری قمری).
 * Supports bidirectional Gregorian <-> Jalali conversion, leap years, and Afghan Dari terminology.
 */
object PersianDateHelper {

    // Dari month names used across Afghanistan
    val DARI_MONTHS = listOf(
        "حمل",    // 1 (Hamal) - Farvardin
        "ثور",    // 2 (Sawr) - Ordibehesht
        "جوزا",   // 3 (Jawza) - Khordad
        "سرطان",  // 4 (Saratan) - Tir
        "اسد",    // 5 (Asad) - Mordad
        "سنبله",  // 6 (Sonbola) - Shahrivar
        "میزان",  // 7 (Mizan) - Mehr
        "عقرب",   // 8 (Aqrab) - Aban
        "قوس",    // 9 (Qaws) - Azar
        "جدی",    // 10 (Jadi) - Dey
        "دلو",    // 11 (Dalwa) - Bahman
        "حوت"     // 12 (Hoot) - Esfand
    )

    // Iranian month names
    val IRANIAN_MONTHS = listOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    )

    // Gregorian month names in Persian/Dari
    val GREGORIAN_MONTHS = listOf(
        "ژانویه", "فوریه", "مارس", "آوریل", "مه", "ژوئن",
        "ژوئیه", "اوت", "سپتامبر", "اکتبر", "نوامبر", "دسامبر"
    )

    // Islamic Lunar Hijri months
    val LUNAR_MONTHS = listOf(
        "محرم", "صفر", "ربیع‌الاول", "ربیع‌الثانی", "جمادی‌الاول", "جمادی‌الثانی",
        "رجب", "شعبان", "رمضان", "شوال", "ذی‌القعده", "ذی‌الحجه"
    )

    data class JalaliDate(
        val year: Int,
        val month: Int, // 1..12
        val day: Int    // 1..31
    ) {
        val monthName: String
            get() = if (month in 1..12) DARI_MONTHS[month - 1] else ""

        fun formatLong(): String = "$day $monthName $year"

        fun formatNumeric(): String = String.format(Locale.US, "%04d/%02d/%02d", year, month, day)
    }

    /**
     * Converts Gregorian (year, month 1-12, day 1-31) to JalaliDate
     */
    fun gregorianToJalali(gy: Int, gm: Int, gd: Int): JalaliDate {
        val gDaysInMonth = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
        val gy2 = if (gm > 2) gy else gy - 1
        var days = 355666 + (365 * gy) + ((gy2 + 3) / 4) - ((gy2 + 99) / 100) + ((gy2 + 399) / 400) + gd + gDaysInMonth[gm - 1]
        var jy = -1595 + (33 * (days / 12053))
        days %= 12053
        jy += 4 * (days / 1461)
        days %= 1461
        if (days > 365) {
            jy += (days - 1) / 365
            days = (days - 1) % 365
        }
        val jm: Int
        val jd: Int
        if (days < 186) {
            jm = 1 + (days / 31)
            jd = 1 + (days % 31)
        } else {
            jm = 7 + ((days - 186) / 30)
            jd = 1 + ((days - 186) % 30)
        }
        return JalaliDate(jy, jm, jd)
    }

    /**
     * Converts Jalali (year, month 1-12, day 1-31) to Gregorian (gy, gm, gd)
     */
    fun jalaliToGregorian(jy: Int, jm: Int, jd: Int): Triple<Int, Int, Int> {
        val jy2 = jy + 1595
        var days = -355668 + (365 * jy2) + ((jy2 / 33) * 8) + (((jy2 % 33) + 3) / 4) + jd
        if (jm < 7) {
            days += (jm - 1) * 31
        } else {
            days += ((jm - 7) * 30) + 186
        }
        var gy = 400 * (days / 146097)
        days %= 146097
        if (days > 36524) {
            days -= 1
            gy += 100 * (days / 36524)
            days %= 36524
            if (days >= 365) {
                days += 1
            }
        }
        gy += 4 * (days / 1461)
        days %= 1461
        if (days > 365) {
            gy += (days - 1) / 365
            days = (days - 1) % 365
        }
        var gd = days + 1
        val isLeap = ((gy % 4 == 0 && gy % 100 != 0) || gy % 400 == 0)
        val salA = intArrayOf(0, 31, if (isLeap) 29 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        var gm = 0
        while (gm < 13 && gd > salA[gm]) {
            gd -= salA[gm]
            gm += 1
        }
        return Triple(gy, gm, gd)
    }

    /**
     * Converts a Unix timestamp in milliseconds to JalaliDate
     */
    fun timestampToJalali(timestamp: Long): JalaliDate {
        val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
        return gregorianToJalali(
            gy = cal.get(Calendar.YEAR),
            gm = cal.get(Calendar.MONTH) + 1,
            gd = cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    /**
     * Converts JalaliDate to Unix timestamp at specified hour and minute
     */
    fun jalaliToTimestamp(jy: Int, jm: Int, jd: Int, hour: Int = 12, minute: Int = 0): Long {
        val (gy, gm, gd) = jalaliToGregorian(jy, jm, jd)
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, gy)
            set(Calendar.MONTH, gm - 1)
            set(Calendar.DAY_OF_MONTH, gd)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    /**
     * Converts Gregorian date to Unix timestamp at specified hour and minute
     */
    fun gregorianToTimestamp(gy: Int, gm: Int, gd: Int, hour: Int = 12, minute: Int = 0): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, gy)
            set(Calendar.MONTH, (gm - 1).coerceIn(0, 11))
            set(Calendar.DAY_OF_MONTH, gd.coerceIn(1, 31))
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    /**
     * Calculates days in Gregorian month
     */
    fun getDaysInGregorianMonth(gy: Int, gm: Int): Int {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, gy)
            set(Calendar.MONTH, (gm - 1).coerceIn(0, 11))
            set(Calendar.DAY_OF_MONTH, 1)
        }
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    /**
     * Converts Islamic Lunar Hijri date to Unix timestamp
     */
    fun hijriToTimestamp(hy: Int, hm: Int, hd: Int, hour: Int = 12, minute: Int = 0): Long {
        val jdn = (11 * hy + 3) / 30 + 354 * hy + 30 * hm - (hm - 1) / 2 + hd + 1948440 - 385
        var l = jdn + 68569
        val n = (4 * l) / 146097
        l -= (146097 * n + 3) / 4
        val i = (4000 * (l + 1)) / 1461001
        l = l - (1461 * i) / 4 + 31
        val j = (80 * l) / 2447
        val gd = l - (2447 * j) / 80
        l = j / 11
        val gm = j + 2 - 12 * l
        val gy = 100 * (n - 49) + i + l
        return gregorianToTimestamp(gy, gm, gd, hour, minute)
    }

    /**
     * Converts a Unix timestamp to Islamic Lunar Hijri (Year, Month 1-12, Day 1-30)
     */
    fun timestampToHijri(timestamp: Long): Triple<Int, Int, Int> {
        val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
        val y = cal.get(Calendar.YEAR)
        val m = cal.get(Calendar.MONTH) + 1
        val d = cal.get(Calendar.DAY_OF_MONTH)
        val a = (14 - m) / 12
        val yj = y + 4800 - a
        val mj = m + 12 * a - 3
        val jdn = d + (153 * mj + 2) / 5 + 365 * yj + yj / 4 - yj / 100 + yj / 400 - 32045
        var l = jdn - 1948440 + 10632
        val n = (l - 1) / 10631
        l = l - 10631 * n + 354
        val j = ((10985 - l) / 5316) * ((50 * l) / 17719) + (l / 5670) * ((43 * l) / 15238)
        l = l - ((30 - j) / 15) * ((17719 * j) / 50) - (j / 16) * ((15238 * j) / 43) + 29
        val hm = (24 * l) / 709
        val hd = l - (709 * hm) / 24
        val hy = 30 * n + j - 30
        return Triple(hy, hm.coerceIn(1, 12), hd.coerceIn(1, 30))
    }

    private val dateCache = java.util.concurrent.ConcurrentHashMap<Long, String>(512)
    private val dateTimeCache = java.util.concurrent.ConcurrentHashMap<Long, String>(512)

    fun clearCache() {
        dateCache.clear()
        dateTimeCache.clear()
        remainingDaysCache.clear()
    }

    /**
     * Calculates the exact start timestamp (00:00:00.000) of the current month based on the specified calendar type.
     * When a new month begins in this calendar, all transactions before this timestamp belong to the previous month.
     */
    fun getStartOfCurrentMonth(type: AppCalendarType = activeCalendarType, timestamp: Long = System.currentTimeMillis()): Long {
        return when (type) {
            AppCalendarType.SOLAR_DARI, AppCalendarType.SOLAR_IRANIAN -> {
                val j = timestampToJalali(timestamp)
                val (gy, gm, gd) = jalaliToGregorian(j.year, j.month, 1)
                val cal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, gy)
                    set(Calendar.MONTH, gm - 1)
                    set(Calendar.DAY_OF_MONTH, gd)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                cal.timeInMillis
            }
            AppCalendarType.GREGORIAN -> {
                val cal = Calendar.getInstance().apply {
                    timeInMillis = timestamp
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                cal.timeInMillis
            }
            AppCalendarType.LUNAR_HIJRI -> {
                val (hy, hm, _) = timestampToHijri(timestamp)
                hijriToTimestamp(hy, hm, 1, 0, 0)
            }
        }
    }

    /**
     * Calculates the exact end timestamp (23:59:59.999) of the current month based on the specified calendar type.
     */
    fun getEndOfCurrentMonth(type: AppCalendarType = activeCalendarType, timestamp: Long = System.currentTimeMillis()): Long {
        return when (type) {
            AppCalendarType.SOLAR_DARI, AppCalendarType.SOLAR_IRANIAN -> {
                val j = timestampToJalali(timestamp)
                val daysInMonth = if (j.month <= 6) 31 else if (j.month <= 11) 30 else if (isJalaliLeapYear(j.year)) 30 else 29
                val (gy, gm, gd) = jalaliToGregorian(j.year, j.month, daysInMonth)
                val cal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, gy)
                    set(Calendar.MONTH, gm - 1)
                    set(Calendar.DAY_OF_MONTH, gd)
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }
                cal.timeInMillis
            }
            AppCalendarType.GREGORIAN -> {
                val cal = Calendar.getInstance().apply {
                    timeInMillis = timestamp
                    val maxDay = getActualMaximum(Calendar.DAY_OF_MONTH)
                    set(Calendar.DAY_OF_MONTH, maxDay)
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }
                cal.timeInMillis
            }
            AppCalendarType.LUNAR_HIJRI -> {
                val (hy, hm, _) = timestampToHijri(timestamp)
                hijriToTimestamp(hy, hm, 30, 23, 59)
            }
        }
    }

    /**
     * Returns the display name of the current month in the active calendar.
     */
    fun getCurrentMonthName(type: AppCalendarType = activeCalendarType, timestamp: Long = System.currentTimeMillis()): String {
        return when (type) {
            AppCalendarType.SOLAR_DARI -> {
                val j = timestampToJalali(timestamp)
                if (j.month in 1..12) DARI_MONTHS[j.month - 1] else ""
            }
            AppCalendarType.SOLAR_IRANIAN -> {
                val j = timestampToJalali(timestamp)
                if (j.month in 1..12) IRANIAN_MONTHS[j.month - 1] else ""
            }
            AppCalendarType.GREGORIAN -> {
                val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
                val m = cal.get(Calendar.MONTH)
                if (m in 0..11) GREGORIAN_MONTHS[m] else ""
            }
            AppCalendarType.LUNAR_HIJRI -> {
                val (_, hm, _) = timestampToHijri(timestamp)
                if (hm in 1..12) LUNAR_MONTHS[hm - 1] else ""
            }
        }
    }

    private fun formatDateInternal(timestamp: Long, type: AppCalendarType, cal: Calendar? = null): String {
        val c = cal ?: Calendar.getInstance().apply { timeInMillis = timestamp }
        return when (type) {
            AppCalendarType.SOLAR_DARI -> {
                val j = gregorianToJalali(
                    gy = c.get(Calendar.YEAR),
                    gm = c.get(Calendar.MONTH) + 1,
                    gd = c.get(Calendar.DAY_OF_MONTH)
                )
                "${j.day} ${if (j.month in 1..12) DARI_MONTHS[j.month - 1] else ""} ${j.year}"
            }
            AppCalendarType.SOLAR_IRANIAN -> {
                val j = gregorianToJalali(
                    gy = c.get(Calendar.YEAR),
                    gm = c.get(Calendar.MONTH) + 1,
                    gd = c.get(Calendar.DAY_OF_MONTH)
                )
                "${j.day} ${if (j.month in 1..12) IRANIAN_MONTHS[j.month - 1] else ""} ${j.year}"
            }
            AppCalendarType.GREGORIAN -> {
                val d = c.get(Calendar.DAY_OF_MONTH)
                val m = c.get(Calendar.MONTH)
                val y = c.get(Calendar.YEAR)
                "$d ${if (m in 0..11) GREGORIAN_MONTHS[m] else ""} $y"
            }
            AppCalendarType.LUNAR_HIJRI -> {
                val (hy, hm, hd) = timestampToHijri(timestamp)
                val mName = if (hm in 1..12) LUNAR_MONTHS[hm - 1] else ""
                "$hd $mName $hy"
            }
        }
    }

    private fun formatDateTimeInternal(timestamp: Long, type: AppCalendarType): String {
        val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
        val dateStr = formatDateInternal(timestamp, type, cal)
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val min = cal.get(Calendar.MINUTE)
        val hStr = if (hour < 10) "0$hour" else "$hour"
        val mStr = if (min < 10) "0$min" else "$min"
        return "$dateStr - $hStr:$mStr"
    }

    /**
     * Formats timestamp into date string according to the selected AppCalendarType
     */
    fun formatDate(timestamp: Long, type: AppCalendarType = AppCalendarType.SOLAR_DARI): String {
        val key = (timestamp shl 3) xor type.ordinal.toLong()
        dateCache[key]?.let { return it }
        if (dateCache.size > 2000) dateCache.clear()
        val result = formatDateInternal(timestamp, type)
        dateCache[key] = result
        return result
    }

    /**
     * Formats timestamp into date and time according to the selected AppCalendarType
     */
    fun formatDateTime(timestamp: Long, type: AppCalendarType = AppCalendarType.SOLAR_DARI): String {
        val key = (timestamp shl 3) xor type.ordinal.toLong()
        dateTimeCache[key]?.let { return it }
        if (dateTimeCache.size > 2000) dateTimeCache.clear()
        val result = formatDateTimeInternal(timestamp, type)
        dateTimeCache[key] = result
        return result
    }

    /**
     * Formats timestamp into numeric format according to the selected AppCalendarType
     */
    fun formatDateNumeric(timestamp: Long, type: AppCalendarType = AppCalendarType.SOLAR_DARI): String {
        val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
        return when (type) {
            AppCalendarType.SOLAR_DARI, AppCalendarType.SOLAR_IRANIAN -> {
                val j = timestampToJalali(timestamp)
                j.formatNumeric()
            }
            AppCalendarType.GREGORIAN -> {
                val d = cal.get(Calendar.DAY_OF_MONTH)
                val m = cal.get(Calendar.MONTH) + 1
                val y = cal.get(Calendar.YEAR)
                String.format(Locale.US, "%04d/%02d/%02d", y, m, d)
            }
            AppCalendarType.LUNAR_HIJRI -> {
                val (hy, hm, hd) = timestampToHijri(timestamp)
                String.format(Locale.US, "%04d/%02d/%02d", hy, hm, hd)
            }
        }
    }

    @Volatile
    var activeCalendarType: AppCalendarType = AppCalendarType.SOLAR_DARI
        set(value) {
            field = value
            clearCache()
        }

    /**
     * Formats timestamp into date string according to activeCalendarType
     */
    fun formatSolarDate(timestamp: Long): String = formatDate(timestamp, activeCalendarType)

    /**
     * Formats timestamp into standard numeric format according to activeCalendarType
     */
    fun formatSolarDateNumeric(timestamp: Long): String = formatDateNumeric(timestamp, activeCalendarType)

    /**
     * Formats timestamp into date with time according to activeCalendarType
     */
    fun formatSolarDateTime(timestamp: Long): String = formatDateTime(timestamp, activeCalendarType)

    /**
     * Returns days in a given Jalali month (first 6 months have 31 days, next 5 have 30, month 12 has 29 or 30)
     */
    fun getDaysInJalaliMonth(year: Int, month: Int): Int {
        return when {
            month in 1..6 -> 31
            month in 7..11 -> 30
            month == 12 -> if (isJalaliLeapYear(year)) 30 else 29
            else -> 30
        }
    }

    /**
     * Checks if a Jalali year is a leap year (سال کبیسه)
     */
    fun isJalaliLeapYear(year: Int): Boolean {
        val rem = year % 33
        return rem in intArrayOf(1, 5, 9, 13, 17, 22, 26, 30)
    }

    private val remainingDaysCache = java.util.concurrent.ConcurrentHashMap<Long, String>(128)

    /**
     * Calculates the remaining days until the given due date timestamp and returns
     * a user-friendly Persian string such as: "۳ روز مانده", "امروز", "۲ روز گذشته".
     */
    fun getRemainingDaysText(dueDateMillis: Long?): String {
        if (dueDateMillis == null || dueDateMillis <= 0L) return ""
        remainingDaysCache[dueDateMillis]?.let { return it }
        val now = System.currentTimeMillis()
        val calNow = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val calDue = Calendar.getInstance().apply {
            timeInMillis = dueDateMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val diffMillis = calDue.timeInMillis - calNow.timeInMillis
        val diffDays = (diffMillis / (24 * 60 * 60 * 1000L)).toInt()

        val result = when {
            diffDays == 0 -> "امروز"
            diffDays == 1 -> "۱ روز مانده"
            diffDays > 1 -> "$diffDays روز مانده"
            diffDays == -1 -> "۱ روز گذشته"
            else -> "${-diffDays} روز گذشته"
        }
        if (remainingDaysCache.size > 500) remainingDaysCache.clear()
        remainingDaysCache[dueDateMillis] = result
        return result
    }

    /**
     * Returns today's JalaliDate
     */
    fun todayJalali(): JalaliDate = timestampToJalali(System.currentTimeMillis())
}
