package io.github.ushiosan23.resgen.config;

import io.github.ushiosan23.resgen.utils.PluginUtils;
import org.gradle.api.Project;
import org.gradle.api.provider.Property;
import org.jetbrains.annotations.NotNull;

public abstract class ResourceGenerationOptions {

    /* ------------------------------------------------------------------
     * Internal Properties
     * ------------------------------------------------------------------ */

    /**
     * Current selected project
     */
    protected final Project currentProject;

    /* ------------------------------------------------------------------
     * Properties
     * ------------------------------------------------------------------ */

    /**
     * Property used to inject dependencies
     */
    private final Property<Boolean> injectDependencies;

    /**
     * Type of resource generation
     */
    private final Property<GeneratorType> generationType;

    /**
     * Target java file group
     */
    private final Property<String> targetPackage;

    /**
     * Dependency manager options
     */
    public final DependencyManagerOptions dependencyOptions;

    /* ------------------------------------------------------------------
     * Constructors
     * ------------------------------------------------------------------ */

    /**
     * Default constructor class
     *
     * @param project Current project
     */
    public ResourceGenerationOptions(@NotNull Project project, @NotNull DependencyManagerOptions options) {
        currentProject = project;
        dependencyOptions = options;
        // Initialize properties
        injectDependencies = project
            .getObjects()
            .property(Boolean.class)
            .value(false);
        generationType = project
            .getObjects()
            .property(GeneratorType.class)
            .value(GeneratorType.PROPERTIES_FILE);
        targetPackage = project
            .getObjects()
            .property(String.class)
            .value(PluginUtils.getJavaGroup(project));
    }

    /* ------------------------------------------------------------------
     * Methods
     * ------------------------------------------------------------------ */

    /**
     * Get inject dependencies
     *
     * @return Get inject dependencies
     */
    public boolean getInjectDependencies() {
        return injectDependencies.getOrElse(false);
    }

    /**
     * Set inject dependencies status
     *
     * @param status Target status
     */
    public void setInjectDependencies(boolean status) {
        injectDependencies.set(status);
        updateDependencies();
    }

    /**
     * Get current java package
     *
     * @return Target package name
     */
    public String getTargetPackage() {
        return targetPackage.getOrElse(PluginUtils.getJavaGroup(currentProject));
    }

    /**
     * Set target group
     *
     * @param packageName Java package name
     */
    public void setTargetPackage(String packageName) {
        packageName = packageName
            .replaceAll("\\s", ".")
            .replaceAll("-", "_");
        targetPackage.set(packageName);
    }

    /**
     * Get generation type
     *
     * @return Get generator type
     */
    public GeneratorType getGenerationType() {
        return generationType.getOrElse(GeneratorType.PROPERTIES_FILE);
    }

    /**
     * Set inject dependencies status
     *
     * @param type Set type generator
     */
    public void setGenerationType(GeneratorType type) {
        generationType.set(type);
    }

    /* ------------------------------------------------------------------
     * Internal methods
     * ------------------------------------------------------------------ */

    /**
     * Update dependencies
     */
    private void updateDependencies() {
        if (getInjectDependencies()) {
            PluginUtils.injectDependencies(currentProject, dependencyOptions);
        }
    }


}
