package pl.marcinchwedczuk.iunrar.gui.decompression;

import java.io.File;
import java.util.Optional;

public interface PasswordProvider {
    Optional<String> askForPassword(File archive);
}
