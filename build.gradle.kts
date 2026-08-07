import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification
import org.gradle.testing.jacoco.tasks.JacocoReport

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    alias(libs.plugins.firebase.perf) apply false
    id("jacoco")
}

subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")

    configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
        buildUponDefaultConfig = true
        config.setFrom(rootProject.file("detekt.yml"))
        source.setFrom(files("src/main/java", "src/test/java"))
    }

    tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
        jvmTarget = "11"
    }
    tasks.withType<io.gitlab.arturbosch.detekt.DetektCreateBaselineTask>().configureEach {
        jvmTarget = "11"
    }
}

// Modules that have unit tests and contribute to the aggregated JaCoco report.
val jacocoModules = listOf(
    "core:domain",
    "feature:products:domain",
    "feature:products:presentation",
    "feature:sales:domain",
    "feature:sales:presentation",
    "feature:devtools:presentation",
)

val jacocoExcludes = listOf(
    "**/*Screen.kt",
    "**/*ScreenKt.class",
    "**/*Screen*.class",
    "**/di/**",
    "**/*_Hilt*.class",
    "**/Hilt_*.class",
    "**/*_Factory*.class",
    "**/*_MembersInjector.class",
    "**/DaggerHilt*.class",
    "**/*Module*.class",
    "**/*Application*.class",
    "**/*MainActivity*.class",
    "android/**/*.class",
    "**/R.class",
    "**/R$*.class",
    "**/BuildConfig.*",
    "**/Manifest*.*",
    "**/*Test*.*",
    "**/room/**",
    "**/*_Impl.class",
)

// Precompute plain File/String paths at configuration time so nothing captures a
// live Project/script-object reference inside task actions (required for the
// configuration cache).
val jacocoModuleDirs = jacocoModules.map { moduleName -> layout.projectDirectory.dir(moduleName.replace(":", "/")) }
val jacocoClassDirs = jacocoModuleDirs.map { it.dir("build/intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes") }
val jacocoSourceDirs = jacocoModuleDirs.map { it.dir("src/main/java") }
val jacocoExecFiles = jacocoModuleDirs.map { it.file("build/outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec") }
val jacocoTestTaskPaths = jacocoModules.map { ":$it:testDebugUnitTest" }

val jacocoAggregatedReport by tasks.registering(JacocoReport::class) {
    group = "verification"
    description = "Generates an aggregated JaCoco coverage report across all tested modules."

    dependsOn(jacocoTestTaskPaths)

    classDirectories.setFrom(jacocoClassDirs.map { dir -> fileTree(dir) { exclude(jacocoExcludes) } })
    sourceDirectories.setFrom(files(jacocoSourceDirs))
    executionData.setFrom(jacocoExecFiles)

    reports {
        xml.required.set(true)
        xml.outputLocation.set(layout.buildDirectory.file("reports/jacoco/aggregated/jacocoAggregatedReport.xml"))
        html.required.set(true)
        html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco/aggregated/html"))
    }
}

val jacocoCoverageVerification by tasks.registering(JacocoCoverageVerification::class) {
    group = "verification"
    description = "Verifies that the aggregated project-wide line coverage is at least 85%."

    dependsOn(jacocoAggregatedReport)

    classDirectories.setFrom(jacocoClassDirs.map { dir -> fileTree(dir) { exclude(jacocoExcludes) } })
    sourceDirectories.setFrom(files(jacocoSourceDirs))
    executionData.setFrom(jacocoExecFiles)

    violationRules {
        rule {
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "0.85".toBigDecimal()
            }
        }
    }
}
