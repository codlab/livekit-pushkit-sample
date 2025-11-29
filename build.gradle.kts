plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(additionals.plugins.android.application) apply false
    alias(additionals.plugins.android.library) apply false
    alias(additionals.plugins.jetbrains.compose) apply false
    alias(additionals.plugins.compose.compiler) apply false
    alias(additionals.plugins.kotlin.jvm) apply false
    alias(additionals.plugins.kotlin.multiplatform) apply false
    alias(additionals.plugins.kotlin.cocoapods) apply false
    alias(libs.plugins.ktor) apply false
    alias(additionals.plugins.kotlin.serialization) apply false
}