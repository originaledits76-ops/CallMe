package com.example

import com.example.data.model.RepeatType
import com.example.scheduling.ScheduleCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class ScheduleCalculatorTest {

    @Test
    fun testOneTimeReminderInFuture() {
        val now = System.currentTimeMillis()
        val futureTime = now + 3600_000L // 1 hour in future

        val next = ScheduleCalculator.calculateNextOccurrence(
            originalScheduledTime = futureTime,
            repeatType = RepeatType.NONE,
            referenceTimeMillis = now
        )

        assertEquals(futureTime, next)
    }

    @Test
    fun testOneTimeReminderInPastReturnsNull() {
        val now = System.currentTimeMillis()
        val pastTime = now - 3600_000L // 1 hour in past

        val next = ScheduleCalculator.calculateNextOccurrence(
            originalScheduledTime = pastTime,
            repeatType = RepeatType.NONE,
            referenceTimeMillis = now
        )

        assertNull(next)
    }

    @Test
    fun testDailyReminderRecalculation() {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 18)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val targetTime = cal.timeInMillis

        // If reference is before 18:00 today, should be today at 18:00
        val refBefore = cal.apply { add(Calendar.HOUR_OF_DAY, -2) }.timeInMillis
        val nextSameDay = ScheduleCalculator.calculateNextOccurrence(
            originalScheduledTime = targetTime,
            repeatType = RepeatType.DAILY,
            referenceTimeMillis = refBefore
        )
        assertNotNull(nextSameDay)
        assertTrue(nextSameDay!! > refBefore)

        // If reference is after 18:00 today, should be tomorrow at 18:00
        val refAfter = cal.apply { add(Calendar.HOUR_OF_DAY, 3) }.timeInMillis
        val nextDay = ScheduleCalculator.calculateNextOccurrence(
            originalScheduledTime = targetTime,
            repeatType = RepeatType.DAILY,
            referenceTimeMillis = refAfter
        )
        assertNotNull(nextDay)
        assertTrue(nextDay!! > refAfter)
    }

    @Test
    fun testSnoozeCalculation() {
        val now = 1000000L
        val snoozeTime = ScheduleCalculator.calculateSnoozeTimestamp(
            snoozeDurationMinutes = 10,
            referenceTimeMillis = now
        )

        assertEquals(now + (10 * 60 * 1000L), snoozeTime)
    }

    @Test
    fun testCustomDayMaskCalculation() {
        val mask = ScheduleCalculator.DAY_MONDAY or ScheduleCalculator.DAY_FRIDAY
        assertEquals(1 or 16, mask)

        val calendarMonday = ScheduleCalculator.calendarDayToBitmask(Calendar.MONDAY)
        assertEquals(ScheduleCalculator.DAY_MONDAY, calendarMonday)

        val calendarFriday = ScheduleCalculator.calendarDayToBitmask(Calendar.FRIDAY)
        assertEquals(ScheduleCalculator.DAY_FRIDAY, calendarFriday)
    }
}
