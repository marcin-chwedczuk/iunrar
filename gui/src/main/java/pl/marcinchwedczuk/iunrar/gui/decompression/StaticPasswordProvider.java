package pl.marcinchwedczuk.iunrar.gui.decompression;

import java.io.File;
import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class StaticPasswordProvider implements PasswordProvider {
    /**
     * `null` means no password was specified by the user.
     */
    private final String password;

    public StaticPasswordProvider(String password) {
        this.password = password;
    }

    @Override
    public Optional<String> askForPassword(File archive) {
        return Optional.of(password);
    }
}
