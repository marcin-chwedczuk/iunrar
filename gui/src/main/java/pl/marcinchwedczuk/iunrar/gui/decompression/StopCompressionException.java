package pl.marcinchwedczuk.iunrar.gui.decompression;

public class StopCompressionException extends RuntimeException {
    public StopCompressionException() {
    }

    public StopCompressionException(String message) {
        super(message);
    }

    public StopCompressionException(String message, Throwable cause) {
        super(message, cause);
    }

    public StopCompressionException(Throwable cause) {
        super(cause);
    }

    public StopCompressionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
