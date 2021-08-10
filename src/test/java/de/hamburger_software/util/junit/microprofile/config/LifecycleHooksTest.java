package de.hamburger_software.util.junit.microprofile.config;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import static de.hamburger_software.util.junit.microprofile.config.PropertyNames.DEFINED_STRING_ENV;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * We verify that parameter resolution works for all required method types as documented in
 * https://junit.org/junit5/docs/current/api/org.junit.jupiter.api/org/junit/jupiter/api/extension/ParameterResolver.html
 */
@ExtendWith(MicroProfileConfigExtension.class)
class LifecycleHooksTest {

    LifecycleHooksTest(@ConfigProperty(name = DEFINED_STRING_ENV) String value) {
        assertNotNull(value);
    }

    @BeforeAll
    static void beforeAll(@ConfigProperty(name = DEFINED_STRING_ENV) String value) {
        assertNotNull(value);
    }

    @AfterAll
    static void afterAll(@ConfigProperty(name = DEFINED_STRING_ENV) String value) {
        assertNotNull(value);
    }

    @BeforeEach
    void beforeEach(@ConfigProperty(name = DEFINED_STRING_ENV) String value) {
        assertNotNull(value);
    }

    @AfterEach
    void afterEach(@ConfigProperty(name = DEFINED_STRING_ENV) String value) {
        assertNotNull(value);
    }

    @Test
    void dummyTestToTriggerLifecycle() {}
}
