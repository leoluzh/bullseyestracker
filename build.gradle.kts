plugins {
    // AGP 9.0+ has built-in Kotlin support - org.jetbrains.kotlin.android is no longer
    // applied/needed and is incompatible with the new DSL (see
    // https://developer.android.com/build/releases/agp-9-0-0-release-notes). AGP 9.3.1 has a
    // runtime dependency on Kotlin Gradle Plugin 2.2.10; the compose plugin and KSP are pinned
    // to match that baseline.
    id("com.android.application") version "9.3.1" apply false
    id("com.android.library") version "9.3.1" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.10" apply false
    id("com.google.devtools.ksp") version "2.2.10-2.0.2" apply false
    id("org.jlleitschuh.gradle.ktlint") version "12.1.0" apply false
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
}
