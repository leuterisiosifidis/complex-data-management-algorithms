
import java.io.*;
import java.util.*;

public class KNNQuery {

    // Represents a Minimum Bounding Rectangle 
    static class MBR {
        double xMin, xMax, yMin, yMax;

        MBR(double xMin, double xMax, double yMin, double yMax) {
            this.xMin = xMin;
            this.xMax = xMax;
            this.yMin = yMin;
            this.yMax = yMax;
        }

        // Computes minimum distance from a point (x,y) to this MBR
        double minDist(double x, double y) {
            double dx = 0.0, dy = 0.0;
            if (x < xMin) dx = xMin - x;
            else if (x > xMax) dx = x - xMax;
            if (y < yMin) dy = yMin - y;
            else if (y > yMax) dy = y - yMax;
            return Math.sqrt(dx * dx + dy * dy);
        }
    }

    // Entry object representing either a data item or a child node in the R-tree
    static class Entry {
        int id;
        MBR mbr;

        Entry(int id, MBR mbr) {
            this.id = id;
            this.mbr = mbr;
        }
    }

    // Node in the R-tree. Contains entries and optionally child nodes
    static class RTreeNode {
        boolean isNonLeaf;
        List<Entry> entries = new ArrayList<>();
        List<RTreeNode> children = new ArrayList<>(); // Only applicable for non-leaf nodes
    }

    // Entry used in the priority queue for kNN search
    static class PQEntry implements Comparable<PQEntry> {
        double distance;
        boolean isNode; // true if this entry refers to a node, false if to a data object
        RTreeNode node; // if isNode == true
        Entry object;   // if isNode == false

        PQEntry(double distance, RTreeNode node) {
            this.distance = distance;
            this.isNode = true;
            this.node = node;
        }

        PQEntry(double distance, Entry object) {
            this.distance = distance;
            this.isNode = false;
            this.object = object;
        }

        public int compareTo(PQEntry other) {
            return Double.compare(this.distance, other.distance);
        }
    }

    // Load the R-tree from file and return the root node
    static RTreeNode loadRTree(String filename) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filename));
        Map<Integer, RTreeNode> nodeMap = new HashMap<>();
        String line;

        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) continue;

            // Format: [1, nodeId, [[entryId, xMin, xMax, yMin, yMax], ...]]
            line = line.substring(1, line.length() - 1);
            String[] parts = line.split(",", 3);

            boolean isNonLeaf = parts[0].trim().equals("1");
            int nodeId = Integer.parseInt(parts[1].trim());
            RTreeNode node = new RTreeNode();
            node.isNonLeaf = isNonLeaf;

            String entriesRaw = parts[2].trim();
            entriesRaw = entriesRaw.substring(1, entriesRaw.length() - 1);
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

            nodeMap.put(nodeId, node);
        }

        br.close();

        // Link children to non-leaf nodes
        for (Map.Entry<Integer, RTreeNode> entry : nodeMap.entrySet()) {
            RTreeNode node = entry.getValue();
            if (node.isNonLeaf) {
                for (Entry e : node.entries) {
                    node.children.add(nodeMap.get(e.id));
                }
            }
        }

        // Assume the node with the highest ID is the root
        int rootId = Collections.max(nodeMap.keySet());
        return nodeMap.get(rootId);
    }

    // Perform k-nearest neighbor search using a priority queue
    static List<Integer> kNN(RTreeNode root, double qx, double qy, int k) {
        PriorityQueue<PQEntry> pq = new PriorityQueue<>();
        List<Integer> result = new ArrayList<>();

        pq.add(new PQEntry(0.0, root)); // Start with root

        while (!pq.isEmpty() && result.size() < k) {
            PQEntry current = pq.poll();

            if (current.isNode) {
                RTreeNode node = current.node;
                if (node.isNonLeaf) {
                    for (int i = 0; i < node.entries.size(); i++) {
                        Entry e = node.entries.get(i);
                        RTreeNode child = node.children.get(i);
                        pq.add(new PQEntry(e.mbr.minDist(qx, qy), child));
                    }
                } else {
                    for (Entry e : node.entries) {
                        pq.add(new PQEntry(e.mbr.minDist(qx, qy), e));
                    }
                }
            } else {
                result.add(current.object.id); // Found one of the k-nearest neighbors
            }
        }

        return result;
    }

     
    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            System.out.println("Usage: java KNNQuery Rtree.txt NNqueries.txt k");
            return;
        }

        String rtreeFile = args[0];
        String queryFile = args[1];
        int k = Integer.parseInt(args[2]);

        RTreeNode root = loadRTree(rtreeFile);//loads R-tree

        BufferedReader br = new BufferedReader(new FileReader(queryFile));
        String line;
        int queryIndex = 0;
        //reads query points, and prints k-nearest neighbors
        while ((line = br.readLine()) != null) {
            String[] parts = line.trim().split("\\s+");
            double x = Double.parseDouble(parts[0]);
            double y = Double.parseDouble(parts[1]);

            List<Integer> knnResults = kNN(root, x, y, k);

            System.out.print(queryIndex + ": ");
            for (int i = 0; i < knnResults.size(); i++) {
                if (i > 0) System.out.print(",");
                System.out.print(knnResults.get(i));
            }
            System.out.println();
            queryIndex++;
        }

        br.close();
    }
}

