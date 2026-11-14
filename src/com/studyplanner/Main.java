package com.studyplanner;

import com.studyplanner.app.Application;
import com.studyplanner.ui.MainFrame;

import java.awt.EventQueue;

/**
 * Application Entry Point.
 * Keeps Main minimal, enables text anti-aliasing hints, and delegates bootstrap to Application.
 */
public class Main {
    public static void main(String[] args) {
        // High-DPI font rendering flags for standard Java AWT
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        EventQueue.invokeLater(() -> {
            try {
                // Initialize services and load local persistence
                Application.getInstance().init();

                // Launch main desktop frame
                MainFrame frame = new MainFrame();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
