package pl.marcinchwedczuk.iunrar.gui.decompressionqueue;

import javafx.beans.binding.Bindings;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.util.ResourceBundle;

public class UnpackingQueueListViewCell
        extends ListCell<UnpackingQueueItem>
        implements Initializable
{
    public static UnpackingQueueListViewCell newCell(ListView<UnpackingQueueItem> parent) {
        FXMLLoader loader = new FXMLLoader(
                UnpackingQueueListViewCell.class.getResource("UnpackingQueueListViewCell.fxml"));
        try {
            loader.load();
            return loader.getController();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Image pauseImage;
    private Image resumeImage;

    @FXML
    private GridPane root;

    @FXML
    private Label archiveName;

    @FXML
    private ProgressBar progress;

    @FXML
    private Label message;

    @FXML
    private Button stopButton;

    @FXML
    private Button pauseButton;

    @FXML
    private ImageView pauseButtonImage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        pauseImage = new Image(getClass().getResource("icons/pause.png").toString());
        resumeImage = new Image(getClass().getResource("icons/resume.png").toString());

        updateSelected(false);
    }

    @Override
    protected void updateItem(UnpackingQueueItem item, boolean empty) {
        super.updateItem(item, empty); // IMPORTANT

        if (empty) {
            setGraphic(null);
            archiveName.setText("");
            message.textProperty().unbind();
            progress.progressProperty().unbind();
            stopButton.disableProperty().unbind();
            pauseButton.disableProperty().unbind();
            pauseButton.textProperty().unbind();
            pauseButtonImage.imageProperty().unbind();
        }
        else {
            setGraphic(root);
            archiveName.setText(item.archiveName());
            message.textProperty().bind(item.messageProperty());
            progress.progressProperty().bind(item.progressProperty());
            stopButton.disableProperty().bind(Bindings.createBooleanBinding(
                    () -> isNoLongerActive(item),
                    item.stateProperty()));

            pauseButton.disableProperty().bind(Bindings.createBooleanBinding(
                    () -> isNoLongerActive(item),
                    item.stateProperty()));
            pauseButton.textProperty().bind(Bindings.createStringBinding(
                    () -> item.pausedProperty().get() ? "Resume" : "Pause",
                    item.pausedProperty()));
            pauseButtonImage.imageProperty().bind(Bindings.createObjectBinding(
                    () -> item.pausedProperty().get() ? resumeImage : pauseImage,
                    item.pausedProperty()));
        }
    }

    private boolean isNoLongerActive(UnpackingQueueItem item) {
        var state = item.stateProperty().getValue();
        return state == Worker.State.SUCCEEDED ||
                state == Worker.State.FAILED ||
                state == Worker.State.CANCELLED;
    }

    @FXML
    private void guiStop() {
        UnpackingQueueItem item = getItem();
        if (item != null) {
            // Interrupt false to allow cancel logic to execute
            item.cancel(false);
        }
    }

    @FXML
    private void guiPause() {
        UnpackingQueueItem item = getItem();
        if (item != null) {
            item.setPaused(!item.isPaused());
        }
    }
}
