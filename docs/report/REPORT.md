# CSEN 702 — Tomasulo Simulator: Team Report

> **Editor notes (delete this block before submission)**
>
> Each member writes their section as `docs/report/Mx-<topic>.md`. Send it to M5 by **Dec 7**. M5 merges everything into this file by **Dec 9**.
>
> Before submitting, check that:
> - [ ] All six module sections are merged under section 3, in one style.
> - [ ] The four explanations the PDF asks for are present: cache addressing (M5), CDB conflicts when two instructions finish in the same cycle (M3), branch misprediction and flush approach (M2), address clashes (M4).
> - [ ] Terms are spelled the same everywhere: "reservation station", "load buffer", "store buffer", "CDB", "regular pipeline".
> - [ ] Every test program in `tests/` is listed in section 4 with its expected result.
> - [ ] The zip `TeamXX` contains this report, the source code, and a text file with names, IDs and tutorial numbers.

## 1. Introduction

*What the simulator does and what the user can enter (PDF "List of Variables").*

## 2. Overall approach

*How the modules fit together: the cycle engine, the stages Issue → Execute → Write Result, and the data passed between modules.*

## 3. Modules

### 3.1 Parser and regular pipeline (M1)

### 3.2 Branch prediction and flush (M2)

### 3.3 Execution, write-back and cycle engine; CDB conflicts (M3)

### 3.4 Reservation stations, load/store buffers and address clashes (M4)

### 3.5 Memory hierarchy (M5)

*Merge from `M5-memory-hierarchy.md`.*

### 3.6 GUI (M6)

## 4. Test cases

| Program | Module | What it checks | Expected result |
|---|---|---|---|
| `tests/m5_same_block.txt` | M5 | Two loads in one block | Miss, then hit |
| `tests/m5_block_boundary.txt` | M5 | One load across a block boundary | 2 misses, H + 2P cycles |
| `tests/m5_lru_eviction.txt` | M5 | LRU eviction in a 2-line cache | 4 misses, 1 hit |

## 5. How to run

*JDK version, JavaFX setup, how to start the program, and how to load a code file.*
