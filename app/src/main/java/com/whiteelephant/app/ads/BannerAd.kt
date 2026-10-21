package com.whiteelephant.app.ads

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.viewinterop.AndroidView

import androidx.compose.ui.unit.dp
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

/**
 * Banner adaptativo ancorado no rodapé.
 *
 * Reserva a altura do anúncio mesmo antes de ele carregar, para o conteúdo não
 * "pular" quando o banner aparece.
 */
@Composable
fun BannerAd(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current

    // Antes do consentimento não se carrega anúncio nenhum. Enquanto isso o
    // banner não ocupa espaço: reservar altura para algo que talvez nunca
    // apareça deixaria uma faixa vazia no rodapé.
    val canRequestAds by ConsentManager.canRequestAds.collectAsState()
    if (!AdIds.enabled || (!canRequestAds && !isPreview)) return

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val widthDp = maxWidth.value.toInt().coerceAtLeast(320)
        val adSize = remember(widthDp) { adaptiveSize(context, widthDp) }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(adSize.height.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (!isPreview) {
                AndroidView(
                    factory = { ctx ->
                        AdView(ctx).apply {
                            setAdSize(adSize)
                            adUnitId = AdIds.banner
                            loadAd(AdRequest.Builder().build())
                        }
                    },
                    onRelease = { it.destroy() },
                )
            }
        }
    }
}

/**
 * Banner ancorado adaptativo. As variantes `getCurrentOrientation*` foram
 * depreciadas no SDK 25; a API atual é a família `getLarge*Anchored*`, que
 * escolhe a altura ideal para a largura e a orientação correntes.
 */
private fun adaptiveSize(context: Context, widthDp: Int): AdSize =
    AdSize.getLargeAnchoredAdaptiveBannerAdSize(context, widthDp)

/**
 * Composable auxiliar para telas que precisam saber a altura reservada ao banner
 * sem instanciá-lo (ex.: padding do conteúdo rolável).
 */
@Composable
fun rememberBannerHeightDp(): Int {
    val context = LocalContext.current
    return remember { adaptiveSize(context, 360).height }
}
