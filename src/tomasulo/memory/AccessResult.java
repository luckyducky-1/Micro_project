package tomasulo.memory;

/** The outcome of one cache access: whether it hit and how many cycles it costs. */
public class AccessResult {
    private final boolean hit;
    private final int cycles;
    private final int missCount;

    AccessResult(boolean hit, int cycles, int missCount) {
        this.hit = hit;
        this.cycles = cycles;
        this.missCount = missCount;
    }

    public boolean isHit() { return hit; }
    public int getCycles() { return cycles; }
    public int getMissCount() { return missCount; }
}
