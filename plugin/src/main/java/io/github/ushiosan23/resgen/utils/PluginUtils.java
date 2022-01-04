package io.github.ushiosan23.resgen.utils;

import io.github.ushiosan23.resgen.config.DependencyManagerOptions;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.PluginContainer;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;

public final class PluginUtils {

    private PluginUtils() {
    }

    /* ------------------------------------------------------------------
     * Properties
     * ------------------------------------------------------------------ */

    /**
     * All plugin dependencies
     */
    @SuppressWarnings("unchecked")
    private static final Class<? extends Plugin<?>>[] pluginDependencies = new Class[]{
        JavaPlugin.class
    };

    /**
     * Output java file name
     */
    public static final String OUTPUT_FILE_NAME = "Res";

    /**
     * Output properties file name
     */
    public static final String OUTPUT_PROPERTIES_NAME = "resources.properties";

    /* ------------------------------------------------------------------
     * Methods
     * ------------------------------------------------------------------ */

    /**
     * Check if current project contains required dependencies
     *
     * @param project Project to check
     */
    public static void checkDependencies(@NotNull Project project) {
        // Get plugin container
        PluginContainer container = project.getPlugins();
        // Check if container has target dependency
        for (Class<? extends Plugin<?>> dependency : pluginDependencies) {
            if (!container.hasPlugin(dependency)) container.apply(dependency);
        }
    }

    /**
     * Check dependency manager and manage all dependencies
     *
     * @param project Project to check
     */
    public static void injectDependencies(@NotNull Project project, @NotNull DependencyManagerOptions options) {
        // Get all dependencies
        Map<String, Property<String>> dependencies = options.getAllDependencies();
        // Iterate dependencies
        for (Map.Entry<String, Property<String>> dependency : dependencies.entrySet()) {
            // Check dependency version content
            if (!dependency.getValue().isPresent()) continue;
            // Resolve dependency
            String dependencyNotation = String.format(dependency.getKey(), dependency.getValue().get());
            project.getDependencies().add("compileOnly", dependencyNotation);
        }
    }

    /**
     * Get source container
     *
     * @param project Target project
     *
     * @return Return current source container
     */
    public static SourceSetContainer getContainer(@NotNull Project project) {
        return project.getExtensions().getByType(SourceSetContainer.class);
    }

    /**
     * Get main container
     *
     * @param project Target project
     *
     * @return Get main source set
     */
    public static SourceSet getMainSourceSet(@NotNull Project project) {
        // Get source sets
        SourceSetContainer container = getContainer(project);
        return container.getByName("main");
    }

    /**
     * Get resource directory set
     *
     * @param project Project to check
     *
     * @return Resource directory check
     */
    public static SourceDirectorySet getResourcesSourceSet(@NotNull Project project) {
        // Get source sets
        SourceSet mainSet = getMainSourceSet(project);
        // Get main resource directory set
        return mainSet.getResources();
    }

    /**
     * Get properties output path
     *
     * @param project Target project
     *
     * @return Valid properties target
     */
    public static @NotNull Path resolvePropertiesPath(@NotNull Project project) {
        // Get build dir
        File buildDir = project.getProjectDir();
        String projectName = project.getName();
        String outFileName = projectName.isBlank() ? OUTPUT_PROPERTIES_NAME : projectName + "_" + OUTPUT_PROPERTIES_NAME;
        // Generate location
        return Path.of(buildDir.getAbsolutePath(), "src", "main", "resources", outFileName);
    }

    /**
     * Resolve project file name
     *
     * @param project Target project
     *
     * @return Valid project target
     */
    public static @NotNull Path resolveJavaPath(@NotNull Project project) {
        // Get build dir
        File buildDir = project.getProjectDir();
        String javaGroup = getJavaGroup(project);
        String groupPath = String.join("/", javaGroup.split("\\."));

        // Generate location
        return Path.of(buildDir.getAbsolutePath(), "src", "main", "java");
    }


    public static @NotNull String getJavaGroup(@NotNull Project project) {
        String group = (String) project.getGroup();
        if (group.isBlank() || group.equals("resourceGeneratorPlugin")) {
            group = "resgen";
        }
        return group;
    }

}
