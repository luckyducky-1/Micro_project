package tomasulo.memory;

/** One slot of the cache: either empty, or holding the block with the given number. */
public class CacheLine {
    boolean valid;
    int blockNumber;
    int lastUsedCycle;

    CacheLine() {
    }

    public boolean isValid() { return valid; }
    public int getBlockNumber() { return blockNumber; }
    public int getLastUsedCycle() { return lastUsedCycle; }
}
