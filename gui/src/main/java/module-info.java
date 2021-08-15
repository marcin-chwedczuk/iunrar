module pl.marcinchwedczuk.iunrar.gui {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.swing;
    requires java.desktop;

    // forced to be modular - see ./modularized directory
    requires _modularized_.com.google.common;
    requires _modularized_.org.apache.commons.imaging;

    exports pl.marcinchwedczuk.iunrar.gui;
    exports pl.marcinchwedczuk.iunrar.gui.mainwindow;
    exports pl.marcinchwedczuk.iunrar.gui.aboutdialog;
    exports pl.marcinchwedczuk.iunrar.gui.testfilechooser;

    // Allow @FXML injection to private fields.
    opens pl.marcinchwedczuk.iunrar.gui.mainwindow;
    opens pl.marcinchwedczuk.iunrar.gui.aboutdialog;
    opens pl.marcinchwedczuk.iunrar.gui.testfilechooser;
}