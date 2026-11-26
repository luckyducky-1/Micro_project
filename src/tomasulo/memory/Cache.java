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
            lines[i] = new CacheLine();
        }
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
     * Looks up the block holding the accessed bytes and returns the cost: hit latency, plus the miss
     * penalty on a miss. Stores are handled exactly like loads (write-allocate); the caller then
     * writes the value into Memory (write-through).
     */
    public AccessResult access(int address, int sizeBytes, boolean isWrite, int cycle) {
        if (address < 0 || sizeBytes <= 0) {
            throw new IllegalArgumentException("Invalid access: " + sizeBytes + " byte(s) at address " + address + ".");
        }
        int block = blockNumberOf(address);
        if (blockNumberOf(address + sizeBytes - 1) != block) {
            throw new UnsupportedOperationException("Accesses that cross a block boundary are not supported yet (Phase 5.3).");
        }

        int missCount = 0;
        CacheLine line = findLine(block);
        if (line == null) {
            missCount++;
            line = findEmptyLine();
            if (line == null) {
                throw new UnsupportedOperationException("The cache is full; LRU eviction is not implemented yet (Phase 5.1).");
            }
            line.valid = true;
            line.blockNumber = block;
        }
        line.lastUsedCycle = cycle;

        return new AccessResult(missCount == 0, hitLatency + missCount * missPenalty, missCount);
    }

    private CacheLine findEmptyLine() {
        for (CacheLine line : lines) {
            if (!line.valid) {
                return line;
            }
        }
        return null;
    }

    public List<CacheLine> getLines() {
        return Collections.unmodifiableList(Arrays.asList(lines));
    }
}
