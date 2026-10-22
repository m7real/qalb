// Root build script. All versions are declared in gradle/libs.versions.toml —
// do not hardcode versions here or anywhere else.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}
