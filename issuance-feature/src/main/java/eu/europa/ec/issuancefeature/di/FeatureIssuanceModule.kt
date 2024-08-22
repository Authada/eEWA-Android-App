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

package eu.europa.ec.issuancefeature.di


import eu.europa.ec.commonfeature.interactor.DeviceAuthenticationInteractor
import eu.europa.ec.corelogic.controller.WalletCoreDocumentsController
import eu.europa.ec.issuancefeature.interactor.SuccessInteractor
import eu.europa.ec.issuancefeature.interactor.SuccessInteractorImpl
import eu.europa.ec.issuancefeature.interactor.document.AddDocumentInteractor
import eu.europa.ec.issuancefeature.interactor.document.AddDocumentInteractorImpl
import eu.europa.ec.issuancefeature.interactor.document.DocumentDetailsInteractor
import eu.europa.ec.issuancefeature.interactor.document.DocumentDetailsInteractorImpl
import eu.europa.ec.issuancefeature.interactor.document.DocumentOfferInteractor
import eu.europa.ec.issuancefeature.interactor.document.DocumentOfferInteractorImpl
import eu.europa.ec.resourceslogic.provider.ResourceProvider
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module

@Module
@ComponentScan("eu.europa.ec.issuancefeature")
class FeatureIssuanceModule

@Factory
fun provideAddDocumentInteractor(
    walletCoreDocumentsController: WalletCoreDocumentsController,
    deviceAuthenticationInteractor: DeviceAuthenticationInteractor
): AddDocumentInteractor =
    AddDocumentInteractorImpl(
        walletCoreDocumentsController,
        deviceAuthenticationInteractor
    )

@Factory
fun provideDocumentDetailsInteractor(
    walletCoreDocumentsController: WalletCoreDocumentsController,
    resourceProvider: ResourceProvider
): DocumentDetailsInteractor =
    DocumentDetailsInteractorImpl(walletCoreDocumentsController, resourceProvider)

@Factory
fun provideSuccessInteractor(
    resourceProvider: ResourceProvider,
    walletCoreDocumentsController: WalletCoreDocumentsController
): SuccessInteractor = SuccessInteractorImpl(
    resourceProvider,
    walletCoreDocumentsController,
)

@Factory
fun provideDocumentOfferInteractor(
    walletCoreDocumentsController: WalletCoreDocumentsController,
    resourceProvider: ResourceProvider,
    deviceAuthenticationInteractor: DeviceAuthenticationInteractor
): DocumentOfferInteractor =
    DocumentOfferInteractorImpl(
        walletCoreDocumentsController,
        deviceAuthenticationInteractor,
        resourceProvider
    )