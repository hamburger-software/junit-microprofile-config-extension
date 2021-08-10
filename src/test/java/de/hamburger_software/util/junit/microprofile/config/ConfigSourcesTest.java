package de.hamburger_software.util.junit.microprofile.config;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static de.hamburger_software.util.junit.microprofile.config.PropertyNames.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * We make sure that the default configuration sources as defined in
 * https://github.com/eclipse/microprofile-config#design work in the context
 * of this extension.
 */
@ExtendWith(MicroProfileConfigExtension.class)
class ConfigSourcesTest {

    @Test
    void systemProperty(@ConfigProperty(name = DEFINED_STRING_ENV) String value) {
        assertEquals("defined in env", value);
    }

    @Test
    void environmentVariable(@ConfigProperty(name = DEFINED_STRING_SYS) String value) {
        assertEquals("defined in system properties", value);
    }

    @Test
    void propertyFile(@ConfigProperty(name = DEFINED_STRING_FILE) String value) {
        assertEquals("defined in properties file", value);
    }
}
