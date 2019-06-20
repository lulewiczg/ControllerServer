package com.github.lulewiczg.controller.actions.processor;

import java.awt.Robot;
import java.awt.datatransfer.Clipboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.lulewiczg.controller.actions.MouseMovingService;

/**
 * Service for controlling PC.
 *
 * @author Grzegurz
 */
@Service
public class ControllingService {

    @Autowired
    private MouseMovingService mouseService;

    @Autowired
    private Robot robot;

    @Autowired
    private Clipboard clipboard;

    public MouseMovingService getMouseService() {
        return mouseService;
    }

    public Robot getRobot() {
        return robot;
    }

    public Clipboard getClipboard() {
        return clipboard;
    }

}