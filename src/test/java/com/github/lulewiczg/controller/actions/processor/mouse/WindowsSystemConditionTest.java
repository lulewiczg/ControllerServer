package com.github.lulewiczg.controller.actions.processor.mouse;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests WindowsSystemCondition class.
 *
 * @author Grzegurz
 *
 */
@ActiveProfiles("test")
@EnableAutoConfiguration
class WindowsSystemConditionTest {

    private static final String OS_NAME = "os.name";

    private static String oldName;

    @BeforeAll
    static void beforeAll() {
        oldName = System.getProperty(OS_NAME);
    }

    @AfterAll
    static void afterAll() {
        System.setProperty(OS_NAME, oldName);
    }

    @DisplayName("Test bean on Windows")
    @ParameterizedTest(name = "''{0}'' should be resolved as Windows'")
    @ValueSource(strings = { "Windows 10", "windows 8.1", "WINDOWS XD", "wInDoWs 69" })
    void testOnWindows(String os) {
        System.setProperty(OS_NAME, os);
        assertTrue(new WindowsSystemCondition().matches(null, null));
    }

    @DisplayName("Test bean on other systems")
    @ParameterizedTest(name = "''{0}'' should not be resolved as Windows'")
    @ValueSource(strings = { "linux 3.14", "unix 123", "MAC X", "Android" })
    void testOnOtherSystems(String os) {
        System.setProperty(OS_NAME, os);
        assertFalse(new WindowsSystemCondition().matches(null, null));
    }

}
