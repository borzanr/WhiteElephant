package com.whiteelephant.app

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.ads.MobileAds
import com.whiteelephant.app.ads.AdIds
import com.whiteelephant.app.ads.ConsentManager
import com.whiteelephant.app.ads.InterstitialManager
import com.whiteelephant.app.ui.AppLanguage
import com.whiteelephant.app.ui.WhiteElephantApp
import com.whiteelephant.app.ui.theme.WhiteElephantTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(AppLanguage.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // O app é sempre escuro, independente do tema do sistema: as barras
        // precisam ser forçadas a dark, senão a de navegação fica branca.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)

        // Aparelhos de teste ANTES de qualquer requisição: o banner pede anúncio
        // assim que o consentimento libera, sem esperar o initialize abaixo.
        AdIds.applyTestDevices()

        // Consentimento primeiro: na Europa é proibido carregar anúncio antes de
        // perguntar. Fora de lá o callback vem quase imediatamente.
        ConsentManager.gather(this) {
            if (!AdIds.enabled) return@gather
            lifecycleScope.launch {
                // A inicialização do SDK faz I/O de disco: sai da main thread.
                withContext(Dispatchers.IO) { MobileAds.initialize(this@MainActivity) }
                // Já o load do interstitial EXIGE a main thread.
                InterstitialManager.preload(applicationContext)
            }
        }

        setContent {
            WhiteElephantTheme {
                WhiteElephantApp()
            }
        }
    }
}
