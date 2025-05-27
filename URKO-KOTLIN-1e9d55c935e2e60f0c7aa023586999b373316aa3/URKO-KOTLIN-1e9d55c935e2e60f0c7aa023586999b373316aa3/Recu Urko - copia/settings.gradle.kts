pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://jitpack.io") }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") } // Add this if needed in dependencies too
    }
    versionCatalogs {
        libs {
            from(files("gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "Recu-Urko"
include(":app")