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

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
}

group = "eu.europa.ec.euidi.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
    compileOnly(libs.secrets.gradlePlugin)
    compileOnly(libs.owasp.dependencycheck.gradlePlugin)
    compileOnly(libs.kotlinx.kover.gradlePlugin)
    compileOnly(libs.baselineprofile.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplicationCompose") {
            id = "eudi.android.application.compose"
            implementationClass = "AndroidApplicationComposeConventionPlugin"
        }
        register("androidApplication") {
            id = "eudi.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidLibraryCompose") {
            id = "eudi.android.library.compose"
            implementationClass = "AndroidLibraryComposeConventionPlugin"
        }
        register("androidLibrary") {
            id = "eudi.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidFeature") {
            id = "eudi.android.feature"
            implementationClass = "AndroidFeatureConventionPlugin"
        }
        register("androidLibraryKover") {
            id = "eudi.android.library.kover"
            implementationClass = "AndroidLibraryKoverConventionPlugin"
        }
        register("androidTest") {
            id = "eudi.android.test"
            implementationClass = "AndroidTestConventionPlugin"
        }
        register("androidFeatureTest") {
            id = "eudi.android.feature.test"
            implementationClass = "AndroidFeatureTestConventionPlugin"
        }
        register("androidKoin") {
            id = "eudi.android.koin"
            implementationClass = "AndroidKoinConventionPlugin"
        }
        register("androidFlavors") {
            id = "eudi.android.application.flavors"
            implementationClass = "AndroidApplicationFlavorsConventionPlugin"
        }
        register("androidLint") {
            id = "eudi.android.lint"
            implementationClass = "AndroidLintConventionPlugin"
        }
        register("jvmLibrary") {
            id = "eudi.jvm.library"
            implementationClass = "JvmLibraryConventionPlugin"
        }
        register("eudiWalletCore") {
            id = "eudi.wallet.core"
            implementationClass = "EudiWalletCorePlugin"
        }
        register("eewaPidLib") {
            id = "eewa.pid.lib"
            implementationClass = "EewaPidLibPlugin"
        }
        register("owaspDependencyCheck") {
            id = "eudi.owasp.dependency.check"
            implementationClass = "OwaspDependencyCheckPlugin"
        }
        register("appCenter") {
            id = "eudi.appcenter"
            implementationClass = "AppCenterPlugin"
        }
        register("sonar") {
            id = "eudi.sonar"
            implementationClass = "SonarPlugin"
        }
        register("androidBaseProfile") {
            id = "eudi.android.base.profile"
            implementationClass = "BaseLineProfilePlugin"
        }
    }
}
