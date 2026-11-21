package io.github.mralireza82.xor;

import io.github.mralireza82.baseconversion.Base;
import io.github.mralireza82.baseconversion.ByteEncoding;

import java.util.Objects;

/**
 * XOR the way you do it on paper: write both values as bits, compare each column with the truth table
 * ("different gives 1, same gives 0"), then read the result back. Slower than {@link Xor} but every step
 * is visible; it exists for learning and as a second opinion in tests. Both give identical results.
 */
public final class SimpleXor {
    private SimpleXor() {
    }

    /** The truth table itself: 1 when the bits differ, 0 when they are the same. */
    public static char xorBit(char a, char b) {
        Base.BINARY.valueOf(a);
        Base.BINARY.valueOf(b);
        return a == b ? '0' : '1';
    }

    /** Column by column on bit text: {@code xorBits("1101", "0110")} is {@code "1011"}. */
    public static String xorBits(String a, String b) {
        Objects.requireNonNull(a, "a");
        Objects.requireNonNull(b, "b");
        if (a.length() != b.length()) {
            throw new IllegalArgumentException(
                    "XOR needs inputs of the same length, got " + a.length() + " and " + b.length() + " bits");
        }
        StringBuilder result = new StringBuilder(a.length());
        for (int i = 0; i < a.length(); i++) {
            result.append(xorBit(a.charAt(i), b.charAt(i)));
        }
        return result.toString();
    }

    /** Hex to bits, XOR column by column, bits back to hex. Same number of hex digits required. */
    public static String xorHex(String a, String b) {
        String bitsA = ByteEncoding.toBits(ByteEncoding.fromHex(a));
        String bitsB = ByteEncoding.toBits(ByteEncoding.fromHex(b));
        return ByteEncoding.toHex(ByteEncoding.fromBits(xorBits(bitsA, bitsB)));
    }
}
