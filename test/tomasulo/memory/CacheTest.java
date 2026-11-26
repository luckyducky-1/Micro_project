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
        firstAccessIsMissThenHit();
        missPlacesBlockInEmptyLine();
        hitUpdatesLastUsedCycle();
        pdfLoopCounts();
        biggerBlocksGiveFewerMisses();
        rejectsInvalidAccess();
        System.out.println("All Cache tests passed.");
    }

    static void firstAccessIsMissThenHit() {
        SimConfig config = new SimConfig();
        config.setCacheHitLatency(2);
        config.setCacheMissPenalty(7);
        Cache cache = new Cache(config);

        AccessResult miss = cache.access(0, 8, false, 1);
        check(!miss.isHit() && miss.getMissCount() == 1, "first access to block 0 should be a compulsory miss");
        check(miss.getCycles() == 2 + 7, "a miss should cost hit latency + miss penalty");

        AccessResult hit = cache.access(8, 8, false, 2);
        check(hit.isHit() && hit.getMissCount() == 0, "address 8 is in block 0, so it should hit");
        check(hit.getCycles() == 2, "a hit should cost the hit latency");

        check(!cache.access(16, 8, false, 3).isHit(), "first access to block 1 should miss");
    }

    static void missPlacesBlockInEmptyLine() {
        Cache cache = cache(64, 16);
        cache.access(40, 8, false, 5);
        CacheLine line = cache.findLine(2);
        check(line != null && line.isValid(), "block 2 should be in the cache after the miss");
        check(line.getLastUsedCycle() == 5, "lastUsedCycle should be the access cycle");
    }

    static void hitUpdatesLastUsedCycle() {
        Cache cache = cache(64, 16);
        cache.access(0, 8, false, 1);
        cache.access(4, 4, true, 9);
        check(cache.findLine(0).getLastUsedCycle() == 9, "a hit should update lastUsedCycle");
    }

    static void pdfLoopCounts() {
        // L.D then S.D at R1 = 0, 8, 16, 24; 16-byte blocks, 64-byte cache
        int[] misses = runPdfLoop(cache(64, 16));
        check(misses[0] == 2 && misses[1] == 6, "PDF loop should give 2 misses and 6 hits, got "
                + misses[0] + " misses and " + misses[1] + " hits");
    }

    static void biggerBlocksGiveFewerMisses() {
        check(runPdfLoop(cache(64, 8))[0] == 4, "8-byte blocks should give 4 misses");
        check(runPdfLoop(cache(64, 32))[0] == 1, "32-byte blocks should give 1 miss");
    }

    static void rejectsInvalidAccess() {
        Cache cache = cache(64, 16);
        try {
            cache.access(-8, 8, false, 1);
        } catch (IllegalArgumentException expected) {
            return;
        }
        throw new AssertionError("a negative address should be rejected");
    }

    /** Returns {misses, hits}. */
    private static int[] runPdfLoop(Cache cache) {
        int misses = 0;
        int hits = 0;
        int cycle = 1;
        for (int address = 0; address < 32; address += 8) {
            for (boolean isWrite : new boolean[] {false, true}) {
                AccessResult result = cache.access(address, 8, isWrite, cycle++);
                misses += result.getMissCount();
                hits += result.isHit() ? 1 : 0;
            }
        }
        return new int[] {misses, hits};
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
