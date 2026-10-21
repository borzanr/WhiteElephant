package com.whiteelephant.app.data

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.whiteelephant.app.game.Game
import com.whiteelephant.app.game.Rules
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

val Context.appDataStore: DataStore<Preferences> by preferencesDataStore(name = "whiteelephant")

/**
 * O que a pessoa monta antes da festa. A primeira parte (nome, data, hora, valor,
 * regras) vira o convite; os nomes só servem na hora, para sortear a ordem.
 */
@Serializable
data class Setup(
    val eventName: String = "",
    /** "yyyy-MM-dd", ou vazio. Texto em vez de data para o JSON não depender de fuso. */
    val date: String = "",
    /** "HH:mm", ou vazio. */
    val time: String = "",
    val names: List<String> = emptyList(),
    val budget: String = "",
    val rules: Rules = Rules(),
)

/**
 * Preparação e partida em andamento, em DataStore com JSON.
 *
 * A partida é salva a cada jogada porque a troca acontece com o celular na mão
 * de todo mundo: tela que apaga, app que o sistema mata, ligação no meio. Voltar e
 * encontrar tudo onde estava é o mínimo. Não há grupos salvos nem histórico — a
 * v1 guarda só a noite atual.
 */
class Store(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    val setup: Flow<Setup> = context.appDataStore.data.map { prefs ->
        decode(prefs[SETUP_KEY]) ?: Setup()
    }

    suspend fun updateSetup(transform: (Setup) -> Setup) {
        context.appDataStore.edit { prefs ->
            val current = decode<Setup>(prefs[SETUP_KEY]) ?: Setup()
            prefs[SETUP_KEY] = json.encodeToString(transform(current))
        }
    }

    suspend fun game(): Game? = decode(context.appDataStore.data.first()[GAME_KEY])

    suspend fun saveGame(game: Game?) {
        context.appDataStore.edit { prefs ->
            if (game == null) prefs.remove(GAME_KEY) else prefs[GAME_KEY] = json.encodeToString(game)
        }
    }

    /** Incrementa o total de trocas terminadas e devolve o novo valor. */
    suspend fun incrementFinishedCount(): Int {
        var updated = 0
        context.appDataStore.edit { prefs ->
            updated = (prefs[FINISHED_COUNT_KEY] ?: 0) + 1
            prefs[FINISHED_COUNT_KEY] = updated
        }
        return updated
    }

    suspend fun hasAskedForReview(): Boolean =
        context.appDataStore.data.first()[REVIEW_ASKED_KEY] ?: false

    suspend fun markReviewAsked() {
        context.appDataStore.edit { it[REVIEW_ASKED_KEY] = true }
    }

    /** JSON ilegível vira `null` em vez de crash: perde-se a noite, não o app. */
    private inline fun <reified T> decode(raw: String?): T? {
        if (raw == null) return null
        return try {
            json.decodeFromString<T>(raw)
        } catch (e: Exception) {
            Log.w(TAG, "Dado salvo ilegível, ignorando", e)
            null
        }
    }

    companion object {
        private const val TAG = "Store"

        private val SETUP_KEY = stringPreferencesKey("setup")
        private val GAME_KEY = stringPreferencesKey("game")
        private val FINISHED_COUNT_KEY = intPreferencesKey("finished_count")
        private val REVIEW_ASKED_KEY = booleanPreferencesKey("review_asked")
    }
}
