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

import eu.europa.ec.euidi.AppBuildType
import eu.europa.ec.euidi.config.LibraryModule
import eu.europa.ec.euidi.getProperty
import java.io.FileInputStream
import java.util.Properties

plugins {
    id("eudi.android.application")
    id("eudi.android.application.compose")
}

// Create a variable called keystorePropertiesFile, and initialize it to your
// keystore.properties file, in the rootProject folder.
val keystorePropertiesFile = file("${rootProject.projectDir}/sign/keystore.properties")

// Initialize a new Properties() object called keystoreProperties.
val keystoreProperties = Properties()

// Load your keystore.properties file into the keystoreProperties object.
keystoreProperties.load(FileInputStream(keystorePropertiesFile))

android {

    signingConfigs {
        create("release") {

            storeFile = file("${rootProject.projectDir}/sign")

            keyAlias = getProperty("androidKeyAlias") ?: System.getenv("ANDROID_KEY_ALIAS")
            keyPassword = getProperty("androidKeyPassword") ?: System.getenv("ANDROID_KEY_PASSWORD")
            storePassword =
                getProperty("androidKeyPassword") ?: System.getenv("ANDROID_KEY_PASSWORD")

            enableV2Signing = true
        }

        getByName("debug") {
            storeFile =
                file("${rootProject.projectDir}/sign/debug/debugKeyStore.keystore")

            keyAlias = keystoreProperties["androidDebugKeyAlias"] as String
            keyPassword = keystoreProperties["androidDebugKeyPassword"] as String
            storePassword = keystoreProperties["androidDebugKeyPassword"] as String
            enableV1Signing = true
            enableV2Signing = true
        }
    }

    defaultConfig {
        applicationId = "de.authada.wallet.funke.eewa.optionc"
        versionCode = 1

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
            isMinifyEnabled = false
            applicationIdSuffix = AppBuildType.DEBUG.applicationIdSuffix
        }
        release {
            isDebuggable = false
            isMinifyEnabled = false
            applicationIdSuffix = AppBuildType.RELEASE.applicationIdSuffix
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }

    namespace = "eu.europa.ec.euidi"
}

dependencies {
    implementation(project(LibraryModule.AssemblyLogic.path))
    "baselineProfile"(project(LibraryModule.BaselineProfileLogic.path))
}
