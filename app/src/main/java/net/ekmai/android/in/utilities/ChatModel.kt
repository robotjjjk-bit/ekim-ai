package net.ekmai.android.`in`.utilities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val isTyping: Boolean = false,
    val error: String? = null
)

class ChatViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val chatList = LinkedList()

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        val userMessage = Message(text.trim(), isUser = true)
        chatList.addMessage(userMessage)

        _uiState.update { state ->
            state.copy(
                messages = state.messages + userMessage,
                isTyping = true,
                error = null
            )
        }

        viewModelScope.launch {
            val response = ApiManager.sendMessage(chatList)
            val replyText = response.text

            val isError = replyText.startsWith("Error") || replyText.startsWith("API Error")

            val aiMessage = Message(replyText, isUser = false)
            if (!isError) chatList.addMessage(aiMessage)

            _uiState.update { state ->
                state.copy(
                    messages = state.messages + aiMessage,
                    isTyping = false,
                    error = if (isError) replyText else null
                )
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}