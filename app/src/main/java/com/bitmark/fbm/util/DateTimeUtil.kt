/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2019 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.util

import com.bitmark.fbm.data.model.entity.Period
import java.text.SimpleDateFormat
import java.util.*

class DateTimeUtil {

    companion object {

        val ISO8601_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'"

        val ISO8601_SIMPLE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'"

        val DATE_FORMAT_1 = "yyyy MMM dd HH:mm:ss"

        val DATE_FORMAT_2 = "yyyy MMM dd"

        val DATE_FORMAT_3 = "MMM dd"

        val DATE_FORMAT_4 = "EEEEE"

        val DATE_FORMAT_5 = "MMM"

        val DATE_FORMAT_6 = "ddd"

        val DATE_FORMAT_7 = "yyyy-MM-dd"

        val DATE_FORMAT_8 = "yyyy"

        val DATE_FORMAT_9 = "yyyy MMM"

        val DATE_FORMAT_10 = "EEE"

        val DATE_FORMAT_11 = "MMM dd, yyyy"

        val DATE_FORMAT_12 = "MMMM"

        val DATE_TIME_FORMAT_1 = "MMM dd hh:mm a"

        val TIME_FORMAT_1 = "hh:mm a"

        val WEEK_MILLIS = 7 * 24 * 60 * 60 * 1000L

        val YEAR_MILLIS = 365 * 24 * 60 * 60 * 1000L

        val DECADE_MILLIS = 10 * YEAR_MILLIS

        fun dateToString(
            date: Date,
            format: String,
            timezone: String = "UTC"
        ): String {
            return try {
                val formatter = SimpleDateFormat(format, Locale.US)
                formatter.timeZone = TimeZone.getTimeZone(timezone)
                formatter.format(date)
            } catch (e: Throwable) {
                ""
            }
        }

        fun stringToDate(date: String) = stringToDate(date, ISO8601_FORMAT)

        fun stringToDate(
            date: String,
            format: String,
            timezone: String = "UTC"
        ): Date? {
            return try {
                val formatter = SimpleDateFormat(format, Locale.US)
                formatter.timeZone = TimeZone.getTimeZone(timezone)
                formatter.parse(date)
            } catch (e: Throwable) {
                null
            }
        }

        fun defaultTimeZone() = TimeZone.getDefault().id

        fun dayCountFrom(date: Date): Long {
            val nowMillis = Date().time
            val diff = nowMillis - date.time
            return diff / (1000 * 60 * 60 * 24)
        }

        fun millisToString(
            millis: Long,
            format: String,
            timezone: String = "UTC"
        ): String {
            val calendar = Calendar.getInstance(TimeZone.getTimeZone(timezone))
            calendar.timeInMillis = millis
            return dateToString(calendar.time, format, timezone)
        }

        fun getStartOfThisWeekMillis(timezone: String = "UTC") =
            getStartOfWeekMillis(Calendar.getInstance().timeInMillis, 0, timezone)

        fun getStartOfWeekMillis(thisWeekMillis: Long, gap: Int, timezone: String = "UTC"): Long {
            val calendar = Calendar.getInstance(TimeZone.getTimeZone(timezone))
            calendar.timeInMillis = thisWeekMillis
            calendar.add(Calendar.DAY_OF_YEAR, gap * 7)
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            val startOfSunday = getStartOfDate(calendar)
            return startOfSunday.timeInMillis
        }

        fun getEndOfWeekMillis(millis: Long, timezone: String = "UTC"): Long {
            val calendar = Calendar.getInstance(TimeZone.getTimeZone(timezone))
            calendar.timeInMillis = millis
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            calendar.add(Calendar.WEEK_OF_YEAR, 1)
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            return getEndOfDate(calendar).timeInMillis
        }

        fun getStartOfThisYearMillis(timezone: String = "UTC"): Long {
            val calendar = Calendar.getInstance(TimeZone.getTimeZone(timezone))
            calendar.set(Calendar.DAY_OF_YEAR, 1)
            val startOfYear = getStartOfDate(calendar)
            return startOfYear.timeInMillis
        }

        fun getStartOfYearMillis(thisYearMillis: Long, gap: Int, timezone: String = "UTC"): Long {
            val calendar = Calendar.getInstance(TimeZone.getTimeZone(timezone))
            calendar.timeInMillis = thisYearMillis
            calendar.set(Calendar.DAY_OF_YEAR, 1)
            calendar.add(Calendar.YEAR, gap)
            val startOfYear = getStartOfDate(calendar)
            return startOfYear.timeInMillis
        }

        fun getEndOfYearMillis(millis: Long, timezone: String = "UTC"): Long {
            val nextYearMillis = getStartOfYearMillis(millis, 1, timezone)
            val calendar = Calendar.getInstance(TimeZone.getTimeZone(timezone))
            calendar.timeInMillis = nextYearMillis
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            return getEndOfDate(calendar).timeInMillis
        }

        fun getStartOfThisDecadeMillis(timezone: String = "UTC"): Long {
            val calendar = Calendar.getInstance(TimeZone.getTimeZone(timezone))
            return getStartOfDecadeMillis(calendar.timeInMillis, 0, timezone)
        }

        fun getStartOfDecadeMillis(
            millis: Long,
            gap: Int,
            timezone: String = "UTC"
        ): Long {
            val calendar = Calendar.getInstance(TimeZone.getTimeZone(timezone))
            calendar.timeInMillis = millis
            val yearGap = calendar.get(Calendar.YEAR) % 10
            calendar.add(Calendar.YEAR, 10 * gap - yearGap)
            calendar.set(Calendar.DAY_OF_YEAR, 1)
            val startOfDecade = getStartOfDate(calendar)
            return startOfDecade.timeInMillis
        }

        fun getEndOfDecadeMillis(millis: Long, timezone: String = "UTC"): Long {
            val nextDecadeMillis = getStartOfDecadeMillis(millis, 1, timezone)
            val calendar = Calendar.getInstance(TimeZone.getTimeZone(timezone))
            calendar.timeInMillis = nextDecadeMillis
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            return getEndOfDate(calendar).timeInMillis
        }

        fun getStartOfDate(calendar: Calendar): Calendar {
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            return calendar
        }

        fun getEndOfDateMillis(millis: Long, timezone: String = "UTC"): Long {
            val calendar = Calendar.getInstance(TimeZone.getTimeZone(timezone))
            calendar.timeInMillis = millis
            return getEndOfDate(calendar).timeInMillis
        }

        fun getEndOfDate(calendar: Calendar): Calendar {
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            calendar.set(Calendar.MILLISECOND, 999)
            return calendar
        }

        fun getDateRangeOfWeek(
            weekMillis: Long,
            timezone: String = "UTC"
        ) = Pair(
            Date(getStartOfWeekMillis(weekMillis, 0, timezone)),
            Date(getEndOfWeekMillis(weekMillis, timezone))
        )

        fun getDateRangeOfYear(
            yearMillis: Long,
            timezone: String = "UTC"
        ) = Pair(
            Date(getStartOfYearMillis(yearMillis, 0, timezone)),
            Date(getEndOfYearMillis(yearMillis, timezone))
        )

        fun getDateRangeOfDecade(
            decadeMillis: Long,
            timezone: String = "UTC"
        ) = Pair(
            Date(getStartOfDecadeMillis(decadeMillis, 0, timezone)),
            Date(getEndOfDecadeMillis(decadeMillis, timezone))
        )

        fun getDoW(millis: Long, timezone: String = "UTC"): Int {
            val calendar = Calendar.getInstance(TimeZone.getTimeZone(timezone))
            calendar.timeInMillis = millis
            return calendar.get(Calendar.DAY_OF_WEEK)
        }

        fun getMoY(millis: Long, timezone: String = "UTC"): Int {
            val calendar = Calendar.getInstance(TimeZone.getTimeZone(timezone))
            calendar.timeInMillis = millis
            return calendar.get(Calendar.MONTH)
        }

        fun getYear(millis: Long, timezone: String = "UTC"): Int {
            val calendar = Calendar.getInstance(TimeZone.getTimeZone(timezone))
            calendar.timeInMillis = millis
            return calendar.get(Calendar.YEAR)
        }

        fun getYear(date: Date, timezone: String = "UTC"): Int {
            val calendar = Calendar.getInstance(TimeZone.getTimeZone(timezone))
            calendar.time = date
            return calendar.get(Calendar.YEAR)
        }

        fun getEndOfMonthMillis(millis: Long, timezone: String = "UTC"): Long {
            val calendar = Calendar.getInstance(TimeZone.getTimeZone(timezone))
            calendar.timeInMillis = millis
            calendar.add(Calendar.MONTH, 1)
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            return getEndOfDate(calendar).timeInMillis
        }

        fun getStartOfDatesMillisInWeek(millis: Long, timezone: String = "UTC"): LongArray {
            val dates = LongArray(7)
            val calendar = Calendar.getInstance(TimeZone.getTimeZone(timezone))
            calendar.timeInMillis = millis
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            val startDateCal = getStartOfDate(calendar)
            for (i in dates.indices) {
                dates[i] = startDateCal.timeInMillis
                startDateCal.add(Calendar.DAY_OF_WEEK, 1)
            }
            return dates
        }

        fun getStartOfDatesMillisInYear(millis: Long, timezone: String = "UTC"): LongArray {
            var calendar = Calendar.getInstance(TimeZone.getTimeZone(timezone))
            calendar.timeInMillis = millis
            calendar.set(Calendar.MONTH, Calendar.JANUARY)
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar = getStartOfDate(calendar)
            val dates = LongArray(12)
            for (i in dates.indices) {
                dates[i] = calendar.timeInMillis
                calendar.add(Calendar.MONTH, 1)
            }
            return dates
        }

        fun getStartOfDatesMillisInDecade(millis: Long, timezone: String = "UTC"): LongArray {
            var calendar = Calendar.getInstance(TimeZone.getTimeZone(timezone))
            calendar.timeInMillis = millis
            val thisYear = calendar.get(Calendar.YEAR)
            calendar.set(Calendar.YEAR, thisYear - thisYear % 10)
            calendar.set(Calendar.MONTH, Calendar.JANUARY)
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar = getStartOfDate(calendar)
            val dates = LongArray(10)
            for (i in dates.indices) {
                dates[i] = calendar.timeInMillis
                calendar.add(Calendar.YEAR, 1)
            }
            return dates
        }
    }
}

fun DateTimeUtil.Companion.formatPeriod(
    period: Period,
    startedTimeMillis: Long,
    timezone: String = "UTC"
): String {
    val calendar = Calendar.getInstance(TimeZone.getTimeZone(timezone))
    calendar.timeInMillis = startedTimeMillis
    return when (period) {
        Period.WEEK -> {
            val range = getDateRangeOfWeek(startedTimeMillis)
            "%d %s-%s".format(
                getYear(calendar.time, timezone),
                dateToString(range.first, DATE_FORMAT_3, timezone),
                dateToString(range.second, DATE_FORMAT_3, timezone)
            )
        }
        Period.YEAR -> {
            "%d".format(getYear(calendar.time, timezone))
        }
        Period.DECADE -> {
            val range = getDateRangeOfDecade(startedTimeMillis, timezone)
            "%s-%s".format(
                dateToString(range.first, DATE_FORMAT_8, timezone),
                dateToString(range.second, DATE_FORMAT_8, timezone)
            )
        }
        else -> error("unsupported period")
    }
}

fun DateTimeUtil.Companion.formatSubPeriod(
    period: Period,
    startedTimeMillis: Long,
    timezone: String = "UTC"
): String {
    val calendar = Calendar.getInstance(TimeZone.getTimeZone(timezone))
    calendar.timeInMillis = startedTimeMillis
    return when (period) {
        Period.WEEK -> millisToString(startedTimeMillis, DATE_FORMAT_10, timezone)
        Period.YEAR -> millisToString(startedTimeMillis, DATE_FORMAT_9, timezone)
        Period.DECADE -> millisToString(startedTimeMillis, DATE_FORMAT_8, timezone)
        else -> error("unsupported period")
    }
}