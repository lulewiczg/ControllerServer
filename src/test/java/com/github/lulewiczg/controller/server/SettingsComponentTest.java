package com.github.lulewiczg.controller.server;

import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.logging.log4j.Level;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.lulewiczg.controller.LightTestConfiguration;

/**
 * Tests SettingsComponent class.
 *
 * @author Grzegurz
 *
 */
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = { LightTestConfiguration.class })
@EnableAutoConfiguration
public class SettingsComponentTest {

    private static final String PORT = "com.github.lulewiczg.setting.port";

    private static final String PASSWORD = "com.github.lulewiczg.setting.password";

    private static final String LOG_LEVEL = "logging.level.com.github.lulewiczg";

    private static final String AUTOSTART = "com.github.lulewiczg.setting.autostart";

    private static final String TEST = "test";

    @Autowired
    private SettingsComponent settings;

    @Value("${com.github.lulewiczg.setting.userFile}")
    private String propsFile;

    @Autowired
    @Qualifier("userProperties")
    private Properties properties;

    @BeforeEach
    public void before() throws IOException {
        properties.clear();
        createProps();
    }

    @AfterEach
    public void after() {
        removeIfExists();
    }

    @Test
    @DisplayName("Save empty properties when file is missing")
    public void testSaveEmptyNoUserFile() throws Exception {
        removeIfExists();

        settings.saveSettings();

        assertThat(new File(propsFile).exists(), Matchers.equalTo(false));
    }

    @Test
    @DisplayName("Save empty properties when file is empty")
    public void testSaveEmptyEmptyUserFile() throws Exception {
        settings.saveSettings();

        Properties saved = loadProps();
        assertThat(saved.isEmpty(), Matchers.equalTo(true));
    }

    @Test
    @DisplayName("Save empty properties")
    public void testSaveEmpty() throws Exception {
        properties.setProperty(TEST, TEST);

        settings.saveSettings();

        Properties saved = loadProps();
        assertThat(saved.size(), Matchers.equalTo(1));
        assertThat(saved.getProperty(TEST), Matchers.equalTo(TEST));
    }

    @Test
    @DisplayName("Save properties when file is missing")
    public void testSaveModifiedNoFile() throws Exception {
        settings.setAutostart(true);
        settings.setLogLevel(Level.FATAL);
        settings.setPassword("pwd");
        settings.setPort(11111);

        settings.saveSettings();

        Properties saved = loadProps();
        assertThat(saved.size(), Matchers.equalTo(4));
        assertThat(saved.getProperty(AUTOSTART), Matchers.equalTo("true"));
        assertThat(saved.getProperty(LOG_LEVEL), Matchers.equalTo(Level.FATAL.toString()));
        assertThat(saved.getProperty(PASSWORD), Matchers.equalTo("pwd"));
        assertThat(saved.getProperty(PORT), Matchers.equalTo("11111"));
    }

    @Test
    @DisplayName("Modify properties but don't save")
    public void testNoSave() throws Exception {
        removeIfExists();
        settings.setAutostart(true);
        settings.setLogLevel(Level.FATAL);
        settings.setPassword("pwd");
        settings.setPort(11111);

        assertThat(new File(propsFile).exists(), Matchers.equalTo(false));
    }

    @Test
    @DisplayName("Save properties when propeties are not empty")
    public void testSaveModifiedAndOld() throws Exception {
        removeIfExists();
        properties.setProperty(TEST, TEST);
        settings.setAutostart(true);
        settings.setLogLevel(Level.FATAL);
        settings.setPassword("pwd");
        settings.setPort(11111);

        settings.saveSettings();

        Properties saved = loadProps();
        assertThat(saved.size(), Matchers.equalTo(5));
        assertThat(saved.getProperty(AUTOSTART), Matchers.equalTo("true"));
        assertThat(saved.getProperty(LOG_LEVEL), Matchers.equalTo(Level.FATAL.toString()));
        assertThat(saved.getProperty(PASSWORD), Matchers.equalTo("pwd"));
        assertThat(saved.getProperty(PORT), Matchers.equalTo("11111"));
        assertThat(saved.getProperty(TEST), Matchers.equalTo(TEST));
    }

    @Test
    @DisplayName("Modify existing property")
    public void testModifyProperty() throws Exception {
        removeIfExists();
        properties.setProperty(PORT, "123");
        settings.setPort(11111);

        settings.saveSettings();

        Properties saved = loadProps();
        assertThat(saved.size(), Matchers.equalTo(1));
        assertThat(saved.getProperty(PORT), Matchers.equalTo("11111"));
    }

    @Test
    @DisplayName("Properties are not saved when there is nothing to save")
    public void testSaveNoProps() throws Exception {
        removeIfExists();

        settings.saveSettings();

        assertThat(new File(propsFile).exists(), Matchers.equalTo(false));
    }

    /**
     * Loads saved properties.
     *
     * @return properties
     * @throws IOException
     *             the IOException
     */
    private Properties loadProps() throws IOException {
        assertThat(new File(propsFile).exists(), Matchers.equalTo(true));
        Properties saved = new Properties();
        FileInputStream inStream = new FileInputStream(propsFile);
        try (inStream) {
            saved.load(inStream);
        }
        return saved;
    }

    /**
     * Removes user properties files.
     */
    private void removeIfExists() {
        File file = new File(propsFile);
        if (file.exists()) {
            assertThat("Could not delete props file", file.delete(), Matchers.equalTo(true));
        }
    }

    /**
     * Removes user properties files.
     *
     * @throws IOException
     *             the IOException
     */
    private void createProps() throws IOException {
        removeIfExists();
        new File(propsFile).createNewFile();
    }

}
