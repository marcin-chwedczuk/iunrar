package pl.marcinchwedczuk.iunrar.gui.decompression;

import com.github.junrar.Archive;
import com.github.junrar.ContentDescription;
import com.github.junrar.UnrarCallback;
import com.github.junrar.exception.RarException;
import com.github.junrar.rarfile.FileHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static pl.marcinchwedczuk.iunrar.gui.decompression.FileConflictResolution.OVERWRITE;

class LocalFolderExtractor {
    private final File archiveFile;
    private final File folderDestination;

    private final long totalSize;
    private final AtomicLong unpackedSize = new AtomicLong(0);
    private final WorkerStatus workerStatus;

    private final FileConflictResolutionProvider conflictResolutionProvider;

    private long extracted = 0;
    private double lastProgress = -100;

    LocalFolderExtractor(File archiveFile,
                         long totalSize,
                         File destination,
                         WorkerStatus workerStatus,
                         FileConflictResolutionProvider conflictResolutionProvider) {
        this.archiveFile = archiveFile;
        this.totalSize = totalSize;
        this.folderDestination = destination;
        this.workerStatus = workerStatus;
        this.conflictResolutionProvider = conflictResolutionProvider;
    }

    private void updateProgress(String path) {
        extracted++;

        double currentProgress = 100.0 * extracted / totalSize;
        if (Math.abs(currentProgress - lastProgress) < 1.0) {
            return;
        }

        workerStatus.updateMessage("Extracting " + path + "...");
        workerStatus.updateProgress(currentProgress);
        lastProgress = currentProgress;
    }

    File createDirectory(final FileHeader fh) {
        String fileName = null;
        if (fh.isDirectory()) {
            fileName = FileNameUtil.getFileName(fh);
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
        if (f != null) {
            try (OutputStream stream =
                    new ProgressReportingOutputStreamDecorator(
                            workerStatus,
                            totalSize,
                            unpackedSize,
                            new PauseCancelOutputStreamDecorator(
                                     workerStatus,
                                     new FileOutputStream(f))))
            {
                arch.extractFile(fileHeader, stream);
            }
        }
        return f;
    }

    private File createFile(FileHeader fh,
                            File destination) throws IOException {
        String name = FileNameUtil.getFileName(fh);
        File f = new File(destination, name);
        String dirCanonPath = f.getCanonicalPath();
        if (!dirCanonPath.startsWith(destination.getCanonicalPath())) {
            String errorMessage = "Rar contains file with invalid path: '" + dirCanonPath + "'";
            throw new IllegalStateException(errorMessage);
        }

        FileConflictResolution resolution = OVERWRITE;
        if (f.exists()) {
            resolution = conflictResolutionProvider.resolveConflict(
                    archiveFile,
                    f,
                    f.length(),
                    fh.getUnpSize());
        }

        switch (resolution) {
            case STOP_OPERATION:
                throw new RuntimeException("Stopped by user!");
            case SKIP:
                f = null;
                break;
            case OVERWRITE:
                if (!f.exists()) {
                    try {
                        f = makeFile(destination, name);
                    } catch (final IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                break;
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

    private static final Logger logger = LoggerFactory.getLogger(LocalFolderExtractor.class);

    public static List<File> extract(final File rar,
                                     final File destinationFolder,
                                     final String password,
                                     long totalSize,
                                     final WorkerStatus progressCallback,
                                     FileConflictResolutionProvider conflictResolutionProvider)
            throws RarException, IOException, InterruptedException {
        validateRarPath(rar);
        validateDestinationPath(destinationFolder);

        final Archive archive = createArchiveOrThrowException(rar, password, null);
        LocalFolderExtractor lfe = new LocalFolderExtractor(
                rar, totalSize, destinationFolder, progressCallback, conflictResolutionProvider);
        return extractArchiveTo(archive, lfe);
    }

    public static List<ContentDescription> getContentsDescription(File rar, String password)
            throws RarException, IOException
    {
        validateRarPath(rar);
        final Archive arch = createArchiveOrThrowException(rar, password, null);
        return getContentsDescriptionFromArchive(arch);
    }

    private static List<ContentDescription> getContentsDescriptionFromArchive(final Archive arch) throws RarException, IOException {
        final List<ContentDescription> contents = new ArrayList<>();
        try {
            if (arch.isEncrypted()) {
                logger.warn("archive is encrypted cannot extract");
                return new ArrayList<>();
            }
            for (final FileHeader fileHeader : arch) {
                contents.add(new ContentDescription(fileHeader.getFileName(), fileHeader.getUnpSize()));
            }
        } finally {
            arch.close();
        }
        return contents;
    }

    private static Archive createArchiveOrThrowException(File file,
                                                         String password,
                                                         UnrarCallback progressCallback) throws RarException, IOException {
        try {
            return new Archive(file, progressCallback, password);
        } catch (final RarException | IOException e) {
            logger.error("Error while creating archive", e);
            throw e;
        }
    }

    private static void validateDestinationPath(final File destinationFolder) {
        if (destinationFolder == null) {
            throw new IllegalArgumentException("archive and destination must me set");
        }
        if (!destinationFolder.exists() || !destinationFolder.isDirectory()) {
            throw new IllegalArgumentException("the destination must exist and point to a directory: " + destinationFolder);
        }
    }

    private static void validateRarPath(final File rar) {
        if (rar == null) {
            throw new IllegalArgumentException("archive and destination must me set");
        }
        if (!rar.exists()) {
            throw new IllegalArgumentException("the archive does not exit: " + rar);
        }
        if (!rar.isFile()) {
            throw new IllegalArgumentException("First argument should be a file but was " + rar.getAbsolutePath());
        }
    }

    private static List<File> extractArchiveTo(final Archive arch, final LocalFolderExtractor destination) throws IOException, RarException {
        final List<File> extractedFiles = new ArrayList<>();
        try {
            for (final FileHeader fh : arch) {
                try { Thread.sleep(3000); } catch (InterruptedException e) { }
                try {
                    final File file = tryToExtract(destination, arch, fh);
                    if (file != null) {
                        extractedFiles.add(file);
                    }
                } catch (final IOException | RarException e) {
                    logger.error("error extracting the file", e);
                    throw e;
                }
            }
        } finally {
            arch.close();
        }
        return extractedFiles;
    }

    private static File tryToExtract(
            final LocalFolderExtractor destination,
            final Archive arch,
            final FileHeader fileHeader
    ) throws IOException, RarException {
        final String fileNameString = fileHeader.getFileName();

        logger.info("extracting: " + fileNameString);
        if (fileHeader.isDirectory()) {
            return destination.createDirectory(fileHeader);
        } else {
            return destination.extract(arch, fileHeader);
        }
    }
}
