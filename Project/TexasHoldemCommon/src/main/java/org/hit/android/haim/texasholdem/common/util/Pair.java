package org.hit.android.haim.texasholdem.common.util;

/**
 * A tuple of two objects.
 * @author Haim Adrian
 * @since 27-Jun-21
 */
public class Pair<S, T> {
    private final S first;
    private final T second;

    /**
     * Constructs a new {@link Pair}
     * @param first First of pair
     * @param second Second of pair
     */
    private Pair(S first, T second) {
        this.first = first;
        this.second = second;
    }

    /**
     * Creates a new {@link Pair} for the given elements.
     * @param first First of pair
     * @param second Second of pair
     * @return The new pair
     */
    public static <S, T> Pair<S, T> of(S first, T second) {
        return new Pair<>(first, second);
    }

    /**
     * @return The first element of the {@link Pair}.
     */
    public S getFirst() {
        return first;
    }

    /**
     * @return The second element of the {@link Pair}.
     */
    public T getSecond() {
        return second;
    }
}

