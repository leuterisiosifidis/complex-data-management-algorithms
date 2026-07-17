
import java.io.*;
import java.util.*;

public class RangeQuery {

    // Represents a Minimum Bounding Rectangle (MBR)
    static class MBR {
        double xMin, xMax, yMin, yMax;

        MBR(double xMin, double xMax, double yMin, double yMax) {
            this.xMin = xMin;
            this.xMax = xMax;
            this.yMin = yMin;
            this.yMax = yMax;
        }

        // Checks if this MBR intersects another MBR (used in range queries)
        boolean intersects(MBR other) {
            return !(this.xMax < other.xMin || this.xMin > other.xMax || this.yMax < other.yMin || this.yMin > other.yMax);
        }

        public String toString() {
            return "[" + xMin + ", " + xMax + ", " + yMin + ", " + yMax + "]";
        }
    }

    // Represents an entry in an R-tree node (can be a polygon or a child node)
    static class Entry {
        int id;      // Object ID (leaf) or Node ID (non-leaf)
        MBR mbr;     // Associated MBR

        Entry(int id, MBR mbr) {
            this.id = id;
            this.mbr = mbr;
        }
    }

    // Represents a node in the R-tree
    static class RTreeNode {
        boolean isNonLeaf;        // true if node is non-leaf (internal), false if leaf
        int nodeId;               // unique identifier
        List<Entry> entries;      // entries (child nodes or objects)

        RTreeNode(boolean isNonLeaf, int nodeId) {
            this.isNonLeaf = isNonLeaf;
            this.nodeId = nodeId;
            this.entries = new ArrayList<>();
        }
    }

    // In-memory representation of the R-tree using nodeId → RTreeNode
    static Map<Integer, RTreeNode> rtree = new HashMap<>();

      //Performs a recursive range query starting from a given node.
      //It collects object IDs whose MBRs intersect the query rectangle.
      static void rangeSearch(int nodeId, MBR query, Set<Integer> result) {
        RTreeNode node = rtree.get(nodeId);
        if (node == null) return;

        for (Entry e : node.entries) {
            if (e.mbr.intersects(query)) {
                if (node.isNonLeaf) {
                    // Continue to child node if current is internal
                    rangeSearch(e.id, query, result);
                } else {
                    // Add polygon ID to result set if current is a leaf node
                    result.add(e.id);
                }
            }
        }
    }

    
     //Loads the R-tree from a formatted text file (Rtree.txt)
     //Each line represents a node: [isnonleaf, node-id, [[id1, MBR1], [id2, MBR2], …, [idn, MBRn]]].
    static void loadRTree(String filename) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filename));
        String line;

        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) continue;

            // Remove the outer brackets from the line
            line = line.substring(1, line.length() - 1);
            String[] parts = line.split(",", 3);

            // Parse node information
            boolean isNonLeaf = parts[0].trim().equals("1");
            int nodeId = Integer.parseInt(parts[1].trim());

            RTreeNode node = new RTreeNode(isNonLeaf, nodeId);

            // Extract and parse all entries within the node
            String entriesRaw = parts[2].trim();
            entriesRaw = entriesRaw.substring(1, entriesRaw.length() - 1); // remove outer [[...]]
            String[] entryTokens = entriesRaw.split("],\\s*\\[");

            for (String entry : entryTokens) {
                entry = entry.replace("[", "").replace("]", "").trim();
                String[] eParts = entry.split(",");
                int id = Integer.parseInt(eParts[0].trim());
                double xMin = Double.parseDouble(eParts[1].trim());
                double xMax = Double.parseDouble(eParts[2].trim());
                double yMin = Double.parseDouble(eParts[3].trim());
                double yMax = Double.parseDouble(eParts[4].trim());

                node.entries.add(new Entry(id, new MBR(xMin, xMax, yMin, yMax)));
            }

            rtree.put(nodeId, node);
        }

        br.close();
    }

    
     //Finds the root node of the R-tree (assumed to be the node with the largest ID)   
    static int findRootId() {
        return Collections.max(rtree.keySet());
    }
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.out.println("Usage: java RangeQuery Rtree.txt Rqueries.txt");
            return;
        }

        String rtreeFile = args[0];     // Path to the R-tree structure
        String queryFile = args[1];     // Path to the range queries

        loadRTree(rtreeFile);           // Build R-tree in memory
        int rootId = findRootId();      // Locate root node (max nodeId)

        BufferedReader br = new BufferedReader(new FileReader(queryFile));
        String line;
        int queryIndex = 0;             // Keeps track of the line index

        // Process each query line
        while ((line = br.readLine()) != null) {
            String[] parts = line.trim().split("\\s+");

            // Create a query MBR based on the rectangle input
            double xMin = Double.parseDouble(parts[0]);
            double yMin = Double.parseDouble(parts[1]);
            double xMax = Double.parseDouble(parts[2]);
            double yMax = Double.parseDouble(parts[3]);
            MBR queryMBR = new MBR(xMin, xMax, yMin, yMax);

            Set<Integer> resultIds = new TreeSet<>(); // Use TreeSet to store unique sorted IDs

            // Execute range search
            rangeSearch(rootId, queryMBR, resultIds);

            // Print the results in the required format
            System.out.print(queryIndex + " (" + resultIds.size() + "): ");
            StringBuilder sb = new StringBuilder();
            Iterator<Integer> iter = resultIds.iterator();
            while (iter.hasNext()) {
                sb.append(iter.next());
                if (iter.hasNext()) sb.append(",");
            }
            System.out.println(sb.toString());

            queryIndex++;
        }

        br.close();
    }

    
    
}
