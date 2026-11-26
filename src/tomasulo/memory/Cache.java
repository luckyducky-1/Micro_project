package tomasulo.memory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Fully associative data cache. It tracks which blocks are present and the access timing;
 * the data itself always lives in Memory (write-through).
 */
public class Cache {
    private final CacheLine[] lines;
    private final int blockSize;
    private final int hitLatency;
    private final int missPenalty;
    private int hits;
    private int misses;

    public Cache(SimConfig config) {
        List<String> errors = config.validate();
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Invalid configuration: " + String.join(" ", errors));
        }
        blockSize = config.getBlockSize();
        hitLatency = config.getCacheHitLatency();
        missPenalty = config.getCacheMissPenalty();
        lines = new CacheLine[config.getCacheSize() / blockSize];
        for (int i = 0; i < lines.length; i++) {
            lines[i] = new CacheLine(blockSize);
        }
    }

    private Cache(Cache other) {
        blockSize = other.blockSize;
        hitLatency = other.hitLatency;
        missPenalty = other.missPenalty;
        hits = other.hits;
        misses = other.misses;
        lines = new CacheLine[other.lines.length];
        for (int i = 0; i < lines.length; i++) {
            lines[i] = other.lines[i].copy();
        }
    int blockNumberOf(int address) {
        return address / blockSize;
    }

    int offsetOf(int address) {
        return address % blockSize;
    }

    /** Fully associative: the block may be in any line, so every line is checked. */
    CacheLine findLine(int blockNumber) {
        for (CacheLine line : lines) {
            if (line.valid && line.blockNumber == blockNumber) {
                return line;
            }
        }
        return null;
    }

    /**
     * Looks up every block the accessed bytes touch and returns the cost: hit latency plus one miss
     * penalty per missing block. Stores are handled exactly like loads (write-allocate); the caller
     * then writes the value into Memory (write-through).
     */
    public AccessResult access(int address, int sizeBytes, boolean isWrite, int cycle) {
        if (address < 0 || sizeBytes <= 0) {
            throw new IllegalArgumentException("Invalid access: " + sizeBytes + " byte(s) at address " + address + ".");
        }
        int firstBlock = blockNumberOf(address);
        int lastBlock = blockNumberOf(address + sizeBytes - 1);

        int missCount = 0;
        for (int block = firstBlock; block <= lastBlock; block++) {
            CacheLine line = findLine(block);
            if (line == null) {
                missCount++;
                line = lineToReplace();
                line.valid = true;
                line.blockNumber = block;
            }
            line.lastUsedCycle = cycle;
        }

        // Statistics count block lookups, so an access touching two blocks adds two to hits + misses.
        misses += missCount;
        hits += (lastBlock - firstBlock + 1) - missCount;
        return new AccessResult(missCount == 0, hitLatency + missCount * missPenalty, missCount);
    }

    /** An empty line if there is one; otherwise the least recently used line (LRU). */
    private CacheLine lineToReplace() {
        CacheLine victim = lines[0];
        for (CacheLine line : lines) {
            if (!line.valid) {
                return line;
            }
            if (line.lastUsedCycle < victim.lastUsedCycle) {
                victim = line;
            }
        }
        return victim;
    }

    public int getHits() {
        return hits;
    }

    public int getMisses() {
        return misses;
    }

    public List<CacheLine> getLines() {
        return Collections.unmodifiableList(Arrays.asList(lines));
    }
}
