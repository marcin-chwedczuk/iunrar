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

    public static GuiConflictResolutionAnswer showAndWaitForAnswer(
            String archiveName, String fileName, long oldSize, long newSize
    ) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    ConflictDialog.class.getResource("ConflictDialog.fxml"));

            Stage childWindow = new Stage();
            childWindow.initModality(Modality.APPLICATION_MODAL);
            childWindow.initStyle(StageStyle.UTILITY);
            childWindow.setTitle("File Conflict");
            childWindow.setScene(new Scene(loader.load()));
            childWindow.setResizable(false);
            childWindow.sizeToScene();

            ConflictDialog controller = loader.getController();
            controller.initialize(fileName, archiveName, oldSize, newSize);

            childWindow.showAndWait();

            return controller.answer;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private VBox rootElement;

    @FXML
    private Label guiFile;
    @FXML
    private Label guiArchive;
    @FXML
    private Label existingSizeLabel;
    @FXML
    private Label newSizeLabel;

    private GuiConflictResolutionAnswer answer = GuiConflictResolutionAnswer.STOP_OPERATION;

    private void initialize(String fileName,
                            String archiveName,
                            long oldSize, long newSize) {
        guiArchive.setText(archiveName);
        guiFile.setText(fileName);
        existingSizeLabel.setText(oldSize + " B");
        newSizeLabel.setText(newSize + " B");
    }

    @FXML
    private void guiStop() {
        answer = GuiConflictResolutionAnswer.STOP_OPERATION;
        guiClose();
    }

    @FXML
    private void guiSkip() {
        answer = GuiConflictResolutionAnswer.SKIP;
        guiClose();
    }

    @FXML
    private void guiSkipAll() {
        answer = GuiConflictResolutionAnswer.SKIP_ALL;
        guiClose();
    }

    @FXML
    private void guiOverwrite() {
        answer = GuiConflictResolutionAnswer.OVERWRITE;
        guiClose();
    }

    @FXML
    private void guiOverwriteAll() {
        answer = GuiConflictResolutionAnswer.OVERWRITE_ALL;
        guiClose();
    }

    @FXML
    private void guiClose() {
        ((Stage) rootElement.getScene().getWindow()).close();
    }
}
