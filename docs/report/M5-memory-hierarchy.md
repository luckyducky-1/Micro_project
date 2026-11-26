# M5 — Memory hierarchy

This module provides the data memory, the data cache, and the configuration that holds every value the user enters. Loads and stores ask the cache how many cycles they take; the memory holds the actual bytes.

## 1. Memory model

Data memory is byte-addressable: a `byte[]` where each address holds one byte. Its size is 1024 bytes by default (a configuration value), and it starts at all zeros. An access of *n* bytes at address *A* uses bytes *A* to *A + n − 1*. An access outside memory stops the simulation with an error that names the address.

| Instructions | Bytes | Value type |
|---|---|---|
| LW / SW | 4 | 32-bit integer |
| LD / SD | 8 | 64-bit integer |
| L.S / S.S | 4 | single-precision float |
| L.D / S.D | 8 | double-precision float |

Multi-byte values are stored **little-endian**: the lowest byte goes at the lowest address, so the word `0x01020304` written at address 0 puts `04` at address 0 and `01` at address 3. Most real machines (x86, ARM, RISC-V) work this way.

## 2. Cache organisation

- **Data only.** Loads and stores go through the cache; instruction fetch does not, so only data misses are counted.
- **Fully associative.** Any block can be placed in any line.
- **Number of lines = cache size ÷ block size.** For example, a 64-byte cache with 16-byte blocks has 4 lines.
- **Starts empty.** Every line is invalid at the start, so the first access to each block is a compulsory miss.
- **Checked input.** Before the simulation starts, the configuration is rejected unless every size and latency is greater than 0, the block size is a power of 2 and no larger than the cache, and the cache size is a multiple of the block size.

## 3. Addressing strategy

```
block number      = address ÷ block size      (integer division)
offset in block   = address mod block size
block b covers    = addresses b × block size  to  b × block size + block size − 1
```

Because the cache is fully associative, an address has no index bits: the block number itself is the tag, and a lookup compares it against every valid line.

**Worked address.** `L.D F0, 24(R0)` with 16-byte blocks: 24 ÷ 16 = block 1, offset 24 mod 16 = 8. The access reads bytes 24–31, which all lie in block 1 (addresses 16–31), so the cache checks one block.

**Access across a block boundary.** `L.D F0, 12(R0)` reads bytes 12–19. Byte 12 is in block 0 and byte 19 is in block 1, so the cache checks both blocks.

## 4. Hit and miss timing

```
cycles for one access = hit latency + (number of missing blocks) × miss penalty
```

| Situation | Cycles |
|---|---|
| Every block touched is present (hit) | H |
| One block missing | H + P |
| Access crosses a boundary and both blocks are missing | H + 2P |

*H* is the cache hit latency and *P* the miss penalty, both entered by the user.

- **When a block counts as present.** A block counts as present from the cycle its miss starts. A second access to that block while the penalty is still being paid is a hit.
- **What the statistics count.** The hit and miss counters count blocks checked, so an access that touches two blocks adds 2 to their total.
- **How a load or store uses the cache.** When the access reaches memory, its buffer calls the cache once and stays busy for the returned number of cycles. It then reads the value from memory (load) or writes it (store).
- **Ordering is not the cache's job.** Keeping loads and stores to the same address in program order is handled by the load/store buffers (M4). The cache serves accesses in the order it receives them.

## 5. Policies

| Decision | Choice | Reason |
|---|---|---|
| Which block is removed when the cache is full | LRU: each line records the cycle it was last used, and the line with the smallest value is evicted | Standard policy from the lectures; needs one number per line |
| Store hit | Write-through: memory is updated on every store | Memory always holds the latest value |
| Store miss | Write-allocate: the block is brought into the cache, like a load | Loads and stores follow one hit/miss rule |
| Access across a block boundary | Check every block touched; one penalty per missing block | Works for any block size the user picks |

Because of write-through, the cache does not need its own copy of the data. Each line stores only whether it is valid, which block it holds, and when it was last used. The GUI shows a block's contents by reading that block's addresses from memory.

## 6. Worked example: the project's loop

The example program sets R1 = 0 and R2 = 32, and each iteration runs `L.D F0, 0(R1)` … `S.D F2, 0(R1)`, then adds 8 to R1. It therefore makes 8 accesses, at R1 = 0, 8, 16 and 24. With a 64-byte cache and 16-byte blocks (4 lines):

| Iteration | Access | Address | Block | Result | Why |
|---|---|---|---|---|---|
| 1 | L.D | 0 | 0 | Miss | Cache is empty (compulsory) |
| 1 | S.D | 0 | 0 | Hit | Block 0 was just brought in |
| 2 | L.D | 8 | 0 | Hit | Bytes 8–15 are also in block 0 |
| 2 | S.D | 8 | 0 | Hit | |
| 3 | L.D | 16 | 1 | Miss | First access to block 1 |
| 3 | S.D | 16 | 1 | Hit | |
| 4 | L.D | 24 | 1 | Hit | Same block as address 16 |
| 4 | S.D | 24 | 1 | Hit | |

**Total: 2 misses, 6 hits.** Under Tomasulo, loads of later iterations can reach the cache before earlier stores. The counts stay the same, because the cache never fills and the first access to each block is a load either way. With 8-byte blocks the same loop gives 4 misses, and with 32-byte blocks it gives 1: larger blocks mean fewer misses when the program walks through consecutive addresses.

## 7. Code structure

All classes are in the package `tomasulo.memory`.

| Class | Responsibility |
|---|---|
| `SimConfig` | Holds every user input (latencies, station and buffer sizes, cache settings, register values) with defaults from the lectures; `validate()` returns one message per invalid value |
| `Memory` | The byte array, with typed reads and writes for 4- and 8-byte integers and floats and a bounds check |
| `Cache` | Lookup, LRU replacement, timing and hit/miss statistics; `access(address, size, isWrite, cycle)` is the only call loads and stores need |
| `CacheLine` | One line: valid flag, block number, last used cycle, and the address range it covers (for the GUI) |
| `AccessResult` | The answer to one access: hit or not, cycles, number of missing blocks |

`Cache.copy()` and `Memory.copy()` make complete independent copies. The simulator saves them every cycle, so stepping backwards shows the exact earlier state.

## 8. Tests

**Unit tests** (`test/tomasulo/memory/`, plain Java). They cover:
- input validation;
- round trips for each value type, byte order and out-of-range errors;
- block and offset mapping;
- the example loop above;
- LRU eviction, write-allocate, and accesses across a block boundary;
- copies that stay independent of the original.

**MIPS test programs** (`tests/`). Each uses R0 = 0 as the base, so every address is known as soon as the load issues. Settings for all three: hit latency H, miss penalty P, 16-byte blocks, and at least 5 load buffers so no load waits for a free buffer.

| Program | Cache size | Address order | Expected result | Cycles per load |
|---|---|---|---|---|
| `m5_same_block.txt` | 64 | 0, 8 | Miss, hit (both in block 0) | H + P, H |
| `m5_block_boundary.txt` | 64 | 12 (bytes 12–19) | 2 misses (blocks 0 and 1) | H + 2P |
| `m5_lru_eviction.txt` | 32 (2 lines) | 0, 16, 0, 32, 16 | Miss, miss, hit, miss (evicts block 1), miss (evicts block 0); 4 misses, 1 hit; blocks 2 and 1 left in the cache | H + P, H + P, H, H + P, H + P |

The same address sequences, run directly on the cache, give exactly these results.
