package com.whiteelephant.app.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

/**
 * Mantém um interstitial pré-carregado e o exibe antes do resumo da noite.
 *
 * Regra de ouro: o resumo da troca NUNCA depende do anúncio. Se a carga
 * falhar, se o SDK demorar ou se o usuário fechar o anúncio, [show] chama
 * `onFinished` do mesmo jeito — o grupo sempre vê quem levou o quê.
 */
object InterstitialManager {

    private const val TAG = "InterstitialManager"

    private var ad: InterstitialAd? = null
    private var loading = false

    val isReady: Boolean get() = ad != null

    fun preload(context: Context) {
        if (!AdIds.enabled || loading || ad != null) return
        loading = true
        InterstitialAd.load(
            context.applicationContext,
            AdIds.interstitial,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(loaded: InterstitialAd) {
                    ad = loaded
                    loading = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w(TAG, "Interstitial não carregou: ${error.message}")
                    ad = null
                    loading = false
                }
            },
        )
    }

    /**
     * Exibe o anúncio se houver um pronto e chama [onFinished] quando a tela for
     * liberada. Sem anúncio disponível, [onFinished] é chamado imediatamente.
     */
    fun show(activity: Activity, onFinished: () -> Unit) {
        val current = ad
        if (current == null) {
            preload(activity)
            onFinished()
            return
        }

        current.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                ad = null
                preload(activity)
                onFinished()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                Log.w(TAG, "Interstitial não exibiu: ${error.message}")
                ad = null
                preload(activity)
                onFinished()
            }
        }
        ad = null
        current.show(activity)
    }
}
