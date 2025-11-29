plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    alias(additionals.plugins.kotlin.serialization)
    application
}

group = "eu.codlab.push"
version = "1.0.0"
application {
    mainClass.set("eu.codlab.push.ApplicationKt")
    
    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

dependencies {
    implementation(libs.logback)
    implementation(libs.ktor.serverCore)
    implementation(libs.ktor.serverNetty)
    implementation(libs.pushy)
    implementation(additionals.kotlinx.serialization.json)

    implementation(libs.ktor.server.json)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.content.negotiation)

    testImplementation(libs.ktor.serverTestHost)
    testImplementation(libs.kotlin.testJunit)
}