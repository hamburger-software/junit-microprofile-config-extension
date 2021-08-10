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

import static de.hamburger_software.util.junit.microprofile.config.PropertyNames.DEFINED_STRING_ENV;
import static de.hamburger_software.util.junit.microprofile.config.PropertyNames.UNDEFINED_STRING;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Unresolved parameters prevent a test from running.
 * Therefore we have to test at framework level.
 *
 * See  <a href="https://github.com/junit-team/junit5/blob/3f7fed61f2edcee2a5551dbde75382b1dbe21267/junit-jupiter-engine/src/test/java/org/junit/jupiter/engine/execution/ExecutableInvokerTests.java#L45">ExecutableInvokerTests</a>
 * for the original idea.
 */
class DefinednessTest {

    private final MethodSource instance = mock(MethodSource.class);
    private final ExtensionContext extensionContext = mock(ExtensionContext.class);
    private final JupiterConfiguration configuration = mock(JupiterConfiguration.class);
    private final MutableExtensionRegistry extensionRegistry = MutableExtensionRegistry
            .createRegistryWithDefaultExtensions(configuration);

    private Method method;

    /**
     * An {@link ExecutableInvoker} will call the methods of this interface on a mocked implementation.
     * We can then verify that the methods were called with correctly resolved parameters.
     */
    interface MethodSource {
        void definedStringPropertyEnv(@ConfigProperty(name = DEFINED_STRING_ENV) String value);

        void definedStringPropertyWithDefault(@ConfigProperty(name = DEFINED_STRING_ENV, defaultValue = "default") String value);

        void undefinedStringProperty(@ConfigProperty(name = UNDEFINED_STRING) String value);

        void undefinedStringPropertyWithDefault(@ConfigProperty(name = UNDEFINED_STRING, defaultValue = "default") String value);
    }

    @Test
    void ensureUndefinedEnvVarIsReallyUnset() {
        assertNull(System.getenv(UNDEFINED_STRING));
        assertNull(System.getProperty(UNDEFINED_STRING));
    }

    @Test
    void aDefinedPropertyIsResolved() {
        useTestMethod("definedStringPropertyEnv");
        addConfigResolver();

        invokeMethod();

        verify(instance).definedStringPropertyEnv("defined in env");
    }

    @Test
    void testsWithAnUndefinedPropertyCannotBeExecuted() {
        useTestMethod("undefinedStringProperty");
        addConfigResolver();

        Exception exception = assertThrows(ParameterResolutionException.class, this::invokeMethod);
        assertThat(exception.getCause(), isA(NoSuchElementException.class));
    }

    @Test
    void anUndefinedStringPropertyWithDefaultIsResolved() {
        useTestMethod("undefinedStringPropertyWithDefault");
        addConfigResolver();

        invokeMethod();

        verify(instance).undefinedStringPropertyWithDefault("default");
    }

    @Test
    void aDefaultValueForADefinedStringPropertyIsIgnored() {
        useTestMethod("definedStringPropertyWithDefault");
        addConfigResolver();

        invokeMethod();

        verify(instance).definedStringPropertyWithDefault("defined in env");
    }

    private void useTestMethod(String methodName) {
        useTestMethod(methodName, String.class);
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
