package pl.marcinchwedczuk.iunrar.gui.mainwindow;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import pl.marcinchwedczuk.iunrar.gui.App;
import pl.marcinchwedczuk.iunrar.gui.OsUtils;
import pl.marcinchwedczuk.iunrar.gui.aboutdialog.AboutDialog;

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
    private MenuItem closeMenuItem;

    private final FileChooser openArchiveFileChooser = FileChoosers.newOpenArchiveFileChooser();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (OsUtils.isMac() && !App.testMode) {
            mainMenu.setUseSystemMenuBar(true);
            // MacOS will add Quit menu entry automatically
            removeMenuItem(closeMenuItem);
        }
    }

    @FXML
    private void guiOpenArchive() {

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
