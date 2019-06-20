package ch.japan_impact.japanimpactpos.network.exceptions;

/**
 * @author Louis Vialar
 */
public abstract class NetworkException extends Exception {
    public abstract String getDescription();
}
