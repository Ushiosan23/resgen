package io.github.ushiosan23.resgen;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DependencyName {

    /**
     * Target dependency notation
     *
     * @return Dependency notation
     */
    String name();

}
