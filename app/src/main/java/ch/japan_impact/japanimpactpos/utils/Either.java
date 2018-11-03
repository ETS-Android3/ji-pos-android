package ch.japan_impact.japanimpactpos.utils;

/**
 * @author Louis Vialar
 */
public class Either<T, U> {
    public final T first;
    public final U second;

    private Either(T t, U u) {
        this.first = t;
        this.second = u;
    }

    public static <T, U> Either<T, U> ofFirst(T t) {
        return new Either<>(t, null);
    }

    public static <T, U> Either<T, U> ofSecond(U u) {
        return new Either<>(null, u);
    }

    public boolean isFirst() {
        return first != null;
    }
}
