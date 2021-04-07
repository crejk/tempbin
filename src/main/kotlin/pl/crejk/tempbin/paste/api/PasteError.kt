package pl.crejk.tempbin.paste.api

import io.ktor.http.HttpStatusCode
import java.util.Locale
import kotlin.reflect.KClass
import pl.crejk.tempbin.api.HttpResponse

sealed class PasteError(
    private val code: HttpStatusCode,
) {

    object NotFound : PasteError(HttpStatusCode.NotFound)

    object Unauthorized : PasteError(HttpStatusCode.Unauthorized)

    sealed class BadRequest : PasteError(HttpStatusCode.BadRequest) {

        object EmptyContent : BadRequest()
        object ContentTooLarge : BadRequest()
        object CannotTransformContent : BadRequest()
    }

    fun toHttpResponse(): HttpResponse =
        HttpResponse(this::class.nameAsDesc(), code)
}

private fun KClass<*>.nameAsDesc() =
    this.simpleName!!.splitByCapitalizeLetters()
        .joinToString(" ") { it.toLowerCase() }
        .capitalize(Locale.US)

private fun String.splitByCapitalizeLetters(): List<String> =
    this.split(Regex("(?<=.)(?=\\p{Lu})"))


