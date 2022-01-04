package io.github.ushiosan23.resgen;

import io.github.ushiosan23.resgen.config.DependencyManagerOptions;
import io.github.ushiosan23.resgen.config.ResourceGenerationOptions;
import io.github.ushiosan23.resgen.task.GenerateResourcesTask;
import io.github.ushiosan23.resgen.utils.PluginUtils;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.jetbrains.annotations.NotNull;

public class ResourceGeneratorEntry implements Plugin<Project> {

    /* ------------------------------------------------------------------
     * Internal Properties
     * ------------------------------------------------------------------ */

    /**
     * Dependency options
     */
    private DependencyManagerOptions dependencyOptions;

    /**
     * Resource generation config
     */
    private ResourceGenerationOptions pluginOptions;

    /**
     * Generation resource task
     */
    private Task generateResourceTask;

    /* ------------------------------------------------------------------
     * Methods
     * ------------------------------------------------------------------ */

    /**
     * Apply plugin on current project
     *
     * @param project lorem
     */
    public void apply(@NotNull Project project) {
        // Check dependencies
        PluginUtils.checkDependencies(project);
        // Register extensions
        dependencyOptions = project.getExtensions()
            .create("resgenDependencies", DependencyManagerOptions.class, project);
        pluginOptions = project.getExtensions()
            .create("resgen", ResourceGenerationOptions.class, project, dependencyOptions);
        // Register a task
        generateResourceTask = project
            .getTasks()
            .register("generateResources", GenerateResourcesTask.class, pluginOptions)
            .get();
        project.getTasks().getByName("compileJava", closure -> closure.dependsOn(generateResourceTask));
    }

}
