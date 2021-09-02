package pl.marcinchwedczuk.iunrar.gui.decompressionqueue;

import javafx.application.Platform;
import javafx.concurrent.Task;
import pl.marcinchwedczuk.iunrar.gui.UiService;
import pl.marcinchwedczuk.iunrar.gui.decompression.*;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Objects.requireNonNull;

public class UnpackingQueueItem extends Task<Void> {
    private final File archive;

    private final FileConflictResolutionProvider fileConflictResolutionProvider;
    private final PasswordProvider passwordProvider;
    private final AtomicBoolean paused = new AtomicBoolean(false);

    public UnpackingQueueItem(File archive,
                              FileConflictResolutionProvider fileConflictResolutionProvider,
                              PasswordProvider passwordProvider) {
        this.archive = requireNonNull(archive);
        this.fileConflictResolutionProvider = requireNonNull(fileConflictResolutionProvider);
        this.passwordProvider = requireNonNull(passwordProvider);
    }

    @Override
    protected Void call() throws Exception {
        try {
            UnpackingQueueItem thisItem = this;
            WorkerStatus workerStatus = new WorkerStatus() {
                @Override
                public void updateMessage(MessageLevel messageLevel, String message) { thisItem.updateMessage(message); }

                @Override
                public void updateProgressPercent(double progressPercent) { thisItem.updateProgressPercent(progressPercent); }

                @Override
                public boolean shouldStop() { return thisItem.isCancelled(); }

                @Override
                public boolean shouldPause() { return thisItem.isPaused(); }
            };

            RarUnpacker unpacker = new RarUnpacker(
                    archive,
                    new ThrottledWorkerStatus(workerStatus),
                    fileConflictResolutionProvider,
                    passwordProvider);

            File destinationDirectory = unpacker.unpack();

            Runtime.getRuntime().exec("open .", new String[]{ }, destinationDirectory);

            updateMessage("Done");
            updateProgress(100.0, 100.0);
        }
        catch (StopCompressionException e) {
            updateMessage(e.getMessage());
        }
        catch (Exception e) {
            updateMessage("Failed");
            e.printStackTrace();

            Platform.runLater(() -> {
                UiService.errorDialog(e.getClass().getName() + ": " + e.getMessage());
            });
        }
        return null;
    }

    public String archiveName() {
        return archive.getName();
    }

    public boolean isPaused() { return paused.get(); }
    public void setPaused(boolean paused) { this.paused.set(paused); }

    private void updateProgressPercent(double percent) {
        this.updateProgress(percent, 100.0);
    }
}
