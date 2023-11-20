public class Process {
  public int cputime;
  public int ioblocking;
  public int cpudone;
  public int ionext;
  public int numblocked;
  public int blockingTime;
  public boolean isBlocked;
  public int absoluteUnblockingTime;

  public Process(int cputime, int ioblocking, int cpudone, int ionext, int numblocked, int blockingTime, boolean isBlocked, int absoluteUnblockingTime) {
    this.cputime = cputime;
    this.ioblocking = ioblocking;
    this.cpudone = cpudone;
    this.ionext = ionext;
    this.numblocked = numblocked;
    this.blockingTime = blockingTime;
    this.isBlocked = isBlocked;
    this.absoluteUnblockingTime = absoluteUnblockingTime;
  } 	
}
