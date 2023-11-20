// This file contains the main() function for the Scheduling
// simulation.  Init() initializes most of the variables by
// reading from a provided file.  SchedulingAlgorithm.Run() is
// called from main() to run the simulation.  Summary-Results
// is where the summary results are written, and Summary-Processes
// is where the process scheduling summary is written.

import java.io.*;
import java.util.*;

public class Scheduling {

    // Configuration file path
    private final static String filePath = "scheduling.conf";

    private static int processNum = 5;
    private static int meanDev = 1000;
    private static int standardDev = 100;
    private static int runtime = 1000;
    private static int blockingTime = 20;

    // Vector to store processes
    private static Vector<Process> processVector = new Vector<>();

    // Results object to store scheduling results
    private static Results result = new Results("null", "null", 0);

    // File to store the summary of results
    private static String resultsFile = "Summary-Results";

    // Initialize the scheduling parameters from the configuration file
    private static void Init(String file) {
        File f = new File(file);
        String line;

        try {
            DataInputStream in = new DataInputStream(new FileInputStream(f));
            while ((line = in.readLine()) != null) {
                // Process the configuration parameters from the file
                if (line.startsWith("numprocess")) {
                    StringTokenizer st = new StringTokenizer(line);
                    st.nextToken();
                    processNum = Common.s2i(st.nextToken());
                }
                if (line.startsWith("meandev")) {
                    StringTokenizer st = new StringTokenizer(line);
                    st.nextToken();
                    meanDev = Common.s2i(st.nextToken());
                }
                if (line.startsWith("standdev")) {
                    StringTokenizer st = new StringTokenizer(line);
                    st.nextToken();
                    standardDev = Common.s2i(st.nextToken());
                }
                if (line.startsWith("blockingTime")) {
                    StringTokenizer st = new StringTokenizer(line);
                    st.nextToken();
                    blockingTime = Common.s2i(st.nextToken());
                }
                if (line.startsWith("process")) {
                    // Process-specific parameters
                    StringTokenizer st = new StringTokenizer(line);
                    st.nextToken();
                    int ioblocking = Common.s2i(st.nextToken());
                    double X = Common.R1();
                    while (X == -1.0) {
                        X = Common.R1();
                    }
                    X = X * standardDev;
                    int cputime = (int) X + meanDev;
                    processVector.addElement(new Process(cputime, ioblocking, 0, 0, 0, blockingTime, false, -1));
                }
                if (line.startsWith("runtime")) {
                    // Total runtime of the simulation
                    StringTokenizer st = new StringTokenizer(line);
                    st.nextToken();
                    runtime = Common.s2i(st.nextToken());
                }
            }
            in.close();
        } catch (IOException e) {
            // Handle exceptions
        }
    }

    // Debug method to print the initialized parameters and process details
    private static void debug() {
        int i = 0;

        System.out.println("processnum " + processNum);
        System.out.println("meandev " + meanDev);
        System.out.println("standdev " + standardDev);
        int size = processVector.size();
        for (i = 0; i < size; i++) {
            Process process = (Process) processVector.elementAt(i);
            System.out.println("process " + i + " " + process.cputime + " " + process.ioblocking + " " + process.cpudone + " " + process.numblocked);
        }
        System.out.println("runtime " + runtime);
    }

    // Main method to execute the scheduling simulation
    public static void main(String[] args) {
        if (args == null || args.length == 0) args = new String[]{filePath};

        int i = 0;

        // Validate command-line arguments
        if (args.length != 1) {
            System.out.println("Usage: 'java Scheduling <INIT FILE>'");
            System.exit(-1);
        }

        // Check if the specified file exists and is readable
        File f = new File(args[0]);
        if (!(f.exists())) {
            System.out.println("Scheduling: error, file '" + f.getName() + "' does not exist.");
            System.exit(-1);
        }
        if (!(f.canRead())) {
            System.out.println("Scheduling: error, read of " + f.getName() + " failed.");
            System.exit(-1);
        }

        System.out.println("Working...");

        // Initialize scheduling parameters and processes from the configuration file
        Init(args[0]);

        // If the number of initialized processes is less than the specified number, generate additional processes
        if (processVector.size() < processNum) {
            i = 0;
            while (processVector.size() < processNum) {
                double X = Common.R1();
                while (X == -1.0) {
                    X = Common.R1();
                }
                X = X * standardDev;
                int cputime = (int) X + meanDev;
                processVector.addElement(new Process(cputime, i * 100, 0, 0, 0, blockingTime, false, -1));
                i++;
            }
        }

        // Run the scheduling algorithm
        result = SchedulingAlgorithm.Run(runtime, processVector, result);

        // Write the summary of results to a file
        try {
            PrintStream out = new PrintStream(new FileOutputStream(resultsFile));
            out.println("Scheduling Type: " + result.schedulingType);
            out.println("Scheduling Name: " + result.schedulingName);
            out.println("Simulation Run Time: " + result.compuTime);
            out.println("Mean: " + meanDev);
            out.println("Standard Deviation: " + standardDev);
            out.println("Process #\tCPU Time\tIO Blocking\tCPU Completed\tCPU Blocked");

            for (i = 0; i < processVector.size(); i++) {
                Process process = (Process) processVector.elementAt(i);
                out.print(i);
                if (i < 100) {
                    out.print("\t\t");
                } else {
                    out.print("\t");
                }
                out.print(process.cputime);
                if (process.cputime < 100) {
                    out.print(" (ms)\t\t");
                } else {
                    out.print(" (ms)\t");
                }
                out.print(process.ioblocking);
                if (process.ioblocking < 100) {
                    out.print(" (ms)\t\t");
                } else {
                    out.print(" (ms)\t");
                }
                out.print(process.cpudone);
                if (process.cpudone < 100) {
                    out.print(" (ms)\t\t");
                } else {
                    out.print(" (ms)\t");
                }
                out.println(process.numblocked + " times");
            }

            out.close();
        } catch (IOException e) {
            // Handle exceptions
        }

        System.out.println("Completed.");
    }
}
