plugins {
    alias(libs.plugins.android.application)
}

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

    // Test dependencies
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}