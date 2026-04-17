package org.thelazybattley.macrotrack.features.profile.personalinformation.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.personal_information
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.features.profile.personalinformation.PersonalInformationCallbacks
import org.thelazybattley.macrotrack.features.profile.personalinformation.PersonalInformationViewModel
import org.thelazybattley.macrotrack.features.profile.personalinformation.PersonalInformationViewState
import org.thelazybattley.macrotrack.ui.common.CommonTopBar
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors

@Composable
fun PersonalInformationScreen(
    modifier: Modifier = Modifier,
    onPopBackStack: () -> Unit
) {
    val viewModel = koinViewModel<PersonalInformationViewModel>()
    val viewState by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.white,
        topBar = {
            CommonTopBar(
                stringResource = Res.string.personal_information,
                onClick = {
                    onPopBackStack()
                }
            )
        }
    ) { innerPadding ->
        PersonalInformationScreen(
            modifier = Modifier.padding(paddingValues = innerPadding),
            viewState = viewState,
            callbacks = viewModel
        )
    }
}

@Composable
private fun PersonalInformationScreen(
    modifier: Modifier = Modifier,
    viewState: PersonalInformationViewState,
    callbacks: PersonalInformationCallbacks
) {

}

@Preview(showBackground = true)
@Composable
private fun PreviewPersonalInformationScreen() {
    MacroTrackTheme {
        PersonalInformationScreen(
            modifier = Modifier,
            viewState = PersonalInformationViewState(),
            callbacks = PersonalInformationCallbacks.default()
        )
    }
}
