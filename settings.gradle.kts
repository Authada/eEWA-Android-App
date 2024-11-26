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

if (file("../eudi-lib-android-wallet-core").exists()) {
    includeBuild("../eudi-lib-android-wallet-core") {
        dependencySubstitution {
            substitute(module("eu.europa.ec.eudi:eudi-lib-android-wallet-core")).using(project(":wallet-core"))
        }
    }
}

if (file("../eewa-pid-lib").exists()) {
    includeBuild("../eewa-pid-lib") {
        dependencySubstitution {
            substitute(module("de.authada.eewa:eewa-pid-lib")).using(project(":eewa-pid"))
        }
    }
}

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        mavenLocal()
        google()
        mavenCentral()
        maven {
            url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            mavenContent { snapshotsOnly() }
        }
        maven {
            url = uri("https://repo.danubetech.com/repository/maven-releases/")
        }
    }
}

rootProject.name = "eEWA"
include(":app")
include(":business-logic")
include(":ui-logic")
include(":network-logic")
include(":resources-logic")
include(":assembly-logic")
include(":startup-feature")
include(":test-logic")
include(":test-feature")
include(":login-feature")
include(":common-feature")
include(":dashboard-feature")
include(":presentation-feature")
include(":proximity-feature")
include(":issuance-feature")
include(":analytics-logic")
include(":baseline-profile")
include(":authentication-logic")
include(":core-logic")
