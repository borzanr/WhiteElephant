import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

// Credenciais da chave de upload. Ficam em keystore.properties, que está no
// .gitignore — senha de assinatura nunca entra no controle de versão.
// Sem esse arquivo o build de release continua funcionando, só sai sem assinar.
val keystorePropsFile = rootProject.file("keystore.properties")
val keystoreProps = Properties().apply {
    if (keystorePropsFile.exists()) keystorePropsFile.inputStream().use { load(it) }
}

android {
    namespace = "com.whiteelephant.app"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.borzanti.whiteelephant"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    signingConfigs {
        if (keystorePropsFile.exists()) {
            create("release") {
                storeFile = rootProject.file(keystoreProps.getProperty("storeFile"))
                storePassword = keystoreProps.getProperty("storePassword")
                keyAlias = keystoreProps.getProperty("keyAlias")
                keyPassword = keystoreProps.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        debug {
            // Sufixo para o debug conviver com o app instalado pela Play em vez de
            // substituí-lo: no MIUI, desinstalar para reinstalar costuma travar.
            applicationIdSuffix = ".debug"
        }

        release {
            signingConfig = signingConfigs.findByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }

        // Igual ao release (R8 + shrink), mas instalável ao lado do app da Play e
        // assinado com a chave de debug. Serve para pegar no aparelho o que só o
        // R8 quebra — serialização, reflexão — sem ocupar o applicationId real.
        create("staging") {
            initWith(getByName("release"))
            applicationIdSuffix = ".staging"
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // O seletor de idioma do app (AppLanguage) troca o locale em tempo de execução.
    // Com o split por idioma ligado, a Play só instala os idiomas do aparelho, e
    // escolher outro dentro do app cairia de volta no idioma do sistema.
    bundle {
        language {
            enableSplit = false
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    debugImplementation(libs.androidx.ui.tooling)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.play.services.ads)
    implementation(libs.play.review)

    // O app não usa Fragment. A dependência existe só para sobrepor o
    // androidx.fragment:fragment 1.1.0 que o play-services-basement arrasta — é
    // a versão que a Play sinaliza como desatualizada no Console.
    implementation(libs.androidx.fragment)

    implementation(libs.konfetti.compose)
    implementation(libs.androidx.navigation.compose)

    testImplementation(libs.junit)
}
