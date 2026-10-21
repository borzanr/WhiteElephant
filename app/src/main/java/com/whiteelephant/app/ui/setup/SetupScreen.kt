package com.whiteelephant.app.ui.setup

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.whiteelephant.app.R
import com.whiteelephant.app.ads.BannerAd
import com.whiteelephant.app.data.Setup
import com.whiteelephant.app.game.Game
import com.whiteelephant.app.game.Rules
import com.whiteelephant.app.share.InviteChannel
import com.whiteelephant.app.share.formatInviteDate
import com.whiteelephant.app.share.formatInviteTime
import com.whiteelephant.app.ui.AppLanguage
import com.whiteelephant.app.ui.WhiteElephantViewModel
import com.whiteelephant.app.ui.components.DrawButton
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Tela única, em duas partes, na ordem em que a noite acontece:
 *
 * 1. **A festa** — nome, data, hora, valor e regras, que viram o convite. É a
 *    parte principal: o app é usado *antes* da brincadeira, pelo organizador, e
 *    o link leva o nome do app ao grupo inteiro.
 * 2. **Na hora da brincadeira** — nomes e sorteio da ordem. Opcional; quem não
 *    quiser celular na mão durante a troca simplesmente não usa.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SetupScreen(
    setup: Setup,
    savedGame: Game?,
    onEventNameChange: (String) -> Unit,
    onDateChange: (String) -> Unit,
    onTimeChange: (String) -> Unit,
    onBudgetChange: (String) -> Unit,
    onRulesChange: (Rules) -> Unit,
    hasWhatsApp: Boolean,
    onSendInvite: (InviteChannel) -> Unit,
    onAddNames: (String) -> Unit,
    onRemoveName: (String) -> Unit,
    onDrawOrder: () -> Unit,
    onResume: () -> Unit,
    onRules: () -> Unit,
    showPrivacyOptions: Boolean,
    onPrivacyOptions: () -> Unit,
    onPrivacyPolicy: () -> Unit,
) {
    var input by rememberSaveable { mutableStateOf("") }
    var rulesOpen by rememberSaveable { mutableStateOf(false) }
    var menuOpen by remember { mutableStateOf(false) }
    var pickingDate by rememberSaveable { mutableStateOf(false) }
    var pickingTime by rememberSaveable { mutableStateOf(false) }
    var choosingChannel by rememberSaveable { mutableStateOf(false) }
    var choosingLanguage by remember { mutableStateOf(false) }
    // Campos de texto editados localmente e salvos a cada tecla; ler direto do
    // DataStore faria o cursor pular quando a gravação volta. A tela só é
    // composta depois de a preparação salva carregar, então o valor inicial vale.
    var eventName by rememberSaveable { mutableStateOf(setup.eventName) }
    var budget by rememberSaveable { mutableStateOf(setup.budget) }
    val canDraw = setup.names.size >= Game.MIN_PLAYERS
    val locale = LocalConfiguration.current.locales[0]
    val focusManager = LocalFocusManager.current
    val doneClearsFocus = KeyboardActions(onDone = { focusManager.clearFocus() })

    fun submit() {
        if (input.isNotBlank()) onAddNames(input)
        input = ""
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    // Menu com texto, igual ao do Amigo Secreto: um ícone de livro
                    // sozinho não dizia "como funciona" para quem abre pela primeira vez.
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.common_more))
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.rules_title)) },
                            onClick = { menuOpen = false; onRules() },
                        )
                        // Só aparece onde a lei exige (EEE, Reino Unido, Suíça):
                        // fora dessas regiões o formulário não tem o que mostrar.
                        if (showPrivacyOptions) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.menu_privacy_options)) },
                                onClick = { menuOpen = false; onPrivacyOptions() },
                            )
                        }
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.menu_language)) },
                            onClick = { menuOpen = false; choosingLanguage = true },
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.menu_privacy_policy)) },
                            onClick = { menuOpen = false; onPrivacyPolicy() },
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground,
                ),
            )
        },
        bottomBar = {
            // Com o teclado aberto o banner sai do caminho: colado no teclado
            // seria clique acidental.
            if (!WindowInsets.isImeVisible) {
                Column(Modifier.navigationBarsPadding()) { BannerAd() }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                // Tocar fora de um campo encerra a edição. Sem isto o cursor ficava
                // piscando no valor mesmo depois de a pessoa seguir para as regras.
                .pointerInput(Unit) { detectTapGestures { focusManager.clearFocus() } },
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // region a festa

            item { SectionTitle(stringResource(R.string.setup_party_title)) }

            item {
                OutlinedTextField(
                    value = eventName,
                    onValueChange = {
                        eventName = it.take(WhiteElephantViewModel.MAX_EVENT)
                        onEventNameChange(eventName)
                    },
                    singleLine = true,
                    label = { Text(stringResource(R.string.setup_event_label)) },
                    placeholder = { Text(stringResource(R.string.setup_event_hint)) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = doneClearsFocus,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    PickerField(
                        icon = Icons.Outlined.CalendarMonth,
                        label = stringResource(R.string.setup_date),
                        value = formatInviteDate(setup.date, locale, DateFormat.MEDIUM),
                        onClick = { pickingDate = true },
                        modifier = Modifier.weight(1.4f),
                    )
                    PickerField(
                        icon = Icons.Outlined.Schedule,
                        label = stringResource(R.string.setup_time),
                        value = formatInviteTime(setup.time, LocalContext.current),
                        onClick = { pickingTime = true },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = budget,
                    onValueChange = {
                        budget = it.take(WhiteElephantViewModel.MAX_BUDGET)
                        onBudgetChange(budget)
                    },
                    singleLine = true,
                    label = { Text(stringResource(R.string.setup_budget_label)) },
                    placeholder = { Text(stringResource(R.string.setup_budget_hint)) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = doneClearsFocus,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            item {
                RulesRow(
                    rules = setup.rules,
                    open = rulesOpen,
                    onToggle = { rulesOpen = !rulesOpen },
                    onChange = onRulesChange,
                )
            }

            item {
                DrawButton(
                    text = stringResource(R.string.setup_invite),
                    onClick = { choosingChannel = true },
                    modifier = Modifier.padding(top = 4.dp),
                )
            }

            item {
                Text(
                    stringResource(R.string.setup_invite_hint),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // endregion

            // region na hora

            item {
                Column(Modifier.padding(top = 20.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    SectionTitle(stringResource(R.string.setup_game_title))
                    Text(
                        stringResource(R.string.setup_game_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // A partida salva mora na parte dela, não no topo: no topo competia
            // com o convite, que é a ação principal da tela.
            if (savedGame != null) {
                item { ResumeCard(game = savedGame, onClick = onResume) }
            }

            item {
                Text(
                    // Em português o plural "one" cobre o zero ("0 pessoa"): lista
                    // vazia ganha um título sem número.
                    if (setup.names.isEmpty()) {
                        stringResource(R.string.setup_players_empty)
                    } else {
                        pluralStringResource(R.plurals.setup_players, setup.names.size, setup.names.size)
                    },
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it },
                        label = { Text(stringResource(R.string.setup_add_hint)) },
                        supportingText = { Text(stringResource(R.string.setup_add_supporting)) },
                        // Várias linhas para aceitar a lista colada do WhatsApp;
                        // o Enter do teclado ainda adiciona.
                        maxLines = 4,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { submit() }),
                        modifier = Modifier.weight(1f),
                    )
                    FilledTonalIconButton(
                        onClick = ::submit,
                        enabled = input.isNotBlank(),
                        modifier = Modifier.padding(start = 8.dp),
                    ) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.setup_add))
                    }
                }
            }

            items(setup.names, key = { it }) { name ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 16.dp),
                    ) {
                        Text(name, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        IconButton(onClick = { onRemoveName(name) }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = stringResource(R.string.setup_remove, name),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            item {
                // Tonal, não dourado: o dourado da tela é do convite.
                OutlinedButton(
                    onClick = onDrawOrder,
                    enabled = canDraw,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                ) {
                    Text(stringResource(R.string.setup_draw_order), style = MaterialTheme.typography.labelLarge)
                }
                if (!canDraw) {
                    Text(
                        stringResource(R.string.setup_needs_more, Game.MIN_PLAYERS),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }

            // endregion
        }
    }

    if (choosingLanguage) {
        LanguageDialog(onDismiss = { choosingLanguage = false })
    }
    if (choosingChannel) {
        InviteSheet(
            hasWhatsApp = hasWhatsApp,
            onDismiss = { choosingChannel = false },
            onPick = { choosingChannel = false; onSendInvite(it) },
        )
    }
    if (pickingDate) {
        DateDialog(
            isoDate = setup.date,
            onDismiss = { pickingDate = false },
            onPick = { pickingDate = false; onDateChange(it) },
        )
    }
    if (pickingTime) {
        TimeDialog(
            time = setup.time,
            onDismiss = { pickingTime = false },
            onPick = { pickingTime = false; onTimeChange(it) },
        )
    }
}

/** Nome de cada idioma escrito nele mesmo, para quem não lê o idioma atual achar o seu. */
@Composable
private fun LanguageDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val current = remember { AppLanguage.current(context) }
    val system = stringResource(R.string.language_system)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.menu_language)) },
        text = {
            Column {
                AppLanguage.OPTIONS.forEach { tag ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.small)
                            .clickable {
                                onDismiss()
                                if (tag != current) {
                                    AppLanguage.set(context, tag)
                                    // Recria a Activity para attachBaseContext aplicar o
                                    // idioma novo. Preparação e partida vivem no
                                    // ViewModel e no DataStore e sobrevivem.
                                    (context as? Activity)?.recreate()
                                }
                            }
                            .padding(vertical = 4.dp),
                    ) {
                        RadioButton(selected = tag == current, onClick = null)
                        Text(
                            text = AppLanguage.nativeName(tag) ?: system,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(start = 12.dp),
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) }
        },
    )
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.headlineSmall)
}

/** Campo que abre um seletor: parece campo, mas é um botão. */
@Composable
private fun PickerField(
    icon: ImageVector,
    label: String,
    value: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        shape = MaterialTheme.shapes.extraSmall,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
        modifier = modifier.heightIn(min = 56.dp),
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            value ?: label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (value == null) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            modifier = Modifier.weight(1f).padding(start = 8.dp),
        )
    }
}

/**
 * O DatePicker do Material trabalha em milissegundos UTC; a data salva é texto
 * "yyyy-MM-dd". A conversão fica em UTC dos dois lados para o dia não escorregar
 * com o fuso.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateDialog(isoDate: String, onDismiss: () -> Unit, onPick: (String) -> Unit) {
    val utc = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }
    }
    val initial = remember(isoDate) { runCatching { utc.parse(isoDate)?.time }.getOrNull() }
    val state = rememberDatePickerState(initialSelectedDateMillis = initial ?: System.currentTimeMillis())
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val millis = state.selectedDateMillis
                if (millis == null) onDismiss() else onPick(utc.format(Date(millis)))
            }) { Text(stringResource(R.string.common_ok)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) }
        },
    ) {
        DatePicker(state = state)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeDialog(time: String, onDismiss: () -> Unit, onPick: (String) -> Unit) {
    val (hour, minute) = remember(time) {
        val parts = time.split(":").mapNotNull { it.toIntOrNull() }
        if (parts.size == 2) parts[0] to parts[1] else 19 to 0
    }
    val context = LocalContext.current
    val state = rememberTimePickerState(
        initialHour = hour,
        initialMinute = minute,
        is24Hour = android.text.format.DateFormat.is24HourFormat(context),
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        text = { TimePicker(state = state) },
        confirmButton = {
            TextButton(onClick = { onPick("%02d:%02d".format(Locale.US, state.hour, state.minute)) }) {
                Text(stringResource(R.string.common_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) }
        },
    )
}

@Composable
private fun ResumeCard(game: Game, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                stringResource(if (game.isFinished) R.string.setup_last_results else R.string.setup_resume),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.tertiary,
            )
            Text(
                stringResource(R.string.setup_resume_progress, game.round, game.players.size),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * "Mudar regras": fechada mostra só o resumo, aberta mostra as três chaves. Os
 * padrões servem para a maioria; quem quer mudar acha, quem não quer nem vê.
 */
@Composable
private fun RulesRow(
    rules: Rules,
    open: Boolean,
    onToggle: () -> Unit,
    onChange: (Rules) -> Unit,
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().clickable(onClick = onToggle).padding(16.dp),
        ) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.setup_change_rules), style = MaterialTheme.typography.titleMedium)
                Text(
                    stringResource(
                        if (rules == Rules()) R.string.setup_rules_default else R.string.setup_rules_custom,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(if (open) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null)
        }
        AnimatedVisibility(visible = open) {
            Column {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                RuleSwitch(
                    title = stringResource(R.string.rule_freeze, Game.MAX_STEALS),
                    body = stringResource(R.string.rule_freeze_body, Game.MAX_STEALS),
                    checked = rules.freezeAfterMax,
                    onChange = { onChange(rules.copy(freezeAfterMax = it)) },
                )
                RuleSwitch(
                    title = stringResource(R.string.rule_no_steal_back),
                    body = stringResource(R.string.rule_no_steal_back_body),
                    checked = rules.noStealBack,
                    onChange = { onChange(rules.copy(noStealBack = it)) },
                )
                RuleSwitch(
                    title = stringResource(R.string.rule_final_swap),
                    body = stringResource(R.string.rule_final_swap_body),
                    checked = rules.finalSwap,
                    onChange = { onChange(rules.copy(finalSwap = it)) },
                )
                if (rules != Rules()) {
                    TextButton(onClick = { onChange(Rules()) }, modifier = Modifier.padding(horizontal = 8.dp)) {
                        Text(stringResource(R.string.setup_rules_reset))
                    }
                }
            }
        }
    }
}

@Composable
private fun RuleSwitch(title: String, body: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Column(Modifier.weight(1f).padding(end = 12.dp)) {
            Text(title, style = MaterialTheme.typography.bodyMedium)
            Text(body, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
