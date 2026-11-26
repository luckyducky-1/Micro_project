package tomasulo.memory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/** Byte-addressable data memory: one array element per address. Multi-byte values are little-endian. */
public class Memory {
    private final byte[] bytes;
    private final ByteBuffer view;

    public Memory(int sizeBytes) {
        if (sizeBytes <= 0) {
            throw new IllegalArgumentException("Memory size must be greater than 0 (got " + sizeBytes + ").");
        }
        bytes = new byte[sizeBytes];
        view = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
    }

    public int size() {
        return bytes.length;
    }

    // LW / SW
    public int readInt(int address) {
        check(address, Integer.BYTES);
        return view.getInt(address);
    }

    public void writeInt(int address, int value) {
        check(address, Integer.BYTES);
        view.putInt(address, value);
    }

    // LD / SD
    public long readLong(int address) {
        check(address, Long.BYTES);
        return view.getLong(address);
    }

    public void writeLong(int address, long value) {
        check(address, Long.BYTES);
        view.putLong(address, value);
    }

    // L.S / S.S
    public float readFloat(int address) {
        check(address, Float.BYTES);
        return view.getFloat(address);
    }

    public void writeFloat(int address, float value) {
        check(address, Float.BYTES);
        view.putFloat(address, value);
    }

    // L.D / S.D
    public double readDouble(int address) {
        check(address, Double.BYTES);
        return view.getDouble(address);
    }

    public void writeDouble(int address, double value) {
        check(address, Double.BYTES);
        view.putDouble(address, value);
    }

    public byte getByte(int address) {
        check(address, 1);
        return bytes[address];
    }

    /** Writes the values as consecutive 8-byte doubles starting at startAddress (test and demo setup). */
    public void preloadDoubles(int startAddress, double... values) {
        check(startAddress, values.length * Double.BYTES);
        for (int i = 0; i < values.length; i++) {
            view.putDouble(startAddress + i * Double.BYTES, values[i]);
        }
    }

    private void check(int address, int byteCount) {
        // Written as "address > length - count" so a huge address cannot overflow the sum.
        if (address < 0 || address > bytes.length - byteCount) {
            throw new IllegalArgumentException("Access of " + byteCount + " byte(s) at address " + address
                    + " is outside memory (valid addresses 0.." + (bytes.length - 1) + ").");
        }
    }
}
