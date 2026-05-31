package com.vesti.app.ui.auth

import android.view.SoundEffectConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vesti.app.AppConfig
import com.vesti.app.R
import com.vesti.app.data.network.LoginRequest
import com.vesti.app.data.network.RegisterRequest
import com.vesti.app.ui.theme.VestiColors
import kotlinx.coroutines.launch

// Özel Renk Paleti Aligned with Your Premium Theme
val ColorDarkCharcoal = Color(0xFF0F1015)
val ColorDeepIndigo = Color(0xFF141622)
val ColorIndigoGlow = Color(0xFF7586FF)
val ColorGlassBackground = Color(0xFFFFFFFF).copy(alpha = 0.06f)
val ColorGlassBorder = Color(0xFFFFFFFF).copy(alpha = 0.12f)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val authState by viewModel.authState.collectAsStateWithLifecycle()

    // State Yönetimi
    var isLampOn by remember { mutableStateOf(false) }
    var isRegisterMode by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }
    var isPasswordVisible by remember { mutableStateOf(false) }

    // Cord pulling physics
    val coroutineScope = rememberCoroutineScope()
    val cordStretchAnim = remember { Animatable(0f) }
    val cordSwayAnim = remember { Animatable(0f) }

    // Infinite pulse transition for knob glow when lamp is off
    val infiniteTransition = rememberInfiniteTransition(label = "knobPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    // Animated background brightness & light alpha
    val bgAlphaTransition by animateFloatAsState(
        targetValue = if (isLampOn) 0.85f else 0.08f,
        animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing),
        label = "BgAlpha"
    )
    val animationAlpha by animateFloatAsState(
        targetValue = if (isLampOn) 1f else 0f,
        animationSpec = tween(durationMillis = 700),
        label = "AlphaAnim"
    )

    val haptic = LocalHapticFeedback.current
    val view = LocalView.current

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onLoginSuccess()
            viewModel.resetState()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF060608)) // Base dark backdrop
    ) {
        // ==================== KATMAN 1: GERÇEKÇİ WARDROBE ARKA PLAN ====================
        // Fades in beautifully when the lamp is turned on.
        Image(
            painter = painterResource(id = R.drawable.image),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = bgAlphaTransition }
        )

        // ==================== KATMAN 2: PROGRAMMATIC SOFT LIGHT CONE ====================
        if (isLampOn) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height

                // Spotlight coordinates matched to programmatically drawn lamp
                val startX = width * 0.23f
                val startY = height * 0.24f

                val conePath = Path().apply {
                    moveTo(startX, startY)
                    // Projects downwards as a gorgeous wide dramatic spotlight beam covering the center
                    lineTo(width * 0.05f, height)
                    lineTo(width * 0.95f, height)
                    close()
                }

                drawPath(
                    path = conePath,
                    brush = Brush.radialGradient(
                        colors = listOf(
                            ColorIndigoGlow.copy(alpha = 0.35f * animationAlpha), // Radiant indigo-blue highlight
                            ColorIndigoGlow.copy(alpha = 0.08f * animationAlpha), // Smooth surrounding glow
                            Color.Transparent
                        ),
                        center = Offset(startX, startY),
                        radius = width * 0.85f
                    )
                )

                // Volumetric Floor Light Reflection Splash
                drawOval(
                    color = ColorIndigoGlow.copy(alpha = 0.22f * animationAlpha),
                    topLeft = Offset(width * 0.1f, height * 0.86f),
                    size = Size(width * 0.8f, height * 0.14f)
                )
            }
        }

        // ==================== KATMAN 3: ULTRA-MODERN TALL 3D VECTOR METAL LAMP (LEFT SIDE ONLY) ====================
        // Positioned on the left 38% of the screen so that it never overlaps with the login card!
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.38f)
                .align(Alignment.CenterStart)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onDragStart = { },
                            onDragEnd = {
                                val didPullPassed = cordStretchAnim.value > 55f
                                if (didPullPassed) {
                                    isLampOn = !isLampOn
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    view.playSoundEffect(SoundEffectConstants.CLICK)
                                }
                                coroutineScope.launch {
                                    cordStretchAnim.animateTo(
                                        targetValue = 0f,
                                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
                                    )
                                }
                                coroutineScope.launch {
                                    // Trigger physical horizontal sway pendulum swing decay
                                    cordSwayAnim.animateTo(
                                        targetValue = 0f,
                                        animationSpec = keyframes {
                                            durationMillis = 1400
                                            0f at 0
                                            -14f at 200
                                            10f at 400
                                            -7f at 600
                                            4f at 800
                                            -2f at 1000
                                            1f at 1200
                                            0f at 1400
                                        }
                                    )
                                }
                            },
                            onDragCancel = {
                                coroutineScope.launch {
                                    cordStretchAnim.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                                }
                            },
                            onVerticalDrag = { change, dragAmount ->
                                change.consume()
                                coroutineScope.launch {
                                    val nextStretch = (cordStretchAnim.value + dragAmount).coerceIn(0f, 130f)
                                    cordStretchAnim.snapTo(nextStretch)
                                }
                            }
                        )
                    }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        isLampOn = !isLampOn
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        coroutineScope.launch {
                            // Micro cord pull animation on click
                            cordStretchAnim.animateTo(12f, tween(100))
                            cordStretchAnim.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                        }
                        coroutineScope.launch {
                            // Realistic sway on click
                            cordSwayAnim.animateTo(
                                targetValue = 0f,
                                animationSpec = keyframes {
                                    durationMillis = 1400
                                    0f at 0
                                    -14f at 200
                                    10f at 400
                                    -7f at 600
                                    4f at 800
                                    -2f at 1000
                                    1f at 1200
                                    0f at 1400
                                }
                            )
                        }
                    }
            ) {
                val width = size.width
                val height = size.height

                // Ultra-modern minimalist coordinates (Gorgeously swept to start a bit more left)
                val baseCenterX = width * 0.25f
                val baseBottomY = height * 0.95f
                val middleJointY = height * 0.60f
                val topJointY = height * 0.22f
                val shadeCenterX = width * 0.62f
                val shadeCenterY = height * 0.25f

                // Ultra-thin high-end brushed steel pole gradient
                val metalGradient = Brush.linearGradient(
                    colors = listOf(Color(0xFF2A2E33), Color(0xFFECEFF1), Color(0xFF455A64)),
                    start = Offset(baseCenterX - 4.dp.toPx(), 0f),
                    end = Offset(baseCenterX + 4.dp.toPx(), 0f)
                )

                // Sleek flat circular base plate (Modern Scandinavian design)
                drawRoundRect(
                    color = Color(0xFF37474F),
                    topLeft = Offset(baseCenterX - 20.dp.toPx(), baseBottomY - 6.dp.toPx()),
                    size = Size(40.dp.toPx(), 6.dp.toPx()),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx())
                )

                // Ultra-thin vertical column
                drawRect(
                    brush = metalGradient,
                    topLeft = Offset(baseCenterX - 2.5f.dp.toPx(), middleJointY),
                    size = Size(5.dp.toPx(), baseBottomY - middleJointY)
                )

                // Sleek minimalist hinge joint node
                drawCircle(
                    color = Color(0xFFB0BEC5),
                    radius = 5.dp.toPx(),
                    center = Offset(baseCenterX, middleJointY)
                )

                // Highly curved elegant arch neck pipe (ultra-thin 3.dp)
                val neckWidth = 3.dp.toPx()
                val neckPath = Path().apply {
                    moveTo(baseCenterX, middleJointY)
                    cubicTo(
                        baseCenterX - 14.dp.toPx(), middleJointY - 100.dp.toPx(),
                        baseCenterX + 10.dp.toPx(), topJointY - 45.dp.toPx(),
                        shadeCenterX, topJointY
                    )
                }
                drawPath(
                    path = neckPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFECEFF1), Color(0xFF37474F))
                    ),
                    style = Stroke(width = neckWidth, cap = StrokeCap.Round)
                )

                // Modern cylinder shade connector joint
                drawCircle(
                    color = Color(0xFFCFD8DC),
                    radius = 4.dp.toPx(),
                    center = Offset(shadeCenterX, topJointY)
                )

                // Sleek modern curved shade dish design
                val shadePath = Path().apply {
                    moveTo(shadeCenterX - 10.dp.toPx(), shadeCenterY - 10.dp.toPx())
                    cubicTo(
                        shadeCenterX - 18.dp.toPx(), shadeCenterY - 12.dp.toPx(),
                        shadeCenterX - 22.dp.toPx(), shadeCenterY + 8.dp.toPx(),
                        shadeCenterX - 18.dp.toPx(), shadeCenterY + 18.dp.toPx()
                    )
                    lineTo(shadeCenterX + 32.dp.toPx(), shadeCenterY + 3.dp.toPx())
                    cubicTo(
                        shadeCenterX + 28.dp.toPx(), shadeCenterY - 10.dp.toPx(),
                        shadeCenterX + 10.dp.toPx(), shadeCenterY - 14.dp.toPx(),
                        shadeCenterX - 10.dp.toPx(), shadeCenterY - 10.dp.toPx()
                    )
                    close()
                }
                val domeGradient = Brush.radialGradient(
                    colors = listOf(Color(0xFFECEFF1), Color(0xFF78909C), Color(0xFF263238)),
                    center = Offset(shadeCenterX - 6.dp.toPx(), shadeCenterY - 4.dp.toPx()),
                    radius = 35.dp.toPx()
                )
                drawPath(path = shadePath, brush = domeGradient)

                // Glossy polished chrome specular horizontal reflection bar on the shade dish
                val shinePath = Path().apply {
                    moveTo(shadeCenterX - 14.dp.toPx(), shadeCenterY - 4.dp.toPx())
                    cubicTo(
                        shadeCenterX - 10.dp.toPx(), shadeCenterY - 8.dp.toPx(),
                        shadeCenterX + 10.dp.toPx(), shadeCenterY - 10.dp.toPx(),
                        shadeCenterX + 24.dp.toPx(), shadeCenterY - 5.dp.toPx()
                    )
                    lineTo(shadeCenterX + 22.dp.toPx(), shadeCenterY - 2.dp.toPx())
                    cubicTo(
                        shadeCenterX + 8.dp.toPx(), shadeCenterY - 7.dp.toPx(),
                        shadeCenterX - 8.dp.toPx(), shadeCenterY - 5.dp.toPx(),
                        shadeCenterX - 12.dp.toPx(), shadeCenterY - 2.dp.toPx()
                    )
                    close()
                }
                drawPath(path = shinePath, color = Color.White.copy(alpha = 0.22f))

                // Chrome rim highlight
                drawLine(
                    color = Color(0xFFECEFF1),
                    start = Offset(shadeCenterX - 18.dp.toPx(), shadeCenterY + 18.dp.toPx()),
                    end = Offset(shadeCenterX + 32.dp.toPx(), shadeCenterY + 3.dp.toPx()),
                    strokeWidth = 1.2.dp.toPx()
                )

                // Filament Bulb inside shade
                val bulbCenterX = shadeCenterX + 6.dp.toPx()
                val bulbCenterY = shadeCenterY + 6.dp.toPx()

                if (isLampOn) {
                    // High-end glowing yellow-orange neon bulb aura
                    drawCircle(
                        color = Color(0xFFFFD54F).copy(alpha = 0.25f),
                        radius = 22.dp.toPx(),
                        center = Offset(bulbCenterX, bulbCenterY)
                    )
                    drawCircle(
                        color = ColorIndigoGlow.copy(alpha = 0.40f),
                        radius = 14.dp.toPx(),
                        center = Offset(bulbCenterX, bulbCenterY)
                    )
                }

                drawCircle(
                    color = if (isLampOn) Color.White.copy(alpha = 0.95f) else Color(0xFF90A4AE).copy(alpha = 0.25f),
                    radius = 7.dp.toPx(),
                    center = Offset(bulbCenterX, bulbCenterY)
                )

                if (isLampOn) {
                    val filament = Path().apply {
                        moveTo(bulbCenterX - 2.dp.toPx(), bulbCenterY - 2.dp.toPx())
                        lineTo(bulbCenterX, bulbCenterY + 1.dp.toPx())
                        lineTo(bulbCenterX + 2.dp.toPx(), bulbCenterY - 2.dp.toPx())
                    }
                    drawPath(
                        path = filament,
                        color = Color(0xFFFFEB3B),
                        style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Elegant beaded chain cord (Swaying pendulum physics)
                val cordStartX = shadeCenterX - 8.dp.toPx()
                val cordStartY = shadeCenterY + 10.dp.toPx()
                val cordLength = 70.dp.toPx()
                val stretch = cordStretchAnim.value
                val sway = cordSwayAnim.value.dp.toPx()

                val numBeads = 10
                val beadRadius = 1.8f.dp.toPx()
                for (i in 0 until numBeads) {
                    val frac = i.toFloat() / (numBeads - 1)
                    val beadY = cordStartY + (cordLength * frac) + (stretch * frac)
                    // Hinge physics: top bead is fixed (sway * 0), bottom bead swings fully (sway * 1)
                    val beadX = cordStartX + (sway * frac)

                    val beadGradient = Brush.radialGradient(
                        colors = listOf(Color.White, Color(0xFF90A4AE), Color(0xFF37474F)),
                        center = Offset(beadX - 0.5.dp.toPx(), beadY - 0.5.dp.toPx()),
                        radius = beadRadius
                    )
                    drawCircle(
                        brush = beadGradient,
                        radius = beadRadius,
                        center = Offset(beadX, beadY)
                    )
                }

                // Pulse guide knob (Swaying at full pendulum extremity)
                val knobY = cordStartY + cordLength + stretch + 4.dp.toPx()
                val knobX = cordStartX + sway

                if (!isLampOn) {
                    drawCircle(
                        color = ColorIndigoGlow.copy(alpha = pulseAlpha),
                        radius = (6.dp + 3.dp * pulseScale).toPx(),
                        center = Offset(knobX, knobY)
                    )
                }

                val knobWidth = 4.dp.toPx()
                val knobHeight = 11.dp.toPx()
                val knobPath = Path().apply {
                    moveTo(knobX, knobY - 4.dp.toPx())
                    cubicTo(
                        knobX - knobWidth, knobY + 1.dp.toPx(),
                        knobX - knobWidth * 1.2f, knobY + knobHeight - 3.dp.toPx(),
                        knobX, knobY + knobHeight
                    )
                    cubicTo(
                        knobX + knobWidth * 1.2f, knobY + knobHeight - 3.dp.toPx(),
                        knobX + knobWidth, knobY + 1.dp.toPx(),
                        knobX, knobY - 4.dp.toPx()
                    )
                    close()
                }

                val knobGradient = Brush.linearGradient(
                    colors = listOf(Color(0xFFCFD8DC), Color(0xFF607D8B), Color(0xFF263238)),
                    start = Offset(knobX - knobWidth, knobY),
                    end = Offset(knobX + knobWidth, knobY)
                )
                drawPath(path = knobPath, brush = knobGradient)
            }
        }

        // ==================== KATMAN 4: BİREBİR FLOATING TÜRKÇE GİRİŞ PANELİ (CENTERED & ERGONOMIC) ====================
        // Positioned perfectly in the center of the screen with a spacious width of 310.dp
        // Frosty glass lets the beautiful metallic arch and spotlight shine gorgeously through it!
        AnimatedVisibility(
            visible = isLampOn,
            enter = slideInVertically(initialOffsetY = { it / 3 }) + fadeIn(animationSpec = tween(600)),
            exit = slideOutVertically(targetOffsetY = { it / 3 }) + fadeOut(animationSpec = tween(500)),
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .width(310.dp) // Spacious 310.dp for absolute typing comfort and readability!
                    .graphicsLayer { clip = true }
                    // Translucent ice glass background
                    .background(Color(0xFF151724).copy(alpha = 0.50f), RoundedCornerShape(24.dp))
                    .border(1.dp, Color(0xFFFFFFFF).copy(alpha = 0.12f), RoundedCornerShape(24.dp))
                    .padding(24.dp),
                horizontalAlignment = Alignment.Start
            ) {
                // Kart Başlığı (Dinamik)
                Text(
                    text = if (isRegisterMode) "Aramıza Katıl" else "Hoş Geldiniz",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Ad Soyad Kutusu (Sadece Kayıt Olma Modunda Görünür)
                if (isRegisterMode) {
                    Text(
                        text = "Ad Soyad",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = Color.White.copy(alpha = 0.4f))
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0xFF0F111A).copy(alpha = 0.4f),
                            unfocusedContainerColor = Color(0xFF0F111A).copy(alpha = 0.4f),
                            focusedBorderColor = ColorIndigoGlow,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.08f)
                        )
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // E-posta Kutusu
                Text(
                    text = "E-posta",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp), // Soft round corners matching the card
                    singleLine = true,
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = Color.White.copy(alpha = 0.4f))
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF0F111A).copy(alpha = 0.4f),
                        unfocusedContainerColor = Color(0xFF0F111A).copy(alpha = 0.4f),
                        focusedBorderColor = ColorIndigoGlow,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.08f)
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Şifre Kutusu
                Text(
                    text = "Şifre",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp), // Soft round corners matching the card
                    singleLine = true,
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = Color.White.copy(alpha = 0.4f))
                    },
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.4f)
                            )
                        }
                    },
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF0F111A).copy(alpha = 0.4f),
                        unfocusedContainerColor = Color(0xFF0F111A).copy(alpha = 0.4f),
                        focusedBorderColor = ColorIndigoGlow,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.08f)
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Beni Hatırla (Sadece Giriş Modunda Görünür)
                if (!isRegisterMode) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = rememberMe,
                            onCheckedChange = { rememberMe = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = ColorIndigoGlow,
                                uncheckedColor = Color.White.copy(alpha = 0.2f),
                                checkmarkColor = Color.White
                            )
                        )
                        Text(
                            text = "Beni hatırla",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (authState is AuthState.Error) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = (authState as AuthState.Error).error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Degradeli (Glow) Volumetrik Aksiyon Butonu
                val isButtonEnabled = if (isRegisterMode) {
                    authState !is AuthState.Loading && name.isNotBlank() && email.isNotBlank() && password.isNotBlank()
                } else {
                    authState !is AuthState.Loading && email.isNotBlank() && password.isNotBlank()
                }
                Button(
                    onClick = {
                        if (isRegisterMode) {
                            if (name.isNotBlank() && email.isNotBlank() && password.isNotBlank()) {
                                viewModel.register(RegisterRequest(name, email, password))
                            }
                        } else {
                            if (email.isNotBlank() && password.isNotBlank()) {
                                viewModel.login(LoginRequest(email, password))
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .then(
                            if (isButtonEnabled) {
                                Modifier.background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(Color(0xFF5E3EFF), Color(0xFF7586FF))
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            } else Modifier
                        ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = isButtonEnabled,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent, // Let the background brush shine
                        disabledContainerColor = Color.White.copy(alpha = 0.12f)
                    ),
                    contentPadding = PaddingValues() // Eliminate default padding for full brush display
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (authState is AuthState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = if (isRegisterMode) "Kayıt Ol" else "Giriş Yap",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Divider(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.08f))
                    Text(
                        text = " veya ",
                        color = Color.White.copy(alpha = 0.35f),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Divider(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.08f))
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { viewModel.loginWithGoogleMock() },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White.copy(alpha = 0.05f),
                        contentColor = Color.White
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        GoogleColoredIcon()
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (isRegisterMode) "Google ile Kayıt Ol" else "Google ile Giriş Yap",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Modlar Arası Geçiş Footer Bağlantısı
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    val annotatedText = buildAnnotatedString {
                        if (isRegisterMode) {
                            withStyle(style = SpanStyle(color = Color.White.copy(alpha = 0.65f))) {
                                append("Zaten hesabınız var mı? ")
                            }
                            withStyle(style = SpanStyle(color = ColorIndigoGlow, fontWeight = FontWeight.Bold)) {
                                append("Giriş Yapın")
                            }
                        } else {
                            withStyle(style = SpanStyle(color = Color.White.copy(alpha = 0.65f))) {
                                append("Hesabınız yoksa ")
                            }
                            withStyle(style = SpanStyle(color = ColorIndigoGlow, fontWeight = FontWeight.Bold)) {
                                append("Kayıt Olun")
                            }
                        }
                    }
                    Text(
                        text = annotatedText,
                        fontSize = 13.sp,
                        modifier = Modifier.clickable {
                            isRegisterMode = !isRegisterMode
                            viewModel.resetState() // Reset loading/error states
                            name = ""
                            email = ""
                            password = ""
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun GoogleColoredIcon() {
    Canvas(modifier = Modifier.size(18.dp)) {
        val strokeWidth = 2.5f.dp.toPx()
        val radius = 9.dp.toPx()
        val center = Offset(size.width / 2, size.height / 2)
        
        // Draw the horizontal blue bar of the G
        drawRect(
            color = Color(0xFF4285F4),
            topLeft = Offset(center.x - 1.dp.toPx(), center.y - 1.25f.dp.toPx()),
            size = Size(8.dp.toPx(), 2.5f.dp.toPx())
        )
        
        // Red segment (top arc)
        drawArc(
            color = Color(0xFFEA4335),
            startAngle = 180f,
            sweepAngle = 135f,
            useCenter = false,
            style = Stroke(width = strokeWidth)
        )
        
        // Blue segment (right arc)
        drawArc(
            color = Color(0xFF4285F4),
            startAngle = -45f,
            sweepAngle = 90f,
            useCenter = false,
            style = Stroke(width = strokeWidth)
        )
        
        // Green segment (bottom arc)
        drawArc(
            color = Color(0xFF34A853),
            startAngle = 45f,
            sweepAngle = 135f,
            useCenter = false,
            style = Stroke(width = strokeWidth)
        )
        
        // Yellow segment (left arc)
        drawArc(
            color = Color(0xFFFBBC05),
            startAngle = 135f,
            sweepAngle = 90f,
            useCenter = false,
            style = Stroke(width = strokeWidth)
        )
    }
}
