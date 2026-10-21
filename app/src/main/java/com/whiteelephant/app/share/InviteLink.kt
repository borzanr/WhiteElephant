package com.whiteelephant.app.share

import com.whiteelephant.app.data.Setup
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * O convite da noite, como a página `site/i/index.html` o lê.
 *
 * Chaves de uma letra porque tudo vai numa URL colada no WhatsApp. **Mudar um
 * [SerialName] quebra todos os convites já enviados.** Sem valores padrão: campo
 * com padrão não seria serializado, e a página teria de adivinhar.
 */
@Serializable
data class InvitePayload(
    @SerialName("v") val version: Int,
    @SerialName("e") val event: String,
    /** "yyyy-MM-dd" ou vazio. */
    @SerialName("d") val date: String,
    /** "HH:mm" ou vazio. */
    @SerialName("t") val time: String,
    @SerialName("b") val budget: String,
    /** As três regras, na ordem de [com.whiteelephant.app.game.Rules]. */
    @SerialName("f") val freeze: Boolean,
    @SerialName("n") val noStealBack: Boolean,
    @SerialName("s") val finalSwap: Boolean,
    /** Idioma da página: "en", "pt" ou "es". */
    @SerialName("l") val language: String,
)

/**
 * Codifica o convite no **fragmento** da URL. O navegador nunca envia o
 * fragmento ao servidor: a página é estática e igual para todos, e nada do que o
 * organizador digitou passa por servidor ou fica em log.
 *
 * Diferente do link do Amigo Secreto, aqui não há ofuscação: o convite é para o
 * grupo inteiro ler, não há segredo a esconder de quem cola o link.
 */
@OptIn(ExperimentalEncodingApi::class)
object InviteLink {

    const val VERSION = 1

    const val BASE_URL = "https://whiteelephant.borzanti.com/i/"

    private val base64 = Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT)

    private val json = Json { ignoreUnknownKeys = true }

    fun url(setup: Setup, language: String): String = BASE_URL + "#" + encode(payload(setup, language))

    fun payload(setup: Setup, language: String) = InvitePayload(
        version = VERSION,
        event = setup.eventName.trim(),
        date = setup.date,
        time = setup.time,
        budget = setup.budget.trim(),
        freeze = setup.rules.freezeAfterMax,
        noStealBack = setup.rules.noStealBack,
        finalSwap = setup.rules.finalSwap,
        language = language,
    )

    fun encode(payload: InvitePayload): String =
        base64.encode(json.encodeToString(payload).encodeToByteArray())

    /** `null` para qualquer entrada que não seja um convite válido. */
    fun decode(token: String): InvitePayload? = runCatching {
        json.decodeFromString<InvitePayload>(base64.decode(token.trim()).decodeToString())
    }.getOrNull()

    /** Idioma da página a partir do idioma do app: inglês, português ou espanhol. */
    fun pageLanguage(languageTag: String): String = when {
        languageTag.startsWith("pt") -> "pt"
        languageTag.startsWith("es") -> "es"
        else -> "en"
    }
}
