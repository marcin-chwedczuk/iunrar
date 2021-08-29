package pl.marcinchwedczuk.iunrar.gui.passworddialog;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import pl.marcinchwedczuk.iunrar.gui.App;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class PasswordDialog implements Initializable {

    public static Optional<String> askPassword(String archiveName, Window owner) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    PasswordDialog.class.getResource("PasswordDialog.fxml"));

            Stage childWindow = new Stage();
            childWindow.initOwner(owner);
            childWindow.initModality(Modality.WINDOW_MODAL);
            childWindow.initStyle(StageStyle.UTILITY);
            childWindow.setTitle("Password Required");
            childWindow.setScene(new Scene(loader.load()));
            childWindow.setResizable(false);
            childWindow.sizeToScene();

            PasswordDialog controller = loader.getController();
            controller.setArchiveName(archiveName);
            childWindow.showAndWait();
            return controller.selectedPassword;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private PasswordField password;

    @FXML
    private Label archiveName;

    @FXML
    private Button okButton;

    private Optional<String> selectedPassword = Optional.empty();

    @FXML
    private GridPane rootElement;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        okButton.disableProperty().bind(password.lengthProperty().isEqualTo(0));
        Platform.runLater(() -> password.requestFocus());
    }

    private void setArchiveName(String name) {
        archiveName.setText(name);
    }

    @FXML
    private void selectPassword() {
        selectedPassword = Optional.of(password.getText());
        closeDialog();
    }

    @FXML
    private void cancel() {
        selectedPassword = Optional.empty();
        closeDialog();
    }

    private void closeDialog() {
        ((Stage)rootElement.getScene().getWindow()).close();
    }
}
