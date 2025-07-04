plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.coffeebear"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.coffeebear"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        // viewBinding true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    implementation(platform(libs.firebase.bom))

    implementation(libs.google.firebase.database)
    implementation(libs.google.firebase.auth)
    implementation(libs.google.firebase.firestore)

    implementation(libs.play.services.auth)

    implementation(libs.circleimageview)
    implementation(libs.glide)
    annotationProcessor(libs.compiler)
    implementation("com.github.IsmaelDivita:chip-navigation-bar:1.4.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}