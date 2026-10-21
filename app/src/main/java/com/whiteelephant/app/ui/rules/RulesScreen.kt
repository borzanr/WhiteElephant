package com.whiteelephant.app.ui.rules

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.whiteelephant.app.R
import com.whiteelephant.app.game.Game
import com.whiteelephant.app.game.Rules

private data class Topic(@param:StringRes val title: Int, @param:StringRes val body: Int)

/** Ordem da noite: antes, durante, roubos, fim. */
private val TOPICS = listOf(
    Topic(R.string.rules_bring_title, R.string.rules_bring_body),
    Topic(R.string.rules_order_title, R.string.rules_order_body),
    Topic(R.string.rules_turn_title, R.string.rules_turn_body),
    Topic(R.string.rules_stolen_title, R.string.rules_stolen_body),
    Topic(R.string.rules_names_title, R.string.rules_names_body),
)

/**
 * Regras da brincadeira para ler em voz alta antes de começar. O primeiro cartão
 * diz quais das três regras variáveis valem hoje — é a dúvida que aparece no
 * meio da partida ("pode roubar de volta?").
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RulesScreen(rules: Rules, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.rules_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                RuleCard(
                    title = stringResource(R.string.rules_tonight),
                    body = listOf(
                        if (rules.freezeAfterMax) {
                            stringResource(R.string.rules_tonight_freeze_on, Game.MAX_STEALS)
                        } else {
                            stringResource(R.string.rules_tonight_freeze_off)
                        },
                        stringResource(
                            if (rules.noStealBack) R.string.rules_tonight_steal_back_off else R.string.rules_tonight_steal_back_on,
                        ),
                        stringResource(
                            if (rules.finalSwap) R.string.rules_tonight_final_on else R.string.rules_tonight_final_off,
                        ),
                    ).joinToString("\n") { "• $it" },
                )
            }
            items(TOPICS) { topic ->
                RuleCard(title = stringResource(topic.title), body = stringResource(topic.body))
            }
        }
    }
}

@Composable
private fun RuleCard(title: String, body: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
