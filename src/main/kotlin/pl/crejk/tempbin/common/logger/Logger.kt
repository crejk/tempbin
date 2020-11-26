package pl.crejk.tempbin.common.logger

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import kotlin.reflect.full.companionObject

fun <T : Any> logger() = LoggerDelegate<T>()

class LoggerDelegate<T : Any>: ReadOnlyProperty<T, Logger> {

    private lateinit var logger: Logger

    override fun getValue(thisRef: T, property: KProperty<*>): Logger {
        if (!::logger.isInitialized) {
            this.logger = LoggerFactory.getLogger(thisRef.getClassForLogging())
        }

        return logger
    }
}

@Suppress("NOTHING_TO_INLINE")
private inline fun <T : Any> T.getClassForLogging(): Class<out Any> =
    this.javaClass.enclosingClass?.takeIf {
        it.kotlin.companionObject?.java == javaClass
    } ?: this.javaClass
