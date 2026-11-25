// Oberste Build-Datei: Plugins werden hier nur deklariert (apply false) und im Modul :app angewendet.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
}