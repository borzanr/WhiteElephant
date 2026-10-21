package com.whiteelephant.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Mesma família do Amigo Secreto (fundo violeta-tinta, roxo de manto, dourado de
 * brasão), para os dois apps serem reconhecidos como do mesmo autor. O que muda é
 * o acento: verde-garrafa no lugar do azul-cristal, usado no que é deste app —
 * contador de roubos, presente congelado, a vez de quem joga.
 */
val Ink = Color(0xFF140E20)
val Surface1 = Color(0xFF1E1630)
val Surface2 = Color(0xFF2A2042)
val Outline = Color(0xFF3D3157)

/** Dourado é reservado para a ação principal (abrir presente, sortear a ordem). */
val Gold = Color(0xFFE0B354)
val Amethyst = Color(0xFF7C5BC7)

/** Verde-garrafa clareado: o escuro do ícone (#2F8F5B) some sobre o violeta. */
val Bottle = Color(0xFF5FC48A)

val TextPrimary = Color(0xFFF2E8D5)
val TextSecondary = Color(0xFFA99BC0)
val Danger = Color(0xFFE5687B)

val DangerContainer = Color(0xFF4A1F33)

/** Cores dos confetes (ARGB int, formato exigido pelo Konfetti). */
val ConfettiColors = listOf(0xE0B354, 0x7C5BC7, 0x5FC48A, 0xF2E8D5, 0x2F8F5B)
