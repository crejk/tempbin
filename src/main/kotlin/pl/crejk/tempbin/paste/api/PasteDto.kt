package pl.crejk.tempbin.paste.api

data class PasteDto(
    val id: String,
    val password: String
) {

    companion object {

        val EMPTY = PasteDto("", "")
    }
}
