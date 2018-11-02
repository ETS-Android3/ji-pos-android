package ch.japan_impact.japanimpactpos.utils;

/**
 * @author Louis Vialar
 */
public class Either<T, U> {
    private final T t;
    private final U u;

    private Either(T t, U u) {
        this.t = t;
        this.u = u;
    }

    public static <T, U> Either<T, U> ofFirst(T t) {
        return new Either<>(t, null);
    }

    public static <T, U> Either<T, U> ofSecond(U u) {
        return new Either<>(null, u);
    }

    public T getFirst() {
        return t;
    }

    public U getSecond() {
        return u;
    }
}
