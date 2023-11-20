import java.util.ArrayList;
import java.util.List;
import java.io.*;
import java.util.Vector;

public class SchedulingAlgorithm {

  // Main method for running the Shortest Remaining Time First (SRTF) scheduling algorithm
  public static Results Run(int runtime, Vector<Process> processArr, Results result) {
    int comptime = 0;
    int currentProcess = 0;
    int previousProcess = 0;
    int size = processArr.size();
    int completed = 0;
    boolean repeat = false;

    // File to store the summary of processes
    String resultsFile = "Summary-Processes";

    result.schedulingType = "Preemptive";
    result.schedulingName = "Shortest Remaining Time First";
    try {
      PrintStream out = new PrintStream(new FileOutputStream(resultsFile));

      currentProcess = getNextProcess(processArr); // Get the initial process to execute
      Process process = processArr.get(currentProcess);

      out.println("Process: " + currentProcess + " registered... (" + process.cputime + " " + process.ioblocking + " " + process.cpudone + ")");
      while (comptime < runtime) {
        if (process.cpudone == process.cputime) {
          completed++;
          out.println("Process: " + currentProcess + " completed... (" + process.cputime + " " + process.ioblocking + " " + process.cpudone + ")");
          if (completed == size) {
            result.compuTime = comptime;
            out.close();
            return result;
          }
          if (completed == size - 1) {
            // If only one process is remaining, select it without checking for other processes
            for (int i = 0; i < processArr.size(); i++) {
              if (processArr.get(i).cpudone < processArr.get(i).cputime) {
                currentProcess = i;
                if (processArr.get(i).isBlocked) {
                  processArr.get(i).isBlocked = false;
                }
              }
            }
            process = processArr.get(currentProcess);
            out.println("Process: " + currentProcess + " registered... (" + process.cputime + " " + process.ioblocking + " " + process.cpudone + ")");
          } else {
            // If more than one process is remaining, get the next process based on the shortest remaining time
            currentProcess = getNextProcess(processArr);
            process = processArr.get(currentProcess);
            out.println("Process: " + currentProcess + " registered... (" + process.cputime + " " + process.ioblocking + " " + process.cpudone + ")");
          }
        }

        // Check if the process is in an I/O blocking state
        if (process.ioblocking <= process.ionext) {
          if (!allProcessInaccessible(processArr, currentProcess)) {
            out.println("Process: " + currentProcess + " I/O blocked... ("
                    + process.cputime + " " + process.ioblocking + " " + process.cpudone + ")");
            process.ionext = 0;
            process.numblocked++;
            process.isBlocked = true;
            process.absoluteUnblockingTime = comptime + process.blockingTime;
            currentProcess = getNextProcess(processArr);
            process = processArr.get(currentProcess);
            out.println("Process: " + currentProcess + " registered... (" + process.cputime + " " + process.ioblocking + " " + process.cpudone + ")");
          }
        }

        // Check if any blocked process is ready to unblock
        for (int i = 0; i < processArr.size(); i++) {
          if (processArr.get(i).absoluteUnblockingTime == comptime) {
            processArr.get(i).isBlocked = false;
            processArr.get(i).ionext = 0;
            processArr.get(i).absoluteUnblockingTime = -1;
            repeat = true;
          }
        }

        // Handle interruption by the scheduler
        if (repeat && completed != size) {
          previousProcess = currentProcess;
          currentProcess = getNextProcess(processArr);
          if (previousProcess != currentProcess) {
            out.println("Process: " + previousProcess + " interrupted by scheduler... (" + process.cputime + " " + process.ioblocking + " " + process.cpudone + ")");
          }
          process = processArr.get(currentProcess);
          out.println("Process: " + currentProcess + " registered... (" + process.cputime + " " + process.ioblocking + " " + process.cpudone + ")");
        }

        process.cpudone++;
        if (process.ioblocking > 0) {
          process.ionext++;
        }
        comptime++;
        repeat = false;
      }
      out.close();
    } catch (IOException e) { /* Handle exceptions */ }
    result.compuTime = comptime;
    return result;
  }

  // Helper method to get the index of the next process to execute based on the shortest remaining time
  private static int getNextProcess(List<Process> processVector) {
    int minProcessIndex = 0;
    Process process = processVector.get(minProcessIndex);
    while (process.isBlocked || process.cpudone >= process.cputime) {
      minProcessIndex++;
      process = processVector.get(minProcessIndex);
    }

    int remainingTime = process.cputime - process.cpudone;

    for (int i = 0; i < processVector.size(); i++) {
      if (i == minProcessIndex) {
        continue;
      }
      process = processVector.get(i);
      if (process.cpudone < process.cputime) {
        if (process.cputime - process.cpudone < remainingTime && !process.isBlocked) {
          minProcessIndex = i;
          remainingTime = process.cputime - process.cpudone;
        }
      }
    }
    return minProcessIndex;
  }

  // Helper method to check if all processes are in a blocked state
  private static boolean allProcessInaccessible(List<Process> processVector, int currentProcess) {
    boolean allBlocked = true;
    for (int i = 0; i < processVector.size(); i++) {
      if (!processVector.get(i).isBlocked && processVector.get(i).cpudone < processVector.get(i).cputime) {
        if (i == currentProcess) {
          continue;
        }
        allBlocked = false;
      }
    }
    return allBlocked;
  }
}
