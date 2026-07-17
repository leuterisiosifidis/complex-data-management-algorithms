
import java.io.*;

public class Union {

    public static void main(String[] args) throws IOException {
       //3 exactly arguments
        if (args.length != 3) {
            System.err.println("Usage: java Union <R_sorted.tsv> <S_sorted.tsv> <RunionS.tsv>");
            System.exit(1);
        }


        union(args[0], args[1], args[2]);//1969
    }

    public static void union(String rFile, String sFile, String outputFile) throws IOException {
        try (BufferedReader rReader = new BufferedReader(new FileReader(rFile));
             BufferedReader sReader = new BufferedReader(new FileReader(sFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {

            String rPointer = rReader.readLine(); 
            String sPointer = sReader.readLine(); 
            String lastWritten = null; 

            
            while (rPointer != null || sPointer != null) {
                String current; // Holds the line to be written

                if (rPointer == null) { // If R is empty, take from S
                    current = sPointer;
                    sPointer = sReader.readLine();
                } else if (sPointer == null) { // If S is empty, take from R
                    current = rPointer;
                    rPointer = rReader.readLine();
                } else { // Both R and S are not empty
                    int compare = rPointer.compareTo(sPointer);
                    if (compare < 0) { // R key is smaller, take from R
                        current = rPointer;
                        rPointer = rReader.readLine();
                    } else if (compare > 0) { // S key is smaller, take from S
                        current = sPointer;
                        sPointer = sReader.readLine();
                    } else { // Both keys are equal, take only one and move both pointers
                        current = rPointer;
                        rPointer = rReader.readLine();
                        sPointer = sReader.readLine();
                    }
                }

                // Avoid writing duplicate lines
                if (lastWritten == null || !current.equals(lastWritten)) {
                    writer.write(current + "\n");
                    lastWritten = current;
                }
            }

            System.out.println("Union completed. Output written to " + outputFile);
        }
    }
}
