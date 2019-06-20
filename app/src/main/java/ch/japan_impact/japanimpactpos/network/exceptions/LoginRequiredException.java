package ch.japan_impact.japanimpactpos.network.exceptions;

/**
 * @author Louis Vialar
 */
public class LoginRequiredException extends NetworkException {
    @Override
    public String getDescription() {
        return "Il est nécessaire de se connecter pour utiliser ceci.";
    }
}
