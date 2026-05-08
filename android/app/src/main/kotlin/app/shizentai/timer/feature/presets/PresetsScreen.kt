package app.shizentai.timer.feature.presets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import app.shizentai.timer.R
import app.shizentai.timer.core.theme.Shizentai
import app.shizentai.timer.data.presets.Preset
import app.shizentai.timer.engine.TimerConfig

@Composable
fun PresetsScreen(viewModel: PresetsViewModel = hiltViewModel()) {
    val presets by viewModel.presets.collectAsState()
    val activeId by viewModel.activeId.collectAsState()
    var dialogOpen by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { dialogOpen = true },
                containerColor = Shizentai.colors.accent,
                contentColor = androidx.compose.ui.graphics.Color.White,
                shape = CircleShape,
            ) { Text("+", fontSize = 28.sp, fontWeight = FontWeight.Bold) }
        },
        containerColor = Shizentai.colors.paper,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Text(
                    stringResource(R.string.presets_title),
                    style = Shizentai.typography.screenTitle,
                    color = Shizentai.colors.ink,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }
            items(presets, key = { it.id }) { preset ->
                PresetRow(
                    preset = preset,
                    isActive = preset.id == activeId,
                    onSelect = { viewModel.select(preset.id) },
                    onDelete = if (preset.isBuiltIn) null else { -> viewModel.delete(preset.id) },
                )
            }
        }
    }

    if (dialogOpen) {
        AddPresetDialog(
            onDismiss = { dialogOpen = false },
            onCreate = { name, config ->
                viewModel.add(name, config)
                dialogOpen = false
            },
        )
    }
}

@Composable
private fun PresetRow(
    preset: Preset,
    isActive: Boolean,
    onSelect: () -> Unit,
    onDelete: (() -> Unit)?,
) {
    val border = if (isActive) Shizentai.colors.accent else Shizentai.colors.line
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Shizentai.colors.paper2, RoundedCornerShape(12.dp))
            .clickable(onClick = onSelect)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .background(if (isActive) Shizentai.colors.accent else androidx.compose.ui.graphics.Color.Transparent, CircleShape)
                .padding(2.dp),
        ) {
            // Hidden indicator dot — border only when inactive.
            if (!isActive) {
                Box(modifier = Modifier.size(10.dp).background(border, CircleShape))
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                preset.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Shizentai.colors.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                "${preset.config.rounds} rounds · ${preset.config.workSeconds}s / ${preset.config.restSeconds}s",
                fontSize = 13.sp,
                color = Shizentai.colors.ink3,
            )
        }
        if (onDelete != null) {
            TextButton(onClick = onDelete) {
                Text(stringResource(R.string.action_delete), color = Shizentai.colors.accent, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun AddPresetDialog(
    onDismiss: () -> Unit,
    onCreate: (String, TimerConfig) -> Unit,
) {
    var name by remember { mutableStateOf(TextFieldValue("")) }
    var work by remember { mutableStateOf(TextFieldValue("60")) }
    var rest by remember { mutableStateOf(TextFieldValue("30")) }
    var rounds by remember { mutableStateOf(TextFieldValue("3")) }
    var prep by remember { mutableStateOf(TextFieldValue("10")) }
    var warning by remember { mutableStateOf(TextFieldValue("10")) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.presets_new_title), style = Shizentai.typography.cardTitleLg) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LabeledField(stringResource(R.string.field_name), name) { name = it }
                LabeledField(stringResource(R.string.field_work), work, KeyboardType.Number) { work = it }
                LabeledField(stringResource(R.string.field_rest), rest, KeyboardType.Number) { rest = it }
                LabeledField(stringResource(R.string.field_rounds), rounds, KeyboardType.Number) { rounds = it }
                LabeledField(stringResource(R.string.field_prep), prep, KeyboardType.Number) { prep = it }
                LabeledField(stringResource(R.string.field_warning), warning, KeyboardType.Number) { warning = it }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cfg = TimerConfig(
                        workSeconds = work.text.toIntOrNull()?.coerceAtLeast(1) ?: 60,
                        restSeconds = rest.text.toIntOrNull()?.coerceAtLeast(0) ?: 30,
                        rounds = rounds.text.toIntOrNull()?.coerceAtLeast(1) ?: 3,
                        prepSeconds = prep.text.toIntOrNull()?.coerceAtLeast(0) ?: 10,
                        warningSeconds = warning.text.toIntOrNull()?.coerceAtLeast(0) ?: 10,
                        signalEndOfRest = true,
                    )
                    val finalName = name.text.trim().ifEmpty { "Custom" }
                    onCreate(finalName, cfg)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Shizentai.colors.accent),
            ) { Text(stringResource(R.string.action_create)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
        containerColor = Shizentai.colors.paper,
    )
}

@Composable
private fun LabeledField(
    label: String,
    value: TextFieldValue,
    keyboardType: KeyboardType = KeyboardType.Text,
    onChange: (TextFieldValue) -> Unit,
) {
    Column {
        Text(label, fontSize = 11.sp, color = Shizentai.colors.ink3, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Shizentai.colors.paper2, RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
        ) {
            BasicTextField(
                value = value,
                onValueChange = onChange,
                singleLine = true,
                textStyle = TextStyle(color = Shizentai.colors.ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = keyboardType),
                cursorBrush = androidx.compose.ui.graphics.SolidColor(Shizentai.colors.accent),
            )
        }
    }
}
