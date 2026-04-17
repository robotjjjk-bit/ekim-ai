package net.ekmai.android.`in`.activities

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import net.ekmai.android.`in`.components.AiModel
import net.ekmai.android.`in`.components.ChatMessageList
import net.ekmai.android.`in`.components.MessageField
import net.ekmai.android.`in`.components.ModelSelectorDropdown
import net.ekmai.android.`in`.ui.theme.EkmAITheme
import net.ekmai.android.`in`.utilities.ModelsManager
import net.ekmai.android.`in`.utilities.NetworkObserver
import net.ekmai.android.`in`.utilities.ChatViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            EkmAITheme {
                MainScreen()
            }
        }
    }
}

@Preview(showSystemUi = true)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    chatViewModel: ChatViewModel = viewModel()
) {
    val context: Context = LocalContext.current
    val uiState by chatViewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    var selectedModel by remember { mutableStateOf(ModelsManager.getCurrentModel()) }
    var showSelector by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }

    val networkObserver = remember { NetworkObserver(context) }
    val isNetworkAvailable by networkObserver.observe()
        .collectAsState(initial = networkObserver.isConnected())

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            chatViewModel.clearError()
        }
    }

    LaunchedEffect(Unit) {
        ModelsManager.loadSavedModel(context)
        selectedModel = ModelsManager.getCurrentModel()
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "ekm",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = TextStyle(
                                fontSize = 22.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = (-0.5).sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .then(
                                    Modifier.background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(
                                                selectedModel.tintColor,
                                                selectedModel.tintColor
                                            )
                                        )
                                    )
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "AI",
                                style = TextStyle(
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    letterSpacing = 0.5.sp
                                )
                            )
                        }
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Outlined.MoreVert,
                                contentDescription = "More options"
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Settings,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text("Settings")
                                    }
                                },
                                onClick = { menuExpanded = false }
                            )
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Info,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text("About")
                                    }
                                },
                                onClick = { menuExpanded = false }
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .navigationBarsPadding()
                    .padding(bottom = 8.dp, start = 6.dp, end = 6.dp)
            ) {
                AnimatedVisibility(
                    visible = showSelector,
                    enter = slideInVertically(initialOffsetY = { -it / 4 }) + fadeIn(tween(200)),
                    exit = slideOutVertically(targetOffsetY = { -it / 4 }) + fadeOut(tween(150)),
                ) {
                    ModelSelectorDropdown(
                        models = ModelsManager.getAllModels(),
                        selectedModel = selectedModel,
                        onModelSelected = {
                            selectedModel = it
                            showSelector = false
                            scope.launch {
                                ModelsManager.setModel(context, it.name)
                            }
                        },
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
                MessageField(
                    selectedModel = AiModel(
                        name = selectedModel.name,
                        shortLabel = selectedModel.initials,
                        tintColor = selectedModel.tintColor
                    ),
                    onModelClick = { showSelector = !showSelector },
                    onSend = { txt ->
                        if (!uiState.isTyping) {
                            chatViewModel.sendMessage(txt)
                        }
                    },
                    onVoice = {},
                    isError = !isNetworkAvailable,
                    errorMessage = if (!isNetworkAvailable)
                        "Network not available"
                    else
                        "Something went wrong. Please try again."
                )
            }
        }
    ) { innerPadding ->
        ChatMessageList(
            messages = uiState.messages,
            isTyping = uiState.isTyping,
            contentPadding = innerPadding,
            modifier = Modifier.fillMaxSize()
        )
    }
}