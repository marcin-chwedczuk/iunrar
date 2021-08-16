package pl.marcinchwedczuk.iunrar.gui;

import javafx.application.Application;

import java.awt.*;
import java.io.File;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Taken from: https://github.com/eschmar/javafx-custom-file-ext-boilerplate/blob/master/src/main/java/com/example/pew/Launcher.java
 *
 * ORIGINAL COMMENT:
 * Minimal launcher class to catch the initial FILE_OPEN events on macOS,
 * before the application is set up.
 *
 * **ATTENTION**: This class is currently a workaround. While it allows to catch
 * initial FILE_OPEN events, using it and launching JavaFX over the Main method
 * means that AWT is the main GUI toolkit. The system menu bar can
 * no longer be used on mac os as a consequence.
 *
 * https://bugs.openjdk.java.net/browse/JDK-8095227
 * https://bugs.openjdk.java.net/browse/JDK-8239590
 * https://bugs.openjdk.java.net/browse/JDK-8208652
 */
public class Launcher {
    static {
        OpenFileEvents.INSTANCE.saveExistingEvents();
    }

    public static void main(String[] args) {
        Application.launch(App.class, args);
        Runtime.getRuntime().exit(0);
    }
}
