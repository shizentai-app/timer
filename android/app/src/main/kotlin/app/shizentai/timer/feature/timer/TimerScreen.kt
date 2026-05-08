package app.shizentai.timer.feature.timer

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import app.shizentai.timer.R
import app.shizentai.timer.core.screen.KeepScreenOn
import app.shizentai.timer.core.screen.SystemBarsColor
import app.shizentai.timer.core.theme.Shizentai
import app.shizentai.timer.engine.Phase
import app.shizentai.timer.engine.formatMmSs

@Composable
fun TimerScreen(viewModel: TimerViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    val preset by viewModel.activePreset.collectAsState()
    val settings by viewModel.settings.collectAsState()
    KeepScreenOn(active = state.isRunning && settings.keepScreenOn)

    val palette = phasePalette(state.phase)
    // System bars snap to the phase color (no animation) — the inner Box bg
    // animates separately via animateColorAsState for a smooth transition.
    SystemBarsColor(palette.bg)
    val bg by animateColorAsState(palette.bg, animationSpec = tween(220), label = "phase-bg")
    val ink by animateColorAsState(palette.ink, animationSpec = tween(220), label = "phase-ink")

    val isIdle = state.phase == Phase.IDLE
    val isFinished = state.phase == Phase.FINISHED
    val isRunning = state.isRunning
    val isPaused = !isRunning && !isIdle && !isFinished

    val display = if (isIdle) formatMmSs(preset.config.workSeconds * 1000L) else formatMmSs(state.remainingMs)
    val subtitle = when {
        isIdle -> stringResource(R.string.rounds_summary, preset.config.rounds, preset.config.workSeconds, preset.config.restSeconds)
        isFinished -> stringResource(R.string.finished_summary, state.totalRounds, state.totalRounds)
        else -> stringResource(R.string.round_progress, state.round.coerceAtLeast(1), state.totalRounds)
    }
    val phaseTitle = phaseLabel(state.phase)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            // Top zone: phase command (during a phase) or preset name (idle).
            Box(
                modifier = Modifier.fillMaxWidth().height(72.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (isIdle) {
                    Text(
                        text = preset.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.6.sp,
                        color = ink.copy(alpha = 0.85f),
                    )
                } else {
                    Text(
                        text = phaseTitle,
                        style = Shizentai.typography.phaseCommand,
                        color = ink,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            // Middle zone: large digit + round counter. The digit auto-sizes
            // to fill the available width — Manrope ExtraBold tabular numerals
            // average ~0.55em per glyph, so a 5-char "MM:SS" needs ~2.8em of
            // width; we divide accordingly and clamp.
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 24.dp),
            ) {
                BoxWithConstraints(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    val density = LocalDensity.current
                    val widthSp = with(density) { maxWidth.toSp().value }
                    val computed = (widthSp / 2.8f).coerceIn(72f, 200f)
                    Text(
                        text = display,
                        style = Shizentai.typography.timerDigit.copy(fontSize = computed.sp),
                        color = ink,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                    )
                }
                Text(
                    text = subtitle,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ink.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center,
                )
            }

            // Bottom zone: actions. Primary button always on its own row;
            // Skip + Reset (paused only) on a second row with equal weight so
            // they fit any language's word lengths (Ukrainian "Продовжити" /
            // "Скинути" are wider than the English equivalents).
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                when {
                    isIdle || isFinished -> PrimaryButton(
                        label = stringResource(if (isFinished) R.string.action_restart else R.string.action_start),
                        ink = ink,
                        onClick = viewModel::start,
                    )
                    isRunning -> PrimaryButton(stringResource(R.string.action_pause), ink, viewModel::pause)
                    else -> PrimaryButton(stringResource(R.string.action_resume), ink, viewModel::resume)
                }
                if (isPaused) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        SecondaryButton(
                            stringResource(R.string.action_skip),
                            ink,
                            viewModel::skip,
                            modifier = Modifier.weight(1f),
                        )
                        SecondaryButton(
                            stringResource(R.string.action_reset),
                            ink,
                            viewModel::reset,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

private data class PhasePalette(val bg: Color, val ink: Color)

@Composable
private fun phasePalette(phase: Phase): PhasePalette {
    val colors = Shizentai.colors
    return when (phase) {
        Phase.WORK -> PhasePalette(colors.phases.work, Color.White)
        Phase.WARNING -> PhasePalette(colors.phases.warning, Color.White)
        Phase.REST -> PhasePalette(colors.phases.rest, Color.White)
        Phase.PREPARE -> PhasePalette(colors.phases.prepare, Color.White)
        Phase.FINISHED -> PhasePalette(colors.phases.finished, Color.White)
        Phase.IDLE -> PhasePalette(colors.paper, colors.ink)
    }
}

@Composable
private fun phaseLabel(phase: Phase): String = stringResource(
    when (phase) {
        Phase.PREPARE -> R.string.phase_prepare
        Phase.WORK -> R.string.phase_work
        Phase.WARNING -> R.string.phase_warning
        Phase.REST -> R.string.phase_rest
        Phase.FINISHED -> R.string.phase_finished
        Phase.IDLE -> R.string.phase_idle
    }
)

@Composable
private fun PrimaryButton(label: String, ink: Color, onClick: () -> Unit) {
    val isWhiteInk = ink == Color.White
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            // White pill always carries dark text; using `Shizentai.colors.ink`
            // here was a bug in dark mode (ink is cream, container is white →
            // invisible). The pill's text colour is tone-locked, not themed.
            containerColor = if (isWhiteInk) Color.White else Shizentai.colors.ink,
            contentColor = if (isWhiteInk) Color(0xFF1A1A1A) else Shizentai.colors.paper,
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.widthIn(min = 160.dp),
    ) {
        Text(label, fontSize = 17.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 4.dp))
    }
}

@Composable
private fun SecondaryButton(
    label: String,
    ink: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = ink),
        shape = RoundedCornerShape(14.dp),
    ) {
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}
