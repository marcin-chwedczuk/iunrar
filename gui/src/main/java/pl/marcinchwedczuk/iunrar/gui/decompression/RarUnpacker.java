package pl.marcinchwedczuk.iunrar.gui.decompression;

import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;

import java.io.File;
import java.io.IOException;

import static java.util.Objects.requireNonNull;
import static pl.marcinchwedczuk.iunrar.gui.decompression.MessageLevel.IMPORTANT;

public class RarUnpacker {
    private final File rarArchive;
    private final WorkerStatus progressCallback;
    private final FileConflictResolutionProvider conflictResolutionProvider;
    private final PasswordProvider passwordProvider;

    public RarUnpacker(File rarArchive,
                       WorkerStatus progressCallback,
                       FileConflictResolutionProvider conflictResolutionProvider,
                       PasswordProvider passwordProvider) {
        this.rarArchive = requireNonNull(rarArchive);
        this.progressCallback = requireNonNull(progressCallback);
        this.conflictResolutionProvider = requireNonNull(conflictResolutionProvider);
        this.passwordProvider = requireNonNull(passwordProvider);
    }

    public File unpack() throws InterruptedException {
        return unpack(null);
    }

    private File unpack(String password) throws InterruptedException {
        try {
            // Remove rar extension
            File destinationDirectory = new File(
                    rarArchive.getAbsolutePath().replaceAll("\\.rar$", ""));

            progressCallback.updateMessage(IMPORTANT, "Creating output directory...");
            if (!destinationDirectory.exists() && !destinationDirectory.mkdirs()) {
                throw new RuntimeException("Cannot create output directory: '" + destinationDirectory + "'.");
            }

            // Find out password and reuse for further operations
            Archive arch = LocalFolderExtractor.createArchiveOrThrowException(rarArchive, passwordProvider);
            RelayPasswordProvider relayPasswordProvider = new RelayPasswordProvider(arch.getPassword());

            progressCallback.updateMessage(IMPORTANT, "Computing total size...");
            long totalSize = LocalFolderExtractor.getContentsDescription(rarArchive, relayPasswordProvider).stream()
                    .mapToLong(cd -> cd.size)
                    .sum();

            progressCallback.updateMessage(IMPORTANT, "Extracting...");
            LocalFolderExtractor.extract(
                    rarArchive,
                    destinationDirectory,
                    totalSize,
                    progressCallback,
                    conflictResolutionProvider,
                    relayPasswordProvider);

            return destinationDirectory;
        }
        catch (RarException | IOException e) {
            if (e.getCause() instanceof StopCompressionException) {
                throw (StopCompressionException)e.getCause();
            }
            throw new RuntimeException("Error while unpacking the archive!", e);
        }
    }
}
