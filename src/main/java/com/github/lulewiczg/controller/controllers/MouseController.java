package com.github.lulewiczg.controller.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.lulewiczg.controller.actions.processor.ControllingService;

@RestController
@RequestMapping("/rest/mouse")
public class MouseController {

    @Autowired
    private ControllingService service;

    @PostMapping("move")
    public void move(long x, long y) {
        System.out.println(x + "  " + y);
        service.getMouseService().move(x, y);
    }

    @PostMapping("press")
    public void press(int key) {
        service.getRobot().mousePress(key);
    }

    @PostMapping("release")
    public void release(int key) {
        service.getRobot().mouseRelease(key);
    }

}
