
import java.io.*;


public class Intersection {
    public static void main(String[] args) throws IOException {
        //3 exactly arguments
        if (args.length != 3) {
            System.err.println("Usage: java Intersection <R_sorted.tsv> <S_sorted.tsv> <RdifferenceS.tsv>");
            System.exit(1);
        }

        intersection(args[0], args[1], args[2]);
        //16 tuples

    }
    public static void intersection(String rFile, String sFile, String outputFile) throws IOException {
        try (BufferedReader rReader = new BufferedReader(new FileReader(rFile));
             BufferedReader sReader = new BufferedReader(new FileReader(sFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {

            String rPointer = rReader.readLine();//R pointer
            String sPointer = sReader.readLine();//S pointer
            String lastWritten = null;

            while (rPointer != null && sPointer != null) {

                int compare = rPointer.compareTo(sPointer);
                
                if (compare < 0) {
                    // R is smaller - advance R
                    rPointer = rReader.readLine();
                } else if (compare > 0) {
                    // S is smaller - advance S
                    sPointer = sReader.readLine();
                } else {
                    // Found intersection
                    if (lastWritten == null || !rPointer.equals(lastWritten)) {
                        writer.write(rPointer + "\n");
                        lastWritten = rPointer;
                    }
                    // Advance both pointers
                    rPointer = rReader.readLine();
                    sPointer = sReader.readLine();
                }
            }

            System.out.println("Intersection completed. Output written to " + outputFile);
        }
    }
}