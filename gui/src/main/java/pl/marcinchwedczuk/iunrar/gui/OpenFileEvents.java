package pl.marcinchwedczuk.iunrar.gui;

import javafx.application.Platform;

import java.awt.*;
import java.io.File;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

public class OpenFileEvents {
    public static final OpenFileEvents INSTANCE = new OpenFileEvents();

    private final ConcurrentLinkedQueue<File> preJavaFxEvents = new ConcurrentLinkedQueue<>();
    private volatile boolean disablePreJavaFxHandler = false;

    /**
     * Must be called before JavaFX is initialized.
     */
    public void saveExistingEvents() {
        if(!Desktop.isDesktopSupported()) {
            // Running in headless mode e.g. on CI
            return;
        }

        try {
            Desktop.getDesktop().setOpenFileHandler(event -> {
                if (disablePreJavaFxHandler) {
                    return;
                }

                for (File file : event.getFiles()) {
                    preJavaFxEvents.offer(file);
                }
            });
        } catch (Exception e) {
            // Actually we will handle error later when JavaFX is properly bootstrapped.
            e.printStackTrace();
        }
    }

    public boolean subscribe(Consumer<File> handler) {
        if(!Desktop.isDesktopSupported()) {
            // Running in headless mode e.g. on CI
            return "true".equals(System.getProperty("java.awt.headless"));
        }

        if (!java.awt.Desktop.getDesktop().isSupported(Desktop.Action.APP_OPEN_FILE)) {
            return false;
        }

        Desktop.getDesktop().setOpenFileHandler(event -> {
            for(File f: event.getFiles()) {
                Platform.runLater(() -> {
                    handler.accept(f);
                });
            }
        });

        disablePreJavaFxHandler = true;

        for (File f: preJavaFxEvents) {
            Platform.runLater(() -> {
                handler.accept(f);
            });
        }

        return true;
    }

}
