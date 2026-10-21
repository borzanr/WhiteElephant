package com.whiteelephant.app.game

import kotlin.random.Random
import kotlinx.serialization.Serializable

/**
 * As três regras que variam de grupo para grupo. Os padrões são os da maioria
 * das festas, e o app não tem tela de configuração: só uma linha "Mudar regras".
 */
@Serializable
data class Rules(
    /** Um presente roubado [Game.MAX_STEALS] vezes congela com quem está. */
    val freezeAfterMax: Boolean = true,
    /**
     * Um presente roubado nesta rodada não pode ser roubado de novo até alguém
     * abrir um presente novo. Inclui o caso clássico de quem perdeu tentar pegar
     * de volta na hora — e é o que impede uma rodada de nunca terminar.
     */
    val noStealBack: Boolean = true,
    /** No fim, quem abriu o primeiro presente pode trocar com qualquer um. */
    val finalSwap: Boolean = true,
)

@Serializable
data class Gift(
    /**
     * Número do presente na ordem em que foi aberto, começando em 1. É o único
     * nome que ele tem: pedir para digitar o que tinha dentro travava a sala a
     * cada presente aberto.
     */
    val number: Int,
    /** Índice em [Game.players] de quem está com ele agora. */
    val holder: Int,
    val steals: Int = 0,
)

enum class Phase {
    /** Rodadas normais: abrir presente novo ou roubar. */
    PLAYING,

    /** Última chance de quem abriu primeiro (regra [Rules.finalSwap]). */
    FINAL_SWAP,
    FINISHED,
}

/** Por que um presente não pode ser roubado agora. */
enum class StealBlock {
    FROZEN,
    STOLEN_THIS_ROUND,
    OWN_GIFT,
}

/**
 * Uma troca de presentes inteira, do sorteio da ordem até o último presente.
 *
 * Imutável: cada jogada devolve um novo [Game]. É isso que torna o "desfazer"
 * trivial (basta guardar o anterior) e a persistência segura (o que se grava é
 * sempre um estado completo e válido).
 *
 * Invariantes que os testes conferem:
 * - na fase [Phase.PLAYING], quem está na vez ([active]) nunca tem presente — ou
 *   ainda não abriu, ou acabou de ser roubado;
 * - cada pessoa termina com exatamente um presente, e cada presente com uma pessoa;
 * - toda rodada termina com um presente novo aberto, então o jogo sempre acaba.
 */
@Serializable
data class Game(
    /** Nomes já na ordem sorteada: o índice é a posição na fila. */
    val players: List<String>,
    val rules: Rules = Rules(),
    val gifts: List<Gift> = emptyList(),
    /** Índice de quem está agindo agora: o dono da rodada ou quem foi roubado. */
    val active: Int = 0,
    /** Números dos presentes roubados desde o último presente aberto. */
    val stolenThisRound: Set<Int> = emptySet(),
    val phase: Phase = Phase.PLAYING,
) {
    /** Quantos presentes já foram abertos = quantas rodadas já terminaram. */
    val round: Int get() = gifts.size

    val isFinished: Boolean get() = phase == Phase.FINISHED

    /** Dono da rodada atual, que nem sempre é quem está agindo. */
    val roundOwner: Int get() = round.coerceAtMost(players.lastIndex)

    /** Próximo da fila depois da rodada atual, ou `null` na última. */
    val nextUp: Int? get() = (round + 1).takeIf { phase == Phase.PLAYING && it < players.size }

    fun giftOf(player: Int): Gift? = gifts.firstOrNull { it.holder == player }

    fun isFrozen(gift: Gift): Boolean = rules.freezeAfterMax && gift.steals >= MAX_STEALS

    /** `null` quando [active] pode roubar [gift] agora. */
    fun stealBlock(gift: Gift): StealBlock? = when {
        gift.holder == active -> StealBlock.OWN_GIFT
        isFrozen(gift) -> StealBlock.FROZEN
        phase == Phase.PLAYING && rules.noStealBack && gift.number in stolenThisRound ->
            StealBlock.STOLEN_THIS_ROUND
        else -> null
    }

    fun canSteal(gift: Gift): Boolean = phase != Phase.FINISHED && stealBlock(gift) == null

    /** Quem está na vez abre um presente novo. Encerra a rodada. */
    fun openGift(): Game {
        check(phase == Phase.PLAYING) { "Só se abre presente durante as rodadas" }
        val opened = Gift(number = gifts.size + 1, holder = active)
        val after = copy(gifts = gifts + opened, stolenThisRound = emptySet())
        val nextRound = after.round
        return when {
            nextRound < players.size -> after.copy(active = nextRound)
            // Com uma pessoa só não há com quem trocar; o mínimo do app é 3, mas
            // a engine não depende disso.
            rules.finalSwap && players.size > 1 -> after.copy(active = 0, phase = Phase.FINAL_SWAP)
            else -> after.copy(phase = Phase.FINISHED)
        }
    }

    /**
     * Quem está na vez rouba [giftNumber].
     *
     * Nas rodadas, quem foi roubado passa a agir e a rodada continua. Na troca
     * final, os dois presentes trocam de mão e o jogo acaba.
     */
    fun steal(giftNumber: Int): Game {
        val target = gifts.first { it.number == giftNumber }
        require(canSteal(target)) { "Presente $giftNumber não pode ser roubado agora: ${stealBlock(target)}" }
        val victim = target.holder

        return when (phase) {
            Phase.PLAYING -> copy(
                gifts = gifts.map { if (it.number == giftNumber) it.copy(holder = active, steals = it.steals + 1) else it },
                active = victim,
                stolenThisRound = stolenThisRound + giftNumber,
            )

            Phase.FINAL_SWAP -> {
                val mine = giftOf(active)?.number
                copy(
                    gifts = gifts.map {
                        when (it.number) {
                            giftNumber -> it.copy(holder = active, steals = it.steals + 1)
                            mine -> it.copy(holder = victim)
                            else -> it
                        }
                    },
                    phase = Phase.FINISHED,
                )
            }

            Phase.FINISHED -> error("Jogo encerrado")
        }
    }

    /** Na troca final, quem abriu primeiro fica com o que tem. */
    fun keepGift(): Game {
        check(phase == Phase.FINAL_SWAP) { "Só na troca final" }
        return copy(phase = Phase.FINISHED)
    }

    /** O presente mais disputado da noite; empate fica com o aberto primeiro. */
    fun mostStolen(): Gift? = gifts.filter { it.steals > 0 }.maxWithOrNull(
        compareBy<Gift> { it.steals }.thenByDescending { it.number },
    )

    val totalSteals: Int get() = gifts.sumOf { it.steals }

    companion object {
        const val MIN_PLAYERS = 3
        const val MAX_PLAYERS = 60
        const val MAX_STEALS = 3
        const val MAX_NAME = 30

        /** Sorteia a ordem de escolha. É o único ponto de acaso do jogo. */
        fun start(names: List<String>, rules: Rules, random: Random = Random.Default): Game {
            require(names.size in MIN_PLAYERS..MAX_PLAYERS) { "Entre $MIN_PLAYERS e $MAX_PLAYERS pessoas" }
            return Game(players = names.shuffled(random), rules = rules)
        }

        private val SEPARATORS = Regex("[\r\n,;]+")

        /**
         * Aceita um nome ou uma lista colada do grupo do WhatsApp. Nomes já
         * presentes (ignorando caixa) ficam de fora, para não haver dois "Ana"
         * na fila sem jeito de saber de quem é a vez.
         */
        fun mergeNames(existing: List<String>, raw: String): List<String> {
            val seen = existing.map { it.lowercase() }.toMutableSet()
            val added = raw.split(SEPARATORS)
                .map { it.trim().take(MAX_NAME).trim() }
                .filter { it.isNotEmpty() && seen.add(it.lowercase()) }
            return (existing + added).take(MAX_PLAYERS)
        }
    }
}
