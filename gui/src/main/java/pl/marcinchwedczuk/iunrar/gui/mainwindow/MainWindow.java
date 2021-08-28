package pl.marcinchwedczuk.iunrar.gui.mainwindow;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuBar;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import pl.marcinchwedczuk.iunrar.gui.AppPreferences;
import pl.marcinchwedczuk.iunrar.gui.OpenFileEvents;
import pl.marcinchwedczuk.iunrar.gui.UiService;
import pl.marcinchwedczuk.iunrar.gui.aboutdialog.AboutDialog;
import pl.marcinchwedczuk.iunrar.gui.conflictdialog.ConflictDialog;
import pl.marcinchwedczuk.iunrar.gui.conflictdialog.GuiFileConflictResolutionProvider;
import pl.marcinchwedczuk.iunrar.gui.decompressionqueue.DecompressionQueueItem;
import pl.marcinchwedczuk.iunrar.gui.decompressionqueue.DecompressionQueueListViewCell;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.Executor;

public class MainWindow implements Initializable {

    public static MainWindow show(Stage window, Executor decompressionExecutor) {
        try {
            FXMLLoader loader = new FXMLLoader(MainWindow.class.getResource("MainWindow.fxml"));

            MainWindow controller = new MainWindow(decompressionExecutor);
            loader.setController(controller);

            Scene scene = new Scene(loader.load());

            window.setTitle("iunrar");
            window.setScene(scene);
            window.setResizable(true);

            window.show();

            return controller;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private MenuBar mainMenu;

    @FXML
    private ListView<DecompressionQueueItem> decompressionQueue;

    private final Executor decompressionExecutor;
    private final FileChooser openArchiveFileChooser = FileChoosers.newOpenArchiveFileChooser();

    public MainWindow(Executor decompressionExecutor) {
        this.decompressionExecutor = Objects.requireNonNull(decompressionExecutor);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        decompressionQueue.setCellFactory(DecompressionQueueListViewCell::newCell);

        for (DecompressionQueueItem item : decompressionQueue.getItems()) {
            decompressionExecutor.execute(item);
        }

        System.out.println(new AppPreferences().getOpenFolderAfterDecompression());
        new AppPreferences().setOpenFolderAfterDecompression(false);

        // Start receiving events
        boolean ok = OpenFileEvents.INSTANCE.subscribe(file -> {
        });
        if (!ok) {
            UiService.errorDialog("Error: Cannot subscribe to macOS open file events.");
            Platform.exit();
        }
    }

    @FXML
    private void guiOpenArchive() {
        File archive = openArchiveFileChooser.showOpenDialog(thisWindow());
        if (archive != null) {
            DecompressionQueueItem item = new DecompressionQueueItem(
                    archive,
                    new GuiFileConflictResolutionProvider());

            decompressionQueue.getItems().add(item);
            decompressionExecutor.execute(item);
        }
    }

    @FXML
    private void guiCancelAll() {

    }

    @FXML
    private void guiPauseAll() {

    }

    @FXML
    private void testShowConflict() {
        ConflictDialog.showAndWaitForAnswer("archive.rar", "foo", 10, 20);
    }

    @FXML
    private void guiShowAbout() {
        AboutDialog.show(thisWindow());
    }

    @FXML
    private void guiClose() {
        thisWindow().close();
    }

    private Stage thisWindow() {
        return (Stage)this.mainMenu.getScene().getWindow();
    }
}
