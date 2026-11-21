package io.github.mralireza82.xor;

import io.github.mralireza82.baseconversion.ByteEncoding;

import java.util.Objects;

/**
 * Bitwise exclusive OR (XOR) on data, the quick way: Java's {@code ^} operator, one byte at a time.
 * Each output bit is 1 when the two input bits differ and 0 when they are the same.
 *
 * <p>XOR works on <strong>data</strong>, so both inputs must have exactly the same length; nothing is
 * padded or cut silently. Inputs are never modified; every method returns a new array.
 * {@link SimpleXor} does the same bit by bit with the truth table, for learning.
 */
public final class Xor {
    private Xor() {
    }

    /** {@code a XOR b}, byte by byte. Both arrays must have the same length. */
    public static byte[] xor(byte[] a, byte[] b) {
        requireSameLength(a, b);
        byte[] result = new byte[a.length];
        for (int i = 0; i < a.length; i++) {
            result[i] = (byte) (a[i] ^ b[i]);
        }
        return result;
    }

    /** {@code a XOR b} on hex text: {@code xorHex("D6", "6D")} is {@code "BB"}. Same number of digits required. */
    public static String xorHex(String a, String b) {
        return ByteEncoding.toHex(xor(ByteEncoding.fromHex(a), ByteEncoding.fromHex(b)));
    }

    /**
     * XORs all parts together: {@code parts[0] XOR parts[1] XOR ...}. This is how a clear key is formed
     * from key components held by different custodians. Needs at least two parts of the same length.
     */
    public static byte[] combine(byte[]... parts) {
        Objects.requireNonNull(parts, "parts");
        if (parts.length < 2) {
            throw new IllegalArgumentException("combining needs at least two parts, got " + parts.length);
        }
        byte[] result = parts[0].clone();
        for (int p = 1; p < parts.length; p++) {
            requireSameLength(result, parts[p]);
            for (int i = 0; i < result.length; i++) {
                result[i] ^= parts[p][i];
            }
        }
        return result;
    }

    /**
     * Longitudinal redundancy check: all bytes XORed into one. Terminal, card reader and cash register
     * links send it after a frame so the receiver can detect a changed bit. Empty data gives 0.
     */
    public static byte lrc(byte[] data) {
        Objects.requireNonNull(data, "data");
        byte check = 0;
        for (byte b : data) {
            check ^= b;
        }
        return check;
    }

    static void requireSameLength(byte[] a, byte[] b) {
        Objects.requireNonNull(a, "a");
        Objects.requireNonNull(b, "b");
        if (a.length != b.length) {
            throw new IllegalArgumentException(
                    "XOR needs inputs of the same length, got " + a.length + " and " + b.length + " bytes");
        }
    }
}
