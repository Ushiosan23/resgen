package io.github.ushiosan23.resgen.generators;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import io.github.ushiosan23.resgen.config.ResourceGenerationOptions;
import org.gradle.api.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.Modifier;
import java.io.InputStream;
import java.net.URL;

public abstract class BaseGenerator implements IGenerator {

    /* ------------------------------------------------------------------
     * Properties
     * ------------------------------------------------------------------ */

    /**
     * Current project
     */
    protected final Project currentProject;

    /**
     * Current options plugins
     */
    protected final ResourceGenerationOptions pluginOptions;

    /* ------------------------------------------------------------------
     * Constructors
     * ------------------------------------------------------------------ */

    /**
     * Default constructor
     *
     * @param p   Target project
     * @param opt Plugin options
     */
    public BaseGenerator(@NotNull Project p, @NotNull ResourceGenerationOptions opt) {
        currentProject = p;
        pluginOptions = opt;
    }

    /* ------------------------------------------------------------------
     * Base Methods
     * ------------------------------------------------------------------ */

    /**
     * Generate {@code getResource} method spec
     *
     * @param loaderName Loader variable name
     *
     * @return Method spec result
     */
    protected @NotNull MethodSpec getResourceMethod(@NotNull String loaderName) {
        ParameterSpec.Builder locationParam = ParameterSpec.builder(String.class, "location");
        MethodSpec.Builder builder = MethodSpec.methodBuilder("getResource")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .returns(URL.class);
        // Check if configuration contains injected dependencies
        if (pluginOptions.getInjectDependencies()) {
            builder.addAnnotation(Nullable.class);
            locationParam.addAnnotation(NotNull.class);
        }
        // Add parameters
        builder.addParameter(locationParam.build());
        // Add logic
        builder.addStatement("return $L.getResource(location)", loaderName);
        //Generate methodSpec
        return builder.build();
    }

    /**
     * Generate {@code getResourceAsStream} method spec
     *
     * @param loaderName Loader variable name
     *
     * @return Method spec result
     */
    protected @NotNull MethodSpec getResourceAsStreamMethod(@NotNull String loaderName) {
        ParameterSpec.Builder locationParam = ParameterSpec.builder(String.class, "location");
        MethodSpec.Builder builder = MethodSpec.methodBuilder("getResourceAsStream")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .returns(InputStream.class);
        // Check if configuration contains injected dependencies
        if (pluginOptions.getInjectDependencies()) {
            builder.addAnnotation(Nullable.class);
            locationParam.addAnnotation(NotNull.class);
        }
        // Add parameters
        builder.addParameter(locationParam.build());
        // Add logic
        builder.addStatement("return $L.getResourceAsStream(location)", loaderName);
        // Generate methodSpec
        return builder.build();
    }

}
