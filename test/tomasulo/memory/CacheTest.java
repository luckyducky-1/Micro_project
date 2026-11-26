package tomasulo.memory;

/** Plain-Java tests for Cache. Run main; it throws on the first failure. */
public class CacheTest {
    public static void main(String[] args) {
        lineCountIsCacheSizeOverBlockSize();
        startsEmpty();
        blockAndOffset();
        findLineOnEmptyCacheReturnsNull();
        findLineSearchesEveryLine();
        rejectsInvalidConfig();
        System.out.println("All Cache tests passed.");
    }

    static void lineCountIsCacheSizeOverBlockSize() {
        check(cache(64, 16).getLines().size() == 4, "64-byte cache with 16-byte blocks should have 4 lines");
        check(cache(32, 16).getLines().size() == 2, "32-byte cache with 16-byte blocks should have 2 lines");
        check(cache(64, 64).getLines().size() == 1, "64-byte cache with 64-byte blocks should have 1 line");
    }

    static void startsEmpty() {
        for (CacheLine line : cache(64, 16).getLines()) {
            check(!line.isValid(), "every line should start invalid");
        }
    }

    static void blockAndOffset() {
        Cache cache = cache(64, 16);
        check(cache.blockNumberOf(24) == 1 && cache.offsetOf(24) == 8, "address 24 should be block 1, offset 8");
        check(cache.blockNumberOf(0) == 0 && cache.offsetOf(0) == 0, "address 0 should be block 0, offset 0");
        check(cache.blockNumberOf(15) == 0 && cache.offsetOf(15) == 15, "address 15 should be block 0, offset 15");
        check(cache.blockNumberOf(16) == 1 && cache.offsetOf(16) == 0, "address 16 should be block 1, offset 0");
    }

    static void findLineOnEmptyCacheReturnsNull() {
        // Empty lines have blockNumber 0, so this also checks that invalid lines are ignored.
        check(cache(64, 16).findLine(0) == null, "an empty cache should not find block 0");
    }

    static void findLineSearchesEveryLine() {
        Cache cache = cache(64, 16);
        CacheLine last = cache.getLines().get(3);
        last.valid = true;
        last.blockNumber = 7;
        check(cache.findLine(7) == last, "block 7 should be found in the last line");
        check(cache.findLine(6) == null, "block 6 is not in the cache");
    }

    static void rejectsInvalidConfig() {
        SimConfig config = new SimConfig();
        config.setBlockSize(12);
        try {
            new Cache(config);
        } catch (IllegalArgumentException expected) {
            return;
        }
        throw new AssertionError("a Cache with block size 12 should not be created");
    }

    private static Cache cache(int cacheSize, int blockSize) {
        SimConfig config = new SimConfig();
        config.setCacheSize(cacheSize);
        config.setBlockSize(blockSize);
        return new Cache(config);
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
