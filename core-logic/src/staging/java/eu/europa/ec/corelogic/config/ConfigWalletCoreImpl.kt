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

package eu.europa.ec.corelogic.config

import android.content.Context
import android.os.Build
import eu.europa.ec.businesslogic.config.ConfigLogic
import eu.europa.ec.corelogic.BuildConfig
import eu.europa.ec.corelogic.R
import eu.europa.ec.corelogic.controller.WalletCoreLogController
import eu.europa.ec.corelogic.model.DocumentIdentifier
import eu.europa.ec.eudi.wallet.EudiWalletConfig
import eu.europa.ec.eudi.wallet.issue.openid4vci.OpenId4VciManager
import eu.europa.ec.eudi.wallet.transfer.openid4vp.ClientIdScheme
import eu.europa.ec.eudi.wallet.transfer.openid4vp.EncryptionAlgorithm
import eu.europa.ec.eudi.wallet.transfer.openid4vp.EncryptionMethod
import eu.europa.ec.eudi.wallet.transfer.openid4vp.PreregisteredVerifier
import java.security.KeyStore
import java.security.cert.X509Certificate

internal class WalletCoreConfigImpl(
    private val context: Context,
    private val walletCoreLogController: WalletCoreLogController,
    private val configLogic: ConfigLogic
) : WalletCoreConfig {

    private companion object {
        const val OPENID4VP_VERIFIER_API_URI = "https://id.staging.authada.de/eudi/verifier"
        const val OPENID4VP_VERIFIER_LEGAL_NAME = "EUDI Remote Verifier"
        const val OPENID4VP_VERIFIER_CLIENT_ID = "Verifier"

        const val VCI_ISSUER_URL = "https://id.staging.authada.de/eudi/issuer/pid"
        const val ISSUER_WEBSITE = "https://id.staging.authada.de/eudi/issuer/pid/"

        const val WALLET_CLIENT_ID = "eEWA Wallet"

        const val WALLET_PROVIDER_URL = "https://id.staging.authada.de/eudi/wallet"
        const val AUTHENTICATION_REQUIRED = false
    }

    private var _config: EudiWalletConfig? = null

    override val config: EudiWalletConfig
        get() {
            if (_config == null) {
                _config = EudiWalletConfig.Builder(context)
                    .logger(walletCoreLogController)
                    .userAuthenticationRequired(AUTHENTICATION_REQUIRED)
                    .trustedReaderCertificates(KeyStore.getInstance("PKCS12").run {
                        val canKeyStoreBeProperlyLoadedByAndroidNative =
                        (Build.VERSION.SDK_INT >= 33)
                        val inputStream = if (canKeyStoreBeProperlyLoadedByAndroidNative) {
                            context.resources.openRawResource(R.raw.trustlist)
                        } else {
                            this::class.java.classLoader?.getResourceAsStream("trustlist.p12")
                        }
                        load(inputStream, "password".toCharArray())
                        aliases().toList().map {
                            getCertificate(it) as X509Certificate
                        }
                    })
                    .openId4VpConfig {
                        withEncryptionAlgorithms(listOf(EncryptionAlgorithm.ECDH_ES))
                        withEncryptionMethods(
                            listOf(
                                EncryptionMethod.A128CBC_HS256,
                                EncryptionMethod.A256GCM
                            )
                        )

                        withClientIdSchemes(
                            listOf(
                                ClientIdScheme.X509SanDns,
                                ClientIdScheme.Preregistered(
                                    listOf(
                                        PreregisteredVerifier(
                                            clientId = OPENID4VP_VERIFIER_CLIENT_ID,
                                            verifierApi = OPENID4VP_VERIFIER_API_URI,
                                            legalName = OPENID4VP_VERIFIER_LEGAL_NAME
                                        )
                                    )
                                ),
                                ClientIdScheme.VerifierAttestation
                            )
                        )
                        withScheme(BuildConfig.OPENID4VP_SCHEME)
                    }
                    .openId4VciConfig {
                        clientId(WALLET_CLIENT_ID)
                        issuerMap(
                            mapOf(
                                DocumentIdentifier.PID_SDJWT.docType to OpenId4VciManager.Config.Issuer(
                                    VCI_ISSUER_URL
                                )
                            ).withDefault { OpenId4VciManager.Config.Issuer(VCI_ISSUER_URL) }
                        )
                        authFlowRedirectionURI(BuildConfig.ISSUE_AUTHORIZATION_DEEPLINK)
                        useStrongBoxIfSupported(true)
                        useDPoP(true)
                        useClientAttestation(WALLET_PROVIDER_URL)
                    }
                    .issuerWebsiteForBrowser(ISSUER_WEBSITE)
                    .build()
            }
            return _config!!
        }
}