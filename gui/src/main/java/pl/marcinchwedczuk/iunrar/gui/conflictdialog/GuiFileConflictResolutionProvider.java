package pl.marcinchwedczuk.iunrar.gui.conflictdialog;

import javafx.application.Platform;
import javafx.stage.Stage;
import pl.marcinchwedczuk.iunrar.gui.decompression.FileConflictResolution;
import pl.marcinchwedczuk.iunrar.gui.decompression.FileConflictResolutionProvider;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class GuiFileConflictResolutionProvider implements FileConflictResolutionProvider {
    @Override
    public FileConflictResolution resolveConflict(File archive, File file,
                                                  long oldSizeBytes, long newSizeBytes) {
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
        return ConflictDialog.showAndWaitForAnswer(
                archive.getAbsolutePath(),
                file.getAbsolutePath(),
                oldSizeBytes,
                newSizeBytes);
    }
}
