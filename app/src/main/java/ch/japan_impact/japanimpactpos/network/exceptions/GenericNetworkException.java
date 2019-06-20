package ch.japan_impact.japanimpactpos.network.exceptions;

/**
 * @author Louis Vialar
 */
public class GenericNetworkException extends NetworkException {
    private final String message;

    public GenericNetworkException(String message) {
        this.message = message;
    }

    @Override
    public String getDescription() {
        return message;
    }
}
