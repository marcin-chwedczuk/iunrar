package pl.marcinchwedczuk.iunrar.gui.conflictdialog;

import javafx.application.Platform;
import javafx.stage.Stage;
import pl.marcinchwedczuk.iunrar.gui.decompression.FileConflictResolution;
import pl.marcinchwedczuk.iunrar.gui.decompression.FileConflictResolutionProvider;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class GuiFileConflictResolutionProvider implements FileConflictResolutionProvider {
    private final AtomicReference<FileConflictResolution> forAllRemaining = new AtomicReference<>();

    @Override
    public FileConflictResolution resolveConflict(File archive, File file,
                                                  long oldSizeBytes, long newSizeBytes) {
        if (forAllRemaining.get() != null) {
            return forAllRemaining.get();
        }

        if (Platform.isFxApplicationThread()) {
            return resolveConflictImpl(archive, file, oldSizeBytes, newSizeBytes);
        }

        AtomicReference<FileConflictResolution> answer = new AtomicReference<>(null);
        CountDownLatch waitForAnswer = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                answer.set(resolveConflictImpl(archive, file, oldSizeBytes, newSizeBytes));
            } finally {
                waitForAnswer.countDown();
            }
        });

        try {
            waitForAnswer.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return answer.get();
    }

    private FileConflictResolution resolveConflictImpl(File archive,
                                                       File file,
                                                       long oldSizeBytes,
                                                       long newSizeBytes) {
        GuiConflictResolutionAnswer answer = ConflictDialog.showAndWaitForAnswer(
                archive.getAbsolutePath(),
                file.getAbsolutePath(),
                oldSizeBytes,
                newSizeBytes);

        switch (answer) {
            case STOP_OPERATION:
                return FileConflictResolution.STOP_OPERATION;

            case SKIP:
                return FileConflictResolution.SKIP;

            case SKIP_ALL:
                forAllRemaining.set(FileConflictResolution.SKIP);
                return forAllRemaining.get();

            case OVERWRITE:
                return FileConflictResolution.OVERWRITE;

            case OVERWRITE_ALL:
                forAllRemaining.set(FileConflictResolution.OVERWRITE);
                return forAllRemaining.get();
        }

        throw new AssertionError();
    }
}
