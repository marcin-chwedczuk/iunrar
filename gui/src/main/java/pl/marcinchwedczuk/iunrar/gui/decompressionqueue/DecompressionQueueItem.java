package pl.marcinchwedczuk.iunrar.gui.decompressionqueue;

import javafx.application.Platform;
import javafx.concurrent.Task;
import pl.marcinchwedczuk.iunrar.gui.UiService;
import pl.marcinchwedczuk.iunrar.gui.decompression.FileConflictResolutionProvider;
import pl.marcinchwedczuk.iunrar.gui.decompression.RarUnpacker;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Objects.requireNonNull;

public class DecompressionQueueItem extends Task<Void> {
    private final File archive;

    private final FileConflictResolutionProvider fileConflictResolutionProvider;
    private final AtomicBoolean paused = new AtomicBoolean(false);

    public DecompressionQueueItem(File archive,
                                  FileConflictResolutionProvider fileConflictResolutionProvider) {
        this.archive = requireNonNull(archive);
        this.fileConflictResolutionProvider = requireNonNull(fileConflictResolutionProvider);
    }

    @Override
    protected Void call() throws Exception {
        try {

            RarUnpacker unpacker = new RarUnpacker(
                    archive,
                    (message, progress) -> {

                        updateMessage(message);
                        updateProgress(progress, 100.0);
                    },
                    fileConflictResolutionProvider);

            File destinationDirectory = unpacker.unpack();

            Runtime.getRuntime().exec("open .", new String[]{ }, destinationDirectory);

            updateMessage("Done.");
            updateProgress(100.0, 100.0);
        } catch (Exception e) {
            e.printStackTrace();

            Platform.runLater(() -> {
                UiService.errorDialog(e.getClass().getName() + ": " + e.getMessage());
            });
        }
        return null;
    }

    protected Void callOld() throws Exception {
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
