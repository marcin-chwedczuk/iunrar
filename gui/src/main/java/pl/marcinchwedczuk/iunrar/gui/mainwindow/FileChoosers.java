package pl.marcinchwedczuk.iunrar.gui.mainwindow;

import javafx.stage.FileChooser;

import java.io.File;

public class FileChoosers {
    public static FileChooser newOpenArchiveFileChooser() {
        FileChooser fileChooser = new FileChooser();

        fileChooser.setTitle("Select archive...");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Archives", "*.rar", "*.cbr"),
                new FileChooser.ExtensionFilter("RAR Archives", "*.rar", "*.cbr")
        );

        return fileChooser;
    }
}
