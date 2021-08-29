package pl.marcinchwedczuk.iunrar.gui.decompression;

import java.io.File;
import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class RelayPasswordProvider implements PasswordProvider {
    private final String password;

    public RelayPasswordProvider(String password) {
        this.password = password; // can be null
    }

    @Override
    public Optional<String> askForPassword(File archive) {
        return Optional.of(password);
    }
}
