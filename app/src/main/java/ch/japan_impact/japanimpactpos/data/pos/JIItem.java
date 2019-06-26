package ch.japan_impact.japanimpactpos.data.pos;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Louis Vialar
 */
public class JIItem implements Parcelable {
    private int id;
    private String name;
    private int price;
    // Skipped: description, longDescription, maxItems, eventId, isTicket, freePrice, isVisible

    public JIItem(int id, String name, int price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public JIItem() {
    }

    protected JIItem(Parcel in) {
        id = in.readInt();
        name = in.readString();
        price = in.readInt();
    }

    public static final Creator<JIItem> CREATOR = new Creator<JIItem>() {
        @Override
        public JIItem createFromParcel(Parcel in) {
            return new JIItem(in);
        }

        @Override
        public JIItem[] newArray(int size) {
            return new JIItem[size];
        }
    };

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeInt(price);
    }
}
