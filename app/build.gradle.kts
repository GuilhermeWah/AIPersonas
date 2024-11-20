plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services") // Plugin for Google Services
}

android {
    namespace = "com.example.aipersonas"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.aipersonas"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}


dependencies {
    // AndroidX dependencies
    implementation("androidx.appcompat:appcompat:1.5.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.activity:activity-ktx:1.5.1")

    // Material Components
    implementation("com.google.android.material:material:1.6.1")

    // Firebase BOM (to manage versions of Firebase libraries consistently)
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-analytics")


    // Google Play Services - Recaptcha
    implementation("com.google.android.gms:play-services-recaptcha:17.1.0")
    implementation("com.google.android.gms:play-services-oss-licenses:17.0.0")
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    // Room Database
    implementation("androidx.room:room-runtime:2.4.3")
    implementation("androidx.room:room-ktx:2.4.3")
    annotationProcessor("androidx.room:room-compiler:2.4.3") // For Java-based projects

    //Icons
    implementation ("com.google.android.material:material:1.9.0")

    //Testing Room db Stetho

 
    // Test Dependencies
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.4")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.0")
}

// Apply the Google services JSON parsing and services linking
apply(plugin = "com.google.gms.google-services")
