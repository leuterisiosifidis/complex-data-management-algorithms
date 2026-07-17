# Relational Operators

Java implementations of common relational-algebra operations over text files.

This module demonstrates sequential processing of sorted relations, duplicate elimination, merge-based algorithms, and grouped aggregation without external libraries.

## Included Programs

| File | Operation | Purpose |
|---|---|---|
| `MergeJoin.java` | Merge join | Joins two sorted relations on their first column |
| `Union.java` | Union | Combines two sorted relations without duplicate rows |
| `Intersection.java` | Intersection | Returns rows present in both sorted relations |
| `SetDifference.java` | Difference | Returns rows present in `R` but not in `S` |
| `GroupBySum.java` | Group and sum | Sorts records by key and aggregates their integer values |

## Requirements

- Java Development Kit (JDK)
- `javac` and `java` available from the command line

No external Java libraries are required.

## Compile

From this folder:

```bash
javac *.java
```

## Input Format

### Merge Join

Both input files must be sorted by their first tab-separated column.

```text
key<TAB>value
```

Example:

```text
A	10
A	20
B	30
```

### Union, Intersection and Difference

The input files must be sorted. These implementations compare complete lines and write unique matching results.

### Group By Sum

Each input line must contain a key and an integer value separated by a tab:

```text
key<TAB>integer
```

Example:

```text
A	10
B	5
A	7
```

## Run

### Merge Join

```bash
java MergeJoin R_sorted.tsv S_sorted.tsv RjoinS.tsv
```

When equal keys are found, matching rows from `S` are buffered and combined with matching rows from `R`. The program also reports the maximum buffer size used.

### Union

```bash
java Union R_sorted.tsv S_sorted.tsv RunionS.tsv
```

Merges the two sorted inputs and avoids writing duplicate rows.

### Intersection

```bash
java Intersection R_sorted.tsv S_sorted.tsv RintersectionS.tsv
```

Writes rows that appear in both inputs.

### Set Difference

```bash
java SetDifference R_sorted.tsv S_sorted.tsv RdifferenceS.tsv
```

Writes rows that appear in `R` but not in `S`.

### Group By Sum

```bash
java GroupBySum R.tsv Rgroupby.tsv
```

Sorts records with merge sort and combines records with equal keys by summing their values.

## Example Workflow

```bash
javac *.java
java MergeJoin R_sorted.tsv S_sorted.tsv RjoinS.tsv
```

The output is written to the filename supplied as the final command-line argument.

## Concepts Demonstrated

- Relational-algebra operations
- Sequential file processing
- Merge-based comparison
- Duplicate elimination
- Temporary buffering for duplicate join keys
- Merge sort
- Grouped integer aggregation

## Notes

- The merge-based programs expect sorted input.
- Output files are overwritten when the program runs.
- Input validation is limited, so use correctly formatted files.
