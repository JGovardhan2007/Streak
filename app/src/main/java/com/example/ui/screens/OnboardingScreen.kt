package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.StreakViewModel
import com.example.ui.components.GoogleAccountPickerDialog
import kotlinx.coroutines.launch

data class OnboardingPage(
    val title: String,
    val subtitle: String,
    val description: String,
    val icon: ImageVector,
    val accentColor: Color
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    viewModel: StreakViewModel,
    onFinished: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var showAuthDialog by remember { mutableStateOf(false) }
    var showGoogleAccountPicker by remember { mutableStateOf(false) }
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }

    val pages = listOf(
        OnboardingPage(
            title = "Build Unbreakable Habits",
            subtitle = "Daily Consistency Engine",
            description = "Track daily routines, maintain blazing hot fire streaks, and achieve your personal goals effortlessly with smart reminders.",
            icon = Icons.Filled.LocalFireDepartment,
            accentColor = MaterialTheme.colorScheme.primary
        ),
        OnboardingPage(
            title = "Habit Circles & Squads",
            subtitle = "Together We Thrive",
            description = "Form circles with friends and colleagues, share photo proof check-ins, send ⚡ nudges, and conquer milestones as a team.",
            icon = Icons.Filled.Group,
            accentColor = MaterialTheme.colorScheme.secondary
        ),
        OnboardingPage(
            title = "Analytics & Streak Freezes",
            subtitle = "Protected Progress",
            description = "Gain deep insights into your weekly consistency rates, earn achievement badges, and safeguard streaks with Streak Freeze passes.",
            icon = Icons.Filled.Shield,
            accentColor = MaterialTheme.colorScheme.tertiary
        )
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background subtle gradient glow
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                MaterialTheme.colorScheme.surface,
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top bar skip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "STREAK",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.5.sp
                            )
                        )
                    }

                    TextButton(
                        onClick = {
                            viewModel.setOnboardingCompleted(true)
                            onFinished()
                        },
                        modifier = Modifier.testTag("skip_onboarding_button")
                    ) {
                        Text("Skip", fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Pager
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) { pageIndex ->
                    val page = pages[pageIndex]
                    val isCurrentPage = pagerState.currentPage == pageIndex

                    val pageScale by animateFloatAsState(
                        targetValue = if (isCurrentPage) 1f else 0.82f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "PageSpringScale"
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Animated Hero Illustration Card
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(170.dp)
                                .graphicsLayer(scaleX = pageScale, scaleY = pageScale)
                        ) {
                            // Background radial glow halo
                            Box(
                                modifier = Modifier
                                    .size(170.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(
                                                page.accentColor.copy(alpha = 0.35f),
                                                page.accentColor.copy(alpha = 0.1f),
                                                Color.Transparent
                                            )
                                        )
                                    )
                            )

                            Surface(
                                shape = RoundedCornerShape(36.dp),
                                color = page.accentColor.copy(alpha = 0.18f),
                                modifier = Modifier.size(140.dp),
                                shadowElevation = 0.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = page.icon,
                                        contentDescription = null,
                                        tint = page.accentColor,
                                        modifier = Modifier.size(72.dp)
                                    )
                                }
                            }

                            // Secondary decorative badge
                            Surface(
                                shape = CircleShape,
                                color = page.accentColor,
                                shadowElevation = 4.dp,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .offset(x = (-8).dp, y = (-8).dp)
                                    .size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(40.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = page.accentColor.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = page.subtitle.uppercase(),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp
                                ),
                                color = page.accentColor
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = page.title,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = page.description,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Indicators
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(3) { index ->
                        val isSelected = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .height(8.dp)
                                .width(if (isSelected) 24.dp else 8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outlineVariant
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Action buttons
                if (pagerState.currentPage == 2) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                try {
                                    val credentialManager = androidx.credentials.CredentialManager.create(context)
                                    val googleIdOption = com.google.android.libraries.identity.googleid.GetGoogleIdOption.Builder()
                                        .setFilterByAuthorizedAccounts(false)
                                        .setAutoSelectEnabled(false)
                                        .build()
                                    val request = androidx.credentials.GetCredentialRequest.Builder()
                                        .addCredentialOption(googleIdOption)
                                        .build()
                                    coroutineScope.launch {
                                        try {
                                            val result = credentialManager.getCredential(context as android.app.Activity, request)
                                            val credential = result.credential
                                            if (credential is androidx.credentials.CustomCredential &&
                                                credential.type == com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                                val googleIdTokenCredential = com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.createFrom(credential.data)
                                                viewModel.googleSignInWithOAuth(googleIdTokenCredential.idToken, googleIdTokenCredential.id)
                                                viewModel.setOnboardingCompleted(true)
                                                onFinished()
                                            } else {
                                                showGoogleAccountPicker = true
                                            }
                                        } catch (e: Exception) {
                                            showGoogleAccountPicker = true
                                        }
                                    }
                                } catch (e: Exception) {
                                    showGoogleAccountPicker = true
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .testTag("google_signin_onboarding_btn"),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        ) {
                            Icon(Icons.Filled.CloudSync, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Sign In with Google",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Button(
                            onClick = { showAuthDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .testTag("get_started_signin_button"),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Filled.AccountCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Sign In with Email",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.setOnboardingCompleted(true)
                                onFinished()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .testTag("continue_as_guest_button"),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = "Continue as Guest",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }
                } else {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("next_onboarding_button"),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "Next",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Filled.ArrowForward, contentDescription = null)
                    }
                }
            }
        }
    }

    if (showAuthDialog) {
        AlertDialog(
            onDismissRequest = { showAuthDialog = false },
            title = {
                Text("Sign In to Streak", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            showGoogleAccountPicker = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("dialog_google_signin_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(Icons.Filled.CloudSync, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sign In with Google", fontWeight = FontWeight.Bold)
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    Text(
                        text = "Or enter your email to sync your habit circles and streaks.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Email Address") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_email_input")
                    )
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("Password") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_password_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (emailInput.isNotBlank()) {
                            viewModel.signIn(emailInput.trim())
                            viewModel.setOnboardingCompleted(true)
                            showAuthDialog = false
                            onFinished()
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("auth_confirm_button")
                ) {
                    Text("Sign In", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.setOnboardingCompleted(true)
                        showAuthDialog = false
                        onFinished()
                    }
                ) {
                    Text("Skip for Now")
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    if (showGoogleAccountPicker) {
        GoogleAccountPickerDialog(
            onDismiss = { showGoogleAccountPicker = false },
            onAccountSelected = { email ->
                showGoogleAccountPicker = false
                showAuthDialog = false
                viewModel.googleSignIn(email)
                viewModel.setOnboardingCompleted(true)
                onFinished()
            },
            onOAuthSuccess = { idToken, email ->
                showGoogleAccountPicker = false
                showAuthDialog = false
                viewModel.googleSignInWithOAuth(idToken, email)
                viewModel.setOnboardingCompleted(true)
                onFinished()
            },
            onRegisterSelected = { email ->
                showGoogleAccountPicker = false
                showAuthDialog = false
                viewModel.register(email)
                viewModel.setOnboardingCompleted(true)
                onFinished()
            }
        )
    }
}
