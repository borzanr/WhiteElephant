package com.whiteelephant.app.ui.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.whiteelephant.app.R
import com.whiteelephant.app.game.Game
import com.whiteelephant.app.ui.components.DrawButton
import com.whiteelephant.app.ui.play.giftTitle
import com.whiteelephant.app.ui.theme.ConfettiColors
import java.util.concurrent.TimeUnit
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter

/**
 * Fim da noite: quem leva o quê e o presente mais disputado. É a tela que vira
 * foto no grupo, então o destaque vai para a história ("a caneca foi roubada 3
 * vezes"), não para a lista.
 */
@Composable
fun SummaryScreen(game: Game, onDone: () -> Unit, onPlayAgain: () -> Unit) {
    val mostStolen = game.mostStolen()

    // Surface dá a cor de texto padrão do tema: sem Scaffold em volta, o título
    // sem cor explícita saía preto sobre o fundo escuro.
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
    ) {
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    Text(
                        stringResource(R.string.summary_title),
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                }

                if (mostStolen != null) {
                    item {
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                            Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    stringResource(R.string.summary_most_wanted),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.tertiary,
                                )
                                Text(
                                    giftTitle(mostStolen),
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                Text(
                                    stringResource(
                                        R.string.summary_most_wanted_body,
                                        mostStolen.steals,
                                        game.players[mostStolen.holder],
                                    ),
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                    }
                }

                item {
                    Text(
                        stringResource(R.string.summary_totals, game.players.size, game.totalSteals),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp),
                    )
                }

                items(game.players.indices.toList()) { player ->
                    val gift = game.giftOf(player) ?: return@items
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
                            Text(
                                game.players[player],
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f).padding(end = 12.dp),
                            )
                            Text(
                                giftTitle(gift),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }

            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DrawButton(text = stringResource(R.string.summary_done), onClick = onDone)
                OutlinedButton(onClick = onPlayAgain, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.summary_play_again))
                }
            }
        }

        KonfettiView(modifier = Modifier.fillMaxSize(), parties = remember { confettiParties() })
    }
    }
}

private fun confettiParties(): List<Party> {
    val emitter = Emitter(duration = 220, TimeUnit.MILLISECONDS).max(180)
    return listOf(
        Party(
            speed = 12f,
            maxSpeed = 42f,
            damping = 0.9f,
            spread = 360,
            colors = ConfettiColors,
            position = Position.Relative(0.5, 0.0),
            emitter = emitter,
        ),
    )
}
