package pl.marcinchwedczuk.iunrar.gui.decompression;

import com.github.junrar.UnrarCallback;
import com.github.junrar.volume.Volume;

import java.io.File;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class RarUnpacker {
    private final File rarArchive;
    private final String password;
    private final File destinationDirectory;
    private final UnpackProgressCallback progressCallback;

    public RarUnpacker(File rarArchive,
                       Optional<String> password,
                       File destinationDirectory,
                       UnpackProgressCallback progressCallback) {
        this.rarArchive = requireNonNull(rarArchive);
        this.password = password.orElse(null);
        this.destinationDirectory = requireNonNull(destinationDirectory);
        this.progressCallback = requireNonNull(progressCallback);
    }

    public void unpack() {
        try {
            long totalFiles = LocalJunrar.getContentsDescription(
                    rarArchive,
                    password,
                    new RelayProgressCallback("Getting list of files...", progressCallback)
            ).size();

            LocalJunrar.extract(rarArchive, destinationDirectory, password, totalFiles, progressCallback);

        } catch (Exception e) {
            throw new RuntimeException("Error while unpacking archive!", e);
        }
    }

    private class RelayProgressCallback implements UnrarCallback {
        private final String message;
        private final UnpackProgressCallback progressCallback;

        private double lastProgress = -100;

        private RelayProgressCallback(String message, UnpackProgressCallback progressCallback) {
            this.message = message;
            this.progressCallback = progressCallback;
        }

        @Override
        public void volumeProgressChanged(long current, long total) {
            double currentProgress = 100.0 * current / total;
            if (Math.abs(currentProgress - lastProgress) < 1.0) {
                // Avoid to much updates
                return;
            }

            progressCallback.update(message, currentProgress);
            lastProgress = currentProgress;
        }

        @Override
        public boolean isNextVolumeReady(Volume nextVolume) {
            // No idea what this does...
            return true;
        }
    }
}
