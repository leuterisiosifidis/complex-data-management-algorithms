
import java.io.*;
import java.util.*;

public class RTreeBuilder {

    // Class to represent a 2D point
    static class Point {
        double x, y;
        Point(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    // Class to represent a Minimum Bounding Rectangle (MBR)
    static class MBR {
        double xMin, xMax, yMin, yMax;

        MBR(double xMin, double xMax, double yMin, double yMax) {
            this.xMin = xMin;
            this.xMax = xMax;
            this.yMin = yMin;
            this.yMax = yMax;
        }

        // Create MBR from a list of points
        static MBR SquareFromPoints(List<Point> points) {
            double xMin = Double.MAX_VALUE, xMax = -Double.MAX_VALUE;
            double yMin = Double.MAX_VALUE, yMax = -Double.MAX_VALUE;
            for (Point p : points) {
                xMin = Math.min(xMin, p.x);
                xMax = Math.max(xMax, p.x);
                yMin = Math.min(yMin, p.y);
                yMax = Math.max(yMax, p.y);
            }
            return new MBR(xMin, xMax, yMin, yMax);
        }

        // Get the center point of the MBR
        Point getCenter() {
            return new Point((xMin + xMax) / 2, (yMin + yMax) / 2);
        }

        public String toString() {
            return "[" + xMin + ", " + xMax + ", " + yMin + ", " + yMax + "]";
        }
    }

    // Class representing a polygon with an ID and MBR
    static class Polygon {
        int id;
        List<Point> points;
        MBR mbr;
        long zValue;

        Polygon(int id, List<Point> points) {
            this.id = id;
            this.points = points;
            this.mbr = MBR.SquareFromPoints(points);
            Point center = this.mbr.getCenter();
            this.zValue = computeZOrder(center.x, center.y);
        }
    }

    // Class representing a node in the R-tree
    static class RTreeNode {
        boolean isNonLeaf;
        int nodeId;
        List<Entry> entries;

        RTreeNode(boolean isNonLeaf, int nodeId, List<Entry> entries) {
            this.isNonLeaf = isNonLeaf;
            this.nodeId = nodeId;
            this.entries = entries;
        }

        // Entry: either a child node or polygon
        static class Entry {
            int id;
            MBR mbr;

            Entry(int id, MBR mbr) {
                this.id = id;
                this.mbr = mbr;
            }

            public String toString() {
                return "[" + id + ", " + mbr.toString() + "]";
            }
        }

        public String toString() {
            return "[" + (isNonLeaf ? 1 : 0) + ", " + nodeId + ", " + entries + "]";
        }
    }

    // Morton Z-order encoding (bit interleaving)
    static long interleave(int x, int y) {
        long z = 0;
        for (int i = 0; i < 32; i++) {
            z |= ((long)(y >> i) & 1L) << (2 * i + 1);
            z |= ((long)(x >> i) & 1L) << (2 * i);
        }
        return z;
    }

    // Compute Z-order value for a real-valued point
    static long computeZOrder(double x, double y) {
        double scale = 10000.0;
        int nx = (int)(x * scale);
        int ny = (int)(y * scale);
        return interleave(nx, ny);
    }

    // Compute MBR that wraps around a list of entries
    static MBR getUnionMBR(List<RTreeNode.Entry> entries) {
        double xMin = Double.MAX_VALUE, xMax = -Double.MAX_VALUE;
        double yMin = Double.MAX_VALUE, yMax = -Double.MAX_VALUE;
        for (RTreeNode.Entry e : entries) {
            MBR m = e.mbr;
            xMin = Math.min(xMin, m.xMin);
            xMax = Math.max(xMax, m.xMax);
            yMin = Math.min(yMin, m.yMin);
            yMax = Math.max(yMax, m.yMax);
        }
        return new MBR(xMin, xMax, yMin, yMax);
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.out.println("Usage: java RTreeBuilder coords.txt offsets.txt");
            return;
        }

        String coordsFile = args[0];
        String offsetsFile = args[1];

        // Read all coordinates
        List<Point> allCoords = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(coordsFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                allCoords.add(new Point(Double.parseDouble(parts[0]), Double.parseDouble(parts[1])));
            }
        }

        // Read polygons and compute MBR + zValue
        List<Polygon> polygons = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(offsetsFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                int id = Integer.parseInt(parts[0]);
                int start = Integer.parseInt(parts[1]);
                int end = Integer.parseInt(parts[2]);

                if (start < 0 || end < start || end >= allCoords.size()) {
                    System.err.println("Skipping invalid polygon ID " + id);
                    continue;
                }

                List<Point> pts = new ArrayList<>();
                for (int i = start; i <= end; i++) {
                    pts.add(allCoords.get(i));
                }

                polygons.add(new Polygon(id, pts));
            }
        }

        // Sort polygons by Z-order
        polygons.sort(Comparator.comparingLong(p -> p.zValue));

        int maxEntries = 20, minEntries = 8;
        List<RTreeNode> currentLevel = new ArrayList<>();
        int nodeCounter = 0;

        // Create leaf nodes
        for (int i = 0; i < polygons.size(); ) {
            int remaining = polygons.size() - i;
            int entriesInNode = Math.min(maxEntries, remaining);
            //if less than 8 nodes
            if (remaining < minEntries && !currentLevel.isEmpty()) {
                RTreeNode prev = currentLevel.remove(currentLevel.size() - 1);
                List<RTreeNode.Entry> combined = new ArrayList<>(prev.entries);
                for (int j = i; j < polygons.size(); j++) {
                    combined.add(new RTreeNode.Entry(polygons.get(j).id, polygons.get(j).mbr));
                }

                int mid = combined.size() / 2;
                currentLevel.add(new RTreeNode(false, nodeCounter++, combined.subList(0, mid)));
                currentLevel.add(new RTreeNode(false, nodeCounter++, combined.subList(mid, combined.size())));
                break;
            }

            List<RTreeNode.Entry> entries = new ArrayList<>();
            for (int j = 0; j < entriesInNode && i < polygons.size(); j++, i++) {
                Polygon p = polygons.get(i);
                entries.add(new RTreeNode.Entry(p.id, p.mbr));
            }

            currentLevel.add(new RTreeNode(false, nodeCounter++, entries));
        }

        // Build inner nodes
        List<RTreeNode> allNodes = new ArrayList<>();
        Map<Integer, Integer> levelCount = new LinkedHashMap<>();
        int level = 0;

        while (currentLevel.size() > 1) {
            levelCount.put(level++, currentLevel.size());
            List<RTreeNode> nextLevel = new ArrayList<>();

            for (int i = 0; i < currentLevel.size(); ) {
                int remaining = currentLevel.size() - i;
                int entriesInNode = Math.min(maxEntries, remaining);
                //if less than 8 nodes
                if (remaining < minEntries && !nextLevel.isEmpty()) {
                    RTreeNode prev = nextLevel.remove(nextLevel.size() - 1);
                    List<RTreeNode.Entry> combined = new ArrayList<>(prev.entries);
                    for (int j = i; j < currentLevel.size(); j++) {
                        RTreeNode child = currentLevel.get(j);
                        combined.add(new RTreeNode.Entry(child.nodeId, getUnionMBR(child.entries)));
                    }

                    int mid = combined.size() / 2;
                    nextLevel.add(new RTreeNode(true, nodeCounter++, combined.subList(0, mid)));
                    nextLevel.add(new RTreeNode(true, nodeCounter++, combined.subList(mid, combined.size())));
                    break;
                }

                List<RTreeNode.Entry> entries = new ArrayList<>();
                for (int j = 0; j < entriesInNode && i < currentLevel.size(); j++, i++) {
                    RTreeNode child = currentLevel.get(i);
                    entries.add(new RTreeNode.Entry(child.nodeId, getUnionMBR(child.entries)));
                }

                nextLevel.add(new RTreeNode(true, nodeCounter++, entries));
            }

            allNodes.addAll(currentLevel);
            currentLevel = nextLevel;
        }

        // Add root node
        levelCount.put(level, currentLevel.size());
        allNodes.addAll(currentLevel);

        // Print level count
        for (Map.Entry<Integer, Integer> entry : levelCount.entrySet()) {
            System.out.println(entry.getValue() + " nodes at level " + entry.getKey());
        }

        // Output R-tree to file
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("Rtree.txt"))) {
            allNodes.sort(Comparator.comparingInt(n -> n.nodeId));
            for (RTreeNode node : allNodes) {
                bw.write(node.toString());
                bw.newLine();
            }
        }
    }
}
