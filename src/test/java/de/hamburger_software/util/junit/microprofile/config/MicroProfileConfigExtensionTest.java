package de.hamburger_software.util.junit.microprofile.config;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.engine.config.JupiterConfiguration;
import org.junit.jupiter.engine.execution.ExecutableInvoker;
import org.junit.jupiter.engine.execution.ExecutableInvoker.ReflectiveInterceptorCall;
import org.junit.jupiter.engine.extension.MutableExtensionRegistry;
import org.junit.platform.commons.util.ReflectionUtils;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.NoSuchElementException;

import static de.hamburger_software.util.junit.microprofile.config.MicroProfileConfigExtensionTest.MethodSource.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Note: These test use environment variables and system properties set by the Maven Surefire Plugin.
 */
class MicroProfileConfigExtensionTest {
    private static final String DEFINED_STRING_PROPERTY_ENV = "defined.string.property.env";
    private static final String DEFINED_STRING_PROPERTY_SYS = "defined.string.property.sys";
    private static final String DEFINED_STRING_PROPERTY_FILE = "defined.string.property.file";
    private static final String DEFINED_INTEGER_PROPERTY = "defined.integer.property";
    private static final String UNDEFINED_STRING_PROPERTY = "undefined.string.property";
    private static final String UNDEFINED_INTEGER_PROPERTY = "undefined.integer.property";

    private final MethodSource instance = mock(MethodSource.class);

    private final ExtensionContext extensionContext = mock(ExtensionContext.class);

    private final JupiterConfiguration configuration = mock(JupiterConfiguration.class);

    private final MutableExtensionRegistry extensionRegistry = MutableExtensionRegistry
            .createRegistryWithDefaultExtensions(configuration);

    private Method method;

    /**
     * An {@link ExecutableInvoker} will call the methods of this interface on a mocked implementation.
     * We can then verify that the methods were called with correctly resolved parameters.
     * See  <a href="https://github.com/junit-team/junit5/blob/3f7fed61f2edcee2a5551dbde75382b1dbe21267/junit-jupiter-engine/src/test/java/org/junit/jupiter/engine/execution/ExecutableInvokerTests.java#L45">ExecutableInvokerTests</a> for the original idea.
     */
    interface MethodSource {
        String METHOD_DEFINED_STRING_PROPERTY_ENV = "definedStringPropertyEnv";
        void definedStringPropertyEnv(@ConfigProperty(name = DEFINED_STRING_PROPERTY_ENV) String value);

        String METHOD_DEFINED_STRING_PROPERTY_SYS = "definedStringPropertySys";
        void definedStringPropertySys(@ConfigProperty(name = DEFINED_STRING_PROPERTY_SYS) String value);

        String METHOD_DEFINED_STRING_PROPERTY_FILE = "definedStringPropertyFile";
        void definedStringPropertyFile(@ConfigProperty(name = DEFINED_STRING_PROPERTY_FILE) String value);

        String METHOD_DEFINED_INTEGER_PROPERTY = "definedIntegerProperty";
        void definedIntegerProperty(@ConfigProperty(name = DEFINED_INTEGER_PROPERTY) Integer value);

        String METHOD_DEFINED_STRING_PROPERTY_WITH_DEFAULT = "definedStringPropertyWithDefault";
        void definedStringPropertyWithDefault(@ConfigProperty(name = DEFINED_STRING_PROPERTY_ENV, defaultValue = "default") String value);

        String METHOD_UNDEFINED_STRING_PROPERTY = "undefinedStringProperty";
        void undefinedStringProperty(@ConfigProperty(name = UNDEFINED_STRING_PROPERTY) String value);

        String METHOD_UNDEFINED_STRING_PROPERTY_WITH_DEFAULT = "undefinedStringPropertyWithDefault";
        void undefinedStringPropertyWithDefault(@ConfigProperty(name = UNDEFINED_STRING_PROPERTY, defaultValue = "default") String value);

        String METHOD_UNDEFINED_INTEGER_PROPERTY_WITH_DEFAULT = "undefinedIntegerPropertyWithDefault";
        void undefinedIntegerPropertyWithDefault(@ConfigProperty(name = UNDEFINED_INTEGER_PROPERTY, defaultValue = "-1") Integer value);
    }

    @Test
    void ensureUndefinedEnvVarsAreReallyUndefined() {
        assertNull(System.getenv(UNDEFINED_STRING_PROPERTY));
        assertNull(System.getenv(UNDEFINED_INTEGER_PROPERTY));
    }

    @Test
    void aDefinedStringPropertyIsResolvedFromEnvironment() {
        useTestMethod(METHOD_DEFINED_STRING_PROPERTY_ENV, String.class);
        addConfigResolver();

        invokeMethod();

        verify(instance).definedStringPropertyEnv("defined in env");
    }

    @Test
    void aDefinedStringPropertyIsResolvedFromSystemProperties() {
        useTestMethod(METHOD_DEFINED_STRING_PROPERTY_SYS, String.class);
        addConfigResolver();

        invokeMethod();

        verify(instance).definedStringPropertySys("defined in system properties");
    }

    @Test
    void aDefinedStringPropertyIsResolvedFromPropertyFile() {
        useTestMethod(METHOD_DEFINED_STRING_PROPERTY_FILE, String.class);
        addConfigResolver();

        invokeMethod();

        verify(instance).definedStringPropertyFile("defined in properties file");
    }

    @Test
    void aDefinedIntegerPropertyIsResolved() {
        useTestMethod(METHOD_DEFINED_INTEGER_PROPERTY, Integer.class);
        addConfigResolver();

        invokeMethod();

        verify(instance).definedIntegerProperty(42);
    }

    @Test
    void testsWithAnUndefinedStringPropertyCannotBeExecuted() {
        useTestMethod(METHOD_UNDEFINED_STRING_PROPERTY, String.class);
        addConfigResolver();

        Exception exception = assertThrows(ParameterResolutionException.class, this::invokeMethod);
        assertThat(exception.getCause(), isA(NoSuchElementException.class));
    }

    @Test
    void anUndefinedStringPropertyWithDefaultIsResolved() {
        useTestMethod(METHOD_UNDEFINED_STRING_PROPERTY_WITH_DEFAULT, String.class);
        addConfigResolver();

        invokeMethod();

        verify(instance).undefinedStringPropertyWithDefault("default");
    }

    @Test
    void anUndefinedIntegerPropertyWithDefaultIsResolved() {
        useTestMethod(METHOD_UNDEFINED_INTEGER_PROPERTY_WITH_DEFAULT, Integer.class);
        addConfigResolver();

        invokeMethod();

        verify(instance).undefinedIntegerPropertyWithDefault(-1);
    }

    @Test
    void aDefaultValueForADefinedStringPropertyIsIgnored() {
        useTestMethod(METHOD_DEFINED_STRING_PROPERTY_WITH_DEFAULT, String.class);
        addConfigResolver();

        invokeMethod();

        verify(instance).definedStringPropertyWithDefault("defined in env");
    }

    private void useTestMethod(String methodName, Class<?>... parameterTypes) {
        this.method = ReflectionUtils
                .findMethod(this.instance.getClass(), methodName, parameterTypes)
                .orElseThrow(IllegalStateException::new);
    }

    private void addConfigResolver() {
        extensionRegistry.registerExtension(new MicroProfileConfigExtension(), this);
    }

    private void invokeMethod() {
        new ExecutableInvoker().invoke(this.method, this.instance, this.extensionContext, this.extensionRegistry,
                passthroughInterceptor());
    }

    private static <E extends Executable, T> ReflectiveInterceptorCall<E, T> passthroughInterceptor() {
        return (interceptor, invocation, invocationContext, extensionContext) -> invocation.proceed();
    }
}
