import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension

plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.kotlinx.kover) apply true
    alias(libs.plugins.detekt) apply true
}

// Total coverage report configuration
dependencies {
    kover(project(":shared"))
    kover(project(":androidApp"))
}

// --- Detekt Root Configuration ---
detekt {
    toolVersion = libs.versions.detekt.get()
    config.setFrom(layout.projectDirectory.file("config/detekt/detekt.yml"))
    parallel = true
    buildUponDefaultConfig = true
    reportsDir = layout.buildDirectory.dir("reports/detekt").get().asFile
}

fun Detekt.configureReports() {
    reports {
        xml.required.set(true)
        html.required.set(true)
        txt.required.set(false)
        sarif.required.set(false)
        md.required.set(false)
    }
}

tasks.withType<Detekt>().configureEach {
    configureReports()
}

// --- Kover Common Exclusions ---
val koverExcludedClasses = listOf(
    "*.MainActivity",
    "dev.gustavo.finance.AppKt",
    "dev.gustavo.finance.FinanceApp",
    "*.ComposableSingletons*",
    "*.ExchangeRateScreenKt",
)

val koverExcludedPackages = listOf(
    "dev.gustavo.finance.di",
    "dev.gustavo.finance.di.*",
    "dev.gustavo.finance.data.local",
    "dev.gustavo.finance.data.local.*",
    "finance_tracker.shared.generated.resources",
    "finance_tracker.shared.generated.resources.*",
)

val koverExcludedAnnotations = listOf("androidx.compose.runtime.Composable")

kover {
    reports {
        total {
            html {
                onCheck.set(true)
            }
        }
        filters {
            excludes {
                classes(koverExcludedClasses)
                packages(koverExcludedPackages)
                annotatedBy(koverExcludedAnnotations.first())
            }
        }
    }
}

// --- Subproject Specific Configurations ---
subprojects {
    plugins.withId("io.gitlab.arturbosch.detekt") {
        extensions.configure<DetektExtension> {
            reportsDir = layout.buildDirectory.dir("reports/detekt").get().asFile
        }
        tasks.withType<Detekt>().configureEach {
            configureReports()
        }
    }

    plugins.withId("org.jetbrains.kotlinx.kover") {
        configure<KoverProjectExtension> {
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
