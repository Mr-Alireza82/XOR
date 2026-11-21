# ⊕ XOR — Exclusive OR

> A from-zero guide to XOR, the simplest and most used operation in cryptography: what it means, why it can undo itself, how to calculate it by hand (one simple and one quick way), and where payment systems use it — key components, PIN blocks, Retail MAC chaining and LRC checksums.

> [!NOTE]
> **Prerequisite:** this article works on bits, bytes and hexadecimal. If `D6`, `1101 0110` and "two hex digits = one byte" are not yet familiar, read [Base Conversion — Bits, Bytes and the Four Number Bases](https://github.com/Mr-Alireza82/Base-Conversion) first. Links like *[BC §5]* point to its sections.

---

## Table of Contents

**Part A — Understanding (no math)**

1. [What Is a Logical Operation?](#1-what-is-a-logical-operation)
2. [What Is XOR?](#2-what-is-xor)
3. [XOR in the Real World](#3-xor-in-the-real-world)
4. [XOR Next to AND, OR and NOT](#4-xor-next-to-and-or-and-not)

**Part B — The Math**

5. [Definition](#5-definition)
6. [The Five Properties](#6-the-five-properties)
7. [XOR Is Addition Without Carry](#7-xor-is-addition-without-carry)

**Part C — Calculating**

8. [From Bits to Bytes: Bitwise XOR](#8-from-bits-to-bytes-bitwise-xor)
9. [Simple Way — Through Binary](#9-simple-way--through-binary)
10. [Quick Way — Nibble by Nibble](#10-quick-way--nibble-by-nibble)

**Part D — XOR in Payment Systems**

11. [Hiding and Revealing — Why XOR Is in Every Cipher](#11-hiding-and-revealing--why-xor-is-in-every-cipher)
12. [Key Components](#12-key-components)
13. [PIN Block — ISO 9564 Format 0](#13-pin-block--iso-9564-format-0)
14. [Retail MAC and CBC Chaining](#14-retail-mac-and-cbc-chaining)
15. [LRC — The Frame Checksum](#15-lrc--the-frame-checksum)

**Part E — In Code**

16. [XOR in Java](#16-xor-in-java)
17. [Using the Library](#17-using-the-library)

**Part F — Reference**

18. [Cheat Sheet](#18-cheat-sheet)
19. [Practice](#19-practice)
20. [Conclusion](#20-conclusion)

---

# Part A — Understanding (no math)

## 1. What Is a Logical Operation?

A **bit** is one yes/no answer *[[BC §4](https://github.com/Mr-Alireza82/Base-Conversion#4-what-is-a-bit)]*. A **logical operation** takes one or two of those answers and gives a new yes/no answer by a fixed rule.

You use them in plain language every day:

| Sentence | Rule |
|---|---|
| "You may enter if you have a ticket **and** an ID." | Both must be yes → **AND** |
| "You may pay with cash **or** card." | At least one yes → **OR** |
| "The shop is **not** open." | Turn the answer around → **NOT** |
| "You get soup **or** salad (but not both)." | Exactly one yes → **XOR** |

The last one is the subject of this article.

---

## 2. What Is XOR?

**XOR** means **eXclusive OR**: *one or the other, but not both*.

Put simply, XOR answers one question:

> **"Are these two bits different?"**

| First bit | Second bit | Different? | XOR |
|:---:|:---:|:---:|:---:|
| 0 | 0 | no | **0** |
| 0 | 1 | yes | **1** |
| 1 | 0 | yes | **1** |
| 1 | 1 | no | **0** |

That table — the **truth table** — is all of XOR. Everything else in this article follows from it.

There is a second way to read the same table, and it is the one cryptography cares about:

> **XOR with 1 flips the bit. XOR with 0 leaves it alone.**

| Bit | XOR with | Result | What happened |
|:---:|:---:|:---:|---|
| 0 | 0 | 0 | kept |
| 1 | 0 | 1 | kept |
| 0 | 1 | 1 | flipped |
| 1 | 1 | 0 | flipped |

So the second value works like a **mask** or a **stencil**: wherever it has a `1`, the first value's bit is flipped.

---

## 3. XOR in the Real World

### The staircase light

A staircase has a light switch at the bottom and another at the top. Flipping **either** switch changes the light. That is XOR: the light is on when the two switches are in *different* positions.

| Bottom switch | Top switch | Light |
|:---:|:---:|:---:|
| down | down | off |
| down | up | **on** |
| up | down | **on** |
| up | up | off |

### Flip it twice, and it's back

Press the staircase switch once: the light changes. Press it again: the light is back to where it was. Flipping twice is the same as not flipping at all. This "undo by doing it again" is the reason XOR is everywhere in cryptography (see [§11](#11-hiding-and-revealing--why-xor-is-in-every-cipher)).

### Spot the difference

Lay two equal pictures on top of each other and mark every point where they differ. The marks *are* the XOR of the two pictures. Where they are the same, XOR gives 0; where they differ, it gives 1. A result of all zeros means "identical".

### Odd or even

XOR many bits together and the result tells you whether the number of `1`s is **odd** (result 1) or **even** (result 0). That is **parity**, the oldest error check: if one bit changes on the way, the parity changes too.

---

## 4. XOR Next to AND, OR and NOT

| A | B | A AND B | A OR B | A XOR B | NOT A |
|:---:|:---:|:---:|:---:|:---:|:---:|
| 0 | 0 | 0 | 0 | **0** | 1 |
| 0 | 1 | 0 | 1 | **1** | 1 |
| 1 | 0 | 0 | 1 | **1** | 0 |
| 1 | 1 | 1 | 1 | **0** | 0 |

XOR and OR differ in exactly **one** row: when both bits are 1. OR says "yes, at least one"; XOR says "no, not exactly one".

| Operation | Symbol (math) | Java operator | Question it answers |
|---|---|:---:|---|
| AND | $\land$ | `&` | Are both 1? |
| OR | $\lor$ | `\|` | Is at least one 1? |
| XOR | $\oplus$ | `^` | Are they different? |
| NOT | $\lnot$ | `~` | What is the opposite? |

> [!TIP]
> Of these four, only XOR can be **undone without losing information**. AND with 0 destroys the bit (always 0); OR with 1 destroys it (always 1). XOR only ever flips or keeps, so every result can be turned back.

---

# Part B — The Math

## 5. Definition

For single bits $a, b \in \{0, 1\}$:

$$\boxed{a \oplus b = (a + b) \bmod 2}$$

Add the two bits and keep only the remainder after dividing by 2: $0+0=0$, $0+1=1$, $1+0=1$, $1+1=2 \to 0$.

The same thing written with AND, OR and NOT:

$$a \oplus b = (a \land \lnot b) \lor (\lnot a \land b) = (a \lor b) \land \lnot(a \land b)$$

In words: *"$a$ but not $b$, or $b$ but not $a$"* — or — *"at least one, but not both"*.

For values with many bits, XOR is applied **to each position separately** (bitwise). For $n$-bit values $A = a_{n-1} \dots a_0$ and $B = b_{n-1} \dots b_0$:

$$(A \oplus B)_i = a_i \oplus b_i \qquad \text{for every position } i$$

No position affects any other.

---

## 6. The Five Properties

| # | Name | Rule | Meaning |
|---|---|---|---|
| P1 | Identity | $A \oplus 0 = A$ | XOR with zero changes nothing. |
| P2 | Self-inverse | $A \oplus A = 0$ | Anything XOR itself is zero. |
| P3 | Commutative | $A \oplus B = B \oplus A$ | Order of the two doesn't matter. |
| P4 | Associative | $(A \oplus B) \oplus C = A \oplus (B \oplus C)$ | Grouping doesn't matter. |
| P5 | Complement | $A \oplus 1\dots1 = \lnot A$ | XOR with all ones flips everything. |

Put P1–P4 together and you get the most important result in this article:

$$\boxed{(A \oplus K) \oplus K = A \oplus (K \oplus K) = A \oplus 0 = A}$$

**XOR with the same value twice gives back the original.** Doing it once hides $A$; doing it again reveals it. The tool to lock and the tool to unlock are the same.

It also means you can move a value to the other side of an equation, like subtraction:

$$A \oplus B = C \quad\Longleftrightarrow\quad A = C \oplus B \quad\Longleftrightarrow\quad B = C \oplus A$$

<details>
<summary>Proof of P1–P4 with the truth table</summary>

Bitwise XOR works position by position, so it is enough to prove each property for single bits by checking every case.

**P1** — $a \oplus 0$: $0 \oplus 0 = 0$, $1 \oplus 0 = 1$. Same as $a$. ✓

**P2** — $a \oplus a$: $0 \oplus 0 = 0$, $1 \oplus 1 = 0$. Always 0. ✓

**P3** — the truth table is symmetric: $0 \oplus 1 = 1 = 1 \oplus 0$. ✓

**P4** — $(a + b) \bmod 2 + c$, then $\bmod 2$ is the same as $(a + b + c) \bmod 2$, which does not depend on grouping. ✓

</details>

---

## 7. XOR Is Addition Without Carry

Normal addition in binary carries a 1 to the next position when $1 + 1 = 10_2$. XOR does the same addition but **throws the carry away**. That is why no position affects any other.

Take our running number from Base Conversion, $D6_{16} = 214$, and $6D_{16} = 109$:

| | Hex | Bits | Decimal |
|---|:---:|:---:|---:|
| A | `D6` | `1101 0110` | 214 |
| B | `6D` | `0110 1101` | 109 |
| A + B (with carry) | `143` | `1 0100 0011` | 323 |
| **A ⊕ B (no carry)** | **`BB`** | **`1011 1011`** | 187 |
| A AND B | `44` | `0100 0100` | 68 |
| A OR B | `FF` | `1111 1111` | 255 |

Two things to notice:

- Addition grew by one bit (a carry left the byte). **XOR never grows**: 8 bits in, 8 bits out.
- XOR is **not** a calculation on amounts. $214 \oplus 109 = 187$ has no meaning as money or distance. XOR is an operation on **data** — on bit patterns *[[BC §7](https://github.com/Mr-Alireza82/Base-Conversion#7-number-or-data--the-most-important-distinction)]*.

---

# Part C — Calculating

## 8. From Bits to Bytes: Bitwise XOR

To XOR two bytes, line them up and apply the truth table to each column:

| Bit position | 7 | 6 | 5 | 4 | 3 | 2 | 1 | 0 |
|---|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
| A = `D6` | 1 | 1 | 0 | 1 | 0 | 1 | 1 | 0 |
| B = `6D` | 0 | 1 | 1 | 0 | 1 | 1 | 0 | 1 |
| Different? | ✓ | | ✓ | ✓ | ✓ | | ✓ | ✓ |
| **A ⊕ B** | **1** | **0** | **1** | **1** | **1** | **0** | **1** | **1** |

Result: `1011 1011` = **`BB`**.

For longer data — 8-byte blocks, 16-byte keys — do the same for every byte. Byte 1 with byte 1, byte 2 with byte 2, and so on.

> [!IMPORTANT]
> **Both values must have exactly the same length.** In payment systems XOR works on data blocks (8 bytes for DES, 16 for AES). If the lengths differ, something upstream is wrong — a missing padding step or a lost leading `00` byte. Never pad or cut silently to make XOR "work".

---

## 9. Simple Way — Through Binary

1. Convert each hex digit to its 4 bits *[[BC §15.1](https://github.com/Mr-Alireza82/Base-Conversion#151-hexadecimal--binary)]*.
2. XOR each column with the truth table: **same → 0, different → 1**.
3. Group the result into 4 bits and convert back to hex *[[BC §12.3](https://github.com/Mr-Alireza82/Base-Conversion#123-binary--hexadecimal)]*.

Example: `C3 ⊕ A5`.

| Step | High nibble | Low nibble |
|---|:---:|:---:|
| A (hex) | `C` | `3` |
| B (hex) | `A` | `5` |
| A (bits) | `1100` | `0011` |
| B (bits) | `1010` | `0101` |
| A ⊕ B (bits) | `0110` | `0110` |
| **A ⊕ B (hex)** | **`6`** | **`6`** |

Result: **`66`**.

Slow, but every step can be checked, and it needs nothing but the truth table.

---

## 10. Quick Way — Nibble by Nibble

Hex digits are independent groups of 4 bits, so you can XOR **one hex digit at a time** without writing bits.

### Trick: the 8-4-2-1 weights

Every hex digit is a sum of some of the weights **8, 4, 2, 1** *[[BC §12.3 tip](https://github.com/Mr-Alireza82/Base-Conversion#123-binary--hexadecimal)]*. To XOR two digits:

> **Write each digit as weights. Cross out the weights that appear in both. Add the rest.**

| Digits | First as weights | Second as weights | In both (cancel) | Left over | Result |
|---|---|---|---|---|:---:|
| `D ⊕ 6` | 8 + 4 + 1 | 4 + 2 | 4 | 8 + 2 + 1 = 11 | **B** |
| `6 ⊕ D` | 4 + 2 | 8 + 4 + 1 | 4 | 8 + 2 + 1 = 11 | **B** |
| `C ⊕ A` | 8 + 4 | 8 + 2 | 8 | 4 + 2 = 6 | **6** |
| `3 ⊕ 5` | 2 + 1 | 4 + 1 | 1 | 4 + 2 = 6 | **6** |
| `F ⊕ 9` | 8 + 4 + 2 + 1 | 8 + 1 | 8, 1 | 4 + 2 = 6 | **6** |
| `7 ⊕ 7` | 4 + 2 + 1 | 4 + 2 + 1 | all | 0 | **0** |

So `D6 ⊕ 6D` = `B` `B` = **`BB`**, and `C3 ⊕ A5` = `6` `6` = **`66`** — same answers as the simple way, with no bits written.

### Shortcuts worth knowing

| Rule | Example |
|---|---|
| $x \oplus 0 = x$ | `D ⊕ 0 = D` |
| $x \oplus x = 0$ | `A ⊕ A = 0` |
| $x \oplus F$ = flip all 4 bits = $15 - x$ | `D ⊕ F = 2`, `6 ⊕ F = 9` |
| Digits 0–7 XOR 8 just add or remove the 8 | `3 ⊕ 8 = B`, `B ⊕ 8 = 3` |

The full lookup table is in the [Cheat Sheet](#18-cheat-sheet).

---

# Part D — XOR in Payment Systems

## 11. Hiding and Revealing — Why XOR Is in Every Cipher

From [§6](#6-the-five-properties): $(A \oplus K) \oplus K = A$. Use a secret $K$ as the "key":

| Step | Operation | Value | Meaning |
|---|---|:---:|---|
| 1 | Message $M$ | `50` | the letter `P` |
| 2 | Key $K$ | `3C` | known only to sender and receiver |
| 3 | $C = M \oplus K$ | `6C` | what travels — looks like the letter `l` |
| 4 | $C \oplus K$ | `50` | the receiver gets `P` back |

Without $K$, the value `6C` could have come from *any* message: for every possible $M$ there is a $K$ that produces `6C`. If $K$ is truly random, as long as the message, and **used only once**, this is the *one-time pad*, which Claude Shannon proved perfectly secret in 1949.

> [!WARNING]
> **XOR alone is not encryption you can use.** Reuse the key once and it breaks: $C_1 \oplus C_2 = (M_1 \oplus K) \oplus (M_2 \oplus K) = M_1 \oplus M_2$ — the key cancels out and the attacker sees how the two messages differ. Real ciphers (DES, AES) use XOR **inside** a much larger design. Never invent your own XOR "encryption".

---

## 12. Key Components

A payment key (for example a terminal master key) must never be known by one person. **Split knowledge** is required: the key is delivered as two or three **components**, each held by a different custodian. The key is the XOR of all components:

$$K = C_1 \oplus C_2 \;(\oplus\; C_3)$$

| | Value | Who knows it |
|---|---|---|
| Component 1 | `1C2D3E4F5A6B7C8D` | Custodian A only |
| Component 2 | `1D0E7B28D3C0B162` | Custodian B only |
| **Key = C1 ⊕ C2** | **`0123456789ABCDEF`** | Nobody — it exists only inside the HSM / PIN pad |

Byte by byte:

| Byte | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 |
|---|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
| C1 | `1C` | `2D` | `3E` | `4F` | `5A` | `6B` | `7C` | `8D` |
| C2 | `1D` | `0E` | `7B` | `28` | `D3` | `C0` | `B1` | `62` |
| **K** | **`01`** | **`23`** | **`45`** | **`67`** | **`89`** | **`AB`** | **`CD`** | **`EF`** |

Why it is safe: from [§11](#11-hiding-and-revealing--why-xor-is-in-every-cipher), one component alone is like a ciphertext without its key — it says **nothing** about $K$. Only all components together reveal it.

(`0123456789ABCDEF` is the demonstration key K1 of the [ANSI X9.19 Retail MAC article](https://github.com/Mr-Alireza82/ANSI-X9.19-Retail-MAC-Algorithm).)

> [!IMPORTANT]
> Demonstration values only. Real components are generated randomly, printed in sealed PIN mailers or smart cards, and entered directly into an HSM or a secure key-loading device — never into ordinary software.

---

## 13. PIN Block — ISO 9564 Format 0

A PIN is never sent alone. It is first mixed with the card number into an 8-byte **PIN block**, which is then encrypted. In ISO 9564-1 **Format 0** (also called ISO-0 or ANSI X9.8), the mixing is a XOR.

Example with PIN `1234` and the well-known test card number `4111 1111 1111 1111`:

**PIN field** — `0`, PIN length, PIN digits, then `F` up to 16 hex digits:

| Part | `0` | Length | PIN | Fill |
|---|:---:|:---:|:---:|:---:|
| Value | `0` | `4` | `1234` | `FFFFFFFFFF` |

**PAN field** — `0000`, then the 12 rightmost card digits **without** the last (check) digit:

| Card number | `4111111111111111` |
|---|---|
| Drop the check digit | `411111111111111` |
| Rightmost 12 digits | `111111111111` |
| PAN field | `0000111111111111` |

**XOR the two fields:**

| Byte | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 |
|---|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
| PIN field | `04` | `12` | `34` | `FF` | `FF` | `FF` | `FF` | `FF` |
| PAN field | `00` | `00` | `11` | `11` | `11` | `11` | `11` | `11` |
| **PIN block** | **`04`** | **`12`** | **`25`** | **`EE`** | **`EE`** | **`EE`** | **`EE`** | **`EE`** |

Clear PIN block: **`041225EEEEEEEEEE`**. This is then encrypted (with DES/TDES or AES) before it leaves the PIN pad.

Why XOR with the card number? The same PIN on two different cards gives two different PIN blocks, so an attacker cannot build a dictionary of "this encrypted block = PIN 1234". And the host, which knows the card number, simply XORs it again ([§6](#6-the-five-properties)) to get the PIN field back.

> [!CAUTION]
> In a real terminal the PIN block is built **inside the secure PIN pad** and is encrypted before any application code can see it. The application must never see, store or log a clear PIN or a clear PIN block (PCI PIN Security). The example uses a public test card number.

---

## 14. Retail MAC and CBC Chaining

The [ANSI X9.19 Retail MAC](https://github.com/Mr-Alireza82/ANSI-X9.19-Retail-MAC-Algorithm) — and the CBC mode of every block cipher — chains blocks together with XOR:

$$H_i = E_{K_1}(M_i \oplus H_{i-1})$$

Each message block is XORed with the previous result before it is encrypted, so changing one early block changes every block after it. Step 4 of the MAC article, done with this article's method:

| Byte | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 |
|---|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
| $M_2$ | `32` | `33` | `00` | `00` | `00` | `00` | `00` | `00` |
| $H_1$ | `35` | `52` | `27` | `E1` | `1D` | `96` | `7D` | `54` |
| **$M_2 \oplus H_1$** | **`07`** | **`61`** | **`27`** | **`E1`** | **`1D`** | **`96`** | **`7D`** | **`54`** |

Notice bytes 3–8: $M_2$ is `00` there, so by P1 ($A \oplus 0 = A$) they are copied from $H_1$ unchanged. Only bytes 1–2 need calculating:

| Byte 1 | Weights | | Byte 2 | Weights |
|---|---|---|---|---|
| `3 ⊕ 3` | 2+1 vs 2+1 → all cancel → **0** | | `3 ⊕ 5` | 2+1 vs 4+1 → **6** |
| `2 ⊕ 5` | 2 vs 4+1 → **7** | | `3 ⊕ 2` | 2+1 vs 2 → **1** |

Byte 1 = `07`, byte 2 = `61` ✓.

---

## 15. LRC — The Frame Checksum

Terminals, card readers and cash registers often talk over serial or USB links in **frames**:

```text
STX  data …  ETX  LRC
```

The **LRC** (Longitudinal Redundancy Check) is one byte: the XOR of all bytes after `STX` up to and including `ETX`. The receiver XORs the same bytes; if its result differs from the received LRC, a bit was damaged on the way and the frame is rejected.

Example: data `1234` (ASCII `31 32 33 34`), `ETX` = `03`:

| Step | Byte | Running XOR |
|:---:|:---:|:---:|
| 1 | `31` | `31` |
| 2 | `32` | `31 ⊕ 32 = 03` |
| 3 | `33` | `03 ⊕ 33 = 30` |
| 4 | `34` | `30 ⊕ 34 = 04` |
| 5 | `03` (ETX) | `04 ⊕ 03 = 07` |

**LRC = `07`**. The frame on the wire: `02 31 32 33 34 03 07`.

This is the "odd or even" idea from [§3](#odd-or-even), done for 8 bit-columns at once.

> [!NOTE]
> An LRC detects accidental damage, not attacks: anyone can recompute it. Protection against deliberate changes needs a key — a MAC.

---

# Part E — In Code

## 16. XOR in Java

Java's XOR operator is `^`:

```java
int a = 0xD6;
int b = 0x6D;
int c = a ^ b;            // 0xBB
```

Two Java details matter for bytes:

1. **`byte ^ byte` gives an `int`.** Java widens both bytes to `int` before the operation, so the result must be cast back: `(byte) (x ^ y)`.
2. **Signed bytes don't matter for XOR.** `(byte) 0xD6` is shown as `-42`, but XOR only sees bits *[[BC §16](https://github.com/Mr-Alireza82/Base-Conversion#16-which-type-should-a-conversion-return)]*. When widened, both bytes get the same extra leading 1s or 0s, and the cast back to `byte` drops them again. The 8 bits you get are always right.

```java
byte x = (byte) 0xD6;
byte y = (byte) 0x6D;
byte z = (byte) (x ^ y);  // 0xBB (shown as -69 by Java)
```

Don't confuse `^` with power: in Java `2 ^ 3` is `1` (XOR), not 8. Use `Math.pow` or shifts for powers.

---

## 17. Using the Library

Package `io.github.mralireza82.xor`, pure Java 17. It depends only on [Base-Conversion](https://github.com/Mr-Alireza82/Base-Conversion) (for hex ↔ bytes ↔ bits).

| Class | Use it for |
|---|---|
| `Xor` | The quick way (`^` on bytes). Use this in real code. |
| `SimpleXor` | The paper way (bits and the truth table). For learning; same results. |

```java
// Quick
Xor.xorHex("D6", "6D");                                          // "BB"
Xor.xor(ByteEncoding.fromHex("3233000000000000"),
        ByteEncoding.fromHex("355227E11D967D54"));               // bytes 07 61 27 E1 1D 96 7D 54
Xor.combine(component1, component2);                              // key from its components
Xor.lrc(ByteEncoding.fromHex("3132333403"));                      // (byte) 0x07

// Simple — same results, every step visible
SimpleXor.xorBit('1', '0');                                       // '1'
SimpleXor.xorBits("1101", "0110");                                // "1011"
SimpleXor.xorHex("C3", "A5");                                     // "66"
```

**Rules:**

- Both inputs must have the same length; otherwise `IllegalArgumentException`. Nothing is padded or cut.
- Inputs are never changed; every method returns a new value.
- Hex input may be upper or lower case; output is upper case.

**How it is tested:** every example in this article is a test case; the five properties of [§6](#6-the-five-properties) are checked on 1,000 random inputs of up to 32 bytes; and the simple way is compared with the quick way on every one of them.

**Run it yourself:** the code is in [`src/main/java`](src/main/java/io/github/mralireza82/xor) and the tests in [`src/test/java`](src/test/java/io/github/mralireza82/xor). With Java 17 or newer installed:

```bash
./gradlew test        # Windows: gradlew.bat test
```

**Use it in your project** (Gradle, through [JitPack](https://jitpack.io/#Mr-Alireza82/XOR)):

```kotlin
repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.Mr-Alireza82:XOR:v1.0.1")
}
```

---

# Part F — Reference

## 18. Cheat Sheet

### Truth table

| A | B | A ⊕ B |
|:---:|:---:|:---:|
| 0 | 0 | 0 |
| 0 | 1 | 1 |
| 1 | 0 | 1 |
| 1 | 1 | 0 |

### Properties

$$A \oplus 0 = A \qquad A \oplus A = 0 \qquad A \oplus B = B \oplus A \qquad (A \oplus B) \oplus C = A \oplus (B \oplus C) \qquad (A \oplus K) \oplus K = A$$

### Hex digit XOR table

Find the first digit in the left column and the second in the top row.

| ⊕ | **0** | **1** | **2** | **3** | **4** | **5** | **6** | **7** | **8** | **9** | **A** | **B** | **C** | **D** | **E** | **F** |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **0** | 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | A | B | C | D | E | F |
| **1** | 1 | 0 | 3 | 2 | 5 | 4 | 7 | 6 | 9 | 8 | B | A | D | C | F | E |
| **2** | 2 | 3 | 0 | 1 | 6 | 7 | 4 | 5 | A | B | 8 | 9 | E | F | C | D |
| **3** | 3 | 2 | 1 | 0 | 7 | 6 | 5 | 4 | B | A | 9 | 8 | F | E | D | C |
| **4** | 4 | 5 | 6 | 7 | 0 | 1 | 2 | 3 | C | D | E | F | 8 | 9 | A | B |
| **5** | 5 | 4 | 7 | 6 | 1 | 0 | 3 | 2 | D | C | F | E | 9 | 8 | B | A |
| **6** | 6 | 7 | 4 | 5 | 2 | 3 | 0 | 1 | E | F | C | D | A | B | 8 | 9 |
| **7** | 7 | 6 | 5 | 4 | 3 | 2 | 1 | 0 | F | E | D | C | B | A | 9 | 8 |
| **8** | 8 | 9 | A | B | C | D | E | F | 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 |
| **9** | 9 | 8 | B | A | D | C | F | E | 1 | 0 | 3 | 2 | 5 | 4 | 7 | 6 |
| **A** | A | B | 8 | 9 | E | F | C | D | 2 | 3 | 0 | 1 | 6 | 7 | 4 | 5 |
| **B** | B | A | 9 | 8 | F | E | D | C | 3 | 2 | 1 | 0 | 7 | 6 | 5 | 4 |
| **C** | C | D | E | F | 8 | 9 | A | B | 4 | 5 | 6 | 7 | 0 | 1 | 2 | 3 |
| **D** | D | C | F | E | 9 | 8 | B | A | 5 | 4 | 7 | 6 | 1 | 0 | 3 | 2 |
| **E** | E | F | C | D | A | B | 8 | 9 | 6 | 7 | 4 | 5 | 2 | 3 | 0 | 1 |
| **F** | F | E | D | C | B | A | 9 | 8 | 7 | 6 | 5 | 4 | 3 | 2 | 1 | 0 |

Notice: the diagonal is all `0` ($x \oplus x = 0$), the first row and column copy the other digit ($x \oplus 0 = x$), and the table is mirrored across the diagonal ($a \oplus b = b \oplus a$).

### Rules in one place

- XOR asks **"different?"** — different → 1, same → 0.
- XOR with 1 **flips**, XOR with 0 **keeps**.
- Doing the same XOR twice **undoes** it.
- XOR works per bit, **no carries**, output length = input length.
- Inputs must have the **same length**.
- Quick by hand: per hex digit, **cancel shared 8-4-2-1 weights**, add the rest.

---

## 19. Practice

1. `1010 ⊕ 0110` (bits).
2. `A5 ⊕ 5A`.
3. `3C ⊕ 3C`.
4. A receiver gets $C$ = `6C` and knows $K$ = `3C`. What was the message, and which letter is it?
5. A key is `0123456789ABCDEF` and component 1 is `FFFFFFFFFFFFFFFF`. What is component 2?
6. LRC of the bytes `41 42 43`.
7. Why can't you XOR an 8-byte block with a 7-byte block, and what has probably gone wrong if you have one?

<details>
<summary>Answers</summary>

1. `1100` — columns differ in the first two positions only.
2. `FF` — `A ⊕ 5`: 8+2 vs 4+1, nothing shared → 15 = `F`; same for `5 ⊕ A`.
3. `00` — anything XOR itself is zero.
4. $M = C \oplus K$ = `6C ⊕ 3C` = `50`, the letter `P`.
5. $C_2 = K \oplus C_1$ = `0123456789ABCDEF ⊕ FFFFFFFFFFFFFFFF` = `FEDCBA9876543210` (XOR with all `F` flips every bit).
6. `41 ⊕ 42 = 03`, `03 ⊕ 43 = 40` → **`40`**.
7. XOR combines position with position; a missing byte has no partner. Usually a leading `00` byte was lost by treating data as a number *[[BC §7](https://github.com/Mr-Alireza82/Base-Conversion#7-number-or-data--the-most-important-distinction)]*, or a padding step was skipped.

</details>

---

## 20. Conclusion

- XOR answers **"are these bits different?"** — 1 if different, 0 if the same.
- Read the other way, XOR with 1 **flips** a bit and XOR with 0 **keeps** it.
- Its key property: $(A \oplus K) \oplus K = A$ — the same operation **hides** and **reveals**.
- It works per bit, with **no carries**, on **data of equal length**.
- In payment systems it **combines key components**, **builds PIN blocks**, **chains blocks** in CBC and Retail MAC, and **checks frames** with the LRC.

XOR mixes data with a key, but alone it is easy to break. The next step is a real block cipher built around it: **DES**.

---

## References and Further Reading

- [Base Conversion — Bits, Bytes and the Four Number Bases](https://github.com/Mr-Alireza82/Base-Conversion) *(prerequisite)*
- ISO 9564-1 — Financial services — Personal Identification Number (PIN) management and security, Part 1 (PIN block formats)
- ANSI X9.24 — Retail Financial Services Symmetric Key Management (key components, split knowledge)
- [NIST SP 800-38A — Recommendation for Block Cipher Modes of Operation](https://csrc.nist.gov/pubs/sp/800/38/a/final) (CBC)
- [Java Language Specification — §15.22.1 Integer Bitwise Operators](https://docs.oracle.com/javase/specs/jls/se17/html/jls-15.html#jls-15.22.1)
- [GitHub — Basic Writing and Formatting Syntax](https://docs.github.com/en/get-started/writing-on-github/getting-started-with-writing-and-formatting-on-github/basic-writing-and-formatting-syntax)

**Related articles:**

- [Base Conversion — Bits, Bytes and the Four Number Bases](https://github.com/Mr-Alireza82/Base-Conversion)
- [ANSI X9.19 — Retail MAC Algorithm](https://github.com/Mr-Alireza82/ANSI-X9.19-Retail-MAC-Algorithm)
- [DES and Triple DES — Data Encryption Standard](https://github.com/Mr-Alireza82/DES) *(next)*

---

## License

[MIT](LICENSE) — free to use, copy, modify and share, including commercially. Keep the copyright notice.
