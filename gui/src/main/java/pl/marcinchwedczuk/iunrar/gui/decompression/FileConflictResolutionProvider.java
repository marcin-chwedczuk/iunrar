package pl.marcinchwedczuk.iunrar.gui.decompression;

import java.io.File;

public interface FileConflictResolutionProvider {
    FileConflictResolution resolveConflict(
            File file, long oldSizeBytes, long newSizeBytes);
}
