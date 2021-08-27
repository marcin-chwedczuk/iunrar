package pl.marcinchwedczuk.iunrar.gui.decompression;

import com.github.junrar.Archive;
import com.github.junrar.ContentDescription;
import com.github.junrar.UnrarCallback;
import com.github.junrar.exception.RarException;
import com.github.junrar.rarfile.FileHeader;
import com.github.junrar.volume.VolumeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

class LocalJunrar {

    private static final Logger logger = LoggerFactory.getLogger(LocalJunrar.class);

    public static List<File> extract(final File rar,
                                     final File destinationFolder,
                                     final String password,
                                     long totalFiles,
                                     final UnpackProgressCallback progressCallback)
            throws RarException, IOException {
        validateRarPath(rar);
        validateDestinationPath(destinationFolder);

        final Archive archive = createArchiveOrThrowException(rar, password, null);
        LocalFolderExtractor lfe = new LocalFolderExtractor(totalFiles, destinationFolder, progressCallback);
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

    private static Archive createArchiveOrThrowException(final VolumeManager volumeManager, final String password) throws RarException, IOException {
        try {
            return new Archive(volumeManager, null, password);
        } catch (final RarException | IOException e) {
            LocalJunrar.logger.error("Error while creating archive", e);
            throw e;
        }
    }

    private static Archive createArchiveOrThrowException(final InputStream rarAsStream, final String password) throws RarException, IOException {
        try {
            return new Archive(rarAsStream, password);
        } catch (final RarException | IOException e) {
            LocalJunrar.logger.error("Error while creating archive", e);
            throw e;
        }
    }

    private static Archive createArchiveOrThrowException(File file,
                                                         String password,
                                                         UnrarCallback progressCallback) throws RarException, IOException {
        try {
            return new Archive(file, progressCallback, password);
        } catch (final RarException | IOException e) {
            LocalJunrar.logger.error("Error while creating archive", e);
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
                try { Thread.sleep(1000); } catch (InterruptedException e) { }
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

        LocalJunrar.logger.info("extracting: " + fileNameString);
        if (fileHeader.isDirectory()) {
            return destination.createDirectory(fileHeader);
        } else {
            return destination.extract(arch, fileHeader);
        }
    }

}
