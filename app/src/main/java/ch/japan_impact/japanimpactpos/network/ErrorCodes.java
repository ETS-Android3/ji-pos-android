package ch.japan_impact.japanimpactpos.network;

public enum ErrorCodes {
    USER_OR_PASSWORD_INVALID(201, "Email ou mot de passe incorrect"),
    EMAIL_NOT_CONFIRMED(202, "Email non confirmé"),

    UNKNOWN_ERROR(100, "Une erreur inconnue s'est produite"),
    MISSING_DATA(101, "Erreur lors de l'envoi des données"),
    UNKNOWN_APP(102, "Application mal configurée (erreur 102)"),


    NETWORK_ERROR(-1, "Une erreur réseau s'est produite"),
    ;

    public final int code;
    public final String message;

    ErrorCodes(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public static ErrorCodes fromCode(int errorCode) {
        for (ErrorCodes e : ErrorCodes.values())
            if (e.code == errorCode)
                return e;
        return UNKNOWN_ERROR;
    }
}