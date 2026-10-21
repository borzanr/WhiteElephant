package com.whiteelephant.app.share

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.core.net.toUri
import com.whiteelephant.app.R
import com.whiteelephant.app.data.Setup
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Por onde o convite sai.
 *
 * O app não depende só do seletor do sistema: no Android 11 do Redmi de teste
 * ele passou a fechar sozinho (NullPointerException em `ChooserListAdapter`,
 * processo `android:ui`) assim que um app publicou um atalho de compartilhamento
 * direto sem nome — e de dentro do app não há como saber que ele caiu. Os canais
 * diretos e o "copiar" funcionam mesmo com o seletor quebrado.
 */
enum class InviteChannel { WHATSAPP, SMS, COPY, OTHER }

private val WHATSAPP_PACKAGES = listOf("com.whatsapp", "com.whatsapp.w4b")

/** Pacote do WhatsApp instalado (comum ou Business), ou `null`. */
fun Context.whatsAppPackage(): String? = WHATSAPP_PACKAGES.firstOrNull { pkg ->
    runCatching {
        if (Build.VERSION.SDK_INT >= 33) {
            packageManager.getPackageInfo(pkg, PackageManager.PackageInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            packageManager.getPackageInfo(pkg, 0)
        }
    }.isSuccess
}

/**
 * Envia o convite pelo canal escolhido.
 *
 * @return `true` se outro app foi aberto — é o que diz se vale esperar a volta
 * para pedir avaliação. "Copiar" fica no app e devolve `false`.
 */
fun Context.sendInvite(channel: InviteChannel, setup: Setup, url: String): Boolean {
    val message = inviteMessage(setup, url)
    return when (channel) {
        InviteChannel.WHATSAPP -> {
            val pkg = whatsAppPackage()
            if (pkg == null) {
                openChooser(message)
            } else {
                tryStart(sendIntent(message).setPackage(pkg)) || openChooser(message)
            }
        }

        InviteChannel.SMS -> tryStart(
            Intent(Intent.ACTION_SENDTO, "smsto:".toUri()).putExtra("sms_body", message),
        ) || openChooser(message)

        InviteChannel.COPY -> {
            val clipboard = getSystemService(ClipboardManager::class.java)
            clipboard.setPrimaryClip(ClipData.newPlainText(getString(R.string.invite_chooser), message))
            // A partir do Android 13 o próprio sistema mostra que copiou; antes
            // disso, sem aviso a pessoa não sabe se deu certo.
            if (Build.VERSION.SDK_INT < 33) {
                Toast.makeText(this, R.string.invite_copied, Toast.LENGTH_SHORT).show()
            }
            false
        }

        InviteChannel.OTHER -> openChooser(message)
    }
}

/**
 * Texto do convite. A data vai também no texto, não só na página: no WhatsApp é
 * a mensagem que todo mundo lê, e muita gente nunca toca no link.
 */
fun Context.inviteMessage(setup: Setup, url: String): String {
    val event = setup.eventName.trim().ifEmpty { getString(R.string.invite_default_event) }
    val whenText = listOfNotNull(
        formatInviteDate(setup.date, Locale.getDefault()),
        formatInviteTime(setup.time, this),
    ).joinToString(", ")
    return if (whenText.isEmpty()) {
        getString(R.string.invite_message_no_date, event, url)
    } else {
        getString(R.string.invite_message, event, whenText, url)
    }
}

private fun sendIntent(message: String) = Intent(Intent.ACTION_SEND).apply {
    type = "text/plain"
    putExtra(Intent.EXTRA_TEXT, message)
}

private fun Context.openChooser(message: String): Boolean =
    tryStart(Intent.createChooser(sendIntent(message), getString(R.string.invite_chooser)))

private fun Context.tryStart(intent: Intent): Boolean = try {
    startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    true
} catch (e: ActivityNotFoundException) {
    false
}

/**
 * "18:00" → "18:00" ou "6:00 PM", conforme o relógio do aparelho (12 ou 24 horas);
 * `null` se vazio ou ilegível. A hora fica salva sempre como "HH:mm".
 */
fun formatInviteTime(time: String, context: Context): String? {
    val parsed = runCatching { SimpleDateFormat("HH:mm", Locale.US).apply { isLenient = false }.parse(time) }
        .getOrNull() ?: return null
    return android.text.format.DateFormat.getTimeFormat(context).format(parsed)
}

/** "2026-12-12" → "sábado, 12 de dezembro de 2026"; `null` se vazio ou ilegível. */
fun formatInviteDate(isoDate: String, locale: Locale, style: Int = DateFormat.FULL): String? {
    if (isoDate.isBlank()) return null
    val parsed = runCatching { SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }.parse(isoDate) }
        .getOrNull() ?: return null
    return DateFormat.getDateInstance(style, locale).format(parsed)
}
