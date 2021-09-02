package pl.marcinchwedczuk.iunrar.gui.unpackingqueue;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import pl.marcinchwedczuk.iunrar.gui.AppPreferences;
import pl.marcinchwedczuk.iunrar.gui.UiService;
import pl.marcinchwedczuk.iunrar.gui.decompression.*;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Objects.requireNonNull;

public class UnpackingQueueItem extends Task<Void> {
    private final File archive;

    private final FileConflictResolutionProvider fileConflictResolutionProvider;
    private final PasswordProvider passwordProvider;

    // Property is used by JavaFX, Atomic is used to pass flag to the backing thread
    private final AtomicBoolean paused = new AtomicBoolean(false);
    private final SimpleBooleanProperty pausedProperty = new SimpleBooleanProperty(paused.get());

    private final BooleanBinding canPauseProperty;
    private final BooleanBinding canResumeProperty;
    private final BooleanBinding canCancelProperty;

    public UnpackingQueueItem(File archive,
                              FileConflictResolutionProvider fileConflictResolutionProvider,
                              PasswordProvider passwordProvider) {
        this.archive = requireNonNull(archive);
        this.fileConflictResolutionProvider = requireNonNull(fileConflictResolutionProvider);
        this.passwordProvider = requireNonNull(passwordProvider);

        canPauseProperty = Bindings.createBooleanBinding(
                () -> isActive() && !pausedProperty.get(),
                this.stateProperty(), pausedProperty);

        canResumeProperty = Bindings.createBooleanBinding(
                () -> isActive() && pausedProperty.get(),
                this.stateProperty(), pausedProperty);

        canCancelProperty = Bindings.createBooleanBinding(
                () -> isActive(),
                this.stateProperty());
    }


    private boolean isActive() {
        var state = this.stateProperty().getValue();
        return !(state == Worker.State.SUCCEEDED ||
                state == Worker.State.FAILED ||
                state == Worker.State.CANCELLED);
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

            updateProgressPercent(0);

            RarUnpacker unpacker = new RarUnpacker(
                    archive,
                    new ThrottledWorkerStatus(workerStatus),
                    fileConflictResolutionProvider,
                    passwordProvider);
            File destinationDirectory = unpacker.unpack();

            var preferences = new AppPreferences();

            if (preferences.getOpenFolderAfterUnpacking()) {
                Runtime.getRuntime().exec("open .", new String[]{}, destinationDirectory);
            }

            if (preferences.getRemoveArchiveAfterUnpacking()) {
                archive.delete(); // ignore result
            }

            updateMessage("Done");
            updateProgressPercent(100.0);
        }
        catch (StopCompressionException e) {
            updateMessage(e.getMessage());
        }
        catch (Exception e) {
            updateMessage("Failed");
            e.printStackTrace();

            Platform.runLater(() -> {
                UiService.errorDialog(
                        String.format("Cannot unpack archive: %s", archive.getName()),
                        String.format("%s: %s", e.getClass().getName(), e.getMessage()));
            });
        }
        return null;
    }

    public String archiveName() {
        return archive.getName();
    }

    public boolean isPaused() { return paused.get(); }
    public void setPaused(boolean paused) {
        this.paused.set(paused);
        this.pausedProperty.set(paused);
    }
    public BooleanProperty pausedProperty() { return pausedProperty; }

    public BooleanBinding canPauseProperty() { return canPauseProperty; }
    public BooleanBinding canResumeProperty() { return canResumeProperty; }
    public BooleanBinding canCancelProperty() { return canCancelProperty; }

    private void updateProgressPercent(double percent) {
        this.updateProgress(percent, 100.0);
    }
}
