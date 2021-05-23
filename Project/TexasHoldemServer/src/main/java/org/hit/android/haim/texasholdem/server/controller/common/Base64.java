package org.hit.android.haim.texasholdem.server.controller.common;

import java.nio.charset.StandardCharsets;

/**
 * Wrap {@link java.util.Base64 Base64}'s {@link java.util.Base64.Encoder Encoder} and {@link java.util.Base64.Decoder Decoder}
 *
 * @author Haim Adrian
 * @since 08-May-21
 */
public class Base64 {
    /**
     * An {@link java.util.Base64.Encoder encoder} we use in order to make game hashes<br/>
     * We do not want padding in the hashes, to make it easier for players to write the hashes without
     * irrelevant characters.
     */
    private static final java.util.Base64.Encoder encoder = java.util.Base64.getUrlEncoder().withoutPadding();

    /**
     * An {@link java.util.Base64.Decoder decoder} we use in order to get identifier out of game hash
     */
    private static final java.util.Base64.Decoder decoder = java.util.Base64.getUrlDecoder();

    private Base64() {

    }

    public static String encodeToString(int integer) {
        return encodeToString(String.valueOf(integer));
    }

    public static String encodeToString(String string) {
        return encodeToString(string.getBytes(StandardCharsets.UTF_8));
    }

    public static String encodeToString(byte[] bytes) {
        return new String(encode(bytes));
    }

    public static byte[] encode(byte[] bytes) {
        return encoder.encode(bytes);
    }

    public static int decodeToInt(String string) {
        int result;

        try {
            result = Integer.parseInt(decodeToString(string));
        } catch (NumberFormatException ignore) {
            result = -1;
        }

        return result;
    }

    public static String decodeToString(String string) {
        return decodeToString(string.getBytes(StandardCharsets.UTF_8));
    }

    public static String decodeToString(byte[] bytes) {
        return new String(decode(bytes));
    }

    public static byte[] decode(byte[] bytes) {
        return decoder.decode(bytes);
    }
}

