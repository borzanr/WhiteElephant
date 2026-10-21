package com.whiteelephant.app.ads

import com.google.android.gms.ads.MobileAds
import com.whiteelephant.app.BuildConfig

/**
 * IDs de anúncio, centralizados para a troca de teste → produção ser de uma linha.
 *
 * Builds debug SEMPRE usam os IDs de teste oficiais do Google. Isso é proposital:
 * clicar nos próprios anúncios reais durante o desenvolvimento é a forma mais
 * comum de ter a conta AdMob banida.
 *
 * O APPLICATION_ID (com "~") fica no AndroidManifest.xml e é o mesmo em todos os
 * builds — o que separa teste de produção são os unit IDs (com "/") daqui.
 */
object AdIds {

    private const val TEST_BANNER = "ca-app-pub-3940256099942544/6300978111"
    private const val TEST_INTERSTITIAL = "ca-app-pub-3940256099942544/1033173712"

    // Blocos reais da conta AdMob, app "White Elephant" (criados em 20/10/2026). O
    // prefixo bate com o APPLICATION_ID do manifest.
    private const val PROD_BANNER = "ca-app-pub-5043299631567488/2831296941"
    private const val PROD_INTERSTITIAL = "ca-app-pub-5043299631567488/4911946632"

    /**
     * Falha fechada: um release sem IDs de produção não pede anúncio nenhum, em
     * vez de pedir com ID de teste (impressões que não pagam) ou com ID errado
     * (tráfego inválido na conta).
     */
    val enabled: Boolean
        get() = BuildConfig.DEBUG || (PROD_BANNER.isNotBlank() && PROD_INTERSTITIAL.isNotBlank())

    val banner: String get() = if (BuildConfig.DEBUG) TEST_BANNER else PROD_BANNER
    val interstitial: String get() = if (BuildConfig.DEBUG) TEST_INTERSTITIAL else PROD_INTERSTITIAL

    /**
     * Aparelhos que sempre recebem anúncios de teste, mesmo num build de release
     * com os IDs reais.
     *
     * ⚠️ O mesmo aparelho gera hashes diferentes conforme **a chave que assinou o
     * app**: no Redmi, o build local (chave de debug) deu `E3153D…` e o app
     * baixado da Play (assinado pelo Google) deu `6CD394C0…`. Por isso um hash
     * testado em debug não protege a versão da loja. Também muda quando o
     * identificador de publicidade é redefinido.
     *
     * A fonte de verdade é o painel **AdMob → Configurações → Dispositivos de
     * teste**, que vale na hora e sem nova versão. Esta lista é a segunda camada.
     */
    val TEST_DEVICE_IDS: List<String> = listOf(
        "6CD394C0EB5DE91059535D5B35FC532F", // Redmi Note 9S — app instalado pela Play
        "8B3FEDD52B52AD340B8E016780B25AD0", // Redmi Note 9S — após reset do AAID
        "E3153D8858021F4D58DC5E9F4F11BDD9", // Redmi Note 9S — hash anterior
    )

    /** Aplica os aparelhos de teste ao SDK. Chamar antes de carregar anúncios. */
    fun applyTestDevices() {
        if (TEST_DEVICE_IDS.isEmpty()) return
        MobileAds.setRequestConfiguration(
            MobileAds.getRequestConfiguration().toBuilder()
                .setTestDeviceIds(TEST_DEVICE_IDS)
                .build()
        )
    }
}
