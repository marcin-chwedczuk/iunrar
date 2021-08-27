package pl.marcinchwedczuk.iunrar.gui.decompression;

import com.github.junrar.exception.RarException;

import java.io.File;
import java.io.IOException;

import static java.util.Objects.requireNonNull;

public class RarUnpacker {
    private final File rarArchive;
    private final UnpackProgressCallback progressCallback;
    private final FileConflictResolutionProvider conflictResolutionProvider;

    public RarUnpacker(File rarArchive,
                       UnpackProgressCallback progressCallback,
                       FileConflictResolutionProvider conflictResolutionProvider) {
        this.rarArchive = requireNonNull(rarArchive);
        this.progressCallback = requireNonNull(progressCallback);
        this.conflictResolutionProvider = conflictResolutionProvider;
    }

    public File unpack() {
        return unpack(null);
    }

    private File unpack(String password) {
        try {
            // Remove rar extension
            File destinationDirectory = new File(
                    rarArchive.getAbsolutePath().replaceAll("\\.rar$", ""));

            progressCallback.update("Creating output directory...", 0.0);
            if (!destinationDirectory.exists() && !destinationDirectory.mkdirs()) {
                throw new RuntimeException("Cannot create output directory: '" + destinationDirectory + "'.");
            }

            progressCallback.update("Getting list of files...", 0.0);
            long totalFiles = LocalJunrar.getContentsDescription(rarArchive, password).size();

            LocalJunrar.extract(
                    rarArchive,
                    destinationDirectory,
                    password,
                    totalFiles,
                    progressCallback,
                    conflictResolutionProvider);

            return destinationDirectory;
        } catch (RarException | IOException e) {
            throw new RuntimeException("Error while unpacking the archive!", e);
        }
    }
}
