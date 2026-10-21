package com.whiteelephant.app.ui

import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import androidx.core.content.edit
import java.util.Locale

/**
 * Idioma escolhido dentro do app, independente do idioma do sistema — para quem
 * organiza a festa num idioma diferente do aparelho e para capturar a ficha da
 * loja em cada idioma. Mesmo mecanismo do Amigo Secreto.
 *
 * Também decide o idioma da página do convite: o link sai no idioma do app.
 *
 * Fica em SharedPreferences, e não no DataStore, porque precisa ser lido de forma
 * síncrona em attachBaseContext, antes de qualquer tela existir.
 */
object AppLanguage {

    /** Vazio = seguir o idioma do sistema. */
    const val SYSTEM = ""

    val OPTIONS: List<String> = listOf(SYSTEM, "pt-BR", "en", "es")

    private const val PREFS = "app_language"
    private const val KEY_TAG = "tag"

    // Guardado antes da primeira troca, para "idioma do sistema" voltar a valer
    // também na formatação de datas sem precisar reiniciar o processo.
    private val systemDefault: Locale = Locale.getDefault()

    fun current(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_TAG, SYSTEM).orEmpty()

    fun set(context: Context, tag: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit { putString(KEY_TAG, tag) }
    }

    /** Contexto com o idioma escolhido aplicado; sem escolha, devolve o original. */
    fun wrap(base: Context): Context {
        val tag = current(base)
        if (tag == SYSTEM) {
            Locale.setDefault(systemDefault)
            return base
        }
        val locale = Locale.forLanguageTag(tag)
        Locale.setDefault(locale)
        val config = Configuration(base.resources.configuration).apply { setLocales(LocaleList(locale)) }
        return base.createConfigurationContext(config)
    }

    /**
     * Nome do idioma escrito nele mesmo: quem abriu o app num idioma que não lê
     * ainda consegue achar o seu na lista.
     */
    fun nativeName(tag: String): String? = when (tag) {
        "pt-BR" -> "Português (Brasil)"
        "en" -> "English"
        "es" -> "Español"
        else -> null
    }
}
