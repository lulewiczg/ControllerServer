package com.github.lulewiczg.controller.server;

import com.github.lulewiczg.controller.MockUtilConfiguration;
import com.github.lulewiczg.controller.TestPropertiesConfiguration;
import com.github.lulewiczg.controller.actions.processor.connection.JsonClientConnection;
import org.apache.logging.log4j.Level;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import static org.junit.Assert.assertThat;

/**
 * Tests SettingsComponent class.
 *
 * @author Grzegurz
 *
 */
@ActiveProfiles("test")
@SpringBootTest(classes = { TestPropertiesConfiguration.class, MockUtilConfiguration.class })
@EnableAutoConfiguration
class SettingsComponentTest {

    private static final String PORT = "com.github.lulewiczg.setting.port";

    private static final String PASSWORD = "com.github.lulewiczg.setting.password";

    private static final String TIMEOUT = "com.github.lulewiczg.setting.serverTimeout";

    private static final String LOG_LEVEL = "com.github.lulewiczg.setting.logLevel";

    private static final String AUTOSTART = "com.github.lulewiczg.setting.autostart";

    private static final String CONNECTION_TYPE = "com.github.lulewiczg.setting.connectionType";

    private static final String TEST = "test";

    @Autowired
    private SettingsComponent settings;

    @Value("${com.github.lulewiczg.setting.userFile}")
    private String propsFile;

    @Autowired
    @Qualifier("userProperties")
    private Properties properties;

    @BeforeEach
    void before() throws IOException {
        properties.clear();
        createProps();
    }

    @AfterEach
    void after() {
        removeIfExists();
    }

    @Test
    @DisplayName("Save empty properties when file is missing")
    void testSaveEmptyNoUserFile() throws Exception {
        removeIfExists();

        settings.saveSettings();

        assertThat(new File(propsFile).exists(), Matchers.equalTo(false));
    }

    @Test
    @DisplayName("Save empty properties when file is empty")
    void testSaveEmptyEmptyUserFile() throws Exception {
        settings.saveSettings();

        Properties saved = loadProps();
        assertThat(saved.isEmpty(), Matchers.equalTo(true));
    }

    @Test
    @DisplayName("Save other property")
    void testSaveOtherProperty() throws Exception {
        properties.setProperty(TEST, TEST);

        settings.saveSettings();

        Properties saved = loadProps();
        assertThat(saved.size(), Matchers.equalTo(1));
        assertThat(saved.getProperty(TEST), Matchers.equalTo(TEST));
    }

    @Test
    @DisplayName("Save properties when file is missing")
    void testSaveModifiedNoFile() throws Exception {
        settings.setAutostart(true);
        settings.setLogLevel(Level.FATAL);
        settings.setPassword("pwd");
        settings.setPort(11111);
        settings.setTimeout(1122);
        settings.setConnectionType(JsonClientConnection.NAME);


        settings.saveSettings();

        Properties saved = loadProps();
        assertThat(saved.size(), Matchers.equalTo(6));
        assertThat(saved.getProperty(AUTOSTART), Matchers.equalTo("true"));
        assertThat(saved.getProperty(LOG_LEVEL), Matchers.equalTo(Level.FATAL.toString()));
        assertThat(saved.getProperty(PASSWORD), Matchers.equalTo("pwd"));
        assertThat(saved.getProperty(PORT), Matchers.equalTo("11111"));
        assertThat(saved.getProperty(TIMEOUT), Matchers.equalTo("1122"));
        assertThat(saved.getProperty(CONNECTION_TYPE), Matchers.equalTo(JsonClientConnection.NAME));
    }

    @Test
    @DisplayName("Modify properties but don't save")
    void testNoSave() {
        removeIfExists();
        settings.setAutostart(true);
        settings.setLogLevel(Level.FATAL);
        settings.setPassword("pwd");
        settings.setPort(11111);
        settings.setTimeout(1122);
        settings.setConnectionType(JsonClientConnection.NAME);

        assertThat(new File(propsFile).exists(), Matchers.equalTo(false));
    }

    @Test
    @DisplayName("Save properties when propeties are not empty")
    void testSaveModifiedAndOld() throws Exception {
        removeIfExists();
        properties.setProperty(TEST, TEST);
        settings.setAutostart(true);
        settings.setLogLevel(Level.FATAL);
        settings.setPassword("pwd");
        settings.setPort(11111);
        settings.setTimeout(1122);
        settings.setConnectionType(JsonClientConnection.NAME);

        settings.saveSettings();

        Properties saved = loadProps();
        assertThat(saved.size(), Matchers.equalTo(7));
        assertThat(saved.getProperty(AUTOSTART), Matchers.equalTo("true"));
        assertThat(saved.getProperty(LOG_LEVEL), Matchers.equalTo(Level.FATAL.toString()));
        assertThat(saved.getProperty(PASSWORD), Matchers.equalTo("pwd"));
        assertThat(saved.getProperty(PORT), Matchers.equalTo("11111"));
        assertThat(saved.getProperty(TEST), Matchers.equalTo(TEST));
        assertThat(saved.getProperty(TIMEOUT), Matchers.equalTo("1122"));
        assertThat(saved.getProperty(CONNECTION_TYPE), Matchers.equalTo(JsonClientConnection.NAME));
    }

    @Test
    @DisplayName("Modify existing property")
    void testModifyProperty() throws Exception {
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
    void testSaveNoProps() {
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
