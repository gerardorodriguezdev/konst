plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    jvm()
}

dependencies {
    commonMainImplementation(project(":core"))
    kotlinCompilerPluginClasspath(project(":compiler"))
    commonTestImplementation(libs.kotlin.test)
}
