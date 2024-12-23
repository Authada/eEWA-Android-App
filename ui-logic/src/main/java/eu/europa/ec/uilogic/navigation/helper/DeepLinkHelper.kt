/*
 * Copyright (c) 2023 European Commission
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
 *
 * Modified by AUTHADA GmbH August 2024
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

package eu.europa.ec.uilogic.navigation.helper

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import eu.europa.ec.corelogic.util.CoreActions
import eu.europa.ec.uilogic.BuildConfig
import eu.europa.ec.uilogic.container.EudiComponentActivity
import eu.europa.ec.uilogic.extension.openUrl
import eu.europa.ec.uilogic.navigation.IssuanceScreens
import eu.europa.ec.uilogic.navigation.PresentationScreens
import eu.europa.ec.uilogic.navigation.Screen

fun <T> generateComposableArguments(arguments: Map<String, T>): String {
    if (arguments.isEmpty()) return ""
    return StringBuilder().apply {
        append("?")
        arguments.onEachIndexed { index, entry ->
            if (index > 0) {
                append("&")
            }
            append("${entry.key}=${entry.value}")
        }
    }.toString()
}

fun generateComposableDeepLinkUri(screen: Screen, arguments: String): Uri =
    generateComposableDeepLinkUri(screen.screenName, arguments)

fun generateComposableDeepLinkUri(screen: String, arguments: String): Uri =
    "${BuildConfig.DEEPLINK}/${screen}$arguments".toUri()

fun generateComposableNavigationLink(screen: Screen, arguments: String): String =
    generateComposableNavigationLink(screen.screenName, arguments)

fun generateComposableNavigationLink(screen: String, arguments: String): String =
    "${screen}$arguments"

fun generateNewTaskDeepLink(
    context: Context,
    screen: Screen,
    arguments: String = "",
    flags: Int = 0
): Intent =
    generateNewTaskDeepLink(context, screen.screenName, arguments, flags)

fun generateNewTaskDeepLink(
    context: Context,
    screen: String,
    arguments: String = "",
    flags: Int = 0
): Intent =
    Intent(
        Intent.ACTION_VIEW,
        generateComposableDeepLinkUri(screen, arguments),
        context,
        EudiComponentActivity::class.java
    ).apply {
        addFlags(flags)
    }

fun hasDeepLink(deepLinkUri: Uri?): DeepLinkAction? {
    return deepLinkUri?.let { uri ->
        DeepLinkAction(link = uri, type = DeepLinkType.parse(uri))
    }
}

fun handleDeepLinkAction(
    navController: NavController,
    uri: Uri,
    arguments: String? = null
) {
    hasDeepLink(uri)?.let { action ->

        val screen: Screen

        when (action.type) {
            DeepLinkType.OPENID4VP -> {
                screen = PresentationScreens.PresentationRequest
            }

            DeepLinkType.OPENID4VCI -> {
                screen = IssuanceScreens.DocumentOffer
            }

            DeepLinkType.ISSUANCE -> {
                notifyOnResumeIssuance(navController.context, authorizationLink = action.link)
                return@let
            }

            DeepLinkType.EXTERNAL -> {
                navController.context.openUrl(action.link)
                return@let
            }
        }

        val navigationLink = arguments?.let {
            generateComposableNavigationLink(
                screen = screen,
                arguments = arguments
            )
        } ?: screen.screenRoute

        navController.navigate(navigationLink) {
            popUpTo(screen.screenRoute) { inclusive = true }
        }
    }
}

data class DeepLinkAction(val link: Uri, val type: DeepLinkType)
enum class DeepLinkType(val host: String? = null) {

    OPENID4VP,
    OPENID4VCI,
    ISSUANCE("authorization"),
    EXTERNAL;

    companion object {
        fun parse(uri: Uri): DeepLinkType = when {

            uri.scheme?.contains(OPENID4VP.name.lowercase()) == true -> {
                OPENID4VP
            }

            uri.scheme?.contains(BuildConfig.OPENID4VP_SCHEME, true) == true -> {
                OPENID4VP
            }

            uri.scheme?.contains(OPENID4VCI.name, true) == true -> {
                OPENID4VCI
            }

            uri.scheme?.contains(BuildConfig.OPENID4VCI_SCHEME) == true -> {
                OPENID4VCI
            }

            uri.scheme?.contains(ISSUANCE.name.lowercase()) == true && uri.host == ISSUANCE.host -> {
                ISSUANCE
            }

            uri.toString().startsWith(BuildConfig.ISSUE_AUTHORIZATION_DEEPLINK, true) -> {
                ISSUANCE
            }

            else -> EXTERNAL
        }
    }
}

private fun notifyOnResumeIssuance(context: Context, authorizationLink: Uri?) {
    Intent().also { intent ->
        intent.action = CoreActions.VCI_RESUME_ACTION
        authorizationLink?.let {
            intent.putExtras(bundleOf(Pair("uri", authorizationLink.toString())))
        }
        context.sendBroadcast(intent)
    }
}