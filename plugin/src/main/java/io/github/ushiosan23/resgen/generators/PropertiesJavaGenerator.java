package io.github.ushiosan23.resgen.generators;

import com.squareup.javapoet.*;
import io.github.ushiosan23.resgen.config.ResourceGenerationOptions;
import io.github.ushiosan23.resgen.utils.PluginUtils;
import org.gradle.api.Project;
import org.gradle.api.file.SourceDirectorySet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class PropertiesJavaGenerator extends BaseGenerator {

    /* ------------------------------------------------------------------
     * Properties
     * ------------------------------------------------------------------ */

    /**
     * Output file location
     */
    private final Path outputJavaFile;

    /**
     * Output file location
     */
    private final Path outputPropertiesFile;

    /* ------------------------------------------------------------------
     * Constructor
     * ------------------------------------------------------------------ */

    /**
     * Default constructor
     *
     * @param project Target project
     * @param options Plugin options
     */
    public PropertiesJavaGenerator(Project project, ResourceGenerationOptions options) {
        super(project, options);
        // Initialize properties
        outputPropertiesFile = PluginUtils.resolvePropertiesPath(currentProject);
        outputJavaFile = PluginUtils.resolveJavaPath(currentProject);
    }

    /* ------------------------------------------------------------------
     * Methods
     * ------------------------------------------------------------------ */

    /**
     * Create files if it´s necessary
     *
     * @throws IOException Error to create files
     */
    @Override
    public void createIfIsNeed() throws IOException {
        // Check if file exists
        if (!Files.exists(outputPropertiesFile)) {
            Files.createDirectories(outputPropertiesFile.getParent());
            Files.createFile(outputPropertiesFile);
        }

        Files.write(outputPropertiesFile, new byte[0], StandardOpenOption.TRUNCATE_EXISTING);
    }

    /**
     * Generate files and write all data
     *
     * @throws IOException Error to generate it
     */
    @Override
    public void generate() throws IOException {
        // Create files if not exists
        createIfIsNeed();
        // Walk resources directory
        SourceDirectorySet resourceDirSet = PluginUtils.getResourcesSourceSet(currentProject);
        List<Path> resourceDirs = resourceDirSet.getSrcDirs()
            .stream()
            .map(File::toPath)
            .collect(Collectors.toList());

        // Generate base properties object
        Properties outResourceProps = new Properties();
        // Iterate all paths
        for (Path baseDir : resourceDirs) {
            // Walk directory
            try (Stream<Path> walker = Files.walk(baseDir)) {
                walker
                    .filter(Files::isRegularFile)
                    .filter(path -> !path.toString().equals(outputPropertiesFile.toString()))
                    .forEachOrdered(path -> {
                        // Relativize location
                        Path finalPath = baseDir.relativize(path);
                        String keyName = finalPath.toString()
                            .replace(".", "_")
                            .replace("\\", ".")
                            .replace("/", ".");
                        String keyVal = finalPath.toString()
                            .replace("\\", "/")
                            .replace("\\/", "/");
                        outResourceProps.setProperty(keyName, keyVal);
                    });
            }
        }

        // Store properties
        try (Writer stream = Files.newBufferedWriter(outputPropertiesFile)) {
            outResourceProps.store(stream, null);
        }
        // Store java class
        JavaFile javaFile = generateJavaFileSpec();
        javaFile.writeTo(outputJavaFile);
    }

    /* ------------------------------------------------------------------
     * Internal methods
     * ------------------------------------------------------------------ */

    private @NotNull JavaFile generateJavaFileSpec() {
        return JavaFile.builder(pluginOptions.getTargetPackage(), generateBaseClass())
            .build();
    }

    private @NotNull TypeSpec generateBaseClass() {
        String outPropertiesName = outputPropertiesFile.getFileName().toString();
        // Generate builder
        TypeSpec.Builder builder = TypeSpec.classBuilder(PluginUtils.OUTPUT_FILE_NAME)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL);
        // Add properties
        builder.addField(FieldSpec.builder(ClassLoader.class, "systemLoader")
            .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
            .initializer("$T.getSystemClassLoader()", ClassLoader.class)
            .build());
        builder.addField(FieldSpec.builder(String.class, "propertiesLocation")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
            .initializer("$S", outPropertiesName)
            .build());
        builder.addField(FieldSpec.builder(Properties.class, "registeredResources")
            .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
            .initializer("new $T()", Properties.class)
            .build());

        // Static initializer
        builder.addStaticBlock(CodeBlock.builder()
            .beginControlFlow("try($T baseResources = systemLoader.getResourceAsStream(propertiesLocation))", InputStream.class)
            .addStatement("registeredResources.load(baseResources)")
            .nextControlFlow("catch ($T err)", Exception.class)
            .addStatement("err.printStackTrace()")
            .endControlFlow()
            .build());

        // Add methods
        builder.addMethod(getResourceMethod("systemLoader"));
        builder.addMethod(getResourceAsStreamMethod("systemLoader"));
        builder.addMethod(getRegisteredResourceMethod());
        builder.addMethod(getRegisteredResourceMethodAsStream());

        return builder.build();
    }

    private @NotNull MethodSpec getRegisteredResourceMethod() {
        ParameterSpec.Builder locationParam = ParameterSpec.builder(String.class, "location");
        MethodSpec.Builder builder = MethodSpec.methodBuilder("getRegisteredResource")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .returns(URL.class);
        // Check if configuration contains injected dependencies
        if (pluginOptions.getInjectDependencies()) {
            registeredInjectAnnotations(builder, locationParam);
        }
        // Add parameters
        builder.addParameter(locationParam.build());
        // Add logic
        builder
            .addStatement("$T property = registeredResources.getProperty(location, null)", String.class)
            .addComment("Check if property is null")
            .addStatement("$T.requireNonNull(property, location + $S)", Objects.class, " resource not found")
            .addStatement("return getResource(property)");

        // Generate methodSpec
        return builder.build();
    }

    private @NotNull MethodSpec getRegisteredResourceMethodAsStream() {
        ParameterSpec.Builder locationParam = ParameterSpec.builder(String.class, "location");
        MethodSpec.Builder builder = MethodSpec.methodBuilder("getRegisteredResourceAsStream")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .returns(InputStream.class);
        // Check if configuration contains injected dependencies
        if (pluginOptions.getInjectDependencies()) {
            registeredInjectAnnotations(builder, locationParam);
        }
        // Add parameters
        builder.addParameter(locationParam.build());
        // Add logic
        builder
            .addStatement("$T property = registeredResources.getProperty(location, null)", String.class)
            .addComment("Check if property is null")
            .addStatement("$T.requireNonNull(property, location + $S)", Objects.class, " resource not found")
            .addStatement("return getResourceAsStream(property)", InputStream.class);
        // Generate methodSpec
        return builder.build();
    }

    private void registeredInjectAnnotations(MethodSpec.Builder method, ParameterSpec.Builder param) {
        Path propertiesFilename = outputPropertiesFile.getFileName();
        String cleanFilename = propertiesFilename.toString();
        // Clean filename
        int indexOf = cleanFilename.indexOf(".");
        if (indexOf != -1) cleanFilename = cleanFilename.substring(0, indexOf);
        // Attach to param
        method.addAnnotation(NotNull.class);
        param.addAnnotation(NotNull.class)
            .addAnnotation(AnnotationSpec.builder(PropertyKey.class)
                .addMember("resourceBundle", "$S", cleanFilename)
                .build());
    }

}
