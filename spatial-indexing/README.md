# Spatial Indexing with an R-tree

Java implementations for building an R-tree and executing spatial range and k-nearest-neighbour queries.

The module uses minimum bounding rectangles (MBRs), Z-order sorting, bulk loading, and best-first search to organize and retrieve spatial objects.

## Included Programs

| File | Purpose |
|---|---|
| `RTreeBuilder.java` | Builds and stores the R-tree |
| `RangeQuery.java` | Finds objects intersecting rectangular query regions |
| `KNNQuery.java` | Finds the `k` nearest object MBRs to query points |

## Requirements

- Java Development Kit (JDK)
- `javac` and `java` available from the command line

No external Java libraries are required.

## Compile

From this folder:

```bash
javac *.java
```

## 1. Build the R-tree

```bash
java RTreeBuilder coords.txt offsets.txt
```

### Coordinate File

`coords.txt` contains one point per line:

```text
x,y
```

Example:

```text
10.0,15.0
12.5,18.0
20.0,25.0
```

### Offset File

`offsets.txt` defines each polygon using inclusive indexes into `coords.txt`:

```text
polygonId,startIndex,endIndex
```

Example:

```text
0,0,2
1,3,6
```

For each polygon, the builder:

1. Reads its points from the coordinate file.
2. Calculates its minimum bounding rectangle.
3. Calculates a Z-order value from the MBR centre.
4. Sorts polygons by Z-order.
5. Bulk-loads leaf nodes.
6. Builds internal levels from the bottom up.

Nodes normally contain between 8 and 20 entries. The resulting tree is written to:

```text
Rtree.txt
```

## 2. Run Range Queries

```bash
java RangeQuery Rtree.txt Rqueries.txt
```

Each line in `Rqueries.txt` represents a rectangular query:

```text
xMin yMin xMax yMax
```

Example:

```text
0 0 50 50
20 10 40 30
```

The search starts at the root and follows entries whose MBRs intersect the query rectangle. Matching object IDs are printed in sorted order.

Output format:

```text
queryIndex (resultCount): id1,id2,id3
```

## 3. Run k-Nearest-Neighbour Queries

```bash
java KNNQuery Rtree.txt NNqueries.txt 10
```

Each line in `NNqueries.txt` contains a query point:

```text
x y
```

Example:

```text
15.0 20.0
42.5 10.0
```

The final argument is the number of neighbours to return.

`KNNQuery` performs best-first search with a priority queue. Internal nodes are expanded according to their minimum distance from the query point, and the search stops after finding `k` object MBRs.

Output format:

```text
queryIndex: id1,id2,...,idK
```

## Quick Workflow

```bash
javac *.java
java RTreeBuilder coords.txt offsets.txt
java RangeQuery Rtree.txt Rqueries.txt
java KNNQuery Rtree.txt NNqueries.txt 10
```

## Concepts Demonstrated

- Minimum bounding rectangles
- Z-order spatial sorting
- R-tree bulk loading
- Bottom-up tree construction
- Spatial range search
- MBR intersection testing
- Priority queues
- Best-first k-nearest-neighbour search

## Notes

- `RTreeBuilder` must be run before the query programs.
- Query programs expect the generated `Rtree.txt` file.
- Input validation is limited, so files must follow the formats above.
