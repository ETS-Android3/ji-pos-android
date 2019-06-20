package ch.japan_impact.japanimpactpos.views.pos;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;
import ch.japan_impact.japanimpactpos.R;
import ch.japan_impact.japanimpactpos.data.JIItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author Louis Vialar
 */
public class Cart {
    private final List<CartedItem> content = new ArrayList<>();
    private final CartedItemAdapter adapter = new CartedItemAdapter();

    private final POSActivity activity;
    private final MutableLiveData<Integer> totalPrice = new MutableLiveData<>();

    public Cart(POSActivity activity) {
        this.activity = activity;
        this.totalPrice.setValue(0);
    }

    public void clear() {
        synchronized (content) {
            this.content.clear();
        }
    }

    public LiveData<Integer> getPrice() {
        return this.totalPrice;
    }

    public CartedItemAdapter getAdapter() {
        return adapter;
    }

    public List<CartedItem> getContent() {
        return Collections.unmodifiableList(content);
    }

    public void addItem(JIItem item) {
        synchronized (content) {
            totalPrice.setValue(totalPrice.getValue() + item.getPrice());

            for (CartedItem c : content) {
                if (c.item.getId() == item.getId()) {
                    c.add(1);
                    return;
                }
            }

            content.add(new CartedItem(item, 1));
            adapter.notifyDataSetChanged();
        }

    }

    public void removeItem(JIItem item) {
        synchronized (content) {
            totalPrice.setValue(totalPrice.getValue() - item.getPrice());

            Iterator<CartedItem> iter = content.iterator();
            CartedItem c;

            while ((c = iter.next()) != null) {
                if (c.item.getId() == item.getId()) {
                    boolean delete = c.remove(1);
                    if (delete) {
                        iter.remove();
                        adapter.notifyDataSetChanged();
                    }
                    return;
                }
            }
        }
    }

    public static class CartedItem {
        private JIItem item;
        private MutableLiveData<Integer> quantity;

        public CartedItem(JIItem item, int quantity) {
            this.item = item;
            this.quantity = new MutableLiveData<>();
            this.quantity.postValue(quantity);
        }

        public JIItem getItem() {
            return item;
        }

        public void setItem(JIItem item) {
            this.item = item;
        }

        public LiveData<Integer> getQuantity() {
            return quantity;
        }

        public void add(int howMany) {
            this.setQuantity(this.quantity.getValue() + 1);
        }

        public boolean remove(int howMany) {
            int q = this.quantity.getValue() - howMany;
            this.setQuantity(q);

            return q <= 0;
        }

        public void setQuantity(int quantity) {
            this.quantity.postValue(quantity);
        }
    }


    class CartedItemAdapter extends RecyclerView.Adapter<CartedItemViewHolder> {
        @NonNull
        @Override
        public CartedItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            View view = layoutInflater.inflate(R.layout.cart_line, parent, false);

            return new CartedItemViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CartedItemViewHolder holder, int position) {
            Log.i("WTF", "Binding item " + position + " " + content.get(position) + " to holder " + holder);
            holder.setItem(content.get(position));
        }

        @Override
        public int getItemCount() {
            return content.size();
        }
    }

    class CartedItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private CartedItem item;
        private TextView itemName;
        private TextView itemPrice;
        private TextView itemQuantity;

        public CartedItemViewHolder(@NonNull View itemView) {
            super(itemView);

            itemView.setOnClickListener(this);
            itemName = itemView.findViewById(R.id.carted_item_name);
            itemPrice = itemView.findViewById(R.id.carted_item_price);
            itemQuantity = itemView.findViewById(R.id.carted_item_quantity);

            itemView.setVisibility(View.VISIBLE);
        }

        @Override
        public void onClick(View v) {
            if (this.item != null) {
                removeItem(this.item.item);
                Toast.makeText(activity, "Retiré: 1x " + item.item.getName(), Toast.LENGTH_SHORT).show();
            }
        }

        @SuppressLint("SetTextI18n")
        void setItem(@Nullable CartedItem item) {
            this.item = item;

            if (item != null) {
                this.itemView.setBackgroundResource(R.drawable.itemborder);
                itemName.setText(item.item.getName());
                item.quantity.observe(activity, integer -> {
                    itemQuantity.setText("x" + integer);
                    itemPrice.setText("Soit " + item.item.getPrice() * integer + " .-");
                });
            }
        }
    }
}
