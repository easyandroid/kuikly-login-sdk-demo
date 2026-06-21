pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven("https://mirrors.tencent.com/nexus/repository/maven-tencent/")
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://mirrors.tencent.com/nexus/repository/maven-tencent/")
    }
}

rootProject.name = "kuikly-login-sdk-demo"

include(":login-sdk")
include(":android-host")
