package com.mlomsky.messierviewer.model

import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * One observing session runs from 6pm local time to 6am the next day. If "now" is at or after
 * 6am, the upcoming/current session is tonight 6pm -> tomorrow 6am. If "now" is between midnight
 * and 6am, we're in the tail end of the session that started the previous evening, so the window
 * is yesterday 6pm -> today 6am.
 */
data class ViewingSession(val start: Instant, val end: Instant, val zoneId: ZoneId) {
    companion object {
        private const val SESSION_START_HOUR = 18
        private const val SESSION_END_HOUR = 6

        fun forNow(now: ZonedDateTime): ViewingSession {
            val zone = now.zone
            val today = now.toLocalDate()
            val sessionStartDate = if (now.hour < SESSION_END_HOUR) today.minusDays(1) else today

            val start = ZonedDateTime.of(sessionStartDate, LocalTime.of(SESSION_START_HOUR, 0), zone)
            val end = start.plusDays(1).withHour(SESSION_END_HOUR).withMinute(0).withSecond(0).withNano(0)

            return ViewingSession(start.toInstant(), end.toInstant(), zone)
        }
    }
}
