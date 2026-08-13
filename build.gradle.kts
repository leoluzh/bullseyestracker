plugins {
    id("com.android.application") version "9.3.1" apply false
    id("com.android.library") version "9.3.1" apply false
    id("org.jetbrains.kotlin.android") version "2.4.10" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.21" apply false
    id("com.google.devtools.ksp") version "2.1.21-2.0.1" apply false
    id("org.jlleitschuh.gradle.ktlint") version "12.1.0" apply false
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
}
