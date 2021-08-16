package pl.marcinchwedczuk.iunrar.gui.decompressionqueue;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class DecompressionQueueItem extends Task<Void> {
    private final File archive;

    private final AtomicBoolean paused = new AtomicBoolean(false);

    public DecompressionQueueItem(File archive) {
        this.archive = Objects.requireNonNull(archive);
    }

    @Override
    protected Void call() throws Exception {
        for (int i = 0; i <= 20; i++) {
            spinWaitWhenPaused();

            Thread.sleep(500);
            if (isCancelled()) {
                updateMessage("Stopped.");
                return null;
            }
            updateProgress(i, 20);
            updateMessage("Currently @" + i);
        }

        updateMessage("Done");
        return null;
    }

    private void spinWaitWhenPaused() throws InterruptedException {
        while (isPaused()) {
            if (isCancelled()) break;
            Thread.sleep(100);
        }
    }

    public boolean isPaused() { return paused.get(); }
    public void setPaused(boolean paused) { this.paused.set(paused); }
}
