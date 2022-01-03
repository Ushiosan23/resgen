buildscript {
    /* ------------------------------------------------------------------
     * Extra configurations
     * ------------------------------------------------------------------ */

    // Maven configurations
    extra.set("maven.artifactUrl", "https://github.com/Ushiosan23/resgen")
    extra.set("maven.licenseType", "MIT")
    extra.set("maven.licenseUrl", "${extra["maven.artifactUrl"]}/blob/main/LICENCE.md")

    extra.set(
        "maven.developers", listOf(
            mapOf(
                "id" to "Ushiosan23",
                "name" to "Brian Alvarez",
                "email" to "haloleyendee@outlook.com"
            )
        )
    )
    // Git configuration
    extra.set("scm.connection", "scm:git:github.com/Ushiosan23/resgen.git")
    extra.set("scm.connection.ssh", "scm:git:ssh:github.com/Ushiosan23/resgen.git")
    extra.set("scm.url", "${extra["maven.artifactUrl"]}/tree/main")

    extra.set("project.plugin.version", "0.0.1")
    extra.set("project.plugin.group", "io.github.ushiosan23")
    extra.set("project.plugin.id", "${extra["project.plugin.group"]}.resgen")
    extra.set("project.plugin.entry", "${extra["project.plugin.id"]}.ResourceGeneratorEntry")
    extra.set("project.plugin.displayName", "Project resource generator")
    extra.set(
        "project.plugin.description",
        """
        It is a plugin used to automatically generate assets to a java application (Similar to what android studio does,
        but very basic)
        """.trimIndent()
    )

    /* ------------------------------------------------------------------
     * Repositories
     * ------------------------------------------------------------------ */

    repositories {
        mavenCentral()
        mavenLocal()
    }

    /* ------------------------------------------------------------------
     * Dependencies
     * ------------------------------------------------------------------ */

    dependencies {

    }
}

/* ------------------------------------------------------------------
 * All projects configurations
 * ------------------------------------------------------------------ */

allprojects {
    repositories {
        mavenCentral()
    }
}
