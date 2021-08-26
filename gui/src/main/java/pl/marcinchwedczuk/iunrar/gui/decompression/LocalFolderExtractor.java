package pl.marcinchwedczuk.iunrar.gui.decompression;

import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.rarfile.FileHeader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

class LocalFolderExtractor {
    private final long totalFiles;
    private final File folderDestination;
    private final UnpackProgressCallback progressCallback;

    private long extracted = 0;
    private double lastProgress = -100;

    LocalFolderExtractor(long totalFiles, final File destination, UnpackProgressCallback progressCallback) {
        this.totalFiles = totalFiles;
        this.folderDestination = destination;
        this.progressCallback = progressCallback;
    }

    private void updateProgress(String path) {
        extracted++;

        double currentProgress = 100.0 * extracted / totalFiles;
        if (Math.abs(currentProgress - lastProgress) < 1.0) {
            return;
        }

        progressCallback.update("Extracting " + path + "...", currentProgress);
        lastProgress = currentProgress;
    }

    File createDirectory(final FileHeader fh) {
        String fileName = null;
        if (fh.isDirectory()) {
            fileName = fh.getFileName();
        }

        if (fileName == null) {
            return null;
        }

        File f = new File(folderDestination, fileName);
        try {
            String fileCanonPath = f.getCanonicalPath();
            if (!fileCanonPath.startsWith(folderDestination.getCanonicalPath())) {
                String errorMessage = "Rar contains invalid path: '" + fileCanonPath + "'";
                throw new IllegalStateException(errorMessage);
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        updateProgress(fileName);
        return f;
    }

    File extract(
        final Archive arch,
        final FileHeader fileHeader
    ) throws RarException, IOException {
        final File f = createFile(fileHeader, folderDestination);
        try (OutputStream stream = new FileOutputStream(f)) {
            arch.extractFile(fileHeader, stream);
        }
        return f;
    }

    private File createFile(final FileHeader fh, final File destination) throws IOException {
        String name = fh.getFileName();
        File f = new File(destination, name);
        String dirCanonPath = f.getCanonicalPath();
        if (!dirCanonPath.startsWith(destination.getCanonicalPath())) {
            String errorMessage = "Rar contains file with invalid path: '" + dirCanonPath + "'";
            throw new IllegalStateException(errorMessage);
        }
        if (!f.exists()) {
            try {
                f = makeFile(destination, name);
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }

        updateProgress(name);
        return f;
    }

    private File makeFile(final File destination, final String name) throws IOException {
        final String[] dirs = name.split("\\\\");
        String path = "";
        final int size = dirs.length;
        if (size == 1) {
            return new File(destination, name);
        } else if (size > 1) {
            for (int i = 0; i < dirs.length - 1; i++) {
                path = path + File.separator + dirs[i];
                File dir = new File(destination, path);
                dir.mkdir();
            }
            path = path + File.separator + dirs[dirs.length - 1];
            final File f = new File(destination, path);
            f.createNewFile();
            return f;
        } else {
            return null;
        }
    }
}
