package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.KuraLogoBadge
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.OnPrimaryWhite
import com.example.ui.theme.OnSecondaryFixedVariant
import com.example.ui.theme.OnSurface
import com.example.ui.theme.OnSurfaceVariant
import com.example.ui.theme.OnTertiaryContainer
import com.example.ui.theme.OnTertiaryFixedVariant
import com.example.ui.theme.Outline
import com.example.ui.theme.PrimaryBlack
import com.example.ui.theme.Saffron
import com.example.ui.theme.SecondaryContainer
import com.example.ui.theme.SecondaryFixed
import com.example.ui.theme.SurfaceContainerHighest
import com.example.ui.theme.SurfaceContainerLow
import com.example.ui.theme.TertiaryFixed
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val badgeScale = remember { Animatable(0.6f) }
    val contentAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            badgeScale.animateTo(
                1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        }
        launch {
            delay(180L)
            contentAlpha.animateTo(1f, tween(550, easing = FastOutSlowInEasing))
        }
        delay(1700L)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .safeDrawingPadding()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onFinished
            )
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.scale(badgeScale.value)) {
                KuraLogoBadge(badgeSize = 92.dp, iconSize = 44.dp, cornerRadius = 24.dp)
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = buildAnnotatedString {
                    append("Kura")
                    withStyle(SpanStyle(color = Saffron, fontWeight = FontWeight.Black)) {
                        append(".")
                    }
                },
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = OnSurface,
                modifier = Modifier.alpha(contentAlpha.value)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Know who owes you.",
                style = MaterialTheme.typography.bodyLarge,
                color = Outline,
                modifier = Modifier.alpha(contentAlpha.value)
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .alpha(contentAlpha.value),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(SurfaceContainerLow)
                    .padding(horizontal = 16.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = Outline,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = "Private, fast & offline-ready.",
                    style = MaterialTheme.typography.labelMedium,
                    color = OnSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Simple credit ledger",
                style = MaterialTheme.typography.bodySmall,
                color = Outline
            )
        }
    }
}

@Composable
fun ShopOwnerWelcomeScreen(
    onContinue: () -> Unit,
    onSkip: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .safeDrawingPadding()
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 540.dp)
                .fillMaxSize()
                .align(Alignment.Center)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    KuraLogoBadge(badgeSize = 26.dp, iconSize = 14.dp, cornerRadius = 8.dp)
                    Text(
                        text = buildAnnotatedString {
                            append("Kura")
                            withStyle(SpanStyle(color = Saffron, fontWeight = FontWeight.Black)) {
                                append(".")
                            }
                        },
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = OnSurface
                    )
                }
                TextButton(
                    onClick = onSkip,
                    modifier = Modifier.testTag("welcome_story_skip_button")
                ) {
                    Text("Skip", style = MaterialTheme.typography.labelLarge, color = Outline)
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = SurfaceContainerLow,
                    border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(4f / 3f)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_shop_owner_welcome),
                            contentDescription = "Friendly shop owner holding a yellow credit ledger notebook behind a counter with a customer",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Surface(
                            shape = CircleShape,
                            color = SecondaryContainer,
                            shadowElevation = 2.dp,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Storefront,
                                    contentDescription = null,
                                    tint = PrimaryBlack,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = "Built for shop & chop owners",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = PrimaryBlack
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Your daily credit book, without the lost paper.",
                    style = MaterialTheme.typography.headlineLarge,
                    color = OnSurface,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Whether you run a chop bar, provision store, or market stall, Kura helps you track every credit plate, bag of rice, or supplier balance in seconds.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(max = 360.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    WelcomeMeaningRow(
                        icon = Icons.AutoMirrored.Filled.MenuBook,
                        iconBg = SecondaryFixed,
                        iconTint = OnSecondaryFixedVariant,
                        title = "Track credit & payments at the counter",
                        subtitle = "Know exactly who owes you and who you owe at a glance."
                    )
                    WelcomeMeaningRow(
                        icon = Icons.Default.ChatBubbleOutline,
                        iconBg = TertiaryFixed,
                        iconTint = OnTertiaryFixedVariant,
                        title = "Polite WhatsApp reminders",
                        subtitle = "Share clear balance reminders in one tap without awkward calls."
                    )
                    WelcomeMeaningRow(
                        icon = Icons.Default.Lock,
                        iconBg = SurfaceContainerLow,
                        iconTint = OnSurface,
                        title = "Offline & protected by passcode",
                        subtitle = "Saved right on your phone so your business stays private."
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                onClick = onContinue,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlack,
                    contentColor = OnPrimaryWhite
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("welcome_story_continue_button")
            ) {
                Text("Continue", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun WelcomeMeaningRow(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.labelLarge, color = OnSurface)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
            }
        }
    }
}

@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()
    val isLast = pagerState.currentPage == 2

    val titles = listOf(
        "Record credit in seconds",
        "Remind on WhatsApp",
        "Money at a glance"
    )
    val descriptions = listOf(
        "Log what customers take on credit from your phone. Never lose track of a single cedi.",
        "Send a friendly reminder with the exact amount owed in one tap. No awkward conversations.",
        "See who owes you, who you owe and your net balance the moment you open the app."
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .safeDrawingPadding()
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 540.dp)
                .fillMaxSize()
                .align(Alignment.Center)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(SecondaryContainer)
                    )
                    Text(
                        text = "Kura ledger",
                        style = MaterialTheme.typography.labelMedium,
                        color = OnSurfaceVariant
                    )
                }
                if (!isLast) {
                    TextButton(
                        onClick = onComplete,
                        modifier = Modifier.testTag("skip_onboarding_button")
                    ) {
                        Text("Skip", style = MaterialTheme.typography.labelLarge, color = Outline)
                    }
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { page ->
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceContainerLow)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        when (page) {
                            0 -> OnboardingSlideOneVisual()
                            1 -> OnboardingSlideTwoVisual()
                            else -> OnboardingSlideThreeVisual()
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Text(
                        text = titles[page],
                        style = MaterialTheme.typography.headlineLarge,
                        color = OnSurface,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = descriptions[page],
                        style = MaterialTheme.typography.bodyLarge,
                        color = OnSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.widthIn(max = 360.dp)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 18.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { idx ->
                    val active = pagerState.currentPage == idx
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .height(8.dp)
                            .width(if (active) 24.dp else 8.dp)
                            .clip(CircleShape)
                            .background(if (active) PrimaryBlack else SurfaceContainerHighest)
                    )
                }
            }

            Button(
                onClick = {
                    if (isLast) onComplete()
                    else scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlack,
                    contentColor = OnPrimaryWhite
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("onboarding_next_button")
            ) {
                Text(
                    text = if (isLast) "Get started" else "Next",
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = if (isLast) Icons.Default.Check else Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun OnboardingSlideOneVisual() {
    Box(modifier = Modifier.widthIn(max = 280.dp).fillMaxWidth()) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color.White,
            shadowElevation = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("New credit entry", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                        Text("Kofi Mensah", style = MaterialTheme.typography.labelLarge, color = OnSurface)
                    }
                    Text("Just now", style = MaterialTheme.typography.labelMedium, color = Outline)
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = SurfaceContainerHighest)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("2× Jasmine rice (5kg)", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                    Text("GH₵ 180.00", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = OnSurface)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("1× Cooking oil", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                    Text("GH₵ 65.00", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = OnSurface)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceContainerLow)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Balance due", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                    Text("GH₵ 245.00", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = OnSurface)
                }
            }
        }
        Surface(
            shape = CircleShape,
            color = Color.White,
            shadowElevation = 2.dp,
            modifier = Modifier.align(Alignment.TopEnd).padding(end = 10.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = OnTertiaryContainer, modifier = Modifier.size(16.dp))
                Text("SAVED", style = MaterialTheme.typography.labelSmall, color = OnTertiaryContainer)
            }
        }
    }
}

@Composable
private fun OnboardingSlideTwoVisual() {
    Column(
        modifier = Modifier.widthIn(max = 280.dp).fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp, bottomEnd = 14.dp, bottomStart = 4.dp),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Text(
                text = buildAnnotatedString {
                    append("Hello Ama, a friendly reminder of your balance of ")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("GH₵ 420") }
                    append(". Thank you!")
                },
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurface,
                modifier = Modifier.padding(12.dp)
            )
        }
        Surface(
            shape = RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp, bottomStart = 14.dp, bottomEnd = 4.dp),
            color = TertiaryFixed,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(
                text = "Will pay tomorrow",
                style = MaterialTheme.typography.bodyMedium,
                color = OnTertiaryFixedVariant,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun OnboardingSlideThreeVisual() {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = Modifier.widthIn(max = 280.dp).fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Net balance", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
            Text("GH₵ 1,055", style = MaterialTheme.typography.headlineLarge, color = OnSurface)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(SurfaceContainerHighest)
            ) {
                Box(modifier = Modifier.weight(0.71f).height(8.dp).background(OnTertiaryContainer))
                Box(modifier = Modifier.weight(0.29f).height(8.dp).background(ErrorRed))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Owed to you", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                Text("You owe", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
            }
        }
    }
}
