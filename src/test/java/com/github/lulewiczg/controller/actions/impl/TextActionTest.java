package com.github.lulewiczg.controller.actions.impl;

import static org.junit.Assert.assertThat;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;

import org.hamcrest.Matchers;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

import com.github.lulewiczg.controller.actions.Action;
import com.github.lulewiczg.controller.exception.AuthorizationException;

/**
 * Tests TextAction.
 *
 * @author Grzegurz
 */
@EnableAutoConfiguration
public class TextActionTest extends ActionTestTemplate {

    private static final String TXT = "test txt";

    @Override
    protected Action getAction() {
        return new TextAction(TXT);
    }

    @Override
    protected void doTestInWaiting() throws Exception {
        processor.processAction(server);
        Mockito.verify(clipboard, Mockito.never()).setContents(Mockito.any(), Mockito.any());
        Mockito.verify(robot, Mockito.never()).keyPress(KeyEvent.VK_CONTROL);
        Mockito.verify(robot, Mockito.never()).keyPress(KeyEvent.VK_V);
        Mockito.verify(robot, Mockito.never()).keyRelease(KeyEvent.VK_V);
        Mockito.verify(robot, Mockito.never()).keyRelease(KeyEvent.VK_CONTROL);
        assertStatusNotOK(AuthorizationException.class);
    }

    @Override
    protected void doTestInConencted() throws Exception {
        processor.processAction(server);

        ArgumentCaptor<StringSelection> argument = ArgumentCaptor.forClass(StringSelection.class);
        InOrder inOrder = Mockito.inOrder(clipboard, robot);
        inOrder.verify(clipboard).setContents(argument.capture(), Mockito.eq(null));
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
        Mockito.verify(clipboard, Mockito.never()).setContents(Mockito.any(), Mockito.any());
        Mockito.verify(robot, Mockito.never()).keyPress(KeyEvent.VK_CONTROL);
        Mockito.verify(robot, Mockito.never()).keyPress(KeyEvent.VK_V);
        Mockito.verify(robot, Mockito.never()).keyRelease(KeyEvent.VK_V);
        Mockito.verify(robot, Mockito.never()).keyRelease(KeyEvent.VK_CONTROL);
        assertStatusNotOK(AuthorizationException.class);
    }
}
