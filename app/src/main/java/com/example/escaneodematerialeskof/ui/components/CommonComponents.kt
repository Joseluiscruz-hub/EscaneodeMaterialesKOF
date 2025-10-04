package com.example.escaneodematerialeskof.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.escaneodematerialeskof.ui.theme.DesignSystem

/**
 * Componentes Reutilizables para Escaneo de Materiales KOF
 * Todos implementan Material Design 3 y Accesibilidad
 */

// ==================== BOTONES ====================

enum class FEMSAButtonStyle {
    Primary, Secondary, Success, Warning, Error, Outlined, Text
}

@Composable
fun FEMSAButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    style: FEMSAButtonStyle = FEMSAButtonStyle.Primary,
    icon: ImageVector? = null,
    loading: Boolean = false
) {
    val containerColor = when (style) {
        FEMSAButtonStyle.Primary -> DesignSystem.Colors.Primary
        FEMSAButtonStyle.Secondary -> DesignSystem.Colors.Secondary
        FEMSAButtonStyle.Success -> DesignSystem.Colors.Success
        FEMSAButtonStyle.Warning -> DesignSystem.Colors.Warning
        FEMSAButtonStyle.Error -> DesignSystem.Colors.Error
        FEMSAButtonStyle.Outlined -> Color.Transparent
        FEMSAButtonStyle.Text -> Color.Transparent
    }

    val contentColor = when (style) {
        FEMSAButtonStyle.Outlined -> DesignSystem.Colors.Primary
        FEMSAButtonStyle.Text -> DesignSystem.Colors.Primary
        else -> DesignSystem.Colors.TextOnPrimary
    }

    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier
            .height(DesignSystem.ComponentSize.buttonHeightLarge)
            .semantics {
                this.contentDescription = text
                role = Role.Button
            },
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = DesignSystem.Alpha.disabled),
            disabledContentColor = contentColor.copy(alpha = DesignSystem.Alpha.disabled)
        ),
        shape = RoundedCornerShape(DesignSystem.CornerRadius.button),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (style == FEMSAButtonStyle.Outlined || style == FEMSAButtonStyle.Text) 0.dp else DesignSystem.Elevation.level2,
            pressedElevation = DesignSystem.Elevation.level4
        ),
        border = if (style == FEMSAButtonStyle.Outlined) ButtonDefaults.outlinedButtonBorder else null,
        contentPadding = PaddingValues(horizontal = DesignSystem.Spacing.lg)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(DesignSystem.IconSize.sm),
                    strokeWidth = 2.dp,
                    color = contentColor
                )
                Spacer(Modifier.width(DesignSystem.Spacing.sm))
            } else if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(DesignSystem.IconSize.button)
                )
                Spacer(Modifier.width(DesignSystem.Spacing.sm))
            }
            Text(
                text = text,
                style = DesignSystem.Typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ==================== CARDS ====================

@Composable
fun FEMSACard(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    iconTint: Color = DesignSystem.Colors.Primary,
    onClick: (() -> Unit)? = null,
    elevation: Dp = DesignSystem.Elevation.level3,
    content: @Composable ColumnScope.() -> Unit = {}
) {
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(clickableModifier)
            .semantics {
                contentDescription = "$title ${subtitle ?: ""}"
                if (onClick != null) role = Role.Button
            },
        shape = RoundedCornerShape(DesignSystem.CornerRadius.card),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        colors = CardDefaults.cardColors(containerColor = DesignSystem.Colors.Surface)
    ) {
        Column(
            modifier = Modifier.padding(DesignSystem.Spacing.cardPadding)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                icon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(DesignSystem.IconSize.lg)
                    )
                    Spacer(Modifier.width(DesignSystem.Spacing.md))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = DesignSystem.Typography.titleLarge,
                        color = DesignSystem.Colors.TextPrimary
                    )
                    subtitle?.let {
                        Spacer(Modifier.height(DesignSystem.Spacing.xs))
                        Text(
                            text = it,
                            style = DesignSystem.Typography.bodyMedium,
                            color = DesignSystem.Colors.TextSecondary
                        )
                    }
                }
            }
            if (content != {}) {
                Spacer(Modifier.height(DesignSystem.Spacing.md))
                content()
            }
        }
    }
}

// ==================== KPI CARDS ====================

@Composable
fun MetricCard(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color = DesignSystem.Colors.Primary,
    modifier: Modifier = Modifier,
    trend: String? = null,
    trendUp: Boolean = true
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "$label: $value ${trend ?: ""}"
            },
        shape = RoundedCornerShape(DesignSystem.CornerRadius.card),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = DesignSystem.Elevation.level2)
    ) {
        Row(
            modifier = Modifier.padding(DesignSystem.Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono con fondo
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(DesignSystem.IconSize.lg)
                )
            }

            Spacer(Modifier.width(DesignSystem.Spacing.md))

            // Información
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = DesignSystem.Typography.bodyMedium,
                    color = DesignSystem.Colors.TextSecondary
                )
                Spacer(Modifier.height(DesignSystem.Spacing.xs))
                Text(
                    text = value,
                    style = DesignSystem.Typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = DesignSystem.Colors.TextPrimary
                )
                trend?.let {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (trendUp) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                            contentDescription = null,
                            tint = if (trendUp) DesignSystem.Colors.Success else DesignSystem.Colors.Error,
                            modifier = Modifier.size(DesignSystem.IconSize.sm)
                        )
                        Spacer(Modifier.width(DesignSystem.Spacing.xs))
                        Text(
                            text = it,
                            style = DesignSystem.Typography.bodySmall,
                            color = if (trendUp) DesignSystem.Colors.Success else DesignSystem.Colors.Error
                        )
                    }
                }
            }
        }
    }
}

// ==================== ALERTAS ====================

enum class AlertType {
    Success, Warning, Error, Info
}

@Composable
fun FEMSAAlert(
    message: String,
    type: AlertType = AlertType.Info,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    dismissible: Boolean = false,
    onDismiss: (() -> Unit)? = null
) {
    val (backgroundColor, iconColor, defaultIcon) = when (type) {
        AlertType.Success -> Triple(
            DesignSystem.Colors.Success.copy(alpha = 0.1f),
            DesignSystem.Colors.Success,
            Icons.Default.CheckCircle
        )
        AlertType.Warning -> Triple(
            DesignSystem.Colors.Warning.copy(alpha = 0.1f),
            DesignSystem.Colors.Warning,
            Icons.Default.Warning
        )
        AlertType.Error -> Triple(
            DesignSystem.Colors.Error.copy(alpha = 0.1f),
            DesignSystem.Colors.Error,
            Icons.Default.Error
        )
        AlertType.Info -> Triple(
            DesignSystem.Colors.Info.copy(alpha = 0.1f),
            DesignSystem.Colors.Info,
            Icons.Default.Info
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "${type.name}: $message"
            },
        shape = RoundedCornerShape(DesignSystem.CornerRadius.md),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = DesignSystem.Elevation.level1)
    ) {
        Row(
            modifier = Modifier.padding(DesignSystem.Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon ?: defaultIcon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(DesignSystem.IconSize.md)
            )
            Spacer(Modifier.width(DesignSystem.Spacing.md))
            Text(
                text = message,
                style = DesignSystem.Typography.bodyMedium,
                color = DesignSystem.Colors.TextPrimary,
                modifier = Modifier.weight(1f)
            )
            if (dismissible && onDismiss != null) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar alerta",
                        tint = iconColor
                    )
                }
            }
        }
    }
}

// ==================== INDICADORES DE PROGRESO ====================

@Composable
fun CircularProgressWithLabel(
    progress: Float,
    label: String,
    modifier: Modifier = Modifier,
    size: Dp = 100.dp,
    strokeWidth: Dp = 8.dp,
    color: Color = DesignSystem.Colors.Primary
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = progress,
            modifier = Modifier.fillMaxSize(),
            strokeWidth = strokeWidth,
            color = color,
            trackColor = color.copy(alpha = 0.2f)
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${(progress * 100).toInt()}%",
                style = DesignSystem.Typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = DesignSystem.Colors.TextPrimary
            )
            Text(
                text = label,
                style = DesignSystem.Typography.bodySmall,
                color = DesignSystem.Colors.TextSecondary
            )
        }
    }
}

@Composable
fun SaturationBar(
    current: Int,
    max: Int,
    modifier: Modifier = Modifier,
    showLabel: Boolean = true
) {
    val percentage = if (max > 0) (current.toFloat() / max.toFloat()) * 100 else 0f

    val color = when {
        percentage >= 90 -> DesignSystem.Colors.SaturationCritical
        percentage >= 75 -> DesignSystem.Colors.SaturationHigh
        percentage >= 50 -> DesignSystem.Colors.SaturationMedium
        else -> DesignSystem.Colors.SaturationLow
    }

    Column(modifier = modifier.fillMaxWidth()) {
        if (showLabel) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$current / $max",
                    style = DesignSystem.Typography.bodyMedium,
                    color = DesignSystem.Colors.TextPrimary
                )
                Text(
                    text = "${percentage.toInt()}%",
                    style = DesignSystem.Typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
            Spacer(Modifier.height(DesignSystem.Spacing.xs))
        }

        LinearProgressIndicator(
            progress = percentage / 100f,
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(DesignSystem.CornerRadius.sm)),
            color = color,
            trackColor = Color.LightGray.copy(alpha = 0.3f)
        )
    }
}

// ==================== LOADING ====================

@Composable
fun FEMSALoadingDialog(
    message: String = "Cargando...",
    show: Boolean
) {
    if (show) {
        AlertDialog(
            onDismissRequest = { },
            confirmButton = { },
            title = null,
            text = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(40.dp),
                        color = DesignSystem.Colors.Primary
                    )
                    Spacer(Modifier.width(DesignSystem.Spacing.md))
                    Text(
                        text = message,
                        style = DesignSystem.Typography.bodyLarge
                    )
                }
            }
        )
    }
}

// ==================== BADGES ====================

@Composable
fun FEMSABadge(
    count: Int,
    modifier: Modifier = Modifier
) {
    if (count > 0) {
        Badge(
            modifier = modifier,
            containerColor = DesignSystem.Colors.Error,
            contentColor = Color.White
        ) {
            Text(
                text = if (count > 99) "99+" else count.toString(),
                style = DesignSystem.Typography.labelSmall
            )
        }
    }
}

// ==================== EMPTY STATE ====================

@Composable
fun EmptyState(
    icon: ImageVector,
    message: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(DesignSystem.Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = DesignSystem.Colors.TextSecondary.copy(alpha = 0.5f),
            modifier = Modifier.size(DesignSystem.IconSize.xxxl)
        )
        Spacer(Modifier.height(DesignSystem.Spacing.md))
        Text(
            text = message,
            style = DesignSystem.Typography.bodyLarge,
            color = DesignSystem.Colors.TextSecondary,
            textAlign = TextAlign.Center
        )
        if (actionText != null && onActionClick != null) {
            Spacer(Modifier.height(DesignSystem.Spacing.lg))
            FEMSAButton(
                text = actionText,
                onClick = onActionClick,
                style = FEMSAButtonStyle.Outlined
            )
        }
    }
}

// ==================== SUCCESS ANIMATION ====================

@Composable
fun SuccessAnimation(
    show: Boolean,
    onAnimationComplete: () -> Unit = {}
) {
    AnimatedVisibility(
        visible = show,
        enter = scaleIn(
            initialScale = 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeIn(),
        exit = scaleOut() + fadeOut()
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Éxito",
                tint = DesignSystem.Colors.Success,
                modifier = Modifier.size(100.dp)
            )
        }

        LaunchedEffect(show) {
            if (show) {
                kotlinx.coroutines.delay(1500)
                onAnimationComplete()
            }
        }
    }
}
