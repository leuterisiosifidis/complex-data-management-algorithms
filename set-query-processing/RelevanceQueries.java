

import java.io.*;
import java.util.*;

public class RelevanceQueries {

    public static void main(String[] args) throws IOException {
        if (args.length < 5) {
            System.err.println("Usage: java RelevanceQueries transactions.txt queries.txt qnum method k");
            return;
        }

        // Parse command-line arguments
        String transFile = args[0];
        String queryFile = args[1];
        int qnum = Integer.parseInt(args[2]); // query number (-1 = all)
        int method = Integer.parseInt(args[3]); // method (0 = naive, 1 = inverted, -1 = both)
        int k = Integer.parseInt(args[4]); // number of top-k results to return

        // Load transactions and queries from input files
        List<Map<Integer, Integer>> transactions = loadWithCounts(transFile);
        List<Set<Integer>> queries = loadQueries(queryFile);

        // Build inverted index and compute inverse frequencies
        Map<Integer, List<int[]>> inverted = buildInvertedIndex(transactions);
        Map<Integer, Double> invfreq = computeInverseFrequency(inverted, transactions.size());

        // Save inverted index and frequencies to file
        saveInvertedFile(inverted, invfreq, "invfileocc.txt");

        // Execute queries
        if (qnum == -1) { // Run all queries
            if (method == 0 || method == -1) {
                long start = System.currentTimeMillis();
                for (Set<Integer> query : queries) {
                    relevanceNaive(transactions, query, invfreq);
                }
                long end = System.currentTimeMillis();
                System.out.println("Naive Method computation time = " + (end - start) + " ms");
            }

            if (method == 1 || method == -1) {
                long start = System.currentTimeMillis();
                for (Set<Integer> query : queries) {
                    relevanceInverted(query, inverted, invfreq);
                }
                long end = System.currentTimeMillis();
                System.out.println("Inverted File computation time = " + (end - start) + " ms");
            }
        } else { // Run a specific query
            Set<Integer> query = queries.get(qnum);

            if (method == 0 || method == -1) {
                long start = System.currentTimeMillis();
                List<double[]> result = relevanceNaive(transactions, query, invfreq);
                long end = System.currentTimeMillis();
                System.out.println("Naive Method result:");
                System.out.println(formatTopK(result, k));
                System.out.println("Naive Method computation time = " + (end - start) + " ms");
            }

            if (method == 1 || method == -1) {
                long start = System.currentTimeMillis();
                List<double[]> result = relevanceInverted(query, inverted, invfreq);
                long end = System.currentTimeMillis();
                System.out.println("Inverted File result:");
                System.out.println(formatTopK(result, k));
                System.out.println("Inverted File computation time = " + (end - start) + " ms");
            }
        }
    }

    // Load transactions as List of Map<ItemID, Count>
    public static List<Map<Integer, Integer>> loadWithCounts(String filename) throws IOException {
        List<Map<Integer, Integer>> transactions = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String line;

        while ((line = reader.readLine()) != null) {
            line = line.replaceAll("[\\[\\],]", " "); // remove brackets/commas
            Map<Integer, Integer> transaction = new HashMap<>();
            for (String token : line.trim().split("\\s+")) {
                if (!token.isEmpty()) {
                    int item = Integer.parseInt(token);
                    transaction.put(item, transaction.getOrDefault(item, 0) + 1);
                }
            }
            transactions.add(transaction);
        }

        reader.close();
        return transactions;
    }

    // Load queries (set semantics)
    public static List<Set<Integer>> loadQueries(String filename) throws IOException {
        List<Set<Integer>> queries = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String line;

        while ((line = reader.readLine()) != null) {
            line = line.replaceAll("[\\[\\],]", " ");
            Set<Integer> query = new HashSet<>();
            for (String token : line.trim().split("\\s+")) {
                if (!token.isEmpty()) {
                    query.add(Integer.parseInt(token));
                }
            }
            queries.add(query);
        }

        reader.close();
        return queries;
    }

    // Build inverted index
    public static Map<Integer, List<int[]>> buildInvertedIndex(List<Map<Integer, Integer>> transactions) {
        Map<Integer, List<int[]>> inverted = new HashMap<>();

        for (int tid = 0; tid < transactions.size(); tid++) {
            for (Map.Entry<Integer, Integer> entry : transactions.get(tid).entrySet()) {
                int item = entry.getKey();
                int count = entry.getValue();
                inverted.putIfAbsent(item, new ArrayList<>());
                inverted.get(item).add(new int[]{tid, count});
            }
        }

        return inverted;
    }

    // Compute inverse frequency: |T| / trf(i)
    public static Map<Integer, Double> computeInverseFrequency(Map<Integer, List<int[]>> inverted, int totalTransactions) {
        Map<Integer, Double> invfreq = new HashMap<>();

        for (Map.Entry<Integer, List<int[]>> entry : inverted.entrySet()) {
            int item = entry.getKey();
            int trf = entry.getValue().size();
            invfreq.put(item, totalTransactions / (double) trf);
        }

        return invfreq;
    }

    // Save inverted file: item
    public static void saveInvertedFile(Map<Integer, List<int[]>> inverted, Map<Integer, Double> invfreq, String filename) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
        List<Integer> items = new ArrayList<>(inverted.keySet());
        Collections.sort(items);

        for (int item : items) {
            writer.write(item + ": " + invfreq.get(item) + ", [");
            List<int[]> pairs = inverted.get(item);
            for (int i = 0; i < pairs.size(); i++) {
                int[] p = pairs.get(i);
                writer.write("[" + p[0] + ", " + p[1] + "]");
                if (i < pairs.size() - 1) writer.write(", ");
            }
            writer.write("]");
            writer.newLine();
        }

        writer.close();
    }

    // Naïve relevance computation
    public static List<double[]> relevanceNaive(List<Map<Integer, Integer>> transactions, Set<Integer> query, Map<Integer, Double> invfreq) {
        List<double[]> result = new ArrayList<>();

        for (int tid = 0; tid < transactions.size(); tid++) {
            Map<Integer, Integer> trans = transactions.get(tid);
            double score = 0;

            for (int item : query) {
                if (trans.containsKey(item)) {
                    score += trans.get(item) * invfreq.getOrDefault(item, 0.0);
                }
            }

            if (score > 0) {
                result.add(new double[]{score, tid});
            }
        }

        result.sort((a, b) -> Double.compare(b[0], a[0]));
        return result;
    }

    // Inverted file relevance computation
    public static List<double[]> relevanceInverted(Set<Integer> query, Map<Integer, List<int[]>> inverted, Map<Integer, Double> invfreq) {
        Map<Integer, Double> relMap = new HashMap<>();

        for (int item : query) {
            if (!inverted.containsKey(item)) continue;
            double weight = invfreq.get(item);

            for (int[] pair : inverted.get(item)) {
                int tid = pair[0], count = pair[1];
                relMap.put(tid, relMap.getOrDefault(tid, 0.0) + count * weight);
            }
        }

        List<double[]> result = new ArrayList<>();
        for (Map.Entry<Integer, Double> e : relMap.entrySet()) {
            result.add(new double[]{e.getValue(), e.getKey()});
        }

        result.sort((a, b) -> Double.compare(b[0], a[0]));
        return result;
    }

    // Format result as a top-k list
    public static String formatTopK(List<double[]> results, int k) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < Math.min(k, results.size()); i++) {
            double[] r = results.get(i);
            sb.append("[" + r[0] + ", " + (int) r[1] + "]");
            if (i < k - 1 && i < results.size() - 1) sb.append(", ");
        }
        sb.append("]");
        return sb.toString();
    }

} 