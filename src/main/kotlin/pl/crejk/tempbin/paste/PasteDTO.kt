package pl.crejk.tempbin.paste

import java.time.Duration

data class PasteDTO constructor(
    val content: String,
    val expiration: Expiration = Expiration.HOUR,
    val deleteAfterReading: Boolean = false
)

data class Test(
    val content: String
)

enum class Expiration(
    private val duration: Duration
) {

    //FIVE_SECONDS(Duration.ofSeconds(5)),
    //MINUTE(Duration.ofMinutes(1)),
    TEN_MINUTES(Duration.ofMinutes(10)),
    HOUR(Duration.ofHours(1)),
    DAY(Duration.ofDays(1)),
    WEEK(Duration.ofDays(7)),
    TWO_WEEKS(Duration.ofDays(14)),
    MONTH(Duration.ofDays(30)),
    ;

    val nanos = this.duration.toNanos()
}
