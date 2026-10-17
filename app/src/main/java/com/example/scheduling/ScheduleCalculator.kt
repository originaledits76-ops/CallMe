package com.example.scheduling

import com.example.data.model.RepeatType
import java.util.Calendar

object ScheduleCalculator {

    /**
     * Bitmask representation for days:
     * Monday = 1 (1 shl 0)
     * Tuesday = 2 (1 shl 1)
     * Wednesday = 4 (1 shl 2)
     * Thursday = 8 (1 shl 3)
     * Friday = 16 (1 shl 4)
     * Saturday = 32 (1 shl 5)
     * Sunday = 64 (1 shl 6)
     */
    const val DAY_MONDAY = 1
    const val DAY_TUESDAY = 2
    const val DAY_WEDNESDAY = 4
    const val DAY_THURSDAY = 8
    const val DAY_FRIDAY = 16
    const val DAY_SATURDAY = 32
    const val DAY_SUNDAY = 64

    val WEEKDAYS_MASK = DAY_MONDAY or DAY_TUESDAY or DAY_WEDNESDAY or DAY_THURSDAY or DAY_FRIDAY
    val WEEKENDS_MASK = DAY_SATURDAY or DAY_SUNDAY
    val EVERY_DAY_MASK = WEEKDAYS_MASK or WEEKENDS_MASK

    /**
     * Maps java.util.Calendar day of week (SUNDAY=1, MONDAY=2, ..., SATURDAY=7)
     * to our bitmask flag.
     */
    fun calendarDayToBitmask(calendarDayOfWeek: Int): Int {
        return when (calendarDayOfWeek) {
            Calendar.MONDAY -> DAY_MONDAY
            Calendar.TUESDAY -> DAY_TUESDAY
            Calendar.WEDNESDAY -> DAY_WEDNESDAY
            Calendar.THURSDAY -> DAY_THURSDAY
            Calendar.FRIDAY -> DAY_FRIDAY
            Calendar.SATURDAY -> DAY_SATURDAY
            Calendar.SUNDAY -> DAY_SUNDAY
            else -> 0
        }
    }

    /**
     * Calculates the next trigger timestamp strictly after [referenceTimeMillis].
     * Returns null if it's a one-time reminder and its time is already past.
     */
    fun calculateNextOccurrence(
        originalScheduledTime: Long,
        repeatType: RepeatType,
        customDaysMask: Int = 0,
        referenceTimeMillis: Long = System.currentTimeMillis()
    ): Long? {
        val targetCal = Calendar.getInstance().apply {
            timeInMillis = originalScheduledTime
        }
        val targetHour = targetCal.get(Calendar.HOUR_OF_DAY)
        val targetMinute = targetCal.get(Calendar.MINUTE)
        val targetSecond = targetCal.get(Calendar.SECOND)

        // For non-recurring reminders:
        if (repeatType == RepeatType.NONE) {
            return if (originalScheduledTime > referenceTimeMillis) {
                originalScheduledTime
            } else {
                null
            }
        }

        // Determine effective mask
        val effectiveMask = when (repeatType) {
            RepeatType.DAILY -> EVERY_DAY_MASK
            RepeatType.WEEKDAYS -> WEEKDAYS_MASK
            RepeatType.WEEKENDS -> WEEKENDS_MASK
            RepeatType.CUSTOM -> if (customDaysMask == 0) EVERY_DAY_MASK else customDaysMask
            RepeatType.NONE -> 0
        }

        // Search day by day starting from today up to 14 days
        val candidate = Calendar.getInstance().apply {
            timeInMillis = referenceTimeMillis
            set(Calendar.HOUR_OF_DAY, targetHour)
            set(Calendar.MINUTE, targetMinute)
            set(Calendar.SECOND, targetSecond)
            set(Calendar.MILLISECOND, 0)
        }

        for (dayOffset in 0..14) {
            val candidateDayOfWeek = candidate.get(Calendar.DAY_OF_WEEK)
            val bit = calendarDayToBitmask(candidateDayOfWeek)

            val matchesSchedule = (effectiveMask and bit) != 0
            val isStrictlyInFuture = candidate.timeInMillis > referenceTimeMillis

            if (matchesSchedule && isStrictlyInFuture) {
                return candidate.timeInMillis
            }

            candidate.add(Calendar.DAY_OF_YEAR, 1)
        }

        return null
    }

    /**
     * Calculates snooze timestamp given snooze minutes
     */
    fun calculateSnoozeTimestamp(
        snoozeDurationMinutes: Int,
        referenceTimeMillis: Long = System.currentTimeMillis()
    ): Long {
        val duration = if (snoozeDurationMinutes <= 0) 10 else snoozeDurationMinutes
        return referenceTimeMillis + (duration * 60 * 1000L)
    }
}
