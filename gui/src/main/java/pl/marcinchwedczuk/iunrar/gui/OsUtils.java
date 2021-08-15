package pl.marcinchwedczuk.iunrar.gui;

import static java.lang.System.getProperty;

public class OsUtils {
    private OsUtils() { }

    public static boolean isMac() {
        return getProperty("os.name").contains("Mac");
    }
}
