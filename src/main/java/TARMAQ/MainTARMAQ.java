package TARMAQ;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Example of how to run TARMAQ
 *
 * @author sadjad
 */
public class MainTARMAQ {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        
        String sequencesPath = "input.txt";
        String distFilePath = "output.txt";
        List<String> itemConstraint = Arrays.asList("a");

        Runtime rt = Runtime.getRuntime();
        long totalMem = rt.totalMemory();
        long maxMem = rt.maxMemory();
        long freeMem = rt.freeMemory();
        double megs = 1048576.0;

        System.out.println("Total Memory: " + totalMem + " (" + (totalMem / megs) + " MiB)");
        System.out.println("Max Memory:   " + maxMem + " (" + (maxMem / megs) + " MiB)");
        System.out.println("Free Memory:  " + freeMem + " (" + (freeMem / megs) + " MiB)");
        AlgoTARMAQExecutor.runFile(itemConstraint, sequencesPath, distFilePath);
    }
}
