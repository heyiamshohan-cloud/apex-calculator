package com.shohankhan.apexcalculator.core.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shohankhan.apexcalculator.core.designsystem.ApexColors
import com.shohankhan.apexcalculator.core.designsystem.ApexIcons
import com.shohankhan.apexcalculator.core.designsystem.ApexRadius
import com.shohankhan.apexcalculator.core.designsystem.ApexSpacing
import com.shohankhan.apexcalculator.core.designsystem.SmallPill
import com.shohankhan.apexcalculator.core.designsystem.apexColors

/** Consistent list row used by the formula catalogue, solvers and tool hubs. */
@Composable
fun CatalogRow(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    badge: String? = null,
    icon: ImageVector? = null,
    isFavorite: Boolean = false,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.apexColors
    Surface(
        color = colors.surface,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(ApexRadius.md),
        modifier = modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = ApexSpacing.md, vertical = ApexSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Box(
                    modifier = Modifier
                        .size(36.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(icon, contentDescription = null, tint = colors.primary, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(ApexSpacing.sm))
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (isFavorite) {
                        Spacer(Modifier.width(ApexSpacing.xs))
                        Icon(
                            imageVector = ApexIcons.Star,
                            contentDescription = "Favorite",
                            tint = colors.warning,
                            modifier = Modifier.size(13.dp),
                        )
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (badge != null) {
                Spacer(Modifier.width(ApexSpacing.sm))
                SmallPill(badge)
            }
            Spacer(Modifier.width(ApexSpacing.xs))
            Icon(
                imageVector = ApexIcons.ChevronRight,
                contentDescription = null,
                tint = colors.textTertiary,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    val colors: ApexColors = MaterialTheme.apexColors
    com.shohankhan.apexcalculator.core.designsystem.ApexTextField(
        value = query,
        onValueChange = onQueryChange,
        label = placeholder,
        modifier = modifier,
        singleLine = true,
    )
}

@Composable
fun StepList(steps: List<String>, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.apexColors
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(ApexSpacing.sm)) {
        steps.forEachIndexed { index, step ->
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(20.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "${index + 1}",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.primary,
                    )
                }
                Spacer(Modifier.width(ApexSpacing.sm))
                Text(
                    text = step,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                )
            }
        }
    }
}
