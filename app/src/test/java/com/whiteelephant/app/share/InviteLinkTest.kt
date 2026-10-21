package com.whiteelephant.app.share

import com.whiteelephant.app.data.Setup
import com.whiteelephant.app.game.Rules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class InviteLinkTest {

    private val setup = Setup(
        eventName = "  Festa da firma  ",
        date = "2026-12-12",
        time = "19:30",
        budget = "R$ 50",
        rules = Rules(freezeAfterMax = true, noStealBack = false, finalSwap = true),
    )

    @Test
    fun `ida e volta preserva tudo, inclusive acento e emoji`() {
        val withEmoji = setup.copy(eventName = "Confraternização 🎁")
        val payload = InviteLink.payload(withEmoji, "pt")
        assertEquals(payload, InviteLink.decode(InviteLink.encode(payload)))
    }

    @Test
    fun `url leva o convite no fragmento`() {
        val url = InviteLink.url(setup, "pt")
        assertTrue(url.startsWith("https://whiteelephant.borzanti.com/i/#"))
        val decoded = InviteLink.decode(url.substringAfter('#'))!!
        assertEquals("Festa da firma", decoded.event)
        assertFalse(decoded.noStealBack)
    }

    /**
     * A página lê estas chaves exatas. Se este teste quebrar, `site/i/index.html`
     * precisa mudar junto — ou os convites já enviados param de abrir.
     */
    @Test
    fun `chaves do json sao o contrato com a pagina`() {
        val token = InviteLink.encode(InviteLink.payload(Setup(), "en"))
        val raw = java.util.Base64.getUrlDecoder().decode(token).decodeToString()
        for (key in listOf("v", "e", "d", "t", "b", "f", "n", "s", "l")) {
            assertTrue("falta a chave $key em $raw", raw.contains("\"$key\":"))
        }
    }

    @Test
    fun `lixo nao vira convite`() {
        assertNull(InviteLink.decode("isto-nao-e-um-convite"))
        assertNull(InviteLink.decode(""))
    }

    @Test
    fun `idioma da pagina`() {
        assertEquals("pt", InviteLink.pageLanguage("pt-BR"))
        assertEquals("es", InviteLink.pageLanguage("es-MX"))
        assertEquals("en", InviteLink.pageLanguage("de-DE"))
    }
}
