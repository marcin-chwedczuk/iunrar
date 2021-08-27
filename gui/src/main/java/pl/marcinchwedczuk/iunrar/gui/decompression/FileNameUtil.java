package pl.marcinchwedczuk.iunrar.gui.decompression;

import com.github.junrar.rarfile.FileHeader;

import java.io.File;

public class FileNameUtil {
    public static String getFileName(FileHeader fh) {
        // Rar archive can contain Windows paths with backslashes as separators.
        // Of course this can break Linux filenames with backslashes in it,
        // but let's be serious what sort of person does that...
        return fh.getFileName().replace('\\', File.separatorChar);
    }
}
