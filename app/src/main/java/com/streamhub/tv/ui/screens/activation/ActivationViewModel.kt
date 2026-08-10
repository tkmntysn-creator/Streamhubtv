package com.streamhub.tv.ui.screens.activation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamhub.tv.data.repository.ActivationRepository
import com.streamhub.tv.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ActivationUiState(
    val code: String = "",
    val isChecking: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ActivationViewModel @Inject constructor(
    private val activationRepository: ActivationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActivationUiState())
    val uiState: StateFlow<ActivationUiState> = _uiState

    fun onCodeChange(code: String) {
        _uiState.value = _uiState.value.copy(code = code, errorMessage = null)
    }

    fun submit(onSuccess: () -> Unit) {
        val code = _uiState.value.code.trim()
        if (code.isEmpty()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Enter the activation code")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isChecking = true, errorMessage = null)
            when (val result = activationRepository.verifyCode(code)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(isChecking = false)
                    if (result.data) {
                        onSuccess()
                    } else {
                        _uiState.value = _uiState.value.copy(errorMessage = "Incorrect code, try again")
                    }
                }
                is Resource.Error -> _uiState.value = _uiState.value.copy(
                    isChecking = false,
                    errorMessage = result.message
                )
                Resource.Loading -> Unit
            }
        }
    }
}
