plugins {
    `java-gradle-plugin`
    id("maven-publish")
    id("com.gradle.plugin-publish") version "0.14.0"
}

/* ------------------------------------------------------------------
 * Extra properties access
 * ------------------------------------------------------------------ */

val rpExtra get() = rootProject.extra

group = rpExtra["project.plugin.group"] as String
version = rpExtra["project.plugin.version"] as String

/* ------------------------------------------------------------------
 * Gradle plugin configuration
 * ------------------------------------------------------------------ */

gradlePlugin {
    // Define the plugin
    plugins {
        create("resgen") {
            id = rpExtra["project.plugin.id"] as String
            displayName = rpExtra["project.plugin.displayName"] as String
            description = rpExtra["project.plugin.description"] as String
            implementationClass = rpExtra["project.plugin.entry"] as String
        }
    }
}

/* ------------------------------------------------------------------
 * Testing configurations
 * ------------------------------------------------------------------ */

val functionalTestSourceSet = sourceSets.create("functionalTest") {}
gradlePlugin.testSourceSets(functionalTestSourceSet)
configurations["functionalTestImplementation"].extendsFrom(configurations["testImplementation"])
// Add a task to run the functional tests

val functionalTest by tasks.registering(Test::class) {
    testClassesDirs = functionalTestSourceSet.output.classesDirs
    classpath = functionalTestSourceSet.runtimeClasspath
    useJUnitPlatform()
}
tasks.check {
    // Run the functional tests as part of `check`
    dependsOn(functionalTest)
}

tasks.test {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}

/* ------------------------------------------------------------------
 * Plugin dependencies
 * ------------------------------------------------------------------ */

dependencies {
    implementation("com.squareup:javapoet:1.13.0")
    implementation("org.jetbrains:annotations:22.0.0")
    // Use JUnit Jupiter for testing.
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
}

/* ------------------------------------------------------------------
 * Publishing configuration
 * ------------------------------------------------------------------ */

pluginBundle {
    mavenCoordinates {
        website = "https://github.com/Ushiosan23/resgen"
        vcsUrl = "https://github.com/Ushiosan23/resgen.git"
        tags = listOf("java", "generator", "resources")
    }
}
