pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Desafio Omie"
include(":app")
include(":core:domain")
include(":core:database")
include(":core:designsystem")
include(":core:presentation")
include(":core:analytics")
include(":feature:devtools:presentation")
include(":feature:products:domain")
include(":feature:products:data")
include(":feature:products:presentation")
include(":feature:sales:domain")
include(":feature:sales:data")
include(":feature:sales:presentation")
