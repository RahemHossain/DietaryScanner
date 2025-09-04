plugins {
    alias(libs.plugins.android.application)
}
val nav_version = "2.7.7"


android {
    namespace = "com.example.dietaryscanner"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.dietaryscanner"
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
    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    // Updated lint configuration for Kotlin DSL
    lint {
        abortOnError = false
        disable += "Deprecation"
        checkReleaseBuilds = false
    }
}

// Handle Java compilation warnings for Kotlin DSL
tasks.withType<JavaCompile> {
    options.compilerArgs.addAll(listOf("-Xlint:-deprecation", "-Xlint:-unchecked"))
    options.isDeprecation = false
    options.isWarnings = false
}

dependencies {
    implementation("androidx.cardview:cardview:1.0.0")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // For image loading from URLs (Picasso)
    implementation("com.squareup.picasso:picasso:2.8")

    // For API calls (Retrofit)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("androidx.navigation:navigation-fragment:${nav_version}")
    implementation("androidx.navigation:navigation-ui:${nav_version}")
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // Camera X dependencies - REQUIRED for real camera
    implementation("androidx.camera:camera-camera2:1.3.1")
    implementation("androidx.camera:camera-lifecycle:1.3.1")
    implementation("androidx.camera:camera-view:1.3.1")

    implementation ("androidx.camera:camera-core:1.3.0")
    implementation ("androidx.camera:camera-camera2:1.3.0")
    implementation ("androidx.camera:camera-lifecycle:1.3.0")

    // ML Kit Barcode Scanning - REQUIRED for barcode detection
    implementation("com.google.mlkit:barcode-scanning:17.2.0")

    // Retrofit for API calls
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Glide for image loading (optional but recommended)
    implementation("com.github.bumptech.glide:glide:4.16.0")

    implementation ("com.github.bumptech.glide:glide:4.14.2")
    annotationProcessor("com.github.bumptech.glide:compiler:4.14.2")
    implementation("com.github.bumptech.glide:glide:4.14.2")
    annotationProcessor("com.github.bumptech.glide:compiler:4.14.2")


    // Test dependencies
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // For image loading from URLs (Picasso)
    implementation("com.squareup.picasso:picasso:2.8")

    // For API calls (Retrofit)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("androidx.navigation:navigation-fragment:${nav_version}")
    implementation("androidx.navigation:navigation-ui:${nav_version}")

    // Glide for image loading - CLEAN VERSION
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // Camera X dependencies - CLEAN VERSION
    implementation("androidx.camera:camera-core:1.3.1")
    implementation("androidx.camera:camera-camera2:1.3.1")
    implementation("androidx.camera:camera-lifecycle:1.3.1")
    implementation("androidx.camera:camera-view:1.3.1")

    implementation("androidx.camera:camera-view:1.3.0")

    // ML Kit Barcode Scanning - REQUIRED for barcode detection
    implementation("com.google.mlkit:barcode-scanning:17.2.0")

    // ML Kit Text Recognition - REQUIRED for OCR
    implementation("com.google.mlkit:text-recognition:16.0.0")

    // Test dependencies
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}