package org.thelazybattley.macrotrack.features.profile.personalinformation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class PersonalInformationViewModel : ViewModel(), PersonalInformationCallbacks {

    private val _state = MutableStateFlow(value = PersonalInformationViewState())

    val state = _state.asStateFlow()
}
