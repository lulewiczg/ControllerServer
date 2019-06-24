package com.github.lulewiczg.controller.actions.processor.mouse;

import org.junit.Assert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.lulewiczg.controller.TestConfiguration;

/**
 * Tests WindowsSystemCondition class.
 *
 * @author Grzegurz
 *
 */
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = { TestConfiguration.class })
@EnableAutoConfiguration
public class WindowsSystemConditionTest {

    private static final String OS_NAME = "os.name";

    private static String oldName;

    @BeforeAll
    public static void beforeAll() {
        oldName = System.getProperty(OS_NAME);
    }

    @AfterAll
    public static void afterAll() {
        System.setProperty(OS_NAME, oldName);
    }

    @DisplayName("Test bean on Windows")
    @ParameterizedTest(name = "''{0}'' should be resolved as Windows'")
    @ValueSource(strings = { "Windows 10", "windows 8.1", "WINDOWS XD", "wInDoWs 69" })
    public void testOnWindows(String os) {
        System.setProperty(OS_NAME, os);
        Assert.assertTrue(new WindowsSystemCondition().matches(null, null));
    }

    @DisplayName("Test bean on other systems")
    @ParameterizedTest(name = "''{0}'' should not be resolved as Windows'")
    @ValueSource(strings = { "linux 3.14", "unix 123", "MAC X", "Android" })
    public void testOnOtherSystems(String os) {
        System.setProperty(OS_NAME, os);
        Assert.assertFalse(new WindowsSystemCondition().matches(null, null));
    }

}