package ch.japan_impact.japanimpactpos.views.pos;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import ch.japan_impact.japanimpactpos.R;
import dagger.android.AndroidInjection;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Louis Vialar
 */
public class OrderSummaryActivity extends AppCompatActivity {
    public static final String CART_CONTENT_PARCEL = "CART_CONTENT";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_summary);

        setTitle(getString(R.string.order_summary_title));

        Button close = findViewById(R.id.order_summary_close);
        close.setOnClickListener(l -> finish());

        Intent intent = getIntent();
        List<Cart.CartedItem> list = intent.getParcelableArrayListExtra(CART_CONTENT_PARCEL);

        TableLayout layout = findViewById(R.id.table);

        Log.i("OrderSummary", "got list " + list);

        list.forEach(item -> {
            Log.i("OrderSummary", "got item " + item.item.getName());
            TableRow row = new TableRow(this);

            TextView tv1 = new TextView(this);
            tv1.setText(item.item.getName());

            TextView tv2 = new TextView(this);
            tv2.setText("" + item.quantity);

            row.addView(tv2);
            row.addView(tv1);
            layout.addView(row);
        });

        int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.8);
        int height = (int) (getResources().getDisplayMetrics().heightPixels * 0.5);

        getWindow().setLayout(width, height);
    }

    public static Intent intent(Context ctx, List<Cart.CartedItem> content) {
        Intent i = new Intent(ctx, OrderSummaryActivity.class);
        i.putParcelableArrayListExtra(CART_CONTENT_PARCEL, new ArrayList<>(content));
        return i;
    }
}
