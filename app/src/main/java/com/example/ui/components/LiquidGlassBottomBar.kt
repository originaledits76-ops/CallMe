package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.MainNavigationTab
import com.example.ui.theme.LiquidGlassBackground
import com.example.ui.theme.LiquidGlassBorder
import com.example.ui.theme.LiquidGlassHighlight
import com.example.ui.theme.MutedTextGray
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SolidBlack
import com.example.ui.theme.SophisticatedBlue
import com.example.ui.theme.SophisticatedBlueGlow

@Composable
fun LiquidGlassBottomBar(
    selectedTab: MainNavigationTab,
    onTabSelected: (MainNavigationTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = listOf(
        MainNavigationTab.CALLS to Icons.Default.Call,
        MainNavigationTab.HISTORY to Icons.Default.History,
        MainNavigationTab.SETTINGS to Icons.Default.Settings
    )

    // Floating island container: wrapped tightly around icons, rounded corners, liquid glass
    Box(
        modifier = modifier
            .wrapContentWidth()
            .shadow(
                elevation = 20.dp,
                shape = CircleShape,
                ambientColor = Color(0x18000000),
                spotColor = Color(0x28000000)
            )
            .clip(CircleShape)
            .background(LiquidGlassBackground)
            .border(
                width = 1.2.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        LiquidGlassHighlight,
                        LiquidGlassBorder
                    )
                ),
                shape = CircleShape
            )
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag("floating_liquid_glass_nav")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tabs.forEach { (tab, icon) ->
                val isSelected = selectedTab == tab
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.05f else 0.95f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    label = "tab_scale"
                )
                val backgroundColor by animateColorAsState(
                    targetValue = if (isSelected) SophisticatedBlue else Color.Transparent,
                    animationSpec = tween(durationMillis = 240),
                    label = "tab_bg_color"
                )
                val iconColor by animateColorAsState(
                    targetValue = if (isSelected) PureWhite else MutedTextGray,
                    animationSpec = tween(durationMillis = 200),
                    label = "tab_icon_color"
                )

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .scale(scale)
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(backgroundColor)
                        .clickable {
                            onTabSelected(tab)
                        }
                        .testTag("nav_tab_${tab.name.lowercase()}")
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = tab.name,
                        tint = iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
