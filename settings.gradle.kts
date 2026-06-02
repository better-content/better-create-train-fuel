pluginManagement {
    repositories {
        maven("https://maven.minecraftforge.net")
        maven("https://repo.spongepowered.org/repository/maven-public/")
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven("https://maven.minecraftforge.net")
        maven("https://repo.spongepowered.org/repository/maven-public/")
        maven("https://maven.createmod.net")
        maven("https://maven.tterrag.com/")
    }
}

rootProject.name = "create-train-fuel-scaling"
