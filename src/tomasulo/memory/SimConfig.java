package tomasulo.memory;

import java.util.ArrayList;
import java.util.List;

/** Every value the user can enter before a simulation starts. Defaults follow the lecture examples. */
public class SimConfig {
    public static final int REGISTER_COUNT = 32;

    // Execution latencies, in cycles
    private int fpAddLatency = 2;    // ADD.D, ADD.S, SUB.D, SUB.S
    private int fpMulLatency = 10;
    private int fpDivLatency = 40;
    private int intAluLatency = 1;   // ADDI, SUBI
    private int branchLatency = 1;
    private int loadLatency = 2;
    private int storeLatency = 2;

    // Number of entries in each station group and buffer
    private int fpAddStations = 3;
    private int fpMulDivStations = 2;
    private int integerStations = 2;
    private int loadBuffers = 3;
    private int storeBuffers = 3;

    // Sizes in bytes, latencies in cycles
    private int cacheSize = 64;
    private int blockSize = 16;
    private int cacheHitLatency = 1;
    private int cacheMissPenalty = 10;
    private int memorySize = 1024;

    private final long[] intRegisters = new long[REGISTER_COUNT];
    private final double[] fpRegisters = new double[REGISTER_COUNT];

    /** Returns one message per invalid input; an empty list means the configuration is usable. */
    public List<String> validate() {
        List<String> errors = new ArrayList<>();

        requirePositive(errors, "FP add/sub latency", fpAddLatency);
        requirePositive(errors, "FP multiply latency", fpMulLatency);
        requirePositive(errors, "FP divide latency", fpDivLatency);
        requirePositive(errors, "Integer ALU latency", intAluLatency);
        requirePositive(errors, "Branch latency", branchLatency);
        requirePositive(errors, "Load latency", loadLatency);
        requirePositive(errors, "Store latency", storeLatency);

        requirePositive(errors, "FP add/sub stations", fpAddStations);
        requirePositive(errors, "FP mul/div stations", fpMulDivStations);
        requirePositive(errors, "Integer stations", integerStations);
        requirePositive(errors, "Load buffers", loadBuffers);
        requirePositive(errors, "Store buffers", storeBuffers);

        requirePositive(errors, "Cache size", cacheSize);
        requirePositive(errors, "Block size", blockSize);
        requirePositive(errors, "Cache hit latency", cacheHitLatency);
        requirePositive(errors, "Cache miss penalty", cacheMissPenalty);
        requirePositive(errors, "Memory size", memorySize);

        if (blockSize > 0 && (blockSize & (blockSize - 1)) != 0) {
            errors.add("Block size must be a power of 2 (got " + blockSize + ").");
        }
        if (blockSize > 0 && cacheSize > 0) {
            if (blockSize > cacheSize) {
                errors.add("Block size (" + blockSize + ") cannot be larger than the cache size (" + cacheSize + ").");
            } else if (cacheSize % blockSize != 0) {
                errors.add("Cache size (" + cacheSize + ") must be a multiple of the block size (" + blockSize + ").");
            }
        }
        if (intRegisters[0] != 0) {
            errors.add("R0 must be 0.");
        }

        return errors;
    }

    private static void requirePositive(List<String> errors, String name, int value) {
        if (value <= 0) {
            errors.add(name + " must be greater than 0 (got " + value + ").");
        }
    }

    public int getFpAddLatency() { return fpAddLatency; }
    public void setFpAddLatency(int cycles) { fpAddLatency = cycles; }

    public int getFpMulLatency() { return fpMulLatency; }
    public void setFpMulLatency(int cycles) { fpMulLatency = cycles; }

    public int getFpDivLatency() { return fpDivLatency; }
    public void setFpDivLatency(int cycles) { fpDivLatency = cycles; }

    public int getIntAluLatency() { return intAluLatency; }
    public void setIntAluLatency(int cycles) { intAluLatency = cycles; }

    public int getBranchLatency() { return branchLatency; }
    public void setBranchLatency(int cycles) { branchLatency = cycles; }

    public int getLoadLatency() { return loadLatency; }
    public void setLoadLatency(int cycles) { loadLatency = cycles; }

    public int getStoreLatency() { return storeLatency; }
    public void setStoreLatency(int cycles) { storeLatency = cycles; }

    public int getFpAddStations() { return fpAddStations; }
    public void setFpAddStations(int count) { fpAddStations = count; }

    public int getFpMulDivStations() { return fpMulDivStations; }
    public void setFpMulDivStations(int count) { fpMulDivStations = count; }

    public int getIntegerStations() { return integerStations; }
    public void setIntegerStations(int count) { integerStations = count; }

    public int getLoadBuffers() { return loadBuffers; }
    public void setLoadBuffers(int count) { loadBuffers = count; }

    public int getStoreBuffers() { return storeBuffers; }
    public void setStoreBuffers(int count) { storeBuffers = count; }

    public int getCacheSize() { return cacheSize; }
    public void setCacheSize(int bytes) { cacheSize = bytes; }

    public int getBlockSize() { return blockSize; }
    public void setBlockSize(int bytes) { blockSize = bytes; }

    public int getCacheHitLatency() { return cacheHitLatency; }
    public void setCacheHitLatency(int cycles) { cacheHitLatency = cycles; }

    public int getCacheMissPenalty() { return cacheMissPenalty; }
    public void setCacheMissPenalty(int cycles) { cacheMissPenalty = cycles; }

    public int getMemorySize() { return memorySize; }
    public void setMemorySize(int bytes) { memorySize = bytes; }

    public long getIntRegister(int index) { return intRegisters[index]; }
    public void setIntRegister(int index, long value) { intRegisters[index] = value; }

    public double getFpRegister(int index) { return fpRegisters[index]; }
    public void setFpRegister(int index, double value) { fpRegisters[index] = value; }
}
