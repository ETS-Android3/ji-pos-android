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
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import ch.japan_impact.japanimpactpos.R;
import ch.japan_impact.japanimpactpos.data.pos.PosConfigResponse;
import ch.japan_impact.japanimpactpos.data.pos.PosConfiguration;
import ch.japan_impact.japanimpactpos.data.pos.PosItem;
import ch.japan_impact.japanimpactpos.network.BackendService;
import ch.japan_impact.japanimpactpos.network.exceptions.LoginRequiredException;
import ch.japan_impact.japanimpactpos.network.exceptions.NetworkException;
import ch.japan_impact.japanimpactpos.views.ConfigurationPickerActivity;
import ch.japan_impact.japanimpactpos.views.LoginActivity;
import dagger.android.AndroidInjection;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class POSActivity extends AppCompatActivity {
    public static final String TAG = "POSActivity";
    public static final String POS_CONFIG_ID = "POS_CONFIG_ID";
    public static final String POS_EVENT_ID = "POS_EVENT_ID";

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

    @Inject
    BackendService backendService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pos);

        Intent intent = getIntent();
        this.configId = intent.getIntExtra(POS_CONFIG_ID, -1);
        this.eventId = intent.getIntExtra(POS_EVENT_ID, -1);

        if (configId <= 0 || eventId <= 0) {
            Toast.makeText(this, "Numéro de configuration invalide", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        mCartView = findViewById(R.id.cart);
        mPosView = findViewById(R.id.pos_view);
        mCartPrice = findViewById(R.id.cart_total_price);
        mCard = findViewById(R.id.cart_pay_card);
        mCash = findViewById(R.id.cart_pay_cash);

        cart = new Cart(this);
        cart.getPrice().observe(this, p -> mCartPrice.setText("Total : " + p + ".-"));

        adapter = new ItemAdapter();
        mPosView.setAdapter(adapter);
        mPosView.setLayoutManager(new GridLayoutManager(this, 1)); // placeholder

        mCartView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        mCartView.setAdapter(cart.getAdapter());

        load();
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

    private class ItemAdapter extends RecyclerView.Adapter<ItemViewHolder> {
        private PosItem[][] grid = new PosItem[1][1];

        public void updateGrid(PosItem[] items) {
            PosItem[][] grid;
            int rows = Arrays.stream(items).map(PosItem::getRow).max(Integer::compare).orElse(0) + 1;
            int cols = Arrays.stream(items).map(PosItem::getCol).max(Integer::compare).orElse(0) + 1;

            grid = new PosItem[rows][cols];

            for (PosItem item : items) {
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
            int row = position / grid.length;
            int col = position % grid.length;

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
