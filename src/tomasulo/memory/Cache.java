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

    public Cache(SimConfig config) {
        List<String> errors = config.validate();
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Invalid configuration: " + String.join(" ", errors));
        }
        blockSize = config.getBlockSize();
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

    public List<CacheLine> getLines() {
        return Collections.unmodifiableList(Arrays.asList(lines));
    }
}
