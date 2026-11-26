package tomasulo.memory;

/** One slot of the cache: either empty, or holding the block with the given number. */
public class CacheLine {
    private final int blockSize;
    boolean valid;
    int blockNumber;
    int lastUsedCycle;

    CacheLine(int blockSize) {
        this.blockSize = blockSize;
    }

    public boolean isValid() { return valid; }
    public int getBlockNumber() { return blockNumber; }
    public int getLastUsedCycle() { return lastUsedCycle; }

    // The address range is only meaningful when the line is valid.
    public int getFirstAddress() { return blockNumber * blockSize; }
    public int getLastAddress() { return blockNumber * blockSize + blockSize - 1; }

    CacheLine copy() {
        CacheLine copy = new CacheLine(blockSize);
        copy.valid = valid;
        copy.blockNumber = blockNumber;
        copy.lastUsedCycle = lastUsedCycle;
        return copy;
    }
}
