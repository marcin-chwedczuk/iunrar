module pl.marcinchwedczuk.iunrar.gui {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.swing;
    requires java.desktop;
    requires java.prefs;

    // forced to be modular - see ./modularized directory
    requires _modularized_.com.google.common;
    requires _modularized_.com.github.junrar;

    exports pl.marcinchwedczuk.iunrar.gui;
    exports pl.marcinchwedczuk.iunrar.gui.mainwindow;
    // exports pl.marcinchwedczuk.iunrar.gui.decompressionqueue;
    exports pl.marcinchwedczuk.iunrar.gui.aboutdialog;
    exports pl.marcinchwedczuk.iunrar.gui.testfilechooser;

    // Allow @FXML injection to private fields.
    opens pl.marcinchwedczuk.iunrar.gui.mainwindow;
    opens pl.marcinchwedczuk.iunrar.gui.decompressionqueue;
    opens pl.marcinchwedczuk.iunrar.gui.aboutdialog;
    opens pl.marcinchwedczuk.iunrar.gui.testfilechooser;
}