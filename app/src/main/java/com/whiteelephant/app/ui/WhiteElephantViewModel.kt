package com.whiteelephant.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.whiteelephant.app.data.Setup
import com.whiteelephant.app.data.Store
import com.whiteelephant.app.game.Game
import com.whiteelephant.app.game.Rules
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Sinais de uma vez só para a UI: navegar, mostrar anúncio, pedir avaliação. */
sealed interface UiEvent {
    data object OrderReady : UiEvent

    /** A troca acabou; a UI exibe o anúncio se [showAd] e então o resumo. */
    data class Finished(val showAd: Boolean) : UiEvent
    data object AskForReview : UiEvent
}

class WhiteElephantViewModel(app: Application) : AndroidViewModel(app) {

    private val store = Store(app)

    /** `null` só até a preparação salva carregar. */
    val setup: StateFlow<Setup?> = store.setup
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _game = MutableStateFlow<Game?>(null)
    val game: StateFlow<Game?> = _game.asStateFlow()

    /**
     * Jogadas anteriores da partida atual, para o "desfazer". Só em memória: o
     * botão existe para corrigir um toque errado na hora, não para reescrever a
     * noite depois de o app ser fechado.
     */
    private val undoStack = ArrayDeque<Game>()
    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()

    private val _drawing = MutableStateFlow(false)

    /** `true` durante o suspense do sorteio da ordem. */
    val drawing: StateFlow<Boolean> = _drawing.asStateFlow()

    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events: Flow<UiEvent> = _events.receiveAsFlow()

    init {
        viewModelScope.launch { _game.value = store.game() }
    }

    // region preparação

    fun addNames(raw: String) = editSetup { it.copy(names = Game.mergeNames(it.names, raw)) }

    fun removeName(name: String) = editSetup { it.copy(names = it.names - name) }

    fun setEventName(name: String) = editSetup { it.copy(eventName = name.take(MAX_EVENT)) }

    fun setDate(isoDate: String) = editSetup { it.copy(date = isoDate) }

    fun setTime(time: String) = editSetup { it.copy(time = time) }

    fun setBudget(budget: String) = editSetup { it.copy(budget = budget.take(MAX_BUDGET)) }

    fun setRules(rules: Rules) = editSetup { it.copy(rules = rules) }

    private fun editSetup(transform: (Setup) -> Setup) {
        viewModelScope.launch { store.updateSetup(transform) }
    }

    // endregion

    // region partida

    /**
     * A ordem é decidida no clique; o suspense é só encenação, como no Amigo
     * Secreto. Uma partida anterior, terminada ou não, é substituída.
     */
    fun drawOrder() {
        val current = setup.value ?: return
        if (current.names.size < Game.MIN_PLAYERS || _drawing.value) return
        val started = Game.start(current.names, current.rules)
        _drawing.value = true
        undoStack.clear()
        _canUndo.value = false
        setGame(started)
        viewModelScope.launch {
            delay(SUSPENSE_MILLIS)
            _drawing.value = false
            _events.send(UiEvent.OrderReady)
        }
    }

    fun openGift() = play { it.openGift() }

    fun steal(giftNumber: Int) = play { it.steal(giftNumber) }

    fun keepGift() = play { it.keepGift() }

    fun undo() {
        val previous = undoStack.removeLastOrNull() ?: return
        _canUndo.value = undoStack.isNotEmpty()
        setGame(previous)
    }

    /** Descarta a partida e volta à preparação, com as mesmas pessoas. */
    fun discardGame() {
        undoStack.clear()
        _canUndo.value = false
        setGame(null)
    }

    /** Chamado quando o resumo já está na tela, depois do anúncio se houve. */
    fun onSummaryShown() {
        // Deixa o confete cair antes de o diálogo da Play cobrir a tela.
        maybeAskForReview(delayMillis = SUMMARY_REVIEW_DELAY_MILLIS)
    }

    /**
     * Chamado quando a pessoa volta ao app depois de enviar o convite. É o
     * trabalho principal do app concluído — e, como o condutor da partida é
     * opcional, para muita gente é o único. Esperar só o resumo repetiria o erro
     * do Amigo Secreto até a 1.2: convite atrás de um passo que quase ninguém faz.
     */
    fun onInviteSent() = maybeAskForReview(delayMillis = INVITE_REVIEW_DELAY_MILLIS)

    /** Uma vez por aparelho; a Play ainda limita por conta própria. */
    private fun maybeAskForReview(delayMillis: Long) {
        viewModelScope.launch {
            if (store.hasAskedForReview()) return@launch
            store.markReviewAsked()
            delay(delayMillis)
            _events.send(UiEvent.AskForReview)
        }
    }

    private fun play(move: (Game) -> Game) {
        val before = _game.value ?: return
        if (before.isFinished) return
        val after = move(before)
        undoStack.addLast(before)
        if (undoStack.size > MAX_UNDO) undoStack.removeFirst()
        _canUndo.value = true
        setGame(after)

        if (after.isFinished) {
            viewModelScope.launch {
                val total = store.incrementFinishedCount()
                _events.send(UiEvent.Finished(showAd = shouldShowAd(total)))
            }
        }
    }

    private fun setGame(game: Game?) {
        _game.value = game
        viewModelScope.launch { store.saveGame(game) }
    }

    // endregion

    /**
     * Nunca depois da primeira troca do aparelho — é ali que a pessoa decide se o
     * app presta, e é ali que sai o convite para avaliar. Depois, em trocas
     * alternadas. White elephant é uma ou duas vezes por ano: a frequência real
     * já é baixíssima.
     */
    private fun shouldShowAd(totalFinished: Int): Boolean =
        totalFinished >= 2 && totalFinished % AD_EVERY_N_GAMES == 0

    companion object {
        const val SUSPENSE_MILLIS = 1_500L
        const val MAX_BUDGET = 20
        const val MAX_EVENT = 40

        private const val SUMMARY_REVIEW_DELAY_MILLIS = 2_500L
        private const val INVITE_REVIEW_DELAY_MILLIS = 700L
        private const val AD_EVERY_N_GAMES = 2
        private const val MAX_UNDO = 200
    }
}
