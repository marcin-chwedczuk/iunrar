package pl.marcinchwedczuk.iunrar.gui.decompression;

import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;

import java.io.File;
import java.io.IOException;

import static java.util.Objects.requireNonNull;
import static pl.marcinchwedczuk.iunrar.gui.decompression.MessageLevel.IMPORTANT;

public class RarUnpacker {
    private final File archive;
    private final WorkerStatus workerStatus;
    private final FileConflictResolutionProvider fileConflictResolutionProvider;
    private final PasswordProvider passwordProvider;

    public RarUnpacker(File archive,
                       WorkerStatus workerStatus,
                       FileConflictResolutionProvider fileConflictResolutionProvider,
                       PasswordProvider passwordProvider) {
        this.archive = requireNonNull(archive);
        this.workerStatus = requireNonNull(workerStatus);
        this.fileConflictResolutionProvider = requireNonNull(fileConflictResolutionProvider);
        this.passwordProvider = requireNonNull(passwordProvider);
    }

    public File unpack() throws InterruptedException {
        try {
            // Remove rar extension
            File destinationDirectory = new File(
                    archive.getAbsolutePath().replaceAll("\\.rar$", ""));

            workerStatus.updateMessage(IMPORTANT, "Creating output directory...");
            if (!destinationDirectory.exists() && !destinationDirectory.mkdirs()) {
                throw new RuntimeException("Cannot create output directory: '" + destinationDirectory + "'.");
            }

            // Find out password and reuse for further operations
            Archive arch = LocalFolderExtractor.createArchiveOrThrowException(archive, passwordProvider);
            StaticPasswordProvider staticPasswordProvider = new StaticPasswordProvider(arch.getPassword());

            workerStatus.updateMessage(IMPORTANT, "Computing total size...");
            long totalSize = LocalFolderExtractor.getContentsDescription(archive, staticPasswordProvider).stream()
                    .mapToLong(cd -> cd.size)
                    .sum();

            workerStatus.updateMessage(IMPORTANT, "Extracting...");
            LocalFolderExtractor.extract(
                    archive,
                    destinationDirectory,
                    totalSize,
                    workerStatus,
                    fileConflictResolutionProvider,
                    staticPasswordProvider);

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
