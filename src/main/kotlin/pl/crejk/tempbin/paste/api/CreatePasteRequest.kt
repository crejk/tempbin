package pl.crejk.tempbin.paste.api

import java.time.Duration

data class CreatePasteRequest(
    val content: String,
    val expiration: Expiration = Expiration.HOUR,
    val deleteAfterReading: Boolean = false
)

enum class Expiration(
    private val duration: Duration
) {

    TEN_MINUTES(Duration.ofMinutes(10)),
    HOUR(Duration.ofHours(1)),
    DAY(Duration.ofDays(1)),
    WEEK(Duration.ofDays(7)),
    TWO_WEEKS(Duration.ofDays(14)),
    MONTH(Duration.ofDays(30)),
    ;

    val minutes = this.duration.toMinutes()
}
