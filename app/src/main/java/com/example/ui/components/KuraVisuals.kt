package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ErrorContainer
import com.example.ui.theme.OnErrorContainer
import com.example.ui.theme.OnSecondaryFixedVariant
import com.example.ui.theme.OnSurface
import com.example.ui.theme.OnTertiaryContainer
import com.example.ui.theme.PrimaryBlack
import com.example.ui.theme.PrimaryContainerDark
import com.example.ui.theme.Saffron
import com.example.ui.theme.SecondaryFixed
import com.example.ui.theme.SurfaceContainer
import com.example.ui.theme.SurfaceContainerHighest
import com.example.ui.theme.TertiaryFixedDim

@Composable
fun KuraBookIcon(
    modifier: Modifier = Modifier,
    tint: Color = PrimaryBlack
) {
    Canvas(modifier = modifier) {
        val sx = size.width / 32f
        val sy = size.height / 32f
        val stroke = Stroke(
            width = 2.3f * minOf(sx, sy),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )

        val bookPath = Path().apply {
            moveTo(16f * sx, 8.5f * sy)
            cubicTo(13.2f * sx, 6.2f * sy, 8.5f * sx, 6.2f * sy, 4.5f * sx, 7.4f * sy)
            lineTo(4.5f * sx, 24.5f * sy)
            cubicTo(8.8f * sx, 23.3f * sy, 13.2f * sx, 23.6f * sy, 16f * sx, 25.8f * sy)
            cubicTo(18.8f * sx, 23.6f * sy, 23.2f * sx, 23.3f * sy, 27.5f * sx, 24.5f * sy)
            lineTo(27.5f * sx, 7.4f * sy)
            cubicTo(23.5f * sx, 6.2f * sy, 18.8f * sx, 6.2f * sy, 16f * sx, 8.5f * sy)
            close()
        }
        drawPath(path = bookPath, color = tint, style = stroke)

        drawLine(tint, Offset(16f * sx, 8.5f * sy), Offset(16f * sx, 25.8f * sy), stroke.width, StrokeCap.Round)
        drawLine(tint, Offset(8f * sx, 12.5f * sy), Offset(12.5f * sx, 12.5f * sy), stroke.width, StrokeCap.Round)
        drawLine(tint, Offset(8f * sx, 16.5f * sy), Offset(12.5f * sx, 16.5f * sy), stroke.width, StrokeCap.Round)
        drawLine(tint, Offset(19.5f * sx, 12.5f * sy), Offset(24f * sx, 12.5f * sy), stroke.width, StrokeCap.Round)
        drawLine(tint, Offset(19.5f * sx, 16.5f * sy), Offset(24f * sx, 16.5f * sy), stroke.width, StrokeCap.Round)
    }
}

@Composable
fun KuraLogoBadge(
    badgeSize: Dp = 92.dp,
    iconSize: Dp = 44.dp,
    cornerRadius: Dp = 24.dp
) {
    Box(
        modifier = Modifier
            .size(badgeSize)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(cornerRadius),
                ambientColor = Saffron.copy(alpha = 0.55f),
                spotColor = Saffron.copy(alpha = 0.55f)
            )
            .clip(RoundedCornerShape(cornerRadius))
            .background(Saffron),
        contentAlignment = Alignment.Center
    ) {
        KuraBookIcon(
            modifier = Modifier.size(iconSize),
            tint = OnSurface
        )
    }
}

@Composable
fun DarkHeroCard(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 14.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = PrimaryContainerDark.copy(alpha = 0.45f),
                spotColor = PrimaryContainerDark.copy(alpha = 0.45f)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(PrimaryContainerDark)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Saffron.copy(alpha = 0.25f), Color.Transparent),
                    center = Offset(size.width * 0.92f, size.height * 0.05f),
                    radius = size.width * 0.65f
                ),
                center = Offset(size.width * 0.92f, size.height * 0.05f),
                radius = size.width * 0.65f
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(TertiaryFixedDim.copy(alpha = 0.12f), Color.Transparent),
                    center = Offset(size.width * 0.08f, size.height * 0.95f),
                    radius = size.width * 0.55f
                ),
                center = Offset(size.width * 0.08f, size.height * 0.95f),
                radius = size.width * 0.55f
            )
        }
        Box(modifier = Modifier.fillMaxSize(), content = content)
    }
}

private val avatarPalettes: List<Pair<Color, Color>> = listOf(
    SurfaceContainerHighest to OnTertiaryContainer,
    SecondaryFixed to OnSecondaryFixedVariant,
    SurfaceContainerHighest to PrimaryContainerDark,
    ErrorContainer to OnErrorContainer,
    SurfaceContainer to PrimaryBlack
)

@Composable
fun KuraAvatar(
    name: String,
    size: Dp = 48.dp,
    fontSize: TextUnit = 17.sp,
    showSaffronRing: Boolean = false,
    overdueDotColor: Color? = null
) {
    val idx = (name.sumOf { it.code }) % avatarPalettes.size
    val (bg, fg) = avatarPalettes[idx]
    Box(modifier = Modifier.size(size)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (showSaffronRing) {
                        Modifier
                            .border(3.dp, Saffron.copy(alpha = 0.45f), CircleShape)
                            .clip(CircleShape)
                    } else {
                        Modifier.clip(CircleShape)
                    }
                )
                .background(bg),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = KuraFormatters.initials(name),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = fontSize,
                    fontWeight = FontWeight.Bold
                ),
                color = fg
            )
        }
        if (overdueDotColor != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 1.dp, y = (-1).dp)
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(overdueDotColor)
                    .border(2.dp, Color.White, CircleShape)
            )
        }
    }
}
