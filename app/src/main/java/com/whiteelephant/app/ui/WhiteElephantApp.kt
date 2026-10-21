package com.whiteelephant.app.ui

import android.content.Intent
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.whiteelephant.app.R
import com.whiteelephant.app.ads.ConsentManager
import com.whiteelephant.app.ads.InterstitialManager
import com.whiteelephant.app.game.Rules
import com.whiteelephant.app.share.InviteLink
import com.whiteelephant.app.share.sendInvite
import com.whiteelephant.app.share.whatsAppPackage
import com.whiteelephant.app.ui.components.SuspenseOverlay
import com.whiteelephant.app.ui.order.OrderScreen
import com.whiteelephant.app.ui.play.PlayScreen
import com.whiteelephant.app.ui.rules.RulesScreen
import com.whiteelephant.app.ui.setup.SetupScreen
import com.whiteelephant.app.ui.summary.SummaryScreen

private object Routes {
    const val SETUP = "setup"
    const val ORDER = "order"
    const val PLAY = "play"
    const val SUMMARY = "summary"
    const val RULES = "rules"
}

private const val PRIVACY_URL = "https://whiteelephant.borzanti.com/privacy/"

@Composable
fun WhiteElephantApp(viewModel: WhiteElephantViewModel = viewModel()) {
    val navController = rememberNavController()
    val activity = LocalActivity.current
    val setup by viewModel.setup.collectAsStateWithLifecycle()
    val game by viewModel.game.collectAsStateWithLifecycle()
    val canUndo by viewModel.canUndo.collectAsStateWithLifecycle()
    val drawing by viewModel.drawing.collectAsStateWithLifecycle()
    val privacyOptionsRequired by ConsentManager.privacyOptionsRequired.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val hasWhatsApp = remember { context.whatsAppPackage() != null }
    // O convite e a política abrem no idioma em que o app está.
    val pageLanguage = InviteLink.pageLanguage(LocalConfiguration.current.locales[0].toLanguageTag())

    // O convite abre o seletor do sistema por cima do app. O pedido de avaliação
    // espera a volta: disparado com o seletor aberto, a Play o descartaria e o
    // único pedido do aparelho seria gasto à toa.
    var inviteSent by rememberSaveable { mutableStateOf(false) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        if (inviteSent) {
            inviteSent = false
            viewModel.onInviteSent()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                UiEvent.OrderReady -> navController.navigate(Routes.ORDER)

                is UiEvent.Finished -> {
                    if (event.showAd && activity != null) {
                        InterstitialManager.show(activity) { navController.showSummary() }
                    } else {
                        navController.showSummary()
                    }
                }

                UiEvent.AskForReview -> activity?.let { ReviewPrompt.request(it) {} }
            }
        }
    }

    // A partida vive no ViewModel, não na rota: preparação, ordem, jogo e resumo
    // olham para o mesmo objeto, salvo a cada jogada.
    NavHost(navController = navController, startDestination = Routes.SETUP) {
        composable(Routes.SETUP) {
            val loaded = setup ?: return@composable
            SetupScreen(
                setup = loaded,
                savedGame = game,
                onEventNameChange = viewModel::setEventName,
                onDateChange = viewModel::setDate,
                onTimeChange = viewModel::setTime,
                onBudgetChange = viewModel::setBudget,
                onRulesChange = viewModel::setRules,
                hasWhatsApp = hasWhatsApp,
                onSendInvite = { channel ->
                    val left = context.sendInvite(channel, loaded, InviteLink.url(loaded, pageLanguage))
                    // Só espera a volta se outro app abriu; "copiar" fica aqui.
                    if (left) inviteSent = true
                },
                onAddNames = viewModel::addNames,
                onRemoveName = viewModel::removeName,
                onDrawOrder = viewModel::drawOrder,
                onResume = {
                    navController.navigate(if (game?.isFinished == true) Routes.SUMMARY else Routes.PLAY)
                },
                onRules = { navController.navigate(Routes.RULES) },
                showPrivacyOptions = privacyOptionsRequired,
                onPrivacyOptions = { activity?.let(ConsentManager::showPrivacyOptions) },
                onPrivacyPolicy = {
                    activity?.startActivity(Intent(Intent.ACTION_VIEW, "$PRIVACY_URL#$pageLanguage".toUri()))
                },
            )
            if (drawing) {
                SuspenseOverlay(samples = loaded.names, label = stringResource(R.string.order_suspense))
            }
        }

        composable(Routes.ORDER) {
            val current = game ?: return@composable
            OrderScreen(
                game = current,
                onBack = { navController.popBackStack() },
                onStart = {
                    navController.navigate(Routes.PLAY) {
                        popUpTo(Routes.SETUP)
                    }
                },
            )
        }

        composable(Routes.PLAY) {
            val current = game ?: return@composable
            PlayScreen(
                game = current,
                budget = setup?.budget.orEmpty(),
                canUndo = canUndo,
                onBack = { navController.popBackStack(Routes.SETUP, inclusive = false) },
                onOpenGift = { viewModel.openGift() },
                onSteal = viewModel::steal,
                onKeep = viewModel::keepGift,
                onUndo = viewModel::undo,
                onRules = { navController.navigate(Routes.RULES) },
                onDiscard = {
                    viewModel.discardGame()
                    navController.popBackStack(Routes.SETUP, inclusive = false)
                },
            )
        }

        composable(Routes.SUMMARY) {
            val current = game?.takeIf { it.isFinished } ?: return@composable
            LaunchedEffect(Unit) { viewModel.onSummaryShown() }
            SummaryScreen(
                game = current,
                onDone = { navController.popBackStack(Routes.SETUP, inclusive = false) },
                onPlayAgain = {
                    navController.popBackStack(Routes.SETUP, inclusive = false)
                    viewModel.drawOrder()
                },
            )
        }

        composable(Routes.RULES) {
            RulesScreen(rules = game?.rules ?: setup?.rules ?: Rules(), onBack = { navController.popBackStack() })
        }
    }
}

/** O resumo substitui a tela de jogo: voltar dele leva à preparação, não à partida encerrada. */
private fun NavHostController.showSummary() {
    navigate(Routes.SUMMARY) {
        popUpTo(Routes.SETUP)
    }
}
