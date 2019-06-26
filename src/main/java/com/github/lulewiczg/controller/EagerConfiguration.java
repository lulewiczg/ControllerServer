package com.github.lulewiczg.controller;

import java.awt.Font;

import javax.swing.JTextArea;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Eager application beans.
 *
 * @author Grzegurz
 */
@Configuration
public class EagerConfiguration {

    @Bean
    public JTextArea logTextArea() {
        JTextArea jTextArea = new JTextArea();
        jTextArea.setEditable(false);
        jTextArea.setOpaque(false);
        jTextArea.setFont(new Font("Arial", 0, 11));
        return jTextArea;
    }

}
