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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberCard
import com.example.ui.theme.CyberNeonBlue
import com.example.ui.theme.CyberNeonGreen
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
            .background(BackgroundColor.copy(alpha = 0.96f))
            .border(width = 1.dp, color = CyberBorder)
            .navigationBarsPadding()
            .height(64.dp)
            .testTag("callminder_bottom_nav")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(64.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                icon = Icons.Default.Schedule,
                label = "HOME",
                isSelected = selectedTab == MainNavigationTab.HOME,
                onClick = { onTabSelected(MainNavigationTab.HOME) }
            )

            BottomNavItem(
                icon = Icons.Default.PhoneCallback,
                label = "NEW CALL",
                isSelected = selectedTab == MainNavigationTab.NEW_CALL,
                onClick = { onTabSelected(MainNavigationTab.NEW_CALL) }
            )

            BottomNavItem(
                icon = Icons.Default.PhoneInTalk,
                label = "LIVE",
                isSelected = selectedTab == MainNavigationTab.LIVE_CALL,
                hasLiveDot = true,
                onClick = { onTabSelected(MainNavigationTab.LIVE_CALL) }
            )

            BottomNavItem(
                icon = Icons.Default.ReceiptLong,
                label = "HISTORY",
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
        targetValue = if (isSelected) CyberNeonBlue else SubtleText,
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
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("nav_item_${label.lowercase().replace(" ", "_")}")
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(if (isSelected) 36.dp else 28.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (isSelected) CyberCard else androidx.compose.ui.graphics.Color.Transparent)
                .border(if (isSelected) 1.dp else 0.dp, if (isSelected) CyberNeonBlue else androidx.compose.ui.graphics.Color.Transparent, RoundedCornerShape(10.dp))
        ) {
            Box {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = contentColor,
                    modifier = Modifier.size(20.dp)
                )

                if (hasLiveDot) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(CyberNeonGreen)
                            .align(Alignment.TopEnd)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            color = contentColor,
            letterSpacing = 0.5.sp
        )
    }
}
