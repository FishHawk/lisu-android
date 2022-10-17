package com.fishhawk.lisu.widget

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fishhawk.lisu.ui.theme.MediumEmphasis

@Composable
fun LisuListHeader(
    text: String,
    modifier: Modifier = Modifier,
) {
    MediumEmphasis {
        Text(
            text = text,
            modifier = modifier.padding(8.dp),
            style = MaterialTheme.typography.titleSmall,
        )
    }
}

@Composable
fun LisuListItem(
    leadingContent: @Composable () -> Unit,
    headlineText: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    overlineText: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
) {
    Surface {
        Row(
            modifier = modifier
                .height(104.dp)
                .padding(horizontal = 8.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leadingContent()
            Column(modifier = Modifier.weight(1f)) {
                overlineText?.let {
                    MediumEmphasis {
                        ProvideTextStyle(value = MaterialTheme.typography.labelSmall) {
                            it()
                        }
                    }
                }
                ProvideTextStyle(value = MaterialTheme.typography.titleMedium.copy(lineHeight = 20.sp)) {
                    headlineText()
                }
                supportingText?.let {
                    MediumEmphasis {
                        ProvideTextStyle(value = MaterialTheme.typography.bodyMedium) {
                            it()
                        }
                    }
                }
            }
            trailingContent?.let { it() }
        }
    }
}