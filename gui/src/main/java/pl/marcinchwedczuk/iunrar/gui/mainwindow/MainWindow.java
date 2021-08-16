package pl.marcinchwedczuk.iunrar.gui.mainwindow;

import com.github.junrar.Junrar;
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
import pl.marcinchwedczuk.iunrar.gui.decompressionqueue.DecompressionQueueItem;
import pl.marcinchwedczuk.iunrar.gui.decompressionqueue.DecompressionQueueListViewCell;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

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

    @FXML
    private MenuBar mainMenu;

    @FXML
    private ListView<DecompressionQueueItem> decompressionQueue;

    private final FileChooser openArchiveFileChooser = FileChoosers.newOpenArchiveFileChooser();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        decompressionQueue.setCellFactory(DecompressionQueueListViewCell::newCell);
        decompressionQueue.getItems().setAll(
                new DecompressionQueueItem(new File("foo.rar")),
                new DecompressionQueueItem(new File("/Users/mc/bar.rar"))
        );

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
    private void guiCancelAll() {

    }

    @FXML
    private void guiPauseAll() {

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
