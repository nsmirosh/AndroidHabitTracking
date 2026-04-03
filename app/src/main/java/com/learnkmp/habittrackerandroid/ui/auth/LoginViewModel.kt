package com.learnkmp.habittrackerandroid.ui.auth

import android.app.Activity
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.learnkmp.habittrackerandroid.data.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

sealed interface LoginUiState {
    data object Idle : LoginUiState
    data object Loading : LoginUiState
    data object Success : LoginUiState
    data class Error(val message: String) : LoginUiState
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val repository: HabitRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val state: StateFlow<LoginUiState> = _state

    fun signIn(activity: Activity, webClientId: String) {
        viewModelScope.launch {
            _state.value = LoginUiState.Loading
            try {
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(webClientId)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val credentialManager = CredentialManager.create(activity)
                val result = credentialManager.getCredential(activity, request)

                val googleCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
                val firebaseCredential = GoogleAuthProvider.getCredential(googleCredential.idToken, null)

                val authResult = auth.signInWithCredential(firebaseCredential).await()
                authResult.user?.uid?.let { uid -> repository.syncFromFirestore(uid) }

                _state.value = LoginUiState.Success
            } catch (e: Exception) {
                _state.value = LoginUiState.Error(e.message ?: "Sign-in failed")
            }
        }
    }

    fun resetError() {
        _state.value = LoginUiState.Idle
    }
}
