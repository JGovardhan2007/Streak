package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.Badge
import com.example.ui.StreakViewModel
import com.example.ui.components.UserAvatarView
import com.example.ui.components.GoogleAccountPickerDialog
import com.example.ui.theme.AppThemeAccent
import com.example.ui.theme.LocalAccentColor
import com.example.ui.theme.StreakOrange
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: StreakViewModel,
    isDarkTheme: Boolean,
    onToggleDarkTheme: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val stats by viewModel.dashboardStats.collectAsState()
    val badges by viewModel.badges.collectAsState()
    val currentAccent by viewModel.currentThemeAccent.collectAsState()
    val accentColor = LocalAccentColor.current

    val userName by viewModel.userName.collectAsState()
    val userBio by viewModel.userBio.collectAsState()
    val userAvatar by viewModel.userAvatar.collectAsState()
    val currentUserEmail by viewModel.currentUserEmail.collectAsState()
    val isCloudSyncing by viewModel.isCloudSyncing.collectAsState()

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showGoogleAccountPicker by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("All") }
    var selectedBadgeForDetail by remember { mutableStateOf<Badge?>(null) }

    val filteredBadges = remember(badges, selectedFilter) {
        when (selectedFilter) {
            "Unlocked" -> badges.filter { it.isUnlocked }
            "Locked" -> badges.filter { !it.isUnlocked }
            "Streak" -> badges.filter { it.category == "Streak" }
            "Consistency" -> badges.filter { it.category == "Consistency" }
            "Circle" -> badges.filter { it.category == "Circle" || it.category == "Squad" || it.category == "Peer" }
            else -> badges
        }
    }

    val unlockedCount = badges.count { it.isUnlocked }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile & Achievements", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 8.dp,
                bottom = innerPadding.calculateBottomPadding() + 80.dp,
                start = 16.dp,
                end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // User Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        UserAvatarView(
                            avatar = userAvatar,
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .border(3.dp, accentColor, CircleShape),
                            size = 72.dp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "$userName (You)",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = userBio,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedButton(
                            onClick = { showEditProfileDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("edit_profile_btn")
                        ) {
                            Text("Edit Profile ✏️", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocalFireDepartment,
                                        contentDescription = "Streak Days",
                                        tint = StreakOrange,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${stats.totalStreakDays}",
                                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                        color = StreakOrange
                                    )
                                }
                                Text("Total Days", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            Divider(
                                modifier = Modifier
                                    .height(36.dp)
                                    .width(1.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            )

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$unlockedCount / ${badges.size}",
                                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                    color = accentColor
                                )
                                Text("Badges Earned", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            // Firebase Auth & Cloud Sync Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Account & Sync",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            if (currentUserEmail != null) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = "Online 🟢",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = "Guest Mode",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = if (currentUserEmail != null)
                                "Signed in as $currentUserEmail. Your habits, streaks, and squad milestones sync securely across all your devices."
                            else
                                "Sign in to your account to securely sync your habits, streaks, and squad milestones.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (currentUserEmail == null) {
                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            try {
                                                val credentialManager = androidx.credentials.CredentialManager.create(context)
                                                val googleIdOption = com.google.android.libraries.identity.googleid.GetGoogleIdOption.Builder()
                                                    .setFilterByAuthorizedAccounts(false)
                                                    .setAutoSelectEnabled(false)
                                                    .build()
                                                val request = androidx.credentials.GetCredentialRequest.Builder()
                                                    .addCredentialOption(googleIdOption)
                                                    .build()
                                                val result = credentialManager.getCredential(context as android.app.Activity, request)
                                                val credential = result.credential
                                                if (credential is androidx.credentials.CustomCredential &&
                                                    credential.type == com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                                    val googleIdTokenCredential = com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.createFrom(credential.data)
                                                    viewModel.googleSignInWithOAuth(googleIdTokenCredential.idToken, googleIdTokenCredential.id)
                                                } else {
                                                    showGoogleAccountPicker = true
                                                }
                                            } catch (e: Exception) {
                                                showGoogleAccountPicker = true
                                            }
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("firebase_sign_in_btn")
                                ) {
                                    Text("Sign In with Google", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            } else {
                                Button(
                                    onClick = { viewModel.syncToFirestore() },
                                    enabled = !isCloudSyncing,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("sync_firestore_btn")
                                ) {
                                    if (isCloudSyncing) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }
                                    Text("Sync to Cloud ☁️", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }

                                OutlinedButton(
                                    onClick = { viewModel.signOut() },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("firebase_sign_out_btn")
                                ) {
                                    Text("Sign Out", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Badges & Achievements Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "MILESTONES & BADGES",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "$unlockedCount of ${badges.size} Unlocked",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = accentColor.copy(alpha = 0.15f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = accentColor, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Level ${unlockedCount + 1}", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = accentColor)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Category/Status Filter Chips
                        val filters = listOf("All", "Unlocked", "Locked", "Streak", "Consistency", "Circle")
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(filters) { filter ->
                                val isSelected = selectedFilter == filter
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedFilter = filter },
                                    label = { Text(filter, fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = accentColor,
                                        selectedLabelColor = currentAccent.onPrimaryColor
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("chip_badge_filter_$filter")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Compact Horizontal Badges Scroll Row
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(filteredBadges, key = { it.id }) { badge ->
                                BadgeCardItem(
                                    badge = badge,
                                    onClick = { selectedBadgeForDetail = badge },
                                    modifier = Modifier.width(130.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Theme Accent & Preferences Settings
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("settings_theme_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "APP THEME & COLOR ACCENT",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Personalize your Streak color accent scheme",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Theme Accent Palette Selector
                        Text(
                            text = "Color Accent Theme",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(AppThemeAccent.entries) { accent ->
                                val isSelected = currentAccent == accent
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isSelected) accent.primaryColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                                    border = BorderStroke(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) accent.primaryColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                    ),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable { viewModel.setThemeAccent(accent) }
                                        .testTag("theme_accent_${accent.name}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = accent.primaryColor,
                                            modifier = Modifier.size(20.dp)
                                        ) {}
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = accent.displayName,
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (isSelected) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Icon(
                                                Icons.Default.CheckCircle,
                                                contentDescription = "Selected",
                                                tint = accent.primaryColor,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Divider(color = MaterialTheme.colorScheme.surfaceVariant)

                        Spacer(modifier = Modifier.height(14.dp))

                        // Dark Canvas Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                                    contentDescription = "Theme Mode",
                                    tint = accentColor
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Dark Canvas",
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (isDarkTheme) "High-contrast dark layout" else "Crisp light surface layout",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Switch(
                                checked = isDarkTheme,
                                onCheckedChange = onToggleDarkTheme,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = currentAccent.onPrimaryColor,
                                    checkedTrackColor = accentColor
                                ),
                                modifier = Modifier.testTag("theme_toggle_switch")
                            )
                        }
                    }
                }
            }

            // About App Footer
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    Text(
                        text = "Streak v1.0",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Build Great Habits Together",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }

    // Badge Detail Dialog
    selectedBadgeForDetail?.let { badge ->
        AlertDialog(
            onDismissRequest = { selectedBadgeForDetail = null },
            icon = { Text(text = badge.icon, fontSize = 48.sp) },
            title = {
                Text(
                    text = badge.title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (badge.isUnlocked) accentColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = if (badge.isUnlocked) "UNLOCKED ${badge.unlockedDate ?: ""}" else "IN PROGRESS",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (badge.isUnlocked) accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = badge.description,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (!badge.isUnlocked) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Requirement", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(badge.progressText, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { badge.progressFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = StreakOrange,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            },
            confirmButton = {
                if (badge.isUnlocked) {
                    Button(
                        onClick = {
                            viewModel.shareBadgeAchievement(badge)
                            selectedBadgeForDetail = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = MaterialTheme.colorScheme.onPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("btn_share_badge")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Brag to Circle", fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    TextButton(onClick = { selectedBadgeForDetail = null }) {
                        Text("Close", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                if (badge.isUnlocked) {
                    TextButton(onClick = { selectedBadgeForDetail = null }) {
                        Text("Dismiss")
                    }
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    if (showEditProfileDialog) {
        var nameInput by remember { mutableStateOf(userName) }
        var bioInput by remember { mutableStateOf(userBio) }
        var avatarInput by remember { mutableStateOf(userAvatar) }
        var emailInput by remember { mutableStateOf(currentUserEmail ?: "") }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = {
                Text(
                    text = "Edit User Profile",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Display Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = bioInput,
                        onValueChange = { bioInput = it },
                        label = { Text("Bio / Status") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Text(
                        text = "Choose Profile Avatar",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    val presetAvatars = listOf("🦁", "🦊", "🐼", "🦉", "🐯", "🐨", "🦄", "🦖")

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(presetAvatars) { emoji ->
                            val isSelected = avatarInput == emoji
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) accentColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                        shape = CircleShape
                                    )
                                    .clickable { avatarInput = emoji },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = emoji, fontSize = 24.sp)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Account Email") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showEditProfileDialog = false
                        viewModel.updateProfile(nameInput, bioInput, avatarInput, emailInput)
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("confirm_edit_profile_btn")
                ) {
                    Text("Save Changes", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showEditProfileDialog = false },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("cancel_edit_profile_btn")
                ) {
                    Text("Cancel", fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    if (showGoogleAccountPicker) {
        GoogleAccountPickerDialog(
            onDismiss = { showGoogleAccountPicker = false },
            onAccountSelected = { email ->
                showGoogleAccountPicker = false
                viewModel.googleSignIn(email)
            },
            onOAuthSuccess = { idToken, email ->
                showGoogleAccountPicker = false
                viewModel.googleSignInWithOAuth(idToken, email)
            },
            onRegisterSelected = { email ->
                showGoogleAccountPicker = false
                viewModel.register(email)
            }
        )
    }
}

@Composable
fun BadgeCardItem(
    badge: Badge,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = LocalAccentColor.current
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (badge.isUnlocked) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = if (badge.isUnlocked) BorderStroke(1.dp, accentColor.copy(alpha = 0.6f)) else null,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("badge_card_${badge.id}")
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.TopEnd) {
                Text(
                    text = badge.icon,
                    fontSize = 24.sp,
                    modifier = Modifier.padding(2.dp)
                )

                if (!badge.isUnlocked) {
                    Surface(
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = "Locked",
                                tint = Color.White,
                                modifier = Modifier.size(10.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = badge.title,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = if (badge.isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = badge.progressText,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = if (badge.isUnlocked) accentColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                maxLines = 1
            )

            if (!badge.isUnlocked) {
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { badge.progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = StreakOrange,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}

