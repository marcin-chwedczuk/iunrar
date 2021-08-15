package pl.marcinchwedczuk.iunrar.gui;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.stage.Stage;
import pl.marcinchwedczuk.iunrar.gui.mainwindow.MainWindow;

import java.util.Arrays;
import java.util.concurrent.Executors;

/**
 * JavaFX App
 */
public class App extends Application {
    public static boolean testMode = false;

    private static String[] args = new String[] { "uninitialized args" };
    public static String[] args() {
        return args.clone();
    }

    private static HostServices hostServices = null;
    public static HostServices hostServices() {
        if (hostServices == null) {
            throw new IllegalStateException();
        }
        return hostServices;
    }

    @Override
    public void start(Stage stage) {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            e.printStackTrace();
            showExceptionDialog(e);
        });

        App.hostServices = this.getHostServices();
        MainWindow.showOn(stage);
    }

    private void showExceptionDialog(Throwable e) {
        StringBuilder msg = new StringBuilder();
        msg.append("Unhandled exception:\n");

        Throwable curr = e;
        while (curr != null) {
            if (curr.getMessage() != null && !curr.getMessage().isBlank()) {
                msg.append(curr.getClass().getSimpleName()).append(": ")
                        .append(curr.getMessage())
                        .append("\n");
            }
            curr = curr.getCause();
        }

        UiService.errorDialog(msg.toString());
    }

    static void main(String[] args) {
        App.args = args;

        launch();
    }
}