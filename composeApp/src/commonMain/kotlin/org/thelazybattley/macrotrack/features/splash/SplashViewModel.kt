package org.thelazybattley.macrotrack.features.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.thelazybattley.macrotrack.domain.usecase.userdetails.GetUserDetailsUseCase
import org.thelazybattley.macrotrack.ui.navigation.MacroTrackDestination

class SplashViewModel(private val getUserDetailsUseCase: GetUserDetailsUseCase) : ViewModel() {

    private val _state = MutableStateFlow(value = SplashViewState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            getUserDetailsUseCase().also { user ->
                val destination = if (user == null) {
                    MacroTrackDestination.ONBOARDING
                } else {
                    MacroTrackDestination.HOME
                }
                _state.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        destination = destination
                    )
                }
            }
        }
    }
}
