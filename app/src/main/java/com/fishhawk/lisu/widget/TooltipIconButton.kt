package com.fishhawk.lisu.widget

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.PlainTooltipBox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TooltipIconButton(
    tooltip: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PlainTooltipBox(tooltip = { Text(tooltip) }) {
        IconButton(
            modifier = modifier.tooltipAnchor(),
            onClick = onClick,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = tooltip,
            )
        }
    }
}