

import java.io.*;

public class MergeJoin {
    public static void main(String[] args) throws IOException {
        //  exactly 3 arguments must be provided
        if (args.length != 3) {
            System.err.println("Usage: java MergeJoin <R_sorted.tsv> <S_sorted.tsv> <RjoinS.tsv>");
            System.exit(1);
        }

        int maxBufferSize = mergeJoin(args[0], args[1], args[2]);//
        System.out.println("Maximum buffer size used: " + maxBufferSize);//6
    }

    public static int mergeJoin(String rFile, String sFile, String outputFile) throws IOException {
        try (BufferedReader rReader = new BufferedReader(new FileReader(rFile));
             BufferedReader sReader = new BufferedReader(new FileReader(sFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {

            String rPointer = rReader.readLine();
            String sPointer = sReader.readLine();

            String[] sBuffer = new String[10]; // Initial capacity
            int sBufferSize = 0;
            int maxBufferSize = 0;
            String currentKey = null;

            while (rPointer != null && sPointer != null) {
                String[] rParts = rPointer.split("\t");
                String[] sParts = sPointer.split("\t");

                if (rParts.length < 2 || sParts.length < 2) {
                    rPointer = rReader.readLine();
                    sPointer = sReader.readLine();
                    continue;
                }

                String rKey = rParts[0];
                String sKey = sParts[0];
                int compare = rKey.compareTo(sKey);

                if (compare == 0) {
                    // Matching keys found
                    currentKey = rKey;
                    sBufferSize = 0;

                    // Buffer all S tuples with the current key
                    while (sPointer != null && sKey.equals(currentKey)) {
                        if (sBufferSize == sBuffer.length) {
                            sBuffer = resizeBuffer(sBuffer);
                        }
                        sBuffer[sBufferSize++] = sPointer;
                        sPointer = sReader.readLine();
                        if (sPointer != null) {
                            sParts = sPointer.split("\t");
                            sKey = sParts[0];
                        }
                    }
                    maxBufferSize = Math.max(maxBufferSize, sBufferSize);

                    // Process all R tuples with the current key
                    while (rPointer != null && rKey.equals(currentKey)) {
                        for (int i = 0; i < sBufferSize; i++) {
                            String[] sValues = sBuffer[i].split("\t");
                            writer.write(rParts[0] + "\t" + rParts[1] + "\t" + sValues[1] + "\n");
                        }
                        rPointer = rReader.readLine();
                        if (rPointer != null) {
                            rParts = rPointer.split("\t");
                            rKey = rParts[0];
                        }
                    }
                } else if (compare < 0) {
                    // R key is smaller - advance R
                    rPointer = rReader.readLine();
                } else {
                    // S key is smaller - advance S
                    sPointer = sReader.readLine();
                }
            }

            System.out.println("MergeJoin completed. Output written to " + outputFile);
            return maxBufferSize;
        }
    }

    //function to resize the Buffer
    private static String[] resizeBuffer(String[] buffer) {
        String[] newBuffer = new String[buffer.length * 2]; // Double the size
        System.arraycopy(buffer, 0, newBuffer, 0, buffer.length);
        return newBuffer;
    }
}
