/*
 * Copyright (c) 2024 AUTHADA GmbH
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work
 * except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF
 * ANY KIND, either express or implied. See the Licence for the specific language
 * governing permissions and limitations under the Licence.
 */

package eu.europa.ec.startupfeature.onboarding

import eu.europa.ec.commonfeature.model.PinFlow
import eu.europa.ec.resourceslogic.R
import eu.europa.ec.uilogic.component.AppIcons
import eu.europa.ec.uilogic.component.content.ScreenNavigateAction
import eu.europa.ec.uilogic.component.utils.NOT_IMPLEMENTED_MESSAGE
import eu.europa.ec.uilogic.mvi.MviViewModel
import eu.europa.ec.uilogic.mvi.ViewEvent
import eu.europa.ec.uilogic.mvi.ViewSideEffect
import eu.europa.ec.uilogic.mvi.ViewState
import eu.europa.ec.uilogic.navigation.CommonScreens
import eu.europa.ec.uilogic.navigation.helper.generateComposableArguments
import eu.europa.ec.uilogic.navigation.helper.generateComposableNavigationLink


data class State(
    val navigatableAction: ScreenNavigateAction,
    val onBackAction: (() -> Unit)? = null,
    val pages: List<OnboardingPage>
) : ViewState

sealed class Event : ViewEvent {
    data class OnPrimaryButtonPressed(val pageIndex: Int) : Event()
    data object OnSecondaryButtonPressed : Event()
    data object Finish : Event()
}

sealed class Effect : ViewSideEffect {
    data class ShowToast(val message: String) : Effect()
    data object SwipeToNextPage : Effect()
    sealed class Navigation : Effect() {
        data object Finish : Navigation()
        data class SwitchScreen(val screenRoute: String) : Navigation()
    }
}


class OnboardingViewModel : MviViewModel<Event, State, Effect>() {

    override fun setInitialState(): State {
        return State(
            navigatableAction = ScreenNavigateAction.CANCELABLE,
            onBackAction = {
            },
            pages = pages
        )
    }

    override fun handleEvents(event: Event) {
        when (event) {
            is Event.OnPrimaryButtonPressed -> {
                if (event.pageIndex == 0) {
                    setEffect {
                        Effect.SwipeToNextPage
                    }
                } else {
                    navigateToPinSetup()
                }
            }

            Event.OnSecondaryButtonPressed -> {
                goToAppInformation()
            }

            Event.Finish -> {
                setEffect {
                    Effect.Navigation.Finish
                }
            }
        }
    }

    private fun goToAppInformation() {
        setEffect {
            Effect.ShowToast(NOT_IMPLEMENTED_MESSAGE)
        }
    }

    private fun navigateToPinSetup() {
        setEffect {
            Effect.Navigation.SwitchScreen(
                generateComposableNavigationLink(
                    screen = CommonScreens.QuickPin,
                    arguments = generateComposableArguments(mapOf("pinFlow" to PinFlow.CREATE))
                )
            )
        }
    }

    private companion object {
        val pages = listOf(
            OnboardingPage(
                pageIndex = 0,
                titleRes = R.string.onboarding_title_0,
                subtitleRes = R.string.onboarding_subtitle_0,
                icon = AppIcons.StoreId,
                primaryButtonTextRes = R.string.onboarding_primary_button_0,
                secondaryButtonTextRes = R.string.onboarding_secondary_button_0
            ),
            OnboardingPage(
                pageIndex = 1,
                titleRes = R.string.onboarding_title_1,
                subtitleRes = R.string.onboarding_subtitle_1,
                icon = AppIcons.Lock,
                primaryButtonTextRes = R.string.onboarding_primary_button_1,
                secondaryButtonTextRes = null
            )
        )
    }
}