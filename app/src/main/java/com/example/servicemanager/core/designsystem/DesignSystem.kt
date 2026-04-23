package com.example.servicemanager.core.designsystem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.servicemanager.core.domain.ServiceStatus
import com.example.servicemanager.core.domain.TimelineState

private val SentinelDeepBlue = Color(0xFF002B59)
private val SentinelDeepBlueElevated = Color(0xFF1A4175)
private val SentinelSurfaceBase = Color(0xFFF8F9FC)
private val SentinelSurfaceLow = Color(0xFFF1F3F9)
private val SentinelSurfaceCard = Color(0xFFFFFFFF)
private val SentinelSurfaceHigh = Color(0xFFE8EAF0)
private val SentinelSurfaceHighest = Color(0xFFDDE1E9)
private val SentinelSuccess = Color(0xFF0D9488)
private val SentinelSuccessTint = Color(0xFFF0FDFA)
private val SentinelDanger = Color(0xFFE11D48)
private val SentinelDangerTint = Color(0xFFFFF1F2)
private val SentinelWarningTint = Color(0xFFFFFBEB)
private val SentinelWarningText = Color(0xFFB45309)
private val SentinelOutlineGhost = Color(0xFFE2E8F0)
private val SentinelInk = Color(0xFF0F172A)
private val SentinelMutedInk = Color(0xFF64748B)

private val SentinelShapes = androidx.compose.material3.Shapes(
    extraSmall = RoundedCornerShape(0.dp),
    small = RoundedCornerShape(0.dp),
    medium = RoundedCornerShape(0.dp),
    large = RoundedCornerShape(0.dp),
    extraLarge = RoundedCornerShape(0.dp),
)

private val SentinelTypography = androidx.compose.material3.Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.5).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 22.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = 1.4.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 1.1.sp,
    ),
)

private val SentinelColors = androidx.compose.material3.lightColorScheme(
    primary = SentinelDeepBlue,
    onPrimary = Color.White,
    primaryContainer = SentinelDeepBlueElevated,
    onPrimaryContainer = Color.White,
    secondary = SentinelDeepBlueElevated,
    onSecondary = Color.White,
    background = SentinelSurfaceBase,
    onBackground = SentinelInk,
    surface = SentinelSurfaceBase,
    onSurface = SentinelInk,
    inverseSurface = SentinelInk,
    inverseOnSurface = Color.White,
    surfaceContainerLow = SentinelSurfaceLow,
    surfaceContainer = SentinelSurfaceHigh,
    surfaceContainerHighest = SentinelSurfaceHighest,
    surfaceContainerLowest = SentinelSurfaceCard,
    outlineVariant = SentinelOutlineGhost,
    error = SentinelDanger,
)

@Composable
fun SentinelTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SentinelColors,
        typography = SentinelTypography,
        shapes = SentinelShapes,
        content = content,
    )
}

@Composable
fun ScreenBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background,
        content = content,
    )
}

@Composable
fun SectionLabel(
    text: String,
    action: String? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = SentinelMutedInk,
        )
        if (action != null) {
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = action.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = SentinelDeepBlue,
                modifier = Modifier.clickable { },
            )
        }
    }
}

@Composable
fun CountChip(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = SentinelDeepBlue,
) {
    Box(
        modifier = modifier
            .background(color = color)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = Color.White,
        )
    }
}

@Composable
fun StatusBadge(status: ServiceStatus, modifier: Modifier = Modifier) {
    val background = when (status) {
        ServiceStatus.QUEUED -> SentinelSurfaceHighest
        ServiceStatus.IN_PROGRESS -> SentinelDeepBlue
        ServiceStatus.DIAGNOSTICS -> SentinelDeepBlueElevated
        ServiceStatus.WAITING_FOR_SPARE -> SentinelDeepBlueElevated
        ServiceStatus.READY_FOR_PICKUP -> SentinelSuccessTint
        ServiceStatus.COMPLETED -> SentinelSuccessTint
        ServiceStatus.CANCELLED -> SentinelDangerTint
    }
    val textColor = when (status) {
        ServiceStatus.READY_FOR_PICKUP -> SentinelSuccess
        ServiceStatus.COMPLETED -> SentinelSuccess
        ServiceStatus.QUEUED -> SentinelMutedInk
        ServiceStatus.CANCELLED -> SentinelDanger
        else -> Color.White
    }
    Box(
        modifier = modifier
            .background(background)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(
            text = when (status) {
                ServiceStatus.COMPLETED -> "COMPLETE"
                ServiceStatus.READY_FOR_PICKUP -> "READY FOR PICKUP"
                else -> status.name.replace("_", " ")
            },
            style = MaterialTheme.typography.labelLarge.copy(fontSize = 10.sp, letterSpacing = 1.8.sp),
            color = textColor,
        )
    }
}

@Composable
fun SentinelCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(0.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(0.dp),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = content,
        )
    }
}

@Composable
fun PrimaryActionButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(56.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 16.dp),
        shape = RoundedCornerShape(0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = SentinelDeepBlue,
            disabledContainerColor = SentinelSurfaceHigh,
            disabledContentColor = SentinelMutedInk
        ),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = if (enabled) Color.White else SentinelMutedInk,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun SecondaryActionButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(56.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 16.dp),
        shape = RoundedCornerShape(0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = SentinelDeepBlue,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = SentinelMutedInk
        ),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun SentinelSnackbarHost(hostState: SnackbarHostState) {
    SnackbarHost(hostState) { data ->
        Snackbar(
            snackbarData = data,
            containerColor = SentinelDeepBlue,
            contentColor = Color.White,
            shape = RoundedCornerShape(0.dp)
        )
    }
}

@Composable
fun SentinelTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    singleLine: Boolean = true,
    hasBorder: Boolean = true,
    trailingIcon: @Composable (() -> Unit)? = null,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier.height(16.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            if (label.isNotBlank()) {
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                    color = SentinelMutedInk,
                )
            }
        }
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .then(if (hasBorder) Modifier.border(0.5.dp, MaterialTheme.colorScheme.outlineVariant) else Modifier),
            singleLine = singleLine,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = SentinelInk,
                unfocusedTextColor = SentinelInk,
                focusedPlaceholderColor = SentinelMutedInk,
                unfocusedPlaceholderColor = SentinelMutedInk,
                cursorColor = SentinelDeepBlue,
            ),
            placeholder = {
                Text(text = placeholder ?: label, color = SentinelMutedInk, style = MaterialTheme.typography.bodyMedium)
            },
            trailingIcon = trailingIcon,
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
        )
    }
}

@Composable
fun SentinelDropdownField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    options: List<String>,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        SentinelTextField(
            value = value,
            onValueChange = { },
            label = label,
            placeholder = placeholder,
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = SentinelMutedInk
                )
            },
            modifier = Modifier.fillMaxWidth()
        )
        // This transparent box covers the TextField to handle clicks
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(top = 28.dp) // Leave space for the label
                .clickable { expanded = true }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth(0.9f) // Adjust width to match input field
                .background(Color.White)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { 
                        Text(
                            text = option, 
                            style = MaterialTheme.typography.bodyLarge,
                            color = SentinelInk
                        ) 
                    },
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun DetailBlock(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(
            modifier = Modifier.height(16.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                color = SentinelMutedInk,
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            color = SentinelInk,
        )
    }
}

@Composable
fun MetricTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(text = value, style = MaterialTheme.typography.headlineMedium)
        Text(text = label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun TimelineRow(
    title: String,
    time: String,
    state: TimelineState,
    modifier: Modifier = Modifier,
    isLast: Boolean = false,
) {
    Row(modifier = modifier.height(56.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        color = when (state) {
                            TimelineState.DONE -> SentinelSuccess
                            TimelineState.ACTIVE -> SentinelDeepBlue
                            TimelineState.PENDING -> SentinelSurfaceHighest
                        },
                        shape = RoundedCornerShape(6.dp),
                    ),
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .weight(1f)
                        .background(SentinelOutlineGhost),
                )
            }
        }
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = if (state == TimelineState.PENDING) SentinelMutedInk else SentinelInk,
            )
            Text(text = time, style = MaterialTheme.typography.labelSmall, color = SentinelMutedInk)
        }
    }
}

@Composable
fun StatusChoiceList(
    selectedStatus: ServiceStatus,
    onStatusSelected: (ServiceStatus) -> Unit,
    options: List<ServiceStatus> = ServiceStatus.values().toList(),
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { status ->
            val isSelected = status == selectedStatus
            Surface(
                onClick = { onStatusSelected(status) },
                shape = RoundedCornerShape(0.dp),
                color = if (isSelected) SentinelDeepBlue else Color.White,
                border = BorderStroke(0.5.dp, if (isSelected) SentinelDeepBlue else SentinelOutlineGhost),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = status.name.replace("_", " "),
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = if (isSelected) Color.White else SentinelInk,
                    )
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SegmentedChoiceRow(
    options: List<String>,
    selected: String,
    modifier: Modifier = Modifier,
    onSelect: (String) -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .border(1.dp, SentinelOutlineGhost, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp)),
    ) {
        options.forEachIndexed { index, option ->
            val isSelected = option == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(if (isSelected) SentinelDeepBlue else Color.White)
                    .clickable { onSelect(option) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = option.replace("_", " "),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) Color.White else SentinelMutedInk,
                    textAlign = TextAlign.Center,
                )
            }
            if (index < options.size - 1) {
                Spacer(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                        .background(SentinelOutlineGhost),
                )
            }
        }
    }
}
@Composable
fun ModalSurface(
    title: String? = null,
    subtitle: String? = null,
    onDismiss: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp), // Sentinel style: square
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            if (title != null || onDismiss != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        if (title != null) {
                            Text(
                                text = title.uppercase(),
                                style = MaterialTheme.typography.headlineMedium,
                                color = SentinelInk
                            )
                        }
                        if (subtitle != null) {
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = SentinelMutedInk
                            )
                        }
                    }
                    if (onDismiss != null) {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Close", tint = SentinelInk)
                        }
                    }
                }
            }
            content()
        }
    }
}

@Composable
fun GradientFab(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(containerColor = SentinelDeepBlue),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null)
            Text(text = text, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun WarningPanel(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SentinelWarningTint)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = SentinelWarningText,
        )
        Text(text = message, style = MaterialTheme.typography.bodySmall, color = SentinelWarningText)
    }
}

@Composable
fun ScreenTitle(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(text = title, style = MaterialTheme.typography.headlineLarge)
        if (subtitle != null) {
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = SentinelMutedInk)
        }
    }
}

@Composable
fun MetadataStrip(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = SentinelMutedInk)
        Text(text = value, style = MaterialTheme.typography.labelSmall, color = SentinelInk)
    }
}

@Composable
fun InlineKeyValue(
    key: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = key, style = MaterialTheme.typography.bodyMedium, color = SentinelMutedInk)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun EmptyState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineSmall)
        Text(text = message, style = MaterialTheme.typography.bodyMedium, color = SentinelMutedInk)
    }
}

object DesignTokens {
    val SurfaceLow = SentinelSurfaceLow
    val SurfaceHigh = SentinelSurfaceHigh
    val SurfaceCard = SentinelSurfaceCard
    val Ink = SentinelInk
    val MutedInk = SentinelMutedInk
    val Success = SentinelSuccess
    val Danger = SentinelDanger
}
