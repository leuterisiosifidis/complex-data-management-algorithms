# Complex Data Management Algorithms

A collection of Java implementations for processing relational, spatial, and set-valued data.

The repository explores how different data structures and indexing techniques can improve query processing, with emphasis on both correctness and execution efficiency.

## Projects

| Module | Main topic | Implementations |
|---|---|---|
| `relational-operators` | Relational data processing | Merge join, union, intersection, set difference, and grouped aggregation |
| `spatial-indexing` | Spatial data indexing | R-tree construction, range queries, and k-nearest-neighbour search |
| `set-query-processing` | Set-valued data queries | Containment queries, relevance ranking, signature files, and inverted indexes |

## 1. Relational Operators

This module implements relational-algebra operations directly over text files.

### Implemented algorithms

- **Merge Join** — joins two sorted relations using a sequential merge process
- **Union** — combines two relations while avoiding duplicate results
- **Intersection** — returns records present in both relations
- **Set Difference** — returns records found in the first relation but not the second
- **Group By Sum** — groups records by key and calculates the sum of their values

Main files:

```text
relational-operators/
├── MergeJoin.java
├── Union.java
├── Intersection.java
├── SetDifference.java
└── GroupBySum.java
```

## 2. Spatial Indexing

This module implements an **R-tree** for storing and searching spatial objects.

### R-tree construction

`RTreeBuilder.java`:

1. Calculates the minimum bounding rectangle of each spatial object
2. Uses the centre of each rectangle to calculate its Z-order value
3. Sorts the objects by Z-order
4. Creates leaf nodes using bulk loading
5. Builds the remaining tree levels from the bottom up

### Supported queries

- **Range Query** — finds spatial objects whose bounding rectangles intersect a given query rectangle
- **k-Nearest-Neighbour Query** — uses a priority queue and best-first search to find the nearest objects

Main files:

```text
spatial-indexing/
├── RTreeBuilder.java
├── RangeQuery.java
└── KNNQuery.java
```

## 3. Set Query Processing

This module compares different techniques for containment and relevance queries over collections of integer-valued sets.

### Containment queries

A containment query finds every transaction that contains all items of the query.

Implemented methods:

- Naive sequential scan
- Exact signature file
- Bit-sliced signature file
- Inverted index

### Relevance queries

A relevance query ranks transactions according to how strongly they match the query items.

Implemented methods:

- Naive transaction scan
- Inverted-index-based processing
- Top-`k` result selection

Main files:

```text
set-query-processing/
├── ContainmentQueries.java
└── RelevanceQueries.java
```

## Repository Structure

```text
complex-data-management-algorithms/
├── relational-operators/
├── spatial-indexing/
├── set-query-processing/
└── README.md
```

## Requirements

- Java Development Kit
- Java command-line tools available through `javac` and `java`
- Input datasets in the format expected by each implementation

No external Java libraries are required.

## Compilation

Enter one of the project folders and compile its Java files:

```bash
cd relational-operators
javac *.java
```

The same process can be used for the other modules:

```bash
cd spatial-indexing
javac *.java
```

```bash
cd set-query-processing
javac *.java
```

Programs can then be executed using:

```bash
java ClassName [arguments]
```

The required files and command-line arguments differ between implementations.

## Concepts Demonstrated

- Relational-algebra algorithms
- Sequential file processing
- Sorting and grouped aggregation
- Minimum bounding rectangles
- Z-order spatial sorting
- R-tree bulk loading
- Range searching
- Best-first nearest-neighbour search
- Signature files
- Bit-sliced indexes
- Inverted indexes
- Containment filtering
- Top-`k` relevance ranking
- Execution-time comparison

## Academic Context

These implementations were developed as university coursework in complex data management and query-processing algorithms.

## Author

**Lefteris Iosifidis**
