package pl.marcinchwedczuk.iunrar.gui;

import java.util.prefs.Preferences;

public class AppPreferences {
    private final Preferences preferences = Preferences.userRoot().node("iunrar");

    public boolean getOpenFolderAfterDecompression() {
        return preferences.getBoolean("openFolderAfterDecompression", true);
    }

    public void setOpenFolderAfterDecompression(boolean value) {
        preferences.putBoolean("openFolderAfterDecompression", value);
    }
}
