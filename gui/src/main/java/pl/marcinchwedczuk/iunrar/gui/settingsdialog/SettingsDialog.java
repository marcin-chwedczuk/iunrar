package pl.marcinchwedczuk.iunrar.gui.settingsdialog;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import pl.marcinchwedczuk.iunrar.gui.AppPreferences;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class SettingsDialog implements Initializable {

    public static void show(Window owner) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    SettingsDialog.class.getResource("SettingsDialog.fxml"));

            Stage childWindow = new Stage();
            childWindow.initOwner(owner);
            childWindow.initModality(Modality.WINDOW_MODAL);
            childWindow.initStyle(StageStyle.UTILITY);
            childWindow.setTitle("Settings");
            childWindow.setScene(new Scene(loader.load()));
            childWindow.setResizable(false);
            childWindow.sizeToScene();

            childWindow.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private VBox rootElement;

    @FXML
    private CheckBox guiOpenFolder;

    @FXML
    private CheckBox guiRemoveArchive;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        AppPreferences prefs = new AppPreferences();

        guiOpenFolder.setSelected(prefs.getOpenFolderAfterUnpacking());
        guiRemoveArchive.setSelected(prefs.getRemoveArchiveAfterUnpacking());
    }

    @FXML
    private void guiSave() {
        new AppPreferences()
                .setOpenFolderAfterDecompression(guiOpenFolder.isSelected())
                .setRemoveArchiveAfterUnpacking(guiRemoveArchive.isSelected())
                .save();

        closeDialog();
    }

    @FXML
    private void guiCancel() {
        closeDialog();
    }

    private void closeDialog() {
        ((Stage) rootElement.getScene().getWindow()).close();
    }
}
