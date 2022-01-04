package io.github.ushiosan23.resgen.task;

import io.github.ushiosan23.resgen.config.ResourceGenerationOptions;
import io.github.ushiosan23.resgen.generators.IGenerator;
import io.github.ushiosan23.resgen.generators.JavaGenerator;
import io.github.ushiosan23.resgen.generators.PropertiesJavaGenerator;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.IOException;

public abstract class GenerateResourcesTask extends DefaultTask {

    /* ------------------------------------------------------------------
     * Properties
     * ------------------------------------------------------------------ */

    /**
     * Resources generation options
     */
    private final ResourceGenerationOptions options;

    /* ------------------------------------------------------------------
     * Methods
     * ------------------------------------------------------------------ */

    /**
     * Default constructor
     */
    @Inject
    public GenerateResourcesTask(ResourceGenerationOptions opt) {
        super();
        // Initialize properties
        options = opt;
    }

    /**
     * Default task action
     */
    @TaskAction
    public void taskAction() throws IOException {
        // Properties
        IGenerator generator = null;
        // Check generation type
        switch (options.getGenerationType()) {
            case PROPERTIES_FILE:
                generator = new PropertiesJavaGenerator(getProject(), options);
                break;
            case JAVA_FILE:
                generator = new JavaGenerator(getProject(), options);
                break;
        }
        // Check if generator is null
        if (generator == null) return;
        // Launch generator
        generator.generate();
    }

}
