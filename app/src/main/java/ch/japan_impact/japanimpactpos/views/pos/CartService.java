package ch.japan_impact.japanimpactpos.views.pos;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Louis Vialar
 */
@Singleton
public class CartService {
    private Map<Integer, Cart> carts = new HashMap<>();

    @Inject
    public CartService() {}

    public Cart createCart(int configId, POSActivity activity) {
        carts.put(configId, new Cart(activity));

        return carts.get(configId);
    }

    public Cart getCart(int configId) {
        return carts.get(configId);
    }
}
