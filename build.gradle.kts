import org.spongepowered.gradle.plugin.config.PluginLoaders
import org.spongepowered.plugin.metadata.model.PluginDependency

plugins {
    `java-library`
    id("org.spongepowered.gradle.plugin") version "2.0.1"
}

group = "io.github.zerthick"
version = "1.1.0"

repositories {
    mavenCentral()
}

sponge {
    apiVersion("8.0.0")
    license("GPLV3")
    loader {
        name(PluginLoaders.JAVA_PLAIN)
        version("1.0")
    }
    plugin("noexp") {
        displayName("NoExp")
        entrypoint("io.github.zerthick.noexp.NoExp")
        description("A simple Minecraft experience control plugin.")
        links {
            homepage("https://ore.spongepowered.org/Zerthick/NoExp")
            source("https://github.com/Zerthick/NoExp")
            issues("https://github.com/Zerthick/NoExp/issues")
        }
        contributor("Zerthick") {
            description("Lead Developer")
        }
        dependency("spongeapi") {
            loadOrder(PluginDependency.LoadOrder.AFTER)
            optional(false)
        }
    }
}

val javaTarget = 8 // Sponge targets a minimum of Java 8
java {
    sourceCompatibility = JavaVersion.toVersion(javaTarget)
    targetCompatibility = JavaVersion.toVersion(javaTarget)
}

tasks.withType(JavaCompile::class).configureEach {
    options.apply {
        encoding = "utf-8" // Consistent source file encoding
        if (JavaVersion.current().isJava10Compatible) {
            release.set(javaTarget)
        }
    }
}

// Make sure all tasks which produce archives (jar, sources jar, javadoc jar, etc) produce more consistent output
tasks.withType(AbstractArchiveTask::class).configureEach {
    isReproducibleFileOrder = true
    isPreserveFileTimestamps = false
}
