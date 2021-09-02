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
import pl.marcinchwedczuk.iunrar.gui.OpenFileEvents;
import pl.marcinchwedczuk.iunrar.gui.UiService;
import pl.marcinchwedczuk.iunrar.gui.aboutdialog.AboutDialog;
import pl.marcinchwedczuk.iunrar.gui.conflictdialog.ConflictDialog;
import pl.marcinchwedczuk.iunrar.gui.conflictdialog.GuiFileConflictResolutionProvider;
import pl.marcinchwedczuk.iunrar.gui.decompressionqueue.UnpackingQueueItem;
import pl.marcinchwedczuk.iunrar.gui.decompressionqueue.UnpackingQueueListViewCell;
import pl.marcinchwedczuk.iunrar.gui.decompressionqueue.NoSelectionModel;
import pl.marcinchwedczuk.iunrar.gui.passworddialog.GuiPasswordProvider;
import pl.marcinchwedczuk.iunrar.gui.settingsdialog.SettingsDialog;

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
    private ListView<UnpackingQueueItem> unpackingQueue;

    private final Executor unpackingExecutor;
    private final FileChooser openArchiveFileChooser = FileChoosers.newOpenArchiveFileChooser();

    public MainWindow(Executor unpackingExecutor) {
        this.unpackingExecutor = Objects.requireNonNull(unpackingExecutor);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        unpackingQueue.setSelectionModel(new NoSelectionModel<>());
        unpackingQueue.setCellFactory(UnpackingQueueListViewCell::newCell);

        // Start receiving events
        boolean ok = OpenFileEvents.INSTANCE.subscribe(file -> {
            startUnpacking(file);
        });
        if (!ok) {
            UiService.errorDialog(
                    "Cannot subscribe to macOS open file events.\n" +
                    "This app will be closed.");
            Platform.exit();
        }
    }

    @FXML
    private void guiOpenArchive() {
        File archive = openArchiveFileChooser.showOpenDialog(thisWindow());
        if (archive != null) {
            startUnpacking(archive);
        }
    }

    private void startUnpacking(File archive) {
        UnpackingQueueItem item = new UnpackingQueueItem(
                archive,
                new GuiFileConflictResolutionProvider(),
                new GuiPasswordProvider(thisWindow()));

        // Add as a first item
        unpackingQueue.getItems().add(0, item);
        unpackingExecutor.execute(item);
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
    private void guiOpenSettingsDialog() {
        SettingsDialog.show(thisWindow());
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
