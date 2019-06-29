package com.github.lulewiczg.controller.actions;

import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.lulewiczg.controller.MockPropertiesConfiguration;
import com.github.lulewiczg.controller.exception.AuthorizationException;

/**
 * Tests LoginRequiredAction.
 *
 * @author Grzegurz
 */
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = { MockPropertiesConfiguration.class })
@EnableAutoConfiguration
public class LoginRequiredActionTest {

    @Spy
    private LoginRequiredAction action;

    @Test
    @DisplayName("Action run in proper state")
    public void testRunInProperState() throws Exception {
        Exception e = assertThrows(AuthorizationException.class, () -> action.doThrowException());
        assertThat(e.getMessage(),
                Matchers.equalTo(String.format("Action %s requires login", action.getClass().getSimpleName())));

    }

}
