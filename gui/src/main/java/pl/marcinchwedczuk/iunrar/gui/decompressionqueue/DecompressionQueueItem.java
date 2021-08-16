package pl.marcinchwedczuk.iunrar.gui.decompressionqueue;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.io.File;

public class DecompressionQueueItem {
    private final ReadOnlyObjectProperty<File> archive;
    private final StringBinding absolutePath;

    public DecompressionQueueItem(File archive) {
        this.archive = new SimpleObjectProperty<>(this, "archive", archive);
        this.absolutePath = Bindings.createStringBinding(
                () -> this.archive.getValue().getAbsolutePath(),
                this.archive);
    }

    public StringBinding absolutePathProperty() {
        return absolutePath;
    }
}
