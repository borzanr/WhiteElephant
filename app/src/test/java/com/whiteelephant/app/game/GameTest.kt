package com.whiteelephant.app.game

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GameTest {

    private fun game(n: Int = 4, rules: Rules = Rules()) =
        Game(players = List(n) { "P$it" }, rules = rules)

    @Test
    fun `abrir presente passa a vez para o proximo da fila`() {
        val g = game().openGift()
        assertEquals(1, g.active)
        assertEquals(0, g.giftOf(0)?.holder)
        assertEquals(1, g.gifts.single().number)
    }

    @Test
    fun `roubo passa a vez para quem foi roubado e nao encerra a rodada`() {
        val g = game().openGift().steal(1)
        assertEquals(1, g.giftOf(1)?.number)
        assertEquals(0, g.active)
        assertEquals(1, g.round)
        assertNull(g.giftOf(0))
    }

    @Test
    fun `quem perdeu nao pode roubar de volta na mesma rodada`() {
        val g = game().openGift().openGift().steal(1) // P2 rouba o 1 de P0
        val gift1 = g.gifts.first { it.number == 1 }
        assertEquals(StealBlock.STOLEN_THIS_ROUND, g.stealBlock(gift1))
        // O outro presente continua disponível.
        assertTrue(g.canSteal(g.gifts.first { it.number == 2 }))
        // Depois que alguém abre um presente novo, a trava some.
        val next = g.openGift()
        assertTrue(next.canSteal(next.gifts.first { it.number == 1 }))
    }

    @Test
    fun `sem a regra de roubo de volta o presente pode voltar na hora`() {
        val g = game(rules = Rules(noStealBack = false)).openGift().steal(1).steal(1)
        assertEquals(0, g.giftOf(0)?.holder)
        assertEquals(2, g.gifts.single().steals)
    }

    @Test
    fun `presente congela no terceiro roubo`() {
        var g = game(n = 6).openGift() // P0 tem o 1
        g = g.steal(1).openGift() // P1 rouba; P0 abre o 2
        g = g.steal(1).openGift() // P2 rouba de P1; P1 abre o 3
        g = g.steal(1) // P3 rouba de P2: terceiro roubo
        val gift1 = g.gifts.first { it.number == 1 }
        assertEquals(3, gift1.steals)
        assertTrue(g.isFrozen(gift1))
        g = g.openGift() // P2 abre; rodada nova
        assertEquals(StealBlock.FROZEN, g.stealBlock(g.gifts.first { it.number == 1 }))
    }

    @Test
    fun `sem congelar nao ha limite de roubos`() {
        var g = game(n = 6, rules = Rules(freezeAfterMax = false)).openGift()
        repeat(4) { g = g.steal(1).openGift() }
        assertEquals(4, g.gifts.first { it.number == 1 }.steals)
        assertTrue(g.canSteal(g.gifts.first { it.number == 1 }))
    }

    @Test
    fun `troca final da a quem abriu primeiro a chance de trocar`() {
        var g = game(n = 3).openGift().openGift().openGift()
        assertEquals(Phase.FINAL_SWAP, g.phase)
        assertEquals(0, g.active)
        assertEquals(StealBlock.OWN_GIFT, g.stealBlock(g.giftOf(0)!!))
        g = g.steal(3)
        assertTrue(g.isFinished)
        assertEquals(3, g.giftOf(0)?.number)
        assertEquals(1, g.giftOf(2)?.number)
    }

    @Test
    fun `troca final respeita presente congelado`() {
        var g = game(n = 5).openGift()
        g = g.steal(1).openGift().steal(1).openGift().steal(1).openGift().openGift()
        assertEquals(Phase.FINAL_SWAP, g.phase)
        val frozen = g.gifts.first { it.number == 1 }
        if (frozen.holder != g.active) assertEquals(StealBlock.FROZEN, g.stealBlock(frozen))
    }

    @Test
    fun `ficar com o presente encerra o jogo sem mudar nada`() {
        val before = game(n = 3).openGift().openGift().openGift()
        val after = before.keepGift()
        assertTrue(after.isFinished)
        assertEquals(before.gifts, after.gifts)
    }

    @Test
    fun `sem troca final o jogo acaba no ultimo presente`() {
        val g = game(n = 3, rules = Rules(finalSwap = false)).openGift().openGift().openGift()
        assertTrue(g.isFinished)
    }

    @Test
    fun `mais roubado desempata pelo aberto primeiro`() {
        val g = game(n = 4, rules = Rules(noStealBack = false))
            .openGift().openGift().steal(2).steal(1).openGift()
        assertEquals(1, g.mostStolen()?.number)
        assertNull(game().openGift().mostStolen())
    }

    @Test
    fun `mergeNames aceita lista colada e ignora repetidos`() {
        val names = Game.mergeNames(listOf("Ana"), "ana\nBruno, Carla;  \n\nBruno")
        assertEquals(listOf("Ana", "Bruno", "Carla"), names)
    }

    @Test
    fun `start embaralha sem perder ninguem`() {
        val names = List(10) { "N$it" }
        val g = Game.start(names, Rules(), Random(42))
        assertEquals(names.toSet(), g.players.toSet())
        assertEquals(10, g.players.size)
    }

    /**
     * Joga milhares de partidas com jogadas aleatórias válidas, em todas as
     * combinações de regras, e confere que o jogo sempre termina com uma pessoa
     * por presente — e que a pessoa na vez nunca tem presente durante as rodadas.
     */
    @Test
    fun `partidas aleatorias sempre terminam com um presente por pessoa`() {
        val random = Random(7)
        val combos = listOf(true, false).flatMap { a ->
            listOf(true, false).flatMap { b -> listOf(true, false).map { c -> Rules(a, b, c) } }
        }
        var steals = 0
        for (rules in combos) repeat(300) {
            val n = random.nextInt(Game.MIN_PLAYERS, 12)
            var g = Game.start(List(n) { "P$it" }, rules, random)
            var moves = 0
            while (!g.isFinished) {
                check(++moves < 10_000) { "Partida não terminou: $g" }
                if (g.phase == Phase.PLAYING) assertNull(g.giftOf(g.active))
                val stealable = g.gifts.filter { g.canSteal(it) }
                // Sem a regra de roubo de volta, roubar sempre faria o jogo girar
                // para sempre; jogadores reais param, e aqui param com 60%.
                g = when {
                    g.phase == Phase.FINAL_SWAP ->
                        if (stealable.isNotEmpty() && random.nextBoolean()) g.steal(stealable.random(random).number) else g.keepGift()
                    stealable.isNotEmpty() && random.nextDouble() < 0.4 -> {
                        steals++
                        g.steal(stealable.random(random).number)
                    }
                    else -> g.openGift()
                }
            }
            assertEquals(n, g.gifts.size)
            assertEquals((0 until n).toSet(), g.gifts.map { it.holder }.toSet())
        }
        assertTrue("o teste precisa exercitar roubos", steals > 1_000)
    }
}
