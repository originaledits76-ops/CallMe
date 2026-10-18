package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhoneCallback
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainNavigationTab
import com.example.ui.theme.BackgroundColor
import com.example.ui.theme.BorderLight
import com.example.ui.theme.DarkNeutral
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.SubtleText

@Composable
fun CallMinderBottomNav(
    selectedTab: MainNavigationTab,
    onTabSelected: (MainNavigationTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(BackgroundColor.copy(alpha = 0.98f))
            .border(width = 0.8.dp, color = BorderLight)
            .navigationBarsPadding()
            .height(60.dp)
            .testTag("callminder_bottom_nav")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(60.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tab 1: Home
            BottomNavItem(
                icon = Icons.Default.Schedule,
                label = "Home",
                isSelected = selectedTab == MainNavigationTab.HOME,
                onClick = { onTabSelected(MainNavigationTab.HOME) }
            )

            // Tab 2: New Call
            BottomNavItem(
                icon = Icons.Default.PhoneCallback,
                label = "New Call",
                isSelected = selectedTab == MainNavigationTab.NEW_CALL,
                onClick = { onTabSelected(MainNavigationTab.NEW_CALL) }
            )

            // Tab 3: Live Call (with emerald dot)
            BottomNavItem(
                icon = Icons.Default.PhoneInTalk,
                label = "Live Call",
                isSelected = selectedTab == MainNavigationTab.LIVE_CALL,
                hasLiveDot = true,
                onClick = { onTabSelected(MainNavigationTab.LIVE_CALL) }
            )

            // Tab 4: History
            BottomNavItem(
                icon = Icons.Default.ReceiptLong,
                label = "History",
                isSelected = selectedTab == MainNavigationTab.HISTORY,
                onClick = { onTabSelected(MainNavigationTab.HISTORY) }
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    hasLiveDot: Boolean = false,
    onClick: () -> Unit
) {
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) DarkNeutral else SubtleText,
        label = "nav_item_color"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .testTag("nav_item_${label.lowercase().replace(" ", "_")}")
    ) {
        Box {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(22.dp)
            )

            if (hasLiveDot) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(EmeraldGreen)
                        .align(Alignment.TopEnd)
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = contentColor,
            letterSpacing = (-0.2).sp
        )
    }
}
