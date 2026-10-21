package com.whiteelephant.app.ui.play

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.whiteelephant.app.R
import com.whiteelephant.app.game.Game
import com.whiteelephant.app.game.Gift
import com.whiteelephant.app.game.Phase
import com.whiteelephant.app.game.StealBlock
import com.whiteelephant.app.ui.components.DrawButton

/**
 * A partida. O celular fica na mão de quem conduz a brincadeira, então a tela
 * responde a uma pergunta só, em letra grande: de quem é a vez. Todo o resto —
 * quem está com o quê, quantos roubos, o que está congelado — é a lista abaixo.
 *
 * Nenhuma jogada pede confirmação nem digitação: abrir e roubar são um toque
 * cada, e o toque errado se corrige no "desfazer". O app é apoio da brincadeira;
 * se ele fizer a sala esperar, deixa de ser usado.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayScreen(
    game: Game,
    budget: String,
    canUndo: Boolean,
    onBack: () -> Unit,
    onOpenGift: () -> Unit,
    onSteal: (Int) -> Unit,
    onKeep: () -> Unit,
    onUndo: () -> Unit,
    onRules: () -> Unit,
    onDiscard: () -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    var confirmDiscard by remember { mutableStateOf(false) }
    val finalSwap = game.phase == Phase.FINAL_SWAP
    val activeName = game.players[game.active]

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (finalSwap) {
                            stringResource(R.string.play_final_swap_title)
                        } else {
                            stringResource(R.string.play_round, game.round + 1, game.players.size)
                        },
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                },
                actions = {
                    IconButton(onClick = onUndo, enabled = canUndo) {
                        Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = stringResource(R.string.play_undo))
                    }
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.common_more))
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.rules_title)) },
                            onClick = { menuOpen = false; onRules() },
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.play_discard)) },
                            onClick = { menuOpen = false; confirmDiscard = true },
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground,
                ),
            )
        },
        bottomBar = {
            Column(Modifier.navigationBarsPadding().padding(16.dp)) {
                if (finalSwap) {
                    DrawButton(text = stringResource(R.string.play_keep), onClick = onKeep)
                } else {
                    DrawButton(text = stringResource(R.string.play_open), onClick = onOpenGift)
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item { TurnHeader(game = game, activeName = activeName, budget = budget) }

            if (game.gifts.isNotEmpty()) {
                item {
                    Text(
                        stringResource(R.string.play_gifts_opened),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }

            items(game.gifts, key = { it.number }) { gift ->
                GiftCard(
                    gift = gift,
                    holderName = game.players[gift.holder],
                    frozenRuleOn = game.rules.freezeAfterMax,
                    block = game.stealBlock(gift),
                    actionLabel = stringResource(if (finalSwap) R.string.play_swap else R.string.play_steal),
                    onSteal = { onSteal(gift.number) },
                )
            }
        }
    }

    if (confirmDiscard) {
        AlertDialog(
            onDismissRequest = { confirmDiscard = false },
            title = { Text(stringResource(R.string.play_discard_title)) },
            text = { Text(stringResource(R.string.play_discard_body)) },
            confirmButton = {
                TextButton(
                    onClick = { confirmDiscard = false; onDiscard() },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) { Text(stringResource(R.string.play_discard)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDiscard = false }) { Text(stringResource(R.string.common_cancel)) }
            },
        )
    }
}

@Composable
private fun TurnHeader(game: Game, activeName: String, budget: String) {
    val stolenFrom = game.phase == Phase.PLAYING && game.active != game.round
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                stringResource(
                    when {
                        game.phase == Phase.FINAL_SWAP -> R.string.play_final_swap_label
                        stolenFrom -> R.string.play_stolen_label
                        else -> R.string.play_turn_label
                    },
                ),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.tertiary,
            )
            Text(
                activeName,
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 44.sp, lineHeight = 50.sp),
                color = MaterialTheme.colorScheme.primary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                stringResource(
                    when {
                        game.phase == Phase.FINAL_SWAP -> R.string.play_final_swap_hint
                        stolenFrom -> R.string.play_stolen_hint
                        else -> R.string.play_turn_hint
                    },
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
            game.nextUp?.let { next ->
                Text(
                    stringResource(R.string.play_next_up, game.players[next]),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (budget.isNotBlank()) {
                Text(
                    stringResource(R.string.play_budget, budget),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun GiftCard(
    gift: Gift,
    holderName: String,
    frozenRuleOn: Boolean,
    block: StealBlock?,
    actionLabel: String,
    onSteal: () -> Unit,
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    giftTitle(gift),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    stringResource(R.string.play_with, holderName),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                StealMeter(steals = gift.steals, limited = frozenRuleOn)
            }
            when (block) {
                null -> FilledTonalButton(onClick = onSteal) { Text(actionLabel) }
                StealBlock.OWN_GIFT -> Unit
                StealBlock.FROZEN -> Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        stringResource(R.string.play_frozen),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.padding(start = 4.dp),
                    )
                }
                StealBlock.STOLEN_THIS_ROUND -> Text(
                    stringResource(R.string.play_stolen_this_round),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
    }
}

/** Bolinhas até o limite de roubos; sem limite, só o número. */
@Composable
private fun StealMeter(steals: Int, limited: Boolean) {
    if (!limited) {
        if (steals > 0) {
            Text(
                stringResource(R.string.play_steals, steals),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.tertiary,
            )
        }
        return
    }
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(Game.MAX_STEALS) { index ->
            Box(
                Modifier
                    .size(8.dp)
                    .background(
                        if (index < steals) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline,
                        CircleShape,
                    ),
            )
        }
    }
}

@Composable
fun giftTitle(gift: Gift): String = stringResource(R.string.gift_untitled, gift.number)
