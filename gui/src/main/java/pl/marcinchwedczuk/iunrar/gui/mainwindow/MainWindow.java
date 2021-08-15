package pl.marcinchwedczuk.iunrar.gui.mainwindow;

import com.github.junrar.Junrar;
import com.google.common.io.Files;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import pl.marcinchwedczuk.iunrar.gui.App;
import pl.marcinchwedczuk.iunrar.gui.Launcher;
import pl.marcinchwedczuk.iunrar.gui.OsUtils;
import pl.marcinchwedczuk.iunrar.gui.aboutdialog.AboutDialog;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MainWindow implements Initializable {

    public static MainWindow showOn(Stage window) {
        try {
            FXMLLoader loader = new FXMLLoader(MainWindow.class.getResource("MainWindow.fxml"));

            Scene scene = new Scene(loader.load());
            MainWindow controller = (MainWindow) loader.getController();

            window.setTitle("iunrar");
            window.setScene(scene);
            window.setResizable(true);

            window.show();

            return controller;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /** Queue of files to open */
    private final ConcurrentLinkedQueue<File> fileQueue = new ConcurrentLinkedQueue<>();

    /**
     * Parses path to a file and enqueues the file, if valid.
     *
     * @param path Path to local file.
     */
    public void enqueueFile(String path) {
        try {
            final File file = new File(path);
            fileQueue.add(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private MenuBar mainMenu;

    @FXML
    private MenuItem closeMenuItem;

    @FXML
    private TextArea operationLog;

    private final FileChooser openArchiveFileChooser = FileChoosers.newOpenArchiveFileChooser();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        /* After using getDesktop().setOpenFileHandler menu is broken.

        if (OsUtils.isMac() && !App.testMode) {
            mainMenu.setUseSystemMenuBar(true);
            // MacOS will add Quit menu entry automatically
            removeMenuItem(closeMenuItem);
        }
         */

        if (java.awt.Desktop.getDesktop().isSupported(Desktop.Action.APP_OPEN_FILE)) {
            Desktop.getDesktop().setOpenFileHandler(event -> {
                for (File file : event.getFiles()) enqueueFile(file.getAbsolutePath());
                Platform.runLater(() -> {
                    while (fileQueue.size() > 0) {
                        File f = fileQueue.poll();
                        operationLog.appendText(f.toString() + "\n");
                    }
                });
            });
        }

        operationLog.appendText("FILE EVENTS:\n");
        for (var arg: Launcher.cachedFileOpenEvents) {
            operationLog.appendText(arg);
            operationLog.appendText("\n");
        }
    }

    @FXML
    private void guiOpenArchive() {
        File archive = openArchiveFileChooser.showOpenDialog(thisWindow());
        if (archive != null) {
            try {
                // TODO: Fix shitty logic here
                File destination = new File(archive.getAbsolutePath().replace(".rar", ""));
                if (!destination.mkdir()) {
                    throw new RuntimeException("Cannot create directory: " + destination);
                }

                Junrar.extract(archive, destination);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void guiShowAbout() {
        AboutDialog.show(thisWindow());
    }

    @FXML
    private void guiClose() {
        thisWindow().close();
    }

    private static void removeMenuItem(MenuItem menuItem) {
        menuItem.getParentMenu().getItems().remove(menuItem);
    }

    private Stage thisWindow() {
        return (Stage)this.mainMenu.getScene().getWindow();
    }
}
