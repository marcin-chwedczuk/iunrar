package pl.marcinchwedczuk.iunrar.gui.decompressionqueue;

import javafx.beans.binding.Bindings;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
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
            pauseButton.setText("Pause");
        }
        else {
            setGraphic(root);
            archiveName.setText(item.archiveName());
            message.textProperty().bind(item.messageProperty());
            progress.progressProperty().bind(item.progressProperty());
            stopButton.disableProperty().bind(Bindings.createBooleanBinding(
                    () -> {
                        var state = item.stateProperty().getValue();
                        return state == Worker.State.SUCCEEDED ||
                                state == Worker.State.FAILED ||
                                state == Worker.State.CANCELLED;
                    },
                    item.stateProperty()));

            pauseButton.disableProperty().bind(Bindings.createBooleanBinding(
                    () -> {
                        var state = item.stateProperty().getValue();
                        return state == Worker.State.SUCCEEDED ||
                                state == Worker.State.FAILED ||
                                state == Worker.State.CANCELLED;
                    }, item.stateProperty()));
            pauseButton.setText(item.isPaused() ? "Resume" : "Pause");
        }
    }

    @FXML
    private void guiStop() {
        UnpackingQueueItem item = getItem();
        if (item != null) {
            // Allow cancel logic to execute
            item.cancel(false);
        }
    }

    @FXML
    private void guiPause() {
        UnpackingQueueItem item = getItem();
        if (item != null) {
            item.setPaused(!item.isPaused());
        }

        if (item.isPaused()) {
            pauseButton.setText("Resume");
        }
        else {
            pauseButton.setText("Pausing");
        }
    }
}
