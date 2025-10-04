package com.example.escaneodematerialeskof.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.escaneodematerialeskof.ui.theme.DesignSystem

/**
 * Animaciones Consistentes para toda la aplicación
 */
object Animations {
    // ==================== EASING ====================

    val FastEasing = tween<Float>(
        durationMillis = DesignSystem.Duration.fast,
        easing = FastOutSlowInEasing
    )

    val MediumEasing = tween<Float>(
        durationMillis = DesignSystem.Duration.medium,
        easing = FastOutSlowInEasing
    )

    val SlowEasing = tween<Float>(
        durationMillis = DesignSystem.Duration.slow,
        easing = FastOutSlowInEasing
    )

    // ==================== FADE ====================

    val FadeIn = fadeIn(animationSpec = FastEasing)
    val FadeOut = fadeOut(animationSpec = FastEasing)

    val FadeInSlow = fadeIn(animationSpec = MediumEasing)
    val FadeOutSlow = fadeOut(animationSpec = MediumEasing)

    // ==================== SLIDE ====================

    val SlideInFromRight = slideInHorizontally(
        initialOffsetX = { it },
        animationSpec = tween(
            durationMillis = DesignSystem.Duration.medium,
            easing = FastOutSlowInEasing
        )
    )

    val SlideOutToLeft = slideOutHorizontally(
        targetOffsetX = { -it },
        animationSpec = tween(
            durationMillis = DesignSystem.Duration.medium,
            easing = FastOutSlowInEasing
        )
    )

    val SlideInFromLeft = slideInHorizontally(
        initialOffsetX = { -it },
        animationSpec = tween(
            durationMillis = DesignSystem.Duration.medium,
            easing = FastOutSlowInEasing
        )
    )

    val SlideOutToRight = slideOutHorizontally(
        targetOffsetX = { it },
        animationSpec = tween(
            durationMillis = DesignSystem.Duration.medium,
            easing = FastOutSlowInEasing
        )
    )

    val SlideInFromTop = slideInVertically(
        initialOffsetY = { -it },
        animationSpec = tween(
            durationMillis = DesignSystem.Duration.medium,
            easing = FastOutSlowInEasing
        )
    )

    val SlideOutToBottom = slideOutVertically(
        targetOffsetY = { it },
        animationSpec = tween(
            durationMillis = DesignSystem.Duration.medium,
            easing = FastOutSlowInEasing
        )
    )

    val SlideInFromBottom = slideInVertically(
        initialOffsetY = { it },
        animationSpec = tween(
            durationMillis = DesignSystem.Duration.medium,
            easing = FastOutSlowInEasing
        )
    )

    val SlideOutToTop = slideOutVertically(
        targetOffsetY = { -it },
        animationSpec = tween(
            durationMillis = DesignSystem.Duration.medium,
            easing = FastOutSlowInEasing
        )
    )

    // ==================== SCALE ====================

    val ScaleIn = scaleIn(
        initialScale = 0.8f,
        animationSpec = tween(
            durationMillis = DesignSystem.Duration.fast,
            easing = FastOutSlowInEasing
        )
    )

    val ScaleOut = scaleOut(
        targetScale = 0.8f,
        animationSpec = tween(
            durationMillis = DesignSystem.Duration.fast,
            easing = FastOutSlowInEasing
        )
    )

    val ScaleInBouncy = scaleIn(
        initialScale = 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    // ==================== EXPAND/COLLAPSE ====================

    val ExpandVertically = expandVertically(
        animationSpec = tween(
            durationMillis = DesignSystem.Duration.medium,
            easing = FastOutSlowInEasing
        )
    )

    val CollapseVertically = shrinkVertically(
        animationSpec = tween(
            durationMillis = DesignSystem.Duration.medium,
            easing = FastOutSlowInEasing
        )
    )

    val ExpandHorizontally = expandHorizontally(
        animationSpec = tween(
            durationMillis = DesignSystem.Duration.medium,
            easing = FastOutSlowInEasing
        )
    )

    val CollapseHorizontally = shrinkHorizontally(
        animationSpec = tween(
            durationMillis = DesignSystem.Duration.medium,
            easing = FastOutSlowInEasing
        )
    )

    // ==================== COMBINED ====================

    // Entrada desde la derecha (para navegación forward)
    val EnterFromRight = SlideInFromRight + FadeIn

    // Salida hacia la izquierda (para navegación forward)
    val ExitToLeft = SlideOutToLeft + FadeOut

    // Entrada desde la izquierda (para navegación back)
    val EnterFromLeft = SlideInFromLeft + FadeIn

    // Salida hacia la derecha (para navegación back)
    val ExitToRight = SlideOutToRight + FadeOut

    // Entrada con fade y scale (para diálogos)
    val EnterDialog = FadeIn + ScaleIn

    // Salida con fade y scale (para diálogos)
    val ExitDialog = FadeOut + ScaleOut

    // Entrada desde abajo (para bottom sheets)
    val EnterFromBottom = SlideInFromBottom + FadeIn

    // Salida hacia abajo (para bottom sheets)
    val ExitToBottom = SlideOutToBottom + FadeOut

    // ==================== TRANSICIONES DE CONTENIDO ====================

    @OptIn(ExperimentalAnimationApi::class)
    fun fadeContentTransition(): ContentTransform {
        return fadeIn(animationSpec = FastEasing) togetherWith
                fadeOut(animationSpec = FastEasing)
    }

    @OptIn(ExperimentalAnimationApi::class)
    fun slideContentTransition(): ContentTransform {
        return (SlideInFromRight + FadeIn) togetherWith (SlideOutToLeft + FadeOut)
    }

    // ==================== ANIMACIONES INFINITAS ====================

    @Composable
    fun rememberPulseAnimation(): Float {
        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        return infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse_scale"
        ).value
    }

    @Composable
    fun rememberRotationAnimation(): Float {
        val infiniteTransition = rememberInfiniteTransition(label = "rotation")
        return infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "rotation_angle"
        ).value
    }

    @Composable
    fun rememberShakeAnimation(trigger: Boolean): Float {
        val shakeOffset by animateFloatAsState(
            targetValue = if (trigger) 10f else 0f,
            animationSpec = repeatable(
                iterations = 3,
                animation = tween(durationMillis = 50),
                repeatMode = RepeatMode.Reverse
            ),
            label = "shake"
        )
        return shakeOffset
    }
}

// ==================== EXTENSIONS ====================

/**
 * Extensión para animar cambios de tamaño
 */
fun Modifier.animateSize(enabled: Boolean): Modifier {
    return if (enabled) {
        this
    } else {
        this
    }
}

/**
 * Transición de navegación estándar
 */
@OptIn(ExperimentalAnimationApi::class)
fun navigationTransition(forward: Boolean = true): ContentTransform {
    return if (forward) {
        (Animations.EnterFromRight togetherWith Animations.ExitToLeft)
    } else {
        (Animations.EnterFromLeft togetherWith Animations.ExitToRight)
    }
}
