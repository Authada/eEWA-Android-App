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

package eu.europa.ec.corelogic.util

import eu.europa.ec.eudi.iso18013.transfer.RequestedDocumentData
import eu.europa.ec.eudi.iso18013.transfer.TransferEvent
import java.net.URI

class EudiWalletListenerWrapper(
    private val onConnected: () -> Unit,
    private val onConnecting: () -> Unit,
    private val onDisconnected: () -> Unit,
    private val onError: (String) -> Unit,
    private val onQrEngagementReady: (String) -> Unit,
    private val onRequestReceived: (RequestedDocumentData) -> Unit,
    private val onResponseSent: () -> Unit,
    private val onRedirect: (URI) -> Unit,
) : TransferEvent.Listener {
    override fun onTransferEvent(event: TransferEvent) {
        when (event) {
            is TransferEvent.Connected -> onConnected()
            is TransferEvent.Connecting -> onConnecting()
            is TransferEvent.Disconnected -> onDisconnected()
            is TransferEvent.Error -> {
                onError(event.error.message ?: event.error.toString())
            }
            is TransferEvent.QrEngagementReady -> onQrEngagementReady(event.qrCode.content)
            is TransferEvent.RequestReceived -> onRequestReceived(event.requestedDocumentData)
            is TransferEvent.ResponseSent -> onResponseSent()
            is TransferEvent.Redirect -> onRedirect(event.redirectUri)
        }
    }
}