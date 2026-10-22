package com.shohankhan.apexcalculator.core.math

import java.time.DateTimeException
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit

/** Offline calendar arithmetic built on java.time (available on the minimum supported API). */
object DateMath {

    data class DateDifference(
        val totalDays: Long,
        val weeks: Long,
        val months: Long,
        val years: Long,
        val yearsComponent: Int,
        val monthsComponent: Int,
        val daysComponent: Int,
        val weekdays: Long,
    )

    data class DurationParts(val days: Long, val hours: Long, val minutes: Long, val seconds: Long) {
        fun display(): String = buildString {
            if (days != 0L) append("$days d ")
            append("$hours h $minutes min $seconds s")
        }
    }

    fun parseDate(text: String): LocalDate {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) throw MathEvalException(MathError.Incomplete("Enter a date as yyyy-MM-dd"))
        return try {
            LocalDate.parse(trimmed)
        } catch (exception: DateTimeException) {
            throw MathEvalException(MathError.Domain("Use the date format yyyy-MM-dd, for example 2026-02-28"))
        }
    }

    fun parseTime(text: String): LocalTime {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) throw MathEvalException(MathError.Incomplete("Enter a time as HH:mm[:ss]"))
        return try {
            if (trimmed.count { it == ':' } == 1) LocalTime.parse("$trimmed:00") else LocalTime.parse(trimmed)
        } catch (exception: DateTimeException) {
            throw MathEvalException(MathError.Domain("Use the time format HH:mm or HH:mm:ss"))
        }
    }

    fun difference(start: LocalDate, end: LocalDate): DateDifference {
        val totalDays = ChronoUnit.DAYS.between(start, end)
        val period = start.until(end)
        val months = ChronoUnit.MONTHS.between(start, end)
        val years = ChronoUnit.YEARS.between(start, end)
        return DateDifference(
            totalDays = totalDays,
            weeks = Math.floorDiv(totalDays, 7),
            months = months,
            years = years,
            yearsComponent = period.years,
            monthsComponent = period.months,
            daysComponent = period.days,
            weekdays = countWeekdays(start, end),
        )
    }

    /** Counts Monday-Friday days in the half-open interval [start, end). */
    fun countWeekdays(start: LocalDate, end: LocalDate): Long {
        val from = if (start <= end) start else end
        val to = if (start <= end) end else start
        var count = 0L
        var cursor = from
        while (cursor.isBefore(to)) {
            val day = cursor.dayOfWeek.value
            if (day <= 5) count++
            cursor = cursor.plusDays(1)
        }
        return count
    }

    fun addDays(date: LocalDate, days: Long): LocalDate = date.plusDays(days)

    /** Month arithmetic clamps to the last valid day, e.g. 2026-01-31 plus one month is 2026-02-28. */
    fun addMonths(date: LocalDate, months: Long): LocalDate = date.plusMonths(months)

    fun addYears(date: LocalDate, years: Long): LocalDate = date.plusYears(years)

    fun addWeeks(date: LocalDate, weeks: Long): LocalDate = date.plusWeeks(weeks)

    fun durationBetween(start: LocalTime, end: LocalTime, crossesMidnight: Boolean): DurationParts {
        var seconds = ChronoUnit.SECONDS.between(start, end)
        if (crossesMidnight && seconds < 0) seconds += 24 * 60 * 60
        if (!crossesMidnight && seconds < 0) {
            throw MathEvalException(
                MathError.Domain("The end time is before the start time; enable the next-day option"),
            )
        }
        return fromSeconds(seconds)
    }

    fun fromSeconds(totalSeconds: Long): DurationParts {
        val seconds = Math.floorMod(totalSeconds, 60L)
        val totalMinutes = Math.floorDiv(totalSeconds, 60L)
        val minutes = Math.floorMod(totalMinutes, 60L)
        val totalHours = Math.floorDiv(totalMinutes, 60L)
        val hours = Math.floorMod(totalHours, 24L)
        val days = Math.floorDiv(totalHours, 24L)
        return DurationParts(days, hours, minutes, seconds)
    }

    fun isLeapYear(year: Int): Boolean = java.time.Year.isLeap(year.toLong())

    fun daysInMonth(year: Int, month: Int): Int =
        LocalDate.of(year, month, 1).lengthOfMonth()
}
