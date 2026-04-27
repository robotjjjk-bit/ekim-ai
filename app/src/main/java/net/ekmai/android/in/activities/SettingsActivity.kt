package net.ekmai.android.`in`.activities

import net.ekmai.android.`in`.R
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.core.net.toUri
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import net.ekmai.android.`in`.components.ModelSelectorDropdown
import net.ekmai.android.`in`.utilities.ApiManager
import net.ekmai.android.`in`.utilities.ModelsManager
import net.ekmai.android.`in`.ui.theme.EkmAITheme
import net.ekmai.android.`in`.ui.theme.ThemeManager
import net.ekmai.android.`in`.ui.theme.ThemeOption
import net.ekmai.android.`in`.ui.theme.ThemeViewModel
import net.ekmai.android.`in`.utilities.ChatViewModel
import kotlin.getValue
import net.ekmai.android.`in`.utilities.VersionChecker

fun Activity.launchSettings() {
    val intent = Intent(this, SettingsActivity::class.java)
    startActivity(intent)
    overridePendingTransition(R.anim.slide_up_in, R.anim.fade_out_slow)
}

class SettingsActivity : ComponentActivity() {

    private val viewModel by viewModels<ThemeViewModel> {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ThemeViewModel(this@SettingsActivity) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        onBackPressedDispatcher.addCallback(this) {
            finishWithAnimation()
        }

        ThemeManager.themeState.value = net.ekmai.android.`in`.ui.theme.getTheme(this)
        setContent {
            val theme by viewModel.theme.collectAsState()
            EkmAITheme(
                themeOption = theme
            ) {
                SettingsScreen(
                    onBack = { finishWithAnimation() }
                )
            }
        }
    }

    private fun finishWithAnimation() {
        finish()
        overridePendingTransition(
            R.anim.fade_in_slow,
            R.anim.slide_down_out
        )
    }
}

private val Purple50  = Color(0xFFEEEDFE)
private val Purple200 = Color(0xFFAFA9EC)
private val Purple600 = Color(0xFF534AB7)
private val Teal50    = Color(0xFFE1F5EE)
private val Teal600   = Color(0xFF0F6E56)
private val Coral50   = Color(0xFFFAECE7)
private val Coral600  = Color(0xFF993C1D)
private val Blue50    = Color(0xFFE6F1FB)
private val Blue600   = Color(0xFF185FA5)
private val Amber50   = Color(0xFFFAEEDA)
private val Amber600  = Color(0xFF854F0B)
private val Pink50    = Color(0xFFFBEAF0)
private val Pink600   = Color(0xFF993556)
private val Red50     = Color(0xFFFCEBEB)
private val Red600    = Color(0xFFA32D2D)
private val Gray50    = Color(0xFFF1EFE8)
private val Gray600   = Color(0xFF5F5E5A)
private val Green50   = Color(0xFFEAF3DE)
private val Green600  = Color(0xFF3B6D11)

private data class IconColors(val bg: Color, val tint: Color)
private enum class RowTrailing { CHEVRON, NONE }

fun getSaving(context: Context): Flow<Boolean> {
    return context.settingsDataStore.data.map { prefs ->
        prefs[SettingsDatastore.IMPORT_EXPORT] ?: false
    }
}

suspend fun saveSaving(context: Context, value: Boolean) {
    context.settingsDataStore.edit { prefs ->
        prefs[SettingsDatastore.IMPORT_EXPORT] = value
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current
    var showReasoning by remember {
        mutableStateOf(if (isPreview) false else ApiManager.showReasoning)
    }
    var showSelector by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var currentModel by remember { mutableStateOf(ModelsManager.getCurrentModel()) }

    val import_export by getSaving(context).collectAsState(initial = false)

    val versionChecker = VersionChecker()
    val currentV = context.packageManager.getPackageInfo(context.packageName, 0).versionName

    var latestVersion = "null"
    var updateApkUrl = ""
    versionChecker.fetchLatestVersion(object : VersionChecker.VersionCallback {
        override fun onResponse(info: VersionChecker.VersionInfo) {
            val latestTag = info.tagName
            val latestLink = info.downloadUrl

            latestVersion = latestTag
            updateApkUrl = latestLink

            if (versionChecker.isNewerVersion(currentV, latestTag)) {
                println("Update found: $latestTag at $latestLink")
            }
        }

        override fun onError(message: String) {
            println("Update check failed: $message")
        }
    })

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            AnimatedVisibility(
                visible = showSelector,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    shadowElevation = 16.dp
                ) {
                    ModelSelectorDropdown(
                        models = ModelsManager.getAllModels(),
                        selectedModel = currentModel,
                        onModelSelected = {
                            currentModel = it
                            showSelector = false
                            scope.launch {
                                ModelsManager.setModel(context, it.name)
                            }
                        },
                        modifier = Modifier.padding(16.dp).navigationBarsPadding()
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->

        LazyColumn(
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding() + 12.dp,
                bottom = padding.calculateBottomPadding() + 32.dp,
                start = 16.dp,
                end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {

            item {
                ProfileHero()
                Spacer(modifier = Modifier.height(8.dp))
            }

            item { SectionLabel("AI & Models") }
            item {
                SettingsCard {
                    SettingsRow(
                        icon = Icons.Outlined.AutoAwesome,
                        iconColors = IconColors(Purple50, Purple600),
                        title = "Current Model",
                        subtitle = "Active AI provider",
                        trailingText = currentModel.name,
                        onClick = {
                            showSelector = !showSelector
                        }
                    )
                    RowDivider()
//                    SettingsRow(
//                        icon = Icons.Outlined.Widgets,
//                        iconColors = IconColors(Blue50, Blue600),
//                        title = "Manage Models",
//                        subtitle = "Add, remove, reorder models",
//                        onClick = {}
//                    )
//                    RowDivider()
                    ToggleRow(
                        icon = Icons.Outlined.Psychology,
                        iconColors = IconColors(Teal50, Teal600),
                        title = "Show Reasoning",
                        subtitle = "Append reasoning process to replies",
                        checked = showReasoning,
                        onCheckedChange = {
                            showReasoning = it
                            ApiManager.showReasoning = it
                        }
                    )
                    RowDivider()
                    SettingsRow(
                        icon = Icons.Outlined.EditNote,
                        iconColors = IconColors(Amber50, Amber600),
                        title = "System Prompt",
                        subtitle = "Customize AI instructions",
                        onClick = {
                            (context as Activity).launchSystemPrompt()
                        }
                    )
                }
            }
            item { SectionLabel("Chat") }
            item {
                SettingsCard {
                    ToggleRow(
                        icon = Icons.Outlined.FileDownload,
                        iconColors = IconColors(Coral50, Coral600),
                        title = "Import Export",
                        subtitle = "Allows user to save and reload chats using json",
                        checked = import_export,
                        onCheckedChange = {
                            scope.launch {
                                saveSaving(context, !import_export)
                            }
                        }
                    )
                }
            }

            item { SectionLabel("Appearance") }
            item {
                SettingsCard {
                    SettingsRow(
                        icon = Icons.Outlined.DarkMode,
                        iconColors = IconColors(Gray50, Gray600),
                        title = "Theme",
                        subtitle = "Light, dark, or system",
                        trailingText = "Default",
                        onClick = {
                            (context as Activity).launchThemeSelector()
                        }
                    )
                }
            }

            item { SectionLabel("About") }
            item {
                SettingsCard {
                    SettingsRow(
                        icon = Icons.Outlined.Code,
                        iconColors = IconColors(Teal50, Teal600),
                        title = "App Version",
                        trailing = RowTrailing.NONE,
                        trailingBadge = currentV,
                        trailingBadgeBg = Blue50,
                        trailingBadgeText = Blue600
                    )
                    RowDivider()
                    SettingsRow(
                        icon = Icons.Outlined.SystemUpdate,
                        iconColors = IconColors(Amber50, Amber600),
                        title = "Check for Updates",
                        trailingBadge = when {
                            versionChecker.isNewerVersion(currentV, latestVersion) -> "Action required"
                            else -> "Up to date"
                        },
                        trailingBadgeBg = if (versionChecker.isNewerVersion(currentV, latestVersion)) Coral50 else Teal50,
                        trailingBadgeText = if (versionChecker.isNewerVersion(currentV, latestVersion)) Coral600 else Teal600,
                        onClick = {
                            if (versionChecker.isNewerVersion(currentV, latestVersion)) {
                                val intent = Intent(Intent.ACTION_VIEW, updateApkUrl.toUri())
                                context.startActivity(intent)
                            } else {
                                Toast.makeText(context, "The latest version is already installed", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                    RowDivider()
                    SettingsRow(
                        icon = Icons.Outlined.Shield,
                        iconColors = IconColors(Gray50, Gray600),
                        title = "Privacy Policy",
                        onClick = {
                            (context as Activity).launchPrivacyPolicy()
                        }
                    )
                }
            }

            item { SectionLabel("Current caches") }
            item {
                SettingsCard {
                    SettingsRow(
                        icon = Icons.Outlined.DeleteOutline,
                        iconColors = IconColors(Red50, Red600),
                        title = "Clear All Cache",
                        titleColor = Red600,
                        onClick = {
                            context.cacheDir.deleteRecursively()
                            context.externalCacheDirs.forEach { it?.deleteRecursively() }
                            Toast.makeText(context, "You can clear cache manually for better cleaning", Toast.LENGTH_SHORT).show()
                            openAppSettings(context)
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "ekm AI · made with care",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentWidth()
                )
            }
        }
    }
}

@Composable
private fun ProfileHero() {
    val interactionSource = remember {
        androidx.compose.foundation.interaction.MutableInteractionSource()
    }
    val isPressed by interactionSource.collectIsPressedAsState()
    val bgAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.18f else 0.10f,
        label = "barAlpha"
    )
    val context = LocalContext.current
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = ModelsManager.getCurrentModel().tintColor.copy(bgAlpha),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(Purple200, Color(0xFFD4537E)))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Ekm",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Color(0xFF1D9E75))
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Ekm AI",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "A product by ekam labs",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            OutlinedButton(
                onClick = {
                    (context as Activity).launchAbout()
                },
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Text(text = "About", fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.08.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        modifier = Modifier.padding(start = 4.dp, top = 12.dp, bottom = 4.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    val interactionSource = remember {
        androidx.compose.foundation.interaction.MutableInteractionSource()
    }
    val isPressed by interactionSource.collectIsPressedAsState()
    val bgAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.20f else 0.12f,
        label = "barAlpha"
    )
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = ModelsManager.getCurrentModel().tintColor.copy(bgAlpha),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.fillMaxWidth(), content = content)
    }
}

@Composable
private fun RowDivider() {
    HorizontalDivider(
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outlineVariant,
        modifier = Modifier.padding(start = 66.dp)
    )
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    iconColors: IconColors,
    title: String,
    subtitle: String? = null,
    trailing: RowTrailing = RowTrailing.CHEVRON,
    trailingText: String? = null,
    trailingBadge: String? = null,
    trailingBadgeBg: Color = Teal50,
    trailingBadgeText: Color = Teal600,
    titleColor: Color? = null,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconColors.bg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColors.tint,
                modifier = Modifier.size(18.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                color = titleColor ?: MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 1.dp)
                )
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (trailingText != null) {
                Text(
                    text = trailingText,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (trailingBadge != null) {
                Text(
                    text = trailingBadge,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = trailingBadgeText,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(trailingBadgeBg)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
            if (trailing == RowTrailing.CHEVRON) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowForwardIos,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
private fun ToggleRow(
    icon: ImageVector,
    iconColors: IconColors,
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconColors.bg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColors.tint,
                modifier = Modifier.size(18.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 1.dp)
                )
            }
        }
        EkmSwitch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun EkmSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 20.dp else 0.dp,
        animationSpec = tween(200),
        label = "thumb"
    )
    val trackColor by animateColorAsState(
        targetValue = if (checked) ModelsManager.getCurrentModel().tintColor else MaterialTheme.colorScheme.outlineVariant,
        animationSpec = tween(200),
        label = "track"
    )
    Box(
        modifier = Modifier
            .size(width = 44.dp, height = 24.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(trackColor)
            .clickable { onCheckedChange(!checked) }
            .padding(3.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(18.dp)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}

fun openAppSettings(context: Context) {
    val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
    }
    context.startActivity(intent)
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    EkmAITheme(
        ThemeOption.DARK
    ) {
        SettingsScreen()
    }
}

val Context.settingsDataStore by preferencesDataStore(name = "settings")

object SettingsDatastore {
    val SHOW_REASONING = booleanPreferencesKey("show_reasoning")
    val IMPORT_EXPORT = booleanPreferencesKey("import_export")
}