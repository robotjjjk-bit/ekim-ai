package net.ekmai.android.`in`.activities

import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Upload
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import net.ekmai.android.`in`.components.ChatMessageList
import net.ekmai.android.`in`.components.MessageField
import net.ekmai.android.`in`.components.ModelSelectorDropdown
import net.ekmai.android.`in`.components.VoiceBottomSheet
import net.ekmai.android.`in`.ui.theme.EkmAITheme
import net.ekmai.android.`in`.ui.theme.ThemeManager
import net.ekmai.android.`in`.utilities.ApiManager
import net.ekmai.android.`in`.utilities.ModelsManager
import net.ekmai.android.`in`.utilities.NetworkObserver
import net.ekmai.android.`in`.utilities.ChatViewModel
import net.ekmai.android.`in`.utilities.ImportExport
import net.ekmai.android.`in`.utilities.LinkedList
import net.ekmai.android.`in`.utilities.ModelInfo
import net.ekmai.android.`in`.utilities.PromptPreference
import net.ekmai.android.`in`.utilities.VoiceManager
import net.ekmai.android.`in`.utilities.VoiceState
import net.ekmai.android.`in`.utilities.rememberMicPermission

class MainActivity : ComponentActivity() {
    private lateinit var voiceManager: VoiceManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        voiceManager = VoiceManager(this)
        enableEdgeToEdge()
        val preference = PromptPreference(applicationContext)
        val data = preference.getData("prompt")
        ApiManager.systemPrompt = data ?: ApiManager.systemPrompt
        WindowCompat.setDecorFitsSystemWindows(window, false)
        ThemeManager.themeState.value = net.ekmai.android.`in`.ui.theme.getTheme(this)
        setContent {
            val theme by ThemeManager.themeState.collectAsState()
            EkmAITheme(
                themeOption = theme
            ) {
                MainScreen(voiceManager = voiceManager)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    chatViewModel: ChatViewModel = viewModel(),
    voiceManager: VoiceManager
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val uiState by chatViewModel.uiState.collectAsState()
    val voiceState by voiceManager.state.collectAsState()
    val rmsLevel by voiceManager.rmsLevel.collectAsState()
    var showVoiceSheet by remember { mutableStateOf(false) }
    val currentModel by remember { mutableStateOf(ModelsManager.getCurrentModel()) }
    val accentColor by remember { mutableStateOf(currentModel.tintColor) }
    val requestMic = rememberMicPermission(
        onGranted = {
            showVoiceSheet = true
            voiceManager.startListening()
        },
        onDenied = {
            scope.launch { snackbarHostState.showSnackbar("Microphone permission required") }
        }
    )
    LaunchedEffect(voiceState) {
        if (voiceState is VoiceState.Error) {
            // keep sheet open so user sees error + retry
        }
    }

    if (showVoiceSheet) {
        VoiceBottomSheet(
            state = voiceState,
            rmsLevel = rmsLevel,
            accentColor = accentColor,
            onDismiss = {
                voiceManager.reset()
                showVoiceSheet = false
            },
            onStop = { voiceManager.stopListening() },
            onSend = { text ->
                voiceManager.reset()
                showVoiceSheet = false
                if (!uiState.isTyping) chatViewModel.sendMessage(text)
            },
            onRetry = {
                voiceManager.reset()
                voiceManager.startListening()
            }
        )
    }

    val activity = LocalActivity.current as Activity
    val context: Context = LocalContext.current
    var selectedModel by remember { mutableStateOf(ModelsManager.getCurrentModel()) }
    var showSelector by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }

    val networkObserver = remember { NetworkObserver(context) }
    val isNetworkAvailable by networkObserver.observe()
        .collectAsState(initial = networkObserver.isConnected())

    val isSaving by getSaving(context).collectAsState(initial = false)

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            val messages = ImportExport.import(context, it)
            val newList = LinkedList()
            messages?.forEach { message ->
                newList.addMessage(message)
            }
            chatViewModel.refresh(newList = newList, messages)

            if (messages != null) {
                scope.launch {
                    snackbarHostState.showSnackbar("Imported ${messages.size} messages")
                }
            } else {
                scope.launch {
                    snackbarHostState.showSnackbar("Import failed")
                }
            }
        }
    }

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
                                onClick = {
                                    menuExpanded = false
                                    activity.launchSettings()
                                }
                            )
                            if (isSaving) {
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Upload,
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text("Export")
                                        }
                                    },
                                    onClick = {
                                        ImportExport.export(context, chatList = uiState.messages)
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Download,
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text("Import")
                                        }
                                    },
                                    onClick = {
                                        importLauncher.launch(arrayOf("application/json"))
                                    }
                                )
                            }
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
                                onClick = {
                                    menuExpanded = false
                                    activity.launchAbout()
                                }
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
                    selectedModel = ModelInfo(selectedModel.id, selectedModel.provider, selectedModel.contextWindow, selectedModel.initials, selectedModel.tintColor, selectedModel.name, selectedModel.apiKey, selectedModel.baseUrl, selectedModel.type),
                    onModelClick = { showSelector = !showSelector },
                    onSend = { txt ->
                        if (!uiState.isTyping) {
                            chatViewModel.sendMessage(txt)
                        }
                    },
                    onVoice = {
                        if (voiceManager.isAvailable()) requestMic()
                        else scope.launch { snackbarHostState.showSnackbar("Voice not available on this device") }
                    },
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