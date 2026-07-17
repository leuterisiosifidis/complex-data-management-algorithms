

import java.io.*;
import java.math.BigInteger;
import java.util.*;

public class ContainmentQueries {

    public static void main(String[] args) throws IOException {
        
        if (args.length < 4) {
            System.err.println("Usage: java ContainmentQueries transactions.txt queries.txt qnum method");
            return;
        }

        // Read command-line arguments
        String transactionsFile = args[0];
        String queriesFile = args[1];
        int qnum = Integer.parseInt(args[2]);    // Which query to run (-1 = all)
        int method = Integer.parseInt(args[3]);  // Which method to run (-1 = all)

        // Load transactions and queries from files
        List<Set<Integer>> transactions = load(transactionsFile);
        List<Set<Integer>> queries = load(queriesFile);

        // Method 0: Naïve Method
        if (method == 0 || method == -1) {
            long startTime = System.currentTimeMillis();

            if (qnum == -1) {
                // Evaluate all queries 
                for (Set<Integer> query : queries) {
                    naiveMethod(transactions, query);
                }
            } else {
                // Run a specific query
                Set<Integer> query = queries.get(qnum);
                List<Integer> result = naiveMethod(transactions, query);
                System.out.println("Naive Method result: " + result);
            }

            long endTime = System.currentTimeMillis();
            System.out.println("Naive Method computation time = " + (endTime - startTime) + " ms");
        }

        // Method 1: Exact Signature File 
        if (method == 1 || method == -1) {
            long startTime = System.currentTimeMillis();

            // Build bitmap signatures for all transactions
            List<BigInteger> sigfile = buildSignatures(transactions);
            saveToFile(sigfile, "sigfile.txt");

            if (qnum != -1) {
                // Convert query to bitmap and match using bitwise AND
                Set<Integer> query = queries.get(qnum);
                List<Integer> result = signatureMethod(sigfile, query);
                System.out.println("Exact Signature File result: " + result);
            }

            long endTime = System.currentTimeMillis();
            System.out.println("Exact Signature File computation time = " + (endTime - startTime) + " ms");
        }

        // Method 2: Exact Bitsliced Signature File 
        if (method == 2 || method == -1) {
            long startTime = System.currentTimeMillis();

            // Build bitmap per item
            Map<Integer, BitSet> bitslice = buildBitslice(transactions);
            saveBitslice(bitslice, transactions.size(), "bitslice.txt");

            if (qnum != -1) {
                Set<Integer> query = queries.get(qnum);
                List<Integer> result = bitsliceMethod(bitslice, query, transactions.size());
                System.out.println("Exact Bitsliced Signature File result: " + result);
            }

            long endTime = System.currentTimeMillis();
            System.out.println("Exact Bitsliced Signature File computation time = " + (endTime - startTime) + " ms");
        }

        // Method 3: Inverted File 
        if (method == 3 || method == -1) {
            long startTime = System.currentTimeMillis();

            // Build inverted index for items
            Map<Integer, List<Integer>> inverted = buildInvertedFile(transactions);
            saveInvertedFile(inverted, "invfile.txt");

            if (qnum != -1) {
                Set<Integer> query = queries.get(qnum);
                List<Integer> result = invertedFileMethod(inverted, query);
                System.out.println("Inverted File result: " + result);
            }

            long endTime = System.currentTimeMillis();
            System.out.println("Inverted File computation time = " + (endTime - startTime) + " ms");
        }
        
    }

   
    //Loads the input file and parses each line into a set of integers.
    public static List<Set<Integer>> load(String filename) throws IOException {
        List<Set<Integer>> transactions = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String line;

        while ((line = reader.readLine()) != null) {
            line = line.replaceAll("[\\[\\],]", " "); // Clean brackets or commas

            Set<Integer> transaction = new HashSet<>();
            for (String token : line.trim().split("\\s+")) {
                if (!token.isEmpty()) {
                    transaction.add(Integer.parseInt(token));
                }
            }

            transactions.add(transaction);
        }

        reader.close();
        return transactions;
    }

   
    //Return IDs of transactions that fully contain the query.
    public static List<Integer> naiveMethod(List<Set<Integer>> transactions, Set<Integer> query) {
        List<Integer> results = new ArrayList<>();

        for (int i = 0; i < transactions.size(); i++) {
            if (transactions.get(i).containsAll(query)) {
                results.add(i);
            }
        }

        return results;
    }

   
    //For each transaction, create a bitmap.
    public static List<BigInteger> buildSignatures(List<Set<Integer>> transactions) {
        List<BigInteger> sigfile = new ArrayList<>();

        for (Set<Integer> transaction : transactions) {
            BigInteger signature = BigInteger.ZERO;
            for (int item : transaction) {
                signature = signature.setBit(item);
            }
            sigfile.add(signature);
        }

        return sigfile;
    }

   
    //Save bitmap signatures to file, one per line.
    public static void saveToFile(List<BigInteger> sigfile, String filename) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
        for (BigInteger sig : sigfile) {
            writer.write(sig.toString());
            writer.newLine();
        }
        writer.close();
    }

 
    //Convert query into bitmap and use bitwise AND to check containment.
    public static List<Integer> signatureMethod(List<BigInteger> sigfile, Set<Integer> query) {
        List<Integer> results = new ArrayList<>();
        BigInteger querySig = BigInteger.ZERO;

        for (int item : query) {
            querySig = querySig.setBit(item);
        }

        for (int i = 0; i < sigfile.size(); i++) {
            if (sigfile.get(i).and(querySig).equals(querySig)) {
                results.add(i);
            }
        }

        return results;
    }

   
    //Build bitslice — a BitSet per item, storing transaction occurrences.
    public static Map<Integer, BitSet> buildBitslice(List<Set<Integer>> transactions) {
        Map<Integer, BitSet> bitslice = new HashMap<>();

        for (int tID = 0; tID < transactions.size(); tID++) {
            for (int item : transactions.get(tID)) {
                bitslice.putIfAbsent(item, new BitSet(transactions.size()));
                bitslice.get(item).set(tID);
            }
        }

        return bitslice;
    }

   
    //Save bitslice to file as 0/1 strings per item.
    public static void saveBitslice(Map<Integer, BitSet> bitslice, int totalTransactions, String filename) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
        List<Integer> sortedKeys = new ArrayList<>(bitslice.keySet());
        Collections.sort(sortedKeys);

        for (int item : sortedKeys) {
            BitSet bs = bitslice.get(item);
            StringBuilder sb = new StringBuilder();
            sb.append(item).append(": ");
            for (int i = 0; i < totalTransactions; i++) {
                sb.append(bs.get(i) ? '1' : '0');
            }
            writer.write(sb.toString());
            writer.newLine();
        }

        writer.close();
    }

    //Evaluate query using AND across the query items' bitsets.
    public static List<Integer> bitsliceMethod(Map<Integer, BitSet> bitslice, Set<Integer> query, int totalTransactions) {
        List<Integer> results = new ArrayList<>();
        BitSet result = new BitSet(totalTransactions);
        result.set(0, totalTransactions); // Initially all bits 1

        for (int item : query) {
            BitSet itemBitset = bitslice.get(item);
            if (itemBitset == null) return results; // item not found
            result.and(itemBitset);
        }

        for (int i = result.nextSetBit(0); i >= 0; i = result.nextSetBit(i + 1)) {
            results.add(i);
        }

        return results;
    }


    //Build inverted index 
    public static Map<Integer, List<Integer>> buildInvertedFile(List<Set<Integer>> transactions) {
        Map<Integer, List<Integer>> inverted = new HashMap<>();

        for (int tID = 0; tID < transactions.size(); tID++) {
            for (int item : transactions.get(tID)) {
                inverted.putIfAbsent(item, new ArrayList<>());
                inverted.get(item).add(tID);
            }
        }

        for (List<Integer> list : inverted.values()) {
            Collections.sort(list);
        }

        return inverted;
    }

   
    //Save inverted index to file.
    public static void saveInvertedFile(Map<Integer, List<Integer>> inverted, String filename) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
        List<Integer> sortedKeys = new ArrayList<>(inverted.keySet());
        Collections.sort(sortedKeys);

        for (int item : sortedKeys) {
            writer.write(item + ": " + inverted.get(item).toString());
            writer.newLine();
        }

        writer.close();
    }


    //Evaluate query by intersecting sorted transaction ID lists.
    public static List<Integer> invertedFileMethod(Map<Integer, List<Integer>> inverted, Set<Integer> query) {
        List<List<Integer>> lists = new ArrayList<>();

        for (int item : query) {
            List<Integer> list = inverted.get(item);
            if (list == null) return new ArrayList<>();
            lists.add(list);
        }

        lists.sort(Comparator.comparingInt(List::size));
        List<Integer> result = new ArrayList<>(lists.get(0));

        for (int i = 1; i < lists.size(); i++) {
            result = intersectSortedLists(result, lists.get(i));
            if (result.isEmpty()) break;
        }

        return result;
    }


    //Helper: intersection of two sorted lists (merge-style).
    private static List<Integer> intersectSortedLists(List<Integer> a, List<Integer> b) {
        List<Integer> result = new ArrayList<>();
        int i = 0, j = 0;

        while (i < a.size() && j < b.size()) {
            int x = a.get(i), y = b.get(j);
            if (x == y) {
                result.add(x); i++; j++;
            } else if (x < y) {
                i++;
            } else {
                j++;
            }
        }

        return result;
    }
}
