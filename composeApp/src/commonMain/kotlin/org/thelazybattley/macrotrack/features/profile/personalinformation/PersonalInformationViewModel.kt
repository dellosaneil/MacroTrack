package org.thelazybattley.macrotrack.features.profile.personalinformation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.thelazybattley.macrotrack.domain.usecase.userdetails.GetUserDetailsUseCase

class PersonalInformationViewModel(
    private val getUserDetailsUseCase: GetUserDetailsUseCase,
) : ViewModel(), PersonalInformationCallbacks {

    private val _state = MutableStateFlow(value = PersonalInformationViewState())

    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            getUserDetailsUseCase().let { userDetails ->
                _state.update {currentState ->
                    currentState.copy(
                        userDetails = userDetails
                    )
                }
            }
        }
    }
}
