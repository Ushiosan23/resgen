package io.github.ushiosan23.resgen.config;

import io.github.ushiosan23.resgen.DependencyName;
import org.gradle.api.Project;
import org.gradle.api.provider.Property;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public abstract class DependencyManagerOptions {

    /* ------------------------------------------------------------------
     * Constants
     * ------------------------------------------------------------------ */

    private static final String defaultJetbrainsAnnotationVersion = "23.0.0";

    /* ------------------------------------------------------------------
     * Properties
     * ------------------------------------------------------------------ */

    /**
     * Custom jetbrains annotations version
     */
    @DependencyName(name = "org.jetbrains:annotations:%s")
    public final Property<String> jetbrainsAnnotationsVersion;

    /* ------------------------------------------------------------------
     * Constructors
     * ------------------------------------------------------------------ */

    /**
     * Default constructor
     *
     * @param project Target project
     */
    public DependencyManagerOptions(@NotNull Project project) {
        // Initialize properties
        jetbrainsAnnotationsVersion = project
            .getObjects()
            .property(String.class)
            .value(defaultJetbrainsAnnotationVersion);
    }

    /* ------------------------------------------------------------------
     * Methods
     * ------------------------------------------------------------------ */

    /**
     * Get all configured dependencies
     *
     * @return Returns a map with all dependencies
     */
    @SuppressWarnings("unchecked")
    public Map<String, Property<String>> getAllDependencies() {
        Class<? extends DependencyManagerOptions> current = getClass();
        Field[] definedFields = Arrays.stream(current.getFields())
            .filter(field -> !Modifier.isStatic(field.getModifiers()))
            .filter(field -> field.isAnnotationPresent(DependencyName.class))
            .toArray(Field[]::new);

        // Generate result
        Map<String, Property<String>> result = new HashMap<>();
        // Iterate all fields
        try {
            for (Field field : definedFields) {
                DependencyName annotation = field.getAnnotation(DependencyName.class);
                Property<String> property = (Property<String>) field.get(this);
                result.put(annotation.name(), property);
            }
        } catch (Exception err) {
            err.printStackTrace();
        }
        return result;
    }

}
