// settings.gradle.kts
pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }

    versionCatalogs {
        // Create a single "libs" catalog and import one or more TOML files in a single from(...) call
        create("libs") {
            from(files("gradle/libs.versions.toml"))
            // If you need multiple TOML files, pass them all in one from(...) call:
            // from(files("gradle/libs.versions.toml", "gradle/extra.versions.toml"))
        }
    }
}

rootProject.name = "pushupcounter"
include(":app")