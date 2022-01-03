import io.github.ushiosan23.resgen.config.GeneratorType

plugins {
    java
    id("io.github.ushiosan23.resgen") version "0.0.1"
}

resgen {
    generationType = GeneratorType.JAVA_FILE
    injectDependencies = true
    targetPackage = "com.example"
}
