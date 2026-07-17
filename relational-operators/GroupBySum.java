
import java.io.*;

public class GroupBySum {
    public static void main(String[] args) throws IOException {
        //2 exactly arguments
        if (args.length != 2) {
            System.err.println("Usage: java GroupBySum <R.tsv> <Rgroupby.tsv>");
            System.exit(1);
        }
        
        groupByAndDoSum(args[0], args[1]);//1000
    }

    public static void groupByAndDoSum(String inputFile, String outputFile) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(inputFile));
        String[] lines = reader.lines().toArray(String[]::new);
        reader.close();

        mergeSort(lines, 0, lines.length - 1);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            for (String line : lines) {
                writer.write(line + "\n");
            }
        }
        System.out.println("Set difference completed. Output written to " + outputFile);
    }
    //mergeSort algorithm
    private static void mergeSort(String[] arr, int left, int right) {
        if (left < right) {
            int mid = left + (right - left) / 2;
            mergeSort(arr, left, mid);
            mergeSort(arr, mid + 1, right);
            merge(arr, left, mid, right);
        }
    }
    //compares Keys and avoids dublicates
    private static void merge(String[] arr, int left, int mid, int right) {
        String[] temp = new String[right - left + 1];
        int i = left, j = mid + 1, k = 0;
        String lastKey = null;
        int lastSum = 0;

        while (i <= mid && j <= right) {
            String[] leftParts = arr[i].split("\t");
            String[] rightParts = arr[j].split("\t");
            String leftKey = leftParts[0];
            String rightKey = rightParts[0];
            int leftValue = Integer.parseInt(leftParts[1]);
            int rightValue = Integer.parseInt(rightParts[1]);

            if (leftKey.equals(rightKey)) {
                lastKey = leftKey;
                lastSum = leftValue + rightValue;
                i++;
                j++;
            } else if (leftKey.compareTo(rightKey) < 0) {
                lastKey = leftKey;
                lastSum = leftValue;
                i++;
            } else {
                lastKey = rightKey;
                lastSum = rightValue;
                j++;
            }
            temp[k++] = lastKey + "\t" + lastSum;
        }

        while (i <= mid) temp[k++] = arr[i++];
        while (j <= right) temp[k++] = arr[j++];

        System.arraycopy(temp, 0, arr, left, k);
    }
    
}
