package com.psp.pos.shared.xor;

import com.psp.pos.shared.baseconversion.ByteEncoding;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class XorTest {

    @ParameterizedTest(name = "{0} XOR {1} = {2}")
    @CsvSource({
            // The examples used in the article.
            "D6,               6D,               BB",
            "D6,               00,               D6",
            "D6,               FF,               29",
            "D6,               D6,               00",
            "5041594D454E5431, 0000000000000000, 5041594D454E5431",
            "3233000000000000, 355227E11D967D54, 076127E11D967D54",
            "041234FFFFFFFFFF, 0000111111111111, 041225EEEEEEEEEE",
            "0123456789ABCDEF, FEDCBA9876543210, FFFFFFFFFFFFFFFF",
            "c3,               a5,               66",
    })
    void examples(String a, String b, String expected) {
        assertEquals(expected, Xor.xorHex(a, b));
        assertEquals(expected, SimpleXor.xorHex(a, b));
    }

    @Test
    void truthTable() {
        assertEquals('0', SimpleXor.xorBit('0', '0'));
        assertEquals('1', SimpleXor.xorBit('0', '1'));
        assertEquals('1', SimpleXor.xorBit('1', '0'));
        assertEquals('0', SimpleXor.xorBit('1', '1'));
        assertEquals("1011", SimpleXor.xorBits("1101", "0110"));
    }

    @Test
    void propertiesHoldForRandomData() {
        Random random = new Random(7);
        for (int round = 0; round < 1_000; round++) {
            int length = random.nextInt(33);
            byte[] a = randomBytes(random, length);
            byte[] b = randomBytes(random, length);
            byte[] c = randomBytes(random, length);
            byte[] zero = new byte[length];

            assertArrayEquals(a, Xor.xor(a, zero), "a ^ 0 = a");
            assertArrayEquals(zero, Xor.xor(a, a), "a ^ a = 0");
            assertArrayEquals(Xor.xor(a, b), Xor.xor(b, a), "commutative");
            assertArrayEquals(Xor.xor(Xor.xor(a, b), c), Xor.xor(a, Xor.xor(b, c)), "associative");
            assertArrayEquals(a, Xor.xor(Xor.xor(a, b), b), "XOR with the same value undoes itself");
            assertArrayEquals(Xor.combine(a, b, c), Xor.xor(Xor.xor(a, b), c));
            assertEquals(ByteEncoding.toHex(Xor.xor(a, b)),
                    SimpleXor.xorHex(ByteEncoding.toHex(a), ByteEncoding.toHex(b)), "simple = quick");
        }
    }

    @Test
    void keyComponentsCombineIntoTheKey() {
        byte[] component1 = ByteEncoding.fromHex("1C2D3E4F5A6B7C8D");
        byte[] component2 = ByteEncoding.fromHex("1D0E7B28D3C0B162");
        assertEquals("0123456789ABCDEF", ByteEncoding.toHex(Xor.combine(component1, component2)));
    }

    @Test
    void lrcIsAllBytesXoredTogether() {
        assertEquals((byte) 0x00, Xor.lrc(new byte[0]));
        assertEquals((byte) 0x01, Xor.lrc(ByteEncoding.fromHex("3031")));
        assertEquals((byte) 0x07, Xor.lrc(ByteEncoding.fromHex("3132333403")));
    }

    @Test
    void inputsAreNotChanged() {
        byte[] a = ByteEncoding.fromHex("D6");
        Xor.xor(a, ByteEncoding.fromHex("FF"));
        Xor.combine(a, ByteEncoding.fromHex("FF"));
        assertEquals("D6", ByteEncoding.toHex(a));
    }

    @Test
    void differentLengthsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> Xor.xorHex("D6", "00D6"));
        assertThrows(IllegalArgumentException.class, () -> SimpleXor.xorBits("1", "01"));
        assertThrows(IllegalArgumentException.class, () -> Xor.combine(new byte[1]));
        assertThrows(IllegalArgumentException.class, () -> SimpleXor.xorBit('2', '0'));
    }

    private static byte[] randomBytes(Random random, int length) {
        byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        return bytes;
    }
}
