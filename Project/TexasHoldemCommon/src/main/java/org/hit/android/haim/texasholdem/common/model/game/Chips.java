package org.hit.android.haim.texasholdem.common.model.game;

import java.text.DecimalFormat;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * A class holding amount of chips, and has special string representation of them.<br/>
 * In order to minimize the length of chips amount, we use characters that replace zeroes. For example
 * instead of drawing "1,000", we will draw "1K", and instead of "1,400,000", we will use "1.4M".
 * @author Haim Adrian
 * @since 22-Jun-21
 */
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Chips {
    private static final double THOUSAND = 1e3;
    private static final double MILLION = 1e6;
    private static final double BILLION = 1e9;

    /**
     * Formatter to format amount of chips with commas
     */
    private static final DecimalFormat formatter = new DecimalFormat("#,###");

    /**
     * Amount of chips.
     */
    private long chips;

    /**
     * @return A short representation of the chips. e.g 1000 -> 1K
     */
    public String toShorthand() {
        return format(chips);
    }

    /**
     * Set a new amount of chips
     * @return The old value
     */
    public long set(long newValue) {
        long oldValue = chips;
        chips = newValue;
        return oldValue;
    }

    /**
     * @return The amount of chips
     */
    public long get() {
        return chips;
    }

    /**
     * @return String representation of the chips, with commas to separate thousands
     */
    public String getFormatted() {
        return formatter.format(chips);
    }

    /**
     * Add some chips to this chips instance
     * @param chips The amount of chips to add
     * @throws IllegalArgumentException In case the new amount will be less than zero
     */
    public void add(long chips) {
        if (this.chips + chips < 0) {
            throw new IllegalArgumentException("Amount of chips cannot be negative");
        }

        this.chips += chips;
    }

    /**
     * Remove some chips from this chips instance.
     * @param chips The amount of chips to remove
     * @throws IllegalArgumentException In case the new amount will be less than zero
     */
    public void remove(long chips) {
        add(-1 * chips);
    }

    @Override
    public String toString() {
        return chips + " -> " + toShorthand();
    }

    private static String format(long chips) {
        if (chips < THOUSAND) {
            return String.valueOf(chips);
        }

        StringBuilder shorthand = new StringBuilder();
        if (chips < MILLION) {
            shorthand.append(String.format("%.1fK", chips / THOUSAND));
        } else if (chips < BILLION) {
            shorthand.append(String.format("%.2fM", chips / MILLION));
        } else {
            shorthand.append(String.format("%.3fB", chips / BILLION));
        }

        // No reason to paint trailing zeroes. We'd like to show 1K and not 1.0K, or 1.2 and not 1.20
        while (shorthand.charAt(shorthand.length() - 2) == '0') {
            shorthand.deleteCharAt(shorthand.length() - 2);
        }

        // In case the last character is a dot, remove it, so we will show 1K and not 1.K
        // This might happen when we remove trailing zeroes
        if (shorthand.charAt(shorthand.length() - 2) == '.') {
            shorthand.deleteCharAt(shorthand.length() - 2);
        }

        return shorthand.toString();
    }
}

