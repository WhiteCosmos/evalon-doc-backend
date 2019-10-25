package me.ele.napos.evalon.utils

import java.time.LocalDateTime

class EvalonDateTimeUtils {
    static today() {
        def now = LocalDateTime.now()

        return [
                start: now.withHour(0).withMinute(0).withSecond(0).withNano(0),
                end: now.withHour(23).withMinute(59).withSecond(59).withNano(0)
        ]
    }

    static daysBetween(int days) {
        def now = LocalDateTime.now()

        return [
                start: now.minusDays(days),
                end: now.plusDays(days)
        ]
    }

    static daysBefore(int days) {
        def now = LocalDateTime.now()

        return [
                start: now.minusDays(days),
                end  : now
        ]
    }

    static daysAfter(int days) {
        def now = LocalDateTime.now()

        return [
                start: now,
                end  : now.plusDays(days)
        ]
    }
}
