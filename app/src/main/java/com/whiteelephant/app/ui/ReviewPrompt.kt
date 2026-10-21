package com.whiteelephant.app.ui

import android.app.Activity
import android.util.Log
import com.google.android.play.core.review.ReviewManagerFactory

/**
 * Convite para avaliar o app, via In-App Review do Google.
 *
 * O diálogo aparece dentro do app, sem tirar a pessoa dali. O momento escolhido é
 * o resumo do fim da troca — alguém que acabou de ver o confete está bem mais
 * propenso a dar cinco estrelas do que alguém interrompido no meio da brincadeira.
 *
 * A própria Play limita quantas vezes o diálogo pode aparecer por usuário, e não
 * informa se ele foi de fato exibido nem se a pessoa avaliou. Por isso o código
 * nunca promete nada ao usuário nem premia quem avalia — além de ser proibido
 * pelas políticas, seria impossível de verificar.
 */
object ReviewPrompt {

    private const val TAG = "ReviewPrompt"

    /**
     * Tenta exibir o convite. Falhas são silenciosas de propósito: avaliar é um
     * extra, e nada no app pode depender disso.
     */
    fun request(activity: Activity, onFinished: () -> Unit) {
        val manager = ReviewManagerFactory.create(activity)
        manager.requestReviewFlow().addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fluxo de avaliação indisponível: ${task.exception?.message}")
                onFinished()
                return@addOnCompleteListener
            }
            manager.launchReviewFlow(activity, task.result)
                .addOnCompleteListener { onFinished() }
        }
    }
}
