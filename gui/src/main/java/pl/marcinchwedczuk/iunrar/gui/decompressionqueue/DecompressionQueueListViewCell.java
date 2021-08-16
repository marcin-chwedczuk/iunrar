package pl.marcinchwedczuk.iunrar.gui.decompressionqueue;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.util.ResourceBundle;

public class DecompressionQueueListViewCell
        extends ListCell<DecompressionQueueItem>
        implements Initializable
{
    public static DecompressionQueueListViewCell newCell(ListView<DecompressionQueueItem> parent) {
        FXMLLoader loader = new FXMLLoader(
                DecompressionQueueListViewCell.class.getResource("DecompressionQueueListViewCell.fxml"));
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
    protected void updateItem(DecompressionQueueItem item, boolean empty) {
        super.updateItem(item, empty); // IMPORTANT

        if (empty) {
            setGraphic(null);
        }
        else {
            setGraphic(root);
        }

        bindToModel(item, empty);
    }

    private void bindToModel(DecompressionQueueItem item, boolean empty) {
        if (empty) {
            message.textProperty().unbind();
        }
        else {
            message.textProperty().bind(item.absolutePathProperty());
        }
    }

    @FXML
    private void guiStop() {

    }

    @FXML
    private void guiPause() {

    }
}
