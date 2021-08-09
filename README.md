# JUnit 5 MicroProfile Config Extension

A JUnit 5 extension for parameterizing tests with [MicroProfile Config](https://github.com/eclipse/microprofile-config).

## About

_MicroProfile Config_ defines a comfortable way for injecting configuration into Jakarta EE applications via CDI.
As JUnit test are usually not run inside a CDI container, _MicroProfile Config_ cannot be used in this context.

This extension supports a subset of _MicroProfile Config_ in JUnit by providing a [ParameterResolver](https://junit.org/junit5/docs/current/api/org.junit.jupiter.api/org/junit/jupiter/api/extension/ParameterResolver.html) that can resolve method parameters that are annotated with [@ConfigProperty](https://www.javadoc.io/doc/org.eclipse.microprofile.config/microprofile-config-api/2.0/org/eclipse/microprofile/config/inject/ConfigProperty.html).

The parameters can be provided as system properties, environment variables and properties in _src/test/resources/META-INF/microprofile-config.properties_.

> The actual work is delegated to the SmallRye implementation of Eclipse MicroProfile Config, https://github.com/smallrye/smallrye-config

## Use Cases

Use this extension if you want to be able to configure your tests in various ways.

For example, you could configure the test with environment variables in a parameterized Jenkins job.
During development, you could provide the values as system properties on the command line or in your IDE's run configurations.

## Example

This example shows how to declaratively inject externally defined static configuration into two parameters of a test method.

```java
import de.hamburger_software.util.junit.microprofile.config.MicroProfileConfigExtension;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MicroProfileConfigExtension.class)
class ConnectionTest {

    @Test
    void testConnection(
            @ConfigProperty(name = "db.username", defaultValue = "nobody") String username,
            @ConfigProperty(name = "db.password") String password) {

        // username will be populated from the system property "db.username" or the environment variable
        // "DB_USERNAME" or from a property in src/test/resources/META-INF/microprofile-config.properties.
        // If none of these sources provides a value, the declared default of "nobody" will be used.

        // In contrast, if password cannot be resolved, a ParameterResolutionException will be thrown.
    }

}
```
> Injection also works for constructor parameters and parameters of methods annotated with `@BeforeEach`, `@BeforeAll`, `@AfterEach` and `@AfterAll`.

## Dependencies

Add this fragment to your project's _pom.xml_.

```xml
    <dependencies>
        <dependency>
            <groupId>de.hamburger-software.util</groupId>
            <artifactId>junit-microprofile-config-extension</artifactId>
            <version>0.9.0</version>
            <scope>test</scope>
        </dependency>
    </dependencies>
```