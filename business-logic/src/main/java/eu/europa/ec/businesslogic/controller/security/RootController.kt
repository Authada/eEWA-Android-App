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

package eu.europa.ec.businesslogic.controller.security

import com.scottyab.rootbeer.RootBeer
import eu.europa.ec.resourceslogic.provider.ResourceProvider

interface RootController {
    fun isRooted(): Boolean
    fun areRootCloakingAppsInstalled(): Boolean
}

class RootControllerImpl(
    resourceProvider: ResourceProvider
) : RootController {

    private val rootBeer = RootBeer(resourceProvider.provideContext())

    override fun isRooted() = rootBeer.isRooted

    override fun areRootCloakingAppsInstalled() = rootBeer.detectRootCloakingApps(
        arrayOf(
            "com.android.SSLTrustKiller"
        )
    )
}