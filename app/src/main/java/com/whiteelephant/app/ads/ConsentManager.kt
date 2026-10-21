package com.whiteelephant.app.ads

import android.app.Activity
import android.util.Log
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.whiteelephant.app.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Consentimento de anúncios via User Messaging Platform (UMP) do Google.
 *
 * Na Europa e no Reino Unido é obrigatório pedir consentimento antes de exibir
 * anúncios personalizados. Fora dessas regiões o formulário simplesmente não
 * aparece, e [canRequestAds] fica `true` de imediato.
 *
 * **O app nunca trava por causa disto.** Se o consentimento falhar — rede fora,
 * formulário indisponível, erro do SDK — a troca de presentes continua funcionando; o que
 * se perde é o anúncio, não o app. Mesma regra do [InterstitialManager].
 */
object ConsentManager {

    private const val TAG = "ConsentManager"

    private val _canRequestAds = MutableStateFlow(false)

    /** `true` quando já é permitido carregar anúncios (com ou sem personalização). */
    val canRequestAds: StateFlow<Boolean> = _canRequestAds.asStateFlow()

    private val _privacyOptionsRequired = MutableStateFlow(false)

    /**
     * `true` quando a pessoa está numa região em que a lei exige um caminho
     * permanente para rever o consentimento. A UI mostra a entrada de menu só
     * nesse caso — e a política de privacidade promete que ela existe.
     */
    val privacyOptionsRequired: StateFlow<Boolean> = _privacyOptionsRequired.asStateFlow()

    /**
     * Roda o fluxo de consentimento e libera os anúncios quando possível.
     * Deve ser chamado da Activity, na main thread — o formulário é uma tela.
     *
     * @param onCanRequestAds chamado (uma vez) quando os anúncios são liberados.
     */
    fun gather(activity: Activity, onCanRequestAds: () -> Unit) {
        val consentInformation = UserMessagingPlatform.getConsentInformation(activity)

        consentInformation.requestConsentInfoUpdate(
            activity,
            buildParameters(activity),
            {
                // Exibe o formulário apenas para quem está em região que exige.
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    if (formError != null) {
                        Log.w(TAG, "Formulário de consentimento: ${formError.message}")
                    }
                    release(consentInformation, onCanRequestAds)
                }
            },
            { requestError ->
                Log.w(TAG, "Consentimento não atualizou: ${requestError.message}")
                // Sem informação de consentimento não há anúncio personalizado,
                // mas o SDK ainda pode servir anúncios não personalizados onde
                // isso for permitido — quem decide é o próprio canRequestAds().
                release(consentInformation, onCanRequestAds)
            },
        )
    }

    private fun release(
        consentInformation: ConsentInformation,
        onCanRequestAds: () -> Unit,
    ) {
        // Atualizado antes do retorno antecipado abaixo: quem recusou anúncios
        // é justamente quem mais precisa conseguir voltar ao formulário.
        _privacyOptionsRequired.value = consentInformation.privacyOptionsRequirementStatus ==
            ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
        if (BuildConfig.DEBUG) {
            // Diagnóstico: separa "não pediu consentimento porque não precisa"
            // de "não pediu porque algo falhou".
            Log.i(
                TAG,
                "consentStatus=${consentInformation.consentStatus}" +
                    " canRequestAds=${consentInformation.canRequestAds()}" +
                    " privacyOptions=${consentInformation.privacyOptionsRequirementStatus}",
            )
        }
        if (!consentInformation.canRequestAds()) {
            Log.i(TAG, "Anúncios não liberados pelo consentimento.")
            return
        }
        if (_canRequestAds.value) return
        _canRequestAds.value = true
        onCanRequestAds()
    }

    private fun buildParameters(activity: Activity): ConsentRequestParameters {
        val builder = ConsentRequestParameters.Builder()

        // Em debug dá para forçar o comportamento europeu sem estar na Europa.
        // TEST_DEVICE_HASHED_IDS precisa conter o hash deste aparelho, que o SDK
        // imprime no logcat na primeira execução.
        if (BuildConfig.DEBUG && AdIds.TEST_DEVICE_IDS.isNotEmpty()) {
            val debugSettings = ConsentDebugSettings.Builder(activity)
                .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                .apply { AdIds.TEST_DEVICE_IDS.forEach { addTestDeviceHashedId(it) } }
                .build()
            builder.setConsentDebugSettings(debugSettings)
            Log.i(TAG, "debug EEA ligado para ${AdIds.TEST_DEVICE_IDS.size} aparelho(s)")
        }

        return builder.build()
    }

    /**
     * Reabre o formulário de privacidade para o usuário mudar de ideia. Chamado
     * pelo menu da tela de preparação quando [privacyOptionsRequired] é `true`.
     */
    fun showPrivacyOptions(activity: Activity) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { error ->
            if (error != null) Log.w(TAG, "Opções de privacidade: ${error.message}")
        }
    }
}
