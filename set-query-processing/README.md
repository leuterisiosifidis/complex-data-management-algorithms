# Set Query Processing

Java implementations for containment and relevance queries over collections of integer-valued transactions.

The module compares straightforward scans with signature-based and inverted-index techniques, while also reporting execution times.

## Included Programs

| File | Purpose |
|---|---|
| `ContainmentQueries.java` | Finds transactions that contain every item in a query |
| `RelevanceQueries.java` | Ranks transactions by their relevance to query items |

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

Both programs read one transaction or query per line.

The parser accepts integer items separated by spaces or commas, with optional square brackets.

All of these forms are valid:

```text
1 2 3 4
1,2,3,4
[1, 2, 3, 4]
```

For relevance queries, repeated items in a transaction are counted. Query items use set semantics.

## 1. Containment Queries

A containment query returns the IDs of transactions that contain every item in the selected query.

### Usage

```bash
java ContainmentQueries transactions.txt queries.txt qnum method
```

### Arguments

| Argument | Meaning |
|---|---|
| `transactions.txt` | Input transactions |
| `queries.txt` | Input queries |
| `qnum` | Zero-based query index, or `-1` for all queries |
| `method` | Evaluation method shown below |

### Methods

| Value | Method | Description |
|---:|---|---|
| `0` | Naive scan | Checks every transaction with set containment |
| `1` | Exact signature file | Encodes transactions and the query as bitmaps |
| `2` | Bit-sliced signature file | Stores one transaction bitmap for each item |
| `3` | Inverted file | Intersects transaction-ID lists for query items |
| `-1` | All methods | Runs every available implementation |

### Examples

Run query `0` with every method:

```bash
java ContainmentQueries transactions.txt queries.txt 0 -1
```

Run every query with the inverted-file method:

```bash
java ContainmentQueries transactions.txt queries.txt -1 3
```

Depending on the selected method, the program creates:

```text
sigfile.txt
bitslice.txt
invfile.txt
```

It also prints the computation time for each method.

## 2. Relevance Queries

A relevance query ranks transactions according to the query items they contain.

For each matching item, the score contribution is:

```text
itemCount × (numberOfTransactions / transactionFrequency)
```

Items that occur in fewer transactions therefore contribute more to the final score.

### Usage

```bash
java RelevanceQueries transactions.txt queries.txt qnum method k
```

### Arguments

| Argument | Meaning |
|---|---|
| `transactions.txt` | Input transactions |
| `queries.txt` | Input queries |
| `qnum` | Zero-based query index, or `-1` for all queries |
| `method` | `0` for naive, `1` for inverted index, `-1` for both |
| `k` | Number of highest-scoring transactions to return |

### Examples

Return the top 10 results for query `0` using both methods:

```bash
java RelevanceQueries transactions.txt queries.txt 0 -1 10
```

Evaluate all queries with the inverted-index method:

```bash
java RelevanceQueries transactions.txt queries.txt -1 1 10
```

The program creates:

```text
invfileocc.txt
```

This file stores the inverted index together with item occurrence information. For a selected query, results are sorted by descending score and limited to the requested top `k`.

## Quick Workflow

```bash
javac *.java
java ContainmentQueries transactions.txt queries.txt 0 -1
java RelevanceQueries transactions.txt queries.txt 0 -1 10
```

## Concepts Demonstrated

- Transaction and query parsing
- Set containment
- Naive sequential evaluation
- Exact bitmap signatures
- Bit-sliced signatures
- Inverted indexes
- Merge-style posting-list intersection
- Item-frequency weighting
- Top-`k` relevance ranking
- Basic execution-time comparison

## Notes

- Query numbering is zero-based.
- Generated index files are overwritten when the programs run.
- The implementations are intended for algorithm comparison and educational use.
