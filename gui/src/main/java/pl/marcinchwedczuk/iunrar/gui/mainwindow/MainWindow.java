package pl.marcinchwedczuk.iunrar.gui.mainwindow;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ObservableBooleanValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuBar;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import pl.marcinchwedczuk.iunrar.gui.OpenFileEventTime;
import pl.marcinchwedczuk.iunrar.gui.OpenFileEvents;
import pl.marcinchwedczuk.iunrar.gui.UiService;
import pl.marcinchwedczuk.iunrar.gui.aboutdialog.AboutDialog;
import pl.marcinchwedczuk.iunrar.gui.conflictdialog.GuiFileConflictResolutionProvider;
import pl.marcinchwedczuk.iunrar.gui.unpackingqueue.UnpackingQueueItem;
import pl.marcinchwedczuk.iunrar.gui.unpackingqueue.UnpackingQueueListViewCell;
import pl.marcinchwedczuk.iunrar.gui.unpackingqueue.NoSelectionModel;
import pl.marcinchwedczuk.iunrar.gui.passworddialog.GuiPasswordProvider;
import pl.marcinchwedczuk.iunrar.gui.settingsdialog.SettingsDialog;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
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

    @FXML
    private Button pauseButton;
    @FXML
    private Button resumeButton;
    @FXML
    private Button cancelButton;

    private final Executor unpackingExecutor;
    private final FileChooser openArchiveFileChooser = FileChoosers.newOpenArchiveFileChooser();

    private final Timer autocloseTimer = new Timer("autoclose-timer", true);

    public MainWindow(Executor unpackingExecutor) {
        this.unpackingExecutor = Objects.requireNonNull(unpackingExecutor);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        unpackingQueue.setSelectionModel(new NoSelectionModel<>());
        unpackingQueue.setCellFactory(UnpackingQueueListViewCell::newCell);

        pauseButton.setDisable(true);
        resumeButton.setDisable(true);
        cancelButton.setDisable(true);

        unpackingQueue.getItems()
                .addListener((InvalidationListener) observable -> {
                    // TODO: Can be abstracted
                    {
                        var canPauseProperties = unpackingQueue.getItems().stream()
                                .map(UnpackingQueueItem::canPauseProperty)
                                .toArray(BooleanBinding[]::new);

                        BooleanBinding canPauseAnyBinding = Bindings.createBooleanBinding(
                                () -> Arrays.stream(canPauseProperties).anyMatch(ObservableBooleanValue::get),
                                canPauseProperties);

                        pauseButton.disableProperty().unbind();
                        pauseButton.disableProperty().bind(canPauseAnyBinding.not());
                    }

                    {
                        var canResumeProperties = unpackingQueue.getItems().stream()
                                .map(UnpackingQueueItem::canResumeProperty)
                                .toArray(BooleanBinding[]::new);

                        BooleanBinding canResumeAnyBinding = Bindings.createBooleanBinding(
                                () -> Arrays.stream(canResumeProperties).anyMatch(ObservableBooleanValue::get),
                                canResumeProperties);
                        resumeButton.disableProperty().unbind();
                        resumeButton.disableProperty().bind(canResumeAnyBinding.not());
                    }

                    {
                        var canCancelProperties = unpackingQueue.getItems().stream()
                                .map(UnpackingQueueItem::canCancelProperty)
                                .toArray(BooleanBinding[]::new);
                        BooleanBinding canCancelAnyBinding = Bindings.createBooleanBinding(
                                () -> Arrays.stream(canCancelProperties).anyMatch(ObservableBooleanValue::get),
                                canCancelProperties);
                        cancelButton.disableProperty().unbind();
                        cancelButton.disableProperty().bind(canCancelAnyBinding.not());
                    }
                });

        // Start receiving events
        boolean ok = OpenFileEvents.INSTANCE.subscribe((file, eventTime) -> {
            startUnpacking(file, eventTime);
        });
        if (!ok) {
            UiService.errorDialog(
                    "Cannot subscribe to macOS open file events.\n" +
                            "This application will exit.");
            Platform.exit();
        }

        autocloseTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> checkAutoclose());
            }
        }, 0, 1000);
    }

    private void checkAutoclose() {
        if (unpackingQueue.getItems().isEmpty())
            return;

        boolean canAutoclose = unpackingQueue.getItems().stream()
                .allMatch(item -> item.requestedAtAppStartup() && !item.canCancelProperty().get());

        if (canAutoclose) {
            Platform.runLater(() -> thisWindow().close());
        }
    }

    @FXML
    private void guiOpenArchive() {
        File archive = openArchiveFileChooser.showOpenDialog(thisWindow());
        if (archive != null) {
            startUnpacking(archive, OpenFileEventTime.APP_RUNNING);
        }
    }

    private void startUnpacking(File archive, OpenFileEventTime eventTime) {
        UnpackingQueueItem item = new UnpackingQueueItem(
                archive,
                eventTime,
                new GuiFileConflictResolutionProvider(),
                new GuiPasswordProvider(thisWindow()));

        // Add as a first item
        unpackingQueue.getItems().add(0, item);
        unpackingExecutor.execute(item);
    }

    @FXML
    private void guiCancelAll() {
        if (!UnpackingQueue.hasAnyCancellableItems(unpackingQueue)) {
            return;
        }

        boolean yes = UiService.confirmationDialog("Do you want to cancel all operations?");
        if (yes) {
            UnpackingQueue.cancelAll(unpackingQueue);
        }
    }

    @FXML
    private void guiPauseAll() {
        UnpackingQueue.pauseAll(unpackingQueue);
    }

    @FXML
    private void guiResumeAll() {
        UnpackingQueue.resumeAll(unpackingQueue);
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
