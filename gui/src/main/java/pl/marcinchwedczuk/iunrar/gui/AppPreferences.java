package pl.marcinchwedczuk.iunrar.gui;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class AppPreferences {
    private static final String KEY_openFolderAfterUnpacking = "openFolderAfterUnpacking";
    private static final String KEY_removeArchiveAfterUnpacking = "removeArchiveAfterUnpacking";

    private final Preferences preferences = Preferences
            .userNodeForPackage(AppPreferences.class);

    public boolean getOpenFolderAfterUnpacking() {
        return preferences.getBoolean(KEY_openFolderAfterUnpacking, true);
    }

    public AppPreferences setOpenFolderAfterDecompression(boolean value) {
        preferences.putBoolean(KEY_openFolderAfterUnpacking, value);
        return this;
    }

    public boolean getRemoveArchiveAfterUnpacking() {
        return preferences.getBoolean(KEY_removeArchiveAfterUnpacking, false);
    }

    public AppPreferences setRemoveArchiveAfterUnpacking(boolean value) {
        preferences.putBoolean(KEY_removeArchiveAfterUnpacking, value);
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
