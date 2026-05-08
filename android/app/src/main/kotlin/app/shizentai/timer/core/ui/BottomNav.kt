package app.shizentai.timer.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.shizentai.timer.R
import app.shizentai.timer.core.theme.Shizentai

enum class NavTab { Timer, Presets, Settings }

@Composable
fun BottomNav(
    selected: NavTab,
    onSelect: (NavTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tabs = listOf(NavTab.Timer, NavTab.Presets, NavTab.Settings)
    val lineColor = Shizentai.colors.line
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Shizentai.colors.paperBasement)
            // Top-only separator. The earlier `.border(BorderStroke(...))` drew
            // all four sides; the L/R lines were visible at the screen edges.
            .drawBehind {
                val stroke = 1.dp.toPx()
                drawLine(
                    color = lineColor,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = stroke,
                )
            },
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        tabs.forEach { tab ->
            val active = tab == selected
            val labelRes = navLabelRes(tab)
            val label = stringResource(labelRes)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelect(tab) }
                    .padding(top = 8.dp, bottom = 10.dp),
                contentAlignment = Alignment.TopCenter,
            ) {
                if (active) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .height(3.dp)
                            .width(28.dp)
                            .background(
                                Shizentai.colors.accent,
                                RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp),
                            ),
                    )
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.padding(top = 4.dp),
                ) {
                    Icon(
                        painter = painterResource(navIconRes(tab)),
                        contentDescription = label,
                        modifier = Modifier.size(22.dp),
                        tint = if (active) Shizentai.colors.ink else Shizentai.colors.ink3,
                    )
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                        color = if (active) Shizentai.colors.ink else Shizentai.colors.ink3,
                    )
                }
            }
        }
    }
}

private fun navIconRes(tab: NavTab): Int = when (tab) {
    NavTab.Timer -> R.drawable.ic_nav_timer
    NavTab.Presets -> R.drawable.ic_nav_presets
    NavTab.Settings -> R.drawable.ic_nav_settings
}

private fun navLabelRes(tab: NavTab): Int = when (tab) {
    NavTab.Timer -> R.string.nav_timer
    NavTab.Presets -> R.string.nav_presets
    NavTab.Settings -> R.string.nav_settings
}
