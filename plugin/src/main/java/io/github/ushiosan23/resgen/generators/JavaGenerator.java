package io.github.ushiosan23.resgen.generators;

import com.squareup.javapoet.*;
import io.github.ushiosan23.resgen.config.ResourceGenerationOptions;
import io.github.ushiosan23.resgen.utils.PluginUtils;
import org.gradle.api.Project;
import org.gradle.api.file.SourceDirectorySet;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class JavaGenerator extends BaseGenerator {

    /* ------------------------------------------------------------------
     * Properties
     * ------------------------------------------------------------------ */

    /**
     * Used to check if constant starts with number
     */
    private static final Pattern startConstantPattern = Pattern.compile("^\\d");

    /**
     * All java reserved words
     */
    private static final String[] javaReservedWords = new String[]{
        "abstract", "assert", "boolean", "break", "byte", "case",
        "catch", "char", "class", "const", "continue", "default",
        "double", "do", "else", "enum", "extends", "false", "final",
        "finally", "float", "for", "goto", "if", "implements", "import",
        "instanceof", "int", "interface", "long", "native", "new",
        "null", "package", "private", "protected", "public", "return",
        "short", "static", "strictfp", "super", "switch", "synchronized",
        "this", "throw", "throws", "transient", "true", "try", "void", "volatile", "while"
    };

    /**
     * Output file location
     */
    private final Path outputJavaFile;

    /**
     * Class loader variable name
     */
    private static final String classLoaderName = "$systemClassLoader$";

    /**
     * Array variable name
     */
    private static final String magicArrayName = "$magicArrayContent$";

    /**
     * Counter used to generate resources
     */
    private volatile long resourceCounter = 0;

    /* ------------------------------------------------------------------
     * Constructors
     * ------------------------------------------------------------------ */

    /**
     * Default constructor generator
     *
     * @param project Target project
     * @param options Project options
     */
    public JavaGenerator(Project project, ResourceGenerationOptions options) {
        super(project, options);
        // Initialize properties
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

    }

    /**
     * Generate files and write all data
     *
     * @throws IOException Error to generate it
     */
    @Override
    public synchronized void generate() throws IOException {
        // Walk resources directory
        SourceDirectorySet resourceDirSet = PluginUtils.getResourcesSourceSet(currentProject);
        List<Path> resourceDirs = resourceDirSet.getSrcDirs()
            .stream()
            .map(File::toPath)
            .collect(Collectors.toList());

        TypeSpec.Builder classSpec = generateBaseClass();
        CodeBlock.Builder elementsContent = CodeBlock.builder()
            .add("new $T {\n", String[].class);

        // Generate file storage
        for (Path baseDir : resourceDirs) {
            // Check if exists
            if (!Files.exists(baseDir))
                continue;
            // Walk each directory
            try (Stream<Path> walker = Files.walk(baseDir)) {
                // Filter all results
                walker
                    .filter(Files::isRegularFile)
                    .filter(p -> !p.toString().equals(outputJavaFile.toString()))
                    .forEachOrdered(p -> {
                        insertEachPathContent(baseDir, p, elementsContent);
                        insertEachPathConstant(baseDir, p, classSpec);
                    });
            }
        }

        // Generate magic array
        elementsContent.add("}");
        FieldSpec.Builder arrayContentSpec = FieldSpec.builder(String[].class, magicArrayName)
            .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
            .initializer(elementsContent.build());
        classSpec.addField(arrayContentSpec.build());

        // Attach methods
        classSpec
            .addMethod(getResourceMethod(classLoaderName))
            .addMethod(getResourceAsStreamMethod(classLoaderName))
            .addMethod(getRegisteredResourceMethod())
            .addMethod(getRegisteredResourceAsStreamMethod());

        // Generate java file
        JavaFile outFile = JavaFile.builder(pluginOptions.getTargetPackage(), classSpec.build())
            .indent("\t")
            .build();
        // Write result
        outFile.writeTo(outputJavaFile);
    }

    /* ------------------------------------------------------------------
     * Internal methods
     * ------------------------------------------------------------------ */

    /**
     * @param p       Target file path
     * @param builder Target method spec
     */
    @Contract(pure = true)
    private synchronized void insertEachPathContent(@NotNull Path base, Path p, CodeBlock.@NotNull Builder builder) {
        // Generate relative path
        Path relative = base.relativize(p);
        String relativeString = relative.toString()
            .replace("\\", "/")
            .replace("\\/", "/");
        // Insert element to builder
        String expression = (resourceCounter + 1) % 4 == 0 ? "$S, \n" : "$S, ";
        builder.add(expression, relativeString);
    }

    /**
     * @param p       Target file path
     * @param builder Target type spec
     */
    private synchronized void insertEachPathConstant(@NotNull Path base, Path p, TypeSpec.@NotNull Builder builder) {
        // Generate relative path
        Path relative = base.relativize(p);
        String relativeLocation = relative.toString()
            .replace("\\", "/")
            .replace("\\/", "/");
        String constantName = relative.toString()
            .replace(".", "_")
            .replace("\\", "_")
            .replace("/", "_")
            .replaceAll("\\s", "_")
            .toLowerCase(Locale.ROOT);

        if (startConstantPattern.matcher(constantName).find())
            constantName = "$" + constantName;

        // Check if constant name is a java reserved word
        for (String reserved : javaReservedWords) {
            if (constantName.equals(reserved)) {
                constantName = "$" + constantName + "$";
                break;
            }
        }

        // Generate constant content
        FieldSpec.Builder constant = FieldSpec.builder(int.class, constantName)
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
            .addJavadoc("$L", relativeLocation)
            .initializer("$L", resourceCounter++);
        // Insert constant to type spec
        builder.addField(constant.build());
    }

    /**
     * Get base class spec
     *
     * @return Returns base class specifications
     */
    private TypeSpec.@NotNull Builder generateBaseClass() {
        // Generate base class name
        return TypeSpec
            .classBuilder(PluginUtils.OUTPUT_FILE_NAME)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addJavadoc(CodeBlock.builder()
                .add("Do not edit this file.\n")
                .add("This file is generated automatically and if it is edited it may stop working correctly.")
                .build())
            .addField(
                FieldSpec.builder(ClassLoader.class, classLoaderName)
                    .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                    .addJavadoc("Current application context loader")
                    .initializer("$T.getSystemClassLoader()", ClassLoader.class)
                    .build()
            );
    }

    /**
     * Generate {@code getRegisteredResource} method spec
     *
     * @return method spec instance
     */
    private @NotNull MethodSpec getRegisteredResourceMethod() {
        // Generate builders
        ParameterSpec.Builder resourceIdParam = ParameterSpec.builder(int.class, "resourceId");
        MethodSpec.Builder builder = MethodSpec.methodBuilder("getRegisteredResource")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .returns(URL.class);
        // Check if configuration contains injected dependencies
        if (pluginOptions.getInjectDependencies()) {
            builder.addAnnotation(NotNull.class);
        }
        // Add parameters
        builder.addParameter(resourceIdParam.build());
        // Add logic
        builder
            .addStatement("resourceId = $T.abs(resourceId)", Math.class)
            .beginControlFlow("if (resourceId > $L.length) ", magicArrayName)
            .addStatement(
                "throw new $T($S + $L + $S)",
                IndexOutOfBoundsException.class,
                "Resource ",
                "resourceId",
                " not found")
            .endControlFlow()
            .addStatement("$T res = getResource($L[resourceId])", URL.class, magicArrayName)
            .addStatement(
                "$T.requireNonNull(res, $S + $L[resourceId] + $S)",
                Objects.class,
                "Resource ",
                magicArrayName,
                " not found")
            .addStatement("return res");
        //Generate methodSpec
        return builder.build();
    }

    /**
     * Generate {@code getRegisteredResourceAsStream} method spec
     *
     * @return method spec instance
     */
    private @NotNull MethodSpec getRegisteredResourceAsStreamMethod() {
        // Generate builders
        ParameterSpec.Builder resourceIdParam = ParameterSpec.builder(int.class, "resourceId");
        MethodSpec.Builder builder = MethodSpec.methodBuilder("getRegisteredResourceAsStream")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .returns(InputStream.class);
        // Check if configuration contains injected dependencies
        if (pluginOptions.getInjectDependencies()) {
            builder.addAnnotation(NotNull.class);
        }
        // Add parameters
        builder.addParameter(resourceIdParam.build());
        // Add logic
        builder
            .addStatement("resourceId = $T.abs(resourceId)", Math.class)
            .beginControlFlow("if (resourceId > $L.length) ", magicArrayName)
            .addStatement(
                "throw new $T($S + $L + $S)",
                IndexOutOfBoundsException.class,
                "Resource ",
                "resourceId",
                " not found")
            .endControlFlow()
            .addStatement("$T res = getResourceAsStream($L[resourceId])", InputStream.class, magicArrayName)
            .addStatement(
                "$T.requireNonNull(res, $S + $L[resourceId] + $S)",
                Objects.class,
                "Resource ",
                magicArrayName,
                " not found")
            .addStatement("return res");
        //Generate methodSpec
        return builder.build();
    }

}
