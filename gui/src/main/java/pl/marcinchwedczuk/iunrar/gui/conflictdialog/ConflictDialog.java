package pl.marcinchwedczuk.iunrar.gui.conflictdialog;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import pl.marcinchwedczuk.iunrar.gui.App;
import pl.marcinchwedczuk.iunrar.gui.decompression.FileConflictResolution;

import java.io.IOException;

public class ConflictDialog {

    public static FileConflictResolution show(
            Window owner, String fileName, long oldSize, long newSize
    ) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    ConflictDialog.class.getResource("ConflictDialog.fxml"));

            Stage childWindow = new Stage();
            childWindow.initOwner(owner);
            childWindow.initModality(Modality.WINDOW_MODAL);
            childWindow.initStyle(StageStyle.UNDECORATED);
            childWindow.setTitle("File Conflict");
            childWindow.setScene(new Scene(loader.load()));
            childWindow.setResizable(false);
            childWindow.sizeToScene();

            ConflictDialog controller = loader.getController();
            controller.initialize(fileName, oldSize, newSize);

            childWindow.showAndWait();

            return controller.answer;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private VBox rootElement;

    @FXML
    private TextArea fileNameArea;
    @FXML
    private Label existingSizeLabel;
    @FXML
    private Label newSizeLabel;

    private FileConflictResolution answer = FileConflictResolution.STOP_OPERATION;

    private void initialize(String fileName, long oldSize, long newSize) {
        fileNameArea.setText(fileName);
        existingSizeLabel.setText(oldSize + " B");
        newSizeLabel.setText(newSize + " B");
    }

    @FXML
    private void guiStop() {
        answer = FileConflictResolution.STOP_OPERATION;
        guiClose();
    }

    @FXML
    private void guiSkipAll() {
        answer = FileConflictResolution.SKIP;
        guiClose();
    }

    @FXML
    private void guiSkip() {
        answer = FileConflictResolution.SKIP;
        guiClose();
    }

    @FXML
    private void guiOverwriteAll() {
        answer = FileConflictResolution.OVERWRITE;
        guiClose();
    }

    @FXML
    private void guiOverwrite() {
        answer = FileConflictResolution.OVERWRITE;
        guiClose();
    }

    @FXML
    private void guiClose() {
        ((Stage) rootElement.getScene().getWindow()).close();
    }
}
