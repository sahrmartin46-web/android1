/**
 * Gradle Settings File
 *
 * Configures plugin repositories, global dependency resolution repositories,
 * toolchain resolution plugins, and included subprojects for the build.
 */

// Configure repositories where Gradle resolves plugins used in build scripts
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

// Apply settings plugins (e.g. Foojay Java toolchain resolver convention plugin)
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

// Global repository configuration for resolving project dependencies
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

// Root project name definition
rootProject.name = "socia\' media backend"

// Included modules/subprojects in this Gradle build
include(":app")
