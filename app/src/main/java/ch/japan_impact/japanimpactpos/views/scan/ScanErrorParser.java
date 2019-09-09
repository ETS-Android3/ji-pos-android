package ch.japan_impact.japanimpactpos.views.scan;

import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import ch.japan_impact.japanimpactpos.network.exceptions.ApiException;
import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Louis Vialar
 */
public class ScanErrorParser {
    private final ApiException error;

    private static enum ErrorCodes {
        NOT_FOUND("error.not_found", "{key} introuvable"),
        PRODUCT_NOT_ALLOWED("error.product_not_allowed", "Billet valide mais non autorisé dans la " +
                "configuration actuelle. Refusez le billet et orientez le client vers la bonne file. Ne laissez pas " +
                "rentrer le client."),

        PRODUCTS_ONLY("error.product_only_configuration", "Billet de goodies non autorisé par cette configuration. Ce billet doit être utilisé sur le stand PolyJapan."),
        ALREADY_SCANNED("error.ticket_already_scanned", "Billet déjà scanné.");

        public final String code;
        public final String message;
        private static final Map<String, ErrorCodes> codeToCode = new HashMap<>();

        static {
            for (ErrorCodes ec : values()) codeToCode.put(ec.code, ec);
        }

        public static ErrorCodes fromCode(String code) {
            return codeToCode.get(code);
        }

        ErrorCodes(String code, String message) {
            this.code = code;
            this.message = message;
        }
    }

    public ScanErrorParser(ApiException error) {
        this.error = error;
    }

    public Spanned buildHtml() {
        Log.i("ScanErrorParser", "Error content", error);
        Log.i("ScanErrorParser", "Messages content " + error.messages);

        String messages = error.messages.stream()
                .map(ErrorCodes::fromCode).map(c -> c.message.replace("{key}", error.key))
                .collect(Collectors.joining("; "));

        StringBuilder html = new StringBuilder("<b style='color: red;'>Billet refusé :(</b><br><i>").append(messages).append("</i>");

        if (error.messages.contains(ErrorCodes.ALREADY_SCANNED.code) && error.args.size() > 0 && error.args.get(0).has("scannedAt") && error.args.get(0).has("scannedBy")) {
            try {
                html = html.append("<br>").append("Billet validé par ").append(error.args.get(0).getString("scannedBy")).append(" le ").append(SimpleDateFormat.getDateTimeInstance().format(new Date(error.args.get(0).getLong("scannedAt"))));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return Html.fromHtml(html.toString());
    }
}
