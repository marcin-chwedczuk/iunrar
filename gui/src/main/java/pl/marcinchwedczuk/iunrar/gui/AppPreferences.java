package pl.marcinchwedczuk.iunrar.gui;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class AppPreferences {
    private final Preferences preferences = Preferences
            .userNodeForPackage(AppPreferences.class);

    public boolean getOpenFolderAfterDecompression() {
        return preferences.getBoolean("openFolderAfterDecompression", true);
    }

    public AppPreferences setOpenFolderAfterDecompression(boolean value) {
        preferences.putBoolean("openFolderAfterDecompression", value);
        return this;
    }

    public void save() {
        try {
            preferences.flush();
        } catch (BackingStoreException e) {
            throw new RuntimeException("Cannot save app preferences.", e);
        }
    }
}
