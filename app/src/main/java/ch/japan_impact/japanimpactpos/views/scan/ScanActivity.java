package ch.japan_impact.japanimpactpos.views.scan;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import ch.japan_impact.japanimpactpos.R;
import ch.japan_impact.japanimpactpos.data.pos.JIItem;
import ch.japan_impact.japanimpactpos.data.scan.ScanResult;
import ch.japan_impact.japanimpactpos.network.BackendService;
import ch.japan_impact.japanimpactpos.network.exceptions.ApiException;
import ch.japan_impact.japanimpactpos.network.exceptions.LoginRequiredException;
import ch.japan_impact.japanimpactpos.network.exceptions.NetworkException;
import ch.japan_impact.japanimpactpos.views.ConfigurationPickerActivity;
import ch.japan_impact.japanimpactpos.views.LoginActivity;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.DefaultDecoderFactory;
import dagger.android.AndroidInjection;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Source: https://github.com/journeyapps/zxing-android-embedded/blob/master/sample/src/main/java/example/zxing
 * /ContinuousCaptureActivity.java
 *
 * @author Louis Vialar
 */
public final class ScanActivity extends AppCompatActivity {
    private static final String TAG = "TicketingScanActivity";
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 42; // Magic value
    private static final int OVERLAY_DELAY = 2000;

    private TextView view;
    private DecoratedBarcodeView scanner;
    private View overlay;
    private int configId = -1;
    private SoundAlertManager soundAlertManager;

    @Inject
    BackendService backendService;

    private BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            scanner.pause();
            scanner.setStatusText(result.getText() + " - patientez, scan en cours...");
            view.setText("");

            backendService.scan(configId, result.getText(), new BackendService.ApiCallback<ScanResult>() {
                @Override
                public void onSuccess(ScanResult data) {
                    soundAlertManager.success();
                    setScanResult(buildHtml(data), true);
                    scanner.setStatusText("");
                    resumeScan();
                }

                @Override
                public void onFailure(NetworkException error) {
                    if (error instanceof LoginRequiredException) {
                        Toast.makeText(ScanActivity.this, R.string.requires_login, Toast.LENGTH_LONG).show();
                        startActivity(new Intent(ScanActivity.this, LoginActivity.class));
                        finish();
                    } else if (error instanceof ApiException) {
                        soundAlertManager.failure();

                        setScanResult(buildHtmlForError((ApiException) error), false);

                        scanner.setStatusText("");
                        resumeScan();
                    } else {
                        soundAlertManager.failure();

                        setScanResult(Html.fromHtml("<b style='color: red;'>Erreur inconnue au scan, merci de rééssayer.</b>"), false);

                        scanner.setStatusText("");
                        resumeScan();
                    }
                }
            });
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
        }
    };

    private void initScan() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
        } else {
            scanner.getBarcodeView().setDecoderFactory(new DefaultDecoderFactory(Arrays.asList(BarcodeFormat.values())));
            scanner.initializeFromIntent(new Intent());
        }
    }

    private void setScanResult(Spanned text, Boolean success) {
        int color;
        if (success) {
            color = Color.GREEN;
        } else {
            color = Color.RED;
        }
        view.setText(text);
        view.setTextColor(color);
        overlay.setBackgroundColor(color);
        overlay.setVisibility(View.VISIBLE);
        overlay.postDelayed(() -> overlay.setVisibility(View.INVISIBLE), OVERLAY_DELAY);
    }

    private void appendSuccessData(ScanResult data, StringBuilder html) {
        if (data.getUser() != null) {
            html.append("<br><i>").append(getResources().getString(R.string.ticketing_scan_user)).append("</i>").append(" ").append(data.getUser().getFirstname()).append(" ").append(data.getUser().getLastname()).append(" (").append(data.getUser().getEmail()).append(")");
        }

        if (data.getProduct() != null) {
            html.append("<br><i>").append(getResources().getString(R.string.ticketing_scan_bought_product)).append(
                    "</i>").append(" ").append(data.getProduct().getName()).append(" (<i>").append(data.getProduct().getDescription()).append("</i>)");
        } else if (data.getProducts() != null) {
            html.append("<br><i>").append(getResources().getString(R.string.ticketing_scan_bought_product)).append(
                    "</i>");
            html.append("<ul>");

            for (Map.Entry<JIItem, Integer> p : data.getProducts().entrySet()) {
                html.append("<li>").append(p.getValue()).append(" * ").append(p.getKey().getName()).append(": ").append(p.getKey().getDescription()).append("</li>");
            }

            html.append("</ul>");
        }
    }

    private Spanned buildHtml(ScanResult data) {
        if (data.isSuccess()) {
            StringBuilder html = new StringBuilder();

            html.append("<b color='green'>").append(getResources().getString(R.string.ticketing_scan_success)).append("</b>");


            appendSuccessData(data, html);

            return Html.fromHtml(html.toString());
        } else {
            return Html.fromHtml("<b color='red'>Erreur inconnue, ne laissez pas entrer la personne et réessayez.</b>");
        }
    }

    private Spanned buildHtmlForError(ApiException error) {
        return new ScanErrorParser(error).buildHtml();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticketing_scan);

        this.view = findViewById(R.id.barcode_preview);
        this.scanner = findViewById(R.id.barcode_scanner);
        this.overlay = findViewById(R.id.scan_result_overlay);
        this.soundAlertManager = new SoundAlertManager(ScanActivity.this);

        Intent intent = getIntent();
        this.configId = intent.getIntExtra(ConfigurationPickerActivity.CONFIG_ID, -1);

        if (configId <= 0) {
            Toast.makeText(this, "Numéro de configuration invalide", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setTitle(intent.getStringExtra(ConfigurationPickerActivity.CONFIG_NAME));

        initScan();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initScan();
                } else if (grantResults.length > 0) {
                    Log.e(TAG, "Refused CAMERA access (result " + grantResults[0] + ")");
                    Toast.makeText(this, "Il faut autoriser l'accès à la caméra...", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(this, ConfigurationPickerActivity.class));
                } else {
                    Log.w(TAG, "Permission result with no result");
                }
            }
        }
    }

    private void resumeScan() {
        scanner.resume();
        scanner.decodeContinuous(callback);
    }

    @Override
    protected void onResume() {
        super.onResume();
        resumeScan();
    }

    @Override
    protected void onPause() {
        super.onPause();
        scanner.pause();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return scanner.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        scanner.pause();
        super.onBackPressed();
    }
}
