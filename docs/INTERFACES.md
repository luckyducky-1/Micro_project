# Shared interfaces

Public methods each module exposes to the others. Code against these signatures; if you need a change, ask the owner before editing their package.

## M5 — Memory hierarchy (`tomasulo.memory`)

Status: everything below is implemented and tested, except `copy()` on `Memory` and `Cache` and the `CacheLine` address-range getters (Phase 6).

### SimConfig — every user input

```java
SimConfig config = new SimConfig();     // defaults from the lectures, already valid
List<String> errors = config.validate(); // empty = OK; otherwise show the messages to the user
```

| Group | Getter / setter pair (`getX()` / `setX(int)`) | Default |
|---|---|---|
| Latencies (cycles) | `FpAddLatency` (ADD/SUB .D/.S), `FpMulLatency`, `FpDivLatency`, `IntAluLatency` (ADDI/SUBI), `BranchLatency`, `LoadLatency`, `StoreLatency` | 2, 10, 40, 1, 1, 2, 2 |
| Station / buffer sizes | `FpAddStations`, `FpMulDivStations`, `IntegerStations`, `LoadBuffers`, `StoreBuffers` | 3, 2, 2, 3, 3 |
| Cache and memory | `CacheSize`, `BlockSize` (bytes), `CacheHitLatency`, `CacheMissPenalty` (cycles), `MemorySize` (bytes) | 64, 16, 1, 10, 1024 |
| Registers | `getIntRegister(i)` / `setIntRegister(i, long)`, `getFpRegister(i)` / `setFpRegister(i, double)`, `i` in 0..31 | all 0 |

The station sizes are in the same order as M4's `StationGroups` constructor:

```java
new StationGroups(config.getFpAddStations(), config.getFpMulDivStations(),
                  config.getIntegerStations(), config.getLoadBuffers(), config.getStoreBuffers());
```

### Memory — byte-addressable data

```java
Memory(int sizeBytes)
int    readInt(int address)     void writeInt(int address, int value)        // LW / SW, 4 bytes
long   readLong(int address)    void writeLong(int address, long value)      // LD / SD, 8 bytes
float  readFloat(int address)   void writeFloat(int address, float value)    // L.S / S.S, 4 bytes
double readDouble(int address)  void writeDouble(int address, double value)  // L.D / S.D, 8 bytes
byte   getByte(int address)     // for the GUI
int    size()
void   preloadDoubles(int startAddress, double... values)  // consecutive 8-byte doubles, for test setup
Memory copy()                   // deep copy for cycle snapshots (Phase 6)
```

Little-endian. An out-of-range address throws `IllegalArgumentException` with the address in the message.

### Cache — timing only, never data

```java
Cache(SimConfig config)
AccessResult access(int address, int sizeBytes, boolean isWrite, int cycle)
List<CacheLine> getLines()      // read-only, for the GUI cache table
int getHits()
int getMisses()
Cache copy()                    // deep copy for cycle snapshots

AccessResult: boolean isHit(), int getCycles(), int getMissCount()
CacheLine:    boolean isValid(), int getBlockNumber(), int getLastUsedCycle(),
              int getFirstAddress(), int getLastAddress()
```

How a load or store uses it (M4 in the load/store buffers, M1 in the regular pipeline):

1. When the access reaches memory, call `cache.access(address, size, isWrite, currentCycle)` exactly once.
2. The access takes `result.getCycles()` cycles.
3. Then read the value from `Memory` (load) or write it to `Memory` (store).

Policies: fully associative, starts empty, LRU replacement, write-through, write-allocate. Cost = hit latency + one miss penalty per missing block; an access that crosses a block boundary checks every block it touches. A block counts as present from the cycle its miss starts. `getHits()` and `getMisses()` count block lookups, so an access that touches two blocks adds 2 to their sum.

Addresses are `int`. `LoadStoreBuffer.effectiveAddress()` returns `Long`, so cast with `(int)` when calling the cache.

### Open questions for the team

- **Load/store latency vs. cache cost (M4, M3).** Should a load take `LoadLatency + cache cycles`, or only the cache cycles (as in lecture 13, where the cache miss sets the load time)? `SimConfig` holds both numbers either way.
- **Latency for integer ADD/SUB or BEQ** if the parser supports them (M1). Right now they share `IntAluLatency` / `BranchLatency`.
