# M5 report notes

Raw notes for the M5 report section. Expanded into the final report in Phase 8.

## Memory model

- Memory is byte-addressable: a `byte[]` where each address holds one byte. Default size 1024 bytes (`SimConfig.memorySize`), and memory starts at all zeros.
- An access of n bytes at address A uses bytes A to A + n − 1. An access that falls outside memory stops the run with an error naming the address.

| Instructions | Bytes | Value type |
|---|---|---|
| LW / SW | 4 | 32-bit integer |
| LD / SD | 8 | 64-bit integer |
| L.S / S.S | 4 | single-precision float |
| L.D / S.D | 8 | double-precision float |

## Byte order: little-endian

The lowest byte of a value is stored at the lowest address. For example, writing the word `0x01020304` at address 0 puts `04` at address 0 and `01` at address 3.

Why: most real machines (x86, ARM, RISC-V) are little-endian, and Java's `ByteBuffer` supports it with one setting. The choice only matters if the same bytes are read with two different sizes. Each instruction reads and writes its own size, so programs give the same results either way.
