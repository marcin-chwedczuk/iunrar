package pl.marcinchwedczuk.iunrar.gui.conflictdialog;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import pl.marcinchwedczuk.iunrar.gui.App;

import java.io.IOException;

public class ConflictDialog {
    public static ConflictDialog show(Window owner) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    ConflictDialog.class.getResource("ConflictDialog.fxml"));

            Stage childWindow = new Stage();
            childWindow.initOwner(owner);
            childWindow.initModality(Modality.WINDOW_MODAL);
            childWindow.initStyle(StageStyle.UTILITY);
            childWindow.setTitle("File Conflict");
            childWindow.setScene(new Scene(loader.load()));
            childWindow.setResizable(false);
            childWindow.sizeToScene();

            ConflictDialog controller = loader.getController();
            childWindow.show();
            return controller;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private VBox rootElement;

    @FXML
    private void guiClose() {
        ((Stage) rootElement.getScene().getWindow()).close();
    }

    @FXML
    private void guiStop() {

    }

    @FXML
    private void guiSkipAll() {

    }

    @FXML
    private void guiSkip() {

    }

    @FXML
    private void guiOverwriteAll() {

    }

    @FXML
    private void guiOverwrite() {

    }
}
