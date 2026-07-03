plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    id(libs.plugins.google.services.get().pluginId) version libs.versions.googleServices.get() apply false
}
