package org.hit.android.haim.texasholdem.common.model.game;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Haim Adrian
 * @since 22-Jun-21
 */
public class ChipsTest {
    @Test
    public void testChipsShorthand_use1000_expect1K() {
        long amount = 1000;
        String shorthand = "1K";

        Chips chips = new Chips(amount);
        Assertions.assertEquals(shorthand, chips.toShorthand(), "Shorthand of " + amount + " is " + shorthand);
    }

    @Test
    public void testChipsShorthand_use3000000_expect3M() {
        long amount = 3000000;
        String shorthand = "3M";

        Chips chips = new Chips(amount);
        Assertions.assertEquals(shorthand, chips.toShorthand(), "Shorthand of " + amount + " is " + shorthand);
    }

    @Test
    public void testChipsShorthand_use100000000000_expect100B() {
        long amount = 100000000000L;
        String shorthand = "100B";

        Chips chips = new Chips(amount);
        Assertions.assertEquals(shorthand, chips.toShorthand(), "Shorthand of " + amount + " is " + shorthand);
    }

    @Test
    public void testChipsShorthand_use1200_expect1Point2K() {
        long amount = 1200;
        String shorthand = "1.2K";

        Chips chips = new Chips(amount);
        Assertions.assertEquals(shorthand, chips.toShorthand(), "Shorthand of " + amount + " is " + shorthand);
    }

    @Test
    public void testChipsShorthand_use1211_expect1Point2K() {
        long amount = 1211;
        String shorthand = "1.2K";

        Chips chips = new Chips(amount);
        Assertions.assertEquals(shorthand, chips.toShorthand(), "Shorthand of " + amount + " is " + shorthand);
    }

    @Test
    public void testChipsShorthand_use3200000_expect3Point2M() {
        long amount = 3200000;
        String shorthand = "3.2M";

        Chips chips = new Chips(amount);
        Assertions.assertEquals(shorthand, chips.toShorthand(), "Shorthand of " + amount + " is " + shorthand);
    }

    @Test
    public void testChipsShorthand_use3201000_expect3Point2M() {
        long amount = 3201000;
        String shorthand = "3.2M";

        Chips chips = new Chips(amount);
        Assertions.assertEquals(shorthand, chips.toShorthand(), "Shorthand of " + amount + " is " + shorthand);
    }

    @Test
    public void testChipsShorthand_use3211000_expect3Point21M() {
        long amount = 3211000;
        String shorthand = "3.21M";

        Chips chips = new Chips(amount);
        Assertions.assertEquals(shorthand, chips.toShorthand(), "Shorthand of " + amount + " is " + shorthand);
    }

    @Test
    public void testChipsShorthand_use3211020_expect3Point21M() {
        long amount = 3211020;
        String shorthand = "3.21M";

        Chips chips = new Chips(amount);
        Assertions.assertEquals(shorthand, chips.toShorthand(), "Shorthand of " + amount + " is " + shorthand);
    }
}

