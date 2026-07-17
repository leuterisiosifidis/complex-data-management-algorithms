
import java.io.*;

public class SetDifference {
    public static void main(String[] args) throws IOException {
        //3 exactly arguments
        if (args.length != 3) {
            System.err.println("Usage: java SetDifference <R_sorted.tsv> <S_sorted.tsv> <RdifferenceS.tsv>");
            System.exit(1);
        }

        difference(args[0], args[1], args[2]);//976
    }

    public static void difference(String rFile, String sFile, String outputFile) throws IOException {
        try (BufferedReader rReader = new BufferedReader(new FileReader(rFile));
             BufferedReader sReader = new BufferedReader(new FileReader(sFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {

            String rPointer = rReader.readLine(); //  R pointer
            String sPointer = sReader.readLine(); //  S pointer
            String lastWritten = null; 

            // Process R and S simultaneously 
            while (rPointer != null) {
                // Move S forward until it reaches or passes R
                while (sPointer != null && sPointer.compareTo(rPointer) < 0) {
                    sPointer = sReader.readLine();
                }

                // If R's current line does not exist in S, write it to the output
                if (sPointer == null || !rPointer.equals(sPointer)) {
                    if (lastWritten == null || !rPointer.equals(lastWritten)) {
                        writer.write(rPointer + "\n");
                        lastWritten = rPointer; // Store last written value to avoid duplicates
                    }
                }

                // Move R forward
                rPointer = rReader.readLine();
            }

            System.out.println("Set difference completed. Output written to " + outputFile);
        }
    }
}
