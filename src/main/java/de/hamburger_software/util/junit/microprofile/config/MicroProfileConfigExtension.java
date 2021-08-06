package de.hamburger_software.util.junit.microprofile.config;

import io.smallrye.config.SmallRyeConfig;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.util.Optional;

import static org.eclipse.microprofile.config.inject.ConfigProperty.UNCONFIGURED_VALUE;

/**
 * A {@link ParameterResolver} for MicroProfile Config-API's {@link ConfigProperty} annotation.
 */
public class MicroProfileConfigExtension implements ParameterResolver {
    private final Config config = ConfigProvider.getConfig();

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.findAnnotation(ConfigProperty.class).isPresent();
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        ConfigProperty configProperty = parameterContext.findAnnotation(ConfigProperty.class).orElseThrow(IllegalStateException::new);

        String key = configProperty.name();
        String defaultValue = configProperty.defaultValue();
        Class<?> type = parameterContext.getParameter().getType();

        return resolveWithMicroProfileConfig(key, defaultValue, type);
    }

    public <T> T resolveWithMicroProfileConfig(String key, String defaultValue, Class<T> type) {
        if (defaultValue.equals(UNCONFIGURED_VALUE)) {
            return config.getValue(key, type);
        } else {
            Optional<T> optionalValue = config.getOptionalValue(key, type);
            return optionalValue.orElseGet(
                    () -> ((SmallRyeConfig) config).convert(defaultValue, type));
        }
    }
}
