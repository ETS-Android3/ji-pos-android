package ch.japan_impact.japanimpactpos.views.pos;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import ch.japan_impact.japanimpactpos.R;
import ch.japan_impact.japanimpactpos.data.ApiResult;
import ch.japan_impact.japanimpactpos.data.PaymentMethod;
import ch.japan_impact.japanimpactpos.data.pos.PosConfigResponse;
import ch.japan_impact.japanimpactpos.data.pos.PosConfiguration;
import ch.japan_impact.japanimpactpos.data.pos.PosItem;
import ch.japan_impact.japanimpactpos.data.pos.PosOrderResponse;
import ch.japan_impact.japanimpactpos.network.BackendService;
import ch.japan_impact.japanimpactpos.network.exceptions.LoginRequiredException;
import ch.japan_impact.japanimpactpos.network.exceptions.NetworkException;
import ch.japan_impact.japanimpactpos.views.ConfigurationPickerActivity;
import ch.japan_impact.japanimpactpos.views.LoginActivity;
import com.sumup.merchant.reader.api.SumUpAPI;
import com.sumup.merchant.reader.api.SumUpLogin;
import com.sumup.merchant.reader.api.SumUpPayment;
import dagger.android.AndroidInjection;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class POSActivity extends AppCompatActivity {
    private static final int SUMUP_LOGIN_REQUEST_CODE = 1;
    private static final int SUMUP_PREPARE_REQUEST_CODE = 2;
    private static final String TAG = "POSActivity";

    private int configId;
    private int eventId;

    private RecyclerView mPosView;
    private RecyclerView mCartView;
    private Button mCash;
    private Button mCard;
    private TextView mCartPrice;

    private PosConfiguration configuration;
    private PosItem[] items;
    private ItemAdapter adapter;
    private Cart cart;

    private boolean disabled = false;

    private PosOrderResponse currentPayment = null;
    private PaymentMethod currentMethod = null;

    @Inject
    BackendService backendService;

    @Inject
    CartService cartService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pos);

        Intent intent = getIntent();
        this.configId = intent.getIntExtra(ConfigurationPickerActivity.CONFIG_ID, -1);
        this.eventId = intent.getIntExtra(ConfigurationPickerActivity.EVENT_ID, -1);

        if (configId <= 0 || eventId <= 0) {
            Toast.makeText(this, "Numéro de configuration invalide", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        mCartView = findViewById(R.id.cart);
        mPosView = findViewById(R.id.pos_view);
        mCartPrice = findViewById(R.id.cart_total_price);
        mCard = findViewById(R.id.cart_pay_card);
        mCard.setOnClickListener(v -> startPayment(PaymentMethod.CARD, this::cardPayment));
        mCash = findViewById(R.id.cart_pay_cash);
        mCash.setOnClickListener(v -> startPayment(PaymentMethod.CASH, this::cashPayment));

        cart = cartService.createCart(configId, this);
        cart.getPrice().observe(this, p -> mCartPrice.setText("Total : " + p + ".-"));

        adapter = new ItemAdapter();
        mPosView.setAdapter(adapter);
        mPosView.setLayoutManager(new GridLayoutManager(this, 1)); // placeholder

        mCartView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        mCartView.setAdapter(cart.getAdapter());

        load();
    }

    private void setEnabled(boolean enabled) {
        this.disabled = !enabled;
        this.mCash.setEnabled(enabled);
        this.mCard.setEnabled(enabled);
        this.cart.setEnabled(enabled);
    }

    private void startPayment(PaymentMethod method, Consumer<PosOrderResponse> callback) {
        if (this.cart.getOrder().isEmpty()) {
            Toast.makeText(this, "Le panier est vide !", Toast.LENGTH_SHORT).show();
            return;
        }

        this.setEnabled(false);

        // TODO: display wait modal

        Consumer<PosOrderResponse> contPayment = data -> {
            backendService.sendPOSLog(data.getOrderId(), PaymentMethod.CASH, false, method + " native android payment start", new BackendService.ApiCallback<ApiResult>() {
                @Override
                public void onSuccess(ApiResult u) { }

                @Override
                public void onFailure(NetworkException error) {
                    Toast.makeText(POSActivity.this, "Oups... Une erreur s'est produite: " + error.getDescription(), Toast.LENGTH_LONG).show();
                    error.printStackTrace();
                }
            });

            // At the same time, continue payment (don't care if start is not logged)
            callback.accept(data);
        };

        if (this.currentPayment != null && !cart.isChanged()) {
            // Cart didn't change!
            currentMethod = method;

            contPayment.accept(currentPayment);
        } else backendService.placeOrder(this.cart.getOrder(), new BackendService.ApiCallback<PosOrderResponse>() {
            @Override
            public void onSuccess(PosOrderResponse data) {
                cart.setServerResponse(data.getOrderId(), data.getPrice());
                currentPayment = data;
                currentMethod = method;

                contPayment.accept(data);
            }

            @Override
            public void onFailure(NetworkException error) {
                setEnabled(true);

                if (error instanceof LoginRequiredException) {
                    Toast.makeText(POSActivity.this, R.string.requires_login, Toast.LENGTH_LONG).show();

                    startActivity(new Intent(POSActivity.this, LoginActivity.class));
                    finish();
                } else {
                    Toast.makeText(POSActivity.this, error.getDescription(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void cardPayment(PosOrderResponse response) {
        if (response.getPrice() < 1) {
            Toast.makeText(this, "Impossible de gérer par carte des paiements de moins de 1.-", Toast.LENGTH_SHORT).show();
            postPaymentRefused();
            return;
        }

        SumUpPayment payment = SumUpPayment.builder(new BigDecimal(response.getPrice()), SumUpPayment.Currency.CHF)
                .title("JapanImpact")
                .build();

        SumUpAPI.checkout(this, payment, requestCode());

    }

    private void cashPayment(PosOrderResponse response) {
        Intent i = new Intent(this, CashPaymentActivity.class);
        i.putExtra(CashPaymentActivity.PRICE_TO_PAY, response.getPrice());
        startActivityForResult(i, requestCode());
    }

    private int requestCode() {
        return requestCode(currentPayment.getOrderId());
    }

    private static int requestCode(int orderId) {
        return 5 * (orderId % (1 << 15));
    }

    private void setupGrid() {
        adapter.updateGrid(items);

        GridLayoutManager layoutManager = new GridLayoutManager(this, adapter.grid[0].length);
        mPosView.setLayoutManager(layoutManager);
    }

    private void load() {
        adapter.updateGrid(new PosItem[0]);
        backendService.getConfig(this.eventId, this.configId, new BackendService.ApiCallback<PosConfigResponse>() {
            @Override
            public void onSuccess(PosConfigResponse data) {
                configuration = data.getConfig();
                items = data.getItems();
                mCard.setVisibility(data.getConfig().isAcceptCards() ? View.VISIBLE : View.GONE);
                setTitle("Vente : " + configuration.getName());

                // Check cards
                if (data.getConfig().isAcceptCards()) {
                    if (!SumUpAPI.isLoggedIn()) {
                        SumUpAPI.openLoginActivity(POSActivity.this, SumUpLogin.builder(getString(R.string.sumup_affiliate_key)).build(), SUMUP_LOGIN_REQUEST_CODE);
                    } else {
                        SumUpAPI.openPaymentSettingsActivity(POSActivity.this, SUMUP_PREPARE_REQUEST_CODE);
                    }
                }

                setupGrid();
            }

            @Override
            public void onFailure(NetworkException error) {
                if (error instanceof LoginRequiredException) {
                    Toast.makeText(POSActivity.this, R.string.requires_login, Toast.LENGTH_LONG).show();

                    startActivity(new Intent(POSActivity.this, LoginActivity.class));
                    finish();
                } else {
                    Toast.makeText(POSActivity.this, error.getDescription(), Toast.LENGTH_LONG).show();
                    startActivity(new Intent(POSActivity.this, ConfigurationPickerActivity.class));
                    finish();
                }
            }

        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == SUMUP_LOGIN_REQUEST_CODE) {
            if (!SumUpAPI.isLoggedIn()) {
                Toast.makeText(this, "Erreur de connexion SumUp, désactivation des cartes bancaires.", Toast.LENGTH_SHORT).show();
                this.mCard.setVisibility(View.GONE);
            } else {
                SumUpAPI.openPaymentSettingsActivity(this, SUMUP_PREPARE_REQUEST_CODE);
            }
        } else if (currentPayment != null && requestCode == requestCode()) {
            if (currentMethod == PaymentMethod.CARD) {
                if (data != null) {
                    int res = data.getExtras().getInt(SumUpAPI.Response.RESULT_CODE);
                    String txCode = data.getExtras().getString(SumUpAPI.Response.TX_CODE);
                    String msg = data.getExtras().getString(SumUpAPI.Response.MESSAGE);
                    boolean receipt = data.getExtras().getBoolean(SumUpAPI.Response.RECEIPT_SENT);

                    if (res != SumUpAPI.Response.ResultCode.SUCCESSFUL) {
                        Toast.makeText(POSActivity.this, "Erreur SumUp: " + msg, Toast.LENGTH_LONG).show();
                    }

                    confirmPayment(res == SumUpAPI.Response.ResultCode.SUCCESSFUL,
                            c -> backendService.sendPOSLog(currentPayment.getOrderId(), PaymentMethod.CARD, res == SumUpAPI.Response.ResultCode.SUCCESSFUL, msg, txCode, null, receipt, c),
                            () -> "order=" + currentPayment.getOrderId() + ", method=" + currentMethod + ", txCode=" + txCode + ", receipt=" + receipt);
                } else {
                    confirmPayment(false,
                            c -> backendService.sendPOSLog(currentPayment.getOrderId(), PaymentMethod.CARD, false, "Android Native Card Empty Result", c),
                            () -> "order=" + currentPayment.getOrderId() + ", method=" + currentMethod + ", activityResult=" + resultCode);

                }
            } else if (currentMethod == PaymentMethod.CASH) {
                boolean res = data != null && data.getExtras().getBoolean(CashPaymentActivity.IS_SUCCESSFUL_PAYMENT);

                confirmPayment(res,
                        c -> backendService.sendPOSLog(currentPayment.getOrderId(), PaymentMethod.CASH, res, "Android Native cash transaction " + (res ? "success" : "failure"), c),
                        () -> "order=" + currentPayment.getOrderId() + ", method=" + currentMethod + ", activityResult=" + resultCode);
            } else {
                Toast.makeText(this, "Erreur: confirmation de paiement reçue mais aucune méthode configurée.", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == SUMUP_PREPARE_REQUEST_CODE) {
            SumUpAPI.prepareForCheckout();
        } else {
            Toast.makeText(this, "Retour inconnu depuis une application, réactivation.", Toast.LENGTH_LONG).show();
            setEnabled(true);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Logs the payment confirmation and finishes the payment process
     * @param paymentSuccess whether the payment was accepted or not
     * @param logSender a method consumming a callback and sending a POS log
     * @param debugString the main data to write down in case the server refuses to ACK
     */
    private void confirmPayment(boolean paymentSuccess, Consumer<BackendService.ApiCallback<ApiResult>> logSender, Supplier<String> debugString) {
        BackendService.ApiCallback<ApiResult> cb = new BackendService.ApiCallback<ApiResult>() {
            @Override
            public void onSuccess(ApiResult data) {
                if (paymentSuccess) {
                    postPaymentAccepted();
                } else {
                    postPaymentRefused();
                }
            }

            @Override
            public void onFailure(NetworkException error) {
                if (!paymentSuccess) {
                    // C'était un fail de toute façon, ya pas mort d'homme.
                    postPaymentRefused();
                } else {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(POSActivity.this);
                    dialog.setTitle("Erreur de validation");
                    dialog.setMessage("Une erreur s'est produite lors de la validation de la commande : " + error.getDescription() + ".\n\n" +
                            "Vous pouvez réessayer dans quelques instants, ou noter+transmettre les informations suivantes sur un papier et abandonner.\n\n" +
                           debugString.get()
                    );

                    dialog.setCancelable(false);
                    dialog.setPositiveButton("Réessayer", (diag, which) -> confirmPayment(paymentSuccess, logSender, debugString));
                    dialog.setNegativeButton("Abandonner", (diag, which) -> {
                        // Second dialog, because it's needed
                        AlertDialog.Builder secondConfirm = new AlertDialog.Builder(POSActivity.this);
                        secondConfirm.setTitle("Arrêt de validation");
                        secondConfirm.setMessage("Assurez vous d'avoir noté et transmises les informations suivantes :\n\n" +
                                debugString.get() + "\n\n" +
                                "Cliquez sur Non une fois cela fait."
                        );
                        secondConfirm.setPositiveButton("Réessayer", (d, w) -> confirmPayment(paymentSuccess, logSender, debugString));
                        secondConfirm.setNegativeButton("Non", (d, w) -> postPaymentAccepted());

                        secondConfirm.create().show();
                    });

                    dialog.create().show();
                }
            }
        };

        logSender.accept(cb);
    }

    /**
     * Called once the payment has been accepted by the method AND acked by the server. Can also be called without
     * server ack if the user bypasses the error.
     */
    private void postPaymentAccepted() {
        Toast.makeText(POSActivity.this, "Paiement accepté!", Toast.LENGTH_SHORT).show();

        startActivity(OrderSummaryActivity.intent(POSActivity.this, cart.getContent()));

        cart.clear();
        setEnabled(true);
        currentPayment = null;
        currentMethod = null;
    }

    /**
     * Called once the payment has been refused by the method
     */
    private void postPaymentRefused() {
        setEnabled(true);
        cart.resetChangeCounter();

        Toast.makeText(POSActivity.this, "Paiement refusé. N'hésitez pas à réessayer.", Toast.LENGTH_SHORT).show();
    }

    private class ItemAdapter extends RecyclerView.Adapter<ItemViewHolder> {
        private PosItem[][] grid = new PosItem[1][1];
        private int rows;
        private int cols;

        public void updateGrid(PosItem[] items) {
            PosItem[][] grid;
            rows = Arrays.stream(items).map(PosItem::getRow).max(Integer::compare).orElse(0) + 1;
            cols = Arrays.stream(items).map(PosItem::getCol).max(Integer::compare).orElse(0) + 1;

            Log.i("Items", items.toString());
            Log.i("Items", "Grid has " + items.length + " items, " +rows+ "r " + cols + "c");

            grid = new PosItem[rows][cols];

            for (PosItem item : items) {
                Log.i("Items", "At pos " + item.getRow() + " " + item.getCol() + " " + item);
                grid[item.getRow()][item.getCol()] = item;
            }

            this.grid = grid;
            this.notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            View view = layoutInflater.inflate(R.layout.pos_item, parent, false);

            Log.i(TAG, "Create view holder...");

            return new ItemViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
            int row = position / cols;
            int col = position % cols;

            if (row >= grid.length || col >= grid[row].length) {
                Log.w(TAG, "OutOfBounds access! " + row + "/" + col + " (pos " + position + ") with array of size " + grid.length + "x" + grid[0].length);
            } else {
                holder.setItem(grid[row][col]);
            }
        }

        @Override
        public int getItemCount() {
            return grid.length * grid[0].length;
        }
    }

    private class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private PosItem item;
        private TextView itemName;
        private TextView itemPrice;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);

            itemView.setOnClickListener(this);
            itemName = itemView.findViewById(R.id.pos_item_name);
            itemPrice = itemView.findViewById(R.id.pos_item_price);
        }

        @Override
        public void onClick(View v) {
            if (item != null) {
                if (disabled) {
                    Toast.makeText(POSActivity.this, "Le composant est désactivé !", Toast.LENGTH_SHORT).show();
                    return;
                }

                cart.addItem(item.getItem());
                Toast.makeText(POSActivity.this, "Ajouté: " + item.getItem().getName(), Toast.LENGTH_SHORT).show();
            }
        }

        @SuppressLint("SetTextI18n")
        void setItem(@Nullable PosItem item) {
            this.item = item;

            if (item != null) {
                itemName.setText(item.getItem().getName());
                itemPrice.setText(item.getItem().getPrice() + ".-");

                this.itemView.setBackgroundResource(R.drawable.itemborder);
                this.itemView.getBackground().mutate().setTint(BACKGROUNDS.get(item.getColor()));
                this.itemPrice.setTextColor(FOREGROUNDS.get(item.getFontColor()));
                this.itemName.setTextColor(FOREGROUNDS.get(item.getFontColor()));
            }
        }
    }

    private static final Map<String, Integer> BACKGROUNDS = new HashMap<>();
    private static final Map<String, Integer> FOREGROUNDS = new HashMap<>();

    static {
        BACKGROUNDS.put("bg-primary", Color.parseColor("#007bff"));
        BACKGROUNDS.put("bg-secondary", Color.parseColor("#6c757d"));
        BACKGROUNDS.put("bg-success", Color.parseColor("#28a745"));
        BACKGROUNDS.put("bg-danger", Color.parseColor("#dc3545"));
        BACKGROUNDS.put("bg-warning", Color.parseColor("#ffc107"));
        BACKGROUNDS.put("bg-info", Color.parseColor("#17a2b8"));
        BACKGROUNDS.put("bg-light", Color.parseColor("#f8f9fa"));
        BACKGROUNDS.put("bg-dark", Color.parseColor("#343a40"));
        BACKGROUNDS.put("bg-white", Color.parseColor("#FFFFFF"));

        FOREGROUNDS.put("text-primary", Color.parseColor("#007bff"));
        FOREGROUNDS.put("text-success", Color.parseColor("#28a745"));
        FOREGROUNDS.put("text-danger", Color.parseColor("#dc3545"));
        FOREGROUNDS.put("text-warning", Color.parseColor("#ffc107"));
        FOREGROUNDS.put("text-info", Color.parseColor("#17a2b8"));
        FOREGROUNDS.put("text-light", Color.parseColor("#f8f9fa"));
        FOREGROUNDS.put("text-dark", Color.parseColor("#343a40"));
        FOREGROUNDS.put("text-muted", Color.parseColor("#6c757d"));
        FOREGROUNDS.put("text-white", Color.parseColor("#FFFFFF"));
    }
}
