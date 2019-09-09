package ch.japan_impact.japanimpactpos.data.scan;

import androidx.annotation.Nullable;
import ch.japan_impact.japanimpactpos.data.ApiResult;
import ch.japan_impact.japanimpactpos.data.pos.JIItem;

import java.util.Map;

/**
 * The result of a scan agains the API. Any of the fields can be present, depending on the way the
 * remote service works.
 * @author Louis Vialar
 */
public class ScanResult extends ApiResult {
    /**
     * The product corresponding to the barcode, if the scan was successful
     */
    @Nullable
    private JIItem product;
    /**
     * Multiple products corresponding to the barcode, if the scan was successful
     */
    @Nullable
    private Map<JIItem, Integer> products;
    /**
     * Client responsible for the order, if the scan was successful
     */
    @Nullable
    private Client user;

    public ScanResult(@Nullable JIItem product, @Nullable Map<JIItem, Integer> products, @Nullable Client user) {
        super(product != null || products != null || user != null);
        this.product = product;
        this.products = products;
        this.user = user;
    }

    public ScanResult() {
    }

    public static class Client {
        private String lastname;
        private String firstname;
        private String email;

        public Client() {
        }

        public Client(String lastname, String firstname, String email) {
            this.lastname = lastname;
            this.firstname = firstname;
            this.email = email;
        }

        public String getLastname() {
            return lastname;
        }

        public String getFirstname() {
            return firstname;
        }

        public String getEmail() {
            return email;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Client client = (Client) o;

            if (getLastname() != null ? !getLastname().equals(client.getLastname()) : client.getLastname() != null)
                return false;
            if (getFirstname() != null ? !getFirstname().equals(client.getFirstname()) : client.getFirstname() != null)
                return false;
            return getEmail() != null ? getEmail().equals(client.getEmail()) : client.getEmail() == null;
        }

        @Override
        public int hashCode() {
            int result = getLastname() != null ? getLastname().hashCode() : 0;
            result = 31 * result + (getFirstname() != null ? getFirstname().hashCode() : 0);
            result = 31 * result + (getEmail() != null ? getEmail().hashCode() : 0);
            return result;
        }
    }

    @Nullable
    public JIItem getProduct() {
        return product;
    }

    @Nullable
    public Map<JIItem, Integer> getProducts() {
        return products;
    }

    @Nullable
    public Client getUser() {
        return user;
    }
}
