package de.hamburger_software.util.junit.microprofile.config;

final class PropertyNames {
    /** Name of a String property that maven-surefire-plugin defines as an environment variable */
    public static final String DEFINED_STRING_ENV = "defined.string.property.env";

    /** Name of a String property that maven-surefire-plugin defines as a system property */
    public static final String DEFINED_STRING_SYS = "defined.string.property.sys";

    /** Name of a String property that is defined in <i>META-INF/microprofile-config.properties</i> */
    public static final String DEFINED_STRING_FILE = "defined.string.property.file";

    /** Name of a String property that is undefined */
    public static final String UNDEFINED_STRING = "undefined.string.property";

    /** Name of an Integer property that maven-surefire-plugin defines as an environment variable */
    public static final String DEFINED_INTEGER_ENV = "defined.integer.property.env";

    private PropertyNames() {
    }
}
