package tomasulo.memory;

/** Plain-Java tests for Memory. Run main; it throws on the first failure. */
public class MemoryTest {
    private static final int SIZE = 1024;

    public static void main(String[] args) {
        startsZeroed();
        intRoundTrip();
        longRoundTrip();
        floatRoundTrip();
        doubleRoundTrip();
        storesLittleEndian();
        neighbouringValuesDoNotOverlap();
        lastValidAddressWorks();
        outOfBoundsThrows();
        rejectsNonPositiveSize();
        preloadDoublesFillsConsecutiveSlots();
        System.out.println("All Memory tests passed.");
    }

    static void startsZeroed() {
        Memory memory = new Memory(SIZE);
        check(memory.size() == SIZE, "size should be " + SIZE);
        check(memory.readLong(0) == 0 && memory.readLong(SIZE - 8) == 0, "memory should start at 0");
    }

    static void intRoundTrip() {
        Memory memory = new Memory(SIZE);
        memory.writeInt(8, 42);
        check(memory.readInt(8) == 42, "readInt(8) should be 42");
        memory.writeInt(8, -7);
        check(memory.readInt(8) == -7, "readInt(8) should be -7");
    }

    static void longRoundTrip() {
        Memory memory = new Memory(SIZE);
        memory.writeLong(16, 1L << 40);
        check(memory.readLong(16) == 1L << 40, "readLong(16) should be 2^40");
    }

    static void floatRoundTrip() {
        Memory memory = new Memory(SIZE);
        memory.writeFloat(4, 1.5f);
        check(memory.readFloat(4) == 1.5f, "readFloat(4) should be 1.5");
    }

    static void doubleRoundTrip() {
        Memory memory = new Memory(SIZE);
        memory.writeDouble(16, 3.5);
        check(memory.readDouble(16) == 3.5, "readDouble(16) should be 3.5");
    }

    static void storesLittleEndian() {
        Memory memory = new Memory(SIZE);
        memory.writeInt(0, 0x01020304);
        check(memory.getByte(0) == 0x04 && memory.getByte(3) == 0x01, "lowest byte should be stored first");
    }

    static void neighbouringValuesDoNotOverlap() {
        Memory memory = new Memory(SIZE);
        memory.writeDouble(0, 1.0);
        memory.writeDouble(8, 2.0);
        check(memory.readDouble(0) == 1.0 && memory.readDouble(8) == 2.0, "adjacent doubles should not overlap");
    }

    static void lastValidAddressWorks() {
        Memory memory = new Memory(SIZE);
        memory.writeDouble(SIZE - 8, 9.25);
        check(memory.readDouble(SIZE - 8) == 9.25, "an 8-byte value ending at the last byte should work");
    }

    static void outOfBoundsThrows() {
        Memory memory = new Memory(SIZE);
        expectThrows(() -> memory.readInt(-1), "readInt(-1)");
        expectThrows(() -> memory.readInt(SIZE - 2), "readInt(size - 2)");
        expectThrows(() -> memory.writeDouble(SIZE, 1.0), "writeDouble(size)");
        expectThrows(() -> memory.readLong(Integer.MAX_VALUE), "readLong(Integer.MAX_VALUE)");
        expectThrows(() -> memory.getByte(SIZE), "getByte(size)");
    }

    static void rejectsNonPositiveSize() {
        expectThrows(() -> new Memory(0), "new Memory(0)");
    }

    static void preloadDoublesFillsConsecutiveSlots() {
        Memory memory = new Memory(SIZE);
        memory.preloadDoubles(0, 1.0, 2.0, 3.0, 4.0);
        check(memory.readDouble(0) == 1.0 && memory.readDouble(24) == 4.0, "preload should fill 0, 8, 16, 24");
        expectThrows(() -> memory.preloadDoubles(SIZE - 8, 1.0, 2.0), "preload past the end");
    }

    private static void expectThrows(Runnable action, String description) {
        try {
            action.run();
        } catch (IllegalArgumentException expected) {
            return;
        }
        throw new AssertionError(description + " should throw IllegalArgumentException");
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
