package pl.marcinchwedczuk.iunrar.gui.decompression;

import com.github.junrar.exception.RarException;

import java.io.File;
import java.io.IOException;

import static java.util.Objects.requireNonNull;

public class RarUnpacker {
    private final File rarArchive;
    private final WorkerStatus progressCallback;
    private final FileConflictResolutionProvider conflictResolutionProvider;

    public RarUnpacker(File rarArchive,
                       WorkerStatus progressCallback,
                       FileConflictResolutionProvider conflictResolutionProvider) {
        this.rarArchive = requireNonNull(rarArchive);
        this.progressCallback = requireNonNull(progressCallback);
        this.conflictResolutionProvider = conflictResolutionProvider;
    }

    public File unpack() throws InterruptedException {
        return unpack(null);
    }

    private File unpack(String password) throws InterruptedException {
        try {
            // Remove rar extension
            File destinationDirectory = new File(
                    rarArchive.getAbsolutePath().replaceAll("\\.rar$", ""));

            progressCallback.updateProgress("Creating output directory...", 0.0);
            if (!destinationDirectory.exists() && !destinationDirectory.mkdirs()) {
                throw new RuntimeException("Cannot create output directory: '" + destinationDirectory + "'.");
            }

            progressCallback.updateProgress("Getting list of files...", 0.0);
            long totalFiles = LocalFolderExtractor.getContentsDescription(rarArchive, password).size();

            LocalFolderExtractor.extract(
                    rarArchive,
                    destinationDirectory,
                    password,
                    totalFiles,
                    progressCallback,
                    conflictResolutionProvider);

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
