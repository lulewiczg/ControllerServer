package com.github.lulewiczg.controller.actions.impl;

import com.github.lulewiczg.controller.actions.Action;
import com.github.lulewiczg.controller.exception.AuthorizationException;
import org.hamcrest.Matchers;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;

import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests TextAction.
 *
 * @author Grzegurz
 */
@EnableAutoConfiguration
class TextActionTest extends ActionTestTemplate {

    private static final String TXT = "test txt";

    @Override
    protected Action getAction() {
        return new TextAction(TXT);
    }

    @Override
    protected void doTestInWaiting() throws Exception {
        processor.processAction(server);
        verify(clipboard, never()).setContents(any(), any());
        verify(robot, never()).keyPress(KeyEvent.VK_CONTROL);
        verify(robot, never()).keyPress(KeyEvent.VK_V);
        verify(robot, never()).keyRelease(KeyEvent.VK_V);
        verify(robot, never()).keyRelease(KeyEvent.VK_CONTROL);
        assertStatusNotOK(AuthorizationException.class);
    }

    @Override
    protected void doTestInConnected() throws Exception {
        processor.processAction(server);

        ArgumentCaptor<StringSelection> argument = ArgumentCaptor.forClass(StringSelection.class);
        InOrder inOrder = inOrder(clipboard, robot);
        inOrder.verify(clipboard).setContents(argument.capture(), eq(null));
        assertThat(argument.getValue().getTransferData(DataFlavor.stringFlavor),
                Matchers.equalTo(new StringSelection(TXT).getTransferData(DataFlavor.stringFlavor)));
        inOrder.verify(robot).keyPress(KeyEvent.VK_CONTROL);
        inOrder.verify(robot).keyPress(KeyEvent.VK_V);
        inOrder.verify(robot).keyRelease(KeyEvent.VK_V);
        inOrder.verify(robot).keyRelease(KeyEvent.VK_CONTROL);
        assertStatusOK();
    }

    @Override
    protected void doTestInShutdown() throws Exception {
        processor.processAction(server);
        verify(clipboard, never()).setContents(any(), any());
        verify(robot, never()).keyPress(KeyEvent.VK_CONTROL);
        verify(robot, never()).keyPress(KeyEvent.VK_V);
        verify(robot, never()).keyRelease(KeyEvent.VK_V);
        verify(robot, never()).keyRelease(KeyEvent.VK_CONTROL);
        assertStatusNotOK(AuthorizationException.class);
    }
}
