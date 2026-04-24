package net.ekmai.android.`in`.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Contrast
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.SettingsBrightness
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jakewharton.processphoenix.ProcessPhoenix
import net.ekmai.android.`in`.R
import net.ekmai.android.`in`.ui.theme.EkmAITheme
import net.ekmai.android.`in`.ui.theme.ThemeOption
import net.ekmai.android.`in`.ui.theme.ThemeViewModel
import net.ekmai.android.`in`.utilities.ModelsManager
import kotlin.getValue

private fun Activity.finishWithAnim() {
    finish()
    overridePendingTransition(R.anim.fade_in_slow, R.anim.slide_down_out)
}

fun Activity.launchThemeSelector() {
    startActivity(Intent(this, ThemeActivity::class.java))
    overridePendingTransition(R.anim.slide_up_in, R.anim.fade_out_slow)
}

class ThemeActivity : ComponentActivity() {
    private val viewModel by viewModels<ThemeViewModel> {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ThemeViewModel(this@ThemeActivity) as T
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        onBackPressedDispatcher.addCallback(this) { finishWithAnim() }
        setContent {
            val theme by viewModel.theme.collectAsState()
            EkmAITheme(theme) { ThemeScreen(viewModel, onBack = { finishWithAnim() }) }
        }
    }
}

enum class AppTheme(
    val label: String,
    val subtitle: String,
    val icon: ImageVector,
    val previewBgTop: Color,
    val previewBgBottom: Color,
    val previewBubble: Color,
    val previewText: Color
) {
    LIGHT(
        "Light", "Always bright", Icons.Outlined.LightMode,
        Color(0xFFF8F8F8), Color(0xFFEEEEEE), Color(0xFF534AB7), Color(0xFF333333)
    ),
    DARK(
        "Dark", "Always dark", Icons.Outlined.DarkMode,
        Color(0xFF1C1C1E), Color(0xFF2C2C2E), Color(0xFF7F77DD), Color(0xFFEEEEEE)
    ),
    SYSTEM(
        "System", "Follows device", Icons.Outlined.SettingsBrightness,
        Color(0xFFEEEEEE), Color(0xFF222222), Color(0xFF534AB7), Color(0xFF888888)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeScreen(
    viewModel: ThemeViewModel? = null,
    onBack: () -> Unit = {}
) {
    val accent = ModelsManager.getCurrentModel().tintColor
    var selected by remember { mutableStateOf(AppTheme.SYSTEM) }
    val interactionSource = remember {
        androidx.compose.foundation.interaction.MutableInteractionSource()
    }
    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Appearance", fontSize = 20.sp, fontWeight = FontWeight.Medium)
                        Text("Choose your theme", fontSize = 12.sp, color = accent, fontWeight = FontWeight.Medium)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, null) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding() + 12.dp,
                bottom = padding.calculateBottomPadding() + 32.dp,
                start = 16.dp, end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            item {
                Text(
                    "Preview", fontSize = 11.sp, fontWeight = FontWeight.Medium,
                    letterSpacing = 0.08.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AppTheme.entries.forEach { theme ->
                        BigThemeCard(
                            theme = theme,
                            isSelected = theme == selected,
                            accent = accent,
                            onClick = {
                                selected = theme
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            item {
                Text(
                    "Theme", fontSize = 11.sp, fontWeight = FontWeight.Medium,
                    letterSpacing = 0.08.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp, bottom = 4.dp)
                )
            }
            item {
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
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        AppTheme.entries.forEachIndexed { i, theme ->
                            ThemeOptionRow(theme, theme == selected, accent) { selected = theme }
                            if (i < AppTheme.entries.lastIndex) {
                                HorizontalDivider(
                                    thickness = 0.5.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                    modifier = Modifier.padding(start = 62.dp)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = { applyAppTheme(viewModel, selected); onBack(); ProcessPhoenix.triggerRebirth(context) },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accent),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Icon(Icons.Outlined.Check, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Apply ${selected.label} Theme", fontWeight = FontWeight.Medium, fontSize = 15.sp)
                }
            }
        }
    }
}

@Composable
private fun BigThemeCard(
    theme: AppTheme,
    isSelected: Boolean,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.95f,
        animationSpec = tween(200), label = "scale"
    )
    val borderAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = tween(200), label = "border"
    )

    Column(
        modifier = modifier.scale(scale),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.65f)
                .clip(RoundedCornerShape(14.dp))
                .border(2.dp, accent.copy(alpha = borderAlpha), RoundedCornerShape(14.dp))
                .background(Brush.verticalGradient(listOf(theme.previewBgTop, theme.previewBgBottom)))
                .clickable(onClick = onClick)
                .padding(10.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                // Mock topbar
                Box(modifier = Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(4.dp))
                    .background(theme.previewText.copy(alpha = 0.2f)))

                Spacer(Modifier.height(4.dp))

                // AI bubbles
                Box(modifier = Modifier.fillMaxWidth(0.82f).height(5.dp).clip(RoundedCornerShape(3.dp))
                    .background(theme.previewText.copy(alpha = 0.14f)))
                Box(modifier = Modifier.fillMaxWidth(0.55f).height(5.dp).clip(RoundedCornerShape(3.dp))
                    .background(theme.previewText.copy(alpha = 0.14f)))

                Spacer(Modifier.height(4.dp))

                // User bubble
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                    Box(modifier = Modifier.fillMaxWidth(0.65f).height(5.dp).clip(RoundedCornerShape(3.dp))
                        .background(theme.previewBubble.copy(alpha = 0.75f)))
                }

                Spacer(Modifier.height(4.dp))

                // Another AI line
                Box(modifier = Modifier.fillMaxWidth(0.7f).height(5.dp).clip(RoundedCornerShape(3.dp))
                    .background(theme.previewText.copy(alpha = 0.14f)))
                Box(modifier = Modifier.fillMaxWidth(0.45f).height(5.dp).clip(RoundedCornerShape(3.dp))
                    .background(theme.previewText.copy(alpha = 0.14f)))
            }

            // Selected check
            if (isSelected) {
                Box(
                    modifier = Modifier.align(Alignment.TopEnd).size(22.dp).clip(CircleShape)
                        .background(accent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Check, null, tint = Color.White, modifier = Modifier.size(12.dp))
                }
            }
        }

        Text(
            text = theme.label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
            color = if (isSelected) accent else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ThemeOptionRow(
    theme: AppTheme,
    isSelected: Boolean,
    accent: Color,
    onClick: () -> Unit
) {
    val bgAlpha by animateFloatAsState(targetValue = if (isSelected) 0.09f else 0f, animationSpec = tween(200), label = "bg")
    val radioColor by animateColorAsState(
        targetValue = if (isSelected) accent else MaterialTheme.colorScheme.outlineVariant,
        animationSpec = tween(200), label = "radio"
    )

    Row(
        modifier = Modifier.fillMaxWidth()
            .background(accent.copy(alpha = bgAlpha))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))
                .background(
                    if (isSelected) accent.copy(alpha = 0.13f)
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                theme.icon, null,
                tint = if (isSelected) accent else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                theme.label, fontSize = 15.sp,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(theme.subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Box(
            modifier = Modifier.size(20.dp).clip(CircleShape).border(2.dp, radioColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(accent))
            }
        }
    }
}

fun applyAppTheme(
    viewModel: ThemeViewModel?,
    theme: AppTheme
) {
    viewModel?.setTheme(when(theme) {
        AppTheme.LIGHT -> ThemeOption.LIGHT
        AppTheme.DARK -> ThemeOption.DARK
        else -> ThemeOption.SYSTEM_DEFAULT
    })
}

@Preview(showSystemUi = true)
@Composable
fun PreviewThemes() {
    EkmAITheme(
        ThemeOption.DARK
    ) {
        ThemeScreen {

        }
    }
}