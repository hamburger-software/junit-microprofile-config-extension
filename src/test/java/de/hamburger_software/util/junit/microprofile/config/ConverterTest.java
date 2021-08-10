package de.hamburger_software.util.junit.microprofile.config;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * We ensure that {@link org.eclipse.microprofile.config.spi.Converter}s work
 * in the context of this extension.
 */
@ExtendWith(MicroProfileConfigExtension.class)
class ConverterTest {

    @Test
    void integerParameter(@ConfigProperty(name = PropertyNames.DEFINED_INTEGER_ENV) Integer value) {
        assertEquals(42, value);
    }
}
