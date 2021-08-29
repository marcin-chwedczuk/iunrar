package pl.marcinchwedczuk.iunrar.gui.passworddialog;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Window;
import pl.marcinchwedczuk.iunrar.gui.decompression.FileConflictResolution;
import pl.marcinchwedczuk.iunrar.gui.decompression.PasswordProvider;

import java.io.File;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class GuiPasswordProvider implements PasswordProvider {
    private final Window dialogOwner;

    public GuiPasswordProvider(Window dialogOwner) {
        this.dialogOwner = dialogOwner;
    }

    @Override
    public Optional<String> askForPassword(File archive) {
        if (Platform.isFxApplicationThread()) {
            return askForPasswordImpl(archive);
        }

        AtomicReference<Optional<String>> answer = new AtomicReference<>(null);
        CountDownLatch waitForAnswer = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                answer.set(askForPasswordImpl(archive));
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

    private Optional<String> askForPasswordImpl(File archive) {
        return PasswordDialog.askPassword(archive.getName(), dialogOwner);
    }
}
