# ResGen

It is a plugin used to automatically generate assets to a java application (Similar to what android studio does, but very basic)

## How does it work?

The plugin parses the project's resource folder recursively and generates a class with an index of all found items. The
resulting class, like android studio, is only an index of the places where the document is located (constants with a particular
number) and the documents are accessible because the class has auxiliary methods for obtaining the files.

## Example

Let's imagine that our project has the following structure.

```text
|- main
    |- java
    |- resources
        |- pictures
            |- logo.png
        |- configuration
            |- base_configuration.xml
            |- database_configuration.xml
        |- fonts
            |- roboto.ttf
            |- icomoon.tff
```

The plugin only registers the directory with the resources and will create a class like the following.

```java
// Res.java -> Generated name

public class Res {

    // All resources here

    // This is just a representation of a number,
    // this does not mean that it will always be like this.
    public static final int pictures_logo_png = 0;
    public static final int configuration_base_configuration_xml = 1;
    public static final int configuration_database_configuration_xml = 2;
    public static final int fonts_roboto_ttf = 3;
    public static final int fonts_icomoon_ttf = 4;

    // Auxiliary methods
    // ...
}
```

The plugin takes into consideration the naming rules for constants and changes the name of the constant depending on the
situation.

Let's take the following example:

```text
|- resources
    |- 1example.txt
    |- this
    |- if
```

The following files are valid document names, but invalid within the java language. The plugin solves this by adding elements so
that there are no problems when compiling.

The following would be the result of the previous example.

```java
public class Res {

    // The dots are replaced by an underscore just like the diagonals.
    // Shift the numbers at the beginning of each constant by a currency symbol.
    public static final int $1example_txt = 0;
    // The reserved words are enclosed in currency symbols.
    public static final int $this$ = 1;
    public static final int $if$ = 2;
}
```
