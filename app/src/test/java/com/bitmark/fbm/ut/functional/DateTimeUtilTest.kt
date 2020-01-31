/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.fbm.ut.functional

import com.bitmark.fbm.ut.Data
import com.bitmark.fbm.util.DateTimeUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.*


class DateTimeUtilTest : FunctionalTest() {

    @Test
    fun testDateToStringWFormatTimeZone() {
        val data = mutableListOf<Data<Date, String>>()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = 1503230400000
        data.add(Data(calendar.time, "2017 Aug 20 12:00:00"))
        calendar.timeInMillis = 1503205200000
        data.add(Data(calendar.time, "2017 Aug 20 05:00:00"))

        data.forEach { d ->
            val actual = DateTimeUtil.dateToString(d.input, DateTimeUtil.DATE_FORMAT_1)
            assertEquals(d.output, actual)
        }
    }

    @Test
    fun testStringToDate() {
        val data = mutableListOf<Data<String, Date>>()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = 1503230400000
        data.add(Data("2017-08-20T12:00:00.000000Z", calendar.time))
        calendar.timeInMillis = 1503205200000
        data.add(Data("2017-08-20T05:00:00.000000Z", calendar.time))

        data.forEach { d ->
            val actual = DateTimeUtil.stringToDate(d.input)
            assertEquals(d.output, actual)
        }
    }

    @Test
    fun testStringToDateWFormatTimeZone() {
        val data = mutableListOf<Data<String, Date>>()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = 1503230400000
        data.add(Data("2017 Aug 20 12:00:00", calendar.time))
        calendar.timeInMillis = 1503205200000
        data.add(Data("2017 Aug 20 05:00:00", calendar.time))

        data.forEach { d ->
            val actual = DateTimeUtil.stringToDate(d.input, format = DateTimeUtil.DATE_FORMAT_1)
            assertEquals(d.output, actual)
        }
    }

    @Test
    fun testMillisToStringWFormatTimeZone() {
        val data = listOf(
            Data(623566800000, "1989-10-05T05:00:00.000000Z"),
            Data(718977600000, "1992-10-13T12:00:00.000000Z"),
            Data(0L, "1970-01-01T00:00:00.000000Z")
        )

        data.forEach { d ->
            val actual = DateTimeUtil.millisToString(d.input, DateTimeUtil.ISO8601_FORMAT)
            assertEquals(d.output, actual)
        }
    }

    @Test
    fun testGetStartOfWeekMillisWMillisGap() {
        val data = listOf(
            Data(623566800000, 623203200000),
            Data(718977600000, 718761600000)
        )

        data.forEach { d ->
            val actual = DateTimeUtil.getStartOfWeekMillis(d.input, 0)
            assertEquals(d.output, actual)
        }
    }

    @Test
    fun testGetEndOfWeekMillisWMillis() {
        val data = listOf(
            Data(623566800000, 623807999999),
            Data(718977600000, 719366399999)
        )

        data.forEach { d ->
            val actual = DateTimeUtil.getEndOfWeekMillis(d.input)
            assertEquals(d.output, actual)
        }
    }

    @Test
    fun testGetStartOfYearMillisWMillisGap() {
        val data = listOf(
            Data(623566800000, 599616000000),
            Data(718977600000, 694224000000)
        )

        data.forEach { d ->
            val actual = DateTimeUtil.getStartOfYearMillis(d.input, 0)
            assertEquals(d.output, actual)
        }
    }

    @Test
    fun testGetEndOfYearMillisWMillis() {
        val data = listOf(
            Data(623566800000, 631151999999),
            Data(718977600000, 725846399999)
        )

        data.forEach { d ->
            val actual = DateTimeUtil.getEndOfYearMillis(d.input)
            assertEquals(d.output, actual)
        }
    }

    @Test
    fun testGetStartOfDecadeMillisWMillis() {
        val data = listOf(
            Data(623566800000, 315532800000),
            Data(718977600000, 631152000000)
        )

        data.forEach { d ->
            val actual = DateTimeUtil.getStartOfDecadeMillis(d.input, 0)
            assertEquals(d.output, actual)
        }
    }

    @Test
    fun testGetEndOfDecadeMillisWMillis() {
        val data = listOf(
            Data(623566800000, 631151999999),
            Data(718977600000, 946684799999)
        )

        data.forEach { d ->
            val actual = DateTimeUtil.getEndOfDecadeMillis(d.input)
            assertEquals(d.output, actual)
        }
    }

    @Test
    fun testGetStartOfDate() {
        val data = listOf(
            Data(623566800000, 623523600000),
            Data(718977600000, 718909200000)
        )

        data.forEach { d ->
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = d.input
            val actual = DateTimeUtil.getStartOfDate(calendar)
            assertEquals(d.output, actual.timeInMillis)
        }

    }

    @Test
    fun testGetEndOfDateMillisWMillis() {
        val data = listOf(
            Data(623566800000, 623609999999),
            Data(718977600000, 718995599999)
        )

        data.forEach { d ->
            val actual = DateTimeUtil.getEndOfDateMillis(d.input, "GMT+7")
            assertEquals(d.output, actual)
        }
    }

    @Test
    fun testGetEndOfDate() {
        val data = listOf(
            Data(623566800000, 623609999999),
            Data(718977600000, 718995599999)
        )

        data.forEach { d ->
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = d.input
            val actual = DateTimeUtil.getEndOfDate(calendar)
            assertEquals(d.output, actual.timeInMillis)
        }
    }

    @Test
    fun testGetDateRangeOfWeek() {
        val data = listOf(
            Data(623566800000, Pair(623203200000, 623807999999)),
            Data(718977600000, Pair(718761600000, 719366399999))
        )

        data.forEach { d ->
            val actual = DateTimeUtil.getDateRangeOfWeek(d.input)
            assertEquals(d.output.first, actual.first.time)
            assertEquals(d.output.second, actual.second.time)
        }
    }

    @Test
    fun testGetDateRangeOfYear() {
        val data = listOf(
            Data(623566800000, Pair(599616000000, 631151999999)),
            Data(718977600000, Pair(694224000000, 725846399999))
        )

        data.forEach { d ->
            val actual = DateTimeUtil.getDateRangeOfYear(d.input)
            assertEquals(d.output.first, actual.first.time)
            assertEquals(d.output.second, actual.second.time)
        }
    }

    @Test
    fun testGetDateRangeOfDecade() {
        val data = listOf(
            Data(623566800000, Pair(315532800000, 631151999999)),
            Data(718977600000, Pair(631152000000, 946684799999))
        )

        data.forEach { d ->
            val actual = DateTimeUtil.getDateRangeOfDecade(d.input)
            assertEquals(d.output.first, actual.first.time)
            assertEquals(d.output.second, actual.second.time)
        }
    }

    @Test
    fun testGetDoWWMillis() {
        val data = listOf(
            Data(623566800000, Calendar.THURSDAY),
            Data(718977600000, Calendar.TUESDAY)
        )

        data.forEach { d ->
            val actual = DateTimeUtil.getDoW(d.input)
            assertEquals(d.output, actual)
        }
    }

    @Test
    fun testGetMoYWMillis() {
        val data = listOf(
            Data(623566800000, Calendar.OCTOBER),
            Data(718977600000, Calendar.OCTOBER)
        )

        data.forEach { d ->
            val actual = DateTimeUtil.getMoY(d.input)
            assertEquals(d.output, actual)
        }
    }

    @Test
    fun testGetYearWMillis() {
        val data = listOf(
            Data(623566800000, 1989),
            Data(718977600000, 1992)
        )

        data.forEach { d ->
            val actual = DateTimeUtil.getYear(d.input)
            assertEquals(d.output, actual)
        }
    }

    @Test
    fun testGetYearWDate() {
        val data = listOf(
            Data(623566800000, 1989),
            Data(718977600000, 1992)
        )

        data.forEach { d ->
            val actual = DateTimeUtil.getYear(Date(d.input))
            assertEquals(d.output, actual)
        }
    }

    @Test
    fun testGetEndOfMonthMillisWMillis() {
        val data = listOf(
            Data(623566800000, 625881599999),
            Data(718977600000, 720575999999)
        )

        data.forEach { d ->
            val actual = DateTimeUtil.getEndOfMonthMillis(d.input)
            assertEquals(d.output, actual)
        }
    }

    @Test
    fun testGetStartOfDatesMillisInWeekWMillis() {
        val data = listOf(
            Data(
                623566800000,
                longArrayOf(
                    623203200000,
                    623289600000,
                    623376000000,
                    623462400000,
                    623548800000,
                    623635200000,
                    623721600000
                )
            ),
            Data(
                718977600000,
                longArrayOf(
                    718761600000,
                    718848000000,
                    718934400000,
                    719020800000,
                    719107200000,
                    719193600000,
                    719280000000
                )
            )
        )

        data.forEach { d ->
            val actual = DateTimeUtil.getStartOfDatesMillisInWeek(d.input)
            assertTrue(d.output.contentEquals(actual))
        }
    }

    @Test
    fun testGetStartOfDatesMillisInYearWMillis() {
        val data = listOf(
            Data(
                623566800000,
                longArrayOf(
                    599616000000,
                    602294400000,
                    604713600000,
                    607392000000,
                    609984000000,
                    612662400000,
                    615254400000,
                    617932800000,
                    620611200000,
                    623203200000,
                    625881600000,
                    628473600000
                )
            ),
            Data(
                718977600000,
                longArrayOf(
                    694224000000,
                    696902400000,
                    699408000000,
                    702086400000,
                    704678400000,
                    707356800000,
                    709948800000,
                    712627200000,
                    715305600000,
                    717897600000,
                    720576000000,
                    723168000000
                )
            )
        )

        data.forEach { d ->
            val actual = DateTimeUtil.getStartOfDatesMillisInYear(d.input)
            assertTrue(d.output.contentEquals(actual))
        }
    }

    @Test
    fun testGetStartOfDatesMillisInDecadeWMillis() {
        val data = listOf(
            Data(
                623566800000,
                longArrayOf(
                    315532800000,
                    347155200000,
                    378691200000,
                    410227200000,
                    441763200000,
                    473385600000,
                    504921600000,
                    536457600000,
                    567993600000,
                    599616000000
                )
            ),
            Data(
                718977600000,
                longArrayOf(
                    631152000000,
                    662688000000,
                    694224000000,
                    725846400000,
                    757382400000,
                    788918400000,
                    820454400000,
                    852076800000,
                    883612800000,
                    915148800000
                )
            )
        )

        data.forEach { d ->
            val actual = DateTimeUtil.getStartOfDatesMillisInDecade(d.input)
            assertTrue(d.output.contentEquals(actual))
        }
    }
}