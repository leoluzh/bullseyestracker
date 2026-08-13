plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.bullseyestracker.cv"
    compileSdk = 34

    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    // Mockito's ByteBuddy doesn't officially support very new JDKs yet; this opts into
    // ByteBuddy's experimental mode so `mock(Bitmap::class.java)` works on JDK 26.
    jvmArgs("-Dnet.bytebuddy.experimental=true")
}

dependencies {
    // OpenCV publishes an official Android AAR to Maven Central since 4.9.0.
    implementation("org.opencv:opencv:5.0.0.1")

    testImplementation("junit:junit:4.13.2")
    // Used only to satisfy FrameInput's Bitmap field in plain-JVM tests that never call
    // methods on it (CvEngineImpl and ScoreMapper never touch Bitmap contents/dimensions).
    testImplementation("org.mockito:mockito-core:5.8.0")

    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test:runner:1.7.0")
    androidTestImplementation("androidx.benchmark:benchmark-junit4:1.2.3")
}
