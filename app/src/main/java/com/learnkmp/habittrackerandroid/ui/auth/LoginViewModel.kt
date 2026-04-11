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
import android.util.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class LoginState(
    val isLoading: Boolean = false,
    val error: String? = null,
)

sealed interface LoginIntent {
    data class SignIn(val activity: Activity, val webClientId: String) : LoginIntent
    data object DismissError : LoginIntent
}

sealed interface LoginEffect {
    data object NavigateToHabitList : LoginEffect
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val repository: HabitRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state

    private val _effect = Channel<LoginEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.SignIn -> signIn(intent.activity, intent.webClientId)
            is LoginIntent.DismissError -> _state.value = _state.value.copy(error = null)
        }
    }

    private fun signIn(activity: Activity, webClientId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                Log.d("LoginViewModel", "Starting sign-in with webClientId=$webClientId")
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(webClientId)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val credentialManager = CredentialManager.create(activity)
                Log.d("LoginViewModel", "Calling getCredential...")
                val result = credentialManager.getCredential(activity, request)
                Log.d("LoginViewModel", "getCredential returned: ${result.credential.type}")

                val googleCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
                val firebaseCredential = GoogleAuthProvider.getCredential(googleCredential.idToken, null)

                val authResult = auth.signInWithCredential(firebaseCredential).await()
                authResult.user?.uid?.let { uid -> repository.syncFromFirestore(uid) }

                Log.d("LoginViewModel", "returning Success")
                _state.value = _state.value.copy(isLoading = false)
                _effect.send(LoginEffect.NavigateToHabitList)
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Sign-in failed", e)
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Sign-in failed",
                )
            }
        }
    }
}
