package com.github.lulewiczg.controller.actions;

import com.github.lulewiczg.controller.MockPropertiesConfiguration;
import com.github.lulewiczg.controller.exception.AuthorizationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Spy;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests LoginRequiredAction.
 *
 * @author Grzegurz
 */
@ActiveProfiles("test")
@SpringBootTest(classes = { MockPropertiesConfiguration.class })
@EnableAutoConfiguration
class LoginRequiredActionTest {

    @Spy
    private LoginRequiredAction action;

    @Test
    @DisplayName("Action run in proper state")
    void testRunInProperState() {
        Exception e = assertThrows(AuthorizationException.class, action::doThrowException);
        assertThat(e.getMessage(),
                equalTo(String.format("Action %s requires login", action.getClass().getSimpleName())));

    }

}
