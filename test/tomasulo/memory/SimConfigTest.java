package tomasulo.memory;

import java.util.List;

/** Plain-Java tests (no JUnit yet, since the project has no build tool). Run main; it throws on the first failure. */
public class SimConfigTest {
    public static void main(String[] args) {
        defaultsAreValid();
        rejectsBadBlockSize();
        rejectsBlockLargerThanCache();
        rejectsCacheNotMultipleOfBlock();
        rejectsNonPositiveLatency();
        rejectsEmptyStationGroup();
        rejectsNonZeroR0();
        registersRoundTrip();
        System.out.println("All SimConfig tests passed.");
    }

    static void defaultsAreValid() {
        List<String> errors = new SimConfig().validate();
        check(errors.isEmpty(), "defaults should be valid, got " + errors);
    }

    static void rejectsBadBlockSize() {
        SimConfig config = new SimConfig();
        config.setCacheSize(64);
        config.setBlockSize(12);
        check(!config.validate().isEmpty(), "block size 12 with cache size 64 should be rejected");
    }

    static void rejectsBlockLargerThanCache() {
        SimConfig config = new SimConfig();
        config.setCacheSize(64);
        config.setBlockSize(128);
        check(!config.validate().isEmpty(), "block larger than cache should be rejected");
    }

    static void rejectsCacheNotMultipleOfBlock() {
        SimConfig config = new SimConfig();
        config.setCacheSize(40);
        config.setBlockSize(16);
        check(!config.validate().isEmpty(), "cache size 40 with block size 16 should be rejected");
    }

    static void rejectsNonPositiveLatency() {
        SimConfig config = new SimConfig();
        config.setFpDivLatency(0);
        check(config.validate().size() == 1, "zero divide latency should give exactly one error");
    }

    static void rejectsEmptyStationGroup() {
        SimConfig config = new SimConfig();
        config.setLoadBuffers(0);
        check(!config.validate().isEmpty(), "zero load buffers should be rejected");
    }

    static void rejectsNonZeroR0() {
        SimConfig config = new SimConfig();
        config.setIntRegister(0, 5);
        check(!config.validate().isEmpty(), "R0 != 0 should be rejected");
    }

    static void registersRoundTrip() {
        SimConfig config = new SimConfig();
        config.setIntRegister(2, 32);
        config.setFpRegister(4, 1.33);
        check(config.getIntRegister(2) == 32, "R2 should be 32");
        check(config.getFpRegister(4) == 1.33, "F4 should be 1.33");
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
