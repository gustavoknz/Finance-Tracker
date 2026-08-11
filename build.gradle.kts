plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.kotlinx.kover) apply true
    alias(libs.plugins.detekt) apply true
}

detekt {
    toolVersion = libs.versions.detekt.get()
    config.setFrom(file("config/detekt/detekt.yml"))
    parallel = true
    buildUponDefaultConfig = true
}

val koverExcludedClasses = listOf("*.MainActivity", "dev.gustavo.finance.AppKt")
val koverExcludedPackages = listOf(
    "dev.gustavo.finance.di",
    "dev.gustavo.finance.di.*",
    "dev.gustavo.finance.data.local",
    "dev.gustavo.finance.data.local.*",
    "finance_tracker.shared.generated.resources",
    "finance_tracker.shared.generated.resources.*"
)
val koverExcludedAnnotations = listOf("androidx.compose.runtime.Composable")

kover {
    reports {
        filters {
            excludes {
                classes(koverExcludedClasses)
                packages(koverExcludedPackages)
                annotatedBy(koverExcludedAnnotations.first())
            }
        }
    }
}

subprojects {
    plugins.withId("org.jetbrains.kotlinx.kover") {
        configure<kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension> {
            reports {
                filters {
                    excludes {
                        classes(koverExcludedClasses)
                        packages(koverExcludedPackages)
                        annotatedBy(koverExcludedAnnotations.first())
                    }
                }
            }
        }
    }
}
